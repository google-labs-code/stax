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
import { TOOLTIPS } from "@/config/constants";
import {
  HumanEvalPassRateData,
  MetricsSummaryResponse,
  SxsHumanEvalPassRateData,
  SxsInferenceMetricsResponse,
} from "@/types";
import { Box, Group, ScrollArea, Text } from "@mantine/core";
import { useMemo } from "react";

import { useProjectContext } from "../../hooks/useProjectContext";
import EvaluatorRateMetricCard from "./MetricCards/EvaluatorRateMetricCard";
import EvaluatorScoreMetricCard from "./MetricCards/EvaluatorScoreMetricCard";
import EvaluatorScoreSxSMetricCard from "./MetricCards/EvaluatorScoreSxSMetricCard";
import LatencyMetricCard from "./MetricCards/LatencyMetricCard";
import TokensMetricCard from "./MetricCards/TokensMetricCard";

type ProjectMetricsData = MetricsSummaryResponse | SxsInferenceMetricsResponse;

type ProjectMetricsProps = {
  providers: string[];
};

const isSxsHumanEvalData = (
  data: HumanEvalPassRateData,
): data is SxsHumanEvalPassRateData => {
  return "ratingCounts" in data && "total" in data;
};

const isSxsMetrics = (
  data: ProjectMetricsData | undefined,
): data is SxsInferenceMetricsResponse => {
  return !!data && "sideA" in data && "sideB" in data;
};

export default function ProjectMetrics({ providers }: ProjectMetricsProps) {
  const { projectState, isSideBySide } = useProjectContext();
  const hasSxsMetrics = useMemo(
    () => isSideBySide && isSxsMetrics(projectState.metricsData),
    [isSideBySide, projectState.metricsData],
  );

  const metricsDataSxS =
    projectState.metricsData as SxsInferenceMetricsResponse;

  const tokenMetricsA = hasSxsMetrics
    ? {
        input: metricsDataSxS?.sideA.total_prompt_tokens,
        output: metricsDataSxS?.sideA.total_completion_tokens,
        total: metricsDataSxS?.sideA.total_tokens,
      }
    : {
        input: (projectState.metricsData as MetricsSummaryResponse)
          ?.total_prompt_tokens,
        output: (projectState.metricsData as MetricsSummaryResponse)
          ?.total_completion_tokens,
        total: (projectState.metricsData as MetricsSummaryResponse)
          ?.total_tokens,
      };

  const tokenMetricsB = hasSxsMetrics
    ? {
        input: metricsDataSxS?.sideB.total_prompt_tokens,
        output: metricsDataSxS?.sideB.total_completion_tokens,
        total: metricsDataSxS?.sideB.total_tokens,
      }
    : undefined;

  let latencyA: number | undefined = undefined;
  let latencyB: number | undefined = undefined;

  if (hasSxsMetrics) {
    latencyA = metricsDataSxS?.sideA.average_turn_time_taken;
    if (metricsDataSxS?.sideB?.average_turn_time_taken) {
      latencyB = metricsDataSxS?.sideB.average_turn_time_taken;
    }
  } else if (projectState.metricsData) {
    latencyA = (projectState.metricsData as MetricsSummaryResponse)
      .average_turn_time_taken;
  }

  return (
    <Box>
      <Group gap={4} className="mb-4 flex flex-row justify-between">
        <Group gap={4} className="cursor-default">
          <Text className="text-title-16">Project metrics</Text>
          <MaterialIcon
            name="info"
            className="text-secondary"
            tooltipClassName="min-w-[260px]"
            tooltipLabel={TOOLTIPS.PROJECT_METRICS}
            size={20}
          />
        </Group>
      </Group>

      <ScrollArea offsetScrollbars="x" type="auto" scrollbarSize={8}>
        <Group className="gap-xl min-w-max flex flex-row items-start">
          <EvaluatorRateMetricCard
            providers={providers}
            passRate={projectState.humanEvalPassRate.passRate}
            likes={projectState.humanEvalPassRate.likes}
            dislikes={projectState.humanEvalPassRate.dislikes}
            ratingCounts={
              isSxsHumanEvalData(projectState.humanEvalPassRate)
                ? projectState.humanEvalPassRate.ratingCounts
                : undefined
            }
            total={
              isSxsHumanEvalData(projectState.humanEvalPassRate)
                ? projectState.humanEvalPassRate.total
                : undefined
            }
          />
          <LatencyMetricCard
            latencyA={latencyA}
            latencyB={isSideBySide ? latencyB : undefined}
            providers={providers}
          />
          <TokensMetricCard
            providers={providers}
            tokenMetricsA={tokenMetricsA}
            tokenMetricsB={isSideBySide ? tokenMetricsB : undefined}
          />

          {projectState?.evalAnalyticsScores?.map((evaluator, key: number) => (
            <EvaluatorScoreMetricCard
              key={key}
              evaluator={evaluator}
              providers={providers}
            />
          ))}
          {projectState?.evalSxSAnalyticsScores?.pointwise?.map(
            (evaluator, key: number) => (
              <EvaluatorScoreSxSMetricCard
                key={key}
                evaluator={evaluator}
                providers={providers}
              />
            ),
          )}
          {projectState?.evalSxSAnalyticsScores?.sideBySide?.map(
            (evaluator, key: number) => (
              <EvaluatorScoreSxSMetricCard
                key={key}
                evaluator={evaluator}
                providers={providers}
              />
            ),
          )}
        </Group>
      </ScrollArea>
    </Box>
  );
}
