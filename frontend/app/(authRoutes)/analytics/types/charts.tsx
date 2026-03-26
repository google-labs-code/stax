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

import { Dayjs } from "dayjs";

export type InferenceRaw = {
  windowStart?: string | Date;
  windowStartEpochMillis?: number;
  totalInferences?: number;
  total_turn_time_taken?: number;
  average_turn_time_taken?: number;
  total_turn_prompt_tokens?: number;
  total_turn_completion_tokens?: number;
  total_turn_total_tokens?: number;
  average_avg_chat_latency?: number;
  total_chat_prompt_tokens?: number;
  total_chat_completion_tokens?: number;
  total_chat_total_tokens?: number;
};

export type InferenceMonitoringResponse = {
  time_series_analytics: InferenceRaw[];
  overall_inferences: number;
  overall_average_latency: number;
  overall_prompt_tokens: number;
  overall_completion_tokens: number;
  overall_total_tokens: number;
};

export type InferenceMonitoringPayload = {
  start_time?: string;
  end_time?: string;
  aggregateWindow?: string;
  projectId?: string;
  modelId?: string;
  modelProvider?: string;
  tagIds?: string;
};

export type AnalyticsData = {
  averageTime: number;
  prompts: number;
  completed: number;
  total: number;
};

export type Chart = {
  selectedDates: Dayjs[];
  datesLabel?: string;
  analyticsData?: AnalyticsData;
};

export enum ChartTabInference {
  LATENCY = "Latency",
  QUERIES = "Queries",
  TOKENS = "Tokens",
}

export interface LineChartItem extends InferenceRaw {
  filter?: string;
}

export enum FilterLabel {
  FILTER_A = "Filter A",
  FILTER_B = "Filter B",
  ALL_MODELS = "All models",
}

export type TooltipContentItem = {
  label: string;
  value?: string | number;
};

export enum ChartTabEvaluation {
  BAR_CHART = "Bar chart",
  DODGE_PLOT = "Dodge plot",
}

export enum ScoreTypes {
  SENTIMENT = "Sentiment",
  BLEU = "BLEU",
  FACTUALITY = "Factuality",
  JSON = "Is Valid JSON",
}
export interface EvaluationChartDataItem {
  evaluator: string;
  datapoints: QualityDatapoint[];
  monitoring: MonitoringStats;
  tab: ChartTabEvaluation;
}

export type EvaluationMonitoringPayload = {
  startTime: string;
  endTime?: string;
  projectId?: string;
  scorerNames?: string[];
  tagIds?: string[];
  modelIds?: string[];
};

export type QualityDatapoint = {
  category: string;
  score: string;
  count: number;
  color: string;
  promptTokens: number;
  completionTokens: number;
  averageLatency: number;
  totalLatency: number;
  averagePromptTokens: number;
  averageCompletionTokens: number;
  totalTokens: number;
  filter?: string;
};

export type MonitoringStats = {
  average_completion_tokens: number;
  average_latency: number;
  average_prompt_tokens: number;
  total_completion_tokens: number;
  total_latency: number;
  total_prompt_tokens: number;
  total_tokens: number;
};

type MetadataStats = {
  failed: number;
  in_progress: number;
  pending: number;
  unknown: number;
};

type EvaluatorMetric = {
  datapoints: QualityDatapoint[];
  monitoring: MonitoringStats;
  metadata: MetadataStats;
};

export type EvaluationMonitoringResponse = {
  [metric: string]: EvaluatorMetric;
}[];

export const INFERENCE_CHART_TABS = [
  ChartTabInference.LATENCY,
  ChartTabInference.QUERIES,
  ChartTabInference.TOKENS,
];

export const CHART_PROPS_BY_TAB = {
  Tokens: {
    tabName: ChartTabInference.TOKENS,
    x: "windowStart",
    y: "value",
    fill: "type",
  },
};

export const TOKEN_TYPES = [
  { name: "output", value: "total_chat_completion_tokens" },
  { name: "input", value: "total_chat_prompt_tokens" },
];

export const FILTER_LABELS = [FilterLabel.FILTER_A, FilterLabel.FILTER_B];

export const EVALUATION_CHART_TABS = [
  ChartTabEvaluation.BAR_CHART,
  ChartTabEvaluation.DODGE_PLOT,
];
