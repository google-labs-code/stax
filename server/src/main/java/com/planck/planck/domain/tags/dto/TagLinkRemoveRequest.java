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
import com.planck.planck.annotation.AtLeastOneFieldRequired;
import com.planck.planck.enums.TagLinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@AtLeastOneFieldRequired(
    fields = {"tagIds", "entityIds"},
    message = "Either tag_ids or entity_ids must be present")
public class TagLinkRemoveRequest {
  @Schema(
      description = "List of Tag IsD, all links to the tag ID will be deleted",
      example = "[\"tag-uuid4-1\", \"tag-uuid4-2\"]",
      requiredMode = Schema.RequiredMode.AUTO)
  @JsonProperty("tag_ids")
  private List<String> tagIds;

  @Schema(
      description = "List of target IDs. Must also specify targetType when used.",
      example = "[\"chat-turn-uuid4-1\", \"chat-turn-uuid4-2\"]",
      requiredMode = Schema.RequiredMode.AUTO)
  @JsonProperty("entity_ids")
  @Size(min = 1, message = "entity_ids must contain at least one element")
  private List<String> entityIds;

  @Schema(
      description =
          "Type of the target link to be deleted. Must be used in conjuction with target_id",
      example = "CHAT_TURN",
      requiredMode = Schema.RequiredMode.AUTO)
  @JsonProperty("entity_type")
  private TagLinkTargetType entityType;
}
