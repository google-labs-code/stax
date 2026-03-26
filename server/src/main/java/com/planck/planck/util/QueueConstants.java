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

package com.planck.planck.util;

public final class QueueConstants {

  private QueueConstants() {}

  // User Deletion Queue
  public static final String USER_DELETION_TOPIC = "user-deletion";
  public static final String USER_DELETION_SUB_GCP = "user-deletion-sub";

  // GEMINI CONSTANTS

  public static final String OPEN_AI_EVAL_PUB_CHANNEL = "openAiEvalPubChannel";
  public static final String GEMINI_EVAL_PUB_CHANNEL = "geminiEvalPubChannel";
  public static final String MISTRAL_EVAL_PUB_CHANNEL = "mistralEvalPubChannel";
  public static final String ANTHROPIC_EVAL_PUB_CHANNEL = "anthropicEvalPubChannel";
  public static final String GROK_EVAL_PUB_CHANNEL = "grokEvalPubChannel";
  public static final String OLLAMA_EVAL_PUB_CHANNEL = "ollamaEvalPubChannel";
  public static final String DEEPSEEK_EVAL_PUB_CHANNEL = "deepseekEvalPubChannel";
  public static final String HUGGINGFACE_EVAL_PUB_CHANNEL = "huggingfaceEvalPubChannel";
  public static final String LLAMA_EVAL_PUB_CHANNEL = "llamaEvalPubChannel";

  public static final String OPEN_AI_EVAL_SUB_CHANNEL = "openAiSubChannel";
  public static final String GEMINI_EVAL_SUB_CHANNEL = "geminiEvalSubChannel";
  public static final String MISTRAL_EVAL_SUB_CHANNEL = "mistralEvalSubChannel";
  public static final String ANTHROPIC_EVAL_SUB_CHANNEL = "anthropicEvalSubChannel";
  public static final String GROK_EVAL_SUB_CHANNEL = "grokEvalSubChannel";
  public static final String OLLAMA_EVAL_SUB_CHANNEL = "ollamaEvalSubChannel";
  public static final String DEEPSEEK_EVAL_SUB_CHANNEL = "deepseekEvalSubChannel";
  public static final String HUGGINGFACE_EVAL_SUB_CHANNEL = "huggingfaceEvalSubChannel";
  public static final String LLAMA_EVAL_SUB_CHANNEL = "llamaEvalSubChannel";

  public static final String OPEN_AI_INFERENCE_PUB_CHANNEL = "openAiInferencePubChannel";
  public static final String GEMINI_INFERENCE_PUB_CHANNEL = "geminiInferencePubChannel";
  public static final String MISTRAL_INFERENCE_PUB_CHANNEL = "mistralInferencePubChannel";
  public static final String ANTHROPIC_INFERENCE_PUB_CHANNEL = "anthropicEvalPubChannel";
  public static final String GROK_INFERENCE_PUB_CHANNEL = "grokInferencePubChannel";
  public static final String OLLAMA_INFERENCE_PUB_CHANNEL = "ollamaInferencePubChannel";
  public static final String DEEPSEEK_INFERENCE_PUB_CHANNEL = "deepseekInferencePubChannel";
  public static final String HUGGINGFACE_INFERENCE_PUB_CHANNEL = "huggingfaceInferencePubChannel";
  public static final String LLAMA_INFERENCE_PUB_CHANNEL = "llamaInferencePubChannel";

  public static final String OPEN_AI_INFERENCE_SUB_CHANNEL = "openAiInferenceSubChannel";
  public static final String GEMINI_INFERENCE_SUB_CHANNEL = "geminiInferenceSubChannel";
  public static final String MISTRAL_INFERENCE_SUB_CHANNEL = "mistralInferenceSubChannel";
  public static final String ANTHROPIC_INFERENCE_SUB_CHANNEL = "anthropicInferenceSubChannel";
  public static final String GROK_INFERENCE_SUB_CHANNEL = "grokInferenceSubChannel";
  public static final String OLLAMA_INFERENCE_SUB_CHANNEL = "ollamaInferenceSubChannel";
  public static final String DEEPSEEK_INFERENCE_SUB_CHANNEL = "deepseekInferenceSubChannel";
  public static final String HUGGINGFACE_INFERENCE_SUB_CHANNEL = "huggingfaceInferenceSubChannel";
  public static final String LLAMA_INFERENCE_SUB_CHANNEL = "llamaInferenceSubChannel";

