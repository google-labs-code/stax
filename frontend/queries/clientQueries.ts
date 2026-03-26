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

import {
  Dataset,
  DatasetRow,
  DatasetRowsResponse,
} from "@/app/(authRoutes)/datasets/types";
import {
  SXSRow,
  Workbook,
  WorkbookItem,
} from "@/app/(authRoutes)/projects/[id]/types";
import { getModelDetails } from "@/config/constants";
import { backendEndpoints } from "@/config/endpoints";
import {
  AllEvaluatorsQueryParams,
  AllEvaluatorsResponse,
  ChatCompletionResponse,
  ChatCompletionSxSResponse,
  ChatHistory,
  ChatTurnByIdResponse,
  ClearResultsPayload,
  ContinueChatBSxSQueryPayload,
  Datasets,
  EvalAnalyticsScore,
  EvalSxSAnalyticsScorePointwise,
  EvalSxSAnalyticsScoreSxS,
  EvaluationChatTurnPayload,
  EvaluationChatTurnsPayload,
  EvaluationChatTurnsResponse,
  EvaluationProjectPayload,
  GenerateOutputsPayload,
  GenerateOutputsSxSAllPayload,
  GenerateOutputsSxSPayload,
  GenerateOutputsSxSResponse,
  HumanEvalPassRateResponse,
  HumanEvalScore,
  InferenceChatCompletionPayload,
  InferenceChatCompletionSxSPayload,
  LLMEvaluatorItem,
  MetricsSummaryResponse,
  Project,
  ProjectsDto,
  TagLink,
  TagLinkEntityType,
  TagLinkPayload,
  TagLinksPayload,
  TagRaw,
  TagsAPIResponse,
} from "@/types";
import {
  deleteRequest,
  getRequest,
  patchRequest,
  postRequest,
  putRequest,
} from "@/utils/apiClient";
import { FileWithPath } from "@mantine/dropzone";

import {
  CreateFeedbackForChatTurnPayload,
  CustomModelPayload,
  DataTransferAllPayload,
  DataTransferPayload,
  DatasetImportPayload,
  DatasetPayload,
  DatasetRowPayload,
  DeleteDatasetRowsBulkParams,
  DuplicateModelPayload,
  FileImportResponse,
  GetProjectsParams,
  HumanEvalSxSPayload,
  HumanEvaluatorQueryParams,
  LLMEvaluatorPayload,
  LLMEvaluatorResponse,
  Model,
  ModelFormValues,
  ModelRaw,
  NewProjectDataRowPayload,
  NewSxSRowResponse,
  SxSInferencePayload,
  SxsProjectImportPayload,
  UpdateModelPayload,
  UpdateSxSPairPayload,
  WorkbookRowUpdatePayload,
} from "./types";

export const getTagsQuery = async () => {
  return (await getRequest(backendEndpoints.TAGS)) as TagsAPIResponse;
};

export const createNewTagQuery = (data: TagRaw) => {
  return postRequest(backendEndpoints.TAGS, data);
};

export const deleteTagQuery = (id: string) => {
  return deleteRequest(`${backendEndpoints.TAGS}/${id}`);
};

export const getProjectsQuery = async (params: GetProjectsParams = {}) => {
  return (await getRequest(
    backendEndpoints.PROJECTS.INDEX,
    params,
  )) as ProjectsDto;
};

export const createProjectQuery = async (data: Project) =>
  (await postRequest(backendEndpoints.PROJECTS.INDEX, data)) as Project;

export const getModelsQuery = async () => {
  const response = (await getRequest(
    backendEndpoints.MODELS.INDEX,
  )) as ModelRaw[];

  return response
    .map((model: ModelRaw) => ({
      ...model,
      icon: getModelDetails(model.provider)?.icon,
    }))
    .filter((model: Model) => !model.is_deprecated);
};

export const getProjectQuery = async (
  id: string,
  pageSize: number,
  page: number,
) => {
  return getRequest(`workbook/${backendEndpoints.PROJECTS.INDEX}/${id}`, {
    page_size: pageSize,
    page_token: page,
  }) as Promise<Workbook>;
};

export const getProjectSxSQuery = async (
  id: string,
  pageSize: number,
  page: number,
) => {
  return getRequest(`sxs/${backendEndpoints.CONTAINERS}/${id}/workbook`, {
    page_size: pageSize,
    page_token: page,
  }) as Promise<Workbook>;
};

