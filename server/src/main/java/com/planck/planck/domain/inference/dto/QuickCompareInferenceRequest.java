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
import com.planck.planck.annotation.AtLeastOneFieldRequired;
import com.planck.planck.llmproviders.dto.Prompt;
import io.swagger.v3.oas.annotations.media.Schema;
// import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@Schema(description = "Request for quick compare inference")
@AllArgsConstructor
@RequiredArgsConstructor
@AtLeastOneFieldRequired(
    fields = {"prompts", "previousChatTurnId"},
    message = "Either non-empty prompts or previousChatTurnId is required")
public class QuickCompareInferenceRequest {

  @NotEmpty(message = "Model Id is required")
  @JsonProperty("model_id")
  @Schema(description = "Model ID for the inference", example = "model-uuid4")
  private String modelId;

  @JsonProperty("previous_chat_turn_id")
  @Schema(description = "Previous chat turn ID for the inference", example = "chat-turn-uuid4")
  private String previousChatTurnId;

  @JsonProperty("prompts")
  private List<Prompt> prompts;

  @JsonProperty("variables")
  private Map<String, String> variables;
}
