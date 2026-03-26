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

import com.planck.planck.domain.evaluator.human.dto.HumanCategoryOption;
import com.planck.planck.enums.LinkedEntityType;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.converter.LinkedEntityTypeConverter;
import com.planck.planck.util.converter.ScopeTypeConverter;
import com.planck.planck.util.converter.ScoringMechanismTypeConverter;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "human_evaluators")
@Getter
@Setter
@NoArgsConstructor
public class HumanEvaluator {

  @Transient private final String ID_PREFIX = "human-eval-";

  @Id
  @Column(unique = true, nullable = false)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id") // Nullable for system evaluators
  private User user;

  @Column(name = "scope_type", nullable = false)
  @Convert(converter = ScopeTypeConverter.class)
  private ScopeType scopeType;

  @Column(name = "scoring_mechanism_type", nullable = false)
  @Convert(converter = ScoringMechanismTypeConverter.class)
  private ScoringMechanismType scoringMechanismType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "categories", columnDefinition = "TEXT")
  private String categories;

  @Column(name = "comments", columnDefinition = "TEXT")
  private String comments;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "range_options", columnDefinition = "TEXT")
  private String rangeOptions;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Timestamp updatedAt;

  @Column(name = "is_deprecated", nullable = false)
  private boolean deprecated = false;

  @Column(name = "associated_entity_id", nullable = true)
  private String associatedEntityId;

  @Column(name = "linked_entity_type", nullable = false)
  @Convert(converter = LinkedEntityTypeConverter.class)
  private LinkedEntityType linkedEntityType;

  @Transient
  public List<HumanCategoryOption> getActiveCategories() {
    List<HumanCategoryOption> allCategories =
        ObjectMapperUtil.convertJsonStringToList(categories, HumanCategoryOption.class);
    if (allCategories == null) {
      return new ArrayList<>();
    }
    return allCategories;
  }

  public void addCategory(HumanCategoryOption category) {
    List<HumanCategoryOption> allCategories =
        ObjectMapperUtil.convertJsonStringToList(categories, HumanCategoryOption.class);
    if (allCategories == null) {
      allCategories = new ArrayList<>();
    }
    allCategories.add(category);
    this.categories = ObjectMapperUtil.toJsonString(allCategories);
  }

  public void removeCategory(String categoryId) {
    List<HumanCategoryOption> allCategories =
        ObjectMapperUtil.convertJsonStringToList(categories, HumanCategoryOption.class);
    if (allCategories != null) {
      allCategories.removeIf(cat -> cat.getId().equals(categoryId));
      this.categories = ObjectMapperUtil.toJsonString(allCategories);
    }
  }

  public void updateCategory(
      String categoryId, String newName, String newDescription, Double newScore) {
    List<HumanCategoryOption> allCategories =
        ObjectMapperUtil.convertJsonStringToList(categories, HumanCategoryOption.class);
    if (allCategories != null) {
      allCategories.stream()
          .filter(cat -> cat.getId().equals(categoryId))
          .findFirst()
          .ifPresent(
              cat -> {
                cat.setCategoryName(newName);
                cat.setDescription(newDescription);
                cat.setScore(newScore);
              });
      this.categories = ObjectMapperUtil.toJsonString(allCategories);
    }
  }
}
