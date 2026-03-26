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

import { LOCAL_STORAGE_KEYS_TO_PRESERVE_ON_CLEAR } from "@/config/constants";

export default class LocalStorage {
  private static IS_SERVER = typeof window === "undefined";

  static get(key: string) {
    if (!this.IS_SERVER) {
      return localStorage.getItem(key);
    }
  }

  static set(key: string, value: any) {
    if (!this.IS_SERVER) {
      return localStorage.setItem(
        key,
        typeof value !== "string" ? JSON.stringify(value) : value,
      );
    }
  }

  static remove(key: string) {
    if (!this.IS_SERVER) {
      return localStorage.removeItem(key);
    }
  }

  static clear() {
    if (!this.IS_SERVER) {
      const preserved: Record<string, string> = {};

      for (const key of LOCAL_STORAGE_KEYS_TO_PRESERVE_ON_CLEAR) {
        const value = LocalStorage.get(key);
        if (value !== null && value !== undefined) {
          preserved[key] = value;
        }
      }

      localStorage.clear();

      for (const key in preserved) {
        LocalStorage.set(key, preserved[key]);
      }
    }
  }
}
