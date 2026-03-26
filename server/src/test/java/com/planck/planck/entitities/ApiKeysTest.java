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

package com.planck.planck.entitities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

public class ApiKeysTest {

  @Test
  public void apiKeysTest() {

    String ID_PRFIX = "api-keys-";
    String anthropicKey = "anthropicKey";
    String openaiKey = "openaiKey";
    String mistralKey = "mistralKey";
    String googleKey = "googleKey";
    String grokKey = "grokKey";
    String ollamaKey = "ollamaKey";
    String deepseekKey = "deepseekKey";
    String huggingfaceKey = "huggingfaceKey";

    User user = new User();
    String firstName = "firstName";
    user.setFirstName(firstName);

    Timestamp createdAt = new Timestamp(1234567895);
    Timestamp updatedAt = new Timestamp(1234567895);

    ApiKeys apiKeys = new ApiKeys();
    apiKeys.setID_PREFIX(ID_PRFIX);
    apiKeys.prePersist();
    apiKeys.prePersist();
    apiKeys.setAnthropicKey(anthropicKey);
    apiKeys.setOpenaiKey(openaiKey);
    apiKeys.setMistralKey(mistralKey);
    apiKeys.setGoogleKey(googleKey);
    apiKeys.setGrokKey(grokKey);
    apiKeys.setOllamaKey(ollamaKey);
    apiKeys.setDeepseekKey(deepseekKey);
    apiKeys.setHuggingfaceKey(huggingfaceKey);
    apiKeys.setUser(user);
    apiKeys.setCreatedAt(createdAt);
    apiKeys.setUpdatedAt(updatedAt);

    assertEquals(ID_PRFIX, apiKeys.getID_PREFIX());
    assertTrue(apiKeys.getId().contains("api-keys-"));
    assertEquals(anthropicKey, apiKeys.getAnthropicKey());
    assertEquals(openaiKey, apiKeys.getOpenaiKey());
    assertEquals(mistralKey, apiKeys.getMistralKey());
    assertEquals(googleKey, apiKeys.getGoogleKey());
    assertEquals(grokKey, apiKeys.getGrokKey());
    assertEquals(ollamaKey, apiKeys.getOllamaKey());
    assertEquals(deepseekKey, apiKeys.getDeepseekKey());
    assertEquals(huggingfaceKey, apiKeys.getHuggingfaceKey());
    assertEquals(firstName, apiKeys.getUser().getFirstName());
    assertNotNull(apiKeys.getCreatedAt());
    assertNotNull(apiKeys.getUpdatedAt());
  }
}
