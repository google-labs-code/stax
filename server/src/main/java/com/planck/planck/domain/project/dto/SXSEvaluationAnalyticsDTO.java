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

package com.planck.planck.domain.project.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.domain.analytics.evaluation.dto.ProjectEvaluationAnalyticsByScorer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(
    description =
        "Evaluation analytics for an SxS project, separated by side A and side B, with delta calculations")
public class SXSEvaluationAnalyticsDTO {

  @Schema(description = "Evaluation analytics for Side A", required = true)
  private ProjectEvaluationAnalyticsByScorer sideA;

  @Schema(description = "Evaluation analytics for Side B", required = true)
  private ProjectEvaluationAnalyticsByScorer sideB;

  @Schema(
      description = "Difference between the average Side A and Side B analytics (Side A - Side B)",
      required = true)
  private Double delta;

  @Schema(description = "ID of the scorer")
  private String scorerId;

  @Schema(description = "Name of the scorer")
  private String scorerName;
}
