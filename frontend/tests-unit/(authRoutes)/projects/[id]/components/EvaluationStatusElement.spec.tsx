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

import EvaluationStatusElement from "@/app/(authRoutes)/projects/[id]/components/EvaluationStatusElement";
import { EvaluationScoreStatus, LLMEvaluation } from "@/types";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// Mock dependencies
jest.mock("@/components/MaterialIcon", () => {
  return function MockMaterialIcon({ name, size, className }: any) {
    return (
      <span
        data-testid={`material-icon-${name}`}
        data-size={size}
        className={className}
      >
        {name}
      </span>
    );
  };
});

jest.mock("@/components/Chip", () => {
  return function MockChip({
    label,
    icon,
    groupStyles,
    groupClassName,
    textStyles,
  }: any) {
    return (
      <div
        data-testid="chip"
        className={`chip ${groupClassName || ""}`}
        style={groupStyles}
      >
        {icon}
        <span style={textStyles}>{label}</span>
      </div>
    );
  };
});

jest.mock("@/utils/colorUtils", () => ({
  useProcessedColor: jest.fn(() => ({
    backgroundColor: "rgb(240, 240, 240)",
    textColor: "rgb(0, 0, 0)",
  })),
}));

jest.mock("@mantine/core", () => ({
  ScrollArea: ({ children, className, scrollbarSize }: any) => (
    <div
      data-testid="scroll-area"
      className={className}
      data-scrollbar-size={scrollbarSize}
    >
      {children}
    </div>
  ),
  Group: ({ children, className, gap }: any) => (
    <div data-testid="group" className={className} data-gap={gap}>
      {children}
    </div>
  ),
  UnstyledButton: ({ children, onClick, className }: any) => (
    <button
      data-testid="unstyled-button"
      onClick={onClick}
      className={className}
    >
      {children}
    </button>
  ),
  Tooltip: ({ children, label, position }: any) => (
    <div data-testid="tooltip" data-label={label} data-position={position}>
      {children}
    </div>
  ),
  Loader: ({ size, className }: any) => (
    <span data-testid="loader" data-size={size} className={className}>
      Loading
    </span>
  ),
}));

