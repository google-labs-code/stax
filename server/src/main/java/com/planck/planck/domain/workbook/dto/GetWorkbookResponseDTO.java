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

package com.planck.planck.domain.workbook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response message for listing Workbook Rows, conforming to AIP-132. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetWorkbookResponseDTO {

  public GetWorkbookResponseDTO(
      List<ChatWorkbookRowDTO> workbookDTO, String nextPageToken, int totalElements) {
    this.workbookRows = workbookDTO;
    this.nextPageToken = nextPageToken;
    this.totalSize = totalElements;

    if (!workbookDTO.stream().anyMatch(row -> row.getSystemInstructions() != null))
      this.emptyColumns.add("system_instructions");

    if (!workbookDTO.stream().anyMatch(row -> row.getExpectedOutput() != null))
      this.emptyColumns.add("expected_output");

    if (!workbookDTO.stream()
        .anyMatch(row -> row.getVariables() != null && !row.getVariables().isEmpty()))
      this.emptyColumns.add("variables");

    if (!workbookDTO.stream().anyMatch(row -> row.getTags() != null && !row.getTags().isEmpty()))
      this.emptyColumns.add("tags");
  }

  @JsonProperty("workbook_rows")
  @Schema(description = "The workbook rows from the specified project.")
  private List<ChatWorkbookRowDTO> workbookRows;

  @JsonProperty("next_page_token")
  @Schema(
      description =
          "A token which can be sent as `page_token` to retrieve the next page. If this field is omitted, there are no subsequent pages.")
  private String nextPageToken;

  @JsonProperty("total_size")
  @Schema(description = "The total number of rows matching the query, possibly estimated.")
  private Integer totalSize;

  @JsonProperty("empty_columns")
  @Schema(
      description = "Contains a list of columns, for which the current workbook page has no data.")
  private List<String> emptyColumns = new ArrayList<>();
}
