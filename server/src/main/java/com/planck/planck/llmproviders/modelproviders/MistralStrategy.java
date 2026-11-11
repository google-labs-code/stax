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
    String resolvedModel = resolveModelName(modelName);
    var optionsBuilder = MistralAiChatModel.builder().apiKey(apiKey).modelName(resolvedModel);

    optionsBuilder.maxRetries(0);
    Object maxTokens =
        this.properties.get("max_output_tokens") != null
            ? this.properties.get("max_output_tokens")
            : this.properties.get("max_tokens");
    if (maxTokens != null) {
      optionsBuilder.maxTokens(Integer.valueOf(maxTokens.toString()));
    }

    Object freqPenalty =
        this.properties.get("frequency_penalty") != null
            ? this.properties.get("frequency_penalty")
            : this.properties.get("frequence_penalty");
    if (freqPenalty != null) {
      optionsBuilder.frequencyPenalty(Double.valueOf(freqPenalty.toString()));
    }

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
    String resolvedModel = resolveModelName(modelName);
    var optionsBuilder =
        MistralAiStreamingChatModel.builder().apiKey(apiKey).modelName(resolvedModel);

    Object maxTokens =
        this.properties.get("max_output_tokens") != null
            ? this.properties.get("max_output_tokens")
            : this.properties.get("max_tokens");
    if (maxTokens != null) {
      optionsBuilder.maxTokens(Integer.valueOf(maxTokens.toString()));
    }

    Object freqPenalty =
        this.properties.get("frequency_penalty") != null
            ? this.properties.get("frequency_penalty")
            : this.properties.get("frequence_penalty");
    if (freqPenalty != null) {
      optionsBuilder.frequencyPenalty(Double.valueOf(freqPenalty.toString()));
    }

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

  private String resolveModelName(String name) {
    if (name == null || name.isBlank()) {
      return "mistral-small-latest";
    }
    switch (name) {
      case "mistral-tiny":
      case "mistral-tiny-2312":
      case "mistral-small-2312":
        return "mistral-small-latest";
      case "mistral-medium-2312":
      case "mistral-medium":
        return "mistral-medium-latest";
      default:
        return name;
    }
  }
}