export const getChatHistory = async (id: string) => {
  return getRequest(
    `${backendEndpoints.CHAT.INDEX}/${id}`,
  ) as Promise<ChatHistory>;
};

export const getChatHistoryFromCurrentChatTurnId = async (
  id: string,
  chatTurnId: string,
) => {
  return getRequest(
    `${backendEndpoints.CHAT.INDEX}/${id}/turns/${chatTurnId}`,
  ) as Promise<ChatHistory>;
};

export const getChatWorkbookHistory = async (id: string) => {
  return getRequest(`${backendEndpoints.CHAT.INDEX}/workbook/${id}`) as Promise<
    WorkbookItem[]
  >;
};

export const getChatSxsWorkbookHistory = async (
  pairId: string,
  projectId: string,
) => {
  return getRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/workbook/${pairId}`,
  ) as Promise<WorkbookItem[]>;
};

export const updateExpectedOutput = async (
  chatTurnId: string,
  expectedOutput: string,
) => {
  return (await putRequest(`${backendEndpoints.CHAT.CHAT_TURN}/${chatTurnId}`, {
    expected_output: expectedOutput,
  })) as Project;
};

export const getChatVariablesQuery = async (chatId: string) => {
  return (await getRequest(
    `${backendEndpoints.CHAT.INDEX}/${chatId}/${backendEndpoints.CHAT.VARIABLES}`,
  )) as string;
};

export const updateChatVariablesQuery = async (
  chatId: string,
  payload?: {
    [key: string]: string;
  },
) => {
  return (await putRequest(
    `${backendEndpoints.CHAT.INDEX}/${chatId}/${backendEndpoints.CHAT.VARIABLES}`,
    payload,
  )) as string;
};

export const getChatTurnById = async (chatTurnId: string) => {
  return (await getRequest(
    `${backendEndpoints.CHAT.CHAT_TURN}/${chatTurnId}`,
  )) as ChatTurnByIdResponse;
};

export const deleteAllWorkbookRowsQuery = async (id: string) => {
  return await deleteRequest(
    `workbook/${backendEndpoints.PROJECTS.INDEX}/${id}/delete-rows/all`,
  );
};

export const deleteAllSxSRowsQuery = async (id: string) => {
  return await deleteRequest(`sxs/${backendEndpoints.CONTAINERS}/${id}/all`);
};

export const updateProjectQuery = async (project: Project) => {
  return (await patchRequest(
    `${backendEndpoints.PROJECTS.INDEX}/${project.project_id}`,
    project,
  )) as Project;
};

export const duplicateModelQuery = async (data: ModelFormValues) => {
  const parsedData: DuplicateModelPayload = {
    ...(data.label
      ? {
          new_label: data.label,
        }
      : {}),
    ...(data.description
      ? {
          new_description: data.description,
        }
      : {}),
    new_properties: {
      temperature: data?.temperature,
      max_tokens: data?.maxTokens,
      top_p: data.topP,
      seed: data?.seed,
    },
  };

  return (await postRequest(
    `${backendEndpoints.MODELS.ROOT}/${data.id}/${backendEndpoints.MODELS.DUPLICATE}`,
    parsedData,
  )) as Model;
};

export const updateModelQuery = async (
  data: UpdateModelPayload,
  modelId: string,
) => {
  return (await patchRequest(
    `${backendEndpoints.MODELS.ROOT}/${modelId}`,
    data,
  )) as Model;
};

export const addCustomModelQuery = async (payload: CustomModelPayload) => {
  return await postRequest(backendEndpoints.MODELS.CUSTOM_MODEL, payload);
};

export const getModelProvidersQuery = async (): Promise<
  Record<string, string>
> => {
  return await getRequest(backendEndpoints.MODELS.MODEL_PROVIDERS);
};

export const deleteModelQuery = async (id: string) => {
  return await deleteRequest(`${backendEndpoints.MODELS.ROOT}/${id}`);
};

export const inferenceChatCompletionQuery = async (
  payload: InferenceChatCompletionPayload,
  projectId: string,
) =>
  (await postRequest(
    `inference/projects/${projectId}/${backendEndpoints.INFERENCE.QUICK_COMPARE_CHAT_COMPLETION}`,
    payload,
  )) as ChatCompletionResponse;

export const inferenceAllChatCompletionBulkQuery = async (
  payload: any,
  projectId: string,
) =>
  (await postRequest(
    `/inference/projects/${projectId}/bulk/all`,
    payload,
  )) as ChatCompletionResponse;

export const deleteUserDataQuery = () =>
  deleteRequest(backendEndpoints.AUTH.DELETE_USER_DATA);

export const evaluationChatTurnsQuery = async (
  payload: EvaluationChatTurnsPayload,
  projectId: string,
) =>
  (await postRequest(
    `evaluations/projects/${projectId}/${backendEndpoints.EVALUATIONS.CHAT_TURNS}`,
    payload,
  )) as EvaluationChatTurnsResponse;

export const evaluationProjectQuery = async (
  payload: EvaluationProjectPayload,
  projectId: string,
) =>
  (await postRequest(
    `${backendEndpoints.EVALUATIONS.PROJECT}/${projectId}`,
    payload,
  )) as EvaluationChatTurnsResponse;

export const evaluationWholeProjectQuery = async (
  payload: EvaluationProjectPayload,
  projectId: string,
) =>
  (await postRequest(
    `${backendEndpoints.EVALUATIONS.PROJECT}/${projectId}`,
    payload,
  )) as EvaluationChatTurnsResponse;

export const evaluationChatTurnQuery = async (
  payload: EvaluationChatTurnPayload,
  projectId: string,
) =>
  (await postRequest(
    `evaluations/projects/${projectId}/${backendEndpoints.EVALUATIONS.CHAT_TURN}`,
    payload,
  )) as ChatCompletionResponse;

export const humanEvalPassRateQuery = async (projectId: string) =>
  (await getRequest(
    `projects/${projectId}/${backendEndpoints.HUMAN_EVALUATORS.PASS_RATE}`,
  )) as HumanEvalPassRateResponse;

export const metricsSummaryQuery = async (projectId: string) =>
  (await getRequest(
    `projects/${projectId}/${backendEndpoints.METRICS.ROOT}`,
  )) as MetricsSummaryResponse;

export const generateOutputsQuery = async (
  payload: GenerateOutputsPayload,
  projectId: string,
) =>
  (await postRequest(
    `/inference/projects/${projectId}/bulk`,
    payload,
  )) as EvaluationChatTurnsResponse;

export const clearResultsQuery = async (
  payload: ClearResultsPayload,
  projectId: string,
) =>
  await deleteRequest(
    `/workbook/${backendEndpoints.PROJECTS.INDEX}/${projectId}/clear-results`,
    payload,
  );

export const clearAllResultsQuery = async (projectId: string) =>
  await deleteRequest(
    `/workbook/${backendEndpoints.PROJECTS.INDEX}/${projectId}/clear-results/all`,
  );

export const getLLMEvaluatorSystemQuery = async () => {
  return getRequest(backendEndpoints.LLM_EVALUATOR.SYSTEM) as Promise<
    LLMEvaluatorItem[]
  >;
};

export const getLLMEvaluatorCustomQuery = async () => {
  return getRequest(backendEndpoints.LLM_EVALUATOR.CUSTOM) as Promise<
    LLMEvaluatorItem[]
  >;
};

export const createCustomLLMEvaluatorQuery = async (
  data: LLMEvaluatorPayload,
) => {
  return (await postRequest(
    backendEndpoints.LLM_EVALUATOR.ROOT,
    data,
  )) as LLMEvaluatorResponse;
};

export const getLLMEvaluatorQuery = async (id: string) => {
  return (await getRequest(
    `${backendEndpoints.LLM_EVALUATOR.ROOT}/${id}`,
  )) as LLMEvaluatorResponse;
};

export const updateLLMEvaluatorQuery = async (data: LLMEvaluatorPayload) => {
  return (await patchRequest(
    `${backendEndpoints.LLM_EVALUATOR.ROOT}/${data?.id}`,
    data,
  )) as LLMEvaluatorResponse;
};

export const deleteLLMEvaluatorQuery = async (id: string) => {
  return await deleteRequest(`${backendEndpoints.LLM_EVALUATOR.ROOT}/${id}`);
};

export const updateDatasetQuery = async (data: DatasetPayload) => {
  return (await patchRequest(
    `${backendEndpoints.DATASETS.ROOT}/${data?.id}`,
    data,
  )) as Dataset;
};

export const getDatasetQuery = async (id: string) => {
  return (await getRequest(
    `${backendEndpoints.DATASETS.ROOT}/${id}`,
  )) as Dataset;
};

export const getDatasetsQuery = async () => {
  return (await getRequest(backendEndpoints.DATASETS.ROOT)) as Datasets;
};

export const getDatasetRowsQuery = async (
  id: string,
  pageSize: number,
  page: number,
) => {
  return (await getRequest(`${backendEndpoints.DATASETS.ROOT}/${id}/rows`, {
    page_size: pageSize,
    page_token: page,
  })) as DatasetRowsResponse;
};

export const deleteDatasetQuery = async (id: string) => {
  return await deleteRequest(`${backendEndpoints.DATASETS.ROOT}/${id}`);
};

export const createDatasetQuery = async (data: DatasetPayload) => {
  return (await postRequest(backendEndpoints.DATASETS.ROOT, data)) as Dataset;
};

export const createDatasetRowQuery = async (
  id: string,
  data: DatasetRowPayload,
) => {
  return (await postRequest(
    `${backendEndpoints.DATASETS.ROOT}/${id}/row`,
    data,
  )) as DatasetRow;
};

export const updateWorkbookRowQuery = async (
  projectId: string,
  chatTurnId: string,
  payload: WorkbookRowUpdatePayload,
) => {
  return (await patchRequest(
    `workbook/${backendEndpoints.PROJECTS.INDEX}/${projectId}/rows/${chatTurnId}`,
    payload,
  )) as WorkbookItem;
};

export const createTagLinkForEntityQuery = async (data: TagLinkPayload) => {
  return (await postRequest(
    `${backendEndpoints.TAG_LINKS.ROOT}/${data?.entityType}/${data?.entityId}`,
    {
      tagId: data?.tagId,
    },
  )) as TagLink;
};

export const createTagLinksQuery = async (data: TagLinksPayload) => {
  return (await postRequest(
    backendEndpoints.TAG_LINKS.ROOT,
    data,
  )) as TagLink[];
};

export const getTagLinksForEntityQuery = async (
  entityType: TagLinkEntityType,
  entityId: string,
) => {
  return (await getRequest(
    `${backendEndpoints.TAG_LINKS.ROOT}/${entityType}/${entityId}`,
  )) as TagLink[];
};

export const deleteTagLinksQuery = async (data: TagLinksPayload) => {
  return await postRequest(backendEndpoints.TAG_LINKS.REMOVE, data);
};

export const deleteAllTagsLinksQuery = async (
  projectId: string,
  tagIds: string[],
) => {
  return await postRequest(`/tag_links/remove/project/${projectId}`, tagIds);
};

export const addAllTagsLinksQuery = async (
  projectId: string,
  tagIds: string[],
) => {
  return await postRequest(`/tag_links/project/${projectId}`, tagIds);
};
export const deleteTagLinkQuery = async (id: string) => {
  return await deleteRequest(`${backendEndpoints.TAG_LINKS.ROOT}/${id}`);
};

export const getTagLinksQuery = async () => {
  return (await getRequest(backendEndpoints.TAG_LINKS.ROOT)) as TagLink[];
};

export const updateDatasetRowQuery = async (
  datasetId: string,
  chatTurnId: string,
  data: DatasetRowPayload,
) => {
  return (await patchRequest(
    `${backendEndpoints.DATASETS.ROOT}/${datasetId}/rows/${chatTurnId}`,
    data,
  )) as DatasetRow;
};

export const dataTransferQuery = async (data: DataTransferPayload) => {
  return (await postRequest(
    backendEndpoints.DATA_TRANSFER.ROOT,
    data,
  )) as DatasetRow;
};

export const dataTransferAllQuery = async (data: DataTransferAllPayload) => {
  return (await postRequest(
    backendEndpoints.DATA_TRANSFER.ALL,
    data,
  )) as DatasetRow;
};

export const deleteDatasetRowQuery = async (
  datasetId: string,
  rowId: string,
) => {
  return await deleteRequest(
    `${backendEndpoints.DATASETS.ROOT}/${datasetId}/rows/${rowId}`,
  );
};

export const deleteDatasetRowsBulkQuery = async (
  data: DeleteDatasetRowsBulkParams,
) => {
  return await deleteRequest(
    `${backendEndpoints.DATASETS.ROOT}/${data.id}/rows/bulk`,
    data.payload,
  );
};

export const getHumanEvaluatorsQuery = async (
  params?: HumanEvaluatorQueryParams,
) => {
  return (await getRequest(
    backendEndpoints.HUMAN_EVALUATORS.ROOT,
    params,
  )) as any;
};

export const createFeedbackForChatTurnQuery = async (
  data: CreateFeedbackForChatTurnPayload,
) => {
  return (await postRequest(
    `${backendEndpoints.HUMAN_EVALUATORS.ROOT}/${data.evaluatorId}/chat-turns/${data.chatTurnId}/feedback?score=${data.score}${data.notes ? "&notes=" + data.notes : ""}`,
  )) as HumanEvalScore;
};

export const humanEvalSxSQuery = async (data: HumanEvalSxSPayload) => {
  return (await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${data.projectId}/${data.pairId}/human-feedback`,
    {
      rating: data.rating,
      notes: data.notes,
      ...(data?.chat_turn_a_id && { chat_turn_a_id: data?.chat_turn_a_id }),
      ...(data?.chat_turn_b_id && { chat_turn_b_id: data?.chat_turn_b_id }),
    },
  )) as HumanEvalScore;
};

