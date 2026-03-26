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

import { getModelDetails } from "@/config/constants";
import { Model } from "@/queries/types";
import { Group, Text } from "@mantine/core";

type MetricCardHeaderProps = {
  title: string;
  models?: Model[];
  providers: string[];
};

export default function MetricCardHeader({
  title,
  providers,
}: MetricCardHeaderProps) {
  return (
    <Group className="mb-[4px] flex-col items-start justify-start gap-0">
      <Group className="mb-[8px] gap-0 ml-[6px]">
        {providers?.map((provider, index) => {
          const IconComponent = getModelDetails(provider)?.icon;

          return (
            <Text
              className="border-default ml-[-6px] rounded-sm bg-white p-[4px]"
              key={index}
            >
              {IconComponent && <IconComponent size={14} />}
            </Text>
          );
        })}
      </Group>
      <Text className="!font-medium text-secondary text-body-14">{title}</Text>
    </Group>
  );
}
