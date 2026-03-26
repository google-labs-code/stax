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

import dayjs from "@/utils/dayjsSetup";
import * as Plot from "@observablehq/plot";

import { StackedBarChartProps } from "./types";
import PlotFigure from "./utils/PlotFigure";
import { CHART_COLORS } from "./utils/chartConstants";

export default function StackedBarChart({
  data,
  x,
  y,
  fill,
}: StackedBarChartProps) {
  const maxY = Math.max(...data.map((d) => d.value || 0));

  const generateTooltipContent = (dataPoint: any) => [
    {
      label: "Date",
      value: dayjs(dataPoint.value).format("MMM DD"),
    },
    {
      label: dataPoint.type,
      value: dataPoint.value,
    },
  ];

  return (
    <PlotFigure
      className="stackedBarChart"
      generateTooltipContent={generateTooltipContent}
      options={{
        data,
        x: { label: null },
        y: { label: null, domain: [0, maxY === 0 ? 10 : maxY] },
        fx: { label: null },
        legend: { position: "bottom" },
        color: { legend: true },
        marks: [
          Plot.barY(data, {
            fx: x,
            x: fill,
            y: y,
            fill: fill,
          }),
          Plot.gridY({
            fill: CHART_COLORS.grid,
            fillOpacity: 0.5,
          }),
          Plot.frame({
            stroke: CHART_COLORS.grid,
            anchor: "bottom",
          }),
          Plot.axisY({
            fill: CHART_COLORS.axis,
            tickSize: 0,
          }),
          Plot.axisX({
            tickSize: 0,
            tickFormat: () => "",
          }),
          Plot.axisFx({
            fill: CHART_COLORS.axis,
            ticks: "1 day",
            tickSize: 0,
            anchor: "bottom",
            tickFormat: (d) => dayjs(d).format("MMM DD"),
          }),
        ],
      }}
    />
  );
}
