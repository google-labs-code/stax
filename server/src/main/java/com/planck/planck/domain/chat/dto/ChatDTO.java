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

package com.planck.planck.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.Taggable;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.enums.TagLinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Data Transfer Object for Chat information")
public class ChatDTO implements Taggable {

  @JsonProperty("chat_id")
  @Schema(description = "Unique identifier of the chat")
  private String id;

  @JsonProperty("chat_turns")
  @Schema(description = "List of chat turns in the chat", required = true)
  List<ChatTurnDTO> chatTurns;

  @JsonProperty("tags")
  @Schema(description = "List of tags associated with this chat")
  private List<TagDTO> tags;

  @JsonProperty("variables")
  @Schema(description = "Map of variables associated with this chat")
  private Map<String, String> variables;

  public ChatDTO(Chat chat) {
    this.id = chat.getId();
    this.variables = chat.getVariables();
    for (ChatTurn chatTurn : chat.getTurns()) {
      this.chatTurns.add(new ChatTurnDTO(chatTurn));
    }
  }

  public ChatDTO(List<ChatTurn> chatTurns) {

    this.chatTurns = new ArrayList<>(chatTurns.size());

    chatTurns.stream()
        .findFirst()
        .ifPresent(
            firstTurn -> {
              Chat chat = firstTurn.getChat();
              this.id = chat.getId();
              this.variables = chat.getVariables();
            });

    for (ChatTurn chatTurn : chatTurns) {
      this.chatTurns.add(new ChatTurnDTO(chatTurn));
    }
  }

  @Override
  public TagLinkTargetType getTagLinkTargetType() {
    return TagLinkTargetType.CHAT;
  }
}
