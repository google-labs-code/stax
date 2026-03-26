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

package com.planck.planck.domain.importexport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InferenceAnalyticsDTO {
  @JsonProperty("total_chat_latency")
  private Double totalChatLatency;

  @JsonProperty("average_chat_latency")
  private Double avgChatLatency;

  @JsonProperty("total_chat_prompt_tokens")
  private Long totalChatPromptTokens;

  @JsonProperty("total_chat_completion_tokens")
  private Long totalChatCompletionTokens;

  @JsonProperty("total_chat_tokens")
  private Long totalChatTokens;
}
