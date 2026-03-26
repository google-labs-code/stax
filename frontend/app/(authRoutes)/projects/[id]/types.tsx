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

import { HUMAN_SXS_RATING } from "@/queries/types";
import {
  EvaluationScoreStatus,
  GenerateOutputsType,
  HumanEvalScore,
  LLMEvaluation,
  LLMEvaluations,
  TagRaw,
} from "@/types";
import { QueryObserverResult, RefetchOptions } from "@tanstack/query-core";
import { UseMutationResult } from "@tanstack/react-query";
import { Row } from "@tanstack/react-table";
import { MRT_TableInstance } from "mantine-react-table";
import { Dispatch, MutableRefObject, SetStateAction } from "react";

import { Dataset } from "../../datasets/types";

export type Workbook = {
  workbook_rows?: WorkbookItem[];
  sxs_rows?: SXSRow[];
  total_size: number;
  next_page_token: number;
  empty_columns: string[];
};

export type SXSRow = {
  id: string;
  input: string;
  expected_output: string;
  human_sxs_rating: HUMAN_SXS_RATING;
  human_sxs_notes: string;
  chat_turn_a: WorkbookItem;
  chat_turn_b: WorkbookItem;
  point_evaluations: Record<string, PointEvaluation>;
  sxs_evaluations: Record<string, LLMEvaluation>;
};

export type PointEvaluation = {
  chatTurnA: LLMEvaluation;
  chatTurnB: LLMEvaluation;
  delta: string;
};
export type InferenceTokens = {
  input_tokens: number;
  output_tokens: number;
  total_tokens: number;
};

export type EvaluationScores = {
  [key: string]: number | null;
};

export type EvaluationStatuses = {
  [key: string]: EvaluationScoreStatus;
};

type VariablesValue = {
  [key: string]: string;
};

export enum InferenceStatus {
  PENDING = 0,
  IN_PROGRESS = 2,
  SUCCESSFUL = 3,
  FAILED = -1,
  STOPPED = 4,
}

export type WorkbookItem = {
  id?: string;
  input: string;
  output: string;
  expected_output: string | null;
  model_response_id: string;
  chat_id: string;
  chat_turn_id: string;
  created_at: number | null;
  updated_at: number;
  is_chat: boolean;
  human_evaluation: string;
  model_id: string;
  inference_latency: number;
  inference_tokens: InferenceTokens;
  inference_status: InferenceStatus;
  inference_reason?: string;
  model_name: string;
  model_label: string;
  model_provider: string;
  model_properties: string;
  subRows?: WorkbookItem[];
  pairId?: string;
  human_eval_scores: HumanEvalScore[] | null;
  llm_evaluations?: LLMEvaluations;
  isInputLoading?: boolean;
  isExpectedOutputLoading?: boolean;
  isHumanEvalNotesLoading?: boolean;
  isSystemInstructionsLoading?: boolean;
  variables: Record<string, VariablesValue>;
  tags?: TagRaw[];
  system_instructions?: string;
  human_sxs_notes?: string;
  human_sxs_rating?: HUMAN_SXS_RATING;
  chat_turn_b?: WorkbookItem;
  chat_turn_a: WorkbookItem;
  sequence: number;
  point_evaluations?: Record<string, PointEvaluation>;
  raw_input?: string;
  isAddRow?: boolean;
  isSubRow?: boolean;
  sxs_evaluations?: Record<string, LLMEvaluation>;
};

export interface WorkbookProps {
  data: Workbook | null | undefined;
  projectError: Error | null;
  projectId: string;
  refetchProject: (
    options?: RefetchOptions,
  ) => Promise<QueryObserverResult<Workbook | null, Error>>;
  onRowDelete: UseMutationResult<unknown, Error, string[], unknown>;
  onAddData: () => void;
  setPageSize: (size: number) => void;
  setPage: Dispatch<SetStateAction<number>>;
  page: number;
  pageSize: number;
  totalSize: number | undefined;
  customClassName?: string;
  hideTooltips: boolean;
  isCsvUploadRef?: MutableRefObject<boolean>;
  setRefreshHumanPassRate: React.Dispatch<React.SetStateAction<boolean>>;
}

export interface WorkbookHeaderProps extends WorkbookProps {
  table: MRT_TableInstance<WorkbookItem>;
  openWorkbookDeleteModal?: () => void;
  onEvaluate: () => void;
  onGenerateModels: (type: GenerateOutputsType) => void;
  onClearResults: () => void;
  onAddToProject: () => void;
  onAddToDataset: () => void;
  refresh?: () => void;
  onAddTags: () => void;
  onRemoveTags: () => void;
  selectionBanner?: {
    selectAllRowsInProject: boolean;
    allRowsSelected: boolean;
    totalInProject: number;
    currentPageSize: number;
    selectedCount: number;
    onSelectAll: () => void;
    onClearSelection: () => void;
    visible: boolean;
  };
  selectedRows: Row<WorkbookItem>[];
}

export type AddDatasetModalProps = {
  isOpened: boolean;
  onClose: () => void;
  onImport: () => void;
  onUploadCSV: () => void;
  importedDataset: Dataset | null;
  isCsvUploadRef?: MutableRefObject<boolean>;
};
