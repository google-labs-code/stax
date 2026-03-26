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
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Schema(description = "Dataset CSV Upload Request DTO")
public class DataSetCsvUploadRequest {

  @Schema(description = "CSV file to upload", requiredMode = Schema.RequiredMode.REQUIRED)
  private MultipartFile file;

  @Schema(
      description = "Name of the column containing the model prompt (input)",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String inputColumnName;

  @Schema(
      description = "Name of the column containing the model response (output)",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String outputColumnName;

  @Schema(
      description = "Name of the column containing the expected output (optional)",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String expectedOutputColumnName; // Optional, if you have an expected output

  @Schema(description = "Name for the new dataset", requiredMode = Schema.RequiredMode.REQUIRED)
  private String datasetName;

  @Schema(
      description = "Description for the new dataset",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String datasetDescription;
}
