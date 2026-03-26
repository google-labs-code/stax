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

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(
    name = "monitoring",
    description =
        "Monitoring information about all score/category datapoints for the current scorer - tokens and latency")
public class EvalChartMonitoringStatsDTO {
  @Schema(name = "average_prompt_tokens", description = "Average prompt tokens")
  private long averagePromptTokens;

  @Schema(name = "total_prompt_tokens", description = "Total prompt tokens")
  private long totalPromptTokens;

  @Schema(name = "average_completion_tokens", description = "Average completion tokens")
  private long averageCompletionTokens;

  @Schema(name = "total_completion_tokens", description = "Total completion tokens")
  private long totalCompletionTokens;

  @Schema(name = "average_latency", description = "Average latency")
  private double averageLatency;

  @Schema(name = "total_latency", description = "Total latency")
  private double totalLatency;

  @JsonProperty("total_tokens")
  @Schema(name = "total_tokens", description = "Total tokens (prompt + completion) used")
  public long getTotalTokens() {
    return totalPromptTokens + totalCompletionTokens;
  }
}
