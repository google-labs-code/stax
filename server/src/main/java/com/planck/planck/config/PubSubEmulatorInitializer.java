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

import com.google.cloud.spring.pubsub.PubSubAdmin;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@ConfigurationProperties(prefix = "pubsub.emulator")
public class PubSubEmulatorInitializer {

  private final PubSubAdmin pubSubAdmin;
  private Map<String, String> topics;

  public PubSubEmulatorInitializer(PubSubAdmin pubSubAdmin) {
    this.pubSubAdmin = pubSubAdmin;
  }

  public Map<String, String> getTopics() {
    return topics;
  }

  public void setTopics(Map<String, String> topics) {
    this.topics = topics;
  }

  @PostConstruct
  public void init() {
    System.out.println("Initializing PubSub Emulator");
    topics.forEach(
        (topic, subscription) -> {
          if (pubSubAdmin.getTopic(topic) == null) {
            pubSubAdmin.createTopic(topic);
          }
          if (pubSubAdmin.getSubscription(subscription) == null) {
            pubSubAdmin.createSubscription(subscription, topic);
          }
        });
  }
}
