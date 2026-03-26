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

import InferenceChart from "@/app/(authRoutes)/analytics/components/inference/InferenceChart";
import { getInferenceMonitoring } from "@/app/(authRoutes)/analytics/state/queries";
import {
  ChartTabInference,
  InferenceMonitoringResponse,
} from "@/app/(authRoutes)/analytics/types/charts";
import { testRender } from "@/tests-unit/render";
import dayjs from "@/utils/dayjsSetup";
import { act, fireEvent, screen, waitFor } from "@testing-library/react";

// Mock dependencies
jest.mock("@/app/(authRoutes)/analytics/state/queries", () => ({
  getInferenceMonitoring: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/analytics/components/AverageAnalytics", () => ({
  __esModule: true,
  default: (props: any) => (
    <div data-testid="average-analytics">{JSON.stringify(props)}</div>
  ),
}));

jest.mock("@/app/(authRoutes)/analytics/components/charts/LineChart", () => ({
  __esModule: true,
  default: (props: any) => (
    <div data-testid="line-chart">{JSON.stringify(props)}</div>
  ),
}));

jest.mock(
  "@/app/(authRoutes)/analytics/components/charts/StackedBarChart",
  () => ({
    __esModule: true,
    default: (props: any) => (
      <div data-testid="stacked-bar-chart">{JSON.stringify(props)}</div>
    ),
  }),
);

jest.mock("@/app/(authRoutes)/analytics/components/charts/utils/hooks", () => ({
  usePDFExport: jest.fn(() => ({
    exportPDF: jest.fn(),
    isGenerating: false,
  })),
}));

jest.mock("@/components/ActionMenu", () => ({
  __esModule: true,
  default: ({ menuItems }: any) => (
    <div data-testid="action-menu">
      {menuItems.map((item: any, index: number) => (
        <button
          key={index}
          data-testid={`action-menu-item-${item.label.toLowerCase().replace(/\s+/g, "-")}`}
          onClick={item.onClick}
        >
          {item.label}
        </button>
      ))}
    </div>
  ),
}));

jest.mock("@/components/DateRangeInput", () => ({
  __esModule: true,
  default: ({ onChange, defaultRange }: any) => (
    <div data-testid="date-range-input">
      <button
        data-testid="date-range-change"
        onClick={() => onChange([dayjs("2024-01-01"), dayjs("2024-01-02")])}
      >
        Change Date
      </button>
      <span data-testid="date-range-value">
        {defaultRange?.map((d: any) => d.format("YYYY-MM-DD")).join(" - ")}
      </span>
    </div>
  ),
}));

const mockGetInferenceMonitoring =
  getInferenceMonitoring as jest.MockedFunction<typeof getInferenceMonitoring>;

const mockUsePDFExport =
  require("@/app/(authRoutes)/analytics/components/charts/utils/hooks")
    .usePDFExport as jest.MockedFunction<any>;

