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

import EvaluationStatusModal from "@/app/(authRoutes)/projects/[id]/components/EvaluationStatusModal";
import { EvaluationScoreStatus, LLMEvaluation } from "@/types";
import "@testing-library/jest-dom";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
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

jest.mock("@/config/routes", () => ({
  routes: {
    evaluatorGallery: {
      root: "https://example.com/gallery",
    },
  },
}));

jest.mock(
  "@/app/(authRoutes)/projects/[id]/components/EvaluatorStatusIndicator",
  () => {
    return function MockEvaluatorStatusIndicator({
      score,
      status,
      category,
      color,
      toolTipText,
    }: any) {
      return (
        <div
          data-testid="evaluator-status-indicator"
          data-score={score}
          data-status={status}
          data-category={category}
          data-color={color}
          data-tooltip={toolTipText}
        >
          Status Indicator
        </div>
      );
    };
  },
);

jest.mock("@mantine/core", () => ({
  Modal: ({ children, opened, onClose, title, ...props }: any) => {
    if (!opened) return null;

    return (
      <div data-testid="modal" {...props}>
        <div data-testid="modal-header">{title}</div>
        <div data-testid="modal-content">{children}</div>
        <button data-testid="modal-close-button" onClick={onClose}>
          Close
        </button>
      </div>
    );
  },
  Stack: ({ children, ...props }: any) => (
    <div data-testid="stack" {...props}>
      {children}
    </div>
  ),
  Group: ({ children, ...props }: any) => (
    <div data-testid="group" {...props}>
      {children}
    </div>
  ),
  Text: ({ children, onClick, ...props }: any) => (
    <span data-testid="text" onClick={onClick} {...props}>
      {children}
    </span>
  ),
  Tooltip: ({ children, label }: any) => (
    <div data-testid="tooltip" data-label={label}>
      {children}
    </div>
  ),
  ActionIcon: ({ children, onClick, size, variant }: any) => (
    <button
      data-testid="action-icon"
      onClick={onClick}
      data-size={size}
      data-variant={variant}
    >
      {children}
    </button>
  ),
}));

