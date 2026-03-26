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

import { Dataset, DatasetRow } from "@/app/(authRoutes)/datasets/types";
import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import { Model } from "@/queries/types";
import { ComboboxItem } from "@mantine/core";
import { MRT_Row } from "mantine-react-table";
import { SVGProps } from "react";

export * from "./utils";

export const PROMPT_ROLES = ["system", "user", "assistant", "tool"] as const;
export type PromptRole = (typeof PROMPT_ROLES)[number];

export type PromptMessage = {
  role: PromptRole;
  content: string;
};

export type ModelParameterRaw = {
  frequency_penalty: number;
  logit_bias: null;
  logprobs: boolean;
  top_logprobs: null;
  max_tokens: number;
  n: number;
  presence_penalty: number;
  temperature: number;
  top_p: number;
  seed: number;
};

export type IconSvgProps = SVGProps<SVGSVGElement> & {
  size?: number;
};

export type UserKeysAPIResponse = {
  openaiKeyPresent: boolean;
  mistralKeyPresent: boolean;
  googleKeyPresent: boolean;
  anthropicKeyPresent: boolean;
  grokKeyPresent: boolean;
  deepseekKeyPresent: boolean;
  llamaKeyPresent: boolean;
};

export type PlanckKeysAPIResponse = {
  count: number;
  references: string[];
};

export enum ParameterTypeEnum {
  NUMBER,
  BOOLEAN,
}

export type ScorerCardsProps = {
  id: string;
  title: string;
};

export type LLMEvaluatorVariable = {
  name: string;
  required: boolean;
};

export type LLMEvaluatorOutputCategory = {
  color: string;
  color_name: string;
  end_range?: string;
  name: string;
  start_range?: string;
  value: string;
};

export enum EvaluatorType {
  USER = "USER",
  SYSTEM = "SYSTEM",
}

export enum EvaluatorCategory {
  LLM = "LLM",
  HEURISTIC = "Heuristic",
}

export type LLMEvaluatorItem = {
  id: string;
  name: string;
  description: string;
  model: Model;
  output_categories: LLMEvaluatorOutputCategory[];
  output_format_type: string;
  prompts: ChatCompletionModelInput[];
  type: EvaluatorType;
  evaluation_type: ProjectType;
  variables: LLMEvaluatorVariable[];
  created_at: string;
  updated_at: string;
};

export type User = {
  firstName: string;
  lastName: string;
  email: string;
};

export type Token = {
  token: string;
  status?: number;
  tosId?: string;
  tosContent?: string;
};

export type TokenMutationFn = {
  tokenData: Token;
  withRedirect: boolean;
};

export type UserWithToken = User & Token;

export type TagRaw = {
  id?: string;
  name: string;
  type?: "DATASET" | "MODEL" | "USER";
  color: string;
  created_at?: string;
  updated_at?: string;
  used_count?: number;
};

export type TagsAPIResponse = {
  user_tags: TagRaw[];
  model_tags: TagRaw[];
  dataset_tags: TagRaw[];
};

export type RunFeedbackPayload = {
  scorers: string[];
  value: -1 | 0 | 1 | null;
  response_ids: string[];
};

export type APIResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

export enum SourceTypeEnum {
  DATASET = "DATASET",
  PROJECT = "PROJECT",
}

export type TagColorSwatch = {
  color: string;
  bgColor: string;
};

export type JobStatus = {
  status: JobDetailStatus;
  job_id: string;
  type: string;
  start_time: string;
  end_time?: string;
  input_ids: any[];
  pending: number;
  in_progress: number;
  failed?: number;
  total: number;
};