export const deprecateModelQuery = async (id: string) => {
  return await patchRequest(`${backendEndpoints.MODELS.DEPRECATE}/${id}`);
};

export const createNewProjectDataRowQuery = async (
  data: NewProjectDataRowPayload,
) => {
  return (await postRequest(
    `workbook/${backendEndpoints.PROJECTS.INDEX}/${data.projectId}/row`,
  )) as WorkbookItem;
};

export const deleteFeedbackScoreQuery = async (scoreId: string) => {
  return await deleteRequest(
    `${backendEndpoints.HUMAN_EVALUATORS.FEEDBACK}/${scoreId}`,
  );
};

export const getProjectExportQuery = async (projectId: string) => {
  return await getRequest(
    `${backendEndpoints.PROJECTS.INDEX}/${projectId}/export`,
  );
};

export const getDatasetExportQuery = async (datasetId: string) => {
  return await getRequest(
    `${backendEndpoints.DATASETS.ROOT}/${datasetId}/export`,
  );
};

export const getChatsExportQuery = async (chatIds: string[]) => {
  return await postRequest(`${backendEndpoints.CHAT.INDEX}/export`, chatIds);
};

export const getChatIdExportQuery = async (chatId: string) => {
  return await getRequest(`${backendEndpoints.CHAT.INDEX}/${chatId}/export`);
};

