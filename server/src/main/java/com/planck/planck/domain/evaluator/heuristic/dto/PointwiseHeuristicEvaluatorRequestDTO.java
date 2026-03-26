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

package com.planck.planck.domain.evaluator.heuristic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.enums.CriteriaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a Pointwise Heuristic Evaluator")
@JsonInclude(value = Include.NON_NULL)
public class PointwiseHeuristicEvaluatorRequestDTO {

  @NotNull(message = "Evaluator name cannot be null")
  @JsonProperty("name")
  @Schema(description = "Name of the Heuristic evaluator", example = "Quality Evaluator")
  private String name;

  @NotNull(message = "Criteria cannot be null")
  @JsonProperty("criteria")
  @Schema(
      description = "The text or regex to be used for evaluation",
      example = "This is the expected sentence.")
  private String criteria;

  @JsonProperty("criteria_type")
  @Schema(
      description = "The type of criteria (contains or regex)",
      example = "CONTAINS",
      allowableValues = {"CONTAINS", "REGEX"})
  private CriteriaType criteriaType = CriteriaType.CONTAINS;
}
