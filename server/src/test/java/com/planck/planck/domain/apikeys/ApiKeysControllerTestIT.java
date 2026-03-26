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

package com.planck.planck.domain.apikeys;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.apikeys.dto.ApiKeysRequestDTO;
import com.planck.planck.domain.apikeys.service.ApiKeysService;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.ApiKeys;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.util.PlanckConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ApiKeysControllerTestIT extends IntegrationTestBase {
  @Autowired private ApiKeysService apiKeysService;
  @Autowired private UserRepository userRepository;
  @Autowired private ApiKeysRepository apiKeysRepository;
  @Autowired private ObjectMapper objectMapper;

  private User testUser;

  @BeforeEach
  public void setUp() {
    super.setUp();

    testUser = userRepository.findByEmail(PlanckConstants.DEFAULT_USER).orElseThrow();
    apiKeysService.getApiKeyPresentSet(testUser);
  }

  @Test
  @Transactional
  void getApiKeysByUser_shouldReturnApiKeysResponseDTO() throws Exception {
    mockMvc
        .perform(get("/api-keys/get-keys-by-user").header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openaiKeyPresent", equalTo(false)))
        .andExpect(jsonPath("$.googleKeyPresent", equalTo(false)));
  }

  @Test
  @Transactional
  void setKey_shouldReturnSuccess() throws Exception {
    ApiKeysRequestDTO requestDTO = new ApiKeysRequestDTO(ModelProvider.OPENAI, "dummy-key", null);

    mockMvc
        .perform(
            post("/api-keys/set-key")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON) // Set Content-Type
                .content(objectMapper.writeValueAsString(requestDTO))) // Serialize DTO to JSON
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", equalTo("Success")));
  }

  @Test
  @Transactional
  void deleteApiKeyByUser_shouldReturnSuccess() throws Exception {
    ApiKeys key = new ApiKeys();
    key.setUser(testUser);
    key.setOpenaiKey("test");
    apiKeysRepository.save(key);

    ApiKeysRequestDTO deleteRequestDTO = new ApiKeysRequestDTO(ModelProvider.OPENAI, null, null);

    mockMvc
        .perform(
            delete("/api-keys/delete-key")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", equalTo("API Key deleted successfully")));
  }
}
