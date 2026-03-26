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

import { ModelDescriptors } from "@/queries/types";

export default function InputMinMaxPlaceholder({
  hasError,
  descriptors,
  descriptorKey,
}: {
  hasError: boolean;
  descriptors?: ModelDescriptors;
  descriptorKey: keyof ModelDescriptors;
}) {
  if (
    hasError &&
    descriptors?.[descriptorKey]?.minValue &&
    descriptors?.[descriptorKey]?.maxValue
  ) {
    return `Use a number between ${descriptors?.[descriptorKey].minValue} and ${descriptors?.[descriptorKey].maxValue}`;
  }

  return "";
}
