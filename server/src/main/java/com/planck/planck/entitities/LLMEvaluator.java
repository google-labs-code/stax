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

import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoreType;
import com.planck.planck.util.converter.ScopeTypeConverter;
import com.planck.planck.util.converter.ScoreTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Table(name = "llm_evaluator")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class LLMEvaluator {

  @Transient private final String ID_PREFIX = "llm-eval-";

  @Id
  @Column(unique = true, nullable = false)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @Column(name = "name", unique = true, nullable = false)
  private String name;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "llm_turn_inputs",
      joinColumns = @JoinColumn(name = "turn_id"),
      inverseJoinColumns = @JoinColumn(name = "input_id"))
  private List<ModelInput> inputs;

  @Column(name = "output_format_type", nullable = false)
  @Convert(converter = ScoreTypeConverter.class)
  private ScoreType outputFormatType;

  @Column(name = "type", nullable = false)
  @Convert(converter = ScopeTypeConverter.class)
  private ScopeType type;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "description", length = 1500, nullable = false)
  private String description;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "variables")
  private String variables;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "output_categories")
  private String outputCategories;

  @ManyToOne
  @JoinColumn(name = "model_id", nullable = false)
  private Model model;

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "is_deprecated")
  private boolean deprecated = false;
}
