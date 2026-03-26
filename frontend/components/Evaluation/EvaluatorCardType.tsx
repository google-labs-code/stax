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

"use client";

import { EvaluatorCategory, ProjectType } from "@/types";
import { Chip, Group, Text } from "@mantine/core";

type EvaluatorCardTypeProps = {
  category: EvaluatorCategory;
  evaluationTypes?: ProjectType[];
};

export default function EvaluatorCardType({
  category,
  evaluationTypes,
}: EvaluatorCardTypeProps) {
  return (
    <Group gap={6}>
      <Chip
        size="sm"
        className="pointer-events-none"
        classNames={{
          label:
            "bg-veryLightSilver text-secondary px-[8px] py-[4px] h-[24px] rounded-sm",
        }}
      >
        <Text className="!font-medium text-body-11">{category}</Text>
      </Chip>
      {evaluationTypes?.includes(ProjectType.POINTWISE) && (
        <Chip
          size="sm"
          className="pointer-events-none"
          classNames={{
            label:
              "bg-veryLightSilver text-secondary px-[8px] py-[4px] h-[24px] rounded-sm",
          }}
        >
          <Text className="!font-medium text-body-11">POINTWISE</Text>
        </Chip>
      )}
      {evaluationTypes?.includes(ProjectType.SIDE_BY_SIDE) && (
        <Chip
          size="sm"
          className="pointer-events-none"
          classNames={{
            label:
              "bg-veryLightSilver text-secondary px-[8px] py-[4px] h-[24px] rounded-sm",
          }}
        >
          <Text className="!font-medium text-body-11">SIDE-BY-SIDE</Text>
        </Chip>
      )}
    </Group>
  );
}
