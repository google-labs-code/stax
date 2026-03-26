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

import {
  ComboboxItem,
  ComboboxLikeRenderOptionInput,
  Group,
  Text,
} from "@mantine/core";

import MaterialIcon from "./MaterialIcon";

export default function CustomSelectOption({
  option,
}: {
  option: ComboboxLikeRenderOptionInput<ComboboxItem>;
}) {
  return (
    <Group gap={4}>
      <MaterialIcon
        name="check"
        className={`!text-[16px] ${!option.checked && "text-transparent"}`}
      />
      <Text className="text-title-12">{option.option.label}</Text>
    </Group>
  );
}
