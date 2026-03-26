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

import axios from "axios";
import { NextRequest } from "next/server";

// Force dynamic rendering to prevent caching
export const dynamic = "force-dynamic";
export const revalidate = 0;
export const fetchCache = "force-no-store";
export const runtime = "nodejs";
export const preferredRegion = "auto";

export async function POST(request: NextRequest) {
  const errorStreamData = (error: string) => {
    return `data:${JSON.stringify({
      error,
    })}\n`;
  };

  try {
    const body = await request.json();
    const relativeEndpoint = request.url.split("/streaming/")[1];

    const headers: Record<string, string> = {
      "Content-Type": "application/json",
      Accept: "text/event-stream",
    };

    // Forward authorization header if present
    const authHeader = request.headers.get("Authorization");
    if (authHeader) {
      headers["Authorization"] = authHeader;
    }

    // Create a readable stream for Server-Sent Events
    const stream = new ReadableStream({
      async start(controller) {
        try {
          const response = await axios({
            method: "POST",
            headers,
            data: body,
            responseType: "stream",
            url: relativeEndpoint,
            baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
          });

          // Handle non-200 status codes
          if (response.status !== 200) {
            controller.enqueue(
              new TextEncoder().encode(
                errorStreamData(`Backend error: ${response.status}`),
              ),
            );
            controller.close();

            return;
          }

          // Forward stream data as proper Server-Sent Events
          response.data.on("data", (chunk: Buffer) => {
            const chunkStr = chunk.toString();

            // Format as proper Server-Sent Events
            controller.enqueue(new TextEncoder().encode(chunkStr));
          });

          response.data.on("end", () => controller.close());
          response.data.on("error", () => {
            controller.enqueue(
              new TextEncoder().encode(errorStreamData("Stream error")),
            );
            controller.close();
          });
        } catch (error) {
          controller.enqueue(
            new TextEncoder().encode(
              errorStreamData(
                `Request failed: ${error instanceof Error ? error.message : "Unknown error"}`,
              ),
            ),
          );
          controller.close();
        }
      },
    });

    return new Response(stream, {
      headers: {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache, no-store, must-revalidate",
        Connection: "keep-alive",
      },
    });
  } catch (error: any) {
    return new Response(
      JSON.stringify({
        error: "Streaming route error",
        details: error.message,
      }),
      {
        status: 500,
        headers: { "Content-Type": "application/json" },
      },
    );
  }
}
