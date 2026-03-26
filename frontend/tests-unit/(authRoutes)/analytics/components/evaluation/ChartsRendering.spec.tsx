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

import ChartsRendering from "@/app/(authRoutes)/analytics/components/evaluation/ChartsRendering";
import {
  ChartTabEvaluation,
  EvaluationChartDataItem,
} from "@/app/(authRoutes)/analytics/types/charts";
import { MantineProvider } from "@mantine/core";
import { fireEvent, render, screen } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/analytics/components/charts/BarChart", () => ({
  __esModule: true,
  default: (props: any) => (
    <div data-testid="bar-chart">{JSON.stringify(props)}</div>
  ),
}));

jest.mock("@/app/(authRoutes)/analytics/components/charts/DodgePlot", () => ({
  __esModule: true,
  default: (props: any) => (
    <div data-testid="dodge-plot">{JSON.stringify(props)}</div>
  ),
}));

const mockChart: EvaluationChartDataItem = {
  evaluator: "Chat Quality",
  tab: ChartTabEvaluation.BAR_CHART,
  monitoring: {
    average_prompt_tokens: 913,
    total_prompt_tokens: 913,
    average_completion_tokens: 67,
    total_completion_tokens: 67,
    average_latency: 3652,
    total_latency: 3652,
    total_tokens: 980,
  },
  datapoints: [
    {
      category: "Very good",
      score: "1.0",
      count: 1,
      color: "var(--color-green)",
      promptTokens: 913,
      completionTokens: 67,
      averageLatency: 3652,
      totalLatency: 3652,
      averagePromptTokens: 913,
      averageCompletionTokens: 67,
      totalTokens: 980,
      filter: "Filter A",
    },
    {
      category: "Good",
      score: "0.75",
      count: 0,
      color: "var(--color-purple)",
      promptTokens: 0,
      completionTokens: 0,
      averageLatency: 0,
      totalLatency: 0,
      averagePromptTokens: 0,
      averageCompletionTokens: 0,
      totalTokens: 0,
      filter: "Filter A",
    },
    {
      category: "Ok",
      score: "0.5",
      count: 0,
      color: "var(--color-orange)",
      promptTokens: 0,
      completionTokens: 0,
      averageLatency: 0,
      totalLatency: 0,
      averagePromptTokens: 0,
      averageCompletionTokens: 0,
      totalTokens: 0,
      filter: "Filter A",
    },
    {
      category: "Bad",
      score: "0.25",
      count: 0,
      color: "var(--color-pink)",
      promptTokens: 0,
      completionTokens: 0,
      averageLatency: 0,
      totalLatency: 0,
      averagePromptTokens: 0,
      averageCompletionTokens: 0,
      totalTokens: 0,
      filter: "Filter A",
    },
    {
      category: "Very bad",
      score: "0.0",
      count: 0,
      color: "var(--color-red)",
      promptTokens: 0,
      completionTokens: 0,
      averageLatency: 0,
      totalLatency: 0,
      averagePromptTokens: 0,
      averageCompletionTokens: 0,
      totalTokens: 0,
      filter: "Filter A",
    },
  ],
};

function renderWithMantine(children: React.ReactNode) {
  return render(<MantineProvider>{children}</MantineProvider>);
}

describe("ChartsRendering", () => {
  it("renders BarChart initially", () => {
    renderWithMantine(
      <ChartsRendering chart={mockChart} isSecondFilterOn={true} />,
    );
    expect(screen.getByTestId("bar-chart")).toBeInTheDocument();
    expect(screen.queryByTestId("dodge-plot")).not.toBeInTheDocument();
  });

  it("switches to DodgePlot when tab is changed", () => {
    renderWithMantine(
      <ChartsRendering chart={mockChart} isSecondFilterOn={false} />,
    );
    const tab = screen.getAllByText("Dodge plot");
    fireEvent.click(tab[0]);
    expect(screen.getByTestId("dodge-plot")).toBeInTheDocument();
    expect(screen.queryByTestId("bar-chart")).not.toBeInTheDocument();
  });

  it("passes correct props to BarChart", () => {
    renderWithMantine(
      <ChartsRendering chart={mockChart} isSecondFilterOn={true} />,
    );
    const chartProps = JSON.parse(
      screen.getByTestId("bar-chart").textContent || "{}",
    );
    expect(chartProps.data).toHaveLength(5);
    expect(chartProps.isSecondFilterOn).toBe(true);
    expect(chartProps.data[0].categoryName).toBe("Very good");
    expect(chartProps.data[0].category).toBe(1);
  });

  it("passes correct props to DodgePlot", () => {
    renderWithMantine(
      <ChartsRendering
        chart={{ ...mockChart, tab: ChartTabEvaluation.DODGE_PLOT }}
        isSecondFilterOn={false}
      />,
    );
    const chartProps = JSON.parse(
      screen.getByTestId("dodge-plot").textContent || "{}",
    );
    expect(chartProps.data).toHaveLength(5);
    expect(chartProps.isSecondFilterOn).toBe(false);
    expect(chartProps.data[0].rawScore).toBe("1.0");
    expect(chartProps.data[0].score).toBe(1);
  });
});
