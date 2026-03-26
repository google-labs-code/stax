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

import com.planck.planck.enums.HumanSxsRating;
import com.planck.planck.util.converter.HumanSxsRatingConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sxs_human_feedback")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SXSHumanFeedback {

  @Transient private final String ID_PREFIX = "sxs-human-eval-";

  @Id
  @Column(unique = true, nullable = false)
  private String id;

  @Column(name = "pair_id", nullable = false)
  private String pairId;

  @Column(name = "project_id", nullable = false)
  private String projectId;

  @Column(name = "chat_turn_a", nullable = false)
  private String chatTurnA;

  @Column(name = "chat_turn_b", nullable = false)
  private String chatTurnB;

  @Convert(converter = HumanSxsRatingConverter.class)
  @Column(name = "human_sxs_rating")
  private HumanSxsRating humanSxsRating;

  @Column(name = "human_sxs_notes", columnDefinition = "TEXT")
  private String humanSxsNotes;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }
}
