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

package com.planck.planck.domain.workbook;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.chat.ChatRepository;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.model.ModelRepository;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.domain.project.dto.CreateProjectCommand;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.domain.workbook.dto.WorkBookClearResultsRequest;
import com.planck.planck.domain.workbook.dto.WorkBookDeleteRowsRequest;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.InputRole;
import com.planck.planck.util.PlanckConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class WorkbookControllerTestIT extends IntegrationTestBase {
  @Autowired private ProjectService projectService;
  @Autowired private UserRepository userRepository;
  @Autowired private ChatRepository chatRepository;
  @Autowired private ChatTurnRepository chatTurnRepository;
  @Autowired private ModelRepository modelRepository;
  @Autowired private EvaluationContainerService evaluationContainerService;
  @Autowired private ObjectMapper objectMapper;
  private User testUser;
  private Project testProject;
  private String modelId = "model_f9be2939-ae45-11f0-93fc-0242ac110002";

  @BeforeEach
  public void setUp() {
    super.setUp();
    testUser = userRepository.findByEmail(PlanckConstants.DEFAULT_USER).orElseThrow();
    testProject =
        projectService.createProject(
            new CreateProjectCommand(
                "export-project", "Export Project", "Description", false, EvaluationType.POINTWISE),
            testUser);
  }

  @Test
  void getWorkbook_shouldReturnPaginatedRows() throws Exception {
    List<Chat> chatsToSave = new ArrayList<>();
    List<ChatTurn> turnsToSave = new ArrayList<>();
    for (int i = 1; i <= 50; i++) {
      Chat chat = Chat.builder().user(testUser).container(testProject).build();
      ChatTurn turn = ChatTurn.builder().chat(chat).user(testUser).sequenceId(i).build();
      chat.setTurns(List.of(turn));
      chatsToSave.add(chat);
      turnsToSave.add(turn);
    }
    chatRepository.saveAll(chatsToSave);
    chatTurnRepository.saveAll(turnsToSave);
    mockMvc
        .perform(
            get("/workbook/projects/{projectId}", testProject.getId())
                .header("Authorization", getBearerJwtToken())
                .queryParam("page_size", "20")
                .queryParam("page_token", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workbook_rows.size()", equalTo(20)))
        .andExpect(jsonPath("$.next_page_token", equalTo("1")));
    mockMvc
        .perform(
            get("/workbook/projects/{projectId}", testProject.getId())
                .header("Authorization", getBearerJwtToken())
                .queryParam("page_size", "20")
                .queryParam("page_token", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workbook_rows.size()", equalTo(20)))
        .andExpect(jsonPath("$.next_page_token", equalTo("2")));
    mockMvc
        .perform(
            get("/workbook/projects/{projectId}", testProject.getId())
                .header("Authorization", getBearerJwtToken())
                .queryParam("page_size", "20")
                .queryParam("page_token", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workbook_rows.size()", equalTo(10)))
        .andExpect(jsonPath("$.next_page_token", nullValue()));
  }

  @Test
  void getWorkbook_shouldReturnNotFound_ifProjectDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/workbook/projects/{projectId}", "nonExistentId")
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isNotFound());
  }

  @Test
  void createProjectRow_shouldCreateNewChatAndTurn() throws Exception {
    Map<String, Object> promptBody = new HashMap<>();
    promptBody.put("text", "This is a new prompt");
    promptBody.put("role", InputRole.USER.name());
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("prompt", promptBody);
    requestBody.put("modelId", modelId);
    mockMvc
        .perform(
            post("/workbook/projects/{projectId}/row", testProject.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.chat_turn_id", notNullValue()))
        .andExpect(jsonPath("$.chat_id", notNullValue()));
    List<ChatTurn> turns = chatTurnRepository.findAllByUserAndChatContainer(testUser, testProject);
    assertEquals(1, turns.size());
    assertEquals("This is a new prompt", turns.get(0).getInputs().get(0).getText());
  }

  private ModelResponse createTestResponse(Model model) {
    ModelResponse resp = new ModelResponse();
    resp.setText("Test Output");
    resp.setUser(testUser);
    resp.setModel(model);
    resp.setContainer(testProject);
    return resp;
  }

  @Test
  void updateProjectRow_shouldReturnNotFound_ifChatTurnDoesNotExist() throws Exception {
    Map<String, Object> promptBody = new HashMap<>();
    promptBody.put("text", "Valid prompt");
    promptBody.put("role", InputRole.USER.name());
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("prompt", promptBody);
    requestBody.put("modelResponse", "Updated Response");
    mockMvc
        .perform(
            patch(
                    "/workbook/projects/{projectId}/rows/{chatTurnId}",
                    testProject.getId(),
                    "nonExistentId")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteChats_shouldDeleteSpecificChatRows() throws Exception {
    Chat chat1 = chatRepository.save(Chat.builder().user(testUser).container(testProject).build());
    Chat chat2 = chatRepository.save(Chat.builder().user(testUser).container(testProject).build());
    Chat chat3 = chatRepository.save(Chat.builder().user(testUser).container(testProject).build());
    chatTurnRepository.save(ChatTurn.builder().chat(chat1).user(testUser).sequenceId(1).build());
    chatTurnRepository.save(ChatTurn.builder().chat(chat2).user(testUser).sequenceId(1).build());
    chatTurnRepository.save(ChatTurn.builder().chat(chat3).user(testUser).sequenceId(1).build());
    assertEquals(
        3, chatTurnRepository.findAllChatIdsByUserAndContainer(testUser, testProject).size());
    WorkBookDeleteRowsRequest requestBody = new WorkBookDeleteRowsRequest();
    requestBody.setChatIds(List.of(chat1.getId(), chat2.getId()));
    mockMvc
        .perform(
            delete("/workbook/projects/{projectId}/delete-rows", testProject.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", equalTo("Chat rows deleted successfully")));
    assertEquals(
        1, chatTurnRepository.findAllChatIdsByUserAndContainer(testUser, testProject).size());
    assertTrue(chatRepository.findById(chat1.getId()).isEmpty());
    assertTrue(chatRepository.findById(chat2.getId()).isEmpty());
    assertTrue(chatRepository.findById(chat3.getId()).isPresent());
  }

  @Test
  void deleteAllChats_shouldDeleteAllChatRowsForProject() throws Exception {
    Chat chat1 = chatRepository.save(Chat.builder().user(testUser).container(testProject).build());
    Chat chat2 = chatRepository.save(Chat.builder().user(testUser).container(testProject).build());
    chatTurnRepository.save(ChatTurn.builder().chat(chat1).user(testUser).sequenceId(1).build());
    chatTurnRepository.save(ChatTurn.builder().chat(chat2).user(testUser).sequenceId(2).build());
    assertEquals(2, chatTurnRepository.findAllByUserAndChatContainer(testUser, testProject).size());
    mockMvc
        .perform(
            delete("/workbook/projects/{projectId}/delete-rows/all", testProject.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", equalTo("Chat rows deleted successfully")));
    assertEquals(0, chatTurnRepository.findAllByUserAndChatContainer(testUser, testProject).size());
  }

  @Test
  void clearResults_shouldClearOutputsForSpecificTurns() throws Exception {
    Chat chat = chatRepository.save(Chat.builder().user(testUser).container(testProject).build());
    Model model = modelRepository.findById(modelId).orElseThrow();
    ChatTurn turn1 =
        chatTurnRepository.save(
            ChatTurn.builder()
                .chat(chat)
                .user(testUser)
                .sequenceId(1)
                .modelResponse(createTestResponse(model))
                .build());
    ChatTurn turn2 =
        chatTurnRepository.save(
            ChatTurn.builder()
                .chat(chat)
                .user(testUser)
                .sequenceId(2)
                .modelResponse(createTestResponse(model))
                .build());
    ChatTurn turn3 =
        chatTurnRepository.save(
            ChatTurn.builder()
                .chat(chat)
                .user(testUser)
                .sequenceId(3)
                .modelResponse(createTestResponse(model))
                .build());
    WorkBookClearResultsRequest requestBody = new WorkBookClearResultsRequest();
    requestBody.setChatTurnIds(List.of(turn1.getId(), turn2.getId()));
    mockMvc
        .perform(
            delete("/workbook/projects/{projectId}/clear-results", testProject.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isOk());
    ChatTurn updatedTurn1 = chatTurnRepository.findById(turn1.getId()).orElseThrow();
    ChatTurn updatedTurn3 = chatTurnRepository.findById(turn3.getId()).orElseThrow();
    assertNull(updatedTurn1.getModelResponse());
    assertNotNull(updatedTurn3.getModelResponse());
  }
}
