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

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(
    name = "ProjectEvaluationAnalyticsByScorer",
    description = "Project evaluation analytics by scorer")
public class ProjectEvaluationAnalyticsByScorer {

  @Schema(description = "Score counts - one object per each score, including count, score, color")
  private List<BaseEvalChartDatapointDTO> scoreCounts;

  @Schema(description = "Average score based on all preset scores")
  private Double averageScore;

  @Schema(description = "Scorer ID")
  private String scorerId;

  @Schema(description = "Name of the scorer")
  private String scorerName;

  public ProjectEvaluationAnalyticsByScorer() {
    this.scoreCounts = List.of();
    this.averageScore = null;
    this.scorerId = null;
  }

  public ProjectEvaluationAnalyticsByScorer(
      List<EvalChartDatapointDTO> datapoints, String scorerId, String scorerName) {
    this.scoreCounts =
        datapoints.stream()
            .map(
                dp ->
                    new BaseEvalChartDatapointDTO(
                        dp.getCategory(), dp.getScore(), dp.getColor(), dp.getCount()))
            .toList();
    long totalCount = scoreCounts.stream().mapToLong(BaseEvalChartDatapointDTO::getCount).sum();
    this.averageScore =
        totalCount > 0
            ? scoreCounts.stream()
                    .mapToDouble(dp -> Double.parseDouble(dp.getScore()) * dp.getCount())
                    .sum()
                / totalCount
            : null;

    this.scorerId = scorerId;
    this.scorerName = scorerName;
  }
}
