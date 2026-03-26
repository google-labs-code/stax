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

package com.planck.planck.domain.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.llmproviders.dto.Prompt;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SxsInferenceRequestDTO {

  @NotEmpty(message = "At least modelIdA is required")
  @Schema(description = "Model ID for side A", required = true, example = "model-uuid4")
  private String modelIdA;

  @Schema(description = "Model ID for side B, optional", example = "model-uuid4")
  private String modelIdB;

  @Schema(
      description = "System instruction for side A, gets appended before the next user prompt",
      example = "You are a helpful assistant.")
  @JsonProperty("model_a_instruction")
  private String modelAInstruction;

  @Schema(
      description = "System instruction for side B, gets appended before the next user prompt",
      example = "You are a critical assistant.")
  @JsonProperty("model_b_instruction")
  private String modelBInstruction;

  @NotEmpty(message = "At least one prompt is required")
  @Schema(
      description = "Prompts for the inference",
      required = true,
      example = "[{\"role\": \"USER\", \"text\": \"What is the capital of France?\"}]")
  private List<Prompt> prompts;

  @Schema(description = "Expected output, optional", example = "Paris")
  private String expectedOutput;

  @Schema(description = "Variables, optional", example = "{\"key\": \"value\"}")
  private Map<String, String> variables;
}