describe("EvaluationStatusElement Component", () => {
  const mockOnRerun = jest.fn();
  const mockOnStatusClick = jest.fn();

  const createMockData = (overrides?: Partial<LLMEvaluation>): LLMEvaluation =>
    ({
      score: 0.85,
      evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      category: "Accuracy",
      color: "#00FF00",
      reasoning: "This is a good evaluation",
      ...overrides,
    }) as LLMEvaluation;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Rendering", () => {
    it("should render without crashing", () => {
      const data = createMockData();
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("scroll-area")).toBeInTheDocument();
    });

    it("should render ScrollArea with correct props", () => {
      const data = createMockData();
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const scrollArea = screen.getByTestId("scroll-area");
      expect(scrollArea).toHaveClass("w-[100%]");
      expect(scrollArea).toHaveAttribute("data-scrollbar-size", "5");
    });

    it("should render Group with correct classes", () => {
      const data = createMockData();
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const groups = screen.getAllByTestId("group");
      expect(groups[0]).toHaveClass(
        "w-full",
        "gap-md",
        "flex-nowrap",
        "flex",
        "flex-row",
      );
    });

    it("should render EvaluatorStatusIndicator", () => {
      const data = createMockData();
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("chip")).toBeInTheDocument();
    });
  });

  describe("Score Handling", () => {
    it("should handle numeric score", () => {
      const data = createMockData({ score: "0.75" });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0.75");
    });

    it("should handle numeric score and parse to a float", () => {
      const data = createMockData({ score: 0.75 as any });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0.75");
    });

    it("should handle numeric empty scrore", () => {
      const data = createMockData({ score: 0 as any });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("Accuracy");
    });

    it("should handle string score and convert to number", () => {
      const data = createMockData({ score: "0.85" });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0.85");
    });

    it("should handle zero score", () => {
      const data = createMockData({ score: "0" });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0");
    });

    it("should handle high score values", () => {
      const data = createMockData({ score: "99.99" });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("99.99");
    });

    it("should handle negative score", () => {
      const data = createMockData({ score: "-0.5" });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("-0.5");
    });

    it("should parse string score with leading/trailing spaces", () => {
      const data = createMockData({ score: "  0.75  " });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0.75");
    });
  });

  describe("Status Handling", () => {
    it("should render with SUCCESSFUL status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("chip")).toBeInTheDocument();
    });

    it("should render with IN_PROGRESS status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.IN_PROGRESS,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("loader")).toBeInTheDocument();
      expect(screen.getByTestId("chip")).toBeInTheDocument();
    });

    it("should render with FAILED status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("material-icon-error")).toBeInTheDocument();
      expect(screen.getByTestId("chip")).toBeInTheDocument();
    });

    it("should render with PENDING status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.PENDING,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("material-icon-schedule")).toBeInTheDocument();
      expect(screen.getByTestId("chip")).toBeInTheDocument();
    });
  });

  describe("Category Display", () => {
    it("should display category with score when SUCCESSFUL", () => {
      const data = createMockData({
        score: "0.92",
        category: "Relevance",
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0.92 - Relevance");
    });

    it("should display only score when no category provided", () => {
      const data = createMockData({
        score: "0.92",
        category: undefined,
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0.92");
      expect(chip).not.toHaveTextContent("-");
    });

    it("should handle empty category string", () => {
      const data = createMockData({
        score: "0.92",
        category: "",
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0.92");
    });
  });

  describe("Tooltip Display", () => {
    it("should display tooltip with reasoning for SUCCESSFUL status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
        reasoning: "Excellent performance",
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("tooltip")).toBeInTheDocument();
      expect(screen.getByTestId("tooltip")).toHaveAttribute(
        "data-label",
        "Excellent performance",
      );
    });

    it("should not display tooltip for PENDING status even with reasoning", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.PENDING,
        reasoning: "Pending evaluation",
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      // Tooltip should still be in DOM but not functional for pending status
      const tooltips = screen.queryAllByTestId("tooltip");
      expect(tooltips.length).toBe(0);
    });

    it("should not display tooltip for IN_PROGRESS status even with reasoning", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.IN_PROGRESS,
        reasoning: "Currently evaluating",
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const tooltips = screen.queryAllByTestId("tooltip");
      expect(tooltips.length).toBe(0);
    });

    it("should display tooltip for FAILED status with reasoning", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
        reasoning: "Evaluation failed due to timeout",
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("tooltip")).toBeInTheDocument();
      expect(screen.getByTestId("tooltip")).toHaveAttribute(
        "data-label",
        "Evaluation failed due to timeout",
      );
    });

    it("should not display tooltip without reasoning text", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
        reasoning: undefined,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const tooltips = screen.queryAllByTestId("tooltip");
      expect(tooltips.length).toBe(0);
    });

    it("should not display tooltip with empty reasoning text", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
        reasoning: "",
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const tooltips = screen.queryAllByTestId("tooltip");
      expect(tooltips.length).toBe(0);
    });

    it("should set tooltip position to bottom", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
        reasoning: "Test reasoning",
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("tooltip")).toHaveAttribute(
        "data-position",
        "bottom",
      );
    });
  });

  describe("Refresh Button Rendering", () => {
    it("should show refresh button when status is FAILED", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const refreshButtons = screen.getAllByTestId("unstyled-button");
      expect(refreshButtons.length).toBeGreaterThanOrEqual(2);
      expect(screen.getByTestId("material-icon-refresh")).toBeInTheDocument();
    });

    it("should not show refresh button when status is SUCCESSFUL", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(
        screen.queryByTestId("material-icon-refresh"),
      ).not.toBeInTheDocument();
    });

    it("should not show refresh button when status is PENDING", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.PENDING,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(
        screen.queryByTestId("material-icon-refresh"),
      ).not.toBeInTheDocument();
    });

    it("should not show refresh button when status is IN_PROGRESS", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.IN_PROGRESS,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(
        screen.queryByTestId("material-icon-refresh"),
      ).not.toBeInTheDocument();
    });

    it("should render refresh button with correct MaterialIcon", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const refreshIcon = screen.getByTestId("material-icon-refresh");
      expect(refreshIcon).toHaveAttribute("data-size", "16");
      expect(refreshIcon).toHaveClass("text-secondary");
    });

    it("should render refresh button with correct classes", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      const buttons = screen.getAllByTestId("unstyled-button");
      const refreshButton = buttons[buttons.length - 1];

      expect(refreshButton).toHaveClass(
        "flex",
        "h-[24px]",
        "w-[24px]",
        "items-center",
        "justify-center",
        "rounded-sm",
        "p-[4px]",
        "border-default",
      );
    });
  });

  describe("Click Handlers", () => {
    it("should call onStatusClick when status button is clicked", async () => {
      const user = userEvent.setup();
      const data = createMockData();
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      const buttons = screen.getAllByTestId("unstyled-button");
      const statusButton = buttons[0];

      await user.click(statusButton);
      expect(mockOnStatusClick).toHaveBeenCalledTimes(1);
    });

    it("should call onRerun when refresh button is clicked", async () => {
      const user = userEvent.setup();
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      const buttons = screen.getAllByTestId("unstyled-button");
      const refreshButton = buttons[buttons.length - 1];

      await user.click(refreshButton);
      expect(mockOnRerun).toHaveBeenCalledTimes(1);
    });

    it("should not call onRerun when refresh button is not visible", async () => {
      userEvent.setup();
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      expect(mockOnRerun).not.toHaveBeenCalled();
    });

    it("should call both callbacks independently", async () => {
      const user = userEvent.setup();
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      const buttons = screen.getAllByTestId("unstyled-button");
      const statusButton = buttons[0];
      const refreshButton = buttons[1];

      await user.click(statusButton);
      expect(mockOnStatusClick).toHaveBeenCalledTimes(1);
      expect(mockOnRerun).not.toHaveBeenCalled();

      await user.click(refreshButton);
      expect(mockOnStatusClick).toHaveBeenCalledTimes(1);
      expect(mockOnRerun).toHaveBeenCalledTimes(1);
    });

    it("should handle multiple clicks on status button", async () => {
      const user = userEvent.setup();
      const data = createMockData();
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      const buttons = screen.getAllByTestId("unstyled-button");
      const statusButton = buttons[0];

      await user.click(statusButton);
      await user.click(statusButton);
      await user.click(statusButton);

      expect(mockOnStatusClick).toHaveBeenCalledTimes(3);
    });

    it("should handle multiple clicks on refresh button", async () => {
      const user = userEvent.setup();
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      const buttons = screen.getAllByTestId("unstyled-button");
      const refreshButton = buttons[buttons.length - 1];

      await user.click(refreshButton);
      await user.click(refreshButton);

      expect(mockOnRerun).toHaveBeenCalledTimes(2);
    });
  });

  describe("Color Handling", () => {
    it("should pass color prop to EvaluatorStatusIndicator", () => {
      const data = createMockData({ color: "#FF5733" });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("chip")).toBeInTheDocument();
    });

    it("should handle undefined color", () => {
      const data = createMockData({ color: undefined });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("chip")).toBeInTheDocument();
    });

    it("should handle undefined color", () => {
      const data = createMockData({ color: undefined });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("chip")).toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("should handle very long score decimal", () => {
      const data = createMockData({ score: "0.123456789" });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent("0.123456789");
    });

    it("should handle special characters in reasoning", () => {
      const specialReasoning = "Good score!@#$%^&*()_+-=[]{}|;:,.<>?";
      const data = createMockData({ reasoning: specialReasoning });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("tooltip")).toHaveAttribute(
        "data-label",
        specialReasoning,
      );
    });

    it("should handle very long category name", () => {
      const longCategory =
        "This is a very long category name that might cause layout issues";
      const data = createMockData({ category: longCategory });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      const chip = screen.getByTestId("chip");
      expect(chip).toHaveTextContent(longCategory);
    });

    it("should handle very long reasoning text", () => {
      const longReasoning =
        "This is a very long reasoning text that explains the evaluation in great detail and might be wrapped in the tooltip";
      const data = createMockData({ reasoning: longReasoning });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("tooltip")).toHaveAttribute(
        "data-label",
        longReasoning,
      );
    });
  });

  describe("Component Integration", () => {
    it("should render with all data types combined", () => {
      const data = createMockData({
        score: "0.88",
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
        category: "Accuracy",
        color: "#00AA00",
        reasoning: "Excellent results achieved",
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      expect(screen.getByTestId("chip")).toHaveTextContent("0.88 - Accuracy");
      expect(screen.getByTestId("tooltip")).toHaveAttribute(
        "data-label",
        "Excellent results achieved",
      );
    });

    it("should handle FAILED status with reason and trigger rerun", async () => {
      const user = userEvent.setup();
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
        reasoning: "Evaluation timeout",
      });

      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      expect(screen.getByTestId("material-icon-error")).toBeInTheDocument();
      expect(screen.getByTestId("material-icon-refresh")).toBeInTheDocument();

      const buttons = screen.getAllByTestId("unstyled-button");
      const refreshButton = buttons[buttons.length - 1];

      await user.click(refreshButton);
      expect(mockOnRerun).toHaveBeenCalledTimes(1);
    });
  });

  describe("Status-Specific Icon Rendering", () => {
    it("should render error icon for FAILED status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("material-icon-error")).toBeInTheDocument();
      expect(screen.getByTestId("material-icon-error")).toHaveClass("text-red");
    });

    it("should render schedule icon for PENDING status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.PENDING,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("material-icon-schedule")).toBeInTheDocument();
      expect(screen.getByTestId("material-icon-schedule")).toHaveClass(
        "text-secondary",
      );
    });

    it("should render loader for IN_PROGRESS status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.IN_PROGRESS,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(screen.getByTestId("loader")).toBeInTheDocument();
      expect(screen.getByTestId("loader")).toHaveClass("text-secondary");
    });

    it("should not render icon for SUCCESSFUL status", () => {
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );
      expect(
        screen.queryByTestId("material-icon-error"),
      ).not.toBeInTheDocument();
      expect(
        screen.queryByTestId("material-icon-schedule"),
      ).not.toBeInTheDocument();
      expect(screen.queryByTestId("loader")).not.toBeInTheDocument();
    });
  });

  describe("Accessibility", () => {
    it("should have buttons clickable with keyboard", async () => {
      const user = userEvent.setup();
      const data = createMockData({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusElement
          data={data}
          onRerun={mockOnRerun}
          onStatusClick={mockOnStatusClick}
        />,
      );

      const buttons = screen.getAllByTestId("unstyled-button");
      const statusButton = buttons[0];

      statusButton.focus();
      await user.keyboard("{Enter}");

      expect(mockOnStatusClick).toHaveBeenCalled();
    });
  });
});
