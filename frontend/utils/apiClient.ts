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
import {
  ERROR_MESSAGES,
  JWT_TOKEN_KEY,
  UNAUTHORIZED_ERROR_CODE,
} from "@/config/constants";
import { backendEndpoints, nextEndpoints } from "@/config/endpoints";
import { getErrorNotificationConfig } from "@/config/notifications";
import { routes } from "@/config/routes";
import { queryClient } from "@/hooks/useGlobalContext";
import { notifications } from "@mantine/notifications";
import axios from "axios";
import { stringify } from "qs";

import LocalStorage from "./LocalStorage";
import { UserDetails } from "./userDetailsStorage";

export async function createRequest(
  method: string,
  url: string,
  data: unknown = {},
  headers = {},
  showErrors: boolean,
) {
  const jwtToken = LocalStorage.get(JWT_TOKEN_KEY);
  if (jwtToken && MainConfig.isAuthEnabled) {
    headers = {
      ...headers,
      Authorization: `Bearer ${jwtToken}`,
    };
  }

  headers = {
    ...headers,
    "X-Request-Timestamp": Date.now().toString(),
    "X-Request-ID": crypto.randomUUID(),
    "Cache-Control": "no-cache, no-store, must-revalidate, private",
    Pragma: "no-cache",
    Expires: "0",
    "X-Cache-Status": "disabled",
  };

  return await axios
    .request({
      url,
      baseURL: process.env.NEXT_PUBLIC_APP_BASE_URL + "/api",
      timeout: 180000,
      headers,
      data,
      method,
      params: method === "GET" ? data : {},
      paramsSerializer: {
        serialize: (params) => stringify(params, { arrayFormat: "comma" }),
      },
    })
    .then((res) => res.data)
    .catch((err) => {
      let errorMessage =
        err?.response?.data?.error || err?.message || JSON.stringify(err);
      if (err?.status === UNAUTHORIZED_ERROR_CODE) {
        errorMessage = ERROR_MESSAGES.SESSION_EXPIRED;
      }

      const isUnauthorized = err?.status === UNAUTHORIZED_ERROR_CODE;
      // We only want to show notification if showError and also if this is any error than 403
      // For 403 we will handle error separately, after checking that token is indeed invalid
      if (showErrors && isUnauthorized === false) {
        notifications.show(getErrorNotificationConfig(errorMessage));
      }

      // Redirect to login if session is over or the request is forbidden
      // Only if token is expired
      if (err?.status === UNAUTHORIZED_ERROR_CODE && MainConfig.isAuthEnabled) {
        axios
          .request({
            url: backendEndpoints.AUTH.VALIDATE_TOKEN,
            baseURL: process.env.NEXT_PUBLIC_APP_BASE_URL + "/api",
            timeout: 180000,
            headers,
            data,
            method,
            params: "GET",
            paramsSerializer: {
              serialize: (params) =>
                stringify(params, { arrayFormat: "comma" }),
            },
          })
          .then((tokenResponse) => {
            const isValidToken = tokenResponse.data.valid;

            if (isValidToken) {
              return;
            }

            queryClient.clear();
            UserDetails.remove();
            LocalStorage.clear();

            if (showErrors) {
              notifications.show(getErrorNotificationConfig(errorMessage));
            }

            if (!window.location.href.endsWith(nextEndpoints.AUTH.SIGNIN)) {
              return setTimeout(() => {
                window.location.replace(routes.signin);
              }, 1500);
            }
          });
      }

      throw new Error(errorMessage);
    });
}

export async function getRequest<T>(
  url: string,
  data: unknown = {},
  headers = {},
  showErrors: boolean = true,
): Promise<T> {
  return createRequest("GET", url, data, headers, showErrors);
}

export async function postRequest(
  url: string,
  data: unknown = {},
  headers = {},
  showErrors: boolean = true,
) {
  return createRequest("POST", url, data, headers, showErrors);
}

export async function postRequestWithoutCache(
  url: string,
  data: unknown = {},
  showErrors: boolean = true,
) {
  return createRequest(
    "POST",
    url,
    data,
    {
      "Cache-Control": "no-cache",
      Pragma: "no-cache",
    },
    showErrors,
  );
}

export async function deleteRequest(
  url: string,
  data: unknown = {},
  headers = {},
  showErrors: boolean = true,
) {
  return createRequest("DELETE", url, data, headers, showErrors);
}

export async function patchRequest(
  url: string,
  data: unknown = {},
  headers = {},
  showErrors: boolean = true,
) {
  return createRequest("PATCH", url, data, headers, showErrors);
}

export async function putRequest(
  url: string,
  data: unknown = {},
  headers = {},
  showErrors: boolean = true,
) {
  return createRequest("PUT", url, data, headers, showErrors);
}
