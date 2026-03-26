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

package com.planck.planck.domain.evaluator.llm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.evaluator.dto.EvaluatorVariableDTO;
import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.domain.model.dto.ModelDTO;
import com.planck.planck.domain.modelinput.dto.ModelInputDTO;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoreType;
import com.planck.planck.util.ObjectMapperUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@Schema(description = "Response DTO for LLM Evaluator")
public class LLMEvaluatorResponseDTO {

  @JsonProperty("id")
  @Schema(description = "Unique identifier of the LLM evaluator", example = "llm-eval-123")
  private String id;

  @JsonProperty("name")
  @Schema(description = "Name of the LLM evaluator", example = "Quality Evaluator")
  private String name;

  @JsonProperty("evaluation_type")
  @Schema(
      description = "Evaluation type of the LLM evaluator",
      example = "POINTWISE",
      allowableValues = {"POINTWISE", "SXS"})
  EvaluationType evaluationType;

  @JsonProperty("type")
  @Schema(
      description = "Type of the LLM evaluator",
      example = "SYSTEM",
      allowableValues = {"SYSTEM", "USER"})
  private ScopeType type;

  @JsonProperty("output_format_type")
  @Schema(
      description = "Output format type of the LLM evaluator",
      example = "Json",
      allowableValues = {"Json", "String", "Double", "Integer", "Boolean", "Choices"})
  private ScoreType outputFormateType;

  @JsonProperty("description")
  @Schema(
      description = "Description of the LLM evaluator",
      example = "Evaluates the quality of the response")
  public String description;

  @JsonProperty("variables")
  @Schema(description = "List of variables used by the LLM evaluator")
  public List<EvaluatorVariableDTO> variables;

  @JsonProperty("output_categories")
  @Schema(description = "List of output categories of the LLM evaluator")
  public List<OutputCategoryDTO> outputCategories;

  @JsonProperty("model")
  @Schema(description = "Model used by the LLM evaluator")
  private ModelDTO modelDTO;

  @JsonProperty("prompts")
  @Schema(description = "List of prompts used by the LLM evaluator")
  private List<ModelInputDTO> prompts;

  @JsonProperty("created_at")
  @Schema(description = "Timestamp of when the evaluator was created")
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  @Schema(description = "Timestamp of when the evaluator was last updated")
  private Timestamp updatedAt;

  @JsonProperty("deprecated")
  @Schema(description = "Whether the evaluator is deprecated or not")
  private boolean deprecated;

  public LLMEvaluatorResponseDTO(LLMEvaluator llmEvaluator) {

    this.id = llmEvaluator.getId();
    this.name = llmEvaluator.getName();
    this.type = llmEvaluator.getType();
    this.outputFormateType = llmEvaluator.getOutputFormatType();
    this.description = llmEvaluator.getDescription();
    this.evaluationType = EvaluationType.POINTWISE;

    this.outputCategories =
        ObjectMapperUtil.convertJsonStringToList(
            llmEvaluator.getOutputCategories(), OutputCategoryDTO.class);
    this.variables =
        ObjectMapperUtil.convertJsonStringToList(
            llmEvaluator.getVariables(), EvaluatorVariableDTO.class);
    this.modelDTO = new ModelDTO(llmEvaluator.getModel());

    this.prompts = llmEvaluator.getInputs().stream().map(ModelInputDTO::new).toList();

    this.createdAt = llmEvaluator.getCreatedAt();
    this.updatedAt = llmEvaluator.getUpdatedAt();

    this.deprecated = llmEvaluator.isDeprecated();
  }

  public LLMEvaluatorResponseDTO(PairwiseLLMEvaluator pairwiseLlmEvaluator) {

    this.id = pairwiseLlmEvaluator.getId();
    this.name = pairwiseLlmEvaluator.getName();
    this.type = pairwiseLlmEvaluator.getType();
    this.outputFormateType = pairwiseLlmEvaluator.getOutputFormatType();
    this.description = pairwiseLlmEvaluator.getDescription();
    this.evaluationType = EvaluationType.SXS;

    this.outputCategories =
        ObjectMapperUtil.convertJsonStringToList(
            pairwiseLlmEvaluator.getOutputCategories(), OutputCategoryDTO.class);
    this.variables =
        ObjectMapperUtil.convertJsonStringToList(
            pairwiseLlmEvaluator.getVariables(), EvaluatorVariableDTO.class);
    this.modelDTO = new ModelDTO(pairwiseLlmEvaluator.getModel());

    this.prompts = pairwiseLlmEvaluator.getInputs().stream().map(ModelInputDTO::new).toList();

    this.createdAt = pairwiseLlmEvaluator.getCreatedAt();
    this.updatedAt = pairwiseLlmEvaluator.getUpdatedAt();

    this.deprecated = pairwiseLlmEvaluator.isDeprecated();
  }
}
