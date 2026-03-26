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

import { getErrorNotificationConfig } from "@/config/notifications";
import {
  getChatHistory,
  getChatSxsWorkbookHistory,
} from "@/queries/clientQueries";
import { EvaluationScoreStatus, LLMEvaluations, ProjectType } from "@/types";
import { notifications } from "@mantine/notifications";
import { useEffect, useRef, useState } from "react";

import { InferenceStatus } from "../types";

export function useEvaluationPolling(
  onEvaluationUpdate: (turnId: string, evaluations: LLMEvaluations) => void,
  findChatId: (turnId: string) => string | null,
  onInferenceStatusUpdate?: (
    turnId: string,
    inferenceStatus: number | null,
    inferenceReason?: string,
  ) => void,
  isSubrow?: (turnId: string) => boolean,
  projectType?: ProjectType,
  projectId?: string,
) {
  const [isPolling, setIsPolling] = useState<boolean>(false);
  const pendingRowsRef = useRef<Set<string>>(new Set());
  const pollingTimerRef = useRef<NodeJS.Timeout | null>(null);
  const pollingCountRef = useRef<number>(0);
  const queriedChatsRef = useRef<Set<string>>(new Set());
  const inferenceTrackingRef = useRef<Set<string>>(new Set());

  const selectedInputsRef = useRef<Set<string>>(new Set());
  const selectedSequencesRef = useRef<Set<number>>(new Set());
  const selectedTurnIdsRef = useRef<Set<string>>(new Set());

  const recentlyStartedEvaluationsRef = useRef<Map<string, number>>(new Map());

  const projectTypeRef = useRef(projectType);
  const projectIdRef = useRef(projectId);

  useEffect(() => {
    projectTypeRef.current = projectType;
    projectIdRef.current = projectId;
  }, [projectType, projectId]);

  const startPolling = (
    turnIds: string[],
    trackInference: boolean = false,
    additionalInfo?: { inputs?: string[]; sequences?: number[] },
  ): void => {
    stopPolling();

    selectedTurnIdsRef.current = new Set(turnIds);

    selectedInputsRef.current.clear();
    selectedSequencesRef.current.clear();

    if (additionalInfo?.inputs) {
      additionalInfo.inputs.forEach((input) =>
        selectedInputsRef.current.add(input),
      );
    }
    if (additionalInfo?.sequences) {
      additionalInfo.sequences.forEach((seq) =>
        selectedSequencesRef.current.add(seq),
      );
    }

    const filteredIds =
      projectTypeRef.current === ProjectType.SIDE_BY_SIDE
        ? turnIds
        : isSubrow
          ? turnIds.filter((id) => isSubrow(id))
          : turnIds;

    pendingRowsRef.current = new Set(filteredIds);

    const now = Date.now();
    filteredIds.forEach((id) => {
      recentlyStartedEvaluationsRef.current.set(id, now);
    });

    if (trackInference) {
      inferenceTrackingRef.current = new Set(filteredIds);
    }

    if (filteredIds.length > 0) {
      pollingCountRef.current = 1;
      setIsPolling(true);
      queriedChatsRef.current = new Set();
      pollPendingEvaluations();
    }
  };

  const stopPolling = (): void => {
    if (pollingTimerRef.current) {
      clearTimeout(pollingTimerRef.current);
      pollingTimerRef.current = null;
    }
    setIsPolling(false);
    inferenceTrackingRef.current.clear();
  };

  const pollPendingEvaluations = (): void => {
    const pendingIds = Array.from(pendingRowsRef.current);

    if (pendingIds.length === 0) {
      stopPolling();

      return;
    }

    queriedChatsRef.current.clear();

    pendingIds.forEach((turnId) => {
      const chatId = findChatId(turnId);

      if (!chatId) {
        pendingRowsRef.current.delete(turnId);
        inferenceTrackingRef.current.delete(turnId);
        recentlyStartedEvaluationsRef.current.delete(turnId);

        return;
      }

      if (queriedChatsRef.current.has(chatId)) return;

      queriedChatsRef.current.add(chatId);

      if (projectTypeRef.current === ProjectType.SIDE_BY_SIDE) {
        pollSxsEvaluation(chatId);
      } else {
        pollChatEvaluation(chatId);
      }
    });

    if (pendingRowsRef.current.size > 0) {
      pollingTimerRef.current = setTimeout(() => {
        pollingCountRef.current += 1;

        if (pollingCountRef.current > 20) {
          stopPolling();

          return;
        }

        pollPendingEvaluations();
      }, 5000);
    }
  };

  const pollChatEvaluation = (chatId: string): void => {
    getChatHistory(chatId)
      .then((response) => {
        processHistoryResponse(response, chatId);
      })
      .catch(() => {
        handleApiError();
      });
  };

  const pollSxsEvaluation = (pairId: string): void => {
    if (!projectIdRef.current) {
      return;
    }

    getChatSxsWorkbookHistory(pairId, projectIdRef.current)
      .then((response) => {
        processSxsResponse(response, pairId);
      })
      .catch(() => {
        handleApiError();
      });
  };

  const processHistoryResponse = (response: any, chatId: string): void => {
    const turnIdsForThisChat = Array.from(pendingRowsRef.current).filter(
      (turnId) => findChatId(turnId) === chatId,
    );

    turnIdsForThisChat.forEach((turnId) => {
      const chatTurn = response.chat_turns?.find(
        (turn: any) => turn.chat_turn_id === turnId,
      );

      if (!chatTurn) {
        return;
      }

      const evaluations = chatTurn.llm_evaluations || {};

      onEvaluationUpdate(turnId, evaluations);

      if (
        inferenceTrackingRef.current.has(turnId) &&
        onInferenceStatusUpdate &&
        chatTurn.inference_status !== undefined
      ) {
        onInferenceStatusUpdate(
          turnId,
          chatTurn.inference_status,
          chatTurn.inference_reason,
        );

        if (
          chatTurn.inference_status === InferenceStatus.SUCCESSFUL ||
          chatTurn.inference_status === InferenceStatus.FAILED
        ) {
          inferenceTrackingRef.current.delete(turnId);
        }
      }

      let shouldContinuePolling = false;

      const startTime = recentlyStartedEvaluationsRef.current.get(turnId);
      const isRecentlyStarted = startTime && Date.now() - startTime < 10000; // 10 seconds

      if (isRecentlyStarted) {
        shouldContinuePolling = true;
      } else if (Object.keys(evaluations).length > 0) {
        shouldContinuePolling = !areAllEvaluationsComplete(evaluations);
      }

      if (inferenceTrackingRef.current.has(turnId)) {
        shouldContinuePolling = true;
      }

      if (!shouldContinuePolling) {
        pendingRowsRef.current.delete(turnId);
        recentlyStartedEvaluationsRef.current.delete(turnId);
      }
    });

    if (pendingRowsRef.current.size === 0) {
      stopPolling();
    }
  };

  const processSxsResponse = (response: any[], pairId: string): void => {
    const selectedInputs = selectedInputsRef.current;
    const selectedSequences = selectedSequencesRef.current;
    const selectedTurnIds = selectedTurnIdsRef.current;

    response.forEach((item) => {
      if (
        (!item.point_evaluations ||
          Object.keys(item.point_evaluations).length === 0) &&
        (!item.sxs_evaluations ||
          Object.keys(item.sxs_evaluations).length === 0)
      ) {
        return;
      }

      const matchesInput =
        selectedInputs.size > 0 && selectedInputs.has(item.input);
      const matchesSequenceA =
        selectedSequences.size > 0 &&
        item.chat_turn_a?.sequence !== undefined &&
        selectedSequences.has(item.chat_turn_a.sequence);
      const matchesSequenceB =
        selectedSequences.size > 0 &&
        item.chat_turn_b?.sequence !== undefined &&
        selectedSequences.has(item.chat_turn_b.sequence);

      const matchesTurnIdA =
        item.chat_turn_a?.chat_turn_id &&
        selectedTurnIds.has(item.chat_turn_a.chat_turn_id);
      const matchesTurnIdB =
        item.chat_turn_b?.chat_turn_id &&
        selectedTurnIds.has(item.chat_turn_b.chat_turn_id);
      const matchesPairId = selectedTurnIds.has(pairId);

      const isSelected =
        matchesInput ||
        matchesSequenceA ||
        matchesSequenceB ||
        matchesTurnIdA ||
        matchesTurnIdB ||
        matchesPairId;

      if (!isSelected) {
        return;
      }

      if (matchesPairId) {
        onEvaluationUpdate(pairId, item.point_evaluations);
        onEvaluationUpdate(pairId, item.sxs_evaluations);
      }

      if (matchesTurnIdA) {
        onEvaluationUpdate(
          item.chat_turn_a.chat_turn_id,
          item.point_evaluations,
        );
        onEvaluationUpdate(item.chat_turn_a.chat_turn_id, item.sxs_evaluations);
      }

      if (matchesTurnIdB) {
        onEvaluationUpdate(
          item.chat_turn_b.chat_turn_id,
          item.point_evaluations,
        );
        onEvaluationUpdate(item.chat_turn_b.chat_turn_id, item.sxs_evaluations);
      }

      const allPointwiseEvaluationsFinished = Object.keys(
        item.point_evaluations,
      ).every((evalType) => {
        const evaluation = item.point_evaluations[evalType];

        const chatTurnADone =
          evaluation.chatTurnA?.evaluationStatus ===
            EvaluationScoreStatus.SUCCESSFUL ||
          evaluation.chatTurnA?.evaluationStatus ===
            EvaluationScoreStatus.FAILED;

        const chatTurnBDone =
          evaluation.chatTurnB?.evaluationStatus ===
            EvaluationScoreStatus.SUCCESSFUL ||
          evaluation.chatTurnB?.evaluationStatus ===
            EvaluationScoreStatus.FAILED;

        return chatTurnADone && chatTurnBDone;
      });

      const allSideBySideEvaluationsFinished = Object.keys(
        item.sxs_evaluations,
      ).every((evalType) => {
        const evaluation = item.sxs_evaluations[evalType];

        return (
          evaluation?.evaluationStatus === EvaluationScoreStatus.SUCCESSFUL ||
          evaluation?.evaluationStatus === EvaluationScoreStatus.FAILED
        );
      });

      if (allPointwiseEvaluationsFinished || allSideBySideEvaluationsFinished) {
        if (matchesPairId) {
          pendingRowsRef.current.delete(pairId);
        }
        if (matchesTurnIdA) {
          pendingRowsRef.current.delete(item.chat_turn_a.chat_turn_id);
        }
        if (matchesTurnIdB) {
          pendingRowsRef.current.delete(item.chat_turn_b.chat_turn_id);
        }
      }
    });

    if (pendingRowsRef.current.size === 0) {
      stopPolling();
    }
  };

  const handleApiError = () => {
    notifications.show(
      getErrorNotificationConfig(
        "There was an error while loading workbook data.",
      ),
    );
  };

  const areAllEvaluationsComplete = (evaluations: LLMEvaluations): boolean => {
    if (Object.keys(evaluations).length === 0) return false;

    const result = !Object.values(evaluations).some(
      (evaluation) =>
        evaluation.evaluationStatus === EvaluationScoreStatus.PENDING ||
        evaluation.evaluationStatus === EvaluationScoreStatus.IN_PROGRESS,
    );

    return result;
  };

  useEffect(() => {
    return () => stopPolling();
  }, []);

  return { isPolling, startPolling, stopPolling };
}