export type Project = {
  project_id: string;
  name: string;
  type: ProjectType;
  description: string;
  created_at: string;
  updated_at: string;
  is_default_project: boolean;
  job_statuses: JobStatus[];
  providers: Provider[];
  inference_monitoring_summary: {
    average_turn_time_taken: number;
    total_completion_tokens: number;
    total_inferences: number;
    total_prompt_tokens: number;
    total_tokens: number;
  };
  finished_job_tasks: number;
  total_job_tasks: number;
  latest_eval_score?: {
    avg_score: number;
    name: string;
  };
  human_eval_metrics?: {
    passRate: number;
    scoreCounts: {
      [score: number]: number;
    };
  };
};

export interface ProjectsDto {
  projects: Project[];
}

export interface ProjectComboboxItem extends ComboboxItem {
  isNew?: boolean;
}

export enum InferenceChatCompletionPromptRole {
  USER = "USER",
  SYSTEM = "SYSTEM",
  ASSISTANT = "ASSISTANT",
}

export type InferenceChatCompletionPrompt = {
  role: InferenceChatCompletionPromptRole;
  text: string;
};

export type InferenceChatCompletionPayload = {
  model_id?: string;
  prompts?: InferenceChatCompletionPrompt[];
  previous_chat_turn_id?: string | null;
  variables?: {
    [key: string]: string;
  };
};

export type InferenceChatCompletionSxSPayload = {
  model_id_a?: string;
  model_id_b?: string;
  model_a_instruction?: string;
  model_b_instruction?: string;
  prompts: InferenceChatCompletionPrompt[];
  previous_chat_turn_id?: string | null;
  variables?: {
    [key: string]: string;
  };
};

export type ContinueChatBSxSQueryPayload = {
  model_id_a?: string | null;
  model_id_b?: string | null;
  prompts: InferenceChatCompletionPrompt[];
};

export type ChatCompletionSxSResponse = {
  id: string;
  project_id: string;
  chat_id_a: string;
  chat_turn_id_a: string;
  chat_id_b: string;
  chat_turn_id_b: string;
  chat_turn_a: ChatCompletionResponse;
  chat_turn_b: ChatCompletionResponse;
};

export type InferenceChatCompletionData = {
  payload: InferenceChatCompletionPayload;
  chat: Chat;
  groupId?: string;
};

export enum ChatMessageType {
  INPUT = "input",
  OUTPUT = "output",
}

export type ChatMessage = {
  id: string;
  role?: InferenceChatCompletionPromptRole;
  text: string;
  chat_turn_id?: string;
  model_id?: string;
  model?: Model;
  inference_monitoring?: ChatCompletionModelOutputInferenceMonitoring;
  created_at?: string;
  updated_at?: string;
  project_id?: string;
  isEditing?: boolean;
  type: ChatMessageType;
  voteDown?: boolean;
  evaluationResults?: EvaluationResults;
  groupId?: string;
  humanEvalScore?: HumanEvalScore | null;
};

export type ChatCompletionModelInput = {
  id: string;
  text: string;
  role: InferenceChatCompletionPromptRole;
  created_at: string;
  updated_at: string;
  raw_text?: string;
};
export type ChatCompletionModelOutputInferenceMonitoring = {
  id: string;
  latency: number;
  prompt_tokens: number;
  completion_tokens: number;
  total_tokens: number;
  avg_chat_latency: number;
  chat_total_tokens: number;
  turn_latency: number;
  turn_total_tokens: number;
};

export type ChatCompletionModelOutput = {
  id: string;
  text: string;
  chat_turn_id: string;
  model_id: string;
  created_at: string;
  updated_at: string;
  project_id: string;
  inference_monitoring: ChatCompletionModelOutputInferenceMonitoring;
  human_eval_scores?: HumanEvalScore[];
};

export type ChatCompletionResponse = {
  model_inputs: ChatCompletionModelInput[];
  model_output: ChatCompletionModelOutput;
  model: Model;
  output: string;
  chat_turn_id: string;
  created_at: string;
  updated_at: string;
  chat_id: string;
  sequence_id: number;
  inference_status?: number;
  inference_reason?: string;
  human_eval_scores?: HumanEvalScore[];
  llm_evaluations?: LLMEvaluations;
  inference_tokens?: {
    input_tokens?: number;
    output_tokens?: number;
    total_tokens?: number;
    thinking_tokens?: number;
  };
  inference_latency?: number;
};

