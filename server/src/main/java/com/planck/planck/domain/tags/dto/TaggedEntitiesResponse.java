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

package com.planck.planck.domain.tags.dto;

import com.planck.planck.domain.analytics.evaluation.dto.EvaluationMonitoringDTO;
import com.planck.planck.domain.analytics.inference.dto.InferenceMonitoringDTO;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.enums.TagLinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class TaggedEntitiesResponse {

  public TaggedEntitiesResponse(Map<TagLinkTargetType, List<Object>> result) {
    List<Object> chatTurnObjects = result.get(TagLinkTargetType.CHAT_TURN);
    if (chatTurnObjects != null) {
      this.chatTurns = new ArrayList<>();
      for (Object obj : chatTurnObjects) {
        this.chatTurns.add((ChatTurnDTO) obj);
      }
    }

    List<Object> chatTurnByChatObjects = result.get(TagLinkTargetType.CHAT);
    if (chatTurnByChatObjects != null) {
      this.chatTurnsByChat = new ArrayList<>();
      for (Object obj : chatTurnByChatObjects) {
        this.chatTurnsByChat.add((ChatTurnDTO) obj);
      }
    }

    List<Object> inferenceMonitoringObjects = result.get(TagLinkTargetType.INFERENCE_MONITORING);
    if (inferenceMonitoringObjects != null) {
      this.inferenceMonitorings = new ArrayList<>();
      for (Object obj : inferenceMonitoringObjects) {
        this.inferenceMonitorings.add((InferenceMonitoringDTO) obj);
      }
    }

    List<Object> evaluationMonitoringObjects = result.get(TagLinkTargetType.INFERENCE_MONITORING);
    if (evaluationMonitoringObjects != null) {
      this.evaluationMonitorings = new ArrayList<>();
      for (Object obj : evaluationMonitoringObjects) {
        this.evaluationMonitorings.add((EvaluationMonitoringDTO) obj);
      }
    }
  }

  @Schema(description = "List of tagged chat turns")
  private List<ChatTurnDTO> chatTurns;

  @Schema(description = "List of tagged chat turns based on the chat id")
  private List<ChatTurnDTO> chatTurnsByChat;

  @Schema(description = "List of tagged inference monitorings")
  private List<InferenceMonitoringDTO> inferenceMonitorings;

  @Schema(description = "List of tagged evaluation monitorings")
  private List<EvaluationMonitoringDTO> evaluationMonitorings;
}
