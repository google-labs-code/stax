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

import { MainConfig } from "@/config/config";
import { ERROR_MESSAGES, UNAUTHORIZED_ERROR_CODE } from "@/config/constants";
import { backendEndpoints } from "@/config/endpoints";
import { getErrorNotificationConfig } from "@/config/notifications";
import { routes } from "@/config/routes";
import { queryClient } from "@/hooks/useGlobalContext";
import LocalStorage from "@/utils/LocalStorage";
import {
  createRequest,
  deleteRequest,
  getRequest,
  patchRequest,
  postRequest,
  putRequest,
} from "@/utils/apiClient";
import { UserDetails } from "@/utils/userDetailsStorage";
import { notifications } from "@mantine/notifications";
import axios from "axios";
import { stringify } from "qs";

jest.mock("axios");
jest.mock("@mantine/notifications");
jest.mock("@/utils/LocalStorage");
jest.mock("@/utils/userDetailsStorage");
jest.mock("@/hooks/useGlobalContext", () => ({
  queryClient: {
    clear: jest.fn(),
  },
}));
jest.mock("@/config/notifications", () => ({
  getErrorNotificationConfig: jest
    .fn()
    .mockReturnValue({ id: "error-notification" }),
}));

Object.defineProperty(global.crypto, "randomUUID", {
  value: jest.fn().mockReturnValue("mock-uuid"),
  configurable: true,
});

