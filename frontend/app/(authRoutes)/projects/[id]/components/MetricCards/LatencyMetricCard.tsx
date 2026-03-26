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
import { convertMsToS, formatLatency } from "@/utils/helpers";
import { Card, Group, Loader, Stack, Text } from "@mantine/core";
import { useEffect, useState } from "react";

import { useProjectContext } from "../../../hooks/useProjectContext";
import SideBySideModelsIcon from "../SideBySideModelsIcon";
import MetricCardFooter from "./MetricCardFooter";
import MetricCardHeader from "./MetricCardHeader";

type LatencyMetricCardProps = {
  latencyA?: number;
  latencyB?: number | null;
  models?: Model[];
  providers: string[];
};

export default function LatencyMetricCard({
  latencyA,
  latencyB,
  models,
  providers,
}: LatencyMetricCardProps) {
  const { projectState, isSideBySide } = useProjectContext();

  const [initialLoadComplete, setInitialLoadComplete] = useState(false);

  useEffect(() => {
    if (!projectState?.inferenceMetricsIsLoading && !initialLoadComplete) {
      setInitialLoadComplete(true);
    }
  }, [projectState?.inferenceMetricsIsLoading, initialLoadComplete]);

  const shouldShowDiff =
    isSideBySide &&
    latencyA !== undefined &&
    latencyA > 0 &&
    latencyB !== undefined &&
    latencyB &&
    latencyB !== null &&
    latencyB > 0;

  const latencyDifference =
    shouldShowDiff && latencyB !== null ? latencyB - latencyA! : 0;

  const differenceColor = latencyDifference < 0 ? "text-lime" : "text-red";
  const isDifferenceZero = Math.abs(latencyDifference) < 0.001;

  const formattedDifference = convertMsToS(latencyDifference);

  const isLoading =
    !initialLoadComplete && projectState?.inferenceMetricsIsLoading;

  return (
    <Card
      padding="md"
      radius="lg"
      withBorder
      className={`${isSideBySide ? "h-[172px]" : "h-[148px]"} w-[272px] max-w-[100%]`}
    >
      <MetricCardHeader
        title="Average Latency"
        models={models}
        providers={providers}
      />
      <Group gap="0" className="flex-1 flex w-full items-center justify-center">
        {isLoading ? (
          <Loader size="sm" />
        ) : (
          <Group gap="0" className="flex-1 flex w-full items-center">
            {isSideBySide ? (
              <Stack gap="8px" className="w-full">
                <Group>
                  <SideBySideModelsIcon variant="first" />
                  <Text className="text-title-14">
                    {formatLatency(latencyA)}
                  </Text>
                </Group>
                <Group justify="space-between" className="w-full">
                  <Group>
                    <SideBySideModelsIcon variant="second" />
                    <Text className="text-title-14">
                      {formatLatency(latencyB)}
                    </Text>
                  </Group>
                  {shouldShowDiff && !isDifferenceZero && (
                    <Text className={`text-body-14 ${differenceColor}`}>
                      {formattedDifference}
                    </Text>
                  )}
                </Group>
              </Stack>
            ) : (
              <Group gap={0}>
                <Text className="!font-medium text-title-28">
                  {latencyA !== undefined ? convertMsToS(latencyA || 0) : "--"}
                </Text>
              </Group>
            )}
          </Group>
        )}
      </Group>

      <MetricCardFooter text="May differ from production performance" />
    </Card>
  );
}
