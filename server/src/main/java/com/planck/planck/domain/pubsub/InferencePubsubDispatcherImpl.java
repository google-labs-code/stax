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

import com.planck.planck.domain.inference.dto.InferenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InferencePubsubDispatcherImpl implements InferencePubsubDispatcher {

  @Autowired PubsubOutboundInferenceGateway messagingInferenceGateway;

  @Override
  public void dispatch(InferenceContext context) {
    dispatch(context, false);
  }

  @Override
  public void dispatch(InferenceContext context, boolean isBulk) {
    if (isBulk) {
      switch (context.model().getProvider()) {
        case OPENAI -> messagingInferenceGateway.sendToOpenAiInferenceBulk(context.payloadJson());
        case MISTRAL -> messagingInferenceGateway.sendToMistralInferenceBulk(context.payloadJson());
        case GOOGLE -> messagingInferenceGateway.sendToGeminiInferenceBulk(context.payloadJson());
        case ANTHROPIC ->
            messagingInferenceGateway.sendToAnthropicInferenceBulk(context.payloadJson());
        case GROK -> messagingInferenceGateway.sendToGrokInferenceBulk(context.payloadJson());
        // case OLLAMA ->
        // messagingInferenceGateway.sendToOllamaInferenceBulk(context.payloadJson());
        case DEEPSEEK ->
            messagingInferenceGateway.sendToDeepSeekInferenceBulk(context.payloadJson());
        // case HUGGINGFACE ->
        //     messagingInferenceGateway.sendToHuggingFaceInferenceBulk(context.payloadJson());
        case LLAMA -> messagingInferenceGateway.sendToLlamaInferenceBulk(context.payloadJson());
        default -> throw new IllegalArgumentException("Unsupported model provider");
      }
    } else {
      switch (context.model().getProvider()) {
        case OPENAI -> messagingInferenceGateway.sendToOpenAiInference(context.payloadJson());
        case MISTRAL -> messagingInferenceGateway.sendToMistralInference(context.payloadJson());
        case GOOGLE -> messagingInferenceGateway.sendToGeminiInference(context.payloadJson());
        case ANTHROPIC -> messagingInferenceGateway.sendToAnthropicInference(context.payloadJson());
        case GROK -> messagingInferenceGateway.sendToGrokInference(context.payloadJson());
        // case OLLAMA -> messagingInferenceGateway.sendToOllamaInference(context.payloadJson());
        case DEEPSEEK -> messagingInferenceGateway.sendToDeepSeekInference(context.payloadJson());
        // case HUGGINGFACE ->
        //     messagingInferenceGateway.sendToHuggingFaceInference(context.payloadJson());
        case LLAMA -> messagingInferenceGateway.sendToLlamaInference(context.payloadJson());
        default -> throw new IllegalArgumentException("Unsupported model provider");
      }
    }
  }
}
