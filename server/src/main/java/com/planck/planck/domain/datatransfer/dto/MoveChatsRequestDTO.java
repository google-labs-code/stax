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
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveChatsRequestDTO extends MoveAllChatsRequestDTO {

  @Schema(
      description = "A list of chat IDs to be moved from the source object.",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "[\"chat_abc1\", \"chat_def2\"]")
  @NotEmpty(message = "List of chat IDs cannot be empty.")
  @Size(
      min = 1,
      max = 100,
      message = "Number of chat IDs must be between 1 and 100.") // Added size constraint in Future
  @JsonProperty("chat_ids")
  private List<String> chatIds;
}
