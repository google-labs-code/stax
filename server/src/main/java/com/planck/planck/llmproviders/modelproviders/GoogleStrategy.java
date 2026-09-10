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

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.errors.ApiException;
import com.google.genai.types.Content;
import com.google.genai.types.FinishReason;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import com.planck.planck.config.DefaultRetryProperties;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.LlmProviderException;
import com.planck.planck.llmproviders.ChatProviderStrategy;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.llmproviders.dto.StreamingChatResponse;
import com.planck.planck.llmproviders.dto.StreamingChatResponse.StreamingChatResponseBuilder;
import com.planck.planck.util.RetryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("google")
@Scope("prototype")
public class GoogleStrategy extends ChatProviderStrategy {
  @Autowired private DefaultRetryProperties defaultRetryProperties;

  @Override
  public ChatResponseWithLatency chat(List<Prompt> prompts) {
    Client client = buildClient();

    Content systemInstruction = null;
    List<Content> contents = new ArrayList<>();
    for (Prompt prompt : prompts) {
      if (prompt.getRole() == InputRole.SYSTEM || prompt.getRole() == InputRole.DEVELOPER) {
        if (prompt.getText() != null && !prompt.getText().trim().isEmpty()) {
          systemInstruction = Content.fromParts(Part.fromText(prompt.getText()));
        }
        continue;
      }
      if (prompt.getText() != null && !prompt.getText().trim().isEmpty()) {
        String role = prompt.getRole() == InputRole.ASSISTANT ? "model" : "user";
        contents.add(
            Content.builder().role(role).parts(List.of(Part.fromText(prompt.getText()))).build());
      }
    }
    if (contents.isEmpty()) {
      contents.add(Content.builder().role("user").parts(List.of(Part.fromText(" "))).build());
    }

    GenerateContentConfig config = getGenerationConfig(systemInstruction);

    String targetModel = resolveModelName(model.getName());
    return getResponseWithRetry(client, targetModel, contents, config);
  }

  @Override
  public Flux<StreamingChatResponse> streamChat(List<Prompt> prompts) {
    Client client = buildClient();

    Content systemInstruction = null;
    List<Content> contents = new ArrayList<>();
    for (Prompt prompt : prompts) {
      if (prompt.getRole() == InputRole.SYSTEM || prompt.getRole() == InputRole.DEVELOPER) {
        if (prompt.getText() != null && !prompt.getText().trim().isEmpty()) {
          systemInstruction = Content.fromParts(Part.fromText(prompt.getText()));
        }
        continue;
      }
      if (prompt.getText() != null && !prompt.getText().trim().isEmpty()) {
        String role = prompt.getRole() == InputRole.ASSISTANT ? "model" : "user";
        contents.add(
            Content.builder().role(role).parts(List.of(Part.fromText(prompt.getText()))).build());
      }
    }
    if (contents.isEmpty()) {
      contents.add(Content.builder().role("user").parts(List.of(Part.fromText(" "))).build());
    }
    GenerateContentConfig config = getGenerationConfig(systemInstruction);

    long startTime = System.currentTimeMillis();
    ResponseStream<GenerateContentResponse> streamResponse;
    String targetModel = resolveModelName(model.getName());
    try {
      streamResponse = client.models.generateContentStream(targetModel, contents, config);
    } catch (Exception e) {
      throw new LlmProviderException(
          "Error while sending request to Gemini API: " + e.getMessage(), e);
    }

    return Flux.fromIterable(streamResponse)
        .map(
            contentResponse -> {
              String content = "";
              Boolean finished = false;
              String finishReason = null;
              contentResponse.checkFinishReason();
              StreamingChatResponseBuilder builder = StreamingChatResponse.builder();
              FinishReason reason = contentResponse.finishReason();

              if (reason.knownEnum() == FinishReason.Known.FINISH_REASON_UNSPECIFIED) {
                content = contentResponse.text();
              } else {
                finished = true;
                finishReason = reason.toString();
                ChatResponseWithLatency chatResponse =
                    new ChatResponseWithLatency(
                        contentResponse, System.currentTimeMillis() - startTime);
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
      Client client, String modelName, List<Content> contents, GenerateContentConfig config) {
    final AtomicLong latencyMs = new AtomicLong(0);

    GenerateContentResponse geminiResponse;
    try {
      geminiResponse =
          RetryUtils.executeWithRetry(
              () -> {
                try {
                  long startTime = System.currentTimeMillis();
                  GenerateContentResponse response =
                      client.models.generateContent(modelName, contents, config);
                  latencyMs.set(System.currentTimeMillis() - startTime);
                  return new RetryUtils.StatusResult<>(response, HttpStatus.SC_OK);
                } catch (ApiException e) {
                  return new RetryUtils.StatusResult<>(null, e.code(), e.getMessage());
                } catch (Exception e) {
                  return new RetryUtils.StatusResult<>(
                      null, HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
              },
              defaultRetryProperties);
    } catch (Exception e) {
      throw new LlmProviderException(
          "Error while sending request to Gemini API: " + e.getMessage(), e);
    }

    return new ChatResponseWithLatency(geminiResponse, latencyMs.longValue());
  }

  private GenerateContentConfig getGenerationConfig(Content systemInstruction) {
    GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();

    if (systemInstruction != null) {
      configBuilder.systemInstruction(systemInstruction);
    }

    if (this.properties.containsKey("temperature")) {
      configBuilder.temperature(Float.valueOf(this.properties.get("temperature").toString()));
    }

    if (this.properties.containsKey("top_p")) {
      configBuilder.topP(Float.valueOf(this.properties.get("top_p").toString()));
    }

    if (this.properties.containsKey("seed")) {
      configBuilder.seed(Integer.valueOf(this.properties.get("seed").toString()));
    }

    if (this.properties.containsKey("max_tokens")) {
      configBuilder.maxOutputTokens(Integer.valueOf(this.properties.get("max_tokens").toString()));
    }

    if (this.properties.containsKey("top_k")) {
      configBuilder.topK(Float.valueOf(this.properties.get("top_k").toString()));
    }

    if (this.properties.containsKey("presence_penalty")) {
      configBuilder.presencePenalty(
          Float.valueOf(this.properties.get("presence_penalty").toString()));
    }

    if (this.properties.containsKey("frequency_penalty")) {
      configBuilder.frequencyPenalty(
          Float.valueOf(this.properties.get("frequency_penalty").toString()));
    }

    if (this.properties.containsKey("response_log_probs")) {
      configBuilder.responseLogprobs(
          Boolean.valueOf(this.properties.get("response_log_probs").toString()));
    }

    if (this.properties.containsKey("log_probs")) {
      configBuilder.logprobs(Integer.valueOf(this.properties.get("log_probs").toString()));
      configBuilder.responseLogprobs(true);
    }

    String targetModel = resolveModelName(modelName);
    if (targetModel != null && targetModel.contains("thinking")) {
      configBuilder.thinkingConfig(ThinkingConfig.builder().includeThoughts(true).build());
    }
    return configBuilder.build();
  }

  private String resolveModelName(String name) {
    if (name == null || name.isBlank()) {
      return "gemini-2.5-flash";
    }
    if (name.startsWith("gemini-2.0") || name.equals("gemini-flash") || name.equals("gemini-pro")) {
      if (name.contains("lite")) {
        return "gemini-2.5-flash-lite";
      }
      return "gemini-2.5-flash";
    }
    return name;
  }

  private Client buildClient() {
    Client client = Client.builder().apiKey(apiKey).build();
    return client;
  }
}
