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
import { useCallback } from "react";

import { FilterLabel } from "../../types/charts";
import { LineChartProps, LineItemData } from "./types";
import PlotFigure from "./utils/PlotFigure";
import { CHART_COLORS } from "./utils/chartConstants";

export default function LineChart({ data, isSecondFilterOn }: LineChartProps) {
  const maxY = Math.max(...data.map((d: LineItemData) => d.value || 0)) * 1.1;

  const getIsPointOverlapping = useCallback(
    (point: LineItemData) =>
      isSecondFilterOn &&
      data.filter(
        (d) => d.value === point.value && d.windowStart === point.windowStart,
      ).length > 1,
    [data, isSecondFilterOn],
  );

  const modifiedData = data.map((d: LineItemData) => ({
    ...d,
    y: d.value,
    x: d.windowStart,
    color:
      d.filter === FilterLabel.FILTER_B
        ? "var(--color-orange)"
        : "var(--color-brand)",
    fillId:
      d.filter === FilterLabel.FILTER_B
        ? "url(#gradient-orange)"
        : "url(#gradient-blue)",
  }));

  const generateTooltipContent = (dataPoint: LineItemData) =>
    getIsPointOverlapping(dataPoint)
      ? [
          {
            label: "Filter A",
          },
          {
            label: "Date",
            value: dayjs(dataPoint.windowStart).format("MMM DD"),
          },
          {
            label: dataPoint.label,
            value:
              dataPoint.value + (dataPoint.label === "Latency" ? " s" : ""),
          },
          {
            label: "\u200B",
          },
          {
            label: "Filter B",
          },
          {
            label: "Date",
            value: dayjs(dataPoint.windowStart).format("MMM DD"),
          },
          {
            label: dataPoint.label,
            value:
              dataPoint.value + (dataPoint.label === "Latency" ? " s" : ""),
          },
        ]
      : [
          {
            label: "Date",
            value: dayjs(dataPoint.windowStart).format("MMM DD"),
          },
          {
            label: dataPoint.label,
            value:
              dataPoint.value + (dataPoint.label === "Latency" ? " s" : ""),
          },
        ];

  return (
    <div className="lineChart">
      <svg width="0" height="0">
        <defs>
          <linearGradient id="gradient-orange" x1="0" x2="0" y1="0" y2="1">
            <stop
              offset="0%"
              stopColor="var(--color-orange)"
              stopOpacity="0.3"
            />
            <stop
              offset="100%"
              stopColor="var(--color-white)"
              stopOpacity="0"
            />
          </linearGradient>
          <linearGradient id="gradient-blue" x1="0" x2="0" y1="0" y2="1">
            <stop
              offset="0%"
              stopColor="var(--color-brand)"
              stopOpacity="0.3"
            />
            <stop
              offset="100%"
              stopColor="var(--color-white)"
              stopOpacity="0"
            />
          </linearGradient>
        </defs>
      </svg>

      <PlotFigure
        className="lineChart"
        generateTooltipContent={generateTooltipContent}
        options={{
          data,
          x: {
            label: null,
            type: "time",
            tickInterval: 60 * 60 * 1000,
            tickFormat: (d: dayjs.ConfigType) => dayjs(d).format("MMM DD"),
          },
          y: { label: null, domain: [0, maxY] },
          color: {
            legend: !!data.length,
            domain: isSecondFilterOn
              ? ["Filter A", "Filter B"]
              : ["ALL MODELS"],
            range: ["var(--color-brand)", "var(--color-orange)"],
          },
          legend: { position: "bottom" },
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
              tickSize: 0,
            }),
            Plot.lineY(modifiedData, {
              x: "x",
              y: "y",
              stroke: "color",
            }),
            Plot.areaY(modifiedData, {
              x: "x",
              y2: "y",
              fill: (d: any) => d.fillId,
            }),
            Plot.axisX({
              tickSize: 0,
              anchor: "bottom",
              dx: 10,
              tickFormat: (d) => dayjs(d).format("MMM DD"),
            }),
            Plot.dotY(modifiedData, {
              x: "x",
              y: "y",
              fill: (d) =>
                getIsPointOverlapping(d) ? "var(--color-orange)" : d.color,
              r: 4,
            }),
          ],
        }}
      />
    </div>
  );
}
