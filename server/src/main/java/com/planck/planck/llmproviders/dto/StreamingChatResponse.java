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

package com.planck.planck.llmproviders.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamingChatResponse {

  @JsonProperty("content")
  private String content;

  @JsonProperty("thinking")
  private String thinking;

  @JsonProperty("isComplete")
  private boolean isComplete;

  @JsonProperty("usage")
  private UsageInfo usage;

  @JsonProperty("model")
  private String model;

  @JsonProperty("finishReason")
  private String finishReason;

  @JsonProperty("latency")
  private Long latency;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("finalChatDTO")
  private ChatTurnDTO finalChatDTO;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UsageInfo {
    @JsonProperty("promptTokens")
    private Integer promptTokens;

    @JsonProperty("completionTokens")
    private Integer completionTokens;

    @JsonProperty("totalTokens")
    private Integer totalTokens;
  }
}
