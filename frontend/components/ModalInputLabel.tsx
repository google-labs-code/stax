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

import { Text } from "@mantine/core";

import MaterialIcon from "./MaterialIcon";

type ModalInputLabelProps = {
  label: string;
  isRequired?: boolean;
  tooltip?: string;
};

export default function ModalInputLabel({
  label,
  isRequired,
  tooltip,
}: ModalInputLabelProps) {
  return (
    <Text className="gap-sm flex h-[20px] w-[100%] items-center text-title-16">
      {label} {isRequired && <span className="text-red">*</span>}
      {tooltip && (
        <MaterialIcon
          name="info"
          size={20}
          className="ml-[5px] cursor-pointer"
          tooltipClassName="max-w-[300px]"
          tooltipLabel={tooltip}
        />
      )}
    </Text>
  );
}
