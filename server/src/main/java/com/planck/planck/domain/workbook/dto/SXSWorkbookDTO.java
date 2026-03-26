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
import com.planck.planck.domain.project.dto.SXSChatRowDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response message for listing SXS Workbook Rows, conforming to AIP-132. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SXSWorkbookDTO {

  public SXSWorkbookDTO(List<SXSChatRowDTO> sxsRows, String nextPageToken, long totalElements) {
    this.sxsRows = sxsRows;
    this.nextPageToken = nextPageToken;
    this.totalSize = totalElements;
  }

  @JsonProperty("sxs_rows")
  @Schema(description = "The SXS workbook rows from the specified project.")
  private List<SXSChatRowDTO> sxsRows;

  @JsonProperty("next_page_token")
  @Schema(
      description =
          "A token which can be sent as `page_token` to retrieve the next page. If this field is omitted, there are no subsequent pages.")
  private String nextPageToken;

  @JsonProperty("total_size")
  @Schema(description = "The total number of rows matching the query, possibly estimated.")
  private Long totalSize;
}
