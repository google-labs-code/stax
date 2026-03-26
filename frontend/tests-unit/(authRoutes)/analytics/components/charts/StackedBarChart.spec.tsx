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

import StackedBarChart from "@/app/(authRoutes)/analytics/components/charts/StackedBarChart";
import { StackChartItem } from "@/app/(authRoutes)/analytics/components/charts/types";
import dayjs from "@/utils/dayjsSetup";
import { render, screen } from "@testing-library/react";

jest.mock(
  "@/app/(authRoutes)/analytics/components/charts/utils/PlotFigure",
  () => ({
    __esModule: true,
    default: ({ generateTooltipContent, options }: any) => (
      <div data-testid="plot-figure">
        <div>Max Y Domain: {options?.y?.domain.join(",")}</div>
        {options?.data?.map((d: any, i: number) => (
          <div key={i} data-testid="data-point">
            {d.value} - {d.type}
          </div>
        ))}
        <div data-testid="tooltip-content">
          {generateTooltipContent(options.data[0]).map(
            (item: any, i: number) => (
              <div key={i}>
                {item.label} {item.value ?? ""}
              </div>
            ),
          )}
        </div>
      </div>
    ),
  }),
);

describe("StackedBarChart", () => {
  const baseData: StackChartItem[] = [
    { value: 10, type: "A", windowStart: new Date("2023-01-01T00:00:00Z") },
    { value: 20, type: "B", windowStart: new Date("2023-01-01T00:00:00Z") },
  ];

  it("renders and displays maxY correctly", () => {
    render(<StackedBarChart data={baseData} x="date" y="value" fill="type" />);
    expect(screen.getByText("Max Y Domain: 0,20")).toBeInTheDocument();
  });

  it("uses default maxY of 10 if all values are zero", () => {
    const zeroData: StackChartItem[] = [
      { value: 0, type: "A", windowStart: new Date("2023-01-01T00:00:00Z") },
    ];
    render(<StackedBarChart data={zeroData} x="date" y="value" fill="type" />);
    expect(screen.getByText("Max Y Domain: 0,10")).toBeInTheDocument();
  });

  it("generates tooltip content correctly", () => {
    render(<StackedBarChart data={baseData} x="date" y="value" fill="type" />);
    const tooltip = screen.getByTestId("tooltip-content");
    expect(tooltip).toHaveTextContent("Date");
    expect(tooltip).toHaveTextContent(
      dayjs(baseData[0].value).format("MMM DD"),
    );
    expect(tooltip).toHaveTextContent(baseData[0].type);
    expect(tooltip).toHaveTextContent(baseData[0].value.toString());
  });
});
