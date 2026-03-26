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

import { Model } from "@/queries/types";
import { Card, Group, Text } from "@mantine/core";

import MetricCardFooter from "./MetricCardFooter";
import MetricCardHeader from "./MetricCardHeader";

type CostMetricCardProps = {
  data: number[];
  models?: Model[];
  providers: string[];
};

export default function CostMetricCard({
  data,
  models,
  providers,
}: CostMetricCardProps) {
  return (
    <Card
      shadow="sm"
      padding="md"
      radius="lg"
      withBorder
      className={`${data.length > 1 ? "h-[172px]" : "h-[148px]"} w-[272px] max-w-[100%]`}
    >
      <MetricCardHeader
        title="Average Cost"
        models={models}
        providers={providers}
      />

      <Group gap="0" className="w-full flex-row flex-wrap items-end">
        {data.length <= 1 ? (
          <Group gap={0}>
            <Text className="!font-medium text-title-28">$ {data[0]}</Text>
          </Group>
        ) : (
          <Group>
            <Text>not supported yet</Text>
          </Group>
        )}
      </Group>

      <MetricCardFooter text="Estimated using input/output prices/Mtok" />
    </Card>
  );
}