export const getExportALLQuery = async (projectId: string) => {
  return await getRequest(
    `${backendEndpoints.PROJECTS.INDEX}/${projectId}/export`,
  );
};

export const getSxsProjectExport = async (projectId: string) => {
  return await getRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/export`,
  );
};

export const getSxsProjectBulkExport = async (
  projectId: string,
  pairIds: string[],
) => {
  return await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/export`,
    pairIds,
  );
};

export const inferenceChatCompletionSxSQuery = async (
  payload: InferenceChatCompletionSxSPayload,
  projectId: string,
) =>
  (await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/inference`,
    payload,
  )) as ChatCompletionSxSResponse;

export const continueChatBSxSQuery = async (
  payload: ContinueChatBSxSQueryPayload,
  projectId: string,
  pairId: string,
) =>
  (await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/${pairId}/continue`,
    payload,
  )) as ChatCompletionSxSResponse;

export const uploadDatasetFileQuery = async (
  datasetId: string,
  file: FileWithPath,
) => {
  return await postRequest(
    `${backendEndpoints.DATASETS.ROOT}/${datasetId}/import`,
    {
      file,
    },
    {
      "Content-Type": "multipart/form-data",
    },
  );
};

export const uploadProjectFileQuery = async (
  projectId: string,
  file: FileWithPath,
) => {
  return await postRequest(
    `${backendEndpoints.PROJECTS.INDEX}/${projectId}/import`,
    {
      file,
    },
    {
      "Content-Type": "multipart/form-data",
    },
  );
};

