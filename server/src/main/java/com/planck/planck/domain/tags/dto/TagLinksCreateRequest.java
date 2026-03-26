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

package com.planck.planck.domain.tags.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.enums.TagLinkTargetType;
import io.micrometer.common.lang.NonNull;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class TagLinksCreateRequest {
  @NonNull
  @Size(min = 1, message = "Minimum of one tag is required")
  @JsonProperty("tag_ids")
  @Schema(
      description = "List of Tag IDs to link to the entities",
      example = "[\"tag-uuid4-1\", \"tag-uuid4-2\"]",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<String> tagIds;

  @JsonProperty("entity_type")
  @NonNull
  @Schema(
      description = "Type of the target entity",
      example = "CHAT_TURN",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private TagLinkTargetType entityType;

  @NonNull
  @Size(min = 1, message = "Minimum of one target entity is required")
  @JsonProperty("entity_ids")
  @Schema(
      description = "List of target entity IDs",
      example = "[\"chat-turn-uuid4-1\", \"chat-turn-uuid4-2\"]",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<String> entityIds;
}
