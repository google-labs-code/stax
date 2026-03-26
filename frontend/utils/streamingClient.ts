/**
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { JWT_TOKEN_KEY } from "@/config/constants";
import { InferenceChatCompletionPayload, StreamingResponse } from "@/types";

import LocalStorage from "./LocalStorage";

export interface StreamingOptions {
  endpoint?: string;
  payload: InferenceChatCompletionPayload;
  onData?: (content: StreamingResponse) => void;
  onError?: (error: string) => void;
  onStart?: () => void;
  onComplete?: (data: StreamingResponse) => void;
}

export async function createStreamingConnection({
  endpoint,
  payload,
  onData,
  onError,
  onStart,
  onComplete,
}: StreamingOptions): Promise<() => void> {
  let isStreaming = true;

  try {
    onStart?.();

    // First, we need to make a POST request to initiate the stream
    // and get the stream URL, then use EventSource to consume it
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_APP_BASE_URL}/streaming${endpoint || ""}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + LocalStorage.get(JWT_TOKEN_KEY) || "",
        },
        body: JSON.stringify(payload),
      },
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Create EventSource from the response stream
    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error("No response body reader available");
    }

    const decoder = new TextDecoder();
    let buffer = "";

    const processStream = async () => {
      try {
        while (isStreaming) {
          const { done, value } = await reader.read();

          if (done) {
            isStreaming = false;
            break;
          }

          // Decode the chunk and add to buffer
          buffer += decoder.decode(value, { stream: true });

          // Process complete lines (lines ending with \n)
          const lines = buffer.split("\n");

          // Keep the last incomplete line in buffer
          buffer = lines.pop() || "";

          for (const line of lines) {
            if (!isStreaming) break;

            if (line.trim() === "") continue;

            if (line.startsWith("data:")) {
              try {
                const data: StreamingResponse = JSON.parse(line.slice(5));
                if (data.error) {
                  onError?.(data.error);
                  isStreaming = false;
                } else if (data.isComplete) {
                  isStreaming = false;
                  onComplete?.(data);
                } else if (!data.isComplete) {
                  onData?.(data);
                }
              } catch {
                //
              }
            }
          }
        }
      } catch (error) {
        if (isStreaming) {
          isStreaming = false;
          onError?.(
            error instanceof Error ? error.message : "Unknown streaming error",
          );
        }
      }
    };

    // Start processing the stream
    processStream();

    return () => {
      isStreaming = false;
      reader.cancel();
    };
  } catch (error) {
    isStreaming = false;
    onError?.(error instanceof Error ? error.message : "Unknown error");

    return () => {};
  }
}
