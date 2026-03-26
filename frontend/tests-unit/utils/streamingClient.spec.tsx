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

import { StreamingResponse } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import { createStreamingConnection } from "@/utils/streamingClient";

// Polyfill TextDecoder for Jest environment
if (typeof TextDecoder === "undefined") {
  global.TextDecoder = class TextDecoder {
    decode(input: any, options?: any) {
      if (options?.stream) {
        // For streaming, we need to handle partial chunks
        return Buffer.from(input).toString("utf-8");
      }

      return Buffer.from(input).toString("utf-8");
    }
  } as any;
}

// Mock LocalStorage
jest.mock("@/utils/LocalStorage", () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
  },
}));

// Mock environment variable
const originalEnv = process.env;

describe("createStreamingConnection", () => {
  const mockPayload = {
    model_id: "test-model",
    prompts: [],
  };

  let mockReader: any;
  let mockRead: jest.Mock;
  let mockCancel: jest.Mock;
  let mockResponse: any;
  let mockFetch: jest.Mock;

  beforeEach(() => {
    jest.clearAllMocks();
    process.env = {
      ...originalEnv,
      NEXT_PUBLIC_APP_BASE_URL: "https://test.example.com",
    };

    // Setup mock reader
    mockRead = jest.fn();
    mockCancel = jest.fn();
    mockReader = {
      read: mockRead,
      cancel: mockCancel,
    };

    // Setup mock response
    const mockBody = {
      getReader: jest.fn().mockReturnValue(mockReader),
    };
    mockResponse = {
      ok: true,
      status: 200,
      body: mockBody as any,
    } as Response;

    // Setup mock fetch
    mockFetch = jest.fn().mockResolvedValue(mockResponse);
    global.fetch = mockFetch;

    // Mock LocalStorage.get
    (LocalStorage.get as jest.Mock).mockReturnValue("test-token");
  });

  afterEach(() => {
    process.env = originalEnv;
  });

  describe("initialization", () => {
    it("calls onStart callback", async () => {
      const onStart = jest.fn();
      const onData = jest.fn();
      const onError = jest.fn();
      const onComplete = jest.fn();

      // Setup stream to complete immediately
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onStart,
        onData,
        onError,
        onComplete,
      });

      expect(onStart).toHaveBeenCalledTimes(1);
    });

    it("makes POST request to correct endpoint", async () => {
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        endpoint: "/test-endpoint",
        payload: mockPayload,
      });

      expect(mockFetch).toHaveBeenCalledWith(
        "https://test.example.com/streaming/test-endpoint",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer test-token",
          },
          body: JSON.stringify(mockPayload),
        },
      );
    });

    it("uses default endpoint when endpoint is not provided", async () => {
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
      });

      expect(mockFetch).toHaveBeenCalledWith(
        "https://test.example.com/streaming",
        expect.any(Object),
      );
    });

    it("includes Authorization header with JWT token", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("my-jwt-token");
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer my-jwt-token",
          }),
        }),
      );
    });

    it("handles missing JWT token", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(null);
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer null",
          }),
        }),
      );
    });
  });

  describe("error handling", () => {
    it("calls onError when fetch fails", async () => {
      const onError = jest.fn();
      mockFetch.mockRejectedValueOnce(new Error("Network error"));

      await createStreamingConnection({
        payload: mockPayload,
        onError,
      });

      await new Promise((resolve) => setTimeout(resolve, 10));

      expect(onError).toHaveBeenCalledWith("Network error");
    });

    it("calls onError when response is not ok", async () => {
      const onError = jest.fn();
      mockResponse.ok = false;
      mockResponse.status = 500;

      await createStreamingConnection({
        payload: mockPayload,
        onError,
      });

      await new Promise((resolve) => setTimeout(resolve, 10));

      expect(onError).toHaveBeenCalledWith("HTTP error! status: 500");
    });

    it("calls onError when response body reader is not available", async () => {
      const onError = jest.fn();
      mockResponse.body = null;

      await createStreamingConnection({
        payload: mockPayload,
        onError,
      });

      await new Promise((resolve) => setTimeout(resolve, 10));

      expect(onError).toHaveBeenCalledWith("No response body reader available");
    });

    it("calls onError when stream processing fails", async () => {
      const onError = jest.fn();
      mockRead.mockRejectedValueOnce(new Error("Stream read error"));

      await createStreamingConnection({
        payload: mockPayload,
        onError,
      });

      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(onError).toHaveBeenCalledWith("Stream read error");
    });

    it("handles non-Error objects in catch blocks", async () => {
      const onError = jest.fn();
      mockFetch.mockRejectedValueOnce("String error");

      await createStreamingConnection({
        payload: mockPayload,
        onError,
      });

      await new Promise((resolve) => setTimeout(resolve, 10));

      expect(onError).toHaveBeenCalledWith("Unknown error");
    });
  });

  describe("stream processing", () => {
    it("processes streaming data chunks correctly", async () => {
      const onData = jest.fn();

      const data1: StreamingResponse = {
        content: "Hello",
        isComplete: false,
        error: "",
        usage: {},
        latency: 100,
        model: "test-model",
        finishReason: "",
      };

      const data2: StreamingResponse = {
        content: " World",
        isComplete: false,
        error: "",
        usage: {},
        latency: 150,
        model: "test-model",
        finishReason: "",
      };

      // First chunk: "data: {...}\n"
      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(Buffer.from(`data: ${JSON.stringify(data1)}\n`)),
      });

      // Second chunk: "data: {...}\n"
      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(Buffer.from(`data: ${JSON.stringify(data2)}\n`)),
      });

      // Stream ends
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onData,
      });

      // Wait for async stream processing
      await new Promise((resolve) => setTimeout(resolve, 100));

      expect(onData).toHaveBeenCalledTimes(2);
      expect(onData).toHaveBeenNthCalledWith(1, data1);
      expect(onData).toHaveBeenNthCalledWith(2, data2);
    });

    it("handles incomplete lines in buffer", async () => {
      const onData = jest.fn();
      const dataString = JSON.stringify({
        content: "Complete",
        isComplete: false,
        error: "",
        usage: {},
        latency: 100,
        model: "test-model",
        finishReason: "",
      });

      const data: StreamingResponse = {
        content: "Complete",
        isComplete: false,
        error: "",
        usage: {},
        latency: 100,
        model: "test-model",
        finishReason: "",
      };

      // First chunk: incomplete line
      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(Buffer.from(`data: ${dataString.slice(0, 10)}`)),
      });

      // Second chunk: completes the line
      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(Buffer.from(`${dataString.slice(10)}\n`)),
      });

      // Stream ends
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onData,
      });

      await new Promise((resolve) => setTimeout(resolve, 100));

      expect(onData).toHaveBeenCalledTimes(1);
      expect(onData).toHaveBeenCalledWith(data);
    });

    it("calls onComplete when isComplete is true", async () => {
      const onComplete = jest.fn();
      const onData = jest.fn();

      const completeData: StreamingResponse = {
        content: "Final content",
        isComplete: true,
        error: "",
        usage: { tokens: 100 },
        latency: 200,
        model: "test-model",
        finishReason: "stop",
      };

      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(
          Buffer.from(`data: ${JSON.stringify(completeData)}\n`),
        ),
      });

      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onData,
        onComplete,
      });

      await new Promise((resolve) => setTimeout(resolve, 100));

      expect(onComplete).toHaveBeenCalledTimes(1);
      expect(onComplete).toHaveBeenCalledWith(completeData);
      expect(onData).not.toHaveBeenCalled();
    });

    it("calls onError when stream contains error", async () => {
      const onError = jest.fn();
      const onData = jest.fn();

      const errorData: StreamingResponse = {
        content: "",
        isComplete: false,
        error: "Stream error occurred",
        usage: {},
        latency: null,
        model: "",
        finishReason: "",
      };

      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(
          Buffer.from(`data: ${JSON.stringify(errorData)}\n`),
        ),
      });

      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onData,
        onError,
      });

      await new Promise((resolve) => setTimeout(resolve, 100));

      expect(onError).toHaveBeenCalledTimes(1);
      expect(onError).toHaveBeenCalledWith("Stream error occurred");
      expect(onData).not.toHaveBeenCalled();
    });

    it("ignores empty lines", async () => {
      const onData = jest.fn();

      const data: StreamingResponse = {
        content: "Test",
        isComplete: false,
        error: "",
        usage: {},
        latency: 100,
        model: "test-model",
        finishReason: "",
      };

      // Stream with empty lines
      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(
          Buffer.from(`\n\ndata: ${JSON.stringify(data)}\n\n`),
        ),
      });

      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onData,
      });

      await new Promise((resolve) => setTimeout(resolve, 100));

      expect(onData).toHaveBeenCalledTimes(1);
      expect(onData).toHaveBeenCalledWith(data);
    });

    it("ignores lines that do not start with 'data:'", async () => {
      const onData = jest.fn();

      const data: StreamingResponse = {
        content: "Test",
        isComplete: false,
        error: "",
        usage: {},
        latency: 100,
        model: "test-model",
        finishReason: "",
      };

      // Stream with non-data lines
      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(
          Buffer.from(
            `event: message\ndata: ${JSON.stringify(data)}\ncomment: test\n`,
          ),
        ),
      });

      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onData,
      });

      await new Promise((resolve) => setTimeout(resolve, 100));

      expect(onData).toHaveBeenCalledTimes(1);
      expect(onData).toHaveBeenCalledWith(data);
    });

    it("handles malformed JSON gracefully", async () => {
      const onData = jest.fn();
      const onError = jest.fn();

      // Stream with malformed JSON
      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(Buffer.from(`data: {invalid json}\n`)),
      });

      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onData,
        onError,
      });

      await new Promise((resolve) => setTimeout(resolve, 100));

      // Should not crash, just ignore the malformed line
      expect(onData).not.toHaveBeenCalled();
      expect(onError).not.toHaveBeenCalled();
    });

    it("stops processing when stream is cancelled", async () => {
      const onData = jest.fn();

      const data: StreamingResponse = {
        content: "Test",
        isComplete: false,
        error: "",
        usage: {},
        latency: 100,
        model: "test-model",
        finishReason: "",
      };

      // First chunk
      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(Buffer.from(`data: ${JSON.stringify(data)}\n`)),
      });

      // Second chunk - but we'll cancel before processing
      mockRead.mockImplementation(() => new Promise(() => {})); // Never resolves

      const cancel = await createStreamingConnection({
        payload: mockPayload,
        onData,
      });

      await new Promise((resolve) => setTimeout(resolve, 50));

      // Cancel the stream
      cancel();

      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(mockCancel).toHaveBeenCalled();
    });
  });

  describe("cleanup function", () => {
    it("returns a cleanup function", async () => {
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      const cancel = await createStreamingConnection({
        payload: mockPayload,
      });

      expect(typeof cancel).toBe("function");
    });

    it("cleanup function cancels the reader", async () => {
      mockRead.mockImplementation(() => new Promise(() => {})); // Never resolves

      const cancel = await createStreamingConnection({
        payload: mockPayload,
      });

      // Wait a bit for the stream to start
      await new Promise((resolve) => setTimeout(resolve, 50));

      cancel();

      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(mockCancel).toHaveBeenCalled();
    });

    it("cleanup function prevents further processing", async () => {
      const onData = jest.fn();

      mockRead.mockImplementation(() => new Promise(() => {})); // Never resolves

      const cancel = await createStreamingConnection({
        payload: mockPayload,
        onData,
      });

      await new Promise((resolve) => setTimeout(resolve, 50));

      cancel();

      // Simulate a late-arriving chunk (should be ignored)
      await new Promise((resolve) => setTimeout(resolve, 50));

      expect(onData).not.toHaveBeenCalled();
    });

    it("returns empty cleanup function when initialization fails", async () => {
      mockFetch.mockRejectedValueOnce(new Error("Init error"));

      const cancel = await createStreamingConnection({
        payload: mockPayload,
      });

      expect(typeof cancel).toBe("function");
      // Should not throw when called
      expect(() => cancel()).not.toThrow();
    });
  });

  describe("optional callbacks", () => {
    it("works without any callbacks", async () => {
      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await expect(
        createStreamingConnection({
          payload: mockPayload,
        }),
      ).resolves.toBeDefined();
    });

    it("works with only onData callback", async () => {
      const onData = jest.fn();

      const data: StreamingResponse = {
        content: "Test",
        isComplete: false,
        error: "",
        usage: {},
        latency: 100,
        model: "test-model",
        finishReason: "",
      };

      mockRead.mockResolvedValueOnce({
        done: false,
        value: new Uint8Array(Buffer.from(`data: ${JSON.stringify(data)}\n`)),
      });

      mockRead.mockResolvedValueOnce({ done: true, value: undefined });

      await createStreamingConnection({
        payload: mockPayload,
        onData,
      });

      await new Promise((resolve) => setTimeout(resolve, 100));

      expect(onData).toHaveBeenCalledTimes(1);
    });
  });
});
