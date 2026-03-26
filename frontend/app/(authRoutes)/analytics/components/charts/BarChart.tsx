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

import * as Plot from "@observablehq/plot";

import "../../styles/main.scss";
import { FilterLabel } from "../../types/charts";
import { generateRange } from "../utils";
import { BarChartData, BarChartProps } from "./types";
import PlotFigure from "./utils/PlotFigure";
import { CHART_COLORS } from "./utils/chartConstants";

export default function BarChart({
  data,
  title,
  subtitle,
  yLabel,
  isSecondFilterOn,
}: BarChartProps) {
  const modifiedData = isSecondFilterOn
    ? data.map((d: BarChartData) => ({
        ...d,
        x:
          d.filter === FilterLabel.FILTER_A
            ? Math.floor((d.category - 0.02) * 100) / 100
            : d.category,

        color: d.color,
        opacity: d.filter === FilterLabel.FILTER_B ? 0.3 : 1,
      }))
    : data;

  const maxY = Math.max(...data.map((d) => d.count)) + 0.05;

  const generateTooltipContent = (dataPoint: any) => [
    {
      label: isSecondFilterOn ? dataPoint.filter : "",
    },
    {
      label: "Category",
      value: dataPoint.categoryName,
    },
    {
      label: "Score",
      value: dataPoint.score,
    },
    {
      label: "Count",
      value: dataPoint.count,
    },
  ];

  return (
    <PlotFigure
      className="chart-with-large-y-labels"
      generateTooltipContent={generateTooltipContent}
      options={{
        data: data,
        height: 400,
        title,
        subtitle,
        x: {
          label: null,
          domain: isSecondFilterOn
            ? generateRange(-0.1, 1, 0.02)
            : generateRange(0, 1, 0.02),
        },
        y: {
          label: "Count",
          domain: [0, maxY],
          ticks: Array.from({ length: maxY }, (_, i) => i),
        },
        legend: { position: "bottom" },
        color: modifiedData.filter((d) => !!d.count).length > 0 && {
          legend: true,
          domain: modifiedData.map((d) => d.label).reverse(),
          range: modifiedData.map((d) => d.color).reverse(),
        },
        marks: [
          Plot.ruleY([0]),
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
            tickFormat: (d: number) => d.toFixed(1),
            label: yLabel,
          }),
          Plot.axisX({
            fill: CHART_COLORS.axis,
            tickSize: 0,
            ticks: generateRange(-0.1, 1, 0.1),
            tickFormat: (d: number) => (d >= 0 ? d : ""),
            labelAnchor: "left",
            dx: isSecondFilterOn ? -14 : 0,
          }),
          Plot.barY(modifiedData, {
            x: isSecondFilterOn ? (d: any) => d.x : "category",
            y: (d) => (d.count === 0 ? 0.01 * maxY : d.count),
            fill: "color",
            opacity: isSecondFilterOn ? "opacity" : 1,
          }),
        ],
      }}
    />
  );
}
