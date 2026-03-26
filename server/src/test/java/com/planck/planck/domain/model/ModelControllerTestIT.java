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

package com.planck.planck.domain.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.model.dto.ModelDTO;
import com.planck.planck.domain.model.dto.ModelUpdateRequest;
import com.planck.planck.domain.model.dto.NewModelRegistrationWithCustomProperitesRequest;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.enums.ModelType;
import com.planck.planck.enums.Role;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ModelControllerTestIT extends IntegrationTestBase {
  private String authorizationHeader = null;

  @Autowired private ModelRepository modelRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ObjectMapper objectMapper;

  private String existingModelId = null;
  private String existingModelLabel = null;

  @BeforeEach
  public void setUp() {
    super.setUp();

    authorizationHeader = getBearerJwtToken();

    if (existingModelId == null || existingModelLabel == null) {
      List<Model> models = modelRepository.findModelsByTypeAndNotDeprecated(ModelType.SYSTEM);
      Optional<Model> firstModel = models.stream().findFirst();

      if (!firstModel.isPresent()) throw new RuntimeException("No models found in the database");

      existingModelId = firstModel.get().getId();
      existingModelLabel = firstModel.get().getLabel();
    }
  }

  @Test
  void getModelsList_all() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/model/list")
                    .header("Authorization", authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    List<ModelDTO> resultList =
        objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<List<ModelDTO>>() {});

    assertNotNull(resultList, "Response body should not be null");
    assertFalse(resultList.isEmpty(), "Response body should not be empty");

    assertTrue(
        resultList.stream().anyMatch(model -> model.getProvider().equals(ModelProvider.GOOGLE)),
        "GOOGLE models should be present");
    assertTrue(
        resultList.stream().anyMatch(model -> model.getProvider().equals(ModelProvider.OPENAI)),
        "OPENAI models should be present");
    assertTrue(
        resultList.stream().anyMatch(model -> model.getProvider().equals(ModelProvider.ANTHROPIC)),
        "ANTHROPIC models should be present");
    assertTrue(
        resultList.stream().anyMatch(model -> model.getProvider().equals(ModelProvider.MISTRAL)),
        "MISTRAL models should be present");
  }

  @Test
  void duplicateAndModify_Success() throws Exception {
    NewModelRegistrationWithCustomProperitesRequest duplicateRequest =
        new NewModelRegistrationWithCustomProperitesRequest();
    String uniqueLabel = "IT Label - " + System.currentTimeMillis(); // Ensure unique label
    duplicateRequest.setLabel(uniqueLabel);
    duplicateRequest.setNewModelproperties(Map.of("temperature", 0.7)); // Example property

    String postPath = getDuplicateModelPath(existingModelId);

    MvcResult result = callDuplicateAndModify(duplicateRequest, postPath);

    assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(), "Expected HTTP 200");

    ModelDTO resultDTO =
        objectMapper.readValue(result.getResponse().getContentAsString(), ModelDTO.class);

    assertNotNull(resultDTO, "Body should not be null");
    assertEquals(uniqueLabel, resultDTO.getLabel(), "Label should be equal to the payload");
    assertNotNull(resultDTO.getId(), "Duplicated model should have a new ID");
  }

  @Test
  void duplicateAndModify_ModelNotFound() throws Exception {
    String invalidModelId = "non-existent-model-id-12345";
    String postPath = getDuplicateModelPath(invalidModelId);

    NewModelRegistrationWithCustomProperitesRequest duplicateRequest =
        new NewModelRegistrationWithCustomProperitesRequest();
    duplicateRequest.setLabel("IT Label - Not Found Test");
    duplicateRequest.setNewModelproperties(Map.of("temperature", 0.7));

    mockMvc
        .perform(
            post(postPath)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateModel_invalidModelId() throws Exception {
    String modelId = "invalid";
    String patchPath = getUpdateModelPath(modelId);

    ModelUpdateRequest updateRequest = new ModelUpdateRequest();
    updateRequest.setLabel("Updated Label");

    mockMvc
        .perform(
            patch(patchPath)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateModel_NotFound_ForSystemModel() throws Exception {
    String modelId = existingModelId; // System model ID
    String patchPath = getUpdateModelPath(modelId);

    ModelUpdateRequest updateRequest = new ModelUpdateRequest();
    updateRequest.setLabel("Updated Label");

    mockMvc
        .perform(
            patch(patchPath)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateModel_Success() throws Exception {
    // set up a user model
    Model model = new Model();
    model.setLabel("Test Label - Update Success");
    model.setProvider(ModelProvider.GOOGLE);
    model.setType(ModelType.USER);
    model.setDeprecated(false);
    model.setName("Test model name");

    // Find a user with Role.USER to own the model
    User testUser =
        userRepository.findAll().stream()
            .filter(user -> user.getRole().equals(Role.USER))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No user with role USER found"));

    model.setUser(testUser);
    model.setUrl("https://testUrl");
    model.setProperties("{}");
    model = modelRepository.save(model);

    String modelId = model.getId();
    String patchPath = getUpdateModelPath(modelId);

    ModelUpdateRequest request = new ModelUpdateRequest();
    request.setLabel("Updated Label");
    request.setDescription("Updated Description");
    request.setComments("Updated Comments");

    MvcResult result =
        mockMvc
            .perform(
                patch(patchPath)
                    .header("Authorization", authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.label", equalTo("Updated Label")))
            .andExpect(jsonPath("$.description", equalTo("Updated Description")))
            .andReturn();

    ModelDTO resultDTO =
        objectMapper.readValue(result.getResponse().getContentAsString(), ModelDTO.class);

    assertNotNull(resultDTO, "Body should not be null");
    assertEquals("Updated Label", resultDTO.getLabel(), "Label should be updated");
    assertEquals(
        "Updated Description", resultDTO.getDescription(), "Description should be updated");
  }

  @Test
  void getModelProviders_shouldReturnAllProviders_usingJsonPath() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/model/providers")
                    .header("Authorization", authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.OPENAI", is("OpenAI")))
            .andExpect(jsonPath("$.MISTRAL", is("Mistral")))
            .andExpect(jsonPath("$.GOOGLE", is("Google")))
            .andExpect(jsonPath("$.ANTHROPIC", is("Anthropic")))
            .andExpect(jsonPath("$.GROK", is("Grok")))
            .andExpect(jsonPath("$.DEEPSEEK", is("DeepSeek")))
            .andReturn();

    Map<String, String> resultProviders =
        objectMapper.readValue(
            result.getResponse().getContentAsString(), new TypeReference<Map<String, String>>() {});
    String firstKey = resultProviders.keySet().iterator().next();
    assertNotNull(firstKey, "Provider key should not be null");
  }

  private String getUpdateModelPath(String modelId) {
    return String.format("/model/%s", modelId);
  }

  private String getDuplicateModelPath(String modelId) {
    return String.format("/model/%s/duplicate-and-modify", modelId);
  }

  private MvcResult callDuplicateAndModify(
      NewModelRegistrationWithCustomProperitesRequest duplicateRequest, String postPath)
      throws Exception {
    return mockMvc
        .perform(
            post(postPath)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
        .andReturn();
  }
}
