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

package com.planck.planck.domain.pubsub;

import com.planck.planck.util.QueueConstants;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface PubsubOutboundInferenceGateway {

  @Gateway(requestChannel = QueueConstants.OPEN_AI_INFERENCE_PUB_CHANNEL)
  void sendToOpenAiInference(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.GEMINI_INFERENCE_PUB_CHANNEL)
  void sendToGeminiInference(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.MISTRAL_INFERENCE_PUB_CHANNEL)
  void sendToMistralInference(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.ANTHROPIC_INFERENCE_PUB_CHANNEL)
  void sendToAnthropicInference(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.GROK_INFERENCE_PUB_CHANNEL)
  void sendToGrokInference(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.OLLAMA_INFERENCE_PUB_CHANNEL)
  void sendToOllamaInference(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.DEEPSEEK_INFERENCE_PUB_CHANNEL)
  void sendToDeepSeekInference(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.HUGGINGFACE_INFERENCE_PUB_CHANNEL)
  void sendToHuggingFaceInference(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.LLAMA_INFERENCE_PUB_CHANNEL)
  void sendToLlamaInference(String inferenceDTOJson);

  // BULK GATEWAYS
  @Gateway(requestChannel = QueueConstants.OPEN_AI_INFERENCE_PUB_CHANNEL_BULK)
  void sendToOpenAiInferenceBulk(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.GEMINI_INFERENCE_PUB_CHANNEL_BULK)
  void sendToGeminiInferenceBulk(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.MISTRAL_INFERENCE_PUB_CHANNEL_BULK)
  void sendToMistralInferenceBulk(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.ANTHROPIC_INFERENCE_PUB_CHANNEL_BULK)
  void sendToAnthropicInferenceBulk(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.GROK_INFERENCE_PUB_CHANNEL_BULK)
  void sendToGrokInferenceBulk(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.OLLAMA_INFERENCE_PUB_CHANNEL_BULK)
  void sendToOllamaInferenceBulk(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.DEEPSEEK_INFERENCE_PUB_CHANNEL_BULK)
  void sendToDeepSeekInferenceBulk(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.HUGGINGFACE_INFERENCE_PUB_CHANNEL_BULK)
  void sendToHuggingFaceInferenceBulk(String inferenceDTOJson);

  @Gateway(requestChannel = QueueConstants.LLAMA_INFERENCE_PUB_CHANNEL_BULK)
  void sendToLlamaInferenceBulk(String inferenceDTOJson);
}
