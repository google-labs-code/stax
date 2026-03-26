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

import ScoreEvaluationModal from "@/components/Evaluation/ScoreEvaluationModal";
import { useChatContext } from "@/hooks/useChatContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import * as clientQueries from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { EvaluationScoreStatus, ProjectType } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: jest.fn(),
}));

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: () => ({
    projectType: ProjectType.POINTWISE,
  }),
}));

jest.mock("@/queries/clientQueries", () => ({
  evaluationProjectQuery: jest.fn(),
  evaluationWholeProjectQuery: jest.fn(),
  evaluationChatTurnsQuery: jest.fn(),
  evaluationChatTurnQuery: jest.fn(),
  getAllEvaluatorsQuery: jest.fn(),
}));

jest.mock("@/utils/logGAevent", () => jest.fn());

const mockStartEvalPooling = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();

  (useChatContext as jest.Mock).mockReturnValue({
    selectedChatTurnIds: ["turn-1"],
    startEvaluationResultsPooling: mockStartEvalPooling,
  });

  (useProjectsContext as jest.Mock).mockReturnValue({
    defaultProjectId: "proj-123",
  });

  (clientQueries.getAllEvaluatorsQuery as jest.Mock).mockResolvedValue({
    llm: [
      {
        id: "eval-1",
        name: "System Eval",
        type: "SYSTEM",
        title: "System Eval",
        evaluation_type: ProjectType.POINTWISE,
      },
      {
        id: "eval-2",
        name: "Custom Eval",
        type: "USER",
        title: "Custom Eval",
        evaluation_type: ProjectType.POINTWISE,
      },
    ],
    heuristic: [],
  });

  (clientQueries.evaluationChatTurnQuery as jest.Mock).mockResolvedValue({
    llm_evaluations: {
      "Eval One": {
        score: 0.95,
        evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
      },
    },
  });

  (clientQueries.evaluationProjectQuery as jest.Mock).mockResolvedValue({
    status: "queued",
  });

  (clientQueries.evaluationWholeProjectQuery as jest.Mock).mockResolvedValue({
    status: "queued",
  });

  (clientQueries.evaluationChatTurnsQuery as jest.Mock).mockResolvedValue({
    status: "queued",
  });
});

const defaultProps = {
  isOpened: true,
  onClose: jest.fn(),
  onRequestComplete: jest.fn(),
  onEvaluationStarted: jest.fn(),
  setRowSelection: jest.fn(),
  projectId: "proj-123",
};

describe("ScoreEvaluationModal", () => {
  it("renders modal with title", async () => {
    testRender(<ScoreEvaluationModal {...defaultProps} />);

    expect(await screen.findByText("Select evaluator(s)")).toBeInTheDocument();
  });

  it("loads system and custom evaluators", async () => {
    testRender(<ScoreEvaluationModal {...defaultProps} />);
    await waitFor(() =>
      expect(clientQueries.getAllEvaluatorsQuery).toHaveBeenCalled(),
    );
  });

  it("disables Run Evaluation button when no evaluator is selected", async () => {
    testRender(<ScoreEvaluationModal {...defaultProps} />);
    const btn = await screen.findByRole("button", { name: /run evaluation/i });
    expect(btn).toBeDisabled();
  });

  it("runs single chat turn evaluation", async () => {
    testRender(<ScoreEvaluationModal {...defaultProps} />);

    const badge = await screen.findAllByText("System Eval");
    fireEvent.click(badge[0]);

    const runBtn = screen.getByRole("button", {
      name: /run evaluation/i,
    });

    fireEvent.click(runBtn);

    await waitFor(() =>
      expect(clientQueries.evaluationChatTurnQuery).toHaveBeenCalledWith(
        {
          chat_turn_id: "turn-1",
          evaluator_id: "eval-1",
        },
        "proj-123",
      ),
    );

    expect(defaultProps.onRequestComplete).toHaveBeenCalled();
  });

  it("runs evaluation for all rows if selectAllRowsInProject is true", async () => {
    testRender(
      <ScoreEvaluationModal {...defaultProps} selectAllRowsInProject={true} />,
    );

    const badge = await screen.findAllByText("System Eval");
    fireEvent.click(badge[0]); // Select

    const runBtn = screen.getByRole("button", { name: /run evaluation/i });
    fireEvent.click(runBtn);

    await waitFor(() =>
      expect(clientQueries.evaluationWholeProjectQuery).toHaveBeenCalled(),
    );
  });

  it("runs evaluation for multiple selected chat turns", async () => {
    (useChatContext as jest.Mock).mockReturnValue({
      selectedChatTurnIds: ["turn-1", "turn-2"],
      startEvaluationResultsPooling: mockStartEvalPooling,
    });

    testRender(<ScoreEvaluationModal {...defaultProps} />);

    const badge = await screen.findAllByText("System Eval");
    fireEvent.click(badge[0]);

    const runBtn = screen.getByRole("button", { name: /run evaluation/i });
    fireEvent.click(runBtn);

    await waitFor(() =>
      expect(clientQueries.evaluationChatTurnsQuery).toHaveBeenCalledWith(
        {
          evaluator_ids: ["eval-1"],
          chat_turn_ids: ["turn-1", "turn-2"],
        },
        "proj-123",
      ),
    );
  });

  it("runs full project evaluation when no chat turns are selected", async () => {
    (useChatContext as jest.Mock).mockReturnValue({
      selectedChatTurnIds: [],
      startEvaluationResultsPooling: mockStartEvalPooling,
    });

    testRender(<ScoreEvaluationModal {...defaultProps} />);

    const badge = await screen.findAllByText("System Eval");
    fireEvent.click(badge[0]);

    const runBtn = screen.getByRole("button", { name: /run evaluation/i });
    fireEvent.click(runBtn);

    await waitFor(() =>
      expect(clientQueries.evaluationProjectQuery).toHaveBeenCalled(),
    );
  });
});
