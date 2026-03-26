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

import {
  EvalSxSAnalyticsScorePointwise,
  EvalSxSAnalyticsScoreSxS,
} from "@/types";
import { Card, Group, Loader, Stack, Text } from "@mantine/core";
import { useEffect, useState } from "react";

import { useProjectContext } from "../../../hooks/useProjectContext";
import SideBySideModelsIcon from "../SideBySideModelsIcon";
import MetricCardFooter from "./MetricCardFooter";
import MetricCardHeader from "./MetricCardHeader";

type EvaluatorScoreMetricCardProps = {
  evaluator: EvalSxSAnalyticsScoreSxS | EvalSxSAnalyticsScorePointwise;
  providers: string[];
};

export default function EvaluatorScoreSxSMetricCard({
  evaluator,
  providers,
}: EvaluatorScoreMetricCardProps) {
  const name = evaluator?.scorer_name || "";
  const { projectState, isSideBySide } = useProjectContext();
  const [initialLoadComplete, setInitialLoadComplete] = useState(false);

  useEffect(() => {
    if (!projectState?.evaluatorSxSMetricsIsLoading && !initialLoadComplete) {
      setInitialLoadComplete(true);
    }
  }, [projectState?.evaluatorSxSMetricsIsLoading, initialLoadComplete]);

  const isLoading =
    !initialLoadComplete && projectState?.evaluatorSxSMetricsIsLoading;

  const EvaluatorItemPointwise = ({
    data,
  }: {
    data: EvalSxSAnalyticsScorePointwise;
  }) => {
    const sideAScore = data?.side_a?.average_score;
    const sideBScore = data?.side_b?.average_score;
    const scoreDelta =
      sideBScore !== undefined && sideAScore !== undefined
        ? sideBScore - sideAScore
        : undefined;

    const shouldShowDiff =
      isSideBySide &&
      sideAScore !== undefined &&
      sideBScore !== undefined &&
      sideAScore > 0 &&
      sideBScore > 0 &&
      Math.abs(scoreDelta || 0) >= 0.01;

    const deltaColor = (scoreDelta || 0) < 0 ? "text-red" : "text-lime";
    const formattedDelta = scoreDelta?.toFixed(2);

    return (
      <Stack gap="8px" py={8} className="w-full">
        <Group>
          <SideBySideModelsIcon variant="first" />
          <Text className="text-title-14">
            {sideAScore ? sideAScore?.toFixed(2) : "--"}
          </Text>
        </Group>
        <Group justify="space-between" className="w-full">
          <Group>
            <SideBySideModelsIcon variant="second" />
            <Text className="text-title-14">
              {sideBScore ? sideBScore?.toFixed(2) : "--"}
            </Text>
          </Group>
          {shouldShowDiff && (
            <Text className={`text-body-14 ${deltaColor}`}>
              {formattedDelta}
            </Text>
          )}
        </Group>
      </Stack>
    );
  };

  const EvaluatorItemSxS = ({ data }: { data: EvalSxSAnalyticsScoreSxS }) => (
    <Stack gap="8px" py={8} className="w-full">
      {data?.datapoints?.map((dataPoint, key) => (
        <Group key={key}>
          <div className="flex h-5 w-8 items-center justify-center rounded bg-lightBlue">
            <Text size="xs" fw={500} className="text-brand">
              {dataPoint.category}
            </Text>
          </div>

          <Text className="text-title-14">{dataPoint.count || 0}</Text>
        </Group>
      ))}
    </Stack>
  );

  return (
    <Card
      padding="md"
      radius="lg"
      withBorder
      className="h-[172px] w-[272px] max-w-[100%]"
    >
      <MetricCardHeader title={name || ""} providers={providers} />

      <Group gap="0" className="flex-1 flex w-full items-center justify-center">
        {isLoading ? (
          <Loader size="sm" />
        ) : "datapoints" in evaluator ? (
          <EvaluatorItemSxS data={evaluator as EvalSxSAnalyticsScoreSxS} />
        ) : (
          <EvaluatorItemPointwise
            data={evaluator as EvalSxSAnalyticsScorePointwise}
          />
        )}
      </Group>

      {"delta" in evaluator && <MetricCardFooter text="Average score" />}
    </Card>
  );
}
