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

import SideBySideModelLetter from "@/components/SideBySideModelLetter";
import { Group, ScrollArea, Text } from "@mantine/core";

export type SideBySideDefaultColumnProps = {
  valueA: string;
  valueB: string;
  difference: string | null;
};

export default function SideBySideDefaultColumn({
  valueA,
  valueB,
  difference,
}: SideBySideDefaultColumnProps) {
  if (!valueA && !valueB) {
    return null;
  }

  return (
    <Group gap={0} className="w-full">
      <Group gap={0} className="flex flex-column w-[100%]">
        <ScrollArea
          className="w-[100%] border-b-default min-h-[40px] p-[12px]"
          scrollbarSize={5}
        >
          <Group className="flex flex-row flex-nowrap gap-sm justify-between w-[100%] ">
            <SideBySideModelLetter letter="A" />
            <Text className="text-body-12 text-secondary">{valueA || "-"}</Text>
          </Group>
        </ScrollArea>
        <ScrollArea
          className="w-[100%] border-b-default min-h-[40px] p-[12px]"
          scrollbarSize={5}
        >
          <Group className="flex flex-row flex-nowrap gap-sm justify-between w-[100%]">
            <SideBySideModelLetter letter="B" />
            <Text className="text-body-12 text-secondary">{valueB || "-"}</Text>
          </Group>
        </ScrollArea>
      </Group>
      <ScrollArea className="w-[100%]" scrollbarSize={5}>
        <Group className="flex flex-row gap-0 p-[12px] justify-between w-[100%] flex-nowrap">
          <Text className="text-body-11 text-resting !font-medium">
            Difference
          </Text>
          {difference ? (
            <Text
              className={`text-body-16 !font-medium ${difference && parseFloat(difference) >= 0 ? "text-lime" : "text-red"}`}
            >
              {difference ? difference : "-"}
            </Text>
          ) : (
            <Text className="text-body-16 !font-medium text-secondary">-</Text>
          )}
        </Group>
      </ScrollArea>
    </Group>
  );
}
