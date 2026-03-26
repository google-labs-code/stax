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

package com.planck.planck.domain.datatransfer.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MoveChatsResponseDTO {

  @Schema(
      description = "A summary message indicating the overall result of the move operation.",
      example = "Move operation completed. 8 of 10 chats moved successfully.")
  private String message;

  @Schema(description = "Number of chats successfully moved.")
  private int successfullyMovedCount;

  @Schema(description = "Number of chats that failed to move.")
  private int failedToMoveCount;

  @Schema(description = "A list of details for chats that failed to move, if any.")
  private List<CopyChatErrorDetailDTO> errors;
}
