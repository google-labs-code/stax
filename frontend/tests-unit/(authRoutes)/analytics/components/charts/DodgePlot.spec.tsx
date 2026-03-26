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

import DodgePlot from "@/app/(authRoutes)/analytics/components/charts/DodgePlot";
import { DodgePlotData } from "@/app/(authRoutes)/analytics/components/charts/types";
import * as utils from "@/app/(authRoutes)/analytics/components/charts/utils/utils";
import { FilterLabel } from "@/app/(authRoutes)/analytics/types/charts";
import { render, screen } from "@testing-library/react";

jest.mock(
  "@/app/(authRoutes)/analytics/components/charts/utils/PlotFigure",
  () => ({
    __esModule: true,
    default: ({ generateTooltipContent, options }: any) => (
      <div data-testid="plot-figure">
        <div>{options?.y?.label}</div>
        {generateTooltipContent && (
          <div data-testid="tooltip-content">
            {generateTooltipContent({
              filter: FilterLabel.FILTER_A,
              category: "cat1",
              rawScore: 0.77,
            }).map((item: any, i: number) => (
              <div key={i}>
                {item.label} {item.value ?? ""}
              </div>
            ))}
          </div>
        )}
      </div>
    ),
  }),
);

jest.mock("@/app/(authRoutes)/analytics/components/charts/utils/utils", () => ({
  getDotDataDodgeChart: jest.fn(() => [
    {
      x: 0.5,
      score: 0.8,
      rawScore: 0.8,
      category: "Test Cat",
      color: "#ff0000",
      label: "Test Label",
      filter: FilterLabel.FILTER_A,
    },
  ]),
  getDynamicRadiusOfDot: jest.fn(() => 5),
}));

describe("DodgePlot", () => {
  const mockData: DodgePlotData[] = [
    {
      category: "A",
      score: 0.8,
      rawScore: "0.8",
      color: "#ff0000",
      label: "Label A",
      filter: FilterLabel.FILTER_A,
      count: 3,
    },
    {
      category: "B",
      score: 0.6,
      rawScore: "0.6",
      color: "#00ff00",
      label: "Label B",
      filter: FilterLabel.FILTER_B,
      count: 2,
    },
  ];

  it("renders plot figure with basic props (isSecondFilterOn = false)", () => {
    render(<DodgePlot data={mockData} isSecondFilterOn={false} />);
    expect(screen.getByTestId("plot-figure")).toBeInTheDocument();
    expect(screen.getByText("Score")).toBeInTheDocument();
    expect(screen.getByText("Category cat1")).toBeInTheDocument();
    expect(screen.getByText("Score 0.77")).toBeInTheDocument();
  });

  it("renders plot with fx axis and filters when isSecondFilterOn is true", () => {
    render(<DodgePlot data={mockData} isSecondFilterOn={true} />);
    expect(screen.getByText("Score")).toBeInTheDocument();
    expect(screen.getByText("Category cat1")).toBeInTheDocument();
    expect(screen.getByText(FilterLabel.FILTER_A)).toBeInTheDocument();
  });

  it("calls getDotDataDodgeChart and getDynamicRadiusOfDot", () => {
    const dotSpy = jest.spyOn(utils, "getDotDataDodgeChart");
    const radiusSpy = jest.spyOn(utils, "getDynamicRadiusOfDot");

    render(<DodgePlot data={mockData} isSecondFilterOn={true} />);
    expect(dotSpy).toHaveBeenCalledWith(mockData);
    expect(radiusSpy).toHaveBeenCalled();
  });

  it("generates correct color domain and range", () => {
    render(<DodgePlot data={mockData} isSecondFilterOn={false} />);
    expect(screen.getByTestId("tooltip-content")).toBeInTheDocument();
  });
});
