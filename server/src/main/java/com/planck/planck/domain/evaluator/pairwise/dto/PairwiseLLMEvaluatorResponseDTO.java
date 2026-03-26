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

package com.planck.planck.domain.evaluator.pairwise.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.evaluator.dto.EvaluatorVariableDTO;
import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.domain.model.dto.ModelDTO;
import com.planck.planck.domain.modelinput.dto.ModelInputDTO;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoreType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@Schema(description = "Response DTO for Pairwise LLM Evaluator")
public class PairwiseLLMEvaluatorResponseDTO {

  @JsonProperty("id")
  @Schema(
      description = "Unique identifier of the Pairwise LLM evaluator",
      example = "pairwise-llm-eval-123")
  private String id;

  @JsonProperty("name")
  @Schema(
      description = "Name of the Pairwise LLM evaluator",
      example = "Fluency Pairwise Evaluator")
  private String name;

  @JsonProperty("type")
  @Schema(
      description = "Type of the Pairwise LLM evaluator",
      example = "SYSTEM",
      allowableValues = {"SYSTEM", "USER"})
  private ScopeType type;

  @JsonProperty("output_format_type")
  @Schema(
      description = "Output format type of the Pairwise LLM evaluator",
      example = "Choices",
      allowableValues = {"Json", "String", "Double", "Integer", "Boolean", "Choices"})
  private ScoreType outputFormateType;

  @JsonProperty("description")
  @Schema(
      description = "Description of the Pairwise LLM evaluator",
      example = "Evaluates fluency between two responses")
  public String description;

  @JsonProperty("variables")
  @Schema(description = "List of variables used by the Pairwise LLM evaluator")
  public List<EvaluatorVariableDTO> variables;

  @JsonProperty("output_categories")
  @Schema(description = "List of output categories of the Pairwise LLM evaluator")
  public List<OutputCategoryDTO> outputCategories;

  @JsonProperty("model")
  @Schema(description = "Model used by the Pairwise LLM evaluator")
  private ModelDTO modelDTO;

  @JsonProperty("prompts")
  @Schema(description = "List of prompts used by the Pairwise LLM evaluator")
  private List<ModelInputDTO> prompts;

  @JsonProperty("created_at")
  @Schema(description = "Timestamp of when the evaluator was created")
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  @Schema(description = "Timestamp of when the evaluator was last updated")
  private Timestamp updatedAt;

  public PairwiseLLMEvaluatorResponseDTO(PairwiseLLMEvaluator pairwiseLlmEvaluator) {
    this.id = pairwiseLlmEvaluator.getId();
    this.name = pairwiseLlmEvaluator.getName();
    this.type = pairwiseLlmEvaluator.getType();
    this.outputFormateType = pairwiseLlmEvaluator.getOutputFormatType();
    this.description = pairwiseLlmEvaluator.getDescription();
    this.variables =
        pairwiseLlmEvaluator.getVariables() != null
            ? com.planck.planck.util.ObjectMapperUtil.convertJsonStringToList(
                pairwiseLlmEvaluator.getVariables(), EvaluatorVariableDTO.class)
            : null;
    this.outputCategories =
        pairwiseLlmEvaluator.getOutputCategories() != null
            ? com.planck.planck.util.ObjectMapperUtil.convertJsonStringToList(
                pairwiseLlmEvaluator.getOutputCategories(), OutputCategoryDTO.class)
            : null;
    this.modelDTO =
        pairwiseLlmEvaluator.getModel() != null
            ? new ModelDTO(pairwiseLlmEvaluator.getModel())
            : null;
    this.prompts =
        pairwiseLlmEvaluator.getInputs() != null
            ? pairwiseLlmEvaluator.getInputs().stream()
                .map(ModelInputDTO::new)
                .collect(Collectors.toList())
            : null;
    this.createdAt = pairwiseLlmEvaluator.getCreatedAt();
    this.updatedAt = pairwiseLlmEvaluator.getUpdatedAt();
  }
}