export type ChatHistory = {
  chat_turns: ChatCompletionResponse[];
  variables?: {
    [key: string]: string;
  };
  tags?: TagRaw[];
  chat_id?: string;
};

export type ChatTurnByIdResponse = {
  readonly chat_id: string;
  readonly chat_turn_id: string;
  readonly model: Model | null;
  readonly model_inputs: ChatCompletionModelInput[];
  readonly model_output?: ChatCompletionModelOutput;
  readonly human_eval_scores?: HumanEvalScore[];
  readonly tagLinkTargetType: string;
  readonly created_at: string;
  readonly updated_at: string;
  readonly sequence_id: number;
  readonly evaluation_scores?: any;
  readonly inference_status?: number;
  readonly tags?: TagRaw[];
  readonly llm_evaluations?: LLMEvaluations;
};

export type Chat = {
  id: string;
  model: Model | null;
  messages: ChatMessage[];
  isLoading?: boolean;
  instructions?: string;
  lastInstructionsSent?: string;
  showInstructions?: boolean;
};

export type EvaluationProjectPayload = {
  evaluator_ids: string[];
};

export type EvaluationChatTurnPair = {
  id: string;
  chat_turn_id_a: string;
  chat_turn_id_b: string;
};

export type EvaluationChatTurnsPayload = {
  chat_turn_ids?: string[] | undefined;
  evaluator_ids: string[];
  pair_evaluations?: EvaluationChatTurnPair[];
};

export type EvaluationChatTurnPayload = {
  chat_turn_id?: string | undefined;
  evaluator_id: string;
  sxs_pair?: EvaluationChatTurnPair;
};

export type GenerateOutputsPayload = {
  chat_turn_ids: string[];
  model_ids: string[];
};

export type GenerateOutputsSxSPayload = {
  sxsPairIds: string[];
  mode: GenerateOutputsSxSMode;
  modelA?: string | null;
  modelB?: string | null;
};

export enum GenerateOutputsSxSMode {
  MISSING_ONLY = "MISSING_ONLY",
  MISSING_AND_MATCHING = "MISSING_AND_MATCHING",
  RUN_OR_RERUN = "RUN_OR_RERUN",
  DUPLICATE = "DUPLICATE",
  AUTORESOLVE = "AUTORESOLVE",
}

export type GenerateOutputsSxSAllPayload = {
  mode: GenerateOutputsSxSMode;
  modelA?: string | null;
  modelB?: string | null;
};

export type GenerateOutputsSxSResponse = {
  newSxsPairIds: string[];
  failedSxsPairIds: string[];
  skippedSxsPairIds: string[];
};

export type ClearResultsPayload = {
  chat_turn_ids: string[];
};

export type EvaluationResults = {
  inProgress?: boolean;
  data: EvaluationResult[];
};

export type EvaluationResult = {
  name: string;
  value: string | null;
};

export type EvaluationChatTurnsResponse = {
  job_id: string;
  chat_turn_ids: string[];
};
export enum EvaluatorTab {
  DEFAULT = "default",
  MY_EVALUATORS = "myEvaluators",
}

export enum JobDetailStatus {
  PENDING = "Pending",
  SUCCESSFUL = "Successful",
  IN_PROGRESS = "In-Progress",
  FAILED = "Failed",
  COMPLETED = "Completed",
}

export type JobDetail = {
  id: string;
  status: JobDetailStatus;
  start_time: string;
  end_time: string;
  evaluator_id: string;
  results?: EvaluationResult;
};

export type JobStatusByIdResponse = {
  aggregate: {
    in_progress: number;
    failed: number;
    total: number;
    successful: number;
  };
  jobs_details: JobDetail[];
};

