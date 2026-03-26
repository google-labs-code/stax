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

import OutputCardEvaluator from "@/app/(authRoutes)/projects/[id]/playground/components/Output/OutputCardEvaluator";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { useHumanEvalsContext } from "@/hooks/useHumanEvalsContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { MantineProvider } from "@mantine/core";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");
jest.mock("@/hooks/usePlaygroundContext");
jest.mock("@/hooks/useHumanEvalsContext");
jest.mock("@/queries/clientQueries", () => ({
  createFeedbackForChatTurnQuery: jest.fn(() =>
    Promise.resolve({ score: 1, notes: "test note" }),
  ),
  humanEvalSxSQuery: jest.fn(() => Promise.resolve({})),
}));

const queryClient = new QueryClient();

const renderComponent = () =>
  render(
    <MantineProvider>
      <QueryClientProvider client={queryClient}>
        <OutputCardEvaluator />
      </QueryClientProvider>
    </MantineProvider>,
  );

describe("OutputCardEvaluator", () => {
  const mockSetHumanEvaluator = jest.fn();
  const mockSetHumanEvalReady = jest.fn();
  const mockOpenPromptCleaningModalOpen = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (useHumanEvalsContext as jest.Mock).mockReturnValue({
      feedbackEvalId: "mock-feedback-id",
    });

    (useProjectContext as jest.Mock).mockReturnValue({
      projectState: {
        project: {
          type: "POINTWISE",
          project_id: "mock-project-id",
        },
      },
    });

    (usePlaygroundContext as jest.Mock).mockReturnValue({
      outputs: [{ text: "Mock output A" }],
      isLoading: [false],
      selectedChatTurnIds: ["turn-1"],
      lastChatTurnId: "turn-1",
      pairId: "pair-1",
      feedbackEvalId: "feedback-id",
      setHumanEvaluator: mockSetHumanEvaluator,
      setHumanEvalReady: mockSetHumanEvalReady,
      openPromptCleaningModalOpen: mockOpenPromptCleaningModalOpen,
      humanEvaluator: {
        value: null,
        notes: "",
      },
    });
  });

  it("renders thumb buttons for pointwise evaluation", () => {
    renderComponent();

    expect(screen.getByText("PASS")).toBeInTheDocument();
    expect(screen.getByText("FAIL")).toBeInTheDocument();
  });

  it("calls pointwise mutation on click", async () => {
    renderComponent();

    const button = screen.getByText("PASS");
    fireEvent.click(button);

    await waitFor(() => {
      expect(mockSetHumanEvaluator).toHaveBeenCalledWith({
        value: 1,
        notes: "test note",
      });
    });

    expect(mockOpenPromptCleaningModalOpen).toHaveBeenCalled();
    expect(mockSetHumanEvalReady).toHaveBeenCalled();
  });

  it("renders SxS buttons when project is side-by-side", () => {
    (useProjectContext as jest.Mock).mockReturnValueOnce({
      projectState: {
        project: {
          type: "SXS",
          project_id: "mock-project-id",
        },
      },
      isSideBySide: true,
    });

    (usePlaygroundContext as jest.Mock).mockReturnValueOnce({
      outputs: [{ text: "A" }, { text: "B" }],
      isLoading: [false, false],
      selectedChatTurnIds: ["turn-a", "turn-b"],
      pairId: "pair-1",
      setHumanEvaluator: mockSetHumanEvaluator,
      setHumanEvalReady: mockSetHumanEvalReady,
      openPromptCleaningModalOpen: mockOpenPromptCleaningModalOpen,
      humanEvaluator: {
        value: null,
        notes: "",
      },
    });

    renderComponent();

    expect(screen.getByText("LEFT IS BETTER")).toBeInTheDocument();
    expect(screen.getByText("IT'S A TIE")).toBeInTheDocument();
    expect(screen.getByText("BOTH ARE BAD")).toBeInTheDocument();
    expect(screen.getByText("RIGHT IS BETTER")).toBeInTheDocument();
  });

  it("calls SxS mutation on selection", async () => {
    (useProjectContext as jest.Mock).mockReturnValueOnce({
      projectState: {
        project: {
          type: "SXS",
          project_id: "mock-project-id",
        },
      },
      isSideBySide: true,
    });

    (usePlaygroundContext as jest.Mock).mockReturnValueOnce({
      outputs: [{ text: "A" }, { text: "B" }],
      isLoading: [false, false],
      selectedChatTurnIds: ["turn-a", "turn-b"],
      pairId: "pair-1",
      setHumanEvaluator: mockSetHumanEvaluator,
      setHumanEvalReady: mockSetHumanEvalReady,
      openPromptCleaningModalOpen: mockOpenPromptCleaningModalOpen,
      humanEvaluator: {
        value: null,
        notes: "",
      },
    });

    renderComponent();

    const button = screen.getByText("LEFT IS BETTER");
    fireEvent.click(button);

    await waitFor(() => {
      expect(mockSetHumanEvaluator).toHaveBeenCalledWith({
        value: "A_IS_BETTER",
        notes: "",
      });
    });

    expect(mockSetHumanEvalReady).toHaveBeenCalled();
  });
});
