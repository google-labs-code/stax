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
import com.planck.planck.enums.TagLinkTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tag_link")
@NoArgsConstructor
@Getter
@Setter
public class TagLink {

  private static final String ID_PREFIX = "tag-link-";

  @Id
  @Column(name = "id", unique = true, nullable = false)
  @JsonProperty("id")
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @ManyToOne
  @JoinColumn(name = "tag_id", nullable = false)
  private Tag tag;

  @Column(name = "target_id", nullable = false)
  private String targetId;

  public String getTargetId() {
    return targetId;
  }

  @Column(name = "target_type", nullable = false)
  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private String targetType;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @CreationTimestamp
  @JsonProperty("created_at")
  private Timestamp createdAt;

  @UpdateTimestamp
  @JsonProperty("updated_at")
  private Timestamp updatedAt;

  public void setTargetType(TagLinkTargetType targetType) {
    this.targetType = targetType.name();
  }

  public TagLinkTargetType getTargetType() {
    return TagLinkTargetType.valueOf(targetType);
  }

  public TagLink(Tag tag, String targetId, TagLinkTargetType targetType, User user) {
    this.tag = tag;
    this.targetId = targetId;
    this.targetType = targetType.name();
    this.user = user;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || getClass() != other.getClass()) return false;
    TagLink otherTagLink = (TagLink) other;
    return Objects.equals(id, otherTagLink.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
