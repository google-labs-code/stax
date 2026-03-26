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

import { Dataset } from "@/app/(authRoutes)/datasets/types";
import {
  EvaluatorType,
  InferenceChatCompletionPromptRole,
  ModelParameterRaw,
  ProjectType,
  Provider,
  ScoringMechanismType,
} from "@/types";
import { SVGProps } from "react";

export type RunInferenceRawPayload = {
  candidate_models: CandidateModelRaw[];
  tag_names: string[];
  response: {
    id: string;
  };
};

export type CandidateModelRaw = {
  model_name: string;
  model_properties: ModelParameterRaw;
  model_version: string;
};

export type DescriptorsNumberType = {
  defaultValue: number;
  description: string;
  editable?: boolean;
  label: string;
  maxValue: number;
  minValue: number;
  type: string;
  comparableMaxValue: number;
  comparableMinValue: number;
  key: string;
};

export type ModelDescriptors = {
  max_output_tokens?: DescriptorsNumberType;
  temperature?: DescriptorsNumberType;
  top_p?: DescriptorsNumberType;
  top_k?: DescriptorsNumberType;
  seed?: DescriptorsNumberType;
};
export type ModelRaw = {
  properties: {
    max_tokens?: number;
    n?: number;
    temperature?: number;
    top_p?: number;
    top_k?: number;
    seed?: number;
  };
  id: string;
  name: string;
  version: string;
  label: string;
  url: string;
  tag: string;
  description?: string;
  pricing?: {
    input_token?: number;
    output_token?: number;
  };
  descriptors?: ModelDescriptors;
  is_api_key_present?: boolean;
  is_deprecated?: boolean;
  provider: Provider;
  model_type: string;
  api_key?: string;
  additional_headers: AdditionalHeadersData;
};

export interface Model extends ModelRaw {
  icon?: React.ComponentType<SVGProps<SVGSVGElement> & { size?: number }>;
  latency?: number;
}

export type DuplicateModelPayload = {
  new_label?: string;
  new_description?: string;
  new_descriptors?: {};
  new_properties: {
    temperature: number | undefined;
    max_tokens: number | undefined;
    top_p: number | undefined;
    seed: number | undefined;
  };
};

export type ModelFormValues = {
  id: string;
  label?: string;
  description?: string;
  name?: string;
  systemsInstruction?: string;
  presenceFrequency?: number;
  penaltyFrequency?: number;
  temperature?: number;
  maxTokens?: number;
  topP?: number;
  seed?: number;
};

export enum ModelTypeEnum {
  SYSTEM = "SYSTEM",
  USER = "USER",
  CUSTOM = "Custom",
}

export type LLMEvaluatorPrompt = { role: string; text: string };
export type LLMEvaluatorOutputCategory = {
  name: string;
  color: string;
  value: string;
  color_name: string;
};
export type LLMEvaluatorVariable = {
  name: string;
  required: boolean;
};

export type LLMEvaluatorPayload = {
  id?: string;
  name: string;
  description?: string;
  output_format_type?: "Json" | "Choices";
  prompts?: LLMEvaluatorPrompt[];
  model_id?: string;
  output_categories?: LLMEvaluatorOutputCategory[];
  variables?: LLMEvaluatorVariable[];
};
export type LLMEvaluatorResponse = {
  id: string;
  name: string;
  description: string;
  output_format_type: string;
  prompts: LLMEvaluatorPrompt[];
  model: Model;
  output_categories: LLMEvaluatorOutputCategory[];
  variables: LLMEvaluatorVariable[];
  type: EvaluatorType;
};

export type EvaluatorFormData = {
  selectedType: ProjectType;
  name: string;
  description: string;
  [ProjectType.POINTWISE]: EvaluatorFormDataItem;
  [ProjectType.SIDE_BY_SIDE]: EvaluatorFormDataItem;
};

export type EvaluatorFormDataItem = {
  id: string;
  model_id: string;
  output_categories: LLMEvaluatorOutputCategory[];
  variables: LLMEvaluatorVariable[];
  prompt: string;
  output_format_type: "Json" | "Choices";
};

export type DatasetPayload = {
  id?: string;
  name?: string;
  description?: string;
};

export type DatasetRowPayload = {
  model_prompt?: string;
  model_response?: string;
  model_id?: string;
  chat_id?: string;
  expected_output?: string;
};

export type DatasetResponse = {
  id: string;
  name: string;
  description: string;
};

export type DatasetsResponse = {
  community_data_sets: Dataset[];
  system_data_sets: Dataset[];
  user_data_sets: Dataset[];
};

