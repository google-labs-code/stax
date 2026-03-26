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

import com.planck.planck.enums.CriteriaType;
import com.planck.planck.util.converter.CriteriaTypeConverter;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(name = "pointwise_heuristic_evaluator")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class PointwiseHeuristicEvaluator {

  @Transient private static final String ID_PREFIX = "heuristic-eval-";

  @Id
  @Column(unique = true, nullable = false)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "criteria")
  private String criteria;

  @Column(name = "criteria_type", nullable = false)
  @Convert(converter = CriteriaTypeConverter.class)
  private CriteriaType criteriaType;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "is_deprecated")
  private boolean deprecated = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Timestamp updatedAt;
}
