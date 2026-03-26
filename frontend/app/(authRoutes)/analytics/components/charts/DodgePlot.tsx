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

import { FilterLabel } from "../../types/charts";
import { generateRange } from "../utils";
import { DodgePlotProps } from "./types";
import PlotFigure from "./utils/PlotFigure";
import { CHART_COLORS } from "./utils/chartConstants";
import { getDotDataDodgeChart, getDynamicRadiusOfDot } from "./utils/utils";

export default function DodgePlot({ data, isSecondFilterOn }: DodgePlotProps) {
  const dotData = getDotDataDodgeChart(data);
  const generateTooltipContent = (dataPoint: any) => [
    {
      label: isSecondFilterOn ? dataPoint.filter : "",
    },
    {
      label: "Category",
      value: dataPoint.category,
    },
    {
      label: "Score",
      value: dataPoint.rawScore,
    },
  ];

  return (
    <PlotFigure
      className="chart-with-large-y-labels"
      generateTooltipContent={generateTooltipContent}
      options={{
        data: dotData,
        height: 400,
        x: {
          label: null,
          type: "band",
          domain: generateRange(-0.7, 1.7, 0.01).reverse(),
          axis: null,
        },
        y: {
          label: "Score",
          domain: [0, 1.09],
          tickFormat: ".1f",
          ticks: generateRange(0, 1, 0.1),
        },
        fx: isSecondFilterOn
          ? {
              label: null,
              domain: [FilterLabel.FILTER_A, FilterLabel.FILTER_B],
            }
          : undefined,
        color: {
          legend: true,
          range: data.map((d) => d.color).reverse(),
          domain: data.map((d) => d.label).reverse(),
        },
        marks: [
          Plot.gridY({
            stroke: CHART_COLORS.axis,
            strokeOpacity: 0.5,
            ticks: generateRange(0, 1, 0.1),
          }),
          Plot.frame({
            stroke: CHART_COLORS.grid,
            anchor: "bottom",
          }),
          isSecondFilterOn
            ? Plot.axisFx({
                anchor: "bottom",
                dy: 10,
              })
            : undefined,
          Plot.dot(
            dotData,
            Plot.dodgeX("middle", {
              x: "x",
              fx: isSecondFilterOn ? "filter" : undefined,
              y: "score",
              fill: "color",
              r: getDynamicRadiusOfDot(dotData, isSecondFilterOn),
            }),
          ),
        ],
      }}
    />
  );
}