  // Bulk channels for inference (jobs > 10)
  public static final String OPEN_AI_INFERENCE_PUB_CHANNEL_BULK = "openAiInferencePubChannelBulk";
  public static final String GEMINI_INFERENCE_PUB_CHANNEL_BULK = "geminiInferencePubChannelBulk";
  public static final String MISTRAL_INFERENCE_PUB_CHANNEL_BULK = "mistralInferencePubChannelBulk";
  public static final String ANTHROPIC_INFERENCE_PUB_CHANNEL_BULK =
      "anthropicInferencePubChannelBulk";
  public static final String GROK_INFERENCE_PUB_CHANNEL_BULK = "grokInferencePubChannelBulk";
  public static final String OLLAMA_INFERENCE_PUB_CHANNEL_BULK = "ollamaInferencePubChannelBulk";
  public static final String DEEPSEEK_INFERENCE_PUB_CHANNEL_BULK =
      "deepseekInferencePubChannelBulk";
  public static final String HUGGINGFACE_INFERENCE_PUB_CHANNEL_BULK =
      "huggingfaceInferencePubChannelBulk";
  public static final String LLAMA_INFERENCE_PUB_CHANNEL_BULK = "llamaInferencePubChannelBulk";

  public static final String OPEN_AI_INFERENCE_SUB_CHANNEL_BULK = "openAiInferenceSubChannelBulk";
  public static final String GEMINI_INFERENCE_SUB_CHANNEL_BULK = "geminiInferenceSubChannelBulk";
  public static final String MISTRAL_INFERENCE_SUB_CHANNEL_BULK = "mistralInferenceSubChannelBulk";
  public static final String ANTHROPIC_INFERENCE_SUB_CHANNEL_BULK =
      "anthropicInferenceSubChannelBulk";
  public static final String GROK_INFERENCE_SUB_CHANNEL_BULK = "grokInferenceSubChannelBulk";
  public static final String OLLAMA_INFERENCE_SUB_CHANNEL_BULK = "ollamaInferenceSubChannelBulk";
  public static final String DEEPSEEK_INFERENCE_SUB_CHANNEL_BULK =
      "deepseekInferenceSubChannelBulk";
  public static final String HUGGINGFACE_INFERENCE_SUB_CHANNEL_BULK =
      "huggingfaceInferenceSubChannelBulk";
  public static final String LLAMA_INFERENCE_SUB_CHANNEL_BULK = "llamaInferenceSubChannelBulk";

  // Bulk channels for evaluation (jobs > 10)
  public static final String OPEN_AI_EVAL_PUB_CHANNEL_BULK = "openAiEvalPubChannelBulk";
  public static final String GEMINI_EVAL_PUB_CHANNEL_BULK = "geminiEvalPubChannelBulk";
  public static final String MISTRAL_EVAL_PUB_CHANNEL_BULK = "mistralEvalPubChannelBulk";
  public static final String ANTHROPIC_EVAL_PUB_CHANNEL_BULK = "anthropicEvalPubChannelBulk";
  public static final String GROK_EVAL_PUB_CHANNEL_BULK = "grokEvalPubChannelBulk";
  public static final String OLLAMA_EVAL_PUB_CHANNEL_BULK = "ollamaEvalPubChannelBulk";
  public static final String DEEPSEEK_EVAL_PUB_CHANNEL_BULK = "deepseekEvalPubChannelBulk";
  public static final String HUGGINGFACE_EVAL_PUB_CHANNEL_BULK = "huggingfaceEvalPubChannelBulk";
  public static final String LLAMA_EVAL_PUB_CHANNEL_BULK = "llamaEvalPubChannelBulk";

  public static final String OPEN_AI_EVAL_SUB_CHANNEL_BULK = "openAiEvalSubChannelBulk";
  public static final String GEMINI_EVAL_SUB_CHANNEL_BULK = "geminiEvalSubChannelBulk";
  public static final String MISTRAL_EVAL_SUB_CHANNEL_BULK = "mistralEvalSubChannelBulk";
  public static final String ANTHROPIC_EVAL_SUB_CHANNEL_BULK = "anthropicEvalSubChannelBulk";
  public static final String GROK_EVAL_SUB_CHANNEL_BULK = "grokEvalSubChannelBulk";
  public static final String OLLAMA_EVAL_SUB_CHANNEL_BULK = "ollamaEvalSubChannelBulk";
  public static final String DEEPSEEK_EVAL_SUB_CHANNEL_BULK = "deepseekEvalSubChannelBulk";
  public static final String HUGGINGFACE_EVAL_SUB_CHANNEL_BULK = "huggingfaceEvalSubChannelBulk";
  public static final String LLAMA_EVAL_SUB_CHANNEL_BULK = "llamaEvalSubChannelBulk";

