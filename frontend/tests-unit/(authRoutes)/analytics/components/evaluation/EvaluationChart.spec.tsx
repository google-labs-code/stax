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

import EvaluationChart from "@/app/(authRoutes)/analytics/components/evaluation/EvaluationChart";
import {
  ChartTabEvaluation,
  EvaluationChartDataItem,
} from "@/app/(authRoutes)/analytics/types/charts";
import { MantineProvider } from "@mantine/core";
import { render, screen } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/analytics/components/AverageAnalytics", () => ({
  __esModule: true,
  default: (props: any) => (
    <div data-testid="average-analytics">{JSON.stringify(props)}</div>
  ),
}));

jest.mock(
  "@/app/(authRoutes)/analytics/components/evaluation/ChartsRendering",
  () => ({
    __esModule: true,
    default: (props: any) => (
      <div data-testid="charts-rendering">{JSON.stringify(props)}</div>
    ),
  }),
);

function renderWithMantine(children: React.ReactNode) {
  return render(<MantineProvider>{children}</MantineProvider>);
}

describe("@/app/(authRoutes)/analytics/components/evaluation/EvaluationChart", () => {
  it("renders loader when loading", () => {
    renderWithMantine(
      <EvaluationChart data={[]} isLoading={true} isSecondFilterOn={false} />,
    );
    const loader = document.querySelector(".mantine-Loader-root");
    expect(loader).toBeInTheDocument();
  });

  it("renders empty state when data is empty", () => {
    renderWithMantine(
      <EvaluationChart data={[]} isLoading={false} isSecondFilterOn={false} />,
    );
    expect(screen.getByText(/no evaluation history/i)).toBeInTheDocument();
  });

  it("renders charts and analytics when data is present", () => {
    const mockData: EvaluationChartDataItem[] = [
      {
        evaluator: "Test Evaluator",
        monitoring: {
          total_prompt_tokens: 50,
          total_tokens: 100,
          total_completion_tokens: 50,
          average_latency: 1.23,
          average_completion_tokens: 0,
          average_prompt_tokens: 0,
          total_latency: 0,
        },
        tab: ChartTabEvaluation.BAR_CHART,
        datapoints: [
          {
            score: "0.5",
            category: "A",
            filter: "FILTER_A",
            count: 10,
            color: "blue",
            promptTokens: 0,
            completionTokens: 0,
            averageLatency: 0,
            totalLatency: 0,
            averagePromptTokens: 0,
            averageCompletionTokens: 0,
            totalTokens: 0,
          },
        ],
      },
    ];

    renderWithMantine(
      <EvaluationChart
        data={mockData}
        isLoading={false}
        isSecondFilterOn={true}
      />,
    );

    expect(screen.getByTestId("average-analytics")).toBeInTheDocument();
    expect(screen.getByTestId("charts-rendering")).toBeInTheDocument();
    expect(
      screen.queryByText(/no evaluation history/i),
    ).not.toBeInTheDocument();
  });
});
