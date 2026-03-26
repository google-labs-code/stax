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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.enums.ChatTurnContainerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyChatsRequestDTO {

  @Schema(
      description = "The ID of the source object from which chats will be copied.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "proj_source_12345")
  @NotNull(message = "Source ID cannot be null.")
  @JsonProperty("source_id")
  private String sourceId;

  @Schema(
      description = "The type of the source object (PROJECT or DATASET).",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "PROJECT")
  @NotNull(message = "Source type cannot be null.")
  @JsonProperty("source_type")
  private ChatTurnContainerType sourceType;

  @Schema(
      description = "The ID of the objects to which chats will be copied.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "proj_target_67890")
  @NotNull(message = "Target ID cannot be null.")
  @JsonProperty("target_id")
  private String targetId;

  @Schema(
      description = "The type of the target object (PROJECT or DATASET).",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "DATASET")
  @NotNull(message = "Target type cannot be null.")
  @JsonProperty("target_type")
  private ChatTurnContainerType targetType;
}