export const importDatasetQuery = async (
  datasetId: string,
  data: DatasetImportPayload,
) => {
  return (await postRequest(
    `${backendEndpoints.DATASETS.ROOT}/${datasetId}/import`,
    data,
    {
      "Content-Type": "multipart/form-data",
    },
  )) as FileImportResponse;
};

export const inferenceSxsMetricsQuery = async (projectId: string) => {
  return await getRequest(
    `/sxs/${backendEndpoints.CONTAINERS}/${projectId}/inference-metrics`,
  );
};

export const SxsHumanEvalPassRateQuery = async (projectId: string) => {
  return await getRequest(
    `/sxs/${backendEndpoints.CONTAINERS}/${projectId}/human-eval-metrics`,
  );
};

export const getSxSEvalAnalyticsQuery = async (projectId: string) => {
  return (await getRequest(
    `/sxs/${backendEndpoints.CONTAINERS}/${projectId}/evaluation-analytics`,
  )) as EvalSxSAnalyticsScorePointwise[];
};

export const getSxSEvalAnalyticsSxSQuery = async (projectId: string) => {
  return (await getRequest(
    `/sxs/${backendEndpoints.CONTAINERS}/${projectId}/sxs-eval-metrics`,
  )) as EvalSxSAnalyticsScoreSxS[];
};

