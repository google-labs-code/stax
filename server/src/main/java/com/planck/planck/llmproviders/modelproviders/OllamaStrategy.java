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
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("ollama")
@Scope("prototype")
public class OllamaStrategy extends ChatProviderStrategy {
  @Autowired private DefaultRetryProperties defaultRetryProperties;

  @Override
  public ChatResponseWithLatency chat(List<Prompt> prompts) {
    OllamaChatModel client = buildClientWithOptions();
    List<ChatMessage> messages = PromptMessageAdapter.toLangChain4jMessages(prompts);

    return getResponseWithRetry(client, messages, defaultRetryProperties);
  }

  @Override
  public Flux<StreamingChatResponse> streamChat(List<Prompt> prompts) {
    StreamingChatModel client = buildStreamingClientWithOptions();
    return buildStreamingChatResponse(client, prompts);
  }

  @SuppressWarnings("unchecked")
  private OllamaChatModel buildClientWithOptions() {
    var optionsBuilder = OllamaChatModel.builder().baseUrl(baseUrl).modelName(modelName);

    optionsBuilder.maxRetries(0);
    if (this.properties.get("stop_sequences") != null
        && !((List<String>) this.properties.get("stop_sequences")).isEmpty()) {
      optionsBuilder.stop((List<String>) this.properties.get("stop_sequences"));
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

    if (this.properties.containsKey("num_predict")) {
      optionsBuilder.numPredict(Integer.valueOf(this.properties.get("num_predict").toString()));
    }

    if (this.properties.containsKey("seed")) {
      optionsBuilder.seed(Integer.valueOf(this.properties.get("seed").toString()));
    }

    if (this.properties.containsKey("num_ctx")) {
      optionsBuilder.numCtx(Integer.valueOf(this.properties.get("num_ctx").toString()));
    }

    return optionsBuilder.build();
  }

  @SuppressWarnings("unchecked")
  private StreamingChatModel buildStreamingClientWithOptions() {
    var optionsBuilder = OllamaStreamingChatModel.builder().baseUrl(baseUrl).modelName(modelName);

    if (this.properties.get("stop_sequences") != null
        && !((List<String>) this.properties.get("stop_sequences")).isEmpty()) {
      optionsBuilder.stop((List<String>) this.properties.get("stop_sequences"));
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

    if (this.properties.containsKey("num_predict")) {
      optionsBuilder.numPredict(Integer.valueOf(this.properties.get("num_predict").toString()));
    }

    if (this.properties.containsKey("seed")) {
      optionsBuilder.seed(Integer.valueOf(this.properties.get("seed").toString()));
    }

    if (this.properties.containsKey("num_ctx")) {
      optionsBuilder.numCtx(Integer.valueOf(this.properties.get("num_ctx").toString()));
    }

    return optionsBuilder.build();
  }
}
