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

package com.planck.planck.domain.inference.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to bulk rerun inference for a list of ChatTurn IDs")
public class BulkInferenceRequest {

  @JsonProperty("model_ids")
  @Schema(description = "Optional list of model IDs to (re)run inference for.")
  private List<String> modelIds;

  @NotEmpty(message = "List of ChatTurn IDs cannot be empty")
  @JsonProperty("chat_turn_ids")
  @Size(min = 1, message = "At least one ChatTurn ID is required")
  @Schema(description = "List of ChatTurn IDs to rerun inference for")
  private List<String> chatTurnIds;
}
