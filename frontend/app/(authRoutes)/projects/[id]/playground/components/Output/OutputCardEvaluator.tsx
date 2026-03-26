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

import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import MaterialIcon from "@/components/MaterialIcon";
import { useHumanEvalsContext } from "@/hooks/useHumanEvalsContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import {
  createFeedbackForChatTurnQuery,
  humanEvalSxSQuery,
} from "@/queries/clientQueries";
import { HUMAN_SXS_RATING, HumanEvalSxSPayload } from "@/queries/types";
import { GAevents } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { Text, UnstyledButton } from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useMemo } from "react";

import { CHAT_THUMBS, SIDE_BY_SIDE_EVAL_OPTIONS } from "../../consts";

export default function OutputCardEvaluator() {
  const { projectState, isSideBySide } = useProjectContext();
  const { feedbackEvalId } = useHumanEvalsContext();
  const {
    outputs,
    openPromptCleaningModalOpen,
    selectedChatTurnIds,
    humanEvaluator,
    setHumanEvaluator,
    pairId,
    setHumanEvalReady,
    isLoading,
    lastChatTurnId,
  } = usePlaygroundContext();
  const isThumbsDisabled = useMemo(
    () =>
      isSideBySide
        ? isLoading?.[0] ||
          !outputs?.[0]?.text ||
          isLoading?.[1] ||
          !outputs?.[1]?.text
        : isLoading?.[0] || !outputs?.[0]?.text,
    [outputs, isLoading, isSideBySide],
  );

  const humanEvalPointwiseMutation = useMutation({
    mutationFn: createFeedbackForChatTurnQuery,
    onSuccess: (response) => {
      setHumanEvaluator({
        value: response?.score || "",
        notes: response?.notes || "",
      });
      openPromptCleaningModalOpen();
      setHumanEvalReady(true);
    },
  });

  const humanEvalSxSMutation = useMutation({
    mutationFn: async (params: HumanEvalSxSPayload) =>
      humanEvalSxSQuery({
        projectId: params.projectId,
        pairId: params.pairId,
        rating: params.rating,
        notes: params.notes,
        chat_turn_a_id: selectedChatTurnIds?.[0] ?? undefined,
        chat_turn_b_id: selectedChatTurnIds?.[1] ?? undefined,
      }),
    onSuccess: (_, params) => {
      setHumanEvaluator({
        value: params?.rating || "",
        notes: params?.notes || "",
      });
      setHumanEvalReady(true);
    },
  });

  return (
    <>
      {(isSideBySide ? SIDE_BY_SIDE_EVAL_OPTIONS : CHAT_THUMBS).map(
        (thumb, index) => {
          const baseButtonClassName =
            "border-default flex flex-1 items-center justify-center gap-[4px] rounded-sm p-[6px] text-secondary transition-all duration-200";
          const isActive = humanEvaluator?.value === thumb.value;

          return (
            <UnstyledButton
              key={index}
              disabled={isThumbsDisabled}
              onClick={() => {
                logGAevent(GAevents.ADD_HUMAN_RATING, {
                  source: "playground",
                });

                if (isSideBySide) {
                  const rating = thumb.value as HUMAN_SXS_RATING;
                  humanEvalSxSMutation.mutate({
                    projectId: projectState?.project?.project_id || "",
                    pairId,
                    rating:
                      rating && rating === humanEvaluator?.value
                        ? null
                        : rating,
                    notes: humanEvaluator?.notes || null,
                  });
                } else {
                  const score = thumb.value as number;
                  humanEvalPointwiseMutation.mutate({
                    evaluatorId: feedbackEvalId || "",
                    chatTurnId: lastChatTurnId || "",
                    notes: humanEvaluator?.notes || "",
                    score: humanEvaluator?.value === score ? 0 : score,
                  });
                }
              }}
              variant="subtle"
              className={`${baseButtonClassName} ${isThumbsDisabled ? "!text-disabled cursor-not-allowed" : "hover:bg-veryLightSilver hover:text-secondaryDark"} ${isActive && !isThumbsDisabled && thumb.activeStyle}`}
            >
              {index !== 3 && <MaterialIcon name={thumb.icon} size={20} />}
              <Text className="text-title-12 mx-1">{thumb.name}</Text>
              {isSideBySide && index === 3 && (
                <MaterialIcon name={thumb.icon} size={20} />
              )}
            </UnstyledButton>
          );
        },
      )}
    </>
  );
}
