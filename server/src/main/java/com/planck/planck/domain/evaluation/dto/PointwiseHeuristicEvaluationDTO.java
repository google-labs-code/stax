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
public class PointwiseHeuristicEvaluationDTO {
  @Schema(description = "ID of the evaluator which was used to run the evaluation")
  @JsonProperty("evaluator_id")
  private String evaluatorId;

  @Schema(
      description =
          "Indicates if the evaluation found a match in the text based on the given criteria.")
  private boolean isMatch;

  @Schema(
      description = "In case evaluation failed, the reason for the failure shall be visible here.")
  private String failureReason;

  @Schema(description = "The status of the evaluation run")
  private String evaluationStatus;

  @Schema(description = "The full status of the evaluation run")
  private String evaluationStatusText;
}
