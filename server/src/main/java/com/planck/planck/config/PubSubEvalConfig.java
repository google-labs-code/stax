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

package com.planck.planck.config;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import com.planck.planck.domain.evaluation.dto.EvaluationDTO;
import com.planck.planck.domain.pubsub.LLMScoreQueueService;
import com.planck.planck.domain.pubsub.PointwiseHeuristicEvaluationScoreQueueService;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.QueueConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!monitoring")
public class PubSubEvalConfig {

  @Autowired LLMScoreQueueService llmScoreQueueService;

  @Autowired PointwiseHeuristicEvaluationScoreQueueService heuristicEvaluationScoreQueueService;

  // PUBLISHER GCP
  @Bean
  @ConditionalOnBean(PubSubTemplate.class)
  @ServiceActivator(inputChannel = QueueConstants.OPEN_AI_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToOpenAiEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.OPEN_AI_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GEMINI_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToGeminiEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.GEMINI_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.MISTRAL_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToMistralEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.MISTRAL_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.ANTHROPIC_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToAnthropicEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.ANTHROPIC_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GROK_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToGrokEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.GROK_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OLLAMA_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToOllamaEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.OLLAMA_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.DEEPSEEK_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToDeepseekEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.DEEPSEEK_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HUGGINGFACE_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToHuggingfaceEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.HUGGINGFACE_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.LLAMA_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToLlamaEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.LLAMA_EVAL_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HEURISTIC_EVAL_PUB_CHANNEL)
  public MessageHandler messageSenderToHeuristicEval(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.HEURISTIC_EVAL_TOPIC_GCP);
  }

  // BULK PUBLISHERS GCP
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OPEN_AI_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToOpenAiEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.OPEN_AI_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GEMINI_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToGeminiEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.GEMINI_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.MISTRAL_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToMistralEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.MISTRAL_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.ANTHROPIC_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToAnthropicEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.ANTHROPIC_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GROK_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToGrokEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.GROK_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OLLAMA_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToOllamaEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.OLLAMA_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.DEEPSEEK_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToDeepseekEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.DEEPSEEK_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HUGGINGFACE_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToHuggingfaceEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.HUGGINGFACE_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.LLAMA_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToLlamaEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.LLAMA_EVAL_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HEURISTIC_EVAL_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToHeuristicEvalBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.HEURISTIC_EVAL_TOPIC_GCP_BULK);
  }

  // Listener GCP

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterOpenAiEval(
      @Qualifier(QueueConstants.OPEN_AI_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.OPEN_AI_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OPEN_AI_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverOpenAiEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  // Listener GCP - Gemini
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterGeminiEval(
      @Qualifier(QueueConstants.GEMINI_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.GEMINI_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GEMINI_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverGeminiEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();

      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  // Listener GCP - Mistral
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterMistralEval(
      @Qualifier(QueueConstants.MISTRAL_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.MISTRAL_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnBean(PubSubTemplate.class)
  @ServiceActivator(inputChannel = QueueConstants.MISTRAL_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverMistralEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterAnthropicEval(
      @Qualifier(QueueConstants.ANTHROPIC_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.ANTHROPIC_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.ANTHROPIC_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverAnthropicEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterGrokEval(
      @Qualifier(QueueConstants.GROK_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.GROK_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GROK_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverGrokEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterOllamaEval(
      @Qualifier(QueueConstants.OLLAMA_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.OLLAMA_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OLLAMA_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverOllamaEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterDeepSeekEval(
      @Qualifier(QueueConstants.DEEPSEEK_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.DEEPSEEK_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.DEEPSEEK_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverDeepSeekEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterHuggingFaceEval(
      @Qualifier(QueueConstants.HUGGINGFACE_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.HUGGINGFACE_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HUGGINGFACE_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverHuggingFaceEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterLlamaEval(
      @Qualifier(QueueConstants.LLAMA_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.LLAMA_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.LLAMA_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverLlamaEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  // Listener GCP - Heuristic

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterHeuristicEval(
      @Qualifier(QueueConstants.HEURISTIC_EVAL_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.HEURISTIC_EVAL_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HEURISTIC_EVAL_SUB_CHANNEL)
  public MessageHandler messageReceiverHeuristicEval() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      heuristicEvaluationScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  // BULK LISTENERS GCP
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterOpenAiEvalBulk(
      @Qualifier(QueueConstants.OPEN_AI_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.OPEN_AI_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OPEN_AI_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverOpenAiEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterGeminiEvalBulk(
      @Qualifier(QueueConstants.GEMINI_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.GEMINI_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GEMINI_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverGeminiEvalBulk() {
    return message -> {
      log.info(
          "Bulk evaluation message arrived! Payload: " + new String((byte[]) message.getPayload()));
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();

      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterMistralEvalBulk(
      @Qualifier(QueueConstants.MISTRAL_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.MISTRAL_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.MISTRAL_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverMistralEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterAnthropicEvalBulk(
      @Qualifier(QueueConstants.ANTHROPIC_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.ANTHROPIC_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.ANTHROPIC_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverAnthropicEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterGrokEvalBulk(
      @Qualifier(QueueConstants.GROK_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.GROK_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GROK_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverGrokEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterDeepSeekEvalBulk(
      @Qualifier(QueueConstants.DEEPSEEK_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.DEEPSEEK_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.DEEPSEEK_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverDeepseekEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterOllamaEvalBulk(
      @Qualifier(QueueConstants.OLLAMA_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.OLLAMA_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OLLAMA_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverOllamaEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterHuggingFaceEvalBulk(
      @Qualifier(QueueConstants.HUGGINGFACE_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.HUGGINGFACE_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HUGGINGFACE_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverHuggingfaceEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterLlamaEvalBulk(
      @Qualifier(QueueConstants.LLAMA_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.LLAMA_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.LLAMA_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverLlamaEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      llmScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  // BULK LISTENERS GCP Heuristic
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterHeuristicEvalBulk(
      @Qualifier(QueueConstants.HEURISTIC_EVAL_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.HEURISTIC_EVAL_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HEURISTIC_EVAL_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverHeuristicEvalBulk() {
    return message -> {
      String evaluationDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      heuristicEvaluationScoreQueueService.process(
          ObjectMapperUtil.convertStringToObject(evaluationDTO, EvaluationDTO.class));
    };
  }

  @Bean
  public MessageChannel openAiEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel openAiEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel geminiEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel geminiEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mistralEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mistralEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel anthropicEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel anthropicEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel grokEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel grokEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel ollamaEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel ollamaEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel deepseekEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel deepseekEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel huggingfaceEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel huggingfaceEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel llamaEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel llamaEvalSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel heuristicEvalPubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel heuristicEvalSubChannel() {
    return new DirectChannel();
  }

  // BULK CHANNELS
  @Bean
  public MessageChannel openAiEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel openAiEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel geminiEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel geminiEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mistralEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mistralEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel anthropicEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel anthropicEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel grokEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel grokEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel ollamaEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel ollamaEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel deepseekEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel deepseekEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel huggingfaceEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel huggingfaceEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel llamaEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel llamaEvalSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel heuristicEvalPubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel heuristicEvalSubChannelBulk() {
    return new DirectChannel();
  }
}