  public static final String OPEN_AI_INFERENCE_TOPIC_GCP = "open-ai-inference";
  public static final String MISTRAL_INFERENCE_TOPIC_GCP = "mistral-inference";
  public static final String ANTHROPIC_INFERENCE_TOPIC_GCP = "anthropic-inference";
  public static final String GEMINI_INFERENCE_TOPIC_GCP = "gemini-inference";
  public static final String GROK_INFERENCE_TOPIC_GCP = "grok-inference";
  public static final String OLLAMA_INFERENCE_TOPIC_GCP = "ollama-inference";
  public static final String DEEPSEEK_INFERENCE_TOPIC_GCP = "deepseek-inference";
  public static final String HUGGINGFACE_INFERENCE_TOPIC_GCP = "huggingface-inference";
  public static final String LLAMA_INFERENCE_TOPIC_GCP = "llama-inference";

  // Bulk topics for inference (jobs > 10)
  public static final String OPEN_AI_INFERENCE_TOPIC_GCP_BULK = "open-ai-inference-bulk";
  public static final String MISTRAL_INFERENCE_TOPIC_GCP_BULK = "mistral-inference-bulk";
  public static final String ANTHROPIC_INFERENCE_TOPIC_GCP_BULK = "anthropic-inference-bulk";
  public static final String GEMINI_INFERENCE_TOPIC_GCP_BULK = "gemini-inference-bulk";
  public static final String GROK_INFERENCE_TOPIC_GCP_BULK = "grok-inference-bulk";
  public static final String OLLAMA_INFERENCE_TOPIC_GCP_BULK = "ollama-inference-bulk";
  public static final String DEEPSEEK_INFERENCE_TOPIC_GCP_BULK = "deepseek-inference-bulk";
  public static final String HUGGINGFACE_INFERENCE_TOPIC_GCP_BULK = "huggingface-inference-bulk";
  public static final String LLAMA_INFERENCE_TOPIC_GCP_BULK = "llama-inference-bulk";

  public static final String OPEN_AI_INFERENCE_SUB_GCP = "open-ai-inference-sub";
  public static final String MISTRAL_INFERENCE_SUB_GCP = "mistral-inference-sub";
  public static final String ANTHROPIC_INFERENCE_SUB_GCP = "anthropic-inference-sub";
  public static final String GEMINI_INFERENCE_SUB_GCP = "gemini-inference-sub";
  public static final String GROK_INFERENCE_SUB_GCP = "grok-inference-sub";
  public static final String DEEPSEEK_INFERENCE_SUB_GCP = "deepseek-inference-sub";
  public static final String HUGGINGFACE_INFERENCE_SUB_GCP = "huggingface-inference-sub";
  public static final String OLLAMA_INFERENCE_SUB_GCP = "ollama-inference-sub";
  public static final String LLAMA_INFERENCE_SUB_GCP = "llama-inference-sub";

  // Bulk subscriptions for inference (jobs > 10)
  public static final String OPEN_AI_INFERENCE_SUB_GCP_BULK = "open-ai-inference-bulk-sub";
  public static final String MISTRAL_INFERENCE_SUB_GCP_BULK = "mistral-inference-bulk-sub";
  public static final String ANTHROPIC_INFERENCE_SUB_GCP_BULK = "anthropic-inference-bulk-sub";
  public static final String GEMINI_INFERENCE_SUB_GCP_BULK = "gemini-inference-bulk-sub";
  public static final String GROK_INFERENCE_SUB_GCP_BULK = "grok-inference-bulk-sub";
  public static final String DEEPSEEK_INFERENCE_SUB_GCP_BULK = "deepseek-inference-bulk-sub";
  public static final String HUGGINGFACE_INFERENCE_SUB_GCP_BULK = "huggingface-inference-bulk-sub";
  public static final String OLLAMA_INFERENCE_SUB_GCP_BULK = "ollama-inference-bulk-sub";
  public static final String LLAMA_INFERENCE_SUB_GCP_BULK = "llama-inference-bulk-sub";

  public static final String OPEN_AI_EVAL_TOPIC_GCP = "open-ai-eval";
  public static final String MISTRAL_EVAL_TOPIC_GCP = "mistral-eval";
  public static final String ANTHROPIC_EVAL_TOPIC_GCP = "anthropic-eval";
  public static final String GEMINI_EVAL_TOPIC_GCP = "gemini-eval";
  public static final String GROK_EVAL_TOPIC_GCP = "grok-eval";
  public static final String OLLAMA_EVAL_TOPIC_GCP = "ollama-eval";
  public static final String DEEPSEEK_EVAL_TOPIC_GCP = "deepseek-eval";
  public static final String HUGGINGFACE_EVAL_TOPIC_GCP = "huggingface-eval";
  public static final String LLAMA_EVAL_TOPIC_GCP = "llama-eval";

