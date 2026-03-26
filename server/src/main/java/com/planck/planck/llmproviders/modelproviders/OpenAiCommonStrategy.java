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

package com.planck.planck.llmproviders.modelproviders;

import com.planck.planck.config.RetryConfig;
import com.planck.planck.llmproviders.ChatProviderStrategy;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.llmproviders.dto.PromptMessageAdapter;
import com.planck.planck.llmproviders.dto.StreamingChatResponse;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder;
import java.util.List;
import reactor.core.publisher.Flux;

public abstract class OpenAiCommonStrategy extends ChatProviderStrategy {
  protected final RetryConfig retryConfig;

  OpenAiCommonStrategy(RetryConfig retryConfig) {
    this.retryConfig = retryConfig;
  }

  @Override
  public ChatResponseWithLatency chat(List<Prompt> prompts) {
    List<ChatMessage> messages = PromptMessageAdapter.toLangChain4jMessages(prompts);
    OpenAiChatModel client = buildClientWithOptions();

    return getResponseWithRetry(client, messages, retryConfig);
  }

  @Override
  public Flux<StreamingChatResponse> streamChat(List<Prompt> prompts) {
    StreamingChatModel client = buildStreamingClientWithOptions();
    return buildStreamingChatResponse(client, prompts);
  }

  protected OpenAiChatModelBuilder getBuilderWithCommonOptions() {
    var optionsBuilder =
        OpenAiChatModel.builder().apiKey(apiKey).baseUrl(baseUrl).modelName(modelName);

    optionsBuilder.maxRetries(0);
    if (this.properties.get("temperature") != null)
      optionsBuilder.temperature(Double.valueOf(this.properties.get("temperature").toString()));

    if (this.properties.get("top_p") != null)
      optionsBuilder.topP(Double.valueOf(this.properties.get("top_p").toString()));

    if (this.properties.get("max_completion_tokens") != null) {
      optionsBuilder.maxCompletionTokens(
          Integer.valueOf(this.properties.get("max_completion_tokens").toString()));
    }
    optionsBuilder.returnThinking(true);
    return optionsBuilder;
  }

  protected OpenAiStreamingChatModelBuilder getStreamingBuilderWithCommonOptions() {
    var optionsBuilder =
        OpenAiStreamingChatModel.builder().apiKey(apiKey).baseUrl(baseUrl).modelName(modelName);

    if (this.properties.get("temperature") != null)
      optionsBuilder.temperature(Double.valueOf(this.properties.get("temperature").toString()));

    if (this.properties.get("top_p") != null)
      optionsBuilder.topP(Double.valueOf(this.properties.get("top_p").toString()));

    if (this.properties.get("max_completion_tokens") != null) {
      optionsBuilder.maxCompletionTokens(
          Integer.valueOf(this.properties.get("max_completion_tokens").toString()));
    }
    optionsBuilder.returnThinking(true);
    return optionsBuilder;
  }

  protected abstract OpenAiChatModel buildClientWithOptions();

  protected abstract StreamingChatModel buildStreamingClientWithOptions();
}
