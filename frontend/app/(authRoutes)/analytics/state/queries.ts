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

import { backendEndpoints } from "@/config/endpoints";
import { getRequest } from "@/utils/apiClient";

import {
  EvaluationMonitoringPayload,
  EvaluationMonitoringResponse,
  InferenceMonitoringPayload,
  InferenceMonitoringResponse,
} from "../types/charts";

export const getEvaluationMonitoring = async (
  params: EvaluationMonitoringPayload,
) => {
  return await getRequest<EvaluationMonitoringResponse>(
    backendEndpoints.USER_EVALUATION_MONITORING.ANALYTICS,
    params,
  );
};

export const getInferenceMonitoring = async (
  params: InferenceMonitoringPayload,
): Promise<InferenceMonitoringResponse> => {
  const response = await getRequest<InferenceMonitoringResponse>(
    backendEndpoints.ANALYTICS.INFERENCE,
    params,
  );

  return response;
};
