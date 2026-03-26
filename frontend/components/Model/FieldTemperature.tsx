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

import ModelFieldName from "../ModelFieldName";
import SliderNumberInput from "../SliderNumberInput";
import FieldContainer from "./FieldContainer";

export default function FieldTemperature({
  form,
  descriptors,
  disabled,
}: {
  form: any;
  descriptors?: ModelDescriptors;
  disabled?: boolean;
}) {
  return (
    <FieldContainer>
      <ModelFieldName
        name="Temperature"
        tooltip={descriptors?.temperature?.description}
      />

      <SliderNumberInput
        disabled={disabled}
        {...form.getInputProps("temperature")}
        min={descriptors?.temperature?.minValue || 0}
        max={descriptors?.temperature?.maxValue || 2}
        step={0.01}
        value={form.values.temperature as number}
        decimalScale={2}
        setValue={(value: number | undefined) => {
          form.setFieldValue("temperature", value);
        }}
        onBlur={() => {
          if (!form.values.temperature) {
            form.setFieldValue(
              "temperature",
              descriptors?.temperature?.defaultValue || 0,
            );
          }
        }}
      />
    </FieldContainer>
  );
}
