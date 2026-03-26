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
import com.planck.planck.domain.pubsub.UserDeletionJobQueueService;
import com.planck.planck.domain.user.dto.UserDeletionDTO;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.QueueConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PubSubUserDeletionConfig {

  @Autowired private UserDeletionJobQueueService userDeletionJobQueueService;

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = "userDeletionChannel")
  public MessageHandler messageSenderToUserDeletion(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, QueueConstants.USER_DELETION_TOPIC);
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapterUserDeletion(
      @Qualifier("userDeletionSubChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, QueueConstants.USER_DELETION_SUB_GCP);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);

    return adapter;
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = "userDeletionSubChannel")
  public MessageHandler messageReceiverUserDeletion() {
    return message -> {
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);

      try {
        String userDeletionDTOJson = new String((byte[]) message.getPayload());

        UserDeletionDTO userDeletionDTO =
            ObjectMapperUtil.convertStringToObject(userDeletionDTOJson, UserDeletionDTO.class);

        userDeletionJobQueueService.processUserDeletion(userDeletionDTO);

        originalMessage.ack();

        log.info(
            "User deletion message processed and acknowledged for user: {}",
            userDeletionDTO.getUserId());
      } catch (Exception e) {
        log.error("Failed to process user deletion message", e);
        throw new RuntimeException("Failed to process user deletion message", e);
      }
    };
  }

  @Bean("userDeletionChannel")
  public MessageChannel userDeletionChannel() {
    return new DirectChannel();
  }

  @Bean("userDeletionSubChannel")
  public MessageChannel userDeletionSubChannel() {
    return new DirectChannel();
  }
}
