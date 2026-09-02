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
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inference.service.InferenceService;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.QueueConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class PubSubInferenceConfig {

  private final InferenceService inferenceService;

  // PUBLISHER GCP
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OPEN_AI_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToOpenAiInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.OPEN_AI_INFERENCE_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GEMINI_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToGeminiInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.GEMINI_INFERENCE_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.MISTRAL_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToMistralInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.MISTRAL_INFERENCE_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.ANTHROPIC_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToAnthropicInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.ANTHROPIC_INFERENCE_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GROK_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToGrokInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.GROK_INFERENCE_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OLLAMA_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToOllamaInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.OLLAMA_INFERENCE_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.DEEPSEEK_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToDeepseekInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.DEEPSEEK_INFERENCE_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HUGGINGFACE_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToHuggingfaceInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.HUGGINGFACE_INFERENCE_TOPIC_GCP);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.LLAMA_INFERENCE_PUB_CHANNEL)
  public MessageHandler messageSenderToLlamaInference(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.LLAMA_INFERENCE_TOPIC_GCP);
  }

  // BULK PUBLISHERS GCP
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OPEN_AI_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToOpenAiInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(
        pubsubTemplate, QueueConstants.OPEN_AI_INFERENCE_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GEMINI_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToGeminiInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.GEMINI_INFERENCE_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.MISTRAL_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToMistralInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(
        pubsubTemplate, QueueConstants.MISTRAL_INFERENCE_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.ANTHROPIC_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToAnthropicInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(
        pubsubTemplate, QueueConstants.ANTHROPIC_INFERENCE_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GROK_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToGrokInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.GROK_INFERENCE_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OLLAMA_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToOllamaInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.OLLAMA_INFERENCE_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.DEEPSEEK_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToDeepseekInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(
        pubsubTemplate, QueueConstants.DEEPSEEK_INFERENCE_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HUGGINGFACE_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToHuggingfaceInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(
        pubsubTemplate, QueueConstants.HUGGINGFACE_INFERENCE_TOPIC_GCP_BULK);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.LLAMA_INFERENCE_PUB_CHANNEL_BULK)
  public MessageHandler messageSenderToLlamaInferenceBulk(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.LLAMA_INFERENCE_TOPIC_GCP_BULK);
  }

  // Listener GCP

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterOpenAiInference(
      @Qualifier(QueueConstants.OPEN_AI_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.OPEN_AI_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OPEN_AI_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverOpenAiInference() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  // Listener GCP - Gemini
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterGeminiInference(
      @Qualifier(QueueConstants.GEMINI_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.GEMINI_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GEMINI_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverGeminiInference() {
    return message -> {
      log.info("Message arrived! Message ID: {}", message.getHeaders().getId());
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();

      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  // Listener GCP - Mistral
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterMistralInference(
      @Qualifier(QueueConstants.MISTRAL_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.MISTRAL_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.MISTRAL_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverMistralInference() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterAnthropicInference(
      @Qualifier(QueueConstants.ANTHROPIC_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.ANTHROPIC_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.ANTHROPIC_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverAnthropicInference() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterGrokInference(
      @Qualifier(QueueConstants.GROK_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.GROK_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GROK_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverGrokInference() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  // Listener GCP - DeepSeek
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterDeepSeekInference(
      @Qualifier(QueueConstants.DEEPSEEK_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.DEEPSEEK_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.DEEPSEEK_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverDeepSeekInference() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterOllamaInference(
      @Qualifier(QueueConstants.OLLAMA_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.OLLAMA_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OLLAMA_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverOllamaInference() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterHuggingFaceInference(
      @Qualifier(QueueConstants.HUGGINGFACE_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.HUGGINGFACE_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HUGGINGFACE_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverHuggingFaceInference() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterLlamaInference(
      @Qualifier(QueueConstants.LLAMA_INFERENCE_SUB_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.LLAMA_INFERENCE_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.LLAMA_INFERENCE_SUB_CHANNEL)
  public MessageHandler messageReceiverLlamaInference() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  // BULK LISTENERS GCP
  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterOpenAiInferenceBulk(
      @Qualifier(QueueConstants.OPEN_AI_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.OPEN_AI_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OPEN_AI_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverOpenAiInferenceBulk() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterGeminiInferenceBulk(
      @Qualifier(QueueConstants.GEMINI_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.GEMINI_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GEMINI_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverGeminiInferenceBulk() {
    return message -> {
      log.info("Bulk message arrived! Message ID: {}", message.getHeaders().getId());
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();

      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterMistralInferenceBulk(
      @Qualifier(QueueConstants.MISTRAL_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.MISTRAL_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.MISTRAL_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverMistralInferenceBulk() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterAnthropicInferenceBulk(
      @Qualifier(QueueConstants.ANTHROPIC_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.ANTHROPIC_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.ANTHROPIC_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverAnthropicInferenceBulk() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterGrokInferenceBulk(
      @Qualifier(QueueConstants.GROK_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.GROK_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.GROK_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverGrokInferenceBulk() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterDeepSeekInferenceBulk(
      @Qualifier(QueueConstants.DEEPSEEK_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.DEEPSEEK_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.DEEPSEEK_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverDeepSeekInferenceBulk() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterOllamaInferenceBulk(
      @Qualifier(QueueConstants.OLLAMA_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.OLLAMA_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.OLLAMA_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverOllamaInferenceBulk() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterHuggingFaceInferenceBulk(
      @Qualifier(QueueConstants.HUGGINGFACE_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.HUGGINGFACE_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.HUGGINGFACE_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverHuggingFaceInferenceBulk() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterLlamaInferenceBulk(
      @Qualifier(QueueConstants.LLAMA_INFERENCE_SUB_CHANNEL_BULK) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, QueueConstants.LLAMA_INFERENCE_SUB_GCP_BULK);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = QueueConstants.LLAMA_INFERENCE_SUB_CHANNEL_BULK)
  public MessageHandler messageReceiverLlamaInferenceBulk() {
    return message -> {
      String inferenceDTO = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      originalMessage.ack();
      inferenceService.process(
          ObjectMapperUtil.convertStringToObject(inferenceDTO, InferenceDTO.class));
    };
  }

  @Bean
  public MessageChannel openAiInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel openAiInferenceSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel geminiInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel geminiInferenceSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mistralInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mistralInferenceSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel anthropicInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel anthropicInferenceSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel grokInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel grokInferenceSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel ollamaInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel ollamaInferenceSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel deepseekInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel deepseekInferenceSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel huggingfaceInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel huggingfaceInferenceSubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel llamaInferencePubChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel llamaInferenceSubChannel() {
    return new DirectChannel();
  }

  // BULK CHANNELS
  @Bean
  public MessageChannel openAiInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel openAiInferenceSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel geminiInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel geminiInferenceSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mistralInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel mistralInferenceSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel anthropicInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel anthropicInferenceSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel grokInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel grokInferenceSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel ollamaInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel ollamaInferenceSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel deepseekInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel deepseekInferenceSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel huggingfaceInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel huggingfaceInferenceSubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel llamaInferencePubChannelBulk() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel llamaInferenceSubChannelBulk() {
    return new DirectChannel();
  }
}
