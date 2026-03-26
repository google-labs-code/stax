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

import {
  acceptTos,
  signInWithGoogle,
} from "@/app/(unauthRoutes)/login/state/queries";
import { backendEndpoints } from "@/config/endpoints";
import { postRequest, postRequestWithoutCache } from "@/utils/apiClient";

jest.mock("@/utils/apiClient", () => ({
  __esModule: true,
  default: "mockedDefaultExport",
  postRequest: jest.fn().mockReturnValue(Promise.resolve({})),
  postRequestWithoutCache: jest.fn(),
}));

describe("Login Queries", () => {
  describe("signInWithGoogle", () => {
    it("should post data with google auth data", async () => {
      const data = {
        authCode: "test",
        redirectUrl: "http://localhost",
      };

      (postRequestWithoutCache as jest.Mock).mockResolvedValueOnce({
        success: true,
      });

      await signInWithGoogle(data);

      expect(postRequestWithoutCache).toHaveBeenCalledWith(
        backendEndpoints.AUTH.GOOGLE_AUTH_CODE,
        data,
      );
    });
  });

  describe("acceptTos", () => {
    it("should post data with TOS token data when tosId exists", async () => {
      const data = { token: "tos-token", tosId: "tos-id-123131" };
      await acceptTos(data);

      expect(postRequest).toHaveBeenCalledWith(
        backendEndpoints.AUTH.TOS_ACCEPT,
        {
          tosId: data?.tosId,
        },
        {
          Authorization: `Bearer ${data.token}`,
          "Content-Type": `application/x-www-form-urlencoded`,
        },
      );
    });

    it("should post empty body data with TOS token data when tosId not present", async () => {
      const data = { token: "tos-token" };
      await acceptTos(data);

      expect(postRequest).toHaveBeenCalledWith(
        backendEndpoints.AUTH.TOS_ACCEPT,
        {},
        {
          Authorization: `Bearer ${data.token}`,
          "Content-Type": `application/x-www-form-urlencoded`,
        },
      );
    });
  });
});
