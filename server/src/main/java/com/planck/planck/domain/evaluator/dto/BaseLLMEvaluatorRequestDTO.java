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

package com.planck.planck.domain.evaluator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.enums.ScoreType;
import com.planck.planck.llmproviders.dto.Prompt;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
@Schema(description = "Base DTO for LLM Evaluator requests")
public abstract class BaseLLMEvaluatorRequestDTO {

  @JsonProperty("name")
  @NotNull(message = "Evaluator name cannot be null")
  @Schema(description = "Name of the LLM evaluator", example = "Quality Evaluator")
  protected String name;

  @JsonProperty("output_format_type")
  @Schema(
      description = "Output format type of the LLM evaluator",
      example = "Json",
      allowableValues = {"Json", "String", "Double", "Integer", "Boolean", "Choices"})
  protected ScoreType outputFormateType;

  @JsonProperty("description")
  @Schema(
      description = "Description of the LLM evaluator",
      example = "Evaluates the quality of the response")
  protected String description;

  @JsonProperty("variables")
  @Schema(description = "List of variables used by the LLM evaluator")
  protected List<EvaluatorVariableDTO> variables;

  @JsonProperty("model_id")
  @NotNull(message = "Evaluator needs a model id")
  @Schema(description = "ID of the model used by the LLM evaluator", example = "model-123")
  protected String modelId;

  @JsonProperty("prompts")
  @NotEmpty(message = "Prompt cannot be empty")
  @Schema(description = "List of prompts used by the LLM evaluator")
  protected List<Prompt> prompts;
}