export enum EvaluationScoreStatus {
  PENDING = "0",
  IN_PROGRESS = "2",
  SUCCESSFUL = "3",
  FAILED = "-1",
  STOPPED = "4",
}

export type TagLink = {
  id: string;
  tag_id: string;
  target_entity: TagLinkEntityType;
  target_entity_id: string;
  created_at: string;
  updated_at: string;
};

export interface Datasets {
  system_data_sets: Dataset[];
  user_data_sets: Dataset[];
  community_data_sets: Dataset[];
}

export enum TagLinkEntityType {
  CHAT_TURN = "CHAT_TURN",
  CHAT = "CHAT",
  INFERENCE_MONITORING = "INFERENCE_MONITORING",
  EVALUATION_MONITORING = "EVALUATION_MONITORING",
}

export type TagLinkPayload = {
  entityType: TagLinkEntityType;
  entityId: string;
  tagId: string;
};

export type TagLinksPayload = {
  tag_ids?: string[];
  entity_type: TagLinkEntityType;
  entity_ids?: string[];
};

export enum GAevents {
  ADDS_API_KEY = "adds_api_key",
  NEW_PROJECT = "new_project",
  ADD_DATA = "add_data",
  PROJECT_EVAL = "project_eval",
  CREATE_CUSTOM_LLM_EVALUATOR = "create_custom_llm_evaluator",
  SAVE_CUSTOM_LLM_EVALUATOR = "save_custom_llm_evaluator",
  CREATE_PROJECT = "create_project",
  ADD_HUMAN_RATING = "add_human_rating",
  GENERATE_OUTPUT = "generate_output",
  SELECT_MODEL = "select_model",
}

export enum EvaluatorModalSource {
  WORKBOOK = "workbook",
}

export type DatasetUploadFormData = {
  file: File | null;
  name: string;
  description: string;

  inputColumn: string | null;
  outputColumn: string | null;
  expectedOutput: string | null;
  tagsColumn: string | null;
  systemInstructionColumn: string | null;
  modelLabelColumn: string | null;
  variablesColumn: string | null;
  humanEvalScoreColumn: string | null;
  humanEvalScoreNotesColumn: string | null;
  chatColumn: string | null;
  llmEvaluationsColumn: string | null;
  inferenceAnalyticsColumn: string | null;

  chatAColumn: string | null;
  chatBColumn: string | null;

  outputColumnA: string | null;
  outputColumnB: string | null;
  systemInstructionColumnA: string | null;
  systemInstructionColumnB: string | null;
  modelLabelColumnA: string | null;
  modelLabelColumnB: string | null;
  llmEvaluationsColumnA: string | null;
  llmEvaluationsColumnB: string | null;
  inferenceAnalyticsColumnA: string | null;
  inferenceAnalyticsColumnB: string | null;
};

export enum ManageTagsModalType {
  ADD = "add",
  REMOVE = "remove",
}

export enum ModelSettingsModalType {
  ADD = "add",
  EDIT = "edit",
  DUPLICATE = "duplicate",
}

export enum CustomModelModalType {
  ADD = "add",
  EDIT = "edit",
  SHOW = "show",
}

export type BulkActionMenuItem = {
  label: string;
  icon: string;
  onClick: () => void;
};

export type HumanEvaluatorCategory = {
  id: string;
  score: number;
  description: string;
  categoryName: string;
};

export enum ScoringMechanismType {
  CATEGORY = "CATEGORY",
  RANGE = "RANGE",
}

export type HumanEvaluator = {
  id: string;
  name: string;
  description: string;
  sortingMechanismType: ScoringMechanismType;
  associatedEntityId: string;
  entityType: string;
  categories: HumanEvaluatorCategory[];
  linkedEntityType: string;
};

