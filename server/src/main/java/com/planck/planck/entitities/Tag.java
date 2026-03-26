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

package com.planck.planck.entitities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue.Builder;
import com.planck.planck.enums.TagType;
import com.planck.planck.util.converter.TagTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "tag",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"tagName", "user"})})
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Tag {

  @Transient private static final String ID_PREFIX = "tag-";

  @Id
  @Column(name = "tag_id", unique = true, nullable = false)
  @JsonProperty("tag_id")
  private String tagId;

  public Tag(String name, User user, TagType tagType) {
    this.tagName = name;
    this.user = user;
    this.type = tagType;
  }

  @PrePersist
  public void prePersist() {
    if (tagId == null) {
      this.tagId = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @JsonProperty("tag_name")
  private String tagName;

  @Convert(converter = TagTypeConverter.class)
  private TagType type;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  private String color;

  @CreationTimestamp
  @JsonProperty("created_at")
  private Timestamp createdAt;

  @UpdateTimestamp
  @JsonProperty("updated_at")
  private Timestamp updatedAt;

  @Column(name = "used_count", columnDefinition = "int default 0")
  private int usedCount;
}
