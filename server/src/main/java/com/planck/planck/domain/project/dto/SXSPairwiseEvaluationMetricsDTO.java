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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.analytics.evaluation.dto.BaseEvalChartDatapointDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO containing side-by-side pairwise evaluation metrics")
public class SXSPairwiseEvaluationMetricsDTO {

  public SXSPairwiseEvaluationMetricsDTO(
      String scorerId, String scorerName, List<BaseEvalChartDatapointDTO> datapoints) {
    this.scorerId = scorerId;
    this.scorerName = scorerName;
    this.datapoints = datapoints != null ? new ArrayList<>(datapoints) : new ArrayList<>();
  }

  @JsonProperty("scorer_id")
  @Schema(description = "Unique identifier of the scorer", example = "pairwise-llm-evaluator-uuid4")
  private String scorerId;

  @JsonProperty("scorer_name")
  @Schema(description = "Name of the scorer", example = "Chat Quality")
  private String scorerName;

  @JsonProperty("datapoints")
  @Schema(
      description =
          "List of evaluation datapoints containing category, score and count information")
  private List<BaseEvalChartDatapointDTO> datapoints;

  public void updateCount(String score, long count) {
    datapoints.stream()
        .filter(dp -> dp.getScore().equals(score))
        .findFirst()
        .ifPresent(dp -> dp.setCount(count));
  }
}
