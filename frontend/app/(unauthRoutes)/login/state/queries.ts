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

import { backendEndpoints } from "@/config/endpoints";
import { Token } from "@/types";
import { postRequest, postRequestWithoutCache } from "@/utils/apiClient";

import { SignInWithGooglePayload } from "../types";

export const signInWithGoogle = (data: SignInWithGooglePayload) =>
  postRequestWithoutCache(backendEndpoints.AUTH.GOOGLE_AUTH_CODE, data);

export const acceptTos = (data: Token) =>
  postRequest(
    backendEndpoints.AUTH.TOS_ACCEPT,
    data?.tosId
      ? {
          tosId: data?.tosId,
        }
      : {},
    {
      Authorization: `Bearer ${data.token}`,
      "Content-Type": `application/x-www-form-urlencoded`,
    },
  );
