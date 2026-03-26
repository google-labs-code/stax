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

import { Provider, UserKeysAPIResponse } from "@/types";

export type KeyDetails = {
  label: string;
  provider: Provider;
  apiKey: keyof UserKeysAPIResponse;
  isKeyPresent: boolean;
  link: string;
  value?: string;
};

export type PlanckKeysAPIResponse = {
  count: number;
  references: string[];
};

export type UserKeyData = {
  provider: Provider;
};

export type DeleteKeyData = UserKeyData;

export type SetKeyData = UserKeyData & {
  key: string;
};

export type PlanckKeyData = {
  reference: string;
};
