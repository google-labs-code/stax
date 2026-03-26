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
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.ArrayList;
import java.util.HashMap;
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

@Schema(description = "Represents a chat conversation")
@Entity
@Table(name = "chat")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Chat {

  @Transient public static final String ID_PREFIX = "chat-";

  @Id
  @JsonProperty("id")
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @ManyToOne(optional = false)
  @JoinColumn(name = "container_id", nullable = false)
  private EvaluationContainer container;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ChatTurn> turns = new ArrayList<>();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "variables", columnDefinition = "json")
  private Map<String, String> variables = new HashMap<>();

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  public ChatTurn getLatestTurn() {
    if (getTurns().isEmpty()) {
      return null;
    }
    return getTurns().get(getTurns().size() - 1);
  }

  public void addTurn(ChatTurn turn) {
    getTurns().add(turn);
    turn.setChat(this);
  }
}
