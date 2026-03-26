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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.evaluator.dto.PointwiseHeuristicEvaluatorUpdateDTO;
import com.planck.planck.domain.evaluator.heuristic.PointwiseHeuristicEvaluatorRepository;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorResponseDTO;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.PointwiseHeuristicEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.CriteriaType;
import com.planck.planck.util.PlanckConstants;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class PointwiseHeuristicEvaluatorControllerTestIT extends IntegrationTestBase {
  private String authorizationHeader;

  @Autowired private UserRepository userRepository;
  @Autowired private PointwiseHeuristicEvaluatorRepository heuristicRepository;
  @Autowired private ObjectMapper objectMapper;

  private static final String BASE_PATH = "/pointwise-heuristic";
  private User defaultUser;

  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();

    authorizationHeader = super.getBearerJwtToken();
    defaultUser = userRepository.findByEmail(PlanckConstants.DEFAULT_USER).orElseThrow();
  }

  @Test
  void testCreateEvaluator_Success() throws Exception {
    PointwiseHeuristicEvaluatorRequestDTO requestDTO = createDefaultEvaluatorRequest();

    MvcResult result =
        mockMvc
            .perform(
                post(BASE_PATH)
                    .header("Authorization", authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andReturn();

    PointwiseHeuristicEvaluatorResponseDTO resultDTO =
        objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PointwiseHeuristicEvaluatorResponseDTO.class);

    assertNotNull(resultDTO.getId());
    assertEquals(requestDTO.getName(), resultDTO.getName());
    assertEquals(requestDTO.getCriteria(), resultDTO.getCriteria());
    assertEquals(requestDTO.getCriteriaType(), resultDTO.getCriteriaType());
  }

  @Test
  void testCreateEvaluator_NameExists_ReturnsConflict() throws Exception {
    createTestEvaluator("Existing Evaluator", defaultUser);

    PointwiseHeuristicEvaluatorRequestDTO requestDTO =
        PointwiseHeuristicEvaluatorRequestDTO.builder()
            .name("Existing Evaluator")
            .criteria("Another test criteria")
            .criteriaType(CriteriaType.CONTAINS)
            .build();

    mockMvc
        .perform(
            post(BASE_PATH)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isConflict());
  }

  @Test
  void testCreateEvaluator_InvalidInput_ReturnsBadRequest() throws Exception {
    PointwiseHeuristicEvaluatorRequestDTO requestDTO =
        PointwiseHeuristicEvaluatorRequestDTO.builder()
            .name(null) // Invalid field (assuming it's required)
            .criteria("Test criteria")
            .criteriaType(CriteriaType.CONTAINS)
            .build();

    mockMvc
        .perform(
            post(BASE_PATH)
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGetAllEvaluators_Success() throws Exception {
    createTestEvaluator("Evaluator 1", defaultUser);
    createTestEvaluator("Evaluator 2", defaultUser);

    MvcResult result =
        mockMvc
            .perform(get(BASE_PATH).header("Authorization", authorizationHeader))
            .andExpect(status().isOk())
            .andReturn();

    // Replace response.getBody().as(TypeRef)
    List<PointwiseHeuristicEvaluatorResponseDTO> resultList =
        objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<List<PointwiseHeuristicEvaluatorResponseDTO>>() {});

    assertNotNull(resultList);
    assertEquals(2, resultList.size());
  }

  @Test
  void testGetEvaluatorById_Success() throws Exception {
    PointwiseHeuristicEvaluator evaluator = createTestEvaluator("Get by ID Test", defaultUser);

    MvcResult result =
        mockMvc
            .perform(
                get(BASE_PATH + "/" + evaluator.getId())
                    .header("Authorization", authorizationHeader))
            .andExpect(status().isOk())
            .andReturn();

    PointwiseHeuristicEvaluatorResponseDTO resultDTO =
        objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PointwiseHeuristicEvaluatorResponseDTO.class);

    assertNotNull(resultDTO);
    assertEquals(evaluator.getId(), resultDTO.getId());
    assertEquals(evaluator.getName(), resultDTO.getName());
  }

  @Test
  void testUpdateEvaluator_Success() throws Exception {
    PointwiseHeuristicEvaluator evaluator = createTestEvaluator("Update Test", defaultUser);
    PointwiseHeuristicEvaluatorUpdateDTO updateDTO =
        PointwiseHeuristicEvaluatorUpdateDTO.builder().name("Updated Name").build();

    MvcResult result =
        mockMvc
            .perform(
                put(BASE_PATH + "/" + evaluator.getId())
                    .header("Authorization", authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk())
            .andReturn();

    PointwiseHeuristicEvaluatorResponseDTO resultDTO =
        objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PointwiseHeuristicEvaluatorResponseDTO.class);

    assertNotNull(resultDTO);
    assertEquals(updateDTO.getName(), resultDTO.getName());
  }

  @Test
  void testDeleteEvaluator_NotFound() throws Exception {
    String nonExistentId = "heuristic-eval-123";

    mockMvc
        .perform(
            delete(BASE_PATH + "/" + nonExistentId).header("Authorization", authorizationHeader))
        .andExpect(status().isNotFound());
  }

  @Test
  void testDeleteEvaluator_Success() throws Exception {
    PointwiseHeuristicEvaluator deleteTest = createTestEvaluator("Delete Test", defaultUser);

    mockMvc
        .perform(
            delete(BASE_PATH + "/" + deleteTest.getId())
                .header("Authorization", authorizationHeader))
        .andExpect(status().isOk());

    assertTrue(heuristicRepository.findById(deleteTest.getId()).isEmpty());
  }

  private PointwiseHeuristicEvaluatorRequestDTO createDefaultEvaluatorRequest() {
    return PointwiseHeuristicEvaluatorRequestDTO.builder()
        .name("Test evaluator")
        .criteria("This is a test criteria.")
        .criteriaType(CriteriaType.CONTAINS)
        .build();
  }

  private PointwiseHeuristicEvaluator createTestEvaluator(String name, User user) {
    PointwiseHeuristicEvaluator evaluator = new PointwiseHeuristicEvaluator();
    evaluator.setName(name);
    evaluator.setCriteria("Test criteria for " + name);
    evaluator.setCriteriaType(CriteriaType.CONTAINS);
    evaluator.setUser(user);
    evaluator.prePersist();
    return heuristicRepository.save(evaluator);
  }
}
