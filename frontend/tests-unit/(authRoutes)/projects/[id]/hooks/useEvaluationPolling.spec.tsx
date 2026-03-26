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

import { useEvaluationPolling } from "@/app/(authRoutes)/projects/[id]/hooks/useEvaluationPolling";
import * as clientQueries from "@/queries/clientQueries";
import { EvaluationScoreStatus, ProjectType } from "@/types";
import { notifications } from "@mantine/notifications";
import { renderHook } from "@testing-library/react";
import { act } from "react-dom/test-utils";

jest.mock("@/queries/clientQueries");
jest.mock("@mantine/notifications");

describe("useEvaluationPolling", () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.clearAllMocks();
  });
  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  const mockOnEvaluationUpdate = jest.fn();
  const mockOnInferenceUpdate = jest.fn();
  const mockFindChatId = jest.fn();

  it("pollSxsEvaluation path when projectType = SIDE_BY_SIDE and projectId is provided", async () => {
    const pairId = "pair-1";
    const projectId = "proj-123";
    const resp = [
      {
        point_evaluations: {
          a: { evaluationStatus: EvaluationScoreStatus.SUCCESSFUL },
        },
        chat_turn_a: { chat_turn_id: "tA", sequence: 1 },
        chat_turn_b: { chat_turn_id: "tB", sequence: 2 },
        input: "input1",
      },
    ];
    (clientQueries.getChatSxsWorkbookHistory as jest.Mock).mockResolvedValue(
      resp,
    );
    mockFindChatId.mockReturnValue(pairId);

    const { result } = renderHook(() =>
      useEvaluationPolling(
        mockOnEvaluationUpdate,
        mockFindChatId,
        mockOnInferenceUpdate,
        undefined,
        ProjectType.SIDE_BY_SIDE,
        projectId,
      ),
    );

    act(() => {
      result.current.startPolling([pairId], true, { inputs: ["input1"] });
    });

    expect(result.current.isPolling).toBe(true);

    await act(async () => {
      jest.runOnlyPendingTimers();
      await Promise.resolve();
    });

    expect(clientQueries.getChatSxsWorkbookHistory).toHaveBeenCalledWith(
      pairId,
      projectId,
    );
    expect(mockOnEvaluationUpdate).toHaveBeenCalledWith(
      pairId,
      resp[0].point_evaluations,
    );
    await act(async () => {
      jest.runOnlyPendingTimers();
    });
    expect(result.current.isPolling).toBe(true);
  });

  it("does not pollSxsEvaluation if projectId is missing", async () => {
    (clientQueries.getChatSxsWorkbookHistory as jest.Mock).mockResolvedValue(
      [],
    );
    mockFindChatId.mockReturnValue("pair-2");

    const { result } = renderHook(() =>
      useEvaluationPolling(
        mockOnEvaluationUpdate,
        mockFindChatId,
        mockOnInferenceUpdate,
        undefined,
        ProjectType.SIDE_BY_SIDE,
        undefined, // missing projectId
      ),
    );

    act(() => {
      result.current.startPolling(["pair-2"]);
    });

    await act(async () => {
      jest.runOnlyPendingTimers();
      await Promise.resolve();
    });

    // If projectId is undefined, getChatSxsWorkbookHistory should not be called
    expect(clientQueries.getChatSxsWorkbookHistory).not.toHaveBeenCalled();
  });

  it("stops polling after pollingCount > max (20)", async () => {
    (clientQueries.getChatHistory as jest.Mock).mockResolvedValue({
      chat_turns: [{ chat_turn_id: "turnX", llm_evaluations: {} }],
    });
    mockFindChatId.mockReturnValue("chatX");
    const { result } = renderHook(() =>
      useEvaluationPolling(mockOnEvaluationUpdate, mockFindChatId),
    );
    act(() => {
      result.current.startPolling(["turnX"]);
    });

    // fast-forward many timers to exceed 20 polls
    for (let i = 0; i < 25; i++) {
      await act(async () => {
        jest.runOnlyPendingTimers();
        await Promise.resolve();
      });
    }

    expect(result.current.isPolling).toBe(false);
  });

  it("calls onInferenceStatusUpdate when inference_status is present and then removes it", async () => {
    const chatResp = {
      chat_turns: [
        {
          chat_turn_id: "turnI",
          llm_evaluations: { e: { evaluationStatus: 1 } },
          inference_status: 100,
          inference_reason: "OK",
        },
      ],
    };
    (clientQueries.getChatHistory as jest.Mock).mockResolvedValue(chatResp);
    mockFindChatId.mockReturnValue("chatI");

    const { result } = renderHook(() =>
      useEvaluationPolling(
        mockOnEvaluationUpdate,
        mockFindChatId,
        mockOnInferenceUpdate,
      ),
    );

    act(() => {
      result.current.startPolling(["turnI"], true);
    });

    await act(async () => {
      jest.runOnlyPendingTimers();
      await Promise.resolve();
    });

    expect(mockOnInferenceUpdate).toHaveBeenCalledWith("turnI", 100, "OK");
  });

  it("handles empty evaluations object and retains polling if recently started", async () => {
    const resp = { chat_turns: [{ chat_turn_id: "tR", llm_evaluations: {} }] };
    (clientQueries.getChatHistory as jest.Mock).mockResolvedValue(resp);
    mockFindChatId.mockReturnValue("chatR");

    const { result } = renderHook(() =>
      useEvaluationPolling(mockOnEvaluationUpdate, mockFindChatId),
    );

    act(() => {
      result.current.startPolling(["tR"]);
    });

    await act(async () => {
      jest.runOnlyPendingTimers();
      await Promise.resolve();
    });

    // Because recently started (within threshold), it should still poll
    expect(result.current.isPolling).toBe(true);
  });

  it("error in pollSxsEvaluation handles API error notification", async () => {
    (clientQueries.getChatSxsWorkbookHistory as jest.Mock).mockRejectedValue(
      new Error("fail"),
    );
    mockFindChatId.mockReturnValue("pairErr");

    const { result } = renderHook(() =>
      useEvaluationPolling(
        mockOnEvaluationUpdate,
        mockFindChatId,
        undefined,
        undefined,
        ProjectType.SIDE_BY_SIDE,
        "projectZ",
      ),
    );
    act(() => {
      result.current.startPolling(["pairErr"]);
    });

    await act(async () => {
      jest.runOnlyPendingTimers();
      await Promise.resolve();
    });

    expect(notifications.show).toHaveBeenCalled();
  });
});
