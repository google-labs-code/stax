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
import { NumberInput } from "@mantine/core";

import ModelFieldName from "../ModelFieldName";
import FieldContainer from "./FieldContainer";
import InputMinMaxPlaceholder from "./InputMinMaxPlaceholder";
import InputRightResetButton from "./InputRightResetButton";

export default function FieldMaxTokens({
  form,
  descriptors,
}: {
  form: any;
  descriptors?: ModelDescriptors;
}) {
  return (
    <FieldContainer>
      <ModelFieldName
        name="Max tokens"
        isRequired={true}
        tooltip={descriptors?.max_output_tokens?.description}
      />

      <NumberInput
        classNames={{
          input: "w-[427px] h-[40px] rounded-lg",
          root: "relative",
          wrapper: "mb-0",
          error: "hidden",
        }}
        allowDecimal={false}
        hideControls
        {...form.getInputProps("maxTokens")}
        value={form.values.maxTokens}
        rightSection={
          <InputRightResetButton
            form={form}
            descriptors={descriptors}
            descriptorKey="max_output_tokens"
            fieldName="maxTokens"
          />
        }
        rightSectionProps={{
          className: "mr-[16px]",
        }}
        placeholder={InputMinMaxPlaceholder({
          hasError: form.errors["maxTokens"] ? true : false,
          descriptors,
          descriptorKey: "max_output_tokens",
        })}
        onChange={(value) => {
          form.setFieldValue("maxTokens", value as number | undefined);
          if (typeof value === "number") {
            const min = descriptors?.max_output_tokens?.minValue;
            const max = descriptors?.max_output_tokens?.maxValue;

            if (min && max && (value < min || value > max)) {
              form.setFieldError("maxTokens", "error");
            } else {
              form.clearFieldError("maxTokens");
            }
          } else if (!value) {
            form.setFieldError("maxTokens", "error");
          }
        }}
        onBlur={() => {
          //
        }}
      />
    </FieldContainer>
  );
}
