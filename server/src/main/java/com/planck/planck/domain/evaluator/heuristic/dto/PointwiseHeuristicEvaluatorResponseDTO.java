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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.entitities.PointwiseHeuristicEvaluator;
import com.planck.planck.enums.CriteriaType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointwiseHeuristicEvaluatorResponseDTO {

  @JsonProperty("id")
  @Schema(
      description = "Unique identifier of the heuristic evaluator",
      example = "heuristic-eval-123")
  private String id;

  @JsonProperty("name")
  @Schema(description = "Name of the heuristic evaluator", example = "Medical Terminology Check")
  private String name;

  @JsonProperty("criteria")
  @Schema(description = "The evaluation criteria (sentence or regex)", example = "Hypertension")
  private String criteria;

  @JsonProperty("criteria_type")
  @Schema(
      description = "The type of criteria (CONTAINS or REGEX)",
      example = "CONTAINS",
      allowableValues = {"CONTAINS", "REGEX"})
  private CriteriaType criteriaType;

  @JsonProperty("deprecated")
  @Schema(description = "Whether the evaluator is deprecated or not", example = "false")
  private boolean deprecated;

  @JsonProperty("created_at")
  @Schema(description = "Timestamp of when the evaluator was created")
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  @Schema(description = "Timestamp of when the evaluator was last updated")
  private Timestamp updatedAt;

  public PointwiseHeuristicEvaluatorResponseDTO(PointwiseHeuristicEvaluator evaluator) {
    this.id = evaluator.getId();
    this.name = evaluator.getName();
    this.criteria = evaluator.getCriteria();
    this.criteriaType = evaluator.getCriteriaType();
    this.deprecated = evaluator.isDeprecated();
    this.createdAt = evaluator.getCreatedAt();
    this.updatedAt = evaluator.getUpdatedAt();
  }
}
