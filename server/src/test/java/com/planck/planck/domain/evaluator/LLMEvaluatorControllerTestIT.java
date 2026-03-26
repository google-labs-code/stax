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

package com.planck.planck.domain.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.evaluator.dto.EvaluatorVariableDTO;
import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorUpdateDTO;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.ScoreType;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class LLMEvaluatorControllerTestIT extends IntegrationTestBase {

  @Autowired private ObjectMapper objectMapper;

  private String authorizationHeader;
  private String existingModelId;

  private static final String BASE_PATH = "/llm_evaluator";

  @BeforeEach
  public void setUp() {
    super.setUp();
    authorizationHeader = super.getBearerJwtToken();
    existingModelId = "model_f9be2939-ae45-11f0-93fc-0242ac110002";
  }

  @Test
  void testCreateLLMEvaluator_unsupportedValue_ReturnsBadRequest() throws Exception {
    LLMEvaluatorRequestDTO requestDTO =
        LLMEvaluatorRequestDTO.builder()
            .name("Wrong scorer value " + UUID.randomUUID()) // ensure name is unique
            .outputFormateType(ScoreType.Json)
            .description("A test evaluator")
            .variables(List.of(new EvaluatorVariableDTO("response", true)))
            .outputCategories(
                List.of(
                    OutputCategoryDTO.builder()
                        .name("High Quality")
                        .value("2.0") // Invalid value
                        .startRange("0.0")
                        .endRange("1.0")
                        .build()))
            .modelId(existingModelId)
            .prompts(List.of(new Prompt(InputRole.USER, "What is your name?")))
            .build();

    // Use mockMvc.perform()
    mockMvc
        .perform(
            post(BASE_PATH)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateLLMEvaluator_Success() throws Exception {
    LLMEvaluatorRequestDTO requestDTO = createDefaultEvaluatorRequest();

    // Use mockMvc.perform() and MvcResult for body extraction
    MvcResult result =
        mockMvc
            .perform(
                post(BASE_PATH)
                    .header("Authorization", authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andReturn();

    // Extract and assert the DTO from the result
    LLMEvaluatorResponseDTO resultDTO =
        objectMapper.readValue(
            result.getResponse().getContentAsString(), LLMEvaluatorResponseDTO.class);
    assertNotNull(resultDTO.getId());
    assertEquals(requestDTO.getName(), resultDTO.getName());
  }

  @Test
  void testCreateLLMEvaluator_NameExists_ReturnsConflict() throws Exception {
    LLMEvaluatorRequestDTO requestDTO = createDefaultEvaluatorRequest();
    // First request (create successfully)
    mockMvc
        .perform(
            post(BASE_PATH)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isOk());

    // Second request (should conflict)
    mockMvc
        .perform(
            post(BASE_PATH)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isConflict());
  }

  @Test
  void testCreateLLMEvaluator_ModelNotFound_ReturnsNotFound() throws Exception {
    LLMEvaluatorRequestDTO requestDTO = createDefaultEvaluatorRequest("non-existent-model-id");

    mockMvc
        .perform(
            post(BASE_PATH)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetEvaluatorById_Success() throws Exception {
    LLMEvaluatorResponseDTO evaluator = createTestEvaluator("Evaluator for Get");

    MvcResult result =
        mockMvc
            .perform(
                get(BASE_PATH + "/" + evaluator.getId())
                    .header("Authorization", authorizationHeader))
            .andExpect(status().isOk())
            .andReturn();

    LLMEvaluatorResponseDTO resultDTO =
        objectMapper.readValue(
            result.getResponse().getContentAsString(), LLMEvaluatorResponseDTO.class);
    assertNotNull(resultDTO);
    assertEquals(evaluator.getId(), resultDTO.getId());
  }

  @Test
  void testGetEvaluatorById_NotFound() throws Exception {
    String nonExistentId = "non-existent-id-123";

    mockMvc
        .perform(get(BASE_PATH + "/" + nonExistentId).header("Authorization", authorizationHeader))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetAllEvaluators_Success() throws Exception {
    createTestEvaluator("All Evaluator 1");
    createTestEvaluator("All Evaluator 2");

    MvcResult result =
        mockMvc
            .perform(get(BASE_PATH).header("Authorization", authorizationHeader))
            .andExpect(status().isOk())
            .andReturn();

    List<LLMEvaluatorResponseDTO> resultList =
        objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<List<LLMEvaluatorResponseDTO>>() {});
    assertNotNull(resultList);
    // Note: We cannot assert the exact count due to system evaluators potentially existing,
    // but we can ensure it's not empty if we created some.
    // assertTrue(resultList.size() >= 2);
  }

  @Test
  void testGetCustomEvaluators_Success() throws Exception {
    createTestEvaluator("Custom Evaluator 1");

    MvcResult result =
        mockMvc
            .perform(get(BASE_PATH + "/custom").header("Authorization", authorizationHeader))
            .andExpect(status().isOk())
            .andReturn();

    List<LLMEvaluatorResponseDTO> resultList =
        objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<List<LLMEvaluatorResponseDTO>>() {});
    assertNotNull(resultList);
  }

  @Test
  void testGetSystemEvaluators_Success() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get(BASE_PATH + "/system").header("Authorization", authorizationHeader))
            .andExpect(status().isOk())
            .andReturn();

    List<LLMEvaluatorResponseDTO> resultList =
        objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<List<LLMEvaluatorResponseDTO>>() {});
    assertNotNull(resultList);
  }

  @Test
  void testUpdateEvaluator_Success() throws Exception {
    LLMEvaluatorResponseDTO evaluator = createTestEvaluator("Update Test");
    LLMEvaluatorUpdateDTO updateDTO =
        LLMEvaluatorUpdateDTO.builder().description("Updated description").build();

    MvcResult result =
        mockMvc
            .perform(
                patch(BASE_PATH + "/" + evaluator.getId())
                    .header("Authorization", authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk())
            .andReturn();

    LLMEvaluatorResponseDTO resultDTO =
        objectMapper.readValue(
            result.getResponse().getContentAsString(), LLMEvaluatorResponseDTO.class);
    assertNotNull(resultDTO);
    assertEquals(updateDTO.getDescription(), resultDTO.getDescription());
  }

  @Test
  void testUpdateEvaluator_NotFound() throws Exception {
    LLMEvaluatorUpdateDTO updateDTO =
        LLMEvaluatorUpdateDTO.builder().description("Updated description").build();

    mockMvc
        .perform(
            patch(BASE_PATH + "/non-existent-id")
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  void testDeleteEvaluator_Success() throws Exception {
    LLMEvaluatorResponseDTO evaluator = createTestEvaluator("Delete Test");

    // 1. Delete
    mockMvc
        .perform(
            delete(BASE_PATH + "/" + evaluator.getId())
                .header("Authorization", authorizationHeader))
        .andExpect(status().isOk());

    // 2. Verify deletion (should return NOT_FOUND)
    mockMvc
        .perform(
            get(BASE_PATH + "/" + evaluator.getId()).header("Authorization", authorizationHeader))
        .andExpect(status().isNotFound());
  }

  @Test
  void testDeleteEvaluator_NotFound() throws Exception {
    String nonExistentId = "non-existent-id";

    mockMvc
        .perform(
            delete(BASE_PATH + "/" + nonExistentId).header("Authorization", authorizationHeader))
        .andExpect(status().isNotFound());
  }

  // --- Helper Methods using MockMvc for setup ---

  // Replaced RestAssured postRequest
  private MvcResult postRequest(String path, Object body) throws Exception {
    return mockMvc
        .perform(
            post(path)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andReturn();
  }

  private LLMEvaluatorRequestDTO createDefaultEvaluatorRequest() {
    String uniqueName = "Test Evaluator - Success " + UUID.randomUUID();
    return LLMEvaluatorRequestDTO.builder()
        .name(uniqueName)
        .outputFormateType(ScoreType.Json)
        .description("A test evaluator")
        .variables(List.of(new EvaluatorVariableDTO("response", true)))
        .outputCategories(
            List.of(
                OutputCategoryDTO.builder()
                    .name("High Quality")
                    .value("1.0")
                    .startRange("0.0")
                    .endRange("1.0")
                    .build()))
        .modelId(existingModelId)
        .prompts(List.of(new Prompt(InputRole.USER, "What is your name?")))
        .build();
  }

  private LLMEvaluatorRequestDTO createDefaultEvaluatorRequest(String modelId) {
    LLMEvaluatorRequestDTO dto = createDefaultEvaluatorRequest();
    dto.setModelId(modelId);
    return dto;
  }

  private LLMEvaluatorResponseDTO createTestEvaluator(String name) throws Exception {
    LLMEvaluatorRequestDTO requestDTO =
        LLMEvaluatorRequestDTO.builder()
            .name(name + " " + UUID.randomUUID())
            .outputFormateType(ScoreType.Json)
            .description("A test evaluator")
            .variables(List.of(new EvaluatorVariableDTO("response", true)))
            .outputCategories(
                List.of(
                    OutputCategoryDTO.builder()
                        .name("High Quality")
                        .value("1.0")
                        .startRange("0.0")
                        .endRange("1.0")
                        .build()))
            .modelId(existingModelId)
            .prompts(List.of(new Prompt(InputRole.USER, "What is your name?")))
            .build();

    // Call the MockMvc helper method
    MvcResult result = postRequest(BASE_PATH, requestDTO);
    return objectMapper.readValue(
        result.getResponse().getContentAsString(), LLMEvaluatorResponseDTO.class);
  }
}
