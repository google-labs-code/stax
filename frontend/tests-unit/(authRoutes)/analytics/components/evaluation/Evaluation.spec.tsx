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

import Evaluation from "@/app/(authRoutes)/analytics/components/evaluation/Evaluation";
import { testRender } from "@/tests-unit/render";
import { act, screen } from "@testing-library/react";

jest.mock("@/hooks/useAnalyticsContext", () => ({
  useAnalyticsContext: () => ({
    selectedFilters: [
      {
        project: "proj-1",
        model: "model-1",
        tags: [],
      },
      {
        project: "proj-2",
        model: "model-2",
        tags: [],
      },
    ],
  }),
}));

jest.mock("@/app/(authRoutes)/analytics/state/queries", () => ({
  getEvaluationMonitoring: jest.fn(
    () =>
      new Promise((resolve) =>
        setTimeout(
          () =>
            resolve({
              Accuracy: {
                monitoring: { metric: 0.9 },
                datapoints: [],
              },
            }),
          300,
        ),
      ),
  ),
}));

jest.mock("@/app/(authRoutes)/analytics/components/charts/utils/hooks", () => ({
  usePDFExport: () => ({
    exportPDF: jest.fn(),
    isGenerating: false,
  }),
}));

jest.mock(
  "@/app/(authRoutes)/analytics/components/evaluation/EvaluatorsOptions",
  () => ({
    __esModule: true,
    default: () => <div data-testid="EvaluatorsOptions" />,
  }),
);

jest.mock(
  "@/app/(authRoutes)/analytics/components/evaluation/EvaluationChart",
  () => ({
    __esModule: true,
    default: ({ data, isLoading }: any) => (
      <div data-testid="EvaluationChart">
        {isLoading ? "Loading..." : `Loaded with ${data?.length} charts`}
      </div>
    ),
  }),
);

jest.mock("@/components/DateRangeInput", () => {
  return function MockDateRangeInput() {
    return <div data-testid="DateRangeInput">Date Picker</div>;
  };
});

jest.mock("@/components/ActionMenu", () => {
  return function MockActionMenu() {
    return <div data-testid="ActionMenu">Export Menu</div>;
  };
});

jest.mock("@/components/MaterialIcon", () => {
  return function MockMaterialIcon() {
    return <div data-testid="MaterialIcon">Icon</div>;
  };
});

jest.mock(
  "@/app/(authRoutes)/analytics/components/AnalyticsFiltersContainer",
  () => {
    return function MockAnalyticsFiltersContainer() {
      return <div data-testid="AnalyticsFiltersContainer">Filters</div>;
    };
  },
);

describe("Evaluation component", () => {
  it("renders key sections and loads chart data", async () => {
    await act(async () => {
      testRender(<Evaluation />);
    });

    expect(screen.getByTestId("AnalyticsFiltersContainer")).toBeInTheDocument();
    expect(screen.getByTestId("DateRangeInput")).toBeInTheDocument();
    expect(screen.getByTestId("ActionMenu")).toBeInTheDocument();
  });

  it("shows loading state while fetching", async () => {
    jest.resetModules();
    const { getEvaluationMonitoring } = await import(
      "@/app/(authRoutes)/analytics/state/queries"
    );
    (getEvaluationMonitoring as jest.Mock).mockImplementation(
      () =>
        new Promise((res) =>
          setTimeout(
            () => res({ Accuracy: { monitoring: {}, datapoints: [] } }),
            300,
          ),
        ),
    );

    await act(async () => {
      testRender(<Evaluation />);
    });

    expect(screen.getByTestId("EvaluationChart")).toHaveTextContent(
      "Loading...",
    );
  }, 10000);
});
