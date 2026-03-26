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

import com.planck.planck.config.DefaultRetryProperties;
import com.planck.planck.llmproviders.ChatProviderStrategy;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.llmproviders.dto.PromptMessageAdapter;
import com.planck.planck.llmproviders.dto.StreamingChatResponse;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("anthropic")
@Scope("prototype")
public class AnthropicStrategy extends ChatProviderStrategy {
  @Autowired private DefaultRetryProperties defaultRetryProperties;

  @Override
  public ChatResponseWithLatency chat(List<Prompt> prompts) {
    AnthropicChatModel client = buildClientWithOptions();
    List<ChatMessage> messages = PromptMessageAdapter.toLangChain4jMessages(prompts);

    return getResponseWithRetry(client, messages, defaultRetryProperties);
  }

  @Override
  public Flux<StreamingChatResponse> streamChat(List<Prompt> prompts) {
    StreamingChatModel client = buildStreamingClientWithOptions();
    return buildStreamingChatResponse(client, prompts);
  }

  @SuppressWarnings("unchecked")
  private AnthropicChatModel buildClientWithOptions() {
    var optionsBuilder = AnthropicChatModel.builder().apiKey(apiKey).modelName(modelName);

    optionsBuilder.maxRetries(0);
    if (this.properties.get("stop_sequences") != null
        && !((List<String>) this.properties.get("stop_sequences")).isEmpty()) {
      optionsBuilder.stopSequences((List<String>) this.properties.get("stop_sequences"));
    }

    if (this.properties.get("temperature") != null) {
      optionsBuilder.temperature(Double.valueOf(this.properties.get("temperature").toString()));
    }

    if (this.properties.get("top_p") != null) {
      optionsBuilder.topP(Double.valueOf(this.properties.get("top_p").toString()));
    }

    if (this.properties.get("top_k") != null) {
      optionsBuilder.topK(Integer.valueOf(this.properties.get("top_k").toString()));
    }

    if (this.properties.get("max_tokens") != null) {
      optionsBuilder.maxTokens(Integer.valueOf(this.properties.get("max_tokens").toString()));
    }

    return optionsBuilder.build();
  }

  @SuppressWarnings("unchecked")
  private StreamingChatModel buildStreamingClientWithOptions() {
    var optionsBuilder = AnthropicStreamingChatModel.builder().apiKey(apiKey).modelName(modelName);

    if (this.properties.get("stop_sequences") != null
        && !((List<String>) this.properties.get("stop_sequences")).isEmpty()) {
      optionsBuilder.stopSequences((List<String>) this.properties.get("stop_sequences"));
    }

    if (this.properties.get("temperature") != null) {
      optionsBuilder.temperature(Double.valueOf(this.properties.get("temperature").toString()));
    }

    if (this.properties.get("top_p") != null) {
      optionsBuilder.topP(Double.valueOf(this.properties.get("top_p").toString()));
    }

    if (this.properties.get("top_k") != null) {
      optionsBuilder.topK(Integer.valueOf(this.properties.get("top_k").toString()));
    }

    if (this.properties.get("max_tokens") != null) {
      optionsBuilder.maxTokens(Integer.valueOf(this.properties.get("max_tokens").toString()));
    }

    return optionsBuilder.build();
  }
}
