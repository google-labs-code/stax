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
import { Group, ScrollArea } from "@mantine/core";

export type SideBySideEvaluatorColumnProps = {
  statusA: React.ReactNode;
  statusB: React.ReactNode;
  sxs?: React.ReactNode;
};

export default function SideBySideEvaluatorColumn({
  statusA,
  statusB,
  sxs,
}: SideBySideEvaluatorColumnProps) {
  return (
    <Group gap={0} className="flex flex-col w-[100%]">
      {statusA && (
        <ScrollArea className="w-[100%]" scrollbarSize={5}>
          <Group className="flex flex-row flex-nowrap w-[100%] gap-xl border-b-default min-h-[40px] p-[12px]">
            <SideBySideModelLetter letter="A" />
            <Group gap={0}>{statusA}</Group>
          </Group>
        </ScrollArea>
      )}
      {statusB && (
        <ScrollArea className="w-[100%]" scrollbarSize={5}>
          <Group className="flex flex-row flex-nowrap w-[100%] gap-xl border-b-default min-h-[40px] p-[12px]">
            <SideBySideModelLetter letter="B" />
            <Group gap={0}>{statusB}</Group>
          </Group>
        </ScrollArea>
      )}
      {sxs && (
        <Group gap={0} className="flex flex-col w-[100%]">
          <ScrollArea className="w-[100%]" scrollbarSize={5}>
            <Group className="flex flex-row flex-nowrap w-[100%] gap-xl min-h-[40px] p-[12px]">
              <Group gap={0}>{sxs}</Group>
            </Group>
          </ScrollArea>
        </Group>
      )}
    </Group>
  );
}
