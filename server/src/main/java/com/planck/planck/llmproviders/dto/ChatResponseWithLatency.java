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

import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseUsage;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class ChatResponseWithLatency {
  private final String reply;
  private final String thinking;
  private final Integer turnCompletionTokens;
  private final Integer promptTokens;
  private final Integer totalTokens;
  private final double latencyMillis;

  public ChatResponseWithLatency(ChatResponse response, long latencyMillis) {
    this.reply = response.aiMessage().text();
    this.thinking = response.aiMessage().thinking();
    this.turnCompletionTokens = response.metadata().tokenUsage().outputTokenCount();
    this.promptTokens = response.metadata().tokenUsage().inputTokenCount();
    this.totalTokens = response.metadata().tokenUsage().totalTokenCount();
    this.latencyMillis = (double) latencyMillis;
  }

  public ChatResponseWithLatency(Response response, long latencyMillis) {
    this.reply =
        response.output().stream()
            .flatMap(item -> item.message().stream())
            .flatMap(message -> message.content().stream())
            .flatMap(content -> content.outputText().stream())
            .findFirst()
            .get()
            .text();
    this.thinking =
        response.output().stream()
            .flatMap(item -> item.reasoning().stream())
            .flatMap(reasoning -> reasoning.summary().stream())
            .map(summary -> summary.text())
            .findFirst()
            .orElse(null);

    if (response.usage().isPresent()) {
      ResponseUsage usage = response.usage().get();
      this.promptTokens = (int) usage.inputTokens();
      this.turnCompletionTokens = (int) usage.outputTokens();
      this.totalTokens = (int) usage.totalTokens();
    } else {
      this.promptTokens = 0;
      this.turnCompletionTokens = 0;
      this.totalTokens = 0;
    }
    this.latencyMillis = (double) latencyMillis;
  }

  public ChatResponseWithLatency(GenerateContentResponse response, long latencyMillis) {
    this.reply = response.text();
    this.thinking =
        response.parts().stream()
            .filter(part -> part.thought().orElse(false))
            .flatMap(part -> part.text().stream())
            .findFirst()
            .orElse(null);

    if (response.usageMetadata().isPresent()) {
      GenerateContentResponseUsageMetadata usage = response.usageMetadata().get();
      this.promptTokens = usage.promptTokenCount().orElse(0);
      this.turnCompletionTokens = usage.candidatesTokenCount().orElse(0);
      this.totalTokens = usage.totalTokenCount().orElse(0);
    } else {
      this.promptTokens = 0;
      this.turnCompletionTokens = 0;
      this.totalTokens = 0;
    }

    this.latencyMillis = (double) latencyMillis;
  }
}
