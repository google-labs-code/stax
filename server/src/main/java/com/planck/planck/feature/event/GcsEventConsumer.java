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

package com.planck.planck.feature.event;

import static com.planck.planck.util.QueueConstants.GCS_EVENTS_INPUT_CHANNEL;

import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.planck.planck.feature.config.FeatureGateKeyProvider;
import com.planck.planck.feature.dto.Feature;
import com.planck.planck.feature.dto.FeatureConfig;
import com.planck.planck.feature.dto.Metadata;
import com.planck.planck.feature.service.FeatureGateService;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "spring.cloud.gcp.pubsub.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class GcsEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(GcsEventConsumer.class);

  private final Storage storageClient;
  private final StringRedisTemplate redisTemplate;
  private final String expectedFileName;
  private final String redisHashKey;
  private final String metadataRedisKey;
  private final FeatureGateService featureGateService;

  public GcsEventConsumer(
      Storage storageClient,
      StringRedisTemplate redisTemplate,
      FeatureGateService featureGateService,
      FeatureGateKeyProvider keyProvider) {
    this.storageClient = storageClient;
    this.redisTemplate = redisTemplate;
    this.featureGateService = featureGateService;
    this.expectedFileName = keyProvider.getExpectedFileName();
    this.redisHashKey = keyProvider.getRedisHashKey();
    this.metadataRedisKey = keyProvider.getMetadataRedisKey();
  }

  @Bean
  @ConditionalOnProperty(
      name = "spring.cloud.gcp.pubsub.enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ServiceActivator(inputChannel = GCS_EVENTS_INPUT_CHANNEL)
  public MessageHandler messageReceiver() {
    return message -> {
      String rawPayload = new String((byte[]) message.getPayload());
      BasicAcknowledgeablePubsubMessage originalMessage =
          message
              .getHeaders()
              .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);

      log.info("Received Pub/Sub message with payload: {}", rawPayload);

      try {
        GcsEvent event =
            ObjectMapperUtil.getObectMapperInstance().readValue(rawPayload, GcsEvent.class);
        if (!expectedFileName.equals(event.name())) {
          log.warn(
              "Ignoring event for unexpected file: {}. Expected: {}",
              event.name(),
              expectedFileName);
          originalMessage.ack();
          return;
        }

        Blob blob = storageClient.get(event.bucket(), event.name());
        String jsonContent = new String(blob.getContent());

        FeatureConfig config =
            ObjectMapperUtil.getObectMapperInstance().readValue(jsonContent, FeatureConfig.class);

        Map<String, String> redisMap = new HashMap<>();
        for (Feature feature : config.features()) {
          redisMap.put(feature.key(), String.valueOf(feature.enabled()));
        }

        updateRedisAtomically(redisMap, config.metadata());

        log.info("Successfully updated Redis with {} features and metadata.", redisMap.size());

        featureGateService.forceSynchronize();

        originalMessage.ack();

      } catch (Exception e) {
        log.error("Fatal error processing GCS event. Message will be NACK'd for retry.", e);
        if (originalMessage != null) {
          originalMessage.nack();
        }
      }
    };
  }

  private void updateRedisAtomically(Map<String, String> featuresMap, Metadata metadata) {
    redisTemplate.execute(
        new SessionCallback<List<Object>>() {
          @Override
          public <K, V> List<Object> execute(RedisOperations<K, V> operations)
              throws DataAccessException {
            StringRedisTemplate stringOps = (StringRedisTemplate) operations;

            stringOps.multi();

            stringOps.delete(redisHashKey);
            stringOps.delete(metadataRedisKey);

            if (!featuresMap.isEmpty()) {
              stringOps.opsForHash().putAll(redisHashKey, featuresMap);
            }

            if (metadata != null) {
              try {
                String metadataJson =
                    ObjectMapperUtil.getObectMapperInstance().writeValueAsString(metadata);
                stringOps.opsForValue().set(metadataRedisKey, metadataJson);
              } catch (Exception e) {
                log.error("Could not serialize metadata, discarding transaction.", e);
                operations.discard();
                throw new IllegalStateException("Metadata serialization failed", e);
              }
            }

            return operations.exec();
          }
        });
  }
}
