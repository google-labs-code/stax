/*
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

package com.planck.planck.domain.inferencemonitoring;

import com.planck.planck.domain.analytics.inference.dto.TimeSeriesAnalyticsDTO;
import com.planck.planck.enums.AggregationWindow;
import com.planck.planck.enums.ModelProvider;
import java.time.Instant;
import java.util.List;

/**
 * Interface defining custom methods for the InferenceMonitoringRepository that require complex
 * implementation (e.g., using EntityManager).
 */
public interface InferenceMonitoringRepositoryCustom {

  /**
   * Retrieves time-series aggregated analytics for InferenceMonitoring records.
   *
   * @param modelId Optional filter for model ID.
   * @param userId Optional filter for user ID.
   * @param projectId Optional filter for project ID.
   * @param modelProvider Optional filter for model provider.
   * @param startDate The start timestamp for the query range (inclusive).
   * @param endDate The end timestamp for the query range (inclusive).
   * @param window The time window to group aggregates by.
   * @return A list of TimeSeriesAnalyticsDTO, each representing an aggregated time window.
   */
  List<TimeSeriesAnalyticsDTO> getAnalyticsTimeSeries(
      String modelId,
      String userId,
      String projectId,
      ModelProvider modelProvider,
      Instant startDate, // Use Instant
      Instant endDate, // Use Instant
      AggregationWindow window,
      List<String> tagIds);
}