export type HumanEvaluatorFeedback = {
  id: string;
  created_at: string;
  updated_at: string;
  score: number;
  score_type: "Json";
  scorer: string;
  llm_response: string;
  llm_output_response_dto: string;
  source_id: string;
  eval_monitoring_id: string;
  eval_monitoring: {
    tagLinkTargetType: string;
    id: string;
    eval_model_id: string;
    project_id: string;
    evaluator_name: string;
    evaluator_id: string;
    time_taken: number;
    prompt_tokens: number;
    completion_tokens: number;
    total_tokens: number;
    created_at: string;
    updated_at: string;
    tags: TagRaw[];
  };
};

export type HumanEvalScore = {
  category_name: string;
  chat_turn_id: string;
  created_at: string;
  human_evaluator_id: string;
  id: string;
  score: number;
  updated_at: string;
  user_id: string;
  notes?: string;
};

export type LLMEvaluation = {
  category?: string;
  evaluationStatus: EvaluationScoreStatus;
  evaluationStatusText?: string;
  llmResponse?: string;
  reasoning?: string;
  score?: string;
  evaluator_id?: string;
  color?: string;
};

export type LLMEvaluations = {
  [key: string]: LLMEvaluation;
};

export enum Provider {
  OPENAI = "OPENAI",
  MISTRAL = "MISTRAL",
  GOOGLE = "GOOGLE",
  ANTHROPIC = "ANTHROPIC",
  GROK = "GROK",
  DEEPSEEK = "DEEPSEEK",
  LLAMA = "LLAMA",
  OLLAMA = "OLLAMA",
  HUGGINGFACE = "HUGGINGFACE",
}

export enum EvaluationStatusColor {
  RED = "var(--color-red)",
  ORANGE = "var(--color-orange)",
  LIME = "var(--color-lime)",
  GREEN = "var(--color-green)",
  BLUE = "var(--color-blue)",
  PURPLE = "var(--color-purple)",
  PINK = "var(--color-pink)",
  GREY = "var(--color-grey)",
  VIOLET = "var(--color-violet)",
}

export enum TableName {
  DATASET = "dataset",
  PROJECT = "project",
}

export enum ProjectType {
  POINTWISE = "POINTWISE",
  SIDE_BY_SIDE = "SXS",
}

export type HumanEvalPassRateResponse = {
  passRate: number | null;
  scoreCounts: {
    [score: number]: number;
  };
};

export type SxsInferenceMetricsResponse = {
  sideA: MetricsSummaryResponse;
  sideB: MetricsSummaryResponse;
  delta: MetricsSummaryResponse;
};

export type RatingCounts = {
  A_IS_BETTER: number;
  B_IS_BETTER: number;
  BOTH_ARE_BAD?: number;
  BOTH_ARE_GOOD?: number;
};

export interface SxsHumanEvalPassRateData extends HumanEvalPassRateData {
  ratingCounts: RatingCounts;
  total: number;
}

export type HumanEvalPassRateData = {
  passRate: number | null;
  likes: number;
  dislikes: number;
};

export type MetricsSummaryResponse = {
  total_inferences: number;
  average_turn_time_taken: number;
  total_prompt_tokens: number;
  total_completion_tokens: number;
  total_tokens: number;
};

export enum UploadDatasetModalSource {
  DATASET = "dataset",
  PROJECT = "project",
}

type ScoreCountEvalAnalytics = {
  category: string;
  score: string;
  count: number;
  color: string;
};

export interface EvalAnalyticsScoreBase {
  scorer_id: string;
  scorer_name: string;
}
export interface EvalAnalyticsScore extends EvalAnalyticsScoreBase {
  score_counts: ScoreCountEvalAnalytics[];
  average_score: number;
}

export interface EvalSxSAnalyticsScorePointwise extends EvalAnalyticsScoreBase {
  side_a: EvalAnalyticsScore;
  side_b: EvalAnalyticsScore | null;
  delta: number;
}

export interface EvalSxSAnalyticsScoreSxS extends EvalAnalyticsScoreBase {
  datapoints: ScoreCountEvalAnalytics[];
}

