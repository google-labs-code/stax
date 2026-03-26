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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sxs_evaluation_pair")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SxsEvaluationPair {

  @Transient private final String ID_PREFIX = "sxs-pair-";

  @Id
  @Column(unique = true, nullable = false)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "container_id", nullable = false)
  private EvaluationContainer container;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_a", nullable = false)
  private Chat chatA;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_b", nullable = true)
  private Chat chatB;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_turn_id_a", nullable = false)
  private ChatTurn chatTurnA;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_turn_id_b", nullable = true)
  private ChatTurn chatTurnB;

  @Column(name = "expected_output", columnDefinition = "LONGTEXT")
  private String expectedOutput;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "variables", columnDefinition = "json")
  private Map<String, String> variables;

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  @OneToMany(mappedBy = "pair", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PairwiseScore> pairwiseScores;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }
}