describe("apiClient", () => {
  const originalEnv = process.env;
  const originalLocationHref = window.location.href;
  const originalLocationReplace = window.location.replace;

  beforeAll(() => {
    process.env = {
      ...originalEnv,
      NEXT_PUBLIC_APP_BASE_URL: "https://example.com",
    };

    Object.defineProperty(window, "location", {
      configurable: true,
      value: {
        href: "https://example.com/some-page",
        replace: jest.fn(),
        pathname: "/some-page",
        origin: "https://example.com",
      },
    });

    jest.useFakeTimers();
  });

  afterAll(() => {
    process.env = originalEnv;

    Object.defineProperty(window, "location", {
      configurable: true,
      value: {
        ...window.location,
        href: originalLocationHref,
        replace: originalLocationReplace,
      },
    });

    jest.useRealTimers();
  });

  beforeEach(() => {
    jest.clearAllMocks();

    Object.defineProperty(window.location, "href", {
      writable: true,
      value: "https://example.com/some-page",
    });

    jest.spyOn(Date, "now").mockReturnValue(1234567890);
  });

  describe("createRequest", () => {
    it("should make a request with the correct configuration and JWT token", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      (axios.request as jest.Mock).mockResolvedValue({
        data: { result: "success" },
      });

      const result = await createRequest(
        "GET",
        "/test",
        { param: "value" },
        {},
        true,
      );

      const headers: any = {
        "X-Request-Timestamp": "1234567890",
        "X-Request-ID": "mock-uuid",
        "Cache-Control": "no-cache, no-store, must-revalidate, private",
        Pragma: "no-cache",
        Expires: "0",
        "X-Cache-Status": "disabled",
      };

      if (MainConfig.isAuthEnabled) {
        headers["Authorization"] = "Bearer mock-jwt-token";
      }

      expect(axios.request).toHaveBeenCalledWith({
        url: "/test",
        baseURL: "https://example.com/api",
        timeout: 180000,
        headers,
        data: { param: "value" },
        method: "GET",
        params: { param: "value" },
        paramsSerializer: expect.any(Object),
      });

      expect(result).toEqual({ result: "success" });
    });

    it("should not include Authorization header when no JWT token is available", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(null);

      (axios.request as jest.Mock).mockResolvedValue({
        data: { result: "success" },
      });

      await createRequest("POST", "/test", { data: "value" }, {}, true);

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.not.objectContaining({
            Authorization: expect.anything(),
          }),
        }),
      );
    });

    it("should handle request errors and show error notification", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      const axiosError = {
        response: { data: { error: "Something went wrong" } },
        status: 500,
      };
      (axios.request as jest.Mock).mockRejectedValue(axiosError);

      await expect(createRequest("GET", "/test", {}, {}, true)).rejects.toThrow(
        "Something went wrong",
      );

      expect(notifications.show).toHaveBeenCalled();
      expect(getErrorNotificationConfig).toHaveBeenCalledWith(
        "Something went wrong",
      );
    });

    it("should not show error notification when showErrors is false", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      const axiosError = {
        response: { data: { error: "Something went wrong" } },
        status: 500,
      };
      (axios.request as jest.Mock).mockRejectedValue(axiosError);

      await expect(
        createRequest("GET", "/test", {}, {}, false),
      ).rejects.toThrow("Something went wrong");

      expect(notifications.show).not.toHaveBeenCalled();
    });

    if (MainConfig.isAuthEnabled) {
      it("should handle unauthorized errors and validate token", async () => {
        (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

        const unauthorizedError = {
          response: { data: { error: "Unauthorized" } },
          status: UNAUTHORIZED_ERROR_CODE,
        };
        (axios.request as jest.Mock).mockRejectedValueOnce(unauthorizedError);

        (axios.request as jest.Mock).mockResolvedValueOnce({
          data: { valid: false },
        });

        await expect(
          createRequest("GET", "/test", {}, {}, true),
        ).rejects.toThrow(ERROR_MESSAGES.SESSION_EXPIRED);

        expect(axios.request).toHaveBeenCalledTimes(2);
        expect(axios.request).toHaveBeenNthCalledWith(
          2,
          expect.objectContaining({
            url: backendEndpoints.AUTH.VALIDATE_TOKEN,
          }),
        );

        expect(queryClient.clear).toHaveBeenCalled();
        expect(UserDetails.remove).toHaveBeenCalled();
        expect(LocalStorage.clear).toHaveBeenCalled();

        jest.advanceTimersByTime(1500);
        expect(window.location.replace).toHaveBeenCalledWith(routes.signin);
      });

      it("should not redirect if token is still valid", async () => {
        (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

        const unauthorizedError = {
          response: { data: { error: "Unauthorized" } },
          status: UNAUTHORIZED_ERROR_CODE,
        };
        (axios.request as jest.Mock).mockRejectedValueOnce(unauthorizedError);

        (axios.request as jest.Mock).mockResolvedValueOnce({
          data: { valid: true },
        });

        await expect(
          createRequest("GET", "/test", {}, {}, true),
        ).rejects.toThrow(ERROR_MESSAGES.SESSION_EXPIRED);

        expect(axios.request).toHaveBeenCalledTimes(2);

        expect(queryClient.clear).not.toHaveBeenCalled();
        expect(UserDetails.remove).not.toHaveBeenCalled();
        expect(LocalStorage.clear).not.toHaveBeenCalled();

        jest.advanceTimersByTime(1500);
        expect(window.location.replace).not.toHaveBeenCalled();
      });
    }

    it("should not redirect if already on signin page", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      Object.defineProperty(window.location, "href", {
        writable: true,
        value: routes.signin,
      });

      const unauthorizedError = {
        response: { data: { error: "Unauthorized" } },
        status: UNAUTHORIZED_ERROR_CODE,
      };
      (axios.request as jest.Mock).mockRejectedValueOnce(unauthorizedError);

      (axios.request as jest.Mock).mockResolvedValueOnce({
        data: { valid: false },
      });

      await expect(createRequest("GET", "/test", {}, {}, true)).rejects.toThrow(
        ERROR_MESSAGES.SESSION_EXPIRED,
      );

      jest.advanceTimersByTime(1500);

      expect(window.location.replace).not.toHaveBeenCalled();
    });
  });

  describe("getRequest", () => {
    it("should make a GET request with the correct parameters", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      const mockResponse = MainConfig.isAuthEnabled
        ? { result: "success" }
        : { valid: false };
      (axios.request as jest.Mock).mockResolvedValue({
        data: mockResponse,
      });

      const result = await getRequest(
        "/test",
        { param: "value" },
        { "Custom-Header": "value" },
        false,
      );

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "GET",
          url: "/test",
          params: { param: "value" },
          headers: expect.objectContaining({
            "Custom-Header": "value",
          }),
        }),
      );

      expect(result).toEqual(mockResponse);
    });

    it("should properly handle type parameters", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      (axios.request as jest.Mock).mockResolvedValue({ data: { value: 123 } });

      const result = await getRequest<{ value: number }>("/test");

      expect(result).toEqual({ value: 123 });
    });
  });

  describe("postRequest", () => {
    it("should make a POST request with the correct parameters", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      const mockedResponse = MainConfig.isAuthEnabled
        ? { result: "success" }
        : { valid: false };

      (axios.request as jest.Mock).mockResolvedValue({
        data: mockedResponse,
      });

      const result = await postRequest("/test", { data: "value" });

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "POST",
          url: "/test",
          data: { data: "value" },
        }),
      );

      expect(result).toEqual(mockedResponse);
    });
  });

  describe("deleteRequest", () => {
    it("should make a DELETE request with the correct parameters", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      (axios.request as jest.Mock).mockResolvedValue({
        data: { result: "success" },
      });

      const result = await deleteRequest("/test", { id: "123" });

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "DELETE",
          url: "/test",
          data: { id: "123" },
        }),
      );

      expect(result).toEqual({ result: "success" });
    });
  });

  describe("patchRequest", () => {
    it("should make a PATCH request with the correct parameters", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      (axios.request as jest.Mock).mockResolvedValue({
        data: { result: "success" },
      });

      const result = await patchRequest("/test", { update: "value" });

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "PATCH",
          url: "/test",
          data: { update: "value" },
        }),
      );

      expect(result).toEqual({ result: "success" });
    });
  });

  describe("putRequest", () => {
    it("should make a PUT request with the correct parameters", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      (axios.request as jest.Mock).mockResolvedValue({
        data: { result: "success" },
      });

      const result = await putRequest("/test", { replace: "value" });

      expect(axios.request).toHaveBeenCalledWith(
        expect.objectContaining({
          method: "PUT",
          url: "/test",
          data: { replace: "value" },
        }),
      );

      expect(result).toEqual({ result: "success" });
    });
  });

  describe("params serialization", () => {
    it("should correctly serialize parameters for GET requests", async () => {
      (LocalStorage.get as jest.Mock).mockReturnValue("mock-jwt-token");

      (axios.request as jest.Mock).mockResolvedValue({
        data: { result: "success" },
      });

      await getRequest("/test", { tags: ["tag1", "tag2"] });

      const axiosCallArgs = (axios.request as jest.Mock).mock.calls[0][0];
      const serializeFunc = axiosCallArgs.paramsSerializer.serialize;

      const params = { tags: ["tag1", "tag2"] };
      const result = serializeFunc(params);

      expect(result).toEqual(stringify(params, { arrayFormat: "comma" }));
    });
  });
});
