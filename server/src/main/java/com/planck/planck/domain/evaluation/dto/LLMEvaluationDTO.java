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

package com.planck.planck.domain.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LLMEvaluationDTO {
  @Schema(description = "ID of the evaluator which was used to run the evaluation")
  @JsonProperty("evaluator_id")
  private String evaluatorId;

  @Schema(
      description =
          "The final score given by the evaluation, based on raw score response and score mappings. NaN if we were unable to match based on response")
  private String score;

  @Schema(description = "The raw response from the LLM")
  private String llmResponse;

  @Schema(
      description =
          "THe reasoning for the response of the LLM. Expected to be on 2nd line of the response")
  private String reasoning;

  @Schema(
      description = "In case evaluation failed, the reason for the failure shall be visible here.")
  private String failureReason;

  @Schema(
      description =
          "The raw score response of the LLM, before matching it to score category. Expected to be on 1st line of the response")
  private String category;

  @Schema(description = "The status of the evaluation run")
  private String evaluationStatus;

  @Schema(description = "The full status of the evaluation run")
  private String evaluationStatusText;

  @Schema(description = "Color that is configured for that score")
  private String color;
}