export type DatasetImportPayload = {
  file: any;
  input_column_name: string;
  output_column_name: string;
  expected_output_name: string;
  tags_column_name: string;
  system_instruction_column_name: string;
  model_label_column_name: string;
  variables_column_names: string;
  human_eval_score_column_name: string;
  human_eval_score_notes_column_name: string;
  chat_column_name: string;
  llm_evaluations_column_name: string;
  inference_analytics_column_name: string;
};

export type SxsProjectImportPayload = {
  file: any;
  chat_a_column_name: string;
  chat_b_column_name: string;

  input_column_name: string;
  output_a_column_name: string;
  output_b_column_name: string;
  system_instruction_a_column_name: string;
  system_instruction_b_column_name: string;
  model_label_a_column_name: string;
  model_label_b_column_name: string;
  llm_evaluations_a_column_name: string;
  llm_evaluations_b_column_name: string;
  inference_analytics_a_column_name: string;
  inference_analytics_b_column_name: string;

  variables_column_name: string;
  expected_output_column_name: string;
  human_sxs_rating_column_name: string;
  human_sxs_notes_column_name: string;
  tags_column_name: string;
};

export type DatasetCsvToExistingPayload = {
  file: any;
  input_column_name: string;
  output_column_name?: string;
  expected_output_name?: string;
  dataset_id: string;
  tags_column_name: string;
  system_instruction_column_name: string;
  model_label_column_name: string;
  variables_column_names: string;
};

export interface DataTransferAllPayload {
  source_id: string;
  source_type: "DATASET" | "PROJECT";
  target_id: string;
  target_type: "DATASET" | "PROJECT";
}

export interface DataTransferPayload extends DataTransferAllPayload {
  chat_ids: string[];
}

export type DeleteDatasetRowsBulkParams = {
  id: string;
  payload: {
    chat_turn_ids: string[];
  };
};

export type HumanEvaluatorQueryParams = {
  scopeType?: "USER" | "SYSTEM";
  scoringMechanismType?: ScoringMechanismType;
};

export type CreateFeedbackForChatTurnPayload = {
  evaluatorId: string;
  chatTurnId: string;
  score: number;
  notes?: string;
};

export type HumanEvalSxSPayload = {
  projectId: string;
  pairId: string;
  rating: string | null;
  notes: string | null;
  chat_turn_a_id?: string;
  chat_turn_b_id?: string;
};

export type NewProjectDataRowPayload = {
  projectId: string;
};

export type WorkbookRowUpdatePayload = {
  model_id?: string;
  prompt?: {
    role: InferenceChatCompletionPromptRole;
    text: string;
  };
};

export type AdditionalHeadersData = {
  [key: string]: string;
};

export type UpdateModelPayload = {
  label?: string;
  description?: string;
  //api_key?: string | null;
  url?: string;
  additional_headers?: AdditionalHeadersData;
  descriptors?: ModelDescriptors;
};

export type CustomModelPayload = {
  name?: string;
  label: string;
  url: string;
  api_key?: string;
  supported_provider: string;
  description?: string;
  additional_headers?: AdditionalHeadersData;
  properties?: {
    temperature?: number;
    max_tokens?: number;
    top_p?: number;
    seed?: number;
  };
};

export type GetProjectsParams = {
  include?: string;
  page_size?: number;
  page_number?: number;
  type?: ProjectType;
};

export type UpdateSxSPairPayload = {
  input?: string;
  expectedOutput?: string;
  variables?: Record<string, string>;
  tags?: string[];
};

export type NewSxSRowPayload = {
  input: string;
  expectedOutput?: string;
};

export type NewSxSRowResponse = {
  chat_id_a: string;
  chat_id_b: string;
  chat_turn_id_a: string;
  chat_turn_id_b: string;
  id: string;
  project_id: string;
  isLoading?: boolean;
};

export interface FileImportResponse {
  errorMessages: string[];
  failedRows: number;
  successfulRows: number;
  totalRows: number;
}

export type SxSInferencePayload = {
  model_id_a?: string;
  model_id_b?: string;
  prompts?: {
    role: InferenceChatCompletionPromptRole;
    text: string;
  }[];
  expected_output?: string;
  variables?: Record<string, string>;
};

export enum HUMAN_SXS_RATING {
  A_IS_BETTER = "A_IS_BETTER",
  B_IS_BETTER = "B_IS_BETTER",
  BOTH_ARE_GOOD = "BOTH_ARE_GOOD",
  BOTH_ARE_BAD = "BOTH_ARE_BAD",
}
