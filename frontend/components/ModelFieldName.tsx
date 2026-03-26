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

import { Stack, Text } from "@mantine/core";

import MaterialIcon from "./MaterialIcon";

export default function ModelFieldName({
  name,
  className,
  isRequired,
  tooltip,
}: {
  name: string;
  className?: string;
  isRequired?: boolean;
  tooltip?: string | undefined;
}) {
  return (
    <Stack
      gap={0}
      className="flex flex-row items-center justify-start self-start pt-[8px]"
    >
      <Text className={`text-body-14 ${className}`}>
        {name}
        {isRequired && <span className="text-red">*</span>}
      </Text>
      {tooltip && (
        <MaterialIcon
          name="info"
          size={20}
          className="text-secondaryDark ml-[4px] cursor-pointer"
          tooltipPosition="top"
          tooltipClassName="max-w-[240px]"
          tooltipLabel={tooltip}
        />
      )}
    </Stack>
  );
}
