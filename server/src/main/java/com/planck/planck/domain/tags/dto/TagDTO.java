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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.entitities.Tag;
import com.planck.planck.enums.TagType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
@Schema(name = "Tag")
public class TagDTO implements Serializable {

  private static final long serialVersionUID = 6822277293482753178L;

  @Schema(description = "The ID of the Tag")
  @JsonProperty("id")
  private String id;

  @Schema(description = "Name of the tag", example = "myTag")
  @JsonProperty("name")
  private String name;

  @Schema(description = "Type of the tag", example = "USER")
  @JsonProperty("type")
  private TagType type;

  @Schema(description = "Color of the tag", example = "#FFFFFF")
  @JsonProperty("color")
  private String color;

  @Schema(description = "Creation timestamp of the tag", example = "2025-04-22T14:30:00Z")
  @JsonProperty("created_at")
  private Timestamp createdAt;

  @Schema(description = "Last update timestamp of the tag", example = "2025-04-22T14:30:00Z")
  @JsonProperty("updated_at")
  private Timestamp updatedAt;

  @Schema(description = "Number of times the tag has been used", example = "0")
  @JsonProperty("used_count")
  private int usedCount;

  public TagDTO(Tag tag) {
    this.id = tag.getTagId();
    this.name = tag.getTagName();
    this.color = tag.getColor();
    this.type = tag.getType();
    this.createdAt = tag.getCreatedAt();
    this.usedCount = tag.getUsedCount();
    this.updatedAt = tag.getUpdatedAt();
  }
}
