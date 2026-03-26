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
import com.planck.planck.entitities.TagLink;
import com.planck.planck.enums.TagLinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(name = "TagLink")
@NoArgsConstructor
public class TagLinkDTO {
  @JsonProperty("id")
  @Schema(description = "Id of the tag link", example = "tag-link-uuid4")
  String id;

  @JsonProperty("tag_id")
  @Schema(description = "Id of the tag which is linked to the target entity", example = "tag-uuid4")
  String tagId;

  @JsonProperty("target_entity")
  @Schema(
      description = "Type of the target entity",
      example = "CHAT_TURN",
      allowableValues = {"CHAT_TURN"})
  TagLinkTargetType targetType;

  @JsonProperty("target_entity_id")
  @Schema(description = "Id of the target entity", example = "chat-turn-uuid4")
  String targetId;

  @Schema(description = "Creation timestamp of the tag link", example = "2025-04-22T14:30:00Z")
  @JsonProperty("created_at")
  private Timestamp createdAt;

  @Schema(description = "Last update timestamp of the tag link", example = "2025-04-22T14:30:00Z")
  @JsonProperty("updated_at")
  private Timestamp updatedAt;

  public TagLinkDTO(TagLink tagLink) {
    this.id = tagLink.getId();
    this.tagId = tagLink.getTag().getTagId();
    this.targetType = tagLink.getTargetType();
    this.targetId = tagLink.getTargetId();
    this.createdAt = tagLink.getCreatedAt();
    this.updatedAt = tagLink.getUpdatedAt();
  }
}
