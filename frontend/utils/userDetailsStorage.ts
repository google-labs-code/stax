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

import { Nullable, User } from "@/types";

import LocalStorage from "./LocalStorage";

export class UserDetails {
  public static STORAGE_KEY = "userDetails";

  static get(): Nullable<User> {
    const details = LocalStorage.get(this.STORAGE_KEY);
    if (!details) return null;

    return JSON.parse(details) as User;
  }

  static set(details: User): void {
    return LocalStorage.set(this.STORAGE_KEY, JSON.stringify(details));
  }

  static remove(): void {
    return LocalStorage.remove(this.STORAGE_KEY);
  }
}
