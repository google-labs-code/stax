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

import LineChart from "@/app/(authRoutes)/analytics/components/charts/LineChart";
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
            {d.windowStart} - {d.value}
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

describe("LineChart", () => {
  const baseData = [
    {
      value: 10,
      windowStart: "2023-01-01T00:00:00Z",
      filter: "FILTER_A",
      label: "Latency",
    },
    {
      value: 20,
      windowStart: "2023-01-02T00:00:00Z",
      filter: "FILTER_B",
      label: "Accuracy",
    },
    {
      value: 10,
      windowStart: "2023-01-01T00:00:00Z",
      filter: "FILTER_B",
      label: "Latency",
    },
  ];

  it("calculates maxY correctly", () => {
    render(<LineChart data={baseData} isSecondFilterOn={false} />);
    expect(screen.getByText("Max Y Domain: 0,22")).toBeInTheDocument();
  });

  it("renders tooltip content for non-overlapping point when isSecondFilterOn=false", () => {
    render(<LineChart data={baseData} isSecondFilterOn={false} />);
    expect(screen.getByTestId("tooltip-content")).toHaveTextContent(
      "Date Jan 01",
    );
    expect(screen.getByTestId("tooltip-content")).toHaveTextContent(
      "Latency 10 s",
    );
    expect(screen.getByTestId("tooltip-content")).not.toHaveTextContent(
      "Filter A",
    );
  });

  it("renders tooltip content for overlapping point when isSecondFilterOn=true", () => {
    render(<LineChart data={baseData} isSecondFilterOn={true} />);
    expect(screen.getByTestId("tooltip-content")).toHaveTextContent("Filter A");
    expect(screen.getByTestId("tooltip-content")).toHaveTextContent("Filter B");
    expect(screen.getByTestId("tooltip-content")).toHaveTextContent(
      "Latency 10 s",
    );
    expect(screen.getByTestId("tooltip-content")).toHaveTextContent(
      "Date Jan 01",
    );
  });

  it("getIsPointOverlapping returns true for overlapping points", () => {
    render(<LineChart data={baseData} isSecondFilterOn={true} />);
    expect(screen.getByTestId("tooltip-content")).toBeInTheDocument();
  });

  it("colors and fillIds are correctly assigned", () => {
    render(<LineChart data={baseData} isSecondFilterOn={false} />);
    baseData.forEach((d) => {
      const expectedColor =
        d.filter === "FILTER_B" ? "var(--color-orange)" : "var(--color-brand)";
      const expectedFillId =
        d.filter === "FILTER_B"
          ? "url(#gradient-orange)"
          : "url(#gradient-blue)";
      expect(expectedColor).toBeDefined();
      expect(expectedFillId).toBeDefined();
    });
  });
});
