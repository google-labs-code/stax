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
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("mistral")
@Scope("prototype")
public class MistralStrategy extends ChatProviderStrategy {
  @Autowired private DefaultRetryProperties defaultRetryProperties;

  @Override
  public ChatResponseWithLatency chat(List<Prompt> prompts) {
    MistralAiChatModel client = buildClientWithOptions();
    List<ChatMessage> messages = PromptMessageAdapter.toLangChain4jMessages(prompts);

    return getResponseWithRetry(client, messages, defaultRetryProperties);
  }

  @Override
  public Flux<StreamingChatResponse> streamChat(List<Prompt> prompts) {
    StreamingChatModel client = buildStreamingClientWithOptions();
    return buildStreamingChatResponse(client, prompts);
  }

  @SuppressWarnings("unchecked")
  private MistralAiChatModel buildClientWithOptions() {
    var optionsBuilder = MistralAiChatModel.builder().apiKey(apiKey).modelName(modelName);

    optionsBuilder.maxRetries(0);
    if (this.properties.get("max_tokens") != null)
      optionsBuilder.maxTokens(Integer.valueOf(this.properties.get("max_tokens").toString()));

    if (this.properties.get("frequence_penalty") != null)
      optionsBuilder.frequencyPenalty(
          Double.valueOf(this.properties.get("frequence_penalty").toString()));

    if (this.properties.get("presence_penalty") != null)
      optionsBuilder.presencePenalty(
          Double.valueOf(this.properties.get("presence_penalty").toString()));

    if (this.properties.get("temperature") != null)
      optionsBuilder.temperature(Double.valueOf(this.properties.get("temperature").toString()));

    if (this.properties.get("top_p") != null)
      optionsBuilder.topP(Double.valueOf(this.properties.get("top_p").toString()));

    if (this.properties.get("seed") != null)
      optionsBuilder.randomSeed(Integer.valueOf(this.properties.get("seed").toString()));

    if (this.properties.get("stop_sequences") != null
        && !((List<String>) this.properties.get("stop_sequences")).isEmpty()) {
      optionsBuilder.stopSequences((List<String>) this.properties.get("stop_sequences"));
    }

    return optionsBuilder.build();
  }

  @SuppressWarnings("unchecked")
  private StreamingChatModel buildStreamingClientWithOptions() {
    var optionsBuilder = MistralAiStreamingChatModel.builder().apiKey(apiKey).modelName(modelName);

    if (this.properties.get("max_tokens") != null)
      optionsBuilder.maxTokens(Integer.valueOf(this.properties.get("max_tokens").toString()));

    if (this.properties.get("frequence_penalty") != null)
      optionsBuilder.frequencyPenalty(
          Double.valueOf(this.properties.get("frequence_penalty").toString()));

    if (this.properties.get("presence_penalty") != null)
      optionsBuilder.presencePenalty(
          Double.valueOf(this.properties.get("presence_penalty").toString()));

    if (this.properties.get("temperature") != null)
      optionsBuilder.temperature(Double.valueOf(this.properties.get("temperature").toString()));

    if (this.properties.get("top_p") != null)
      optionsBuilder.topP(Double.valueOf(this.properties.get("top_p").toString()));

    if (this.properties.get("seed") != null)
      optionsBuilder.randomSeed(Integer.valueOf(this.properties.get("seed").toString()));

    if (this.properties.get("stop_sequences") != null
        && !((List<String>) this.properties.get("stop_sequences")).isEmpty()) {
      optionsBuilder.stopSequences((List<String>) this.properties.get("stop_sequences"));
    }

    return optionsBuilder.build();
  }
}
