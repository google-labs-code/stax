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

package com.planck.planck.domain.analytics.inference.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated inference monitoring summary for a project")
public class ProjectInferenceMonitoringSummaryDTO {

  @Schema(
      description = "Total number of inferences in the project",
      required = true,
      example = "150")
  @JsonProperty("total_inferences")
  private Long totalInferences = 0L;

  @Schema(
      description = "Average turn time taken across all inferences in milliseconds",
      required = true,
      example = "1250.5")
  @JsonProperty("average_turn_time_taken")
  private Double averageTurnTimeTaken = 0.0;

  @Schema(
      description = "Total prompt tokens used across all inferences",
      required = true,
      example = "50000")
  @JsonProperty("total_prompt_tokens")
  private Long totalPromptTokens = 0L;

  @Schema(
      description = "Total completion tokens used across all inferences",
      required = true,
      example = "25000")
  @JsonProperty("total_completion_tokens")
  private Long totalCompletionTokens = 0L;

  @Schema(
      description = "Total tokens used across all inferences",
      required = true,
      example = "75000")
  @JsonProperty("total_tokens")
  private Long totalTokens = 0L;
}
