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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(name = "datapoint", description = "One data point per score/category")
public class EvalChartDatapointDTO extends BaseEvalChartDatapointDTO {

  @Schema(name = "prompt_tokens", description = "Total prompt tokens")
  private long promptTokens;

  @Schema(name = "completion_tokens", description = "Total completion tokens")
  private long completionTokens;

  @Schema(name = "average_latency", description = "Average latency")
  private double averageLatency;

  @Schema(name = "total_latency", description = "Total latency")
  private double totalLatency;

  @Schema(name = "average_prompt_tokens", description = "Average prompt tokens")
  private long averagePromptTokens;

  @Schema(name = "average_completion_tokens", description = "Average completion tokens")
  private long averageCompletionTokens;

  public EvalChartDatapointDTO(String category, String score, String color) {
    super(category, score, color);
    this.promptTokens = 0;
    this.completionTokens = 0;
    this.totalLatency = 0;
    this.averagePromptTokens = 0;
    this.averageCompletionTokens = 0;
    this.averageLatency = 0;
  }

  public void addPromptTokens(int tokens) {
    this.promptTokens += tokens;
  }

  public void addCompletionTokens(int tokens) {
    this.completionTokens += tokens;
  }

  public void addTotalLatency(double latency) {
    this.totalLatency += latency;
  }

  public void updateAverages() {
    if (getCount() > 0) {
      this.averagePromptTokens = promptTokens / getCount();
      this.averageCompletionTokens = completionTokens / getCount();
      this.averageLatency = totalLatency / getCount();
    }
  }

  @JsonProperty("total_tokens")
  @Schema(name = "total_tokens", description = "Total tokens (prompt + completion) used")
  public long getTotalTokens() {
    return promptTokens + completionTokens;
  }
}
