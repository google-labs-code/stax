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
import com.planck.planck.util.PromptUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Schema(description = "Represents a single turn in a chat conversation")
@Entity
@Table(name = "chat_turns")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChatTurn {

  @Transient private final String ID_PREFIX = "chat-turn-";

  @Id
  @JsonProperty("id")
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @ManyToOne
  @JoinColumn(name = "chat_id", nullable = false)
  private Chat chat;

  @Schema(description = "Sequence number of the chat turn within the chat", required = true)
  @Column(name = "sequence_id", nullable = false)
  private Integer sequenceId;

  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "turn_inputs",
      joinColumns = @JoinColumn(name = "turn_id"),
      inverseJoinColumns = @JoinColumn(name = "input_id"))
  private List<ModelInput> inputs;

  @ManyToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "model_response_id", referencedColumnName = "id", nullable = true)
  private ModelResponse modelResponse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  @OneToOne(mappedBy = "chatTurn", cascade = CascadeType.ALL, orphanRemoval = true)
  private InferenceStatus inferenceStatus;

  @Transient private String effectiveSystemInstruction;

  /**
   * Copies sequenceId, user and InferenceStatus from an existing chat turn. In the future, if
   * related entities are properly fixed to have bi-directional relationship they can also be added
   * to this copy method instead of using the ChatTurnCopyService. Generates a new id, and expects
   * chatId and project/dataset to be set in advance
   *
   * @param existing ChatTurn to be copied from
   */
  public void copyFromExisting(ChatTurn existing) {
    this.prePersist();
    this.sequenceId = existing.getSequenceId();
    this.user = existing.getUser();
    if (existing.getInferenceStatus() != null) {
      this.inferenceStatus = existing.getInferenceStatus().deepCopy(this);
    }
  }

  public ChatTurn(List<ModelInput> inputs, ModelResponse modelResponse, User user, Chat chat) {
    this.inputs = inputs;
    this.modelResponse = modelResponse;
    this.user = user;
    this.chat = chat;
    this.sequenceId = 0;
  }

  public ChatTurn(
      List<ModelInput> inputs, ModelResponse modelResponse, User user, ChatTurn previousChatTurn) {
    this.inputs = inputs;
    this.modelResponse = modelResponse;
    this.user = user;
    this.chat = previousChatTurn.getChat();
    this.sequenceId = previousChatTurn.getSequenceId() + 1;
  }

  public ModelInput getLastUserInput() {
    if (this.inputs == null) {
      return null;
    }

    return this.inputs.stream()
        .filter(input -> input.getRole() == InputRole.USER)
        .max(Comparator.comparing(ModelInput::getCreatedAt))
        .orElse(null);
  }

  public String getRawInputText() {
    if (this.inputs == null) {
      return null;
    }

    List<ModelInput> userInputs =
        this.inputs.stream()
            .filter(input -> input.getRole() == InputRole.USER)
            .collect(Collectors.toList());

    if (userInputs.isEmpty()) {
      return null;
    }

    List<String> nonNullTexts =
        userInputs.stream()
            .map(ModelInput::getText)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return nonNullTexts.isEmpty() ? null : String.join("\n", nonNullTexts);
  }

  public String getEnrichedInputText() {
    if (this.inputs == null) {
      return null;
    }

    List<ModelInput> userInputs =
        this.inputs.stream()
            .filter(input -> input.getRole() == InputRole.USER)
            .collect(Collectors.toList());

    if (userInputs.isEmpty()) {
      return null;
    }

    List<String> enrichedTexts =
        userInputs.stream()
            .map(input -> PromptUtil.enrichTextForResponse(input.getText(), input.getVariables()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return enrichedTexts.isEmpty() ? null : String.join("\n", enrichedTexts);
  }

  public ChatTurn(List<ModelInput> inputs, User user, Chat chat) {
    this(inputs, null, user, chat);
  }

  public ChatTurn(List<ModelInput> inputs, User user, ChatTurn previousChatTurn) {
    this(inputs, null, user, previousChatTurn);
  }

  public ChatTurn(
      User user, Chat chat, int sequenceId, ModelInput modelInput, ModelResponse modelResponse) {
    this.user = user;
    this.chat = chat;
    this.sequenceId = sequenceId;
    this.inputs = (modelInput != null) ? List.of(modelInput) : Collections.emptyList();
    this.modelResponse = modelResponse;
  }
}
