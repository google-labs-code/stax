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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CopyChatsResponseDTO {

  @Schema(
      description = "A summary message indicating the overall result of the copy operation.",
      example = "Copy operation completed. 8 of 10 chats copied successfully.")
  private String message;

  @Schema(description = "Number of chats successfully copied.")
  private int successfullyCopiedCount;

  @Schema(description = "Number of chats that failed to copy.")
  private int failedToCopyCount;

  @Schema(
      description =
          "A map where keys are the original chat IDs and values are the new IDs of the successfully copied chats.",
      example = "{\"chat_abc1\": \"new_chat_xyz7\", \"chat_def2\": \"new_chat_uvw8\"}")
  private Map<String, String> copiedChatIdMappings; // <OriginalChatId, NewCopiedChatId>

  @Schema(description = "A list of details for chats that failed to copy, if any.")
  private List<CopyChatErrorDetailDTO> errors;
}