  // Bulk topics for evaluation (jobs > 10)
  public static final String OPEN_AI_EVAL_TOPIC_GCP_BULK = "open-ai-eval-bulk";
  public static final String MISTRAL_EVAL_TOPIC_GCP_BULK = "mistral-eval-bulk";
  public static final String ANTHROPIC_EVAL_TOPIC_GCP_BULK = "anthropic-eval-bulk";
  public static final String GEMINI_EVAL_TOPIC_GCP_BULK = "gemini-eval-bulk";
  public static final String GROK_EVAL_TOPIC_GCP_BULK = "grok-eval-bulk";
  public static final String OLLAMA_EVAL_TOPIC_GCP_BULK = "ollama-eval-bulk";
  public static final String DEEPSEEK_EVAL_TOPIC_GCP_BULK = "deepseek-eval-bulk";
  public static final String HUGGINGFACE_EVAL_TOPIC_GCP_BULK = "huggingface-eval-bulk";
  public static final String LLAMA_EVAL_TOPIC_GCP_BULK = "llama-eval-bulk";

  public static final String OPEN_AI_EVAL_SUB_GCP = "open-ai-eval-sub";
  public static final String MISTRAL_EVAL_SUB_GCP = "mistral-eval-sub";
  public static final String ANTHROPIC_EVAL_SUB_GCP = "anthropic-eval-sub";
  public static final String GEMINI_EVAL_SUB_GCP = "gemini-eval-sub";
  public static final String GROK_EVAL_SUB_GCP = "grok-eval-sub";
  public static final String OLLAMA_EVAL_SUB_GCP = "ollama-eval-sub";
  public static final String DEEPSEEK_EVAL_SUB_GCP = "deepseek-eval-sub";
  public static final String HUGGINGFACE_EVAL_SUB_GCP = "huggingface-eval-sub";
  public static final String LLAMA_EVAL_SUB_GCP = "llama-eval-sub";

  // Bulk subscriptions for evaluation (jobs > 10)
  public static final String OPEN_AI_EVAL_SUB_GCP_BULK = "open-ai-eval-bulk-sub";
  public static final String MISTRAL_EVAL_SUB_GCP_BULK = "mistral-eval-bulk-sub";
  public static final String ANTHROPIC_EVAL_SUB_GCP_BULK = "anthropic-eval-bulk-sub";
  public static final String GEMINI_EVAL_SUB_GCP_BULK = "gemini-eval-bulk-sub";
  public static final String GROK_EVAL_SUB_GCP_BULK = "grok-eval-bulk-sub";
  public static final String OLLAMA_EVAL_SUB_GCP_BULK = "ollama-eval-bulk-sub";
  public static final String DEEPSEEK_EVAL_SUB_GCP_BULK = "deepseek-eval-bulk-sub";
  public static final String HUGGINGFACE_EVAL_SUB_GCP_BULK = "huggingface-eval-bulk-sub";
  public static final String LLAMA_EVAL_SUB_GCP_BULK = "llama-eval-bulk-sub";

  // Heuristic Channels
  public static final String HEURISTIC_EVAL_PUB_CHANNEL = "heuristicEvalPubChannel";
  public static final String HEURISTIC_EVAL_SUB_CHANNEL = "heuristicEvalSubChannel";

  // Bulk Heuristic Channels for evaluation (jobs > 10)
  public static final String HEURISTIC_EVAL_PUB_CHANNEL_BULK = "heuristicEvalPubChannelBulk";
  public static final String HEURISTIC_EVAL_SUB_CHANNEL_BULK = "heuristicEvalSubChannelBulk";

  // Heuristic Topic & Subscription
  public static final String HEURISTIC_EVAL_TOPIC_GCP = "heuristic-eval";
  public static final String HEURISTIC_EVAL_SUB_GCP = "heuristic-eval-sub";

  // Bulk Heuristic Topic & Subscription for evaluation (jobs > 10)
  public static final String HEURISTIC_EVAL_TOPIC_GCP_BULK = "heuristic-eval-bulk";
  public static final String HEURISTIC_EVAL_SUB_GCP_BULK = "heuristic-eval-bulk-sub";

  // GCP Feature Gate
  public static final String GCS_EVENTS_INPUT_CHANNEL = "gcs-feature-gate";
  public static final String GCS_EVENTS_SUBSCRIPTION = "gcs-feature-gate-sub";
}
