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
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoreType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class LLMEvaluator {
  @JsonProperty("evaluator_id")
  private String scorerId;

  @JsonProperty("scope_type")
  public ScopeType scopeType;

  @JsonProperty("evaluator_name")
  private String ScorerName;

  @JsonProperty("evaluator_prompt")
  private String scorerPrompt;

  @JsonProperty("output_format_type")
  private ScoreType outputFormateType;

  @JsonProperty("score_description")
  public String scoreDescription;

  @JsonProperty("evaluator_variables")
  public List<EvaluatorVariableDTO> scorerVariables;

  @JsonProperty("output_categories")
  public List<OutputCategoryDTO> outputCategories;
}
