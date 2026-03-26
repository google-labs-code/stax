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

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.errors.OpenAIException;
import com.openai.models.ChatModel;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseStreamEvent;
import com.planck.planck.config.DefaultRetryProperties;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.LlmProviderException;
import com.planck.planck.llmproviders.ChatProviderStrategy;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.llmproviders.dto.StreamingChatResponse;
import com.planck.planck.llmproviders.dto.StreamingChatResponse.StreamingChatResponseBuilder;
import com.planck.planck.util.RetryUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("openai")
@Scope("prototype")
public class OpenAIStrategy extends ChatProviderStrategy {
  @Autowired private DefaultRetryProperties defaultRetryProperties;

  @Override
  public ChatResponseWithLatency chat(List<Prompt> prompts) {
    OpenAIClient client = buildClient();
    ResponseCreateParams params = getParams(prompts);

    return getResponseWithRetry(client, params);
  }

  @Override
  public Flux<StreamingChatResponse> streamChat(List<Prompt> prompts) {
    OpenAIClient client = buildClient();
    ResponseCreateParams params = getParams(prompts);

    long startTime = System.currentTimeMillis();
    StreamResponse<ResponseStreamEvent> streamResponse;
    try {
      streamResponse = client.responses().createStreaming(params);
    } catch (OpenAIException e) {
      throw new LlmProviderException(
          "Error while sending request to OpenAI API: " + e.getMessage(), e);
    }

    return Flux.fromStream(streamResponse.stream())
        .map(
            event -> {
              String content = "";
              Boolean finished = false;
              String finishReason = null;

              StreamingChatResponseBuilder builder = StreamingChatResponse.builder();

              if (event.isError()) {
                finished = true;
                finishReason = event.asError().message();
              } else if (event.isOutputTextDelta()) {
                content = event.asOutputTextDelta().delta();
              } else if (event.isCompleted()) {
                finished = true;
                finishReason = event.asCompleted().response().status().get().toString();
                ChatResponseWithLatency chatResponse =
                    new ChatResponseWithLatency(
                        event.asCompleted().response(), System.currentTimeMillis() - startTime);
                content = chatResponse.getReply();
                builder.usage(
                    StreamingChatResponse.UsageInfo.builder()
                        .promptTokens(chatResponse.getPromptTokens())
                        .completionTokens(chatResponse.getTurnCompletionTokens())
                        .totalTokens(chatResponse.getTotalTokens())
                        .build());
                builder.latency(System.currentTimeMillis() - startTime);
              }

              builder.content(content);
              builder.isComplete(finished);
              builder.model(modelName);
              builder.finishReason(finishReason);
              return builder.build();
            })
        .filter(
            response -> {
              // Only emit responses that have content OR are the final completion
              return (response.getContent() != null && !response.getContent().isEmpty())
                  || response.isComplete();
            });
  }

  private ChatResponseWithLatency getResponseWithRetry(
      OpenAIClient client, ResponseCreateParams params) {
    final AtomicLong latencyMs = new AtomicLong(0);

    Response openAiResponse;
    try {
      openAiResponse =
          RetryUtils.executeWithRetry(
              () -> {
                try {
                  long startTime = System.currentTimeMillis();
                  Response response = client.responses().create(params);
                  latencyMs.set(System.currentTimeMillis() - startTime);
                  return new RetryUtils.StatusResult<>(response, HttpStatus.SC_OK);
                } catch (OpenAIException e) {
                  throw new RuntimeException(e.getMessage());
                }
              },
              defaultRetryProperties);
    } catch (Exception e) {
      throw new LlmProviderException(
          "Error while sending request to Open AI API: " + e.getMessage(), e);
    }

    return new ChatResponseWithLatency(openAiResponse, latencyMs.longValue());
  }

  private OpenAIClient buildClient() {
    OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    return client;
  }

  private ResponseCreateParams getParams(List<Prompt> prompts) {
    ResponseCreateParams.Builder paramsBuilder =
        ResponseCreateParams.builder()
            .inputOfResponse(getInputFromMessages(prompts))
            .model(ChatModel.of(modelName));

    if (this.properties.get("max_tokens") != null)
      paramsBuilder.maxOutputTokens(Long.valueOf(this.properties.get("max_tokens").toString()));

    if (this.properties.get("temperature") != null)
      paramsBuilder.temperature(Double.valueOf(this.properties.get("temperature").toString()));

    if (this.properties.get("top_p") != null)
      paramsBuilder.topP(Double.valueOf(this.properties.get("top_p").toString()));

    if (this.properties.get("top_logprobs") != null)
      paramsBuilder.topLogprobs(Long.valueOf(this.properties.get("top_logprobs").toString()));

    // paramsBuilder.reasoning(Reasoning.builder().summary(Summary.AUTO).build());
    return paramsBuilder.build();
  }

  private List<ResponseInputItem> getInputFromMessages(List<Prompt> prompts) {
    return prompts.stream()
        .map(
            message ->
                ResponseInputItem.ofEasyInputMessage(
                    EasyInputMessage.builder()
                        .role(mapMessageType(message.getRole()))
                        .content(message.getText())
                        .build()))
        .toList();
  }

  private EasyInputMessage.Role mapMessageType(InputRole messageType) {
    switch (messageType) {
      case ASSISTANT:
        return EasyInputMessage.Role.ASSISTANT;
      case SYSTEM:
        return EasyInputMessage.Role.SYSTEM;
      default:
        return EasyInputMessage.Role.USER;
    }
  }
}
