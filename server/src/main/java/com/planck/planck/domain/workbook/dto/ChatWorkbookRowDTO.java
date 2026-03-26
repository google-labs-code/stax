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

package com.planck.planck.domain.workbook.dto;

import com.planck.planck.domain.model.dto.ModelTokens;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.enums.TagLinkTargetType;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatWorkbookRowDTO extends BaseWorkbookRowDTO {

  public ChatWorkbookRowDTO(Chat chat) {
    super(chat.getLatestTurn());

    if (chat.getTurns().isEmpty()) {
      return;
    }

    setInferenceLatency(calculateAverageLatency(chat.getTurns()));

    int totalInputTokens = calculateTotalInputTokens(chat.getTurns());
    int totalOutputTokens = calculateTotalOutputTokens(chat.getTurns());
    int totalTokens = calculateTotalTokens(chat.getTurns());
    setInferenceTokens(new ModelTokens(totalInputTokens, totalOutputTokens, totalTokens));
  }

  private double calculateAverageLatency(List<ChatTurn> turns) {
    double avgLatency =
        turns.stream()
            .map(ChatTurn::getModelResponse)
            .filter(Objects::nonNull)
            .map(ModelResponse::getInferenceMonitoring)
            .filter(Objects::nonNull)
            .mapToDouble(InferenceMonitoring::getTurnTimeTaken)
            .average()
            .orElse(0.0);
    return Math.round(avgLatency * 100.0) / 100.0;
  }

  private int calculateTotalInputTokens(List<ChatTurn> turns) {
    return turns.stream()
        .map(ChatTurn::getModelResponse)
        .filter(Objects::nonNull)
        .map(ModelResponse::getInferenceMonitoring)
        .filter(Objects::nonNull)
        .map(InferenceMonitoring::getTurnPromptTokens)
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private int calculateTotalOutputTokens(List<ChatTurn> turns) {
    return turns.stream()
        .map(ChatTurn::getModelResponse)
        .filter(Objects::nonNull)
        .map(ModelResponse::getInferenceMonitoring)
        .filter(Objects::nonNull)
        .map(InferenceMonitoring::getTurnCompletionTokens)
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private int calculateTotalTokens(List<ChatTurn> turns) {
    return turns.stream()
        .map(ChatTurn::getModelResponse)
        .filter(Objects::nonNull)
        .map(ModelResponse::getInferenceMonitoring)
        .filter(Objects::nonNull)
        .map(InferenceMonitoring::getTurnTotalTokens)
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .sum();
  }

  @Override
  public String getId() {
    return getChat_id();
  }

  @Override
  public TagLinkTargetType getTagLinkTargetType() {
    return TagLinkTargetType.CHAT;
  }
}
