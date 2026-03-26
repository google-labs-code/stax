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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.enums.TagType;
import java.sql.Timestamp;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
public class TagResponse {

  @JsonProperty("tag_id")
  private String tagId;

  @JsonProperty("tag_name")
  private String tagName;

  @JsonProperty("created_at")
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  private Timestamp updatedAt;

  @JsonIgnore private String responseId;

  private String color;

  private TagType type;

  @JsonProperty("used_count")
  private int usedCount;

  public TagResponse(
      String tagId,
      String tagName,
      String color,
      TagType type,
      Timestamp createdAt,
      int usedCount,
      Timestamp updatedAt) {
    this.tagId = tagId;
    this.tagName = tagName;
    this.color = color;
    this.type = type;
    this.createdAt = createdAt;
    this.usedCount = usedCount;
    this.updatedAt = updatedAt;
  }

  public TagResponse(
      String tagId,
      String tagName,
      String color,
      TagType type,
      Timestamp createdAt,
      String responseId,
      int usedCount,
      Timestamp updatedAt) {
    this.tagId = tagId;
    this.tagName = tagName;
    this.color = color;
    this.type = type;
    this.createdAt = createdAt;
    this.responseId = responseId;
    this.usedCount = usedCount;
    this.updatedAt = updatedAt;
  }

  public TagResponse(
      String tagId,
      String tagName,
      String color,
      TagType type,
      Timestamp createdAt,
      int usedCount) {
    this.tagId = tagId;
    this.tagName = tagName;
    this.color = color;
    this.type = type;
    this.createdAt = createdAt;
    this.usedCount = usedCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TagResponse that = (TagResponse) o;
    return Objects.equals(tagId, that.tagId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tagId);
  }
}
