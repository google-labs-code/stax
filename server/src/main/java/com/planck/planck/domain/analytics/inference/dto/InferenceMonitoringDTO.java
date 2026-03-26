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

package com.planck.planck.domain.analytics.inference.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.Taggable;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.enums.TagLinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Data Transfer Object for Inference Monitoring information")
@AllArgsConstructor
public class InferenceMonitoringDTO implements Taggable {

  @Schema(description = "Unique identifier of the inference monitoring data", required = true)
  private String id;

  @JsonProperty("turn_latency")
  @Schema(description = "Latency of the turn inference", required = true)
  private Double turnTimeTaken;

  @JsonProperty("turn_prompt_tokens")
  @Schema(
      description = "Number of prompt tokens used in the inference for this turn",
      required = true)
  private Integer TurnPromptTokens;

  @JsonProperty("turn_completion_tokens")
  @Schema(
      description = "Number of completion tokens used in the inference for this turn",
      required = true)
  private Integer turnCompletionTokens;

  @JsonProperty("turn_total_tokens")
  @Schema(
      description = "Total number of tokens used in the inference for this turn",
      required = true)
  private Integer turnTotalTokens;

  @JsonProperty("avg_chat_latency")
  @Schema(description = "Average Latency of the chat till now", required = true)
  private Double avgChatLatency;

  @JsonProperty("chat_total_prompt_tokens")
  @Schema(
      description = "Number of prompt tokens used in the inference till now in this chat",
      required = true)
  private Integer chatTotalPromptTokens;

  @JsonProperty("chat_total_completion_tokens")
  @Schema(
      description = "Number of completion tokens used in the inference till now in this chat",
      required = true)
  private Integer chatTotalCompletionTokens;

  @JsonProperty("chat_total_tokens")
  @Schema(
      description = "Total number of tokens used in the inference till now in this chat",
      required = true)
  private Integer chatTotalTokens;

  @JsonProperty("tags")
  private List<TagDTO> tags;

  public InferenceMonitoringDTO(InferenceMonitoring inferenceMonitoring) {
    this.id = inferenceMonitoring.getId();
    this.turnTimeTaken = inferenceMonitoring.getTurnTimeTaken();
    this.TurnPromptTokens = inferenceMonitoring.getTurnPromptTokens();
    this.turnCompletionTokens = inferenceMonitoring.getTurnCompletionTokens();
    this.turnTotalTokens = inferenceMonitoring.getTurnTotalTokens();

    this.avgChatLatency = inferenceMonitoring.getAvgChatLatency();
    this.chatTotalPromptTokens = inferenceMonitoring.getChatTotalPromptTokens();
    this.chatTotalCompletionTokens = inferenceMonitoring.getChatTotalCompletionTokens();
    this.chatTotalTokens = inferenceMonitoring.getChatTotalTokens();
  }

  @Override
  public TagLinkTargetType getTagLinkTargetType() {
    return TagLinkTargetType.INFERENCE_MONITORING;
  }
}
