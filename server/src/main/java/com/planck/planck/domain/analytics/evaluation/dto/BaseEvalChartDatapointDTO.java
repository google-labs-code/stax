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
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(name = "base_datapoint", description = "Base data point with common fields")
public class BaseEvalChartDatapointDTO {

  @Schema(name = "category", description = "Category name")
  private String category;

  @Schema(name = "score", description = "Score name")
  private String score;

  @Schema(name = "count", description = "Count of evaluations with this score")
  private long count;

  @Schema(name = "color", description = "Color of the category")
  private String color;

  public BaseEvalChartDatapointDTO(String category, String score, String color) {
    this.category = category;
    this.score = score;
    this.color = color;
    this.count = 0;
  }

  public BaseEvalChartDatapointDTO(String category, String score, String color, long count) {
    this(category, score, color);
    this.count = count;
  }

  public void incrementCount() {
    this.count++;
  }
}
