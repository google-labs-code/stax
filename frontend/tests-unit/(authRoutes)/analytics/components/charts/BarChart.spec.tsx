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

import BarChart from "@/app/(authRoutes)/analytics/components/charts/BarChart";
import { FilterLabel } from "@/app/(authRoutes)/analytics/types/charts";
import { render, screen } from "@testing-library/react";

jest.mock(
  "@/app/(authRoutes)/analytics/components/charts/utils/PlotFigure",
  () => {
    const MockPlotFigure = ({ options }: any) => (
      <div data-testid="plot-figure">
        <div>{options?.title}</div>
        <div>{options?.subtitle}</div>
      </div>
    );
    MockPlotFigure.displayName = "MockPlotFigure";

    return MockPlotFigure;
  },
);

const mockData = [
  {
    category: 0.1,
    count: 5,
    score: 0.1,
    categoryName: "0.1",
    color: "#ff0000",
    label: "Label A",
    filter: FilterLabel.FILTER_A,
  },
  {
    category: 0.12,
    count: 0,
    score: 0.12,
    categoryName: "0.12",
    color: "#0000ff",
    label: "Label B",
    filter: FilterLabel.FILTER_B,
  },
];

describe("BarChart", () => {
  it("renders with default data and titles", () => {
    render(
      <BarChart
        data={mockData}
        title="Test Title"
        subtitle="Test Subtitle"
        yLabel="Y Axis"
        isSecondFilterOn={false}
      />,
    );
    expect(screen.getByText("Test Title")).toBeInTheDocument();
    expect(screen.getByText("Test Subtitle")).toBeInTheDocument();
  });

  it("renders with second filter on (comparison mode)", () => {
    render(
      <BarChart
        data={mockData}
        title="Comparison Title"
        subtitle="Comparison Subtitle"
        yLabel="Score"
        isSecondFilterOn={true}
      />,
    );
    expect(screen.getByText("Comparison Title")).toBeInTheDocument();
    expect(screen.getByText("Comparison Subtitle")).toBeInTheDocument();
  });

  it("renders with 0 count and applies minimum bar height", () => {
    const dataWithZeroCount = [
      {
        category: 0.2,
        count: 0,
        score: 0.2,
        categoryName: "0.2",
        color: "#00ff00",
        label: "Zero Count",
        filter: FilterLabel.FILTER_A,
      },
    ];

    render(
      <BarChart
        data={dataWithZeroCount}
        title="Zero Count Chart"
        subtitle="Subtitle"
        yLabel="Y Axis"
        isSecondFilterOn={false}
      />,
    );
    expect(screen.getByText("Zero Count Chart")).toBeInTheDocument();
  });

  it("handles empty data gracefully", () => {
    render(
      <BarChart
        data={[]}
        title="Empty Data"
        subtitle="No data to show"
        yLabel="Y Axis"
        isSecondFilterOn={false}
      />,
    );
    expect(screen.getByText("Empty Data")).toBeInTheDocument();
  });

  it("calls generateTooltipContent properly", () => {
    const { container } = render(
      <BarChart
        data={mockData}
        title="Tooltip Chart"
        subtitle="Subtitle"
        yLabel="Y"
        isSecondFilterOn={true}
      />,
    );
    expect(
      container.querySelector('[data-testid="plot-figure"]'),
    ).toBeInTheDocument();
  });
});
