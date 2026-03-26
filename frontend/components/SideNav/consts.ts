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

export const PROTECTED_ROUTES = (process.env.PROTECTED_ROUTES ?? "").split(",");

export const AI_POLICY_TEXT =
  "AI can make mistakes. Google is not responsible for third-party model output.";

export const DOCUMENTATION_LINK =
  process.env.NEXT_PUBLIC_APP_BASE_URL + "/docs";
