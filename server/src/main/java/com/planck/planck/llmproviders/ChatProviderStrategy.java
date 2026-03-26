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

package com.planck.planck.llmproviders;

import com.planck.planck.config.RetryConfig;
import com.planck.planck.entitities.Model;
import com.planck.planck.exceptions.ApiKeyIsMissingException;
import com.planck.planck.exceptions.LlmProviderException;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.llmproviders.dto.PromptMessageAdapter;
import com.planck.planck.llmproviders.dto.StreamingChatResponse;
import com.planck.planck.util.EncryptionUtil;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.RetryUtils;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.InternalServerException;
import dev.langchain4j.exception.InvalidRequestException;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.exception.ModelNotFoundException;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpStatus;
import reactor.core.publisher.Flux;

public abstract class ChatProviderStrategy {
  protected Map<String, Object> properties;
  protected String apiKey;
  protected String modelName;
  protected Model model;
  protected String baseUrl;
  protected String chatCompletionsPath;

  public void setModel(Model model) {
    this.model = model;
    this.modelName = model.getName();
    this.properties = ObjectMapperUtil.convertJsonStringToMap(model.getProperties());
    this.baseUrl = model.getUrl();
  }

  public void setApiKey(String apiKey) {
    if (model.isCustomEndpoint()) {
      String key =
          this.properties.containsKey("encrypted_api_key")
              ? EncryptionUtil.decrypt((String) this.properties.get("encrypted_api_key"))
              : "dummy";

      this.apiKey = key;
    } else if (apiKey != null && !apiKey.isBlank()) this.apiKey = EncryptionUtil.decrypt(apiKey);
    else throw new ApiKeyIsMissingException("API key must be set before calling chat()");
  }

  public abstract ChatResponseWithLatency chat(List<Prompt> prompts);

  /**
   * Stream chat completion responses. Default implementation throws UnsupportedOperationException.
   * Override in specific strategy implementations that support streaming.
   */
  public Flux<StreamingChatResponse> streamChat(List<Prompt> prompts) {
    throw new UnsupportedOperationException("Streaming not supported for this provider");
  }

  protected ChatResponseWithLatency getResponseWithRetry(
      ChatModel client, List<ChatMessage> messages, RetryConfig retryProperties) {
    final AtomicLong latencyMs = new AtomicLong(0);

    ChatResponse modelResponse;
    try {
      modelResponse =
          RetryUtils.executeWithRetry(
              () -> {
                try {
                  long startTime = System.currentTimeMillis();
                  ChatResponse response = client.chat(messages);
                  latencyMs.set(System.currentTimeMillis() - startTime);
                  return new RetryUtils.StatusResult<>(response, HttpStatus.SC_OK);
                } catch (InternalServerException e) {
                  return new RetryUtils.StatusResult<>(null, 500, e.getMessage());
                } catch (AuthenticationException e) {
                  return new RetryUtils.StatusResult<>(null, 401, e.getMessage());
                } catch (ModelNotFoundException e) {
                  return new RetryUtils.StatusResult<>(null, 404, e.getMessage());
                } catch (TimeoutException e) {
                  return new RetryUtils.StatusResult<>(null, 408, e.getMessage());
                } catch (RateLimitException e) {
                  return new RetryUtils.StatusResult<>(null, 429, e.getMessage());
                } catch (InvalidRequestException e) {
                  return new RetryUtils.StatusResult<>(null, 400, e.getMessage());
                } catch (LangChain4jException e) {
                  throw new RuntimeException(e.getMessage());
                }
              },
              retryProperties);
    } catch (Exception e) {
      throw new LlmProviderException(
          "Error while sending request to provider: " + e.getMessage(), e);
    }

    return new ChatResponseWithLatency(modelResponse, latencyMs.longValue());
  }

  protected Flux<StreamingChatResponse> buildStreamingChatResponse(
      StreamingChatModel streamingModel, List<Prompt> prompts) {
    try {
      return Flux.create(
          sink -> {
            List<ChatMessage> messages = PromptMessageAdapter.toLangChain4jMessages(prompts);
            Long startTime = System.currentTimeMillis();
            try {
              streamingModel.chat(
                  messages,
                  new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String token) {
                      StreamingChatResponse partialResponse =
                          StreamingChatResponse.builder()
                              .content(token)
                              .isComplete(false)
                              .model(modelName)
                              .latency(System.currentTimeMillis() - startTime)
                              .build();
                      sink.next(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(
                        dev.langchain4j.model.chat.response.ChatResponse response) {
                      TokenUsage tokenUsage = response.tokenUsage();

                      StreamingChatResponse.UsageInfo usageInfo = null;
                      if (tokenUsage != null) {
                        usageInfo =
                            StreamingChatResponse.UsageInfo.builder()
                                .promptTokens(tokenUsage.inputTokenCount())
                                .completionTokens(tokenUsage.outputTokenCount())
                                .totalTokens(tokenUsage.totalTokenCount())
                                .build();
                      }

                      StreamingChatResponse finalResponse =
                          StreamingChatResponse.builder()
                              .content(response.aiMessage().text())
                              .isComplete(true)
                              .model(modelName)
                              .usage(usageInfo)
                              .finishReason("stop")
                              .latency(System.currentTimeMillis() - startTime)
                              .build();
                      sink.next(finalResponse);
                      sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                      sink.error(
                          new LlmProviderException(
                              "Error while streaming: " + error.getMessage(), error));
                    }
                  });

            } catch (Exception e) {
              sink.error(new LlmProviderException("Error while streaming: " + e.getMessage(), e));
            }
          });

    } catch (Exception e) {
      return Flux.error(
          new LlmProviderException("Error while setting up streaming: " + e.getMessage(), e));
    }
  }
}
