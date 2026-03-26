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

import static com.planck.planck.util.QueueConstants.GCS_EVENTS_INPUT_CHANNEL;
import static com.planck.planck.util.QueueConstants.GCS_EVENTS_SUBSCRIPTION;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;

@Configuration
public class PubSubConfiguration {

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public PubSubInboundChannelAdapter messageChannelAdapter(
      @Qualifier(GCS_EVENTS_INPUT_CHANNEL) MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {

    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, GCS_EVENTS_SUBSCRIPTION);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }
}
