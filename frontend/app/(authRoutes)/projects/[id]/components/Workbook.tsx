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

import AddRunsToProjectModal from "@/components/AddRunsToProjectModal";
import CreateNewProjectModal from "@/components/CreateNewProjectModal";
import { CustomBottomToolbar } from "@/components/CustomBottomToolbar";
import ScoreEvaluationModal from "@/components/Evaluation/ScoreEvaluationModal";
import GenerateOutputsModal from "@/components/GenerateOutputsModal/GenerateOutputsModal";
import ManageTagsModal from "@/components/ManageTagsModal";
import MaterialIcon from "@/components/MaterialIcon";
import SideBySideEvaluatorColumn from "@/components/table/columns/SideBySideEvaluatorColumn";
import {
  getTableBodyCellClassName,
  getTableHeadCellClassName,
  getTableMantineContainerProps,
  getTableMantinePaperProps,
} from "@/components/table/tableProps";
import { getBaseTableConfig } from "@/config/getBaseTableConfig";
import {
  getErrorNotificationConfig,
  getSuccessNotificationConfig,
} from "@/config/notifications";
import { useChatContext } from "@/hooks/useChatContext";
import { useHumanEvalsContext } from "@/hooks/useHumanEvalsContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  createFeedbackForChatTurnQuery,
  createNewProjectDataRowQuery,
  createNewSxSRowQuery,
  evaluationChatTurnQuery,
  generateOutputsQuery,
  getChatSxsWorkbookHistory,
  getChatWorkbookHistory,
  humanEvalSxSQuery,
  updateChatVariablesQuery,
  updateExpectedOutput,
  updateSxSPairQuery,
  updateWorkbookRowQuery,
} from "@/queries/clientQueries";
import { HumanEvalSxSPayload } from "@/queries/types";
import {
  EvaluationChatTurnPayload,
  EvaluationScoreStatus,
  EvaluatorModalSource,
  GAevents,
  GenerateOutputsPayload,
  GenerateOutputsType,
  InferenceChatCompletionPromptRole,
  LLMEvaluation,
  LLMEvaluations,
  ManageTagsModalType,
  ProjectComboboxItem,
  ProjectType,
  TagLinkEntityType,
  WorkbookMeta,
} from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import { buildSxSRowData } from "@/utils/buildSxSRowData";
import logGAevent from "@/utils/logGAevent";
import { useSelectionBanner } from "@/utils/selectionBannerUtils";
import {
  getEvaluationFilterFn,
  getEvaluationSortingFn,
} from "@/utils/sortingUtils";
import {
  findChatIdForTurnInData,
  updateTableDataWithEvaluations,
} from "@/utils/workbookHelpers";
import { Box, Group, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { Row } from "@tanstack/react-table";
import {
  MRT_Column,
  MRT_ColumnDef,
  MRT_Icons,
  MRT_Row,
  MantineReactTable,
  useMantineReactTable,
} from "mantine-react-table";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import { useProjectContext } from "../../hooks/useProjectContext";
import {
  WORKBOOK_TABLE_COLUMNS_POINTWISE,
  WORKBOOK_TABLE_COLUMNS_SIDE_BY_SIDE,
} from "../config/tableConfig";
import { useEvaluationPolling } from "../hooks/useEvaluationPolling";
import { InferenceStatus, WorkbookItem, WorkbookProps } from "../types";
import { symmetricDifference, workbookAddRowElement } from "../utils/workbook";
import ClearResultsModal from "./ClearResultsModal";
import ColumnHeader, { renderColumnActionsMenuItems } from "./ColumnHeader";
import EvaluationStatusElement from "./EvaluationStatusElement";
import EvaluationStatusModal from "./EvaluationStatusModal";
import WorkbookActionMenu from "./WorkbookActionMenu";
import WorkbookDeleteModal from "./WorkbookDeleteModal";
import WorkbookHeader from "./WorkbookHeader";

const cellHeaderIcons: Partial<MRT_Icons> = {
  IconDotsVertical: () => (
    <MaterialIcon name="arrow_drop_down" className="text-resting" size={20} />
  ),
};

export default function Workbook(props: WorkbookProps) {
  const { projectActions, projectState, isSideBySide, projectType } =
    useProjectContext();
  const finalWorkbookColumns = useMemo(
    () =>
      isSideBySide
        ? WORKBOOK_TABLE_COLUMNS_SIDE_BY_SIDE
        : WORKBOOK_TABLE_COLUMNS_POINTWISE,
    [isSideBySide],
  );

  const workbookRows = useMemo(
    () =>
      isSideBySide
        ? props.data?.sxs_rows?.map((sxsRow) => ({
            ...buildSxSRowData(sxsRow), // @TODO: TEMP SOLUTION, REFACTOR THIS AFTER LAUNCH
          }))
        : props.data?.workbook_rows,
    [isSideBySide, props.data],
  );

  const [rowSelection, setRowSelection] = useState<Record<string, boolean>>({});
  const [rowTableSelection, setRowTableSelection] = useState<
    Row<WorkbookItem>[]
  >([]);
  const [rowDataSelection, setRowDataSelection] = useState<WorkbookItem[]>([]);
  const [data, setData] = useState<WorkbookItem[]>();
  const [activeRows, setActiveRows] = useState<WorkbookItem[]>([]);
  const [loadingRows, setLoadingRows] = useState<Set<string>>(new Set());
  const processingRowsRef = useRef(new Set());
  const isManuallyAddingRowRef = useRef(false);
  const currentPageRef = useRef(props.page);
  const defaultHiddenColumns = {
    id: false,
    chat_turn_id: false,
    chat_id: false,
    model_response_id: false,
    "mrt-row-expand": false,
  };
  const evaluationUpdateTimeoutsRef = useRef<Map<string, NodeJS.Timeout>>(
    new Map(),
  );
  const [temporarilyHighlightedRows, setTemporarilyHighlightedRows] = useState(
    new Set(),
  );
  const [
    isWorkbookDeleteModalOpen,
    { open: openWorkbookDeleteModal, close: closeWorkbookDeleteModal },
  ] = useDisclosure(false);
  const [
    isEvaluationModalOpened,
    { open: openEvaluationModal, close: closeEvaluationModal },
  ] = useDisclosure(false);
  const [
    isGenerateOutputsModalOpened,
    { open: openGenerateOutputsModal, close: closeGenerateOutputsModal },
  ] = useDisclosure(false);
  const [generateOutputsType, setGenerateOutputsType] =
    useState<GenerateOutputsType>(GenerateOutputsType.SELECTED_ROWS);
  const [
    isClearResultsModalOpened,
    { open: openClearResultsModal, close: closeClearResultsModal },
  ] = useDisclosure(false);
  const { setSelectedChatTurnIds, setSelectedPairs } = useChatContext();
  const [
    isManageTagsModalOpened,
    { open: openManageTagsModal, close: closeManageTagsModal },
  ] = useDisclosure(false);
  const [manageTagsType, setManageTagsType] = useState<ManageTagsModalType>(
    ManageTagsModalType.ADD,
  );
  const [selectedDataset, setSelectedDataset] =
    useState<ProjectComboboxItem | null>(null);
  const [expandedRowIds, setExpandedRowIds] = useState<Set<string>>(new Set());
  const [
    isAddToProjectOpened,
    { open: openAddToProject, close: closeAddToProject },
  ] = useDisclosure(false);
  const [
    isEvaluationStatusModalOpened,
    { open: openEvaluationStatusModal, close: closeEvaluationStatusModal },
  ] = useDisclosure(false);
  const [
    isCreateNewProjectOpened,
    { open: openCreateNewProjectModal, close: closeCreateNewProjectModal },
  ] = useDisclosure(false);
  const [evaluationStatusData, setEvaluationStatusData] =
    useState<LLMEvaluation | null>(null);
  const pendingNewRowsRef = useRef<Set<string>>(new Set());
  const [subRowsCount, setSubRowsCount] = useState(0);
  const prevRowIdsRef = useRef(new Set());
  const evaluatingRowsRef = useRef<Map<string, Set<string>>>(new Map());
  const lastKnownTotalSizeRef = useRef(props.data?.total_size || 0);
  const lastKnownTotalPagesRef = useRef(1);
  const deletionInProgressRef = useRef(false);
  const [hasNewRow, setHasNewRow] = useState(false);
  const newRowIdRef = useRef<string | null>(null);
  const isRefreshingAfterGenerationRef = useRef(false);
  const [transferToDataset, setTransferToDataset] = useState(false);
  const [columns, setColumns] = useState(finalWorkbookColumns);
  const pendingEvaluationTurnsRef = useRef(new Set<string>());
  const highlightTimeoutsRef = useRef<Map<string, NodeJS.Timeout>>(new Map());
  const evaluationStartTimesRef = useRef<Map<string, number>>(new Map());
  const initialEvaluationStatesRef = useRef<
    Map<string, Record<string, string>>
  >(new Map());
  const [addedRowsCount, setAddedRowsCount] = useState(0);
  const suppressInitialHighlightingRef = useRef(true);

  useEffect(() => {
    if (props.projectId) {
      suppressInitialHighlightingRef.current = true;
      prevDataRef.current = [];
    }
  }, [props.projectId]);

  useEffect(() => {
    currentPageRef.current = props.page;

    setTemporarilyHighlightedRows(new Set());
    evaluationStartTimesRef.current = new Map();
    initialEvaluationStatesRef.current = new Map();
    prevRowIdsRef.current = new Set();
    suppressInitialHighlightingRef.current = true;
    prevDataRef.current = [];
    setAddedRowsCount(0);
  }, [props.page]);

  const openAddToProjectDatasetModal = (showTransferToDataset = false) => {
    setTransferToDataset(showTransferToDataset);
    openAddToProject();
  };

  const getSelectedEntityProps = () => {
    if (transferToDataset) {
      return {
        selectedEntity: selectedDataset,
        setSelectedEntity: setSelectedDataset,
      };
    } else {
      return {
        selectedEntity: selectedProject,
        setSelectedEntity: setSelectedProject,
      };
    }
  };

  const openCreateEntity = () => {
    closeAddToProject();
    openCreateNewProjectModal();
  };
  const prevDataRef = useRef<any[]>([]);
  const changedRowIdsRef = useRef<Set<string>>(new Set());

  const highlightRow = useCallback((turnId: string): void => {
    const highlightStartPage = currentPageRef.current;

    if (highlightTimeoutsRef.current.has(turnId)) {
      clearTimeout(highlightTimeoutsRef.current.get(turnId));
      highlightTimeoutsRef.current.delete(turnId);
    }

    setTemporarilyHighlightedRows((prev) => {
      const newSet = new Set(prev);
      newSet.add(turnId);

      return newSet;
    });

    const highlightDuration = 3000;

    const timeout = setTimeout(() => {
      if (currentPageRef.current === highlightStartPage) {
        setTemporarilyHighlightedRows((prev) => {
          const newSet = new Set(prev);
          newSet.delete(turnId);

          return newSet;
        });
        changedRowIdsRef.current.delete(turnId);
      }
      highlightTimeoutsRef.current.delete(turnId);
    }, highlightDuration);

    highlightTimeoutsRef.current.set(turnId, timeout);
  }, []);

  const getEvaluationObject = useCallback(
    (data: WorkbookItem) => {
      if (isSideBySide) {
        return {
          ...data.point_evaluations,
          ...data.sxs_evaluations,
        };
      }

      return data.llm_evaluations || {};
    },
    [isSideBySide],
  );

  useEffect(() => {
    if (!data || !Array.isArray(data)) {
      return;
    }

    if (
      suppressInitialHighlightingRef.current ||
      props.isCsvUploadRef?.current
    ) {
      prevDataRef.current = JSON.parse(JSON.stringify(data));
      suppressInitialHighlightingRef.current = false;

      return;
    }

    if (!prevDataRef.current || prevDataRef.current.length === 0) {
      prevDataRef.current = JSON.parse(JSON.stringify(data));

      return;
    }

    const hasNewRowsAdded = data.length > prevDataRef.current.length;
    if (hasNewRowsAdded || isManuallyAddingRowRef.current) {
      const prevRowIds = prevDataRef.current
        .map((row) => row.chat_turn_id || row.id)
        .filter((id) => id !== undefined);

      data.forEach((row) => {
        const rowId = row.chat_turn_id || row.id;
        if (rowId && !prevRowIds.includes(rowId)) {
          pendingNewRowsRef.current.add(rowId);
          if (row.chat_turn_a?.chat_turn_id)
            pendingNewRowsRef.current.add(row.chat_turn_a.chat_turn_id);
          if (row.chat_turn_b?.chat_turn_id)
            pendingNewRowsRef.current.add(row.chat_turn_b.chat_turn_id);
        }
      });
    }

    data.forEach((row) => {
      const rowId = row.chat_turn_id || row.id;
      if (!rowId) return;

      const isNewOrPendingRow =
        pendingNewRowsRef.current.has(rowId) ||
        rowId === newRowIdRef.current ||
        hasNewRow;

      if (isNewOrPendingRow) return;

      const prevRow = prevDataRef.current.find(
        (prevRow) => (prevRow.chat_turn_id || prevRow.id) === rowId,
      );

      if (!prevRow) return;

      const evaluationTypes = [
        { curr: row.llm_evaluations, prev: prevRow.llm_evaluations },
        { curr: row.sxs_evaluations, prev: prevRow.sxs_evaluations },
        { curr: row.point_evaluations, prev: prevRow.point_evaluations },
      ];

      let hasEvaluationChanges = false;
      let hasCompletedEvals = false;

      for (const evalType of evaluationTypes) {
        if (!evalType.curr && !evalType.prev) continue;

        if (JSON.stringify(evalType.curr) !== JSON.stringify(evalType.prev)) {
          hasEvaluationChanges = true;

          if (
            evalType.curr === row.point_evaluations &&
            row.point_evaluations
          ) {
            Object.values(row.point_evaluations).forEach((pointEval) => {
              if (
                pointEval.chatTurnA?.evaluationStatus ===
                  EvaluationScoreStatus.SUCCESSFUL ||
                pointEval.chatTurnA?.evaluationStatus ===
                  EvaluationScoreStatus.FAILED ||
                pointEval.chatTurnB?.evaluationStatus ===
                  EvaluationScoreStatus.SUCCESSFUL ||
                pointEval.chatTurnB?.evaluationStatus ===
                  EvaluationScoreStatus.FAILED
              ) {
                hasCompletedEvals = true;
              }
            });
          } else if (evalType.curr) {
            const hasCompleted = Object.values(evalType.curr).some(
              (evaluation) =>
                evaluation.evaluationStatus ===
                  EvaluationScoreStatus.SUCCESSFUL ||
                evaluation.evaluationStatus === EvaluationScoreStatus.FAILED,
            );

            if (hasCompleted) {
              hasCompletedEvals = true;
            }
          }
        }
      }

      if (
        hasEvaluationChanges &&
        hasCompletedEvals &&
        !changedRowIdsRef.current.has(rowId)
      ) {
        changedRowIdsRef.current.add(rowId);
        highlightRow(rowId);
      }
    });

    let pendingCleanupTimeout: NodeJS.Timeout | null = null;
    if (pendingNewRowsRef.current.size > 0) {
      const rowIds = Array.from(pendingNewRowsRef.current);
      pendingCleanupTimeout = setTimeout(() => {
        rowIds.forEach((id) => pendingNewRowsRef.current.delete(id));
      }, 5000);
    }

    prevDataRef.current = JSON.parse(JSON.stringify(data));

    return () => {
      if (pendingCleanupTimeout) {
        clearTimeout(pendingCleanupTimeout);
      }
    };
  }, [data, highlightRow, hasNewRow]);

  const selectionBanner = useSelectionBanner({
    rowSelection,
    setRowSelection,
    setRowDataSelection,
    setRowTableSelection,
    data,
    page: props.page,
    totalSize: (props.data?.total_size || 0) + addedRowsCount,
  });

  const createNewSxSRowMutation = useMutation({
    mutationFn: () => {
      return createNewSxSRowQuery(props.projectId);
    },
    onSuccess: (res) => {
      setData((prevData) => {
        return [
          {
            ...res,
            pairId: res?.id,
            chat_turn_id: res?.chat_turn_id_a,
            chat_id: res?.chat_id_a,
            variables: {},
            chat_turn_a: {
              chat_turn_id: res?.chat_turn_id_a,
              variables: {},
            },
            chat_turn_b: {
              chat_turn_id: res?.chat_turn_id_b,
              variables: {},
            },
          } as unknown as WorkbookItem,
          ...(prevData || []),
        ];
      });
      if (res?.id) {
        highlightRow(res.id);
      }

      setAddedRowsCount((prev) => prev + 1);

      notifications.show(
        getSuccessNotificationConfig(
          "New row added successfully.",
          "new-row-added",
        ),
      );
    },
  });

  const createEmptyRowMutation = useMutation({
    mutationFn: () => {
      return createNewProjectDataRowQuery({
        projectId: props.projectId,
      });
    },
    onSuccess: (res) => {
      setData((prevData) => {
        return [res, ...(prevData || [])];
      });
      const newRowId = res.chat_turn_id || res.id;
      if (newRowId) {
        highlightRow(newRowId);
      }

      setAddedRowsCount((prev) => prev + 1);

      notifications.show(
        getSuccessNotificationConfig(
          "New row added successfully.",
          "new-row-added",
        ),
      );
    },
  });

  const handleInferenceStatusUpdate = (
    turnId: string,
    inferenceStatus: number | null,
    inferenceReason?: string,
  ): void => {
    if (inferenceStatus !== null) {
      const statusAsEnum = inferenceStatus as unknown as InferenceStatus;

      setData((prevData) => {
        if (!prevData) return prevData;

        return prevData.map((row) => {
          const newRow = { ...row };

          if (row.chat_turn_id === turnId) {
            newRow.inference_status = statusAsEnum;
            if (inferenceReason) {
              newRow.inference_reason = inferenceReason;
            }
          }

          if (row.subRows) {
            newRow.subRows = row.subRows.map((subRow) => {
              if (subRow.chat_turn_id === turnId) {
                return {
                  ...subRow,
                  inference_status: statusAsEnum,
                  inference_reason: inferenceReason || subRow.inference_reason,
                };
              }

              return subRow;
            });
          }

          return newRow;
        });
      });

      if (
        inferenceStatus === InferenceStatus.SUCCESSFUL ||
        inferenceStatus === InferenceStatus.FAILED
      ) {
        highlightRow(turnId);

        if (processingRowsRef.current.has(turnId)) {
          processingRowsRef.current.delete(turnId);

          if (processingRowsRef.current.size === 0) {
            isRefreshingAfterGenerationRef.current = false;
          }
        }
      }
    }
  };

  const isSubrow = useCallback(
    (turnId: string): boolean => {
      if (!data) return false;

      for (const row of data) {
        if (row.subRows) {
          const isInSubRows = row.subRows.some(
            (subRow) => subRow.chat_turn_id === turnId,
          );
          if (isInSubRows) return true;
        }
      }

      return false;
    },
    [data],
  );

  const evaluationChatTurnQueryMutation = useMutation({
    mutationFn: (payload: EvaluationChatTurnPayload) => {
      return evaluationChatTurnQuery(payload, props.projectId);
    },
    onSuccess: (_, payload) => {
      const chatTurnId = payload.chat_turn_id;
      if (
        typeof chatTurnId === "string" &&
        evaluatingRowsRef.current.has(chatTurnId)
      ) {
        let pollingOptions = {};

        if (isSideBySide) {
          const selectedInputs: string[] = [];
          const selectedSequences: number[] = [];

          if (data) {
            data.forEach((row) => {
              if (row.chat_turn_a?.chat_turn_id === chatTurnId && row.input) {
                selectedInputs.push(row.input);
                if (row.chat_turn_a.sequence !== undefined) {
                  selectedSequences.push(row.chat_turn_a.sequence);
                }
              } else if (
                row.chat_turn_b?.chat_turn_id === chatTurnId &&
                row.input
              ) {
                selectedInputs.push(row.input);
                if (row.chat_turn_b.sequence !== undefined) {
                  selectedSequences.push(row.chat_turn_b.sequence);
                }
              }

              if (row.subRows) {
                row.subRows.forEach((subRow) => {
                  if (
                    subRow.chat_turn_a?.chat_turn_id === chatTurnId &&
                    subRow.input
                  ) {
                    selectedInputs.push(subRow.input);
                    if (subRow.chat_turn_a.sequence !== undefined) {
                      selectedSequences.push(subRow.chat_turn_a.sequence);
                    }
                  } else if (
                    subRow.chat_turn_b?.chat_turn_id === chatTurnId &&
                    subRow.input
                  ) {
                    selectedInputs.push(subRow.input);
                    if (subRow.chat_turn_b.sequence !== undefined) {
                      selectedSequences.push(subRow.chat_turn_b.sequence);
                    }
                  }
                });
              }
            });
          }

          pollingOptions = {
            inputs: selectedInputs,
            sequences: selectedSequences,
          };
        }

        if (isSideBySide) {
          startPolling([chatTurnId], false, pollingOptions);
        } else {
          startPolling([chatTurnId]);
        }
      }
    },
    onSettled: () => {
      projectActions.setHasRemainingJobs(true);
    },
  });

  const handleEvaluationRerun = (
    chatTurnId: string,
    evaluatorId: string,
    evaluationType: string,
  ) => {
    projectActions.setHasRemainingJobs(true);
    setData((prevData) => {
      if (!prevData) return prevData;

      return prevData.map((row) => {
        if (row.chat_turn_id === chatTurnId) {
          if (row.llm_evaluations?.[evaluationType]) {
            const updatedEvaluations = { ...row.llm_evaluations };
            updatedEvaluations[evaluationType] = {
              ...updatedEvaluations[evaluationType],
              evaluationStatus: EvaluationScoreStatus.PENDING,
            };

            return { ...row, llm_evaluations: updatedEvaluations };
          }
        }

        if (row.subRows) {
          const updatedSubRows = row.subRows.map((subRow) => {
            if (
              subRow.chat_turn_id === chatTurnId &&
              subRow.llm_evaluations?.[evaluationType]
            ) {
              const updatedEvaluations = { ...subRow.llm_evaluations };
              updatedEvaluations[evaluationType] = {
                ...updatedEvaluations[evaluationType],
                evaluationStatus: EvaluationScoreStatus.PENDING,
              };

              return { ...subRow, llm_evaluations: updatedEvaluations };
            }

            return subRow;
          });

          if (
            row.subRows.some((subRow) => subRow.chat_turn_id === chatTurnId)
          ) {
            return { ...row, subRows: updatedSubRows };
          }
        }

        return row;
      });
    });

    const trackedTypes = new Set<string>([evaluationType]);
    evaluatingRowsRef.current.set(chatTurnId, trackedTypes);

    evaluationChatTurnQueryMutation.mutate({
      chat_turn_id: chatTurnId,
      evaluator_id: evaluatorId,
    });
  };

  const getUpdatedEvaluationRows = (
    row: WorkbookItem,
    evaluationType: string,
    type: "A" | "B",
  ) => {
    const updatedEvaluations = { ...row.point_evaluations };
    if (type === "A") {
      updatedEvaluations[evaluationType] = {
        ...updatedEvaluations[evaluationType],
        chatTurnA: {
          ...updatedEvaluations[evaluationType].chatTurnA,
          evaluationStatus: EvaluationScoreStatus.PENDING,
        },
      };
    } else {
      updatedEvaluations[evaluationType] = {
        ...updatedEvaluations[evaluationType],
        chatTurnB: {
          ...updatedEvaluations[evaluationType].chatTurnB,
          evaluationStatus: EvaluationScoreStatus.PENDING,
        },
      };
    }

    return {
      ...row,
      point_evaluations: updatedEvaluations,
      sxs_evaluations: {
        ...row.sxs_evaluations,
        [evaluationType]: {
          ...row?.sxs_evaluations?.[evaluationType],
          evaluationStatus: EvaluationScoreStatus.PENDING,
        },
      },
    };
  };

  const handleSXSEvaluationRerun = (
    chatTurnId: string,
    evaluatorId: string,
    evaluationType: string,
    type: "A" | "B",
  ) => {
    projectActions.setHasRemainingJobs(true);

    setData((prevData) => {
      if (!prevData) return prevData;

      return prevData.map((row) => {
        const isMainRowA = row.chat_turn_a?.chat_turn_id === chatTurnId;
        const isMainRowB = row.chat_turn_b?.chat_turn_id === chatTurnId;

        if (
          (isMainRowA || isMainRowB) &&
          (row.point_evaluations?.[evaluationType] ||
            row.sxs_evaluations?.[evaluationType])
        ) {
          return getUpdatedEvaluationRows(row, evaluationType, type);
        }

        if (row.subRows) {
          const updatedSubRows = row.subRows.map((subRow) => {
            const isSubRowA = subRow.chat_turn_a?.chat_turn_id === chatTurnId;
            const isSubRowB = subRow.chat_turn_b?.chat_turn_id === chatTurnId;

            if (
              (isSubRowA || isSubRowB) &&
              (subRow.point_evaluations?.[evaluationType] ||
                subRow.sxs_evaluations?.[evaluationType])
            ) {
              return getUpdatedEvaluationRows(subRow, evaluationType, type);
            }

            return subRow;
          });

          if (
            row.subRows.some(
              (subRow) =>
                subRow.chat_turn_a?.chat_turn_id === chatTurnId ||
                subRow.chat_turn_b?.chat_turn_id === chatTurnId,
            )
          ) {
            return { ...row, subRows: updatedSubRows };
          }
        }

        return row;
      });
    });

    const trackedTypes = new Set<string>([evaluationType]);
    evaluatingRowsRef.current.set(chatTurnId, trackedTypes);

    evaluationChatTurnQueryMutation.mutate({
      chat_turn_id: chatTurnId,
      evaluator_id: evaluatorId,
    });
  };

  const handleEvaluationUpdate = (
    turnId: string,
    evaluations: LLMEvaluations,
  ): void => {
    setData((prevData) => {
      const updatedData = updateTableDataWithEvaluations(
        prevData,
        turnId,
        evaluations,
      );

      return updatedData;
    });

    const trackedEvaluationTypes = evaluatingRowsRef.current.get(turnId);
    if (trackedEvaluationTypes) {
      Object.keys(evaluations).forEach((type) => {
        trackedEvaluationTypes.add(type);
      });
    }

    const allEvaluationsComplete = Object.values(evaluations).every(
      (evaluation) =>
        evaluation.evaluationStatus === EvaluationScoreStatus.SUCCESSFUL ||
        evaluation.evaluationStatus === EvaluationScoreStatus.FAILED,
    );

    if (allEvaluationsComplete) {
      pendingEvaluationTurnsRef.current.delete(turnId);
    }
  };

  const { startPolling } = useEvaluationPolling(
    handleEvaluationUpdate,
    (turnId: string) =>
      findChatIdForTurnInData(
        data,
        turnId,
        isSideBySide ? ProjectType.SIDE_BY_SIDE : ProjectType.POINTWISE,
      ),
    handleInferenceStatusUpdate,
    isSubrow,
    isSideBySide ? ProjectType.SIDE_BY_SIDE : ProjectType.POINTWISE,
    props.projectId,
  );

  useEffect(() => {
    if (props.page !== currentPageRef.current) {
      setExpandedRowIds(new Set());
      setSubRowsCount(0);
      table?.resetExpanded();
      currentPageRef.current = props.page;
    }
  }, [props.data, props.page]);

  useEffect(() => {
    return () => {
      highlightTimeoutsRef.current.forEach((timeout) => {
        clearTimeout(timeout);
      });
      highlightTimeoutsRef.current.clear();

      evaluationUpdateTimeoutsRef.current.forEach((timeout) => {
        clearTimeout(timeout);
      });
      evaluationUpdateTimeoutsRef.current.clear();
    };
  }, []);

  const [selectedProject, setSelectedProject] =
    useState<ProjectComboboxItem | null>(null);

  const initialColumnVisibility = useMemo(() => {
    const storedColumnValue = LocalStorage.get("projectTableConfig");
    if (!storedColumnValue) {
      return defaultHiddenColumns;
    }

    try {
      const columnsConfig = JSON.parse(storedColumnValue);
      const savedVisibility = columnsConfig.columnVisibility || {};

      return {
        ...savedVisibility,
        ...defaultHiddenColumns,
      };
    } catch {
      return defaultHiddenColumns;
    }
  }, []);

  const onBackClick = () => {
    closeCreateNewProjectModal();
    openAddToProject();
  };

  useEffect(() => {
    if (props.projectError) {
      notifications.show(
        getErrorNotificationConfig(
          props.projectError.message,
          "There was an error while loading workbook data.",
        ),
      );
    }
  });

  useEffect(() => {
    if (!data || data.length === 0) return;

    if (isManuallyAddingRowRef.current && !props.isCsvUploadRef?.current) {
      isManuallyAddingRowRef.current = false;

      return;
    }

    const currentRowIds = data
      .map((row) => row.chat_turn_id || row.id)
      .filter((id): id is string => id !== undefined);

    const prevRowIds = Array.from(prevRowIdsRef.current);
    const rowsToHighlight: string[] = [];

    if (prevRowIds.length === 0 && props.isCsvUploadRef?.current) {
      rowsToHighlight.push(
        ...currentRowIds.filter((id) => id !== newRowIdRef.current),
      );
      prevRowIdsRef.current = new Set(currentRowIds);
    } else if (prevRowIds.length > 0) {
      currentRowIds.forEach((id) => {
        if (!prevRowIds.includes(id) && id !== newRowIdRef.current) {
          rowsToHighlight.push(id);

          if (isRefreshingAfterGenerationRef.current) {
            pendingNewRowsRef.current.add(id);
          }
        }
      });
      prevRowIdsRef.current = new Set(currentRowIds);
    } else {
      prevRowIdsRef.current = new Set(currentRowIds);

      return;
    }

    if (
      isRefreshingAfterGenerationRef.current ||
      pendingNewRowsRef.current.size > 0
    ) {
      const completedRows: string[] = [];

      data.forEach((row) => {
        const rowId = row.chat_turn_id || row.id;
        if (!rowId) return;

        const isGenerationComplete =
          (row.output && row.inference_status === InferenceStatus.SUCCESSFUL) ||
          row.inference_status === InferenceStatus.FAILED;

        if (processingRowsRef.current.has(rowId) && isGenerationComplete) {
          completedRows.push(rowId);
          processingRowsRef.current.delete(rowId);
          highlightRow(rowId);
        } else if (
          pendingNewRowsRef.current.has(rowId) &&
          isGenerationComplete
        ) {
          completedRows.push(rowId);
          pendingNewRowsRef.current.delete(rowId);
          highlightRow(rowId);
        }
      });

      if (processingRowsRef.current.size === 0) {
        isRefreshingAfterGenerationRef.current = false;
      }
    }

    if (rowsToHighlight.length > 0) {
      rowsToHighlight.forEach((id) => {
        highlightRow(id);
      });
    }

    const csvUploadTimeout = setTimeout(() => {
      if (props.isCsvUploadRef) {
        props.isCsvUploadRef.current = false;
      }
    }, 500);

    return () => {
      clearTimeout(csvUploadTimeout);
    };
  }, [data]);

  const handleBeforeDelete = () => {
    deletionInProgressRef.current = true;
  };

  const expandingRowRef = useRef<MRT_Row<WorkbookItem> | null>(null);

  const handleExpandClick = useCallback(
    (row: MRT_Row<WorkbookItem>) => {
      const rowId = row.original.id || `row_${Date.now()}`;

      let requestId = "";
      if (isSideBySide) {
        requestId = row.original.pairId || "";
        if (!requestId) {
          return;
        }
      } else {
        requestId = row.original.chat_id || "";
        if (!requestId) {
          return;
        }
      }

      if (row.getIsExpanded()) {
        row.toggleExpanded(false);

        const newExpandedRows = new Set(expandedRowIds);
        newExpandedRows.delete(rowId);
        setExpandedRowIds(newExpandedRows);

        const subRowCount = row.original.subRows?.length || 0;
        setSubRowsCount((prevCount) => Math.max(0, prevCount - subRowCount));

        return;
      }

      if (row.original.subRows && row.original.subRows.length > 0) {
        row.toggleExpanded(true);
        const newExpandedRows = new Set(expandedRowIds);
        newExpandedRows.add(rowId);
        setExpandedRowIds(newExpandedRows);

        const subRowCount = row.original.subRows.length;
        setSubRowsCount((prevCount) => prevCount + subRowCount);

        return;
      }

      setLoadingRows((prev) => {
        const newSet = new Set(prev);
        newSet.add(rowId);

        return newSet;
      });

      expandingRowRef.current = row;
      chatHistoryMutation.mutate({ requestId, rowId });
    },
    [isSideBySide],
  );

  const chatHistoryMutation = useMutation({
    mutationFn: (params: { requestId: string; rowId: string }) => {
      if (isSideBySide) {
        return getChatSxsWorkbookHistory(params.requestId, props.projectId);
      }

      return getChatWorkbookHistory(params.requestId);
    },
    onSuccess: (
      responseData: WorkbookItem[],
      params: { requestId: string; rowId: string },
    ) => {
      if (responseData.length === 0) {
        setLoadingRows((prev) => {
          const newSet = new Set(prev);
          newSet.delete(params.rowId);

          return newSet;
        });

        return;
      }

      setData((currentData) => {
        if (!currentData) return currentData;

        const rowToUpdate = currentData.find(
          (item) => item.id === params.rowId,
        );

        if (!rowToUpdate) return currentData;

        const finalData = responseData.map((item, index) => ({
          ...item,
          isSubRow: true,
          isTheLastOne: index === responseData.length - 1,
          pairId: params.requestId,
        }));

        if (isSideBySide) {
          finalData.sort((a, b) => {
            const seqA = a.chat_turn_a?.sequence ?? 0;
            const seqB = b.chat_turn_a?.sequence ?? 0;

            return seqA - seqB;
          });
        } else {
          finalData.sort((a, b) => {
            const seqA = a.sequence ?? 0;
            const seqB = b.sequence ?? 0;
            if (seqA !== seqB) return seqA - seqB;

            const timeA = a.created_at ?? 0;
            const timeB = b.created_at ?? 0;

            return timeA - timeB;
          });
        }

        const subRowCount = finalData.length;
        setSubRowsCount((prevCount) => prevCount + subRowCount);

        const updatedData = currentData.map((item) => {
          if (item.id === params.rowId) {
            return {
              ...item,
              subRows: finalData,
            };
          }

          return item;
        });

        return updatedData;
      });

      setTimeout(() => {
        setExpandedRowIds((prevExpandedRows) => {
          const newExpandedRows = new Set(prevExpandedRows);
          newExpandedRows.add(params.rowId);

          return newExpandedRows;
        });

        setTimeout(() => {
          if (expandingRowRef.current) {
            expandingRowRef.current.toggleExpanded(true);
            expandingRowRef.current = null;
          }

          setLoadingRows((prev) => {
            const newSet = new Set(prev);
            newSet.delete(params.rowId);

            return newSet;
          });
        }, 100);
      }, 200);
    },

    onError: (error, params) => {
      expandingRowRef.current = null;

      setLoadingRows((prev) => {
        const newSet = new Set(prev);
        newSet.delete(params.rowId);

        return newSet;
      });

      notifications.show(
        getErrorNotificationConfig(
          error.message || "Failed to load chat history",
        ),
      );
    },
  });

  const updateSxSRatingForSpecificRow = (
    pairId: string,
    chatTurnAId: string,
    chatTurnBId: string,
    field: keyof WorkbookItem,
    data: any,
  ) => {
    setData((prevData) => {
      return prevData?.map((row: any) => {
        const isMatchingParentRow =
          row.pairId === pairId &&
          row.chat_turn_a?.chat_turn_id === chatTurnAId &&
          row.chat_turn_b?.chat_turn_id === chatTurnBId;

        if (isMatchingParentRow && row[field] !== undefined) {
          row[field] = data;
        }

        if (row?.subRows) {
          row.subRows = row?.subRows?.map((subRow: any) => {
            const isMatchingSubRow =
              subRow.pairId === pairId &&
              subRow.chat_turn_a?.chat_turn_id === chatTurnAId &&
              subRow.chat_turn_b?.chat_turn_id === chatTurnBId;

            if (isMatchingSubRow && subRow[field] !== undefined) {
              subRow[field] = data;
            }

            return subRow;
          });
        }

        return row;
      });
    });
  };

  useEffect(() => {
    if (deletionInProgressRef.current && data) {
      deletionInProgressRef.current = false;

      setTemporarilyHighlightedRows(new Set());
      evaluatingRowsRef.current.clear();
      processingRowsRef.current.clear();

      prevRowIdsRef.current = new Set(
        data.map((row) => row.chat_turn_id || row.id),
      );
    }
  }, [data]);

  const tableData = useMemo(() => {
    if (!data) return [];

    return [
      ...data,
      {
        id: "add-row",
        chat_turn_id: "add-row",
        isAddRow: true,
      } as unknown as WorkbookItem,
    ];
  }, [data]);

  const updateRowFieldData = (
    chatTurnId: string,
    field: keyof WorkbookItem,
    data: any,
  ) => {
    setData((prevData) => {
      return prevData?.map((row: any) => {
        if (row.chat_turn_id === chatTurnId && row[field] !== undefined) {
          row[field] = data;
        }

        if (row?.subRows) {
          row.subRows = row?.subRows?.map((subRow: any) => {
            if (
              subRow.chat_turn_id === chatTurnId &&
              subRow[field] !== undefined
            ) {
              subRow[field] = data;
            }

            return subRow;
          });
        }

        return row;
      });
    });
  };

  const updateSxSPairFieldData = (
    pairId: string,
    field: keyof WorkbookItem,
    data: any,
  ) => {
    setData((prevData) => {
      return prevData?.map((row: any) => {
        if (row.pairId === pairId && row[field] !== undefined) {
          row[field] = data;
        }

        if (row?.subRows) {
          row.subRows = row?.subRows?.map((subRow: any) => {
            if (subRow.pairId === pairId && subRow[field] !== undefined) {
              subRow[field] = data;
            }

            return subRow;
          });
        }

        return row;
      });
    });
  };

  const updateInputMutation = useMutation({
    mutationFn: (data: { input: string; rowOriginal: WorkbookItem }) => {
      const chatTurnId = data.rowOriginal.chat_turn_id;

      return updateWorkbookRowQuery(props.projectId, chatTurnId, {
        prompt: {
          role: InferenceChatCompletionPromptRole.USER,
          text: data.input,
        },
      });
    },
    onSuccess: (res, params) => {
      const isEmptyRow =
        hasNewRow && newRowIdRef.current === params.rowOriginal.chat_turn_id;

      setData((prevData) => {
        if (!prevData) return prevData;

        return prevData.map((row) => {
          if (row.chat_turn_id === params.rowOriginal.chat_turn_id) {
            return res;
          }

          return row;
        });
      });

      const rowId = res.chat_turn_id || res.id;
      if (rowId) {
        highlightRow(rowId);
      }

      if (isEmptyRow) {
        setHasNewRow(false);
        newRowIdRef.current = null;
      }

      params.rowOriginal.isInputLoading = false;
    },
    onError: (error, params) => {
      params.rowOriginal.isInputLoading = false;
      notifications.show(
        getErrorNotificationConfig(
          "Failed to update row. Please try again.",
          "update-row-error",
        ),
      );
    },
  });

  const updateInputSxSMutation = useMutation({
    mutationFn: (data: { input: string; rowOriginal: WorkbookItem }) => {
      const pairId = data.rowOriginal.pairId;

      return updateSxSPairQuery(props.projectId, pairId || "", {
        input: data.input,
      });
    },
    onSuccess: (res, params) => {
      const isEmptyRow =
        hasNewRow && newRowIdRef.current === params.rowOriginal.id;

      setData((prevData) => {
        if (!prevData) return prevData;

        return prevData.map((row) => {
          if (row.id === params.rowOriginal.id) {
            return {
              ...buildSxSRowData(res),
            } as unknown as WorkbookItem;
          }

          return row;
        });
      });

      const rowId = res.id;
      if (rowId) {
        highlightRow(rowId);
      }

      if (isEmptyRow) {
        setHasNewRow(false);
        newRowIdRef.current = null;
      }

      params.rowOriginal.isInputLoading = false;
    },
    onError: (error, params) => {
      params.rowOriginal.isInputLoading = false;
      notifications.show(
        getErrorNotificationConfig(
          "Failed to update row. Please try again.",
          "update-row-error",
        ),
      );
    },
  });

  const { feedbackEvalId } = useHumanEvalsContext();

  const humanEvalMutation = useMutation({
    mutationFn: async (params: {
      chatTurnId: string;
      score: number;
      notes: string;
      rowOriginal: WorkbookItem;
    }) =>
      createFeedbackForChatTurnQuery({
        evaluatorId: feedbackEvalId || "",
        chatTurnId: params.rowOriginal.chat_turn_id,
        score: params.score || 0,
        notes: params.notes,
      }),
    onSuccess: (res, params) => {
      if (params.rowOriginal.isHumanEvalNotesLoading) {
        highlightRow(params.chatTurnId);
      } else {
        props.setRefreshHumanPassRate(true);
      }
      updateRowFieldData(params.chatTurnId, "human_eval_scores", [res]);
      params.rowOriginal.isHumanEvalNotesLoading = false;
    },
    onError: (_, params) => {
      params.rowOriginal.isHumanEvalNotesLoading = false;
      notifications.show(
        getErrorNotificationConfig("Failed to save human evaluator"),
      );
    },
  });

  const humanEvalSxSNotesMutation = useMutation({
    mutationFn: async (params: {
      projectId: string;
      pairId: string;
      notes: string | null;
      chatTurnId: string;
      rowOriginal: any;
    }) => {
      const currentRating = params.rowOriginal.human_sxs_rating || null;

      return humanEvalSxSQuery({
        projectId: params.projectId,
        pairId: params.pairId,
        rating: currentRating,
        notes: params.notes,
        chat_turn_a_id: params.rowOriginal?.chat_turn_a?.chat_turn_id,
        chat_turn_b_id: params.rowOriginal?.chat_turn_b?.chat_turn_id,
      });
    },
    onSuccess: (_, params) => {
      const chatTurnAId = params.rowOriginal?.chat_turn_a?.chat_turn_id;
      const chatTurnBId = params.rowOriginal?.chat_turn_b?.chat_turn_id;

      highlightRow(params.chatTurnId);
      updateSxSRatingForSpecificRow(
        params.pairId,
        chatTurnAId,
        chatTurnBId,
        "human_sxs_notes",
        params.notes,
      );
      params.rowOriginal.isHumanEvalNotesLoading = false;
    },
    onError: (_, params) => {
      params.rowOriginal.isHumanEvalNotesLoading = false;
      notifications.show(getErrorNotificationConfig("Failed to save notes"));
    },
  });

  const humanEvalSxSMutation = useMutation({
    mutationFn: async (
      params: HumanEvalSxSPayload & {
        chatTurnId: string;
        rowOriginal: any;
      },
    ) =>
      humanEvalSxSQuery({
        projectId: params.projectId,
        pairId: params.pairId,
        rating: params.rating,
        notes: params.notes,
        chat_turn_a_id: params.rowOriginal?.chat_turn_a?.chat_turn_id,
        chat_turn_b_id: params.rowOriginal?.chat_turn_b?.chat_turn_id,
      }),
    onSuccess: (_, params) => {
      const chatTurnAId = params.rowOriginal?.chat_turn_a?.chat_turn_id;
      const chatTurnBId = params.rowOriginal?.chat_turn_b?.chat_turn_id;

      if (params.rowOriginal.isHumanEvalNotesLoading) {
        highlightRow(params.chatTurnId);
        updateSxSRatingForSpecificRow(
          params.pairId,
          chatTurnAId,
          chatTurnBId,
          "human_sxs_notes",
          params.notes,
        );
        params.rowOriginal.isHumanEvalNotesLoading = false;
      } else {
        updateSxSRatingForSpecificRow(
          params.pairId,
          chatTurnAId,
          chatTurnBId,
          "human_sxs_rating",
          params.rating,
        );
        props.setRefreshHumanPassRate(true);
      }
    },
    onError: (_, params) => {
      params.rowOriginal.isHumanEvalNotesLoading = false;
      notifications.show(getErrorNotificationConfig("Failed to save notes"));
    },
  });

  const handleAddNewRow = useCallback(() => {
    const addRowMutation = isSideBySide
      ? createNewSxSRowMutation
      : createEmptyRowMutation;

    if (addRowMutation && addRowMutation.status !== "pending") {
      addRowMutation.mutate();
    }
  }, [isSideBySide]);

  const updateExpectedOutputMutation = useMutation({
    mutationFn: (params: {
      chatTurnId: string;
      value: string;
      rowOriginal: WorkbookItem;
    }) => updateExpectedOutput(params.chatTurnId, params.value),
    onSuccess: (_, params) => {
      params.rowOriginal.isExpectedOutputLoading = false;
      updateRowFieldData(params.chatTurnId, "expected_output", params.value);
    },
  });

  const updateExpectedOutputSxSMutation = useMutation({
    mutationFn: (params: {
      pairId: string;
      value: string;
      rowOriginal: WorkbookItem;
    }) =>
      updateSxSPairQuery(props.projectId, params.pairId, {
        expectedOutput: params.value,
      }),
    onSuccess: (_, params) => {
      params.rowOriginal.isExpectedOutputLoading = false;
      updateSxSPairFieldData(params.pairId, "expected_output", params.value);
    },
  });

  const { defaultProjectId } = useProjectsContext();
  const onOutputRerunMutation = useMutation({
    mutationFn: (params: {
      data: GenerateOutputsPayload;
      rowOriginal: WorkbookItem;
    }) =>
      generateOutputsQuery(params.data, props.projectId || defaultProjectId),
    onSuccess: (_, params) => {
      projectActions.setHasRemainingJobs(true);
      updateRowFieldData(
        params.rowOriginal.chat_turn_id,
        "inference_status",
        InferenceStatus.PENDING,
      );
    },
  });

  const updateSystemInstructionMutation = useMutation({
    mutationFn: (params: {
      chatTurnId: string;
      value: string;
      rowOriginal: WorkbookItem;
    }) => {
      return updateWorkbookRowQuery(props.projectId, params.chatTurnId, {
        prompt: {
          role: InferenceChatCompletionPromptRole.SYSTEM,
          text: params.value || "",
        },
      });
    },
    onSuccess: (_, params) => {
      params.rowOriginal.isSystemInstructionsLoading = false;

      if (isSideBySide) {
        params.rowOriginal.system_instructions = params.value;
      } else {
        updateRowFieldData(
          params.chatTurnId,
          "system_instructions",
          params.value,
        );
      }

      highlightRow(params.chatTurnId);
    },
    onError: (_, params) => {
      params.rowOriginal.isSystemInstructionsLoading = false;
      notifications.show(
        getErrorNotificationConfig(
          "Failed to update row. Please try again.",
          "update-row-error",
        ),
      );
    },
  });

  const updateVariablesMutation = useMutation({
    mutationFn: (params: {
      chatId: string;
      chatTurnId: string;
      variables: { [key: string]: string };
      rowOriginal: WorkbookItem;
    }) => updateChatVariablesQuery(params.chatId, params.variables),
    onSuccess: (response, params) => {
      updateRowFieldData(params.chatTurnId, "variables", response);
      highlightRow(params.chatTurnId);
    },
  });

  const updateSxSVariablesMutation = useMutation({
    mutationFn: (params: {
      chatTurnId: string;
      pairId: string;
      variables: { [key: string]: string };
      rowOriginal: WorkbookItem;
    }) =>
      updateSxSPairQuery(props.projectId, params.pairId, {
        variables: params.variables,
      }),
    onSuccess: (_, params) => {
      updateRowFieldData(params.chatTurnId, "variables", params.variables);
      highlightRow(params.chatTurnId);
    },
  });

  const tableConfig = getBaseTableConfig<WorkbookItem>();

  useEffect(() => {
    if (props.data && workbookRows) {
      setData((prevData) => {
        if (hasNewRow && newRowIdRef.current && data) {
          const newRow = data.find((row) => row.id === newRowIdRef.current);
          if (newRow) {
            return [newRow, ...(workbookRows || [])];
          }
        }

        if (!prevData) return workbookRows;

        const expandedRowsWithSubRows = new Map();
        prevData.forEach((row) => {
          const idField = isSideBySide ? "id" : "chat_id";
          const rowId = row[idField];

          if (
            rowId &&
            expandedRowIds.has(rowId) &&
            row.subRows &&
            row.subRows.length > 0
          ) {
            expandedRowsWithSubRows.set(rowId, row.subRows);
          }
        });

        if (expandedRowsWithSubRows.size === 0) {
          return workbookRows;
        }

        return (workbookRows || []).map((newRow) => {
          const idField = isSideBySide ? "id" : "chat_id";
          const rowId = newRow[idField];

          if (rowId && expandedRowsWithSubRows.has(rowId)) {
            return {
              ...newRow,
              subRows: expandedRowsWithSubRows.get(rowId),
            };
          }

          return newRow;
        });
      });
    }
  }, [props.data, expandedRowIds]);

  const table = useMantineReactTable({
    ...tableConfig,
    icons: cellHeaderIcons,
    columns,
    state: {
      isLoading: projectState.isLoadingProject,
      sorting: projectState.sorting,
      rowSelection,
    },
    data: tableData || [],
    meta: {
      openTagsModal: (rows) => {
        setActiveRows(rows);
        setManageTagsType(ManageTagsModalType.ADD);
        openManageTagsModal();
      },
      loadingRows,
      projectId: props.projectId,
      refetchProject: props.refetchProject,
      onInputChange: (input, currentValue, rowOriginal) => {
        if (input !== currentValue) {
          if (isSideBySide) {
            if (input.trim()) {
              rowOriginal.isInputLoading = true;
              updateInputSxSMutation.mutate({
                input,
                rowOriginal,
              });
            }
          } else {
            if (input.trim()) {
              rowOriginal.isInputLoading = true;
              updateInputMutation.mutate({
                input,
                rowOriginal,
              });
            }
          }
        }
      },
      onUpdateChatVariables: async (
        variables,
        currentVariables,
        rowOriginal,
      ) => {
        if (!rowOriginal.chat_id) {
          return;
        }

        if (JSON.stringify(currentVariables) !== JSON.stringify(variables)) {
          if (isSideBySide) {
            updateSxSVariablesMutation.mutate({
              chatTurnId: rowOriginal.chat_turn_id,
              pairId: rowOriginal.pairId || "",
              variables,
              rowOriginal,
            });
          } else {
            updateVariablesMutation.mutate({
              chatTurnId: rowOriginal.chat_turn_id,
              chatId: rowOriginal.chat_id,
              variables,
              rowOriginal,
            });
          }
        }
      },
      onExpectedOutputChange: (newValue, currentValue, rowOriginal) => {
        if (!rowOriginal.chat_turn_id) {
          return;
        }

        if (JSON.stringify(newValue) !== JSON.stringify(currentValue)) {
          rowOriginal.isExpectedOutputLoading = true;
          if (isSideBySide) {
            updateExpectedOutputSxSMutation.mutate({
              pairId: rowOriginal.pairId || "",
              value: newValue,
              rowOriginal,
            });
          } else {
            updateExpectedOutputMutation.mutate({
              chatTurnId: rowOriginal.chat_turn_id,
              value: newValue,
              rowOriginal,
            });
          }
        }
      },
      onHumanEvalScoreChange: (newValue, currentValue, rowOriginal) => {
        logGAevent(GAevents.ADD_HUMAN_RATING, {
          source: "workbook",
        });
        if (newValue !== currentValue) {
          humanEvalMutation.mutate({
            chatTurnId: rowOriginal.chat_turn_id,
            score: newValue,
            notes: rowOriginal.human_eval_scores?.[0]?.notes || "",
            rowOriginal,
          });
        } else {
          humanEvalMutation.mutate({
            chatTurnId: rowOriginal.chat_turn_id,
            score: 0,
            notes: rowOriginal.human_eval_scores?.[0]?.notes || "",
            rowOriginal,
          });
        }
      },
      onHumanEvalNotesChange: (newValue, currentValue, rowOriginal) => {
        if (newValue !== currentValue) {
          rowOriginal.isHumanEvalNotesLoading = true;
          humanEvalMutation.mutate({
            chatTurnId: rowOriginal.chat_turn_id,
            score: rowOriginal.human_eval_scores?.[0]?.score || 0,
            notes: newValue || "",
            rowOriginal,
          });
        }
      },
      onHumanEvalSxSNotesChange: (newValue, currentValue, rowOriginal) => {
        if (newValue !== currentValue) {
          rowOriginal.isHumanEvalNotesLoading = true;

          humanEvalSxSNotesMutation.mutate({
            projectId: props.projectId,
            pairId: rowOriginal?.pairId || "",
            chatTurnId: rowOriginal.chat_turn_id,
            notes: newValue || null,
            rowOriginal,
          });
        }
      },
      onHumanEvalSxSRatingChange: (newValue, rowOriginal) => {
        logGAevent(GAevents.ADD_HUMAN_RATING, {
          source: "workbook",
        });

        humanEvalSxSMutation.mutate({
          projectId: props.projectId,
          pairId: rowOriginal?.pairId || "",
          chatTurnId: rowOriginal.chat_turn_id,
          rating: newValue !== rowOriginal?.human_sxs_rating ? newValue : null,
          notes: rowOriginal.human_sxs_notes || null,
          rowOriginal,
        });
      },
      onSystemInstructionChange: (newValue, currentValue, rowOriginal) => {
        if (!rowOriginal?.chat_turn_id) {
          return;
        }

        if (JSON.stringify(newValue) !== JSON.stringify(currentValue)) {
          rowOriginal.isSystemInstructionsLoading = true;
          updateSystemInstructionMutation.mutate({
            chatTurnId: rowOriginal.chat_turn_id,
            value: newValue,
            rowOriginal,
          });
        }
      },
      onTagRemove: (tagId, rowOriginal) => {
        updateRowFieldData(
          rowOriginal.chat_turn_id,
          "tags",
          rowOriginal.tags?.filter((tag) => tag.id !== tagId),
        );
      },
      onTagAdd: (tag, rowOriginal) => {
        updateRowFieldData(rowOriginal.chat_turn_id, "tags", [
          ...(rowOriginal.tags || []),
          tag,
        ]);
      },
      onExpandClick: (row) => {
        handleExpandClick(row);
      },
      onOutputRerun: (rowOriginal) => {
        onOutputRerunMutation.mutate({
          data: {
            chat_turn_ids: [rowOriginal.chat_turn_id],
            model_ids: [rowOriginal.model_id],
          },
          rowOriginal,
        });
      },
      projectType,
    } as WorkbookMeta,
    enablePagination: false,
    enableColumnResizing: true,
    enableTopToolbar: true,
    enableExpandAll: false,
    onRowSelectionChange: (rowSelectionFunc) => {
      const rowsChanged =
        typeof rowSelectionFunc === "function"
          ? rowSelectionFunc(rowSelection)
          : rowSelection;
      const rowsChangedKeys = Object.keys(rowsChanged);
      const rowsSelectedKeys = Object.keys(rowSelection);
      const isRowUnchecked = rowsChangedKeys.length < rowsSelectedKeys.length;
      const changedKeys = symmetricDifference(
        rowsChangedKeys,
        rowsSelectedKeys,
      );

      // Case 1 - row(s) is (are) unchecked:
      // - if everything has been selected, clean state and keep only remaining rows on this page checked
      // - else remove only unchecked row(s) from state
      // Case 2 - rows(s) is (are) checekd:
      // - add the checked rows to the state avoiding repetitions
      if (isRowUnchecked) {
        if (selectionBanner.selectAllRowsInProject) {
          selectionBanner.setSelectAllRowsInProject(false);
          const items = table
            .getCoreRowModel()
            .rows.filter((row) =>
              rowsChangedKeys.includes(row.original.chat_turn_id),
            );
          setRowTableSelection(items);
          setRowDataSelection(items.map((row) => row.original));
        } else {
          setRowTableSelection((prev: Row<WorkbookItem>[]) =>
            prev.filter(
              (item) => !changedKeys.includes(item.original.chat_turn_id),
            ),
          );
          setRowDataSelection((prev: WorkbookItem[]) =>
            prev.filter((item) => !changedKeys.includes(item.chat_turn_id)),
          );
        }
      } else {
        const changedRows = table
          .getCoreRowModel()
          .rows.filter((row) =>
            changedKeys.includes(row.original.chat_turn_id),
          );
        setRowDataSelection((prev: WorkbookItem[]) => {
          // avoid repetitions
          const newItems = changedRows
            .map((row) => row.original)
            .filter(
              (i: WorkbookItem) =>
                !prev.find(
                  (j: WorkbookItem) => j.chat_turn_id === i.chat_turn_id,
                ),
            );

          return !newItems.length ? prev : [...prev, ...newItems];
        });
        setRowTableSelection((prev: Row<WorkbookItem>[]) => {
          // avoid repetitions
          const newItems = changedRows.filter(
            (i: Row<WorkbookItem>) =>
              !prev.find(
                (j: Row<WorkbookItem>) =>
                  j.original.chat_turn_id === i.original.chat_turn_id,
              ),
          );

          return !newItems.length ? prev : [...prev, ...newItems];
        });
      }

      setRowSelection(rowSelectionFunc);
    },
    getRowId: (originalRow: WorkbookItem) => {
      if (isSideBySide && (originalRow as any).isSubRow === true) {
        const sequence = originalRow.chat_turn_a?.sequence;
        const uniqueId = sequence
          ? `${originalRow.pairId}_${sequence}`
          : `${originalRow.pairId}_${originalRow.id}`;

        return uniqueId;
      }

      return originalRow.chat_turn_id || originalRow.id;
    },
    enableSorting: true,
    autoResetExpanded: false,
    enableEditing: false,
    enableRowSelection: (row) => !row.original.isAddRow,
    enableSubRowSelection: false,
    enableColumnActions: true,
    enableColumnPinning: true,
    enableBottomToolbar: true,
    enableFilters: false,
    enableExpanding: true,
    getRowCanExpand: (row) => {
      return row.depth === 0 && row.original.is_chat !== false;
    },
    enableFullScreenToggle: false,
    enableColumnDragging: false,
    enableStickyHeader: true,
    filterFromLeafRows: false,
    layoutMode: "grid",
    mantineSelectCheckboxProps: ({ row }) => {
      if (row.original.isAddRow) {
        return {
          className: "hidden",
          size: "sm",
          disabled: true,
        };
      }

      return { size: "sm" };
    },
    initialState: {
      pagination: { pageSize: 15, pageIndex: 0 },
      columnPinning: {
        left: ["mrt-row-select"],
        right: ["mrt-row-actions"],
      },
      columnVisibility: {
        ...initialColumnVisibility,
        ...defaultHiddenColumns,
      },
      columnOrder: ["mrt-row-select"],
    },
    mantineTableHeadCellProps: ({ column }) => {
      return { className: getTableHeadCellClassName(column.id) };
    },
    mantineTableBodyCellProps: ({ row, column }) => {
      const isAddRow = row.original.isAddRow;

      if (isAddRow) {
        return workbookAddRowElement(column.id, handleAddNewRow);
      }

      return {
        className: getTableBodyCellClassName(column.id),
      };
    },
    mantinePaperProps: ({ table }) => {
      return getTableMantinePaperProps(table);
    },
    mantinePaginationProps: {
      showRowsPerPage: false,
    },
    enableColumnVirtualization: false,
    enableRowVirtualization: false,
    mantineTableContainerProps: ({ table }) => {
      return {
        className: `${getTableMantineContainerProps(table).className} md:!max-h-[458px]`,
      };
    },
    mantineBottomToolbarProps: () => {
      return {
        className: "border-0 shadow-none !p-0",
      };
    },
    renderRowActions: ({ row }) => {
      const selectedRow = row.original;

      return (
        <div className="flex items-center justify-center">
          <WorkbookActionMenu
            initialColumnOrder={initialColumnOrder}
            initialColumnVisibility={initialColumnVisibility}
            setRowSelection={setRowSelection}
            projectId={props.projectId}
            selectedRow={selectedRow}
            onRowDelete={props.onRowDelete}
            onAddTags={() => {
              setActiveRows([selectedRow]);
              setManageTagsType(ManageTagsModalType.ADD);
              openManageTagsModal();
            }}
            onRemoveTags={() => {
              setActiveRows([selectedRow]);
              setManageTagsType(ManageTagsModalType.REMOVE);
              openManageTagsModal();
            }}
            onClearResults={() => {
              setActiveRows([selectedRow]);
              openClearResultsModal();
            }}
            onDuplicateSuccess={(res) => {
              setData((prevData) => {
                let rowExistsAlready = false;
                const veryOldData =
                  prevData?.map((item) => {
                    if (item.chat_turn_id === res.chat_turn_id) {
                      rowExistsAlready = true;

                      return res;
                    }

                    return item;
                  }) || [];

                if (!rowExistsAlready) {
                  return [res, ...veryOldData];
                }

                return veryOldData;
              });
            }}
            setAddedRowsCount={setAddedRowsCount}
          />
        </div>
      );
    },
    defaultColumn: {
      Cell: (props) => (
        <Text className="text-secondary text-body-12">
          {props.renderedCellValue}
        </Text>
      ),
    },
    positionToolbarAlertBanner: "bottom",
    renderColumnActionsMenuItems,
    mantineTableBodyRowProps: ({ row }) => {
      const rowId = row.original.chat_turn_id || row.original.id;
      const pairId = row.original.pairId;
      const turnAId = row.original.chat_turn_a?.chat_turn_id;
      const turnBId = row.original.chat_turn_b?.chat_turn_id;

      // Check if any of these IDs are in our highlight set
      const isHighlighted =
        temporarilyHighlightedRows.has(rowId) ||
        (pairId && temporarilyHighlightedRows.has(pairId)) ||
        (turnAId && temporarilyHighlightedRows.has(turnAId)) ||
        (turnBId && temporarilyHighlightedRows.has(turnBId));

      const isSelected = row.getIsSelected();
      const isAddRow = row.original.isAddRow;

      return {
        className: `${isAddRow ? "cursor-pointer !h-[40px] max-h-[40px] group hover:bg-lightBlue transition-all duration-0" : "border-0"} 
        ${isSelected && "selected-row"} 
        ${isHighlighted && "!bg-lightBlue"}`,
        "data-row-id": rowId,
        "data-add-row": isAddRow ? "true" : undefined,
        ...(isAddRow && {
          onClick: (e) => {
            e.stopPropagation();
            e.preventDefault();
            handleAddNewRow();
          },
        }),
      };
    },
    mantineTableProps: {
      highlightOnHover: false,
    },
    mantineTableHeadRowProps: () => {
      return {
        className: "border-0",
      };
    },
    renderTopToolbar: ({ table }) => {
      const selectedCount = Object.keys(rowSelection).length;

      return (
        <Group>
          <WorkbookHeader
            {...props}
            selectedRows={rowTableSelection}
            onAddTags={() => {
              setActiveRows(rowDataSelection);
              setManageTagsType(ManageTagsModalType.ADD);
              openManageTagsModal();
            }}
            onRemoveTags={() => {
              setActiveRows(rowDataSelection);
              setManageTagsType(ManageTagsModalType.REMOVE);
              openManageTagsModal();
            }}
            onAddToProject={() => openAddToProjectDatasetModal(false)}
            onAddToDataset={() => openAddToProjectDatasetModal(true)}
            table={table}
            onEvaluate={() => {
              let selectedRows: string[] = [];

              if (isSideBySide) {
                rowDataSelection.forEach((row: WorkbookItem) => {
                  if (row.isSubRow === true) {
                    if (
                      row.chat_turn_a?.chat_turn_id &&
                      row.chat_turn_a?.output
                    ) {
                      selectedRows.push(row.chat_turn_a.chat_turn_id);
                    }
                    if (
                      row.chat_turn_b?.chat_turn_id &&
                      row.chat_turn_b?.output
                    ) {
                      selectedRows.push(row.chat_turn_b.chat_turn_id);
                    }
                  } else {
                    if (row.chat_turn_id && row.output) {
                      selectedRows.push(row.chat_turn_id);
                    }
                    if (
                      row.chat_turn_b?.chat_turn_id &&
                      row.chat_turn_b?.output
                    ) {
                      selectedRows.push(row.chat_turn_b.chat_turn_id);
                    }
                  }
                });

                setSelectedPairs(
                  rowDataSelection.map((row: WorkbookItem) => {
                    return {
                      id: row?.pairId || row?.id || "",
                      chat_turn_id_a: row.chat_turn_a?.chat_turn_id || "",
                      chat_turn_id_b: row.chat_turn_b?.chat_turn_id || "",
                    };
                  }),
                );
              } else {
                selectedRows = rowDataSelection
                  .filter((row: WorkbookItem) => !!row.output)
                  .map((row: WorkbookItem) => row.chat_turn_id);
                setSelectedPairs([]);
              }

              setSelectedChatTurnIds(selectedRows);
              openEvaluationModal();
            }}
            onGenerateModels={(type) => {
              setGenerateOutputsType(type);
              openGenerateOutputsModal();
            }}
            openWorkbookDeleteModal={openWorkbookDeleteModal}
            onClearResults={openClearResultsModal}
            hideTooltips={props.hideTooltips}
            projectId={props.projectId}
            selectionBanner={{
              selectAllRowsInProject: selectionBanner.selectAllRowsInProject,
              allRowsSelected: selectionBanner.allRowsSelected,
              totalInProject: selectionBanner.totalInProject,
              currentPageSize: selectionBanner.currentPageSize,
              selectedCount: selectedCount,
              onSelectAll: selectionBanner.handleSelectAllRows,
              onClearSelection: selectionBanner.handleClearSelection,
              visible: selectionBanner.showBanner || selectedCount > 0,
            }}
          />
        </Group>
      );
    },
    renderBottomToolbar: ({ table }) => {
      if (props.data?.total_size !== undefined) {
        lastKnownTotalSizeRef.current = props.data.total_size;
      }

      const adjustedTotalSize = lastKnownTotalSizeRef.current + subRowsCount;

      const calculatedTotalPages = Math.max(
        1,
        Math.ceil(adjustedTotalSize / props.pageSize),
      );

      if (props.data?.total_size !== undefined) {
        lastKnownTotalPagesRef.current = calculatedTotalPages;
      }

      const totalPages =
        props.data?.total_size !== undefined
          ? calculatedTotalPages
          : lastKnownTotalPagesRef.current;

      return (
        <CustomBottomToolbar
          table={table}
          nextPageToken={props.data?.next_page_token}
          totalSize={adjustedTotalSize}
          refetchProject={props.refetchProject}
          totalPages={totalPages}
          addedRowsCount={addedRowsCount}
          setPage={props.setPage}
          pageSize={props.pageSize}
          showPageSizeSelector
          page={props.page}
          setPageSize={props.setPageSize}
          selectAllRowsInProject={selectionBanner.selectAllRowsInProject}
          onClearSelection={selectionBanner.handleClearSelection}
        />
      );
    },
    displayColumnDefOptions: {
      "mrt-row-actions": {
        enableResizing: false,
        header: "",
      },
      "mrt-row-expand": {
        enableResizing: false,
        size: 0,
      },
    },
  });

  const initialColumnOrder = useMemo(() => {
    const storedColumnValue = LocalStorage.get("projectTableConfig");
    const columnsConfig = storedColumnValue
      ? JSON.parse(storedColumnValue)
      : {};

    const parsedColumnOrder: string[] = columnsConfig.columnOrder
      ? columnsConfig.columnOrder
      : [];

    const columnMap = columns.reduce(
      (keyColumnMapping, column) => {
        keyColumnMapping[column.accessorKey as string] = column;

        return keyColumnMapping;
      },
      {} as Record<string, MRT_ColumnDef<WorkbookItem>>,
    );

    const orderedTableColumns = parsedColumnOrder
      .map((key: string) => columnMap[key])
      .filter((column) => !!column);

    const updatedColumns = orderedTableColumns.map((col) => ({
      ...col,
    }));

    return updatedColumns.length > 0 ? updatedColumns : columns;
  }, [columns]);

  const rowsGenerateOutput: WorkbookItem[] = useMemo(() => {
    if (activeRows.length === 1) {
      return activeRows;
    }

    const allVisibleRows: WorkbookItem[] = [...rowDataSelection];

    if (data) {
      data.forEach((row) => {
        const rowId = isSideBySide
          ? row.id || row.pairId
          : row.chat_id || row.id;

        if (rowId && expandedRowIds.has(rowId) && row.subRows) {
          row.subRows.forEach((subRow) => {
            allVisibleRows.push(subRow);
          });
        }
      });
    }

    const allRows = table
      .getRowModel()
      .flatRows.map((row: MRT_Row<WorkbookItem>) => row.original);
    const rowsToReturn = rowDataSelection.length > 0 ? allVisibleRows : allRows;

    return rowsToReturn.filter((row: WorkbookItem) => row.raw_input);
  }, [table, rowDataSelection, data, activeRows, expandedRowIds, isSideBySide]);

  const rowsClearResults: string[] = useMemo(() => {
    const finalChatTurnIds: string[] = [];
    if (activeRows.length === 1) {
      activeRows.forEach((row) => {
        if (row.chat_turn_id) {
          finalChatTurnIds.push(row.chat_turn_id);
        }
        if (row.chat_turn_b?.chat_turn_id) {
          finalChatTurnIds.push(row.chat_turn_b?.chat_turn_id);
        }
      });

      return finalChatTurnIds;
    }

    rowDataSelection.forEach((row: WorkbookItem) => {
      if (row.chat_turn_id) {
        finalChatTurnIds.push(row.chat_turn_id);
      }
      if (row.chat_turn_b?.chat_turn_id) {
        finalChatTurnIds.push(row.chat_turn_b?.chat_turn_id);
      }
    });

    return finalChatTurnIds;
  }, [table, rowDataSelection, data, activeRows]);

  const isThereChatWithoutModel = useMemo(() => {
    const chatsWithoutModel = rowsGenerateOutput.filter((row) => !row.model_id);

    return chatsWithoutModel.length > 0;
  }, [rowsGenerateOutput]);

  const isThereChatWithModel = useMemo(() => {
    if (isSideBySide) {
      return rowsGenerateOutput.some(
        (row) => row?.model_id || row?.chat_turn_b?.model_id,
      );
    }

    return rowsGenerateOutput.some((row) => row?.model_id);
  }, [rowsGenerateOutput, isSideBySide]);

  useEffect(() => {
    if (data && data.length > 0) {
      const allEvaluationKeys: string[] = [];
      data.forEach((item) => {
        Object.keys(getEvaluationObject(item)).map((evalKey) => {
          if (!allEvaluationKeys.includes(evalKey)) {
            allEvaluationKeys.push(evalKey);
          }
        });
      });

      const evaluationColumns: MRT_ColumnDef<WorkbookItem>[] =
        allEvaluationKeys.map((key) => ({
          accessorKey: `evaluation_${key}`,
          id: `evaluation_${key}`,
          enableEditing: false,
          enableColumnFilter: true,
          enableSorting: true,
          size: 150,
          Header: ({ column }) => <ColumnHeader column={column} />,
          sortingFn: getEvaluationSortingFn(key, projectType),
          filterFn: getEvaluationFilterFn(key, projectType),
          header: key
            .replace(/([A-Z])/g, " $1")
            .trim()
            .replace(/^[a-z]/, (match) => match.toUpperCase()),
          Cell: ({ row }) => {
            if (isSideBySide) {
              let sxsData = null;
              if (row.original.sxs_evaluations?.[key]) {
                sxsData = {
                  ...row.original.sxs_evaluations[key],
                };
                delete sxsData.category;
              }

              return (
                <SideBySideEvaluatorColumn
                  statusA={
                    row.original.point_evaluations?.[key] ? (
                      <EvaluationStatusElement
                        data={
                          row.original.point_evaluations?.[key]?.chatTurnA ||
                          ({} as LLMEvaluation)
                        }
                        onRerun={() => {
                          const chatTurnId =
                            row.original.chat_turn_a?.chat_turn_id || "";
                          if (chatTurnId) {
                            handleSXSEvaluationRerun(
                              chatTurnId,
                              row.original.point_evaluations?.[key]?.chatTurnA
                                ?.evaluator_id || "",
                              key,
                              "A",
                            );
                          }
                        }}
                        onStatusClick={() => {
                          setEvaluationStatusData(
                            row.original.point_evaluations?.[key]?.chatTurnA ||
                              ({} as LLMEvaluation),
                          );
                          openEvaluationStatusModal();
                        }}
                      />
                    ) : null
                  }
                  statusB={
                    row.original.point_evaluations?.[key] ? (
                      <EvaluationStatusElement
                        data={
                          row.original.point_evaluations?.[key]?.chatTurnB ||
                          ({} as LLMEvaluation)
                        }
                        onRerun={() => {
                          const chatTurnId =
                            row.original.chat_turn_b?.chat_turn_id || "";
                          if (chatTurnId) {
                            handleSXSEvaluationRerun(
                              chatTurnId,
                              row.original.point_evaluations?.[key]?.chatTurnB
                                ?.evaluator_id || "",
                              key,
                              "B",
                            );
                          }
                        }}
                        onStatusClick={() => {
                          setEvaluationStatusData(
                            row.original.point_evaluations?.[key]?.chatTurnB ||
                              ({} as LLMEvaluation),
                          );
                          openEvaluationStatusModal();
                        }}
                      />
                    ) : null
                  }
                  sxs={
                    sxsData ? (
                      <EvaluationStatusElement
                        data={sxsData || ({} as LLMEvaluation)}
                        onRerun={() => {
                          const chatTurnId =
                            row.original.chat_turn_a?.chat_turn_id || "";
                          if (chatTurnId) {
                            handleSXSEvaluationRerun(
                              chatTurnId,
                              sxsData?.evaluator_id || "",
                              key,
                              "A",
                            );
                          }
                        }}
                        onStatusClick={() => {
                          setEvaluationStatusData(
                            sxsData || ({} as LLMEvaluation),
                          );
                          openEvaluationStatusModal();
                        }}
                      />
                    ) : null
                  }
                />
              );
            }

            return (
              <EvaluationStatusElement
                data={
                  row.original.llm_evaluations?.[key] || ({} as LLMEvaluation)
                }
                onRerun={() => {
                  handleEvaluationRerun(
                    row.original.chat_turn_id,
                    row.original.llm_evaluations?.[key]?.evaluator_id || "",
                    key,
                  );
                }}
                onStatusClick={() => {
                  setEvaluationStatusData(
                    row.original.llm_evaluations?.[key] ||
                      ({} as LLMEvaluation),
                  );
                  openEvaluationStatusModal();
                }}
              />
            );
          },
        }));

      const humanEvaluationNotesColumnIndex = finalWorkbookColumns.findIndex(
        (col) =>
          col.id ===
          (projectType === ProjectType.SIDE_BY_SIDE
            ? "side_by_side_eval_notes"
            : "human_evaluation_notes"),
      );

      setColumns([
        ...finalWorkbookColumns.slice(0, humanEvaluationNotesColumnIndex + 1),
        ...evaluationColumns,
        ...finalWorkbookColumns.slice(humanEvaluationNotesColumnIndex + 1),
      ]);

      setTimeout(() => {
        const currentColumnOrder = [...table.getState().columnOrder];
        const currentColumnVisibility: Record<string, boolean> = {
          ...table.getState().columnVisibility,
          "mrt-row-expand": false,
        };

        evaluationColumns.forEach((col) => {
          const colId = col.id as string;
          currentColumnVisibility[colId] = true;
        });

        const backendHiddenColumns = props?.data?.empty_columns || [];
        try {
          const projectTableConfig: any =
            LocalStorage.get("projectTableConfig");
          const projectTableConfigData = JSON.parse(projectTableConfig);
          const projectTableConfigVisibility =
            projectTableConfigData.columnVisibility;

          const finalBackendHiddenColumns: Record<string, boolean> = {};
          backendHiddenColumns.forEach((key) => {
            if (!projectTableConfigVisibility?.[key]) {
              finalBackendHiddenColumns[key] = false;
            }
          });

          table.setColumnVisibility({
            ...projectTableConfigVisibility,
            ...finalBackendHiddenColumns,
          });
        } catch {
          table.setColumnVisibility({
            ...currentColumnVisibility,
            ...backendHiddenColumns.reduce(
              (acc, key) => {
                acc[key] = false;

                return acc;
              },
              {} as Record<string, boolean>,
            ),
          });
        }

        const tagsIndex = currentColumnOrder.findIndex((id) => id === "tags");

        if (tagsIndex !== -1) {
          const mrtRowSelectIndex =
            currentColumnOrder.indexOf("mrt-row-select");

          if (mrtRowSelectIndex !== -1) {
            currentColumnOrder.splice(mrtRowSelectIndex, 1);
          }

          currentColumnOrder.unshift("mrt-row-select");

          const mrtRowActionsIndex =
            currentColumnOrder.indexOf("mrt-row-actions");
          if (mrtRowActionsIndex !== -1) {
            currentColumnOrder.splice(mrtRowActionsIndex, 1);
            currentColumnOrder.push("mrt-row-actions");
          }
        }
      }, 300);
    } else {
      setColumns(finalWorkbookColumns);
    }
  }, [data, projectType, finalWorkbookColumns]);

  const reorderEvaluationColumns = (columnOrder: string[]): string[] => {
    const humanEvalIndex = columnOrder.indexOf("human_evaluation_notes");
    if (humanEvalIndex === -1) return columnOrder;

    const evaluationKeys = columnOrder.filter((key) =>
      key.startsWith("evaluation_"),
    );
    const otherKeys = columnOrder.filter(
      (key) => !key.startsWith("evaluation_"),
    );

    const before = otherKeys.slice(0, humanEvalIndex + 1);
    const after = otherKeys.slice(humanEvalIndex + 1);

    return [...before, ...evaluationKeys, ...after];
  };

  const reorderSxSColumns = (columnOrder: string[]): string[] => {
    const expectedOutputIndex = columnOrder.indexOf("expected_output");
    if (expectedOutputIndex === -1) return columnOrder;

    const sideBySideKeys = columnOrder.filter((key) =>
      key.startsWith("side_by_side_"),
    );
    const evaluatorKeys = columnOrder.filter((key) =>
      key.startsWith("evaluation_"),
    );

    const otherKeys = columnOrder.filter(
      (key) =>
        !key.startsWith("side_by_side_") && !key.startsWith("evaluation_"),
    );

    const before = otherKeys.slice(0, expectedOutputIndex + 1);
    const after = otherKeys.slice(expectedOutputIndex + 1);

    return [...before, ...sideBySideKeys, ...evaluatorKeys, ...after];
  };

  const allColumns = table.getAllColumns();
  const visibleColumns = table.getVisibleFlatColumns();
  const columnOrder = table.getState().columnOrder;
  const tableHeaders = table.getFlatHeaders();

  const allSelectedRowsCount = Object.keys(
    table.getState().rowSelection,
  ).length;

  useEffect(() => {
    if (
      allSelectedRowsCount > 0 &&
      allSelectedRowsCount === selectionBanner.totalInProject
    ) {
      selectionBanner.setSelectAllRowsInProject(true);
    }
  }, [allSelectedRowsCount, selectionBanner]);

  useEffect(() => {
    const getColumnVisibility = () => {
      const columnVisibility = { ...defaultHiddenColumns };
      try {
        const projectTableConfig = LocalStorage.get("projectTableConfig");
        if (!projectTableConfig) {
          return columnVisibility;
        }

        const projectTableConfigParsed = JSON.parse(projectTableConfig);

        return {
          ...columnVisibility,
          ...projectTableConfigParsed.columnVisibility,
        };
      } catch {
        return columnVisibility;
      }
    };

    const getColumnSizes = () => {
      const columnSizes: Record<string, number> = {};

      allColumns.forEach((column: MRT_Column<WorkbookItem>) => {
        if (column.id !== "mrt-row-select" && column.id !== "mrt-row-actions") {
          columnSizes[column.id] = column.getSize();
        }
      });

      return columnSizes;
    };

    const getColumnOrder = () => {
      const filteredOrder = columnOrder.filter(
        (columnId: string) =>
          !["mrt-row-select", "mrt-row-actions"].includes(columnId),
      );

      return isSideBySide
        ? reorderSxSColumns(filteredOrder)
        : reorderEvaluationColumns(filteredOrder);
    };

    const timeoutId = setTimeout(() => {
      const columnVisibility = getColumnVisibility();
      const columnSizes = getColumnSizes();
      const columnOrder = getColumnOrder();

      table.setColumnOrder(columnOrder);
      LocalStorage.set(
        "projectTableConfig",
        JSON.stringify({
          columnVisibility,
          columnSizes,
          columnOrder,
        }),
      );
    }, 1000);

    return () => clearTimeout(timeoutId);
  }, [visibleColumns, allColumns, columnOrder, tableHeaders, projectType]);

  const isSubrowInData = useCallback(
    (id: string): boolean => {
      if (!data) return false;

      for (const row of data) {
        if (row.subRows) {
          for (const subRow of row.subRows) {
            if (
              subRow.id === id ||
              subRow.pairId === id ||
              (subRow.chat_turn_a && subRow.chat_turn_a.chat_turn_id === id) ||
              (subRow.chat_turn_b && subRow.chat_turn_b.chat_turn_id === id)
            ) {
              return true;
            }
          }
        }
      }

      return false;
    },
    [data],
  );

  const selectedInputsAndSequences = useCallback(
    (turnIds: string[]) => {
      const selectedInputs: string[] = [];
      const selectedSequences: number[] = [];

      if (data && isSideBySide) {
        data.forEach((row) => {
          if (row.id && turnIds.includes(row.id) && row.input) {
            selectedInputs.push(row.input);
          }

          if (row.pairId && turnIds.includes(row.pairId) && row.input) {
            selectedInputs.push(row.input);
          }

          if (
            row.chat_turn_a?.chat_turn_id &&
            turnIds.includes(row.chat_turn_a.chat_turn_id) &&
            row.input
          ) {
            selectedInputs.push(row.input);

            if (row.chat_turn_a.sequence !== undefined) {
              selectedSequences.push(row.chat_turn_a.sequence);
            }
          }

          if (
            row.chat_turn_b?.chat_turn_id &&
            turnIds.includes(row.chat_turn_b.chat_turn_id) &&
            row.input
          ) {
            selectedInputs.push(row.input);

            if (row.chat_turn_b.sequence !== undefined) {
              selectedSequences.push(row.chat_turn_b.sequence);
            }
          }

          if (row.subRows) {
            row.subRows.forEach((subRow) => {
              if (subRow.id && turnIds.includes(subRow.id) && subRow.input) {
                selectedInputs.push(subRow.input);
              }

              if (
                subRow.pairId &&
                turnIds.includes(subRow.pairId) &&
                subRow.input
              ) {
                selectedInputs.push(subRow.input);
              }

              if (
                subRow.chat_turn_a?.chat_turn_id &&
                turnIds.includes(subRow.chat_turn_a.chat_turn_id) &&
                subRow.input
              ) {
                selectedInputs.push(subRow.input);

                if (subRow.chat_turn_a.sequence !== undefined) {
                  selectedSequences.push(subRow.chat_turn_a.sequence);
                }
              }

              if (
                subRow.chat_turn_b?.chat_turn_id &&
                turnIds.includes(subRow.chat_turn_b.chat_turn_id) &&
                subRow.input
              ) {
                selectedInputs.push(subRow.input);

                if (subRow.chat_turn_b.sequence !== undefined) {
                  selectedSequences.push(subRow.chat_turn_b.sequence);
                }
              }
            });
          }
        });
      }

      return {
        selectedInputs,
        selectedSequences,
      };
    },
    [data, isSideBySide],
  );

  return (
    <Box
      className={`rounded-xl border border-solid border-neutrals-300 bg-neutrals-50 p-[24px] md:h-[95%] 2xl:h-[98%] ${props?.customClassName}`}
    >
      <ManageTagsModal
        isOpened={isManageTagsModalOpened}
        onClose={(addedTags, removedTagIds) => {
          closeManageTagsModal();

          if (addedTags || removedTagIds) {
            const parentChatIdsToRefresh = new Set<string>();

            const chatIds = activeRows.map((r) => r.chat_id);
            if (chatIds) {
              setData((currentData) => {
                if (!currentData) return currentData;

                return currentData.map((row) => {
                  const finalTags = row.tags?.filter(
                    (tag) => !removedTagIds?.includes(tag.id || ""),
                  );

                  if (chatIds.includes(row.chat_id)) {
                    return {
                      ...row,
                      tags: [...(finalTags || []), ...(addedTags || [])],
                    };
                  }

                  return row;
                });
              });
            }

            activeRows.forEach((row) => {
              if (!row.subRows && row.chat_id) {
                const isExpandedRow = Array.from(expandedRowIds).some(
                  (expandedId) => expandedId === row.chat_id,
                );

                if (isExpandedRow) {
                  parentChatIdsToRefresh.add(row.chat_id);
                }
              }
            });

            Array.from(parentChatIdsToRefresh).forEach((chatId) => {
              getChatWorkbookHistory(chatId).then((response) => {
                setData((currentData) => {
                  if (!currentData) return currentData;

                  return currentData.map((row) => {
                    if (row.chat_id === chatId) {
                      const updatedSubRows = response.map((newSubRow) => ({
                        ...newSubRow,
                      }));

                      return {
                        ...row,
                        subRows: updatedSubRows,
                      };
                    }

                    return row;
                  });
                });
              });
            });
          }
        }}
        entityType={TagLinkEntityType.CHAT}
        entityIds={activeRows.map((r) => r.chat_id)}
        type={manageTagsType}
        selectAllRowsInProject={selectionBanner.selectAllRowsInProject}
        projectId={props.projectId}
        onClearBannerState={selectionBanner.handleClearSelection}
      />
      <EvaluationStatusModal
        data={evaluationStatusData || ({} as LLMEvaluation)}
        isOpened={isEvaluationStatusModalOpened}
        onClose={closeEvaluationStatusModal}
      />

      <WorkbookDeleteModal
        isOpened={isWorkbookDeleteModalOpen}
        onClose={closeWorkbookDeleteModal}
        selectedRows={rowDataSelection}
        setRowSelection={setRowSelection}
        initialColumnOrder={initialColumnOrder}
        initialColumnVisibility={initialColumnVisibility}
        onRowDelete={props.onRowDelete}
        projectId={props.projectId}
        selectAllRowsInProject={selectionBanner.selectAllRowsInProject}
        onBeforeDelete={handleBeforeDelete}
        onClearBannerState={selectionBanner.handleClearSelection}
        projectType={projectType}
        setAddedRowsCount={setAddedRowsCount}
      />
      <AddRunsToProjectModal
        isOpened={isAddToProjectOpened}
        onCreateNewProjectClick={openCreateEntity}
        selectedProject={getSelectedEntityProps().selectedEntity}
        setSelectedProject={getSelectedEntityProps().setSelectedEntity}
        selectedWorkbookChatIds={rowDataSelection.map(
          (row: WorkbookItem) => row.chat_id,
        )}
        onClose={closeAddToProject}
        sourceProjectId={props.projectId}
        selectAllRowsInProject={selectionBanner.selectAllRowsInProject}
        onClearBannerState={selectionBanner.handleClearSelection}
        transferToDataset={transferToDataset}
      />
      <CreateNewProjectModal
        isOpened={isCreateNewProjectOpened}
        onClose={closeCreateNewProjectModal}
        setSelectedProject={getSelectedEntityProps().setSelectedEntity}
        onBackClick={onBackClick}
        isDataset={transferToDataset}
      />

      <ScoreEvaluationModal
        isOpened={isEvaluationModalOpened}
        setRowSelection={setRowSelection}
        onClose={closeEvaluationModal}
        selectAllRowsInProject={selectionBanner.selectAllRowsInProject}
        onClearBannerState={selectionBanner.handleClearSelection}
        onRequestComplete={() => {
          projectActions.setHasRemainingJobs(true);

          const subRowIds = Array.from(evaluatingRowsRef.current.keys()).filter(
            (id) => {
              return isSubrow(id) === true;
            },
          );

          if (subRowIds.length > 0) {
            startPolling(subRowIds);
          }
        }}
        onEvaluationStarted={(turnIds: string[]) => {
          const now = Date.now();

          turnIds.forEach((id) => {
            const trackedTypes = new Set<string>(["initial"]);
            evaluatingRowsRef.current.set(id, trackedTypes);
            evaluationStartTimesRef.current.set(id, now);
            pendingEvaluationTurnsRef.current.add(id);
            initialEvaluationStatesRef.current.set(id, {});
          });

          if (isSideBySide) {
            const subrowTurnIds = turnIds.filter((id) => isSubrowInData(id));

            if (subrowTurnIds.length > 0) {
              const selectedData = selectedInputsAndSequences(turnIds);

              startPolling(subrowTurnIds, false, {
                inputs: selectedData.selectedInputs,
                sequences: selectedData.selectedSequences,
              });

              return;
            }
          } else {
            startPolling(turnIds);
          }
        }}
        source={EvaluatorModalSource.WORKBOOK}
        projectId={props.projectId}
      />

      <GenerateOutputsModal
        isOpened={isGenerateOutputsModalOpened}
        onGenerateOutputsStart={() => {
          const chatTurnIds = rowsGenerateOutput.map((row) => row.chat_turn_id);
          processingRowsRef.current = new Set(chatTurnIds);
          isRefreshingAfterGenerationRef.current = true;

          notifications.show(
            getSuccessNotificationConfig(
              "Generating model outputs.",
              "generating-outputs",
            ),
          );
          pendingNewRowsRef.current = new Set();
        }}
        onGenerateOutputsSuccess={() => {
          props.refetchProject();
          projectActions.setHasRemainingJobs(true);
        }}
        setRowSelection={setRowSelection}
        setActiveRows={setActiveRows}
        chatTurnIds={rowsGenerateOutput.map((row) => row.chat_turn_id)}
        sxsPairIds={
          isSideBySide
            ? rowsGenerateOutput
                .filter((row) => !(row as any).isSubRow)
                .map((row) => row.pairId)
                .filter((id): id is string => id !== undefined)
            : []
        }
        isThereChatWithoutModel={isThereChatWithoutModel}
        onClose={closeGenerateOutputsModal}
        projectId={props.projectId}
        selectAllRowsInProject={selectionBanner.selectAllRowsInProject}
        onClearBannerState={selectionBanner.handleClearSelection}
        type={generateOutputsType}
        isThereChatWithModel={isThereChatWithModel}
      />

      <ClearResultsModal
        isOpened={isClearResultsModalOpened}
        refetchProject={props.refetchProject}
        setRowSelection={setRowSelection}
        setActiveRows={setActiveRows}
        chatTurnIds={rowsClearResults}
        onClose={closeClearResultsModal}
        selectAllRowsInProject={selectionBanner.selectAllRowsInProject}
        projectId={props.projectId}
      />

      <MantineReactTable table={table} />
    </Box>
  );
}
