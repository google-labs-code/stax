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
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("google")
@Scope("prototype")
@Slf4j
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
      try {
        streamResponse = client.models.generateContentStream(targetModel, contents, config);
      } catch (ApiException e) {
        if (e.code() == 404) {
          String fallbackModel = resolveAvailableGeminiModel(client, targetModel);
          if (!fallbackModel.equals(targetModel)) {
            streamResponse = client.models.generateContentStream(fallbackModel, contents, config);
          } else {
            throw e;
          }
        } else {
          throw e;
        }
      }
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
                  GenerateContentResponse response;
                  try {
                    response = client.models.generateContent(modelName, contents, config);
                  } catch (ApiException e) {
                    if (e.code() == 404) {
                      String fallbackModel = resolveAvailableGeminiModel(client, modelName);
                      if (!fallbackModel.equals(modelName)) {
                        response = client.models.generateContent(fallbackModel, contents, config);
                      } else {
                        throw e;
                      }
                    } else {
                      throw e;
                    }
                  }
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

  private String resolveAvailableGeminiModel(Client client, String requestedModel) {
    try {
      List<String> available = new ArrayList<>();
      for (com.google.genai.types.Model m : client.models.list(null)) {
        String fullName = m.name().orElse("");
        if (fullName.startsWith("models/")) {
          fullName = fullName.substring(7);
        }
        if (!fullName.startsWith("gemini-")) {
          continue;
        }
        if (fullName.contains("-tts")
            || fullName.contains("-image")
            || fullName.contains("-transcribe")
            || fullName.contains("-robotics")
            || fullName.contains("-computer-use")
            || fullName.contains("-embedding")) {
          continue;
        }
        List<String> actions = m.supportedActions().orElse(List.of());
        if (actions.isEmpty() || actions.contains("generateContent")) {
          available.add(fullName);
        }
      }
      log.info(
          "Gemini API 404 for '{}'. Available text/chat Gemini models for this API key: {}",
          requestedModel,
          available);
      if (available.isEmpty()) {
        return requestedModel;
      }
      if (requestedModel.contains("pro")) {
        if (available.contains("gemini-pro-latest")) {
          return "gemini-pro-latest";
        }
        for (String candidate : available) {
          if (candidate.contains("pro")) {
            log.info("Auto-resolving Gemini pro model '{}' -> '{}'", requestedModel, candidate);
            return candidate;
          }
        }
      }
      if (requestedModel.contains("lite")) {
        if (available.contains("gemini-2.5-flash-lite")) {
          return "gemini-2.5-flash-lite";
        }
        if (available.contains("gemini-flash-lite-latest")) {
          return "gemini-flash-lite-latest";
        }
        for (String candidate : available) {
          if (candidate.contains("lite")) {
            return candidate;
          }
        }
      }
      if (requestedModel.contains("flash")) {
        if (available.contains("gemini-flash-latest")) {
          return "gemini-flash-latest";
        }
        if (available.contains("gemini-3.5-flash")) {
          return "gemini-3.5-flash";
        }
        for (String candidate : available) {
          if (candidate.contains("flash") && !candidate.contains("lite")) {
            log.info("Auto-resolving Gemini flash model '{}' -> '{}'", requestedModel, candidate);
            return candidate;
          }
        }
      }
      log.info("Auto-resolving Gemini model '{}' -> '{}'", requestedModel, available.get(0));
      return available.get(0);
    } catch (Exception ex) {
      log.warn(
          "Failed to list available Gemini models during fallback resolution: {}", ex.getMessage());
      return requestedModel;
    }
  }

  private GenerateContentConfig getGenerationConfig(Content systemInstruction) {
    GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();

    if (systemInstruction != null) {
      configBuilder.systemInstruction(systemInstruction);
    }

    if (this.properties != null) {
      if (this.properties.get("temperature") != null) {
        configBuilder.temperature(Float.valueOf(this.properties.get("temperature").toString()));
      }

      if (this.properties.get("top_p") != null) {
        configBuilder.topP(Float.valueOf(this.properties.get("top_p").toString()));
      }

      if (this.properties.get("seed") != null) {
        configBuilder.seed(Integer.valueOf(this.properties.get("seed").toString()));
      }

      if (this.properties.get("max_tokens") != null) {
        configBuilder.maxOutputTokens(
            Integer.valueOf(this.properties.get("max_tokens").toString()));
      } else if (this.properties.get("max_output_tokens") != null) {
        configBuilder.maxOutputTokens(
            Integer.valueOf(this.properties.get("max_output_tokens").toString()));
      }

      if (this.properties.get("top_k") != null) {
        configBuilder.topK(Float.valueOf(this.properties.get("top_k").toString()));
      }

      if (this.properties.get("presence_penalty") != null) {
        configBuilder.presencePenalty(
            Float.valueOf(this.properties.get("presence_penalty").toString()));
      }

      if (this.properties.get("frequency_penalty") != null) {
        configBuilder.frequencyPenalty(
            Float.valueOf(this.properties.get("frequency_penalty").toString()));
      }

      if (this.properties.get("response_log_probs") != null) {
        configBuilder.responseLogprobs(
            Boolean.valueOf(this.properties.get("response_log_probs").toString()));
      }

      if (this.properties.get("log_probs") != null) {
        configBuilder.logprobs(Integer.valueOf(this.properties.get("log_probs").toString()));
        configBuilder.responseLogprobs(true);
      }
    }

    String targetModel = resolveModelName(modelName);
    if (targetModel != null && targetModel.contains("thinking")) {
      configBuilder.thinkingConfig(ThinkingConfig.builder().includeThoughts(true).build());
    }
    return configBuilder.build();
  }

  private String resolveModelName(String name) {
    if (name == null || name.isBlank()) {
      return "gemini-flash-latest";
    }
    if (name.equals("gemini-2.5-flash")) {
      return "gemini-flash-latest";
    }
    if (name.equals("gemini-2.5-pro")) {
      return "gemini-pro-latest";
    }
    if (name.startsWith("gemini-2.0")
        || name.startsWith("gemini-1.5")
        || name.equals("gemini-flash")
        || name.equals("gemini-pro")) {
      if (name.contains("pro")) {
        return "gemini-pro-latest";
      }
      if (name.contains("lite")) {
        return "gemini-2.5-flash-lite";
      }
      return "gemini-flash-latest";
    }
    if (name.startsWith("gemma-3")) {
      return "gemma-4-31b-it";
    }
    if (name.startsWith("learnlm")) {
      return "gemini-flash-latest";
    }
    return name;
  }

  private Client buildClient() {
    Client client = Client.builder().apiKey(apiKey).build();
    return client;
  }
}
