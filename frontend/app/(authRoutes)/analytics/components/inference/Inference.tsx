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

import { useAnalyticsContext } from "@/hooks/useAnalyticsContext";
import dayjs, { Dayjs } from "dayjs";
import { useCallback, useState } from "react";

import { Chart } from "../../types/charts";
import AnalyticsFiltersContainer from "../AnalyticsFiltersContainer";
import InferenceChart from "./InferenceChart";

export default function Inference() {
  const { selectedFilters } = useAnalyticsContext();
  const [charts, setCharts] = useState<Chart[]>([
    {
      selectedDates: [dayjs().subtract(15, "day"), dayjs()],
      datesLabel: "Last 15 days",
      analyticsData: { averageTime: 0, prompts: 0, completed: 0, total: 0 },
    },
  ]);
  const deleteChartAtIndex = useCallback(
    (index: number) =>
      setCharts((prevCharts) => prevCharts.filter((_, ind) => ind !== index)),
    [charts],
  );
  const changeSelectedDatesOfAChart = useCallback(
    (
      {
        selectedDates,
        datesLabel,
      }: {
        selectedDates: Dayjs[];
        datesLabel?: string;
      },
      index: number,
    ) =>
      setCharts((prevCharts) =>
        prevCharts.map((chart: Chart, i) =>
          i === index
            ? {
                ...chart,
                selectedDates,
                datesLabel: datesLabel || "",
              }
            : chart,
        ),
      ),
    [charts],
  );

  return (
    <div className="flex flex-col gap-4">
      <AnalyticsFiltersContainer />

      {charts.map((chart, index) => (
        <InferenceChart
          key={index}
          selectedFilters={selectedFilters}
          selectedDates={chart.selectedDates}
          selectedDatesLabel={chart.datesLabel || ""}
          onDateChange={(selectedDates: Dayjs[], datesLabel?: string) =>
            changeSelectedDatesOfAChart({ selectedDates, datesLabel }, index)
          }
          duplicateChart={() => setCharts([...charts, chart])}
          {...(charts.length > 1
            ? { deleteChart: () => deleteChartAtIndex(index) }
            : {})}
        />
      ))}
    </div>
  );
}
