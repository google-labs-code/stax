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
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(
    description =
        "SXS Pair details for evaluation. If chatTurnA and chatTurnB are not provided, latest chat turns will be used")
@Data
@AllArgsConstructor
public class SXSPairEvaluationRequest {
  @Schema(description = "Unique identifier for the SXS pair", example = "sxs-pair-uuid4")
  @NotEmpty(message = "Pair ID cannot be empty")
  @JsonProperty("id")
  private String id;

  @Schema(description = "Chat Turn A ID", example = "chatTurnA-uuid4")
  @JsonProperty("chat_turn_id_a")
  private String chatTurnIdA;

  @Schema(description = "Chat Turn B ID", example = "chatTurnB-uuid4")
  @JsonProperty("chat_turn_id_b")
  private String chatTurnIdB;
}