describe("InferenceChart", () => {
  const mockDuplicateChart = jest.fn();
  const mockDeleteChart = jest.fn();
  const mockOnDateChange = jest.fn();
  const mockExportPDF = jest.fn();

  const defaultProps = {
    selectedFilters: [
      {
        project: "proj-1",
        model: {
          id: "model-1",
          provider: "OPENAI" as any,
          name: "model-1",
          version: "1.0",
          label: "Model 1",
          url: "",
          tag: "",
          model_type: "",
          properties: {},
          additional_headers: {},
        },
        tags: [],
      },
    ] as any,
    selectedDates: [dayjs("2024-01-01"), dayjs("2024-01-02")],
    selectedDatesLabel: "Last 7 days",
    onDateChange: mockOnDateChange,
    duplicateChart: mockDuplicateChart,
    deleteChart: mockDeleteChart,
  };

  const mockInferenceResponse: InferenceMonitoringResponse = {
    time_series_analytics: [
      {
        windowStart: "2024-01-01T00:00:00Z",
        totalInferences: 100,
        average_avg_chat_latency: 1500,
        total_chat_prompt_tokens: 5000,
        total_chat_completion_tokens: 3000,
        total_chat_total_tokens: 8000,
      },
      {
        windowStart: "2024-01-02T00:00:00Z",
        totalInferences: 150,
        average_avg_chat_latency: 2000,
        total_chat_prompt_tokens: 7000,
        total_chat_completion_tokens: 4000,
        total_chat_total_tokens: 11000,
      },
    ],
    overall_inferences: 250,
    overall_average_latency: 1750,
    overall_prompt_tokens: 12000,
    overall_completion_tokens: 7000,
    overall_total_tokens: 19000,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockUsePDFExport.mockReturnValue({
      exportPDF: mockExportPDF,
      isGenerating: false,
    });
    mockGetInferenceMonitoring.mockResolvedValue(mockInferenceResponse);
  });

  describe("rendering", () => {
    it("renders the component with initial state", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      expect(screen.getByTestId("average-analytics")).toBeInTheDocument();
      expect(screen.getByTestId("date-range-input")).toBeInTheDocument();
      expect(screen.getByTestId("action-menu")).toBeInTheDocument();
    });

    it("shows loading overlay while fetching data", async () => {
      let resolvePromise: (value: InferenceMonitoringResponse) => void;
      const pendingPromise = new Promise<InferenceMonitoringResponse>(
        (resolve) => {
          resolvePromise = resolve;
        },
      );

      mockGetInferenceMonitoring.mockReturnValue(pendingPromise);

      testRender(<InferenceChart {...defaultProps} />);

      // Check for loading overlay
      await waitFor(() => {
        const loadingOverlay = document.querySelector(
          ".mantine-LoadingOverlay-root",
        );
        expect(loadingOverlay).toBeInTheDocument();
      });

      // Resolve the promise
      await act(async () => {
        resolvePromise!(mockInferenceResponse);
        await pendingPromise;
      });

      await waitFor(() => {
        expect(
          document.querySelector(".mantine-LoadingOverlay-root"),
        ).not.toBeInTheDocument();
      });
    });

    it("shows loader when generating PDF", () => {
      mockUsePDFExport.mockReturnValue({
        exportPDF: mockExportPDF,
        isGenerating: true,
      });

      testRender(<InferenceChart {...defaultProps} />);

      const loader = document.querySelector(".mantine-Loader-root");
      expect(loader).toBeInTheDocument();
    });
  });

  describe("data fetching and processing", () => {
    it("fetches data for single filter", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalledTimes(1);
      });

      const callArgs = mockGetInferenceMonitoring.mock.calls[0][0];
      expect(callArgs).toMatchObject({
        aggregateWindow: "daily",
      });
    });

    it("fetches data for multiple filters", async () => {
      const propsWithMultipleFilters = {
        ...defaultProps,
        selectedFilters: [
          {
            project: "proj-1",
            model: {
              id: "model-1",
              provider: "OPENAI" as any,
              name: "model-1",
              version: "1.0",
              label: "Model 1",
              url: "",
              tag: "",
              model_type: "",
              properties: {},
              additional_headers: {},
            },
            tags: [],
          },
          {
            project: "proj-2",
            model: {
              id: "model-2",
              provider: "ANTHROPIC" as any,
              name: "model-2",
              version: "1.0",
              label: "Model 2",
              url: "",
              tag: "",
              model_type: "",
              properties: {},
              additional_headers: {},
            },
            tags: [],
          },
        ] as any,
      };

      testRender(<InferenceChart {...propsWithMultipleFilters} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalledTimes(2);
      });
    });

    it("processes and displays analytics data correctly", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        const averageAnalytics = screen.getByTestId("average-analytics");
        const props = JSON.parse(averageAnalytics.textContent || "{}");
        expect(props.prompts).toBe(12000);
        expect(props.completed).toBe(7000);
        expect(props.total).toBe(19000);
        expect(props.time).toBe(1750);
        expect(props.isSecondFilterOn).toBe(false);
      });
    });

    it("handles multiple filters and sets isSecondFilterOn correctly", async () => {
      const propsWithMultipleFilters = {
        ...defaultProps,
        selectedFilters: [
          {
            project: "proj-1",
            model: {
              id: "model-1",
              provider: "OPENAI" as any,
              name: "model-1",
              version: "1.0",
              label: "Model 1",
              url: "",
              tag: "",
              model_type: "",
              properties: {},
              additional_headers: {},
            },
            tags: [],
          },
          {
            project: "proj-2",
            model: {
              id: "model-2",
              provider: "ANTHROPIC" as any,
              name: "model-2",
              version: "1.0",
              label: "Model 2",
              url: "",
              tag: "",
              model_type: "",
              properties: {},
              additional_headers: {},
            },
            tags: [],
          },
        ] as any,
      };

      testRender(<InferenceChart {...propsWithMultipleFilters} />);

      await waitFor(() => {
        const averageAnalytics = screen.getByTestId("average-analytics");
        const props = JSON.parse(averageAnalytics.textContent || "{}");
        expect(props.isSecondFilterOn).toBe(true);
      });
    });

    it("handles empty time series data", async () => {
      const emptyResponse: InferenceMonitoringResponse = {
        time_series_analytics: [],
        overall_inferences: 0,
        overall_average_latency: 0,
        overall_prompt_tokens: 0,
        overall_completion_tokens: 0,
        overall_total_tokens: 0,
      };

      mockGetInferenceMonitoring.mockResolvedValue(emptyResponse);

      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      // Should still render without errors
      expect(screen.getByTestId("average-analytics")).toBeInTheDocument();
    });

    it("handles errors gracefully", async () => {
      mockGetInferenceMonitoring.mockRejectedValue(new Error("API Error"));

      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      // Component should still render
      expect(screen.getByTestId("average-analytics")).toBeInTheDocument();
    });
  });

  describe("tab switching", () => {
    it("defaults to LATENCY tab", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      // Check that line chart is rendered (used for Latency and Queries)
      expect(screen.getByTestId("line-chart")).toBeInTheDocument();
      expect(screen.queryByTestId("stacked-bar-chart")).not.toBeInTheDocument();
    });

    it("switches to QUERIES tab", async () => {
      const { container } = testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      const segmentedControl = container.querySelector(
        ".mantine-SegmentedControl-root",
      );
      expect(segmentedControl).toBeInTheDocument();

      const queriesButton = Array.from(
        segmentedControl!.querySelectorAll("input"),
      ).find((input) => input.value === ChartTabInference.QUERIES);

      if (queriesButton) {
        fireEvent.click(queriesButton);
      }

      await waitFor(() => {
        expect(screen.getByTestId("line-chart")).toBeInTheDocument();
      });
    });

    it("switches to TOKENS tab and shows stacked bar chart", async () => {
      const { container } = testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      const segmentedControl = container.querySelector(
        ".mantine-SegmentedControl-root",
      );
      expect(segmentedControl).toBeInTheDocument();

      const tokensButton = Array.from(
        segmentedControl!.querySelectorAll("input"),
      ).find((input) => input.value === ChartTabInference.TOKENS);

      if (tokensButton) {
        fireEvent.click(tokensButton);
      }

      await waitFor(() => {
        expect(screen.getByTestId("stacked-bar-chart")).toBeInTheDocument();
        expect(screen.queryByTestId("line-chart")).not.toBeInTheDocument();
      });
    });

    it("displays latency disclaimer when on LATENCY tab", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      expect(
        screen.getByText(
          /Latency results may differ from production performance/i,
        ),
      ).toBeInTheDocument();
    });

    it("does not display latency disclaimer on other tabs", async () => {
      const { container } = testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      const segmentedControl = container.querySelector(
        ".mantine-SegmentedControl-root",
      );
      expect(segmentedControl).toBeInTheDocument();

      const queriesButton = Array.from(
        segmentedControl!.querySelectorAll("input"),
      ).find((input) => input.value === ChartTabInference.QUERIES);

      if (queriesButton) {
        fireEvent.click(queriesButton);
      }

      await waitFor(() => {
        expect(
          screen.queryByText(
            /Latency results may differ from production performance/i,
          ),
        ).not.toBeInTheDocument();
      });
    });
  });

  describe("line chart data transformation", () => {
    it("transforms data correctly for LATENCY tab", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      await waitFor(() => {
        const lineChart = screen.getByTestId("line-chart");
        const props = JSON.parse(lineChart.textContent || "{}");
        expect(props.data).toBeDefined();
        expect(Array.isArray(props.data)).toBe(true);
        expect(props.data.length).toBeGreaterThan(0);
        expect(props.data[0].label).toBe("Latency");
        // Latency should be converted from ms to seconds (1500ms / 1000 = 1.5)
        expect(props.data[0].value).toBeCloseTo(1.5, 2);
      });
    });

    it("transforms data correctly for QUERIES tab", async () => {
      const { container } = testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      // Switch to Queries tab
      const segmentedControl = container.querySelector(
        ".mantine-SegmentedControl-root",
      );
      expect(segmentedControl).toBeInTheDocument();

      const queriesButton = Array.from(
        segmentedControl!.querySelectorAll("input"),
      ).find((input) => input.value === ChartTabInference.QUERIES);

      if (queriesButton) {
        fireEvent.click(queriesButton);
      }

      await waitFor(() => {
        const lineChart = screen.getByTestId("line-chart");
        const props = JSON.parse(lineChart.textContent || "{}");
        expect(props.data[0].label).toBe("Queries");
        expect(props.data[0].value).toBe(100); // totalInferences
      });
    });
  });

  describe("action menu", () => {
    it("calls duplicateChart when duplicate is clicked", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      const duplicateButton = screen.getByTestId("action-menu-item-duplicate");
      fireEvent.click(duplicateButton);

      expect(mockDuplicateChart).toHaveBeenCalledTimes(1);
    });

    it("calls exportPDF when export chart is clicked", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      const exportButton = screen.getByTestId("action-menu-item-export-chart");
      fireEvent.click(exportButton);

      expect(mockExportPDF).toHaveBeenCalledTimes(1);
      expect(mockExportPDF).toHaveBeenCalledWith(
        expect.any(Object),
        "inference-charts",
      );
    });

    it("calls deleteChart when delete is clicked", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      const deleteButton = screen.getByTestId("action-menu-item-delete");
      fireEvent.click(deleteButton);

      expect(mockDeleteChart).toHaveBeenCalledTimes(1);
    });

    it("does not show delete option when deleteChart is not provided", () => {
      const propsWithoutDelete = {
        ...defaultProps,
        deleteChart: undefined,
      };

      testRender(<InferenceChart {...propsWithoutDelete} />);

      expect(
        screen.queryByTestId("action-menu-item-delete"),
      ).not.toBeInTheDocument();
    });
  });

  describe("date range", () => {
    it("displays selected dates", () => {
      testRender(<InferenceChart {...defaultProps} />);

      const dateRangeValue = screen.getByTestId("date-range-value");
      expect(dateRangeValue.textContent).toContain("2024-01-01");
      expect(dateRangeValue.textContent).toContain("2024-01-02");
    });

    it("calls onDateChange when date range is changed", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      const dateRangeChange = screen.getByTestId("date-range-change");
      fireEvent.click(dateRangeChange);

      expect(mockOnDateChange).toHaveBeenCalled();
    });

    it("refetches data when dates change", async () => {
      const { rerender } = testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalledTimes(1);
      });

      const newDates = [dayjs("2024-02-01"), dayjs("2024-02-02")];
      rerender(<InferenceChart {...defaultProps} selectedDates={newDates} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe("filter label assignment", () => {
    it("assigns ALL_MODELS label for single filter", async () => {
      testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      await waitFor(() => {
        const lineChart = screen.getByTestId("line-chart");
        const props = JSON.parse(lineChart.textContent || "{}");
        expect(props.data).toBeDefined();
        expect(Array.isArray(props.data)).toBe(true);
        expect(props.data.length).toBeGreaterThan(0);
        expect(props.data[0].filter).toBe("All models");
      });
    });

    it("assigns FILTER_A and FILTER_B labels for multiple filters", async () => {
      const propsWithMultipleFilters = {
        ...defaultProps,
        selectedFilters: [
          {
            project: "proj-1",
            model: {
              id: "model-1",
              provider: "OPENAI" as any,
              name: "model-1",
              version: "1.0",
              label: "Model 1",
              url: "",
              tag: "",
              model_type: "",
              properties: {},
              additional_headers: {},
            },
            tags: [],
          },
          {
            project: "proj-2",
            model: {
              id: "model-2",
              provider: "ANTHROPIC" as any,
              name: "model-2",
              version: "1.0",
              label: "Model 2",
              url: "",
              tag: "",
              model_type: "",
              properties: {},
              additional_headers: {},
            },
            tags: [],
          },
        ] as any,
      };

      mockGetInferenceMonitoring
        .mockResolvedValueOnce(mockInferenceResponse)
        .mockResolvedValueOnce(mockInferenceResponse);

      testRender(<InferenceChart {...propsWithMultipleFilters} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalledTimes(2);
      });

      await waitFor(() => {
        const lineChart = screen.getByTestId("line-chart");
        const props = JSON.parse(lineChart.textContent || "{}");
        expect(props.data).toBeDefined();
        expect(Array.isArray(props.data)).toBe(true);
        expect(props.data.length).toBeGreaterThan(0);
        // Should have data from both filters
        const filters = props.data.map((d: any) => d.filter);
        expect(filters).toContain("Filter A");
        expect(filters).toContain("Filter B");
      });
    });
  });

  describe("stacked bar chart data", () => {
    it("generates stacked bar data for TOKENS tab", async () => {
      const { container } = testRender(<InferenceChart {...defaultProps} />);

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalled();
      });

      // Switch to Tokens tab
      const segmentedControl = container.querySelector(
        ".mantine-SegmentedControl-root",
      );
      expect(segmentedControl).toBeInTheDocument();

      const tokensButton = Array.from(
        segmentedControl!.querySelectorAll("input"),
      ).find((input) => input.value === ChartTabInference.TOKENS);

      if (tokensButton) {
        fireEvent.click(tokensButton);
      }

      await waitFor(() => {
        const stackedBarChart = screen.getByTestId("stacked-bar-chart");
        const props = JSON.parse(stackedBarChart.textContent || "{}");
        expect(props.data).toBeDefined();
        expect(Array.isArray(props.data)).toBe(true);
        // Should have data for both input and output tokens
        expect(props.data.length).toBeGreaterThan(0);
      });
    });

    it("includes filter labels in stacked bar data for multiple filters", async () => {
      const propsWithMultipleFilters = {
        ...defaultProps,
        selectedFilters: [
          {
            project: "proj-1",
            model: {
              id: "model-1",
              provider: "OPENAI" as any,
              name: "model-1",
              version: "1.0",
              label: "Model 1",
              url: "",
              tag: "",
              model_type: "",
              properties: {},
              additional_headers: {},
            },
            tags: [],
          },
          {
            project: "proj-2",
            model: {
              id: "model-2",
              provider: "ANTHROPIC" as any,
              name: "model-2",
              version: "1.0",
              label: "Model 2",
              url: "",
              tag: "",
              model_type: "",
              properties: {},
              additional_headers: {},
            },
            tags: [],
          },
        ] as any,
      };

      mockGetInferenceMonitoring
        .mockResolvedValueOnce(mockInferenceResponse)
        .mockResolvedValueOnce(mockInferenceResponse);

      const { container } = testRender(
        <InferenceChart {...propsWithMultipleFilters} />,
      );

      await waitFor(() => {
        expect(mockGetInferenceMonitoring).toHaveBeenCalledTimes(2);
      });

      // Switch to Tokens tab
      const segmentedControl = container.querySelector(
        ".mantine-SegmentedControl-root",
      );
      expect(segmentedControl).toBeInTheDocument();

      const tokensButton = Array.from(
        segmentedControl!.querySelectorAll("input"),
      ).find((input) => input.value === ChartTabInference.TOKENS);

      if (tokensButton) {
        fireEvent.click(tokensButton);
      }

      await waitFor(() => {
        const stackedBarChart = screen.getByTestId("stacked-bar-chart");
        const props = JSON.parse(stackedBarChart.textContent || "{}");
        const types = props.data.map((d: any) => d.type);
        // Should include filter labels
        expect(types.some((t: string) => t.includes("Filter A"))).toBe(true);
        expect(types.some((t: string) => t.includes("Filter B"))).toBe(true);
      });
    });
  });
});
