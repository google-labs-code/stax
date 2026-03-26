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

import Card from "@/components/Card";
import { Loader, Text, useCombobox } from "@mantine/core";

import AverageAnalytics from "../AverageAnalytics";
import ChartsRendering from "./ChartsRendering";
import { EvaluationChartProps } from "./types";

export default function EvaluationChart({
  data,
  isLoading,
  isSecondFilterOn,
}: EvaluationChartProps) {
  const combobox = useCombobox({
    onDropdownClose: () => {
      combobox.resetSelectedOption();
    },
  });

  if (isLoading)
    return (
      <Card className="rounded-lg">
        <div className="flex h-[400px] items-center justify-center">
          <Loader size="md" />
        </div>
      </Card>
    );
  if (data?.length === 0) {
    return (
      <Card className="rounded-lg">
        <div className="flex h-[400px] items-center justify-center">
          <Text>No evaluation history available</Text>
        </div>
      </Card>
    );
  }

  return (
    <div>
      {data?.map((chart, index) => (
        <Card
          className={`mt-[24px] w-[100%] rounded-lg pt-0 ${index === 0 && "!mt-[0px]"}`}
          key={index}
        >
          <AverageAnalytics
            label={chart.evaluator}
            prompts={chart.monitoring?.total_prompt_tokens}
            total={chart.monitoring?.total_tokens}
            completed={chart.monitoring?.total_completion_tokens}
            time={chart.monitoring?.average_latency}
            isSecondFilterOn={isSecondFilterOn}
          />
          <ChartsRendering chart={chart} isSecondFilterOn={isSecondFilterOn} />
        </Card>
      ))}
    </div>
  );
}
