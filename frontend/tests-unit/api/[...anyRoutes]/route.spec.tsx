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

import { DELETE, GET, PATCH, POST, PUT } from "@/app/api/[...anyRoutes]/route";
import axios from "axios";
import { NextResponse } from "next/server";

// Mock axios
jest.mock("axios");
jest.mock("next/server", () => ({
  NextResponse: {
    json: jest.fn((data, config) => ({
      data,
      config,
      headers: {
        set: jest.fn(),
        get: jest.fn(),
      },
      status: config?.status || 200,
    })),
  },
}));

describe("API Route Handler", () => {
  const mockBaseUrl = "https://api.example.com";
  const mockTimestamp = 1234567890;

  beforeEach(() => {
    jest.clearAllMocks();

    // Set environment variables
    process.env.NEXT_PUBLIC_API_BASE_URL = mockBaseUrl;

    // Mock Date.now() and Math.random()
    jest.spyOn(Date, "now").mockReturnValue(mockTimestamp);
    jest.spyOn(Math, "random").mockReturnValue(0.123456789); // Will produce mockRandomId substring

    // Mock axios.request
    (axios.request as jest.Mock).mockResolvedValue({
      data: { success: true, message: "Test response" },
      status: 200,
      statusText: "OK",
    });
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  const createMockRequest = (
    method: string = "GET",
    url: string = "http://localhost:3000/api/test",
    headers: Record<string, string> = {},
    body: any = null,
  ): Request => {
    const mockHeaders = new Map([
      ...Object.entries(headers),
      ["content-type", headers["content-type"] || "application/json"],
    ]);

    const mockRequest = {
      method,
      url,
      headers: {
        get: (name: string) => mockHeaders.get(name.toLowerCase()),
      },
      json: jest.fn().mockResolvedValue(body),
      formData: jest.fn().mockResolvedValue(new FormData()),
      text: jest.fn().mockResolvedValue(JSON.stringify(body)),
    } as unknown as Request;

    return mockRequest;
  };

  describe("HTTP Methods", () => {
    it("should handle GET requests", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/users",
      );

      const response = await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "GET",
          url: "users",
          baseURL: mockBaseUrl,
        }),
      );
      expect(response).toBeDefined();
    });

    it("should handle POST requests", async () => {
      const body = { name: "John", email: "john@example.com" };
      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/users",
        { "content-type": "application/json" },
        body,
      );

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "POST",
          url: "users",
          baseURL: mockBaseUrl,
          data: body,
        }),
      );
    });

    it("should handle DELETE requests", async () => {
      const request = createMockRequest(
        "DELETE",
        "http://localhost:3000/api/users/1",
      );

      await DELETE(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "DELETE",
          url: "users/1",
          baseURL: mockBaseUrl,
        }),
      );
    });

    it("should handle PUT requests", async () => {
      const body = { name: "Jane" };
      const request = createMockRequest(
        "PUT",
        "http://localhost:3000/api/users/1",
        { "content-type": "application/json" },
        body,
      );

      await PUT(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "PUT",
          url: "users/1",
          baseURL: mockBaseUrl,
          data: body,
        }),
      );
    });

    it("should handle PATCH requests", async () => {
      const body = { status: "active" };
      const request = createMockRequest(
        "PATCH",
        "http://localhost:3000/api/users/1",
        { "content-type": "application/json" },
        body,
      );

      await PATCH(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "PATCH",
          url: "users/1",
          baseURL: mockBaseUrl,
          data: body,
        }),
      );
    });
  });

  describe("Header Handling", () => {
    it("should forward Authorization header", async () => {
      const authToken = "Bearer token123";
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
        {
          authorization: authToken,
        },
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: authToken,
          }),
        }),
      );
    });

    it("should forward Content-Type header", async () => {
      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/test",
        {
          "content-type": "application/json",
        },
      );

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({
            "Content-Type": "application/json",
          }),
        }),
      );
    });

    it("should include cache-busting headers", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({
            "X-Request-Timestamp": mockTimestamp.toString(),
            "X-Request-ID": expect.any(String),
          }),
        }),
      );
    });

    it("should add cache control headers to response", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );
      const mockSetHeader = jest.fn();

      (NextResponse.json as jest.Mock).mockReturnValue({
        headers: {
          set: mockSetHeader,
        },
      });

      await GET(request);

      // Check that cache control headers are set
      expect(mockSetHeader).toHaveBeenCalledWith(
        "Cache-Control",
        "no-cache, no-store, must-revalidate, private",
      );
      expect(mockSetHeader).toHaveBeenCalledWith("Pragma", "no-cache");
      expect(mockSetHeader).toHaveBeenCalledWith("Expires", "0");
      expect(mockSetHeader).toHaveBeenCalledWith("X-Cache-Status", "disabled");
    });

    it("should not forward missing Authorization header", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
        {},
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.not.objectContaining({
            Authorization: expect.any(String),
          }),
        }),
      );
    });
  });

  describe("Request Payload Handling", () => {
    it("should handle JSON request payload", async () => {
      const body = { name: "John", age: 30 };
      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/users",
        { "content-type": "application/json" },
        body,
      );

      (request.json as jest.Mock).mockResolvedValue(body);

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: body,
        }),
      );
    });

    it("should handle form-urlencoded request payload", async () => {
      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/form",
        { "content-type": "application/x-www-form-urlencoded" },
      );

      const mockFormData = new Map([
        ["name", "John"],
        ["email", "john@example.com"],
      ]);

      (request.formData as jest.Mock).mockResolvedValue(mockFormData);

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.any(Object),
        }),
      );
    });

    it("should handle text request payload", async () => {
      const textBody = "plain text content";
      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/text",
        { "content-type": "text/plain" },
      );

      (request.text as jest.Mock).mockResolvedValue(textBody);

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: textBody,
        }),
      );
    });

    it("should handle request with no payload", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
        {},
      );

      (request.json as jest.Mock).mockRejectedValue(new Error("No body"));
      (request.formData as jest.Mock).mockRejectedValue(new Error("No form"));
      (request.text as jest.Mock).mockRejectedValue(new Error("No text"));

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "GET",
        }),
      );
    });

    it("should handle multipart form data", async () => {
      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/upload",
        { "content-type": "multipart/form-data" },
      );

      await POST(request);

      expect(axios.request).toHaveBeenCalled();
    });
  });

  describe("URL Endpoint Resolution", () => {
    it("should correctly extract endpoint from request URL", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/users/123/profile",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: "users/123/profile",
        }),
      );
    });

    it("should handle nested endpoints", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/projects/1/tasks/2/comments",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: "projects/1/tasks/2/comments",
        }),
      );
    });

    it("should handle query parameters in URL", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/users?page=1&limit=10",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: "users?page=1&limit=10",
        }),
      );
    });
  });

  describe("Timeout Configuration", () => {
    it("should set 3 minute timeout for requests", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          timeout: 180000, // 3 minutes in milliseconds
        }),
      );
    });
  });

  describe("Successful Responses", () => {
    it("should return successful response with correct data", async () => {
      const responseData = { success: true, data: { id: 1, name: "Test" } };

      (axios.request as jest.Mock).mockResolvedValue({
        data: responseData,
      });

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(responseData);
    });

    it("should return response with array data", async () => {
      const responseData = [
        { id: 1, name: "Item 1" },
        { id: 2, name: "Item 2" },
      ];

      (axios.request as jest.Mock).mockResolvedValue({
        data: responseData,
      });

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/items",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(responseData);
    });

    it("should return response with null data", async () => {
      (axios.request as jest.Mock).mockResolvedValue({
        data: null,
      });

      const request = createMockRequest(
        "DELETE",
        "http://localhost:3000/api/test/1",
      );

      await DELETE(request);

      expect(NextResponse.json).toHaveBeenCalledWith(null);
    });
  });

  describe("Error Handling - Response Errors", () => {
    it("should handle error response from backend", async () => {
      const errorData = { error: "User not found" };
      const mockError = new Error("Request failed");
      (mockError as any).response = {
        status: 404,
        data: errorData,
      };

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/users/999",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          error: "User not found",
          status: 404,
        }),
        expect.objectContaining({
          status: 404,
        }),
      );
    });

    it("should handle 400 Bad Request error", async () => {
      const mockError = new Error("Bad request");
      (mockError as any).response = {
        status: 400,
        data: { message: "Invalid input" },
      };

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/test",
        { "content-type": "application/json" },
      );

      await POST(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          status: 400,
        }),
        expect.objectContaining({
          status: 400,
        }),
      );
    });

    it("should handle 401 Unauthorized error", async () => {
      const mockError = new Error("Unauthorized");
      (mockError as any).response = {
        status: 401,
        data: { error: "Invalid token" },
      };

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/protected",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          error: "Invalid token",
          status: 401,
        }),
        expect.objectContaining({
          status: 401,
        }),
      );
    });

    it("should handle 500 Internal Server Error", async () => {
      const mockError = new Error("Server error");
      (mockError as any).response = {
        status: 500,
        data: { error: "Internal server error" },
      };

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          error: "Internal server error",
          status: 500,
        }),
        expect.objectContaining({
          status: 500,
        }),
      );
    });

    it("should use message field if error field is not present", async () => {
      const mockError = new Error("Request failed");
      (mockError as any).response = {
        status: 400,
        data: { message: "Validation failed" },
      };

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/test",
      );

      await POST(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          error: "Validation failed",
        }),
        expect.any(Object),
      );
    });

    it("should use error.message as fallback", async () => {
      const mockError = new Error("Network timeout");
      (mockError as any).response = {
        status: 500,
        data: {},
      };

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          error: "Network timeout",
        }),
        expect.any(Object),
      );
    });
  });

  describe("Error Handling - Request Errors", () => {
    it("should handle network request errors", async () => {
      const mockError = new Error("Network error");
      (mockError as any).request = {}; // This indicates request was made but no response
      (mockError as any).reason = "No response from server";

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          error: "No response from server",
          status: 503,
        }),
        expect.objectContaining({
          status: 503,
        }),
      );
    });

    it("should return 503 for request errors", async () => {
      const mockError = new Error("Connection refused");
      (mockError as any).request = {};

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.any(Object),
        expect.objectContaining({
          status: 503,
        }),
      );
    });
  });

  describe("Error Handling - Setup Errors", () => {
    it("should handle request setup errors", async () => {
      const setupError = new Error("Invalid configuration");

      (axios.request as jest.Mock).mockRejectedValue(setupError);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          error: "Invalid configuration",
          status: 400,
        }),
        expect.objectContaining({
          status: 400,
        }),
      );
    });

    it("should handle unknown errors", async () => {
      const unknownError = new Error("Unknown error occurred");

      (axios.request as jest.Mock).mockRejectedValue(unknownError);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith(
        expect.objectContaining({
          error: "Unknown error occurred",
        }),
        expect.any(Object),
      );
    });
  });

  describe("Error Response Headers", () => {
    it("should add cache control headers to error responses", async () => {
      const mockError = new Error("Server error");
      (mockError as any).response = {
        status: 500,
        data: { error: "Internal error" },
      };

      (axios.request as jest.Mock).mockRejectedValue(mockError);

      const mockSetHeader = jest.fn();

      (NextResponse.json as jest.Mock).mockReturnValue({
        headers: {
          set: mockSetHeader,
        },
      });

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(mockSetHeader).toHaveBeenCalledWith(
        "Cache-Control",
        "no-cache, no-store, must-revalidate, private",
      );
      expect(mockSetHeader).toHaveBeenCalledWith("Pragma", "no-cache");
      expect(mockSetHeader).toHaveBeenCalledWith("Expires", "0");
      expect(mockSetHeader).toHaveBeenCalledWith("X-Cache-Status", "disabled");
    });
  });

  describe("Base URL Configuration", () => {
    it("should use NEXT_PUBLIC_API_BASE_URL from environment", async () => {
      const customBaseUrl = "https://custom-api.example.com";
      process.env.NEXT_PUBLIC_API_BASE_URL = customBaseUrl;

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          baseURL: customBaseUrl,
        }),
      );
    });

    it("should handle missing base URL", async () => {
      delete process.env.NEXT_PUBLIC_API_BASE_URL;

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          baseURL: undefined,
        }),
      );
    });
  });

  describe("Request Configuration", () => {
    it("should convert method to uppercase", async () => {
      const request = createMockRequest(
        "post",
        "http://localhost:3000/api/test",
      );

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "POST",
        }),
      );
    });

    it("should include all required config properties", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: expect.any(String),
          url: expect.any(String),
          baseURL: expect.any(String),
          headers: expect.any(Object),
          timeout: 180000,
        }),
      );
    });
  });

  describe("Cache Busting", () => {
    it("should generate unique X-Request-ID for each request", async () => {
      const request1 = createMockRequest(
        "GET",
        "http://localhost:3000/api/test1",
      );
      const request2 = createMockRequest(
        "GET",
        "http://localhost:3000/api/test2",
      );

      jest.spyOn(Math, "random").mockReturnValueOnce(0.111111111);
      await GET(request1);

      jest.spyOn(Math, "random").mockReturnValueOnce(0.222222222);
      await GET(request2);

      const calls = (axios.request as jest.Mock).mock.calls;
      const id1 = calls[0][0].headers["X-Request-ID"];
      const id2 = calls[1][0].headers["X-Request-ID"];

      expect(id1).not.toBe(id2);
    });

    it("should include current timestamp in headers", async () => {
      const testTimestamp = Date.now();
      jest.spyOn(Date, "now").mockReturnValue(testTimestamp);

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({
            "X-Request-Timestamp": testTimestamp.toString(),
          }),
        }),
      );
    });
  });

  describe("Edge Cases", () => {
    it("should handle empty response data", async () => {
      (axios.request as jest.Mock).mockResolvedValue({
        data: {},
      });

      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/test",
      );

      await GET(request);

      expect(NextResponse.json).toHaveBeenCalledWith({});
    });

    it("should handle very large payload", async () => {
      const largeBody = { data: "x".repeat(1000000) };

      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/upload",
        { "content-type": "application/json" },
        largeBody,
      );

      (request.json as jest.Mock).mockResolvedValue(largeBody);

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          data: largeBody,
        }),
      );
    });

    it("should handle special characters in URL", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/search?q=hello%20world&filter=test%2B",
      );

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          url: "search?q=hello%20world&filter=test%2B",
        }),
      );
    });

    it("should handle headers with multiple values", async () => {
      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/test",
        {
          "content-type": "application/json",
          authorization: "Bearer token",
          "x-custom-header": "custom-value",
        },
      );

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: "Bearer token",
            "Content-Type": "application/json",
          }),
        }),
      );
    });
  });

  describe("Integration Tests", () => {
    it("should handle complete POST request flow with authentication", async () => {
      const body = { username: "john", password: "secret" };
      const responseData = { token: "jwt-token-123" };

      (axios.request as jest.Mock).mockResolvedValue({
        data: responseData,
      });

      const request = createMockRequest(
        "POST",
        "http://localhost:3000/api/auth/login",
        { "content-type": "application/json" },
        body,
      );

      (request.json as jest.Mock).mockResolvedValue(body);

      await POST(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "POST",
          url: "auth/login",
          data: body,
        }),
      );

      expect(NextResponse.json).toHaveBeenCalledWith(responseData);
    });

    it("should handle complete GET with query parameters", async () => {
      const request = createMockRequest(
        "GET",
        "http://localhost:3000/api/users?page=1&limit=20&sort=name",
      );

      (axios.request as jest.Mock).mockResolvedValue({
        data: { users: [], total: 100 },
      });

      await GET(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "GET",
          url: "users?page=1&limit=20&sort=name",
        }),
      );
    });

    it("should handle DELETE with no body", async () => {
      const request = createMockRequest(
        "DELETE",
        "http://localhost:3000/api/users/123",
      );

      (axios.request as jest.Mock).mockResolvedValue({
        data: { success: true },
      });

      await DELETE(request);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "DELETE",
          url: "users/123",
        }),
      );
    });
  });
});
