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

import MaterialIcon from "@/components/MaterialIcon";
import { Box, Group, Text } from "@mantine/core";

export const workbookAddRowElement = (
  columnId: string,
  handleAddNewRow: () => void,
) => {
  if (columnId === "mrt-row-select") {
    return {
      className:
        "border-none cursor-pointer !p-0 [&::before]:!shadow-none [&::before]:!content-none [&::after]:!shadow-none [&::after]:!content-none transition-all duration-0",
      onClick: (e: any) => {
        e.stopPropagation();
        e.preventDefault();
        handleAddNewRow();
      },
      children: (
        <Group
          className="flex w-full h-10 flex-nowrap bg-neutrals-50 group-hover:bg-lightBlue items-center cursor-pointer p-0 transition-all duration-0"
          gap={10}
        >
          <Box
            style={{ borderRight: "1px solid var(--color-light-silver)" }}
            className="flex items-center bg-neutrals-50 group-hover:bg-lightBlue justify-center w-[40px] h-[40px] border-r transition-all duration-0"
          >
            <MaterialIcon
              name="add"
              size={20}
              className="text-resting transition-all duration-0"
            />
          </Box>
          <Text className="text-resting bg-neutrals-50 group-hover:bg-lightBlue text-body-12 transition-all duration-0">
            Add row
          </Text>
        </Group>
      ),
    };
  }

  return {
    className: "border-none hidden",
  };
};

// find the items not common for arr1 and arr2
export function symmetricDifference(arr1: string[], arr2: string[]): string[] {
  const set1 = new Set(arr1);
  const set2 = new Set(arr2);

  const result: string[] = [];

  set1.forEach((item) => {
    if (!set2.has(item)) {
      result.push(item);
    }
  });

  set2.forEach((item) => {
    if (!set1.has(item)) {
      result.push(item);
    }
  });

  return result;
}
