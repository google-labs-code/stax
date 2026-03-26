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
import { UnstyledButton } from "@mantine/core";

export default function InputRightResetButton({
  form,
  descriptors,
  descriptorKey,
  fieldName,
}: {
  form: any;
  descriptors?: ModelDescriptors;
  descriptorKey: keyof ModelDescriptors;
  fieldName: string;
}) {
  if (!form.errors[fieldName] || !descriptors?.[descriptorKey]?.defaultValue) {
    return null;
  }

  return (
    <UnstyledButton
      className="text-secondary text-body-12"
      onClick={() => {
        if (descriptors?.[descriptorKey]?.defaultValue) {
          form.setFieldValue(
            fieldName,
            descriptors?.[descriptorKey]?.defaultValue,
          );
        }
      }}
    >
      Reset
    </UnstyledButton>
  );
}
