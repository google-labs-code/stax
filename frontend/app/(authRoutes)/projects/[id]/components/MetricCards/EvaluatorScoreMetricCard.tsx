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

import { EvalAnalyticsScore } from "@/types";
import { Card, Group, Loader, Text } from "@mantine/core";
import { useEffect, useState } from "react";

import { useProjectContext } from "../../../hooks/useProjectContext";
import MetricCardFooter from "./MetricCardFooter";
import MetricCardHeader from "./MetricCardHeader";

type EvaluatorScoreMetricCardProps = {
  evaluator: EvalAnalyticsScore;
  providers: string[];
};

export default function EvaluatorScoreMetricCard({
  evaluator,
  providers,
}: EvaluatorScoreMetricCardProps) {
  const name = evaluator?.scorer_name || "";
  const averageScore = evaluator?.average_score;
  const { projectState } = useProjectContext();
  const [initialLoadComplete, setInitialLoadComplete] = useState(false);

  useEffect(() => {
    if (!projectState?.evaluatorMetricsIsLoading && !initialLoadComplete) {
      setInitialLoadComplete(true);
    }
  }, [projectState?.evaluatorMetricsIsLoading, initialLoadComplete]);

  const isLoading =
    !initialLoadComplete && projectState?.evaluatorMetricsIsLoading;

  return (
    <Card
      padding="md"
      radius="lg"
      withBorder
      className="h-[148px] w-[272px] max-w-[100%]"
    >
      <MetricCardHeader title={name || ""} providers={providers} />

      <Group gap="0" className="flex-1 flex w-full items-center justify-center">
        {isLoading ? (
          <Loader size="sm" />
        ) : (
          <Group gap="0" className="w-full flex-col flex-wrap items-start">
            {averageScore ? (
              <Text className="!font-medium text-title-28">
                {averageScore.toFixed(2)}
              </Text>
            ) : (
              <Text className="!font-medium text-resting text-title-28">
                --
              </Text>
            )}
          </Group>
        )}
      </Group>

      <MetricCardFooter text="Average score" />
    </Card>
  );
}
