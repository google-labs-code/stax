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

package com.planck.planck.domain.analytics.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(
    name = "UserEvalChartEntryDTO",
    description = "User Eval Chart Entry DTO. Evaluation analytics data for a specific scorer")
public class UserEvalChartEntryDTO {
  @Schema(
      name = "datapoints",
      description =
          "One data point per each score (category). Contains count as well as data about prompt/completion tokens/latency")
  private List<EvalChartDatapointDTO> datapoints;

  @Schema(
      name = "monitoring",
      description =
          "Monitoring information about all score/category datapoints - tokens and latency")
  private EvalChartMonitoringStatsDTO monitoring;

  @Schema(
      name = "variables",
      description = "Variables, contains counts for failed, in progress and pending evaluations")
  private EvalChartEntryVariablesDTO variables;

  @Schema(name = "scorerId", description = "ID of the scorer")
  private String scorerId;

  @Schema(name = "scorerName", description = "Name of the scorer")
  private String scorerName;
}