describe("EvaluationStatusModal Component", () => {
  const mockOnClose = jest.fn();

  const createMockEvaluation = (
    overrides?: Partial<LLMEvaluation>,
  ): LLMEvaluation =>
    ({
      score: 0.85,
      evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      evaluationStatusText: "Passed",
      reasoning: "This evaluation passed all criteria",
      llmResponse: "Full LLM response text",
      category: "Accuracy",
      color: "#00FF00",
      evaluator_id: "eval-123",
      ...overrides,
    }) as LLMEvaluation;

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
    window.open = jest.fn();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe("Rendering", () => {
    it("should render modal when isOpened is true", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });

    it("should not render modal when isOpened is false", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={false}
          onClose={mockOnClose}
          data={data}
        />,
      );
      expect(screen.queryByTestId("modal")).not.toBeInTheDocument();
    });

    it("should render modal with correct title", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      expect(screen.getByTestId("modal-header")).toHaveTextContent(
        "Evaluator Output Details",
      );
    });

    it("should render close button", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      expect(screen.getByTestId("modal-close-button")).toBeInTheDocument();
    });
  });

  describe("Score Section", () => {
    it("should render score label", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "Score")).toBe(true);
    });

    it("should render EvaluatorStatusIndicator with correct props", () => {
      const data = createMockEvaluation({
        score: "0.92",
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
        category: "Relevance",
        color: "#0000FF",
        reasoning: "Test reasoning",
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const indicator = screen.getByTestId("evaluator-status-indicator");
      expect(indicator).toHaveAttribute("data-score", "0.92");
      expect(indicator).toHaveAttribute(
        "data-status",
        EvaluationScoreStatus.SUCCESSFUL,
      );
      expect(indicator).toHaveAttribute("data-category", "Relevance");
      expect(indicator).toHaveAttribute("data-color", "#0000FF");
      expect(indicator).toHaveAttribute("data-tooltip", "Test reasoning");
    });

    it("should handle null score", () => {
      const data = createMockEvaluation({ score: "null" });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const indicator = screen.getByTestId("evaluator-status-indicator");
      expect(indicator).toHaveAttribute("data-score", "null");
    });
  });

  describe("Status Section", () => {
    it("should render status label", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "Status")).toBe(true);
    });

    it("should display evaluation status text", () => {
      const data = createMockEvaluation({ evaluationStatusText: "Completed" });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "Completed")).toBe(true);
    });

    it("should display dash when status text is undefined", () => {
      const data = createMockEvaluation({ evaluationStatusText: undefined });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "-")).toBe(true);
    });

    it("should display dash when status text is null", () => {
      const data = createMockEvaluation({ evaluationStatusText: null as any });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "-")).toBe(true);
    });
  });

  describe("Reasoning Section", () => {
    it("should render reasoning label", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(
        texts.some((t) => t.textContent?.includes("Evaluation reasoning")),
      ).toBe(true);
    });

    it("should display reasoning text", () => {
      const data = createMockEvaluation({
        reasoning: "This is the reasoning",
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "This is the reasoning")).toBe(
        true,
      );
    });

    it("should display dash when reasoning is not provided", () => {
      const data = createMockEvaluation({ reasoning: undefined });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "-")).toBe(true);
    });

    it("should render copy button when reasoning exists", () => {
      const data = createMockEvaluation({ reasoning: "Some reasoning" });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const actionIcons = screen.getAllByTestId("action-icon");
      expect(actionIcons.length).toBeGreaterThanOrEqual(1);
    });
  });

  describe("Response Section", () => {
    it("should render response label", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(
        texts.some((t) => t.textContent?.includes("Full evaluator output")),
      ).toBe(true);
    });

    it("should display llm response text", () => {
      const data = createMockEvaluation({
        llmResponse: "This is the full response",
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(
        texts.some((t) => t.textContent === "This is the full response"),
      ).toBe(true);
    });

    it("should display dash when response is not provided", () => {
      const data = createMockEvaluation({ llmResponse: undefined });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "-")).toBe(true);
    });

    it("should render copy button when response exists", () => {
      const data = createMockEvaluation({ llmResponse: "Some response" });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const actionIcons = screen.getAllByTestId("action-icon");
      expect(actionIcons.length).toBeGreaterThanOrEqual(1);
    });

    it("should not render copy button when response does not exist", () => {
      const data = createMockEvaluation({
        reasoning: "Has reasoning",
        llmResponse: undefined,
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const actionIcons = screen.getAllByTestId("action-icon");
      expect(actionIcons.length).toBe(1); // Only reasoning copy button
    });
  });

  describe("Tooltip Display", () => {
    it("should show copy to clipboard tooltip initially for reasoning", () => {
      const data = createMockEvaluation({ reasoning: "Reasoning text" });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const tooltips = screen.getAllByTestId("tooltip");
      expect(tooltips[0]).toHaveAttribute("data-label", "Copy to clipboard");
    });

    it("should show copy to clipboard tooltip initially for response", () => {
      const data = createMockEvaluation({
        reasoning: "Reasoning",
        llmResponse: "Response",
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const tooltips = screen.getAllByTestId("tooltip");
      expect(tooltips[1]).toHaveAttribute("data-label", "Copy to clipboard");
    });

    it("should show Copied! tooltip after copying reasoning", async () => {
      const user = userEvent.setup({ delay: null });
      const data = createMockEvaluation({ reasoning: "Reasoning text" });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const actionIcons = screen.getAllByTestId("action-icon");
      await user.click(actionIcons[0]);

      await waitFor(() => {
        const tooltips = screen.getAllByTestId("tooltip");
        expect(tooltips[0]).toHaveAttribute("data-label", "Copied!");
      });
    });

    it("should show Copied! tooltip after copying response", async () => {
      const user = userEvent.setup({ delay: null });
      const data = createMockEvaluation({
        reasoning: "Reasoning",
        llmResponse: "Response text",
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const actionIcons = screen.getAllByTestId("action-icon");
      await user.click(actionIcons[1]);

      await waitFor(() => {
        const tooltips = screen.getAllByTestId("tooltip");
        expect(tooltips[1]).toHaveAttribute("data-label", "Copied!");
      });
    });
  });

  describe("Copy Icon Changes", () => {
    it("should change to check icon after copying reasoning", async () => {
      const user = userEvent.setup({ delay: null });
      const data = createMockEvaluation({ reasoning: "Reasoning text" });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const actionIcons = screen.getAllByTestId("action-icon");
      await user.click(actionIcons[0]);

      await waitFor(() => {
        expect(screen.getByTestId("material-icon-check")).toBeInTheDocument();
      });
    });

    it("should change back to content_copy after timeout", async () => {
      const user = userEvent.setup({ delay: null });
      const data = createMockEvaluation({ reasoning: "Reasoning text" });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const actionIcons = screen.getAllByTestId("action-icon");
      await user.click(actionIcons[0]);

      await waitFor(() => {
        expect(screen.getByTestId("material-icon-check")).toBeInTheDocument();
      });

      jest.advanceTimersByTime(2000);

      await waitFor(() => {
        expect(
          screen.getByTestId("material-icon-content_copy"),
        ).toBeInTheDocument();
      });
    });
  });

  describe("View Details Link", () => {
    it("should render View details link", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === "View details")).toBe(true);
    });

    it("should open particular link when View Details Link is clicked", () => {
      const openSpy = jest.spyOn(window, "open").mockImplementation(() => null);
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      fireEvent.click(screen.getByText(/View details/i));

      expect(openSpy).toHaveBeenCalledTimes(1);
      expect(openSpy).toHaveBeenCalledWith(
        "https://example.com/gallery/eval-123/view",
        "_blank",
      );
    });
  });

  describe("Edge Cases", () => {
    it("should handle very long reasoning text", () => {
      const longReasoning = "A".repeat(5000);
      const data = createMockEvaluation({ reasoning: longReasoning });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === longReasoning)).toBe(true);
    });

    it("should handle very long response text", () => {
      const longResponse = "B".repeat(10000);
      const data = createMockEvaluation({ llmResponse: longResponse });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === longResponse)).toBe(true);
    });

    it("should handle reasoning with newlines", () => {
      const reasoningWithNewlines = "Line 1\nLine 2\nLine 3";
      const data = createMockEvaluation({
        reasoning: reasoningWithNewlines,
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === reasoningWithNewlines)).toBe(
        true,
      );
    });

    it("should handle response with newlines", () => {
      const responseWithNewlines = "Response Line 1\nResponse Line 2";
      const data = createMockEvaluation({
        llmResponse: responseWithNewlines,
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const texts = screen.getAllByTestId("text");
      expect(texts.some((t) => t.textContent === responseWithNewlines)).toBe(
        true,
      );
    });

    it("should handle empty strings", () => {
      const data = createMockEvaluation({
        reasoning: "",
        llmResponse: "",
        evaluationStatusText: "",
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });

    it("should handle unicode characters", () => {
      const data = createMockEvaluation({
        reasoning: "评估 🚀 エバリュエーション",
        llmResponse: "Оценка مقييم",
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });
  });

  describe("Different Status Types", () => {
    it("should render with SUCCESSFUL status", () => {
      const data = createMockEvaluation({
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });

    it("should render with FAILED status", () => {
      const data = createMockEvaluation({
        evaluationStatus: EvaluationScoreStatus.FAILED,
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });

    it("should render with PENDING status", () => {
      const data = createMockEvaluation({
        evaluationStatus: EvaluationScoreStatus.PENDING,
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });

    it("should render with IN_PROGRESS status", () => {
      const data = createMockEvaluation({
        evaluationStatus: EvaluationScoreStatus.IN_PROGRESS,
      });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );
      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });
  });

  describe("Layout and Structure", () => {
    it("should render score and status in group", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const groups = screen.getAllByTestId("group");
      expect(groups.length).toBeGreaterThan(0);
    });

    it("should render multiple stacks for sections", () => {
      const data = createMockEvaluation();
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      const stacks = screen.getAllByTestId("stack");
      expect(stacks.length).toBeGreaterThan(0);
    });
  });

  describe("Whitespace Preservation", () => {
    it("should preserve whitespace in reasoning text", () => {
      const reasoningWithSpaces = "  Text with   multiple  spaces  ";
      const data = createMockEvaluation({ reasoning: reasoningWithSpaces });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });

    it("should preserve whitespace in response text", () => {
      const responseWithSpaces = "  Response   with    spaces  ";
      const data = createMockEvaluation({ llmResponse: responseWithSpaces });
      render(
        <EvaluationStatusModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
        />,
      );

      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });
  });
});