export const evalAnalyticsScoreQuery = async (
  projectId: string,
  scoreId: string,
) => {
  return (await getRequest(
    `${backendEndpoints.PROJECTS.INDEX}/${projectId}/${backendEndpoints.PROJECTS.EVAL_ANALYTICS}/${scoreId}`,
  )) as EvalAnalyticsScore;
};

export const updateSxSPairQuery = async (
  projectId: string,
  pairId: string,
  payload: UpdateSxSPairPayload,
) => {
  return (await patchRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/${pairId}`,
    payload,
  )) as SXSRow;
};

export const createNewSxSRowQuery = async (projectId: string) => {
  return (await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}`,
    {},
  )) as NewSxSRowResponse;
};

export const generateOutputsSxSQuery = async (
  projectId: string,
  payload: GenerateOutputsSxSPayload,
) => {
  return (await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/generate-outputs`,
    payload,
  )) as GenerateOutputsSxSResponse;
};

export const generateOutputsSxSAllQuery = async (
  projectId: string,
  payload: GenerateOutputsSxSAllPayload,
) => {
  return (await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/generate-outputs/all`,
    payload,
  )) as GenerateOutputsSxSResponse;
};

export const generateSxSInferenceQuery = async (
  projectId: string,
  payload: SxSInferencePayload,
) => {
  return (await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/inference`,
    payload,
  )) as SXSRow;
};

export const projectHasRemainingJobsQuery = async (projectId: string) => {
  return (await getRequest(
    `job/${backendEndpoints.PROJECTS.INDEX}/${projectId}/has-remaining`,
  )) as boolean;
};

export const importSxsProjectQuery = async (
  projectId: string,
  data: SxsProjectImportPayload,
) => {
  return (await postRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${projectId}/import`,
    data,
    {
      "Content-Type": "multipart/form-data",
    },
  )) as FileImportResponse;
};

export const getPointwiseEvalAnalyticsQuery = async (projectId: string) => {
  return (await getRequest(
    `/projects/${projectId}/${backendEndpoints.PROJECTS.EVAL_ANALYTICS}`,
  )) as EvalAnalyticsScore[];
};

export const getSxSLLMEvaluatorQuery = async (id: string) => {
  return (await getRequest(
    `${backendEndpoints.LLM_PAIRWISE_EVALUATOR}/${id}`,
  )) as LLMEvaluatorResponse;
};

export const createSxSLLMEvaluatorQuery = async (data: LLMEvaluatorPayload) => {
  return (await postRequest(
    backendEndpoints.LLM_PAIRWISE_EVALUATOR,
    data,
  )) as LLMEvaluatorResponse;
};

export const updateSxSLLMEvaluatorQuery = async (data: LLMEvaluatorPayload) => {
  return (await patchRequest(
    `${backendEndpoints.LLM_PAIRWISE_EVALUATOR}/${data?.id}`,
    data,
  )) as LLMEvaluatorResponse;
};

export const getAllEvaluatorsQuery = async (
  params?: AllEvaluatorsQueryParams,
) => {
  return (await getRequest(
    backendEndpoints.EVALUATOR.ALL,
    params,
  )) as AllEvaluatorsResponse;
};

export const deleteSxSLLMEvaluatorQuery = async (id: string) => {
  return await deleteRequest(
    `${backendEndpoints.LLM_PAIRWISE_EVALUATOR}/${id}`,
  );
};
