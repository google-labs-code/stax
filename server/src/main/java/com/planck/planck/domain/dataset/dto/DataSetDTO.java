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

package com.planck.planck.domain.dataset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.entitities.DataSet;
import com.planck.planck.enums.EvaluationType;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
@Schema(description = "Dataset DTO")
public class DataSetDTO {
  @Schema(description = "Dataset ID", accessMode = Schema.AccessMode.READ_ONLY)
  private String id;

  @Schema(description = "Dataset name", accessMode = Schema.AccessMode.READ_WRITE)
  private String name;

  @Schema(
      description = "Dataset Type. Auto-generated if missing, with default value POINTWISE",
      example = "POINTWISE",
      allowableValues = {"POINTWISE", "SXS"})
  EvaluationType type = EvaluationType.POINTWISE;

  @Schema(description = "Dataset description", accessMode = Schema.AccessMode.READ_WRITE)
  private String description;

  @Schema(description = "Dataset creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
  Timestamp createdAt;

  // Output only field - ignored if included in input
  @Schema(description = "Dataset last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
  Timestamp updatedAt;

  public DataSetDTO(DataSet createdDataSet) {
    this.name = createdDataSet.getName();
    this.description = createdDataSet.getDescription();
    this.createdAt = createdDataSet.getCreatedAt();
    this.updatedAt = createdDataSet.getUpdatedAt();
    this.type = createdDataSet.getEvaluationType();
    this.id = createdDataSet.getId();
  }
}
