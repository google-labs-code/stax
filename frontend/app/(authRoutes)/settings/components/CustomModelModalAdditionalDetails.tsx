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

import FieldContainer from "@/components/Model/FieldContainer";
import ModelFieldName from "@/components/ModelFieldName";
import SliderNumberInput from "@/components/SliderNumberInput";
import { CustomModelPayload } from "@/queries/types";
import { NumberInput, Stack } from "@mantine/core";
import { UseFormReturnType } from "@mantine/form";

export default function CustomModelModalAdditionalDetails({
  isDisabled,
  form,
  inputClassNames,
}: {
  isDisabled: boolean;
  form: UseFormReturnType<CustomModelPayload>;
  inputClassNames: string;
}) {
  return (
    <Stack gap={8} className="w-full">
      <FieldContainer>
        <ModelFieldName name="Temperature" />

        <SliderNumberInput
          min={0}
          max={2}
          step={0.01}
          value={form.values.properties?.temperature as number}
          decimalScale={2}
          setValue={(value: number | undefined) => {
            form.setFieldValue("properties.temperature", value);
          }}
          disabled={isDisabled}
        />
      </FieldContainer>
      <FieldContainer>
        <ModelFieldName name="Max tokens" />
        <NumberInput
          classNames={{
            input: "w-[427px] h-[40px] rounded-lg",
            root: "relative",
            wrapper: "mb-0",
            error: "hidden",
          }}
          allowDecimal={false}
          hideControls
          value={form.values.properties?.max_tokens ?? ""}
          rightSectionProps={{
            className: "mr-4",
          }}
          onChange={(value: number | string) => {
            form.setFieldValue("properties.max_tokens", value);
          }}
          disabled={isDisabled}
        />
      </FieldContainer>
      <FieldContainer>
        <ModelFieldName name="Top P" />
        <SliderNumberInput
          min={0}
          max={0.9}
          step={0.1}
          value={form.values.properties?.top_p as number}
          setValue={(value: number | undefined) => {
            form.setFieldValue("properties.top_p", value);
          }}
          disabled={isDisabled}
        />
      </FieldContainer>
      <FieldContainer>
        <ModelFieldName name="Seed" />
        <NumberInput
          classNames={{
            input: inputClassNames,
            wrapper: "mb-0",
          }}
          value={form.values.properties?.seed as number}
          onChange={(value) => {
            form.setFieldValue(
              "properties.seed",
              typeof value === "number" ? value : undefined,
            );
          }}
          disabled={isDisabled}
        />
      </FieldContainer>
    </Stack>
  );
}
