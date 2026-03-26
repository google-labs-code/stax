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

import {
  EvaluationMonitoringPayload,
  FilterLabel,
  InferenceMonitoringPayload,
  QualityDatapoint,
} from "../types/charts";
import { FilterType } from "./types";

export const getEvaluationMonitoringPayload = (
  selectedDates: Dayjs[],
  filters: FilterType = {
    project: "",
    model: null,
    tags: [],
  },
  scorerNames?: string[],
): EvaluationMonitoringPayload => ({
  startTime: selectedDates[0].format("YYYY-MM-DDTHH:mm:ssZ"),
  endTime: selectedDates[1].format("YYYY-MM-DDTHH:mm:ssZ"),
  modelIds: filters.model?.id ? [filters.model?.id] : undefined,
  tagIds: filters?.tags,
  projectId: filters.project,
  scorerNames,
});

export const getInferenceMonitoringPayload = (
  selectedDates: Dayjs[],
  filters: FilterType = {
    project: "",
    model: null,
    tags: [],
  },
): InferenceMonitoringPayload => ({
  start_time: selectedDates[0].format("YYYY-MM-DDTHH:mm:ssZ"),
  end_time: selectedDates[1].format("YYYY-MM-DDTHH:mm:ssZ"),
  aggregateWindow: "daily",
  ...(filters?.project ? { projectId: filters.project } : {}),
  ...(filters?.model
    ? {
        modelId: filters.model.id,
        modelProvider: filters.model.provider,
      }
    : {}),
  ...(filters?.tags
    ? {
        tagIds: filters.tags.join(","),
      }
    : {}),
});

export const getFilterName = (index: number, length: number): FilterLabel => {
  if (index === 0) {
    if (length > 1) {
      return FilterLabel.FILTER_A;
    } else {
      return FilterLabel.ALL_MODELS;
    }
  } else {
    return FilterLabel.FILTER_B;
  }
};

export const getEvaluators = (
  results1: string[],
  results2: string[],
): string[] =>
  [...results1, ...results2].filter(
    (evaluator, index, self) => self.indexOf(evaluator) === index,
  );

export const getDatapoints = (
  filterLabel: string,
  datapoints: QualityDatapoint[],
): QualityDatapoint[] => {
  if (datapoints) {
    return datapoints?.map((dp) => ({
      ...dp,
      filter: filterLabel,
    }));
  }

  return [];
};

export const generateRange = (
  start: number,
  end: number,
  step: number,
): number[] => {
  const result: number[] = [];
  for (let i = start; i <= end + 0.02; i += step) {
    result.push(parseFloat(i.toFixed(2)));
  }

  return result;
};

export const roundUpToEvenHundredth = (input: string): number => {
  const num = parseFloat(input);
  if (isNaN(num)) return 0;

  const hundredths = Math.ceil(num * 100);
  const evenHundredths = hundredths % 2 === 0 ? hundredths : hundredths + 1;

  return parseFloat((evenHundredths / 100).toFixed(2));
};
