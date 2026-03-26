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

export default function FieldTopP({
  form,
  descriptors,
}: {
  form: any;
  descriptors?: ModelDescriptors;
}) {
  return (
    <FieldContainer>
      <ModelFieldName name="Top P" tooltip={descriptors?.top_p?.description} />

      <SliderNumberInput
        {...form.getInputProps("topP")}
        min={descriptors?.top_p?.minValue || 0}
        max={descriptors?.top_p?.maxValue || 0.9}
        step={0.1}
        value={form.values.topP as number}
        setValue={(value: number | undefined) => {
          form.setFieldValue("topP", value);
        }}
        onBlur={() => {
          if (!form.values.topP) {
            form.setFieldValue(
              "topP",
              descriptors?.top_p?.defaultValue || undefined,
            );
          }
        }}
      />
    </FieldContainer>
  );
}
