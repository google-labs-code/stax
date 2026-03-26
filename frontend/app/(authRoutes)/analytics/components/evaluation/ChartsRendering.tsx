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
import { SegmentedControl } from "@mantine/core";
import { useCallback, useState } from "react";

import { ChartTabEvaluation, EVALUATION_CHART_TABS } from "../../types/charts";
import BarChart from "../charts/BarChart";
import DodgePlot from "../charts/DodgePlot";
import { roundUpToEvenHundredth } from "../utils";
import { ChartsRenderingProps } from "./types";

export default function ChartsRendering({
  chart,
  isSecondFilterOn,
}: ChartsRenderingProps) {
  const [tab, setTab] = useState<ChartTabEvaluation>(chart.tab);

  const onChangeTab = useCallback(
    (val: string) => {
      setTab(val as ChartTabEvaluation);
    },
    [tab, chart],
  );

  return (
    <>
      <SegmentedControl
        className="pdf-tabs-transform mb-4 w-[220px] text-14"
        data={EVALUATION_CHART_TABS}
        radius="xl"
        color="var(--color-brand)"
        size="sm"
        classNames={{
          innerLabel: "font-medium",
        }}
        onChange={(val) => onChangeTab(val)}
      />
      <Card className="w-[100%] rounded-lg shadow-none">
        {tab === ChartTabEvaluation.BAR_CHART ? (
          <BarChart
            data={chart.datapoints.map((d) => ({
              ...d,
              category: roundUpToEvenHundredth(d.score),
              categoryName: d.category,
              label: `${d.score} - ${d.category}`,
            }))}
            isSecondFilterOn={isSecondFilterOn}
          />
        ) : (
          <DodgePlot
            data={chart.datapoints.map((d) => ({
              ...d,
              score: roundUpToEvenHundredth(d.score),
              label: `${d.score} - ${d.category}`,
              rawScore: d.score,
            }))}
            isSecondFilterOn={isSecondFilterOn}
          />
        )}
      </Card>
    </>
  );
}