export enum GenerateOutputsType {
  SELECTED_ROWS = "selectedRows",
  ALL = "all",
}

export type HumanEvaluatorSxSData = {
  rating: string | null;
  notes: string | null;
};

export interface ModelProvider {
  key: string;
  accordionName: string;
  icon: any;
  name?: Provider | null;
}

export interface StreamingResponse {
  content: string;
  isComplete: boolean;
  error: string;
  usage: any;
  latency: number | null;
  model: string;
  finishReason: string;
}

export interface HeuristicEvaluator {
  id: string;
  name: string;
  criteria: string;
  criteria_type: string;
  deprecated: boolean;
  created_at: string;
  updated_at: string;
}

export interface HeuristicEvaluatorPayload {
  name: string;
  criteria: string;
  criteria_type: string;
}

export interface AllEvaluatorsResponse {
  llm: LLMEvaluatorItem[];
  heuristic: HeuristicEvaluator[];
}

export interface AllEvaluatorsQueryParams {
  evaluation_type?: ProjectType;
}

export interface EvaluatorCardItemType {
  id: string;
  type: ProjectType;
}

export interface EvaluatorCardItem extends LLMEvaluatorItem {
  evaluationTypes?: EvaluatorCardItemType[];
}
export interface WorkbookMeta {
  openTagsModal?: (rows: WorkbookItem[]) => void;
  loadingRows?: Set<string>;
  projectId?: string;
  refetchProject?: () => void;
  onInputChange?: (
    input: string,
    currentValue: string,
    rowOriginal: WorkbookItem,
  ) => void;
  onUpdateChatVariables?: (
    variables: {
      [key: string]: string;
    },
    currentVariables: {
      [key: string]: string;
    },
    rowOriginal: WorkbookItem,
  ) => void;
  onExpectedOutputChange?: (
    newValue: string,
    currentValue: string,
    rowOriginal: WorkbookItem,
  ) => void;
  onHumanEvalScoreChange?: (
    newValue: number,
    currentValue: number | undefined,
    rowOriginal: WorkbookItem,
  ) => void;
  onHumanEvalNotesChange?: (
    newValue: string,
    currentValue: string | undefined,
    rowOriginal: WorkbookItem,
  ) => void;
  onHumanEvalSxSNotesChange?: (
    newValue: string,
    currentValue: string | undefined,
    rowOriginal: WorkbookItem,
  ) => void;
  onHumanEvalSxSRatingChange?: (
    newValue: string,
    rowOriginal: WorkbookItem,
  ) => void;
  onSystemInstructionChange?: (
    newValue: string,
    currentValue: string,
    rowOriginal: WorkbookItem,
  ) => void;
  onTagRemove?: (tagId: string, rowOriginal: WorkbookItem) => void;
  onTagAdd?: (tag: TagRaw, rowOriginal: WorkbookItem) => void;
  onExpandClick?: (row: MRT_Row<WorkbookItem>) => void;
  onOutputRerun?: (rowOriginal: WorkbookItem) => void;
  projectType?: ProjectType;
  tableName?: TableName;
}

export interface DatasetTableMeta {
  openTagsModal?: (rows: DatasetRow[]) => void;
  onInputChange?: (
    input: string,
    currentValue: string,
    rowOriginal: DatasetRow,
  ) => void;
  onUpdateChatVariables?: (
    variables: {
      [key: string]: string;
    },
    currentVariables: {
      [key: string]: string;
    },
    rowOriginal: DatasetRow,
  ) => void;
  onExpectedOutputChange?: (
    newValue: string,
    currentValue: string,
    rowOriginal: DatasetRow,
  ) => void;
  tableName?: TableName;
}

export type EvalSxSAnalyticsScores = {
  pointwise: EvalSxSAnalyticsScorePointwise[];
  sideBySide: EvalSxSAnalyticsScoreSxS[];
};
