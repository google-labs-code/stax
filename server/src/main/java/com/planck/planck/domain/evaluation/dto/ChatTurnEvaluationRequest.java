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
import com.planck.planck.annotation.AtLeastOneFieldRequired;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request to create an evaluation for a single chat turn")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@AtLeastOneFieldRequired(
    fields = {"chatTurnId", "sxsPair"},
    message = "Either chatTurnId or sxsPair must be provided")
public class ChatTurnEvaluationRequest {
  @Schema(description = "Chat turn ID", example = "chatTurn1")
  @JsonProperty("chat_turn_id")
  private String chatTurnId;

  @Schema(description = "SXS Pair")
  @JsonProperty("sxs_pair")
  private SXSPairEvaluationRequest sxsPair;

  @Schema(description = "Evaluator ID", example = "evaluator1")
  @JsonProperty("evaluator_id")
  @NotEmpty(message = "Evaluator ID cannot be empty")
  private String evaluatorId;
}
