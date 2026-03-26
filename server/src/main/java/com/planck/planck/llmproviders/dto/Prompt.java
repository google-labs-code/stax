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

package com.planck.planck.llmproviders.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.enums.InputRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Prompt {

  @JsonProperty("role")
  @Schema(
      description = "Role of the prompt",
      example = "USER",
      allowableValues = {"USER", "ASSISTANT", "SYSTEM", "DEVELOPER", "TOOL", "FUNCTION"})
  InputRole role;

  @Schema(description = "Text of the prompt", example = "What is the capital of France?")
  @JsonProperty("text")
  String text;

  public Prompt(ModelInput input) {
    this.role = input.getRole();
    this.text = input.getText();
  }

  public Prompt(ModelResponse response) {
    this.role = InputRole.ASSISTANT;
    this.text = response.getText();
  }
}
