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

package com.planck.planck.domain.analytics.inference;

import com.planck.planck.domain.analytics.inference.dto.TimeSeriesAnalyticsDTO;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.AggregationWindow;
import com.planck.planck.enums.ModelProvider;
import java.time.Instant;
import java.util.List;

public interface InferenceAnalyticsService {
  List<TimeSeriesAnalyticsDTO> getInferenceTimeSeriesAnalytics(
      String modelId,
      User user,
      String projectId,
      ModelProvider modelProvider,
      Instant startTime,
      Instant endTime,
      AggregationWindow window, // This is the 'aggregateWindow' from controller, can be null
      List<String> tagIds);
}
