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
import com.planck.planck.domain.evaluator.dto.BaseLLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@Schema(description = "Request DTO for creating an LLM Evaluator")
@JsonInclude(value = Include.NON_NULL)
public class LLMEvaluatorRequestDTO extends BaseLLMEvaluatorRequestDTO {

  @Valid
  @JsonProperty("output_categories")
  @Schema(description = "List of output categories of the LLM evaluator")
  protected List<OutputCategoryDTO> outputCategories;
}
