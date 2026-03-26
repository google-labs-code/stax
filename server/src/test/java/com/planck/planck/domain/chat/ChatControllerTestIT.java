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

package com.planck.planck.domain.chat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.dataset.service.DataSetService;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.DataSet;
import com.planck.planck.entitities.User;
import com.planck.planck.util.PlanckConstants;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ChatControllerTestIT extends IntegrationTestBase {

  @Autowired private ChatService chatService;
  @Autowired private UserRepository userRepository;
  @Autowired private DataSetService dataSetService;

  private String testChatId;
  private User testUser;
  private DataSet dataSet;

  @BeforeEach
  public void setUp() {
    super.setUp();

    // Create test data
    testUser = userRepository.findByEmail(PlanckConstants.DEFAULT_USER).orElseThrow();

    // Create a test chat with variables
    Map<String, String> variables = new HashMap<>();
    variables.put("source", "test");
    variables.put("version", "1.0");
    variables.put("category", "integration");

    dataSet = dataSetService.createDataSet(testUser, "test", null, null);
    Chat testChat = chatService.createChat("chat-test-123", dataSet, testUser, variables);
    testChatId = testChat.getId();
  }

  @Test
  public void testGetChatVariablesKeys() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/chat/{chatId}/variables/keys", testChatId)
                    .header("Authorization", getBearerJwtToken())
                    .contentType(
                        MediaType.APPLICATION_JSON)) // Use MediaType instead of ContentType
            // 4. Use MockMvcResultMatchers for assertions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    assertNotNull(responseBody, "Response body should not be null");
  }

  @Test
  public void testGetChatVariablesKeysEmpty() throws Exception {
    Chat chatWithoutVariables =
        chatService.createChat("chat-test-no-variables-123", dataSet, testUser, null);

    MvcResult result =
        mockMvc
            .perform(
                get("/chat/{chatId}/variables/keys", chatWithoutVariables.getId())
                    .header("Authorization", getBearerJwtToken())
                    .contentType(MediaType.APPLICATION_JSON))
            // 6. Use MockMvcResultMatchers
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    assertNotNull(responseBody, "Response body should not be null");
  }
}
