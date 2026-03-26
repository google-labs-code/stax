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

"use client";

import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import MaterialIcon from "@/components/MaterialIcon";
import { TOOLTIPS } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { routes } from "@/config/routes";
import { useChatContext } from "@/hooks/useChatContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  evaluationChatTurnQuery,
  evaluationChatTurnsQuery,
  evaluationProjectQuery,
  evaluationWholeProjectQuery,
  getAllEvaluatorsQuery,
} from "@/queries/clientQueries";
import {
  EvaluationChatTurnPayload,
  EvaluationChatTurnsPayload,
  EvaluationProjectPayload,
  EvaluatorCardItem,
  EvaluatorModalSource,
  EvaluatorTab,
  GAevents,
  ScorerCardsProps,
} from "@/types";
import { getGroupedEvaluatorsList } from "@/utils/evaluators";
import logGAevent from "@/utils/logGAevent";
import sortByName from "@/utils/sortByName";
import { toggleCardSelection } from "@/utils/toggleScorerCardSelection";
import {
  Badge,
  Button,
  Modal,
  Stack,
  Text,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { Dispatch, SetStateAction, useEffect, useState } from "react";

import EvaluatorTabs from "./EvaluatorTabs";

type ScoreEvaluationModalProps = {
  isOpened: boolean;
  onClose: () => void;
  selectedChatTurns?: WorkbookItem[];
  setRowSelection?: Dispatch<SetStateAction<{}>>;
  onRequestComplete?: () => void;
  onEvaluationStarted?: (turnIds: string[]) => void;
  source?: EvaluatorModalSource;
  projectId?: string;
  selectAllRowsInProject?: boolean;
  onClearBannerState?: () => void;
};

export default function ScoreEvaluationModal({
  isOpened,
  onClose,
  setRowSelection,
  source,
  projectId,
  onRequestComplete,
  onEvaluationStarted,
  selectAllRowsInProject = false,
  onClearBannerState,
}: ScoreEvaluationModalProps) {
  const { startEvaluationResultsPooling, selectedChatTurnIds, selectedPairs } =
    useChatContext();
  const { defaultProjectId } = useProjectsContext();
  const [selectedCards, setSelectedCards] = useState<ScorerCardsProps[]>([]);
  const [data, setData] = useState<EvaluatorCardItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [activeTab, setActiveTab] = useState(EvaluatorTab.DEFAULT);
  const router = useRouter();
  const [initialData, setInitialData] = useState<EvaluatorCardItem[]>([]);
  const { projectType, isSideBySide } = useProjectContext();

  const evaluationProject = useMutation({
    mutationFn: (data: EvaluationProjectPayload) =>
      evaluationProjectQuery(data, projectId || defaultProjectId),
    onSuccess: (response) => {
      startEvaluationResultsPooling(response);
      onRequestComplete && onRequestComplete();
    },
  });

  const evaluationWholeProject = useMutation({
    mutationFn: (data: EvaluationProjectPayload) => {
      notifications.show(
        getSuccessNotificationConfig(
          "Starting evaluation for all rows in the project. This may take some time.",
          "all-rows-evaluation-started",
        ),
      );

      return evaluationWholeProjectQuery(data, projectId || defaultProjectId);
    },
    onSuccess: (response) => {
      startEvaluationResultsPooling(response);
      if (onClearBannerState) {
        onClearBannerState();
      }
      onRequestComplete && onRequestComplete();
    },
  });

  const evaluationChatTurns = useMutation({
    mutationFn: (data: EvaluationChatTurnsPayload) =>
      evaluationChatTurnsQuery(data, projectId || defaultProjectId),
    onSuccess: (response) => {
      if (onEvaluationStarted) {
        onEvaluationStarted(selectedChatTurnIds);
      }
      startEvaluationResultsPooling(response);
      onRequestComplete && onRequestComplete();
    },
  });

  const evaluationChatTurn = useMutation({
    mutationFn: (data: EvaluationChatTurnPayload) =>
      evaluationChatTurnQuery(data, projectId || defaultProjectId),
    onSuccess: (_, params) => {
      const chatTurnId =
        params?.chat_turn_id || params?.sxs_pair?.chat_turn_id_a || null;
      if (onEvaluationStarted && chatTurnId) {
        onEvaluationStarted([chatTurnId]);
      }

      onRequestComplete && onRequestComplete();
    },
  });

  const handleClose = () => {
    onClose();
    setSelectedCards([]);
    setRowSelection && setRowSelection({});
  };

  const handleRemoveCard = (cardId: string) => {
    setSelectedCards((prevCards) =>
      prevCards.filter((card) => card.id !== cardId),
    );
  };

  const handleTabChange: Dispatch<SetStateAction<EvaluatorTab>> = (value) => {
    const newTab = typeof value === "function" ? value(activeTab) : value;
    setActiveTab(newTab);
    setSelectedCards([]);
  };

  const getAllEvaluators = useMutation({
    mutationFn: () => getAllEvaluatorsQuery({ evaluation_type: projectType }),
    onSuccess: (response) => {
      const finalData = getGroupedEvaluatorsList(response);

      setData((prevData) => {
        return sortByName([...prevData, ...finalData]);
      });
      setInitialData((prevData) => {
        return sortByName([...prevData, ...finalData]);
      });
      setIsLoading(false);
    },
  });

  useEffect(() => {
    if (isOpened) {
      setData([]);
      setIsLoading(true);
      getAllEvaluators.mutate();
    }
  }, [isOpened]);

  const handleRunEvaluation = () => {
    handleClose();
    const evaluatorIds = selectedCards.map((card) => card.id);

    if (source === EvaluatorModalSource.WORKBOOK) {
      logGAevent(GAevents.PROJECT_EVAL, {
        evaluatorNames: selectedCards.map((card) => card.title).join(","),
      });
    }

    const isWholeProjectEvaluation = selectAllRowsInProject && projectId;
    if (isWholeProjectEvaluation) {
      evaluationWholeProject.mutate({
        evaluator_ids: evaluatorIds,
      });

      return;
    }

    let isSingleItemEvaluation = false;
    if (isSideBySide) {
      isSingleItemEvaluation =
        !selectAllRowsInProject &&
        selectedPairs.length === 1 &&
        evaluatorIds.length === 1;
    } else {
      isSingleItemEvaluation =
        !selectAllRowsInProject &&
        selectedChatTurnIds.length === 1 &&
        evaluatorIds.length === 1;
    }

    // Single chat turn is supported for Pointwise project for now
    if (isSingleItemEvaluation && !isSideBySide) {
      notifications.hide("single-evaluation-score-started");
      notifications.show(
        getSuccessNotificationConfig(
          "Evaluation running.",
          "single-evaluation-score-started",
        ),
      );

      evaluationChatTurn.mutate({
        evaluator_id: evaluatorIds[0],
        chat_turn_id: selectedChatTurnIds[0],
      });

      return;
    }

    notifications.hide("multi-evaluation-score-started");
    notifications.show(
      getSuccessNotificationConfig(
        <div className="flex flex-row items-center justify-between gap-xl">
          <Text>Evaluation started. This might take a few moments.</Text>

          {defaultProjectId && (
            <UnstyledButton
              className="!font-medium text-body-14"
              onClick={() => {
                router.push(`${routes.projects}/${defaultProjectId}`);
                notifications.hide("multi-evaluation-score-started");
              }}
            >
              Check Status
            </UnstyledButton>
          )}
        </div>,
        "multi-evaluation-score-started",
      ),
    );

    if (selectedChatTurnIds.length === 0) {
      evaluationProject.mutate({
        evaluator_ids: evaluatorIds,
      });
    } else {
      if (isSideBySide) {
        evaluationChatTurns.mutate({
          evaluator_ids: evaluatorIds,
          pair_evaluations: selectedPairs,
        });
      } else {
        evaluationChatTurns.mutate({
          evaluator_ids: evaluatorIds,
          chat_turn_ids: selectedChatTurnIds,
        });
      }
    }
  };

  return (
    <Modal
      opened={isOpened}
      onClose={handleClose}
      size="700px"
      title={
        <Text className="!font-medium text-title-22">Select evaluator(s)</Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0",
        body: "px-0 py-[24px]",
      }}
    >
      <Stack gap="16px">
        <EvaluatorTabs
          isLoading={isLoading}
          activeTab={activeTab}
          setActiveTab={handleTabChange}
          data={data}
          setData={setData}
          initialData={initialData}
          selectedCards={selectedCards}
          setSelectedCards={setSelectedCards}
          toggleCardSelection={toggleCardSelection}
        />

        <div className="px-6 flex items-center justify-between">
          <div className="flex-1 flex flex-wrap gap-2 overflow-y-auto max-w-[460px]">
            {selectedCards.map((card) => (
              <Badge
                key={card.id}
                className="h-[24px] min-h-[24px] rounded-lg bg-lightBlue font-normal !py-0"
                classNames={{
                  root: "border-0 px-3",
                  label: "flex items-center justify-between gap-2",
                }}
                rightSection={
                  <div
                    className="cursor-pointer flex items-center justify-center ml-1"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleRemoveCard(card.id);
                    }}
                  >
                    <MaterialIcon
                      name="close"
                      size={14}
                      className="text-secondary"
                    />
                  </div>
                }
              >
                <span className="text-body-11 text-brand">{card.title}</span>
              </Badge>
            ))}
          </div>

          <div className="ml-4 flex-shrink-0">
            {!selectedCards.length ? (
              <Tooltip
                label={TOOLTIPS.NO_EVALUATORS_SELECTED_MESSAGE}
                position="bottom"
                offset={20}
                withArrow
              >
                <span>
                  <Button disabled={true} color="blue" size="lg">
                    <Text className="text-neutrals-50 text-body-12">
                      Run Evaluation
                    </Text>
                  </Button>
                </span>
              </Tooltip>
            ) : (
              <Button color="blue" size="lg" onClick={handleRunEvaluation}>
                <Text className="text-neutrals-50 text-body-12">
                  Run Evaluation
                </Text>
              </Button>
            )}
          </div>
        </div>
      </Stack>
    </Modal>
  );
}
