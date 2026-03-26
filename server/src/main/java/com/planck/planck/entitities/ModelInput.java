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
import com.planck.planck.enums.InputRole;
import com.planck.planck.llmproviders.dto.Prompt;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "model_input")
@NoArgsConstructor
@Getter
@Setter
public class ModelInput implements Tagable {

  @Transient private final String ID_PREFIX = "model-input-";

  @Id
  @JsonProperty("id")
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  // TODO: Relationship seems to be unused, potential cleanup
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = true)
  private ModelInput modelInput;

  @Column(name = "text", columnDefinition = "longtext")
  private String text;

  @Column(name = "role", length = 50, nullable = false)
  private InputRole role;

  @Column(columnDefinition = "longtext", nullable = true)
  private String expectedOutput;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = true, columnDefinition = "JSON")
  private Map<String, String> variables;

  @CreationTimestamp private Timestamp createdAt;
  @UpdateTimestamp private Timestamp updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)
  private User user;

  @Override
  public String getEntityId() {
    return this.id;
  }

  public ModelInput(Prompt prompt, ModelInput modelInput, User user) {
    this.user = user;
    this.text = prompt.getText();
    this.role = prompt.getRole();
    this.modelInput = modelInput;
  }

  public ModelInput(Prompt prompt, User user) {
    this.user = user;
    this.text = prompt.getText();
    this.role = prompt.getRole();
  }

  public ModelInput(Prompt prompt, User user, Map<String, String> variables) {
    this(prompt, user);
    this.variables = variables;
  }

  public ModelInput deepCopy() {
    ModelInput copy = new ModelInput();
    copy.setExpectedOutput(this.expectedOutput);
    copy.setVariables(this.variables);
    copy.setRole(this.role);
    copy.setText(this.text);
    copy.setUser(this.user);
    return copy;
  }
}
