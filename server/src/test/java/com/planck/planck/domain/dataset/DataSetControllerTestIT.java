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

package com.planck.planck.domain.dataset;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.chat.ChatRepository;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.dataset.dto.ChatTurnGroupCreateRequest;
import com.planck.planck.domain.dataset.dto.DataSetDTO;
import com.planck.planck.domain.dataset.dto.DataSetListResponse;
import com.planck.planck.domain.dataset.dto.DataSetRowDeleteByChatIdRequest;
import com.planck.planck.domain.dataset.dto.DataSetRowDeleteRequest;
import com.planck.planck.domain.dataset.dto.DataSetRowUpdateRequest;
import com.planck.planck.domain.dataset.dto.RowCreateRequest;
import com.planck.planck.domain.dataset.service.DataSetService;
import com.planck.planck.domain.model.ModelRepository;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.DataSet;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.DataSetType;
import com.planck.planck.enums.InputRole;
import com.planck.planck.util.PlanckConstants;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class DataSetControllerTestIT extends IntegrationTestBase {

  @Autowired private DataSetRepository dataSetRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ChatRepository chatRepository;

  @Autowired private ChatTurnRepository chatTurnRepository;

  @Autowired private ModelRepository modelRepository;

  @Autowired private DataSetService dataSetService;

  @Autowired private ObjectMapper objectMapper;

  private User testUser;

  private String modelId = "model_f9be2939-ae45-11f0-93fc-0242ac110002";

  @BeforeEach
  public void setUp() {
    super.setUp();
    testUser = userRepository.findByEmail(PlanckConstants.DEFAULT_USER).orElseThrow();
  }

  @Test
  void getAllDatasets_shouldReturnDatasetsForUser() throws Exception {
    dataSetRepository.save(
        DataSet.builder().user(testUser).name("Dataset A").type(DataSetType.USER).build());
    dataSetRepository.save(
        DataSet.builder().user(testUser).name("Dataset B").type(DataSetType.USER).build());

    MvcResult result =
        mockMvc
            .perform(get("/datasets").header("Authorization", getBearerJwtToken()))
            .andExpect(status().isOk())
            .andReturn();

    DataSetListResponse dataSetListResponse =
        objectMapper.readValue(
            result.getResponse().getContentAsString(), DataSetListResponse.class);

    assertNotNull(dataSetListResponse);
    assertNotNull(dataSetListResponse.getUserDataSets());
    assertEquals(2, dataSetListResponse.getUserDataSets().size());

    assertEquals("Dataset A", dataSetListResponse.getUserDataSets().get(0).getName());
    assertEquals("Dataset B", dataSetListResponse.getUserDataSets().get(1).getName());
  }

  @Test
  void getDatasetById_shouldReturnDataset() throws Exception {
    DataSet dataSet =
        dataSetRepository.save(
            DataSet.builder().user(testUser).name("Test Dataset").type(DataSetType.USER).build());

    mockMvc
        .perform(
            get("/datasets/{id}", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", equalTo("Test Dataset")))
        .andExpect(jsonPath("$.id", equalTo(dataSet.getId())));
  }

  @Test
  void getDatasetById_shouldReturnNotFound_ifDatasetDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/datasets/{id}", "nonExistentId")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void createDataset_shouldReturnCreatedDataset() throws Exception {
    DataSetDTO datasetDTO =
        DataSetDTO.builder().name("New Dataset").description("A new dataset").build();

    mockMvc
        .perform(
            post("/datasets")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datasetDTO))) // Serialize body
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", equalTo("New Dataset")))
        .andExpect(jsonPath("$.description", equalTo("A new dataset")));
  }

  @Test
  void deleteDataset_shouldReturnNoContent() throws Exception {
    DataSet dataSet =
        dataSetRepository.save(
            DataSet.builder().user(testUser).name("To Delete").type(DataSetType.USER).build());

    mockMvc
        .perform(
            delete("/datasets/{id}", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  void importChats_shouldReturnImportResult() throws Exception {
    DataSet dataSet =
        dataSetService.createDataSet(testUser, "import-dataset", "Import Description", null);
    String csvContent =
        """
                                input,turns
                                Hi,"[{""role"":""user"",""content"":""Hi""}]"
                                """;
    MockMultipartFile mockFile =
        new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

    mockMvc
        .perform(
            multipart("/datasets/{id}/import", dataSet.getId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("chat_column_name", "turns"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successfulRows", equalTo(1)));
  }

  @Test
  void importChats_shouldReturnBadRequest_whenFileIsEmpty() throws Exception {
    DataSet dataSet =
        dataSetService.createDataSet(testUser, "empty-file-dataset", "Empty file test", null);
    MockMultipartFile emptyMockFile =
        new MockMultipartFile("file", "empty.csv", "text/csv", "".getBytes());

    mockMvc
        .perform(
            multipart("/datasets/{id}/import", dataSet.getId())
                .file(emptyMockFile)
                .header("Authorization", getBearerJwtToken())
                .param("chat_column_name", "chat"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.successfulRows", equalTo(0)));
  }

  @Test
  void importChats_shouldReturnInternalServerError_whenTooManyVariablesColumnsProvided()
      throws Exception {
    DataSet dataSet =
        dataSetService.createDataSet(testUser, "variables-dataset", "Variables test", null);
    String fileContent = "header1,header2\ndata1,data2";
    MockMultipartFile mockFile =
        new MockMultipartFile("file", "valid.csv", "text/csv", fileContent.getBytes());
    String tooManyVariablesColumns =
        "meta1,meta2,meta3,meta4,meta5,meta6,meta7,meta8,meta9,meta10,meta11";

    mockMvc
        .perform(
            multipart("/datasets/{id}/import", dataSet.getId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("chat_column_name", "header1")
                .param("variables_column_names", tooManyVariablesColumns))
        .andExpect(status().isInternalServerError())
        .andExpect(
            MockMvcResultMatchers.content()
                .string(containsString("The number of variables columns cannot exceed 10.")));
  }

  @Test
  void updateDataset_shouldReturnUpdatedDataset() throws Exception {
    DataSet dataSet =
        dataSetRepository.save(
            DataSet.builder().user(testUser).name("Original Name").type(DataSetType.USER).build());

    Map<String, String> updateRequest = new HashMap<>();
    updateRequest.put("name", "Updated Name");
    updateRequest.put("description", "Updated description");

    mockMvc
        .perform(
            patch("/datasets/{id}", dataSet.getId()) // Use patch builder
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", equalTo("Updated Name")))
        .andExpect(jsonPath("$.description", equalTo("Updated description")))
        .andExpect(jsonPath("$.id", equalTo(dataSet.getId())));

    DataSet updatedDataSet = dataSetRepository.findById(dataSet.getId()).orElseThrow();
    assertEquals("Updated Name", updatedDataSet.getName());
    assertEquals("Updated description", updatedDataSet.getDescription());
  }

  @Test
  void updateDataset_shouldReturnNotFound_ifDatasetDoesNotExist() throws Exception {
    Map<String, String> updateRequest = new HashMap<>();
    updateRequest.put("name", "New Name");

    mockMvc
        .perform(
            patch("/datasets/{id}", "nonExistentId")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void createDatasetRow_shouldCreateNewChatAndTurn_whenNoChatIdProvided() throws Exception {
    DataSet dataSet =
        dataSetRepository.save(
            DataSet.builder().user(testUser).name("Test Dataset").type(DataSetType.USER).build());

    Map<String, String> request = new HashMap<>();

    mockMvc
        .perform(
            post("/datasets/{id}/row", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.chat_turn_id", notNullValue()))
        .andExpect(jsonPath("$.chat_id", notNullValue()));
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void updateDatasetRow_shouldReturnUpdatedRow_whenValidRequest() throws Exception {
    DataSet dataSet =
        dataSetRepository.save(
            DataSet.builder()
                .user(testUser)
                .name("Update Test Dataset")
                .type(DataSetType.USER)
                .build());

    Chat chat = new Chat();
    chat.setUser(testUser);
    chat.setContainer(dataSet);
    chat = chatRepository.save(chat);

    Model model = modelRepository.findById(modelId).orElseThrow();

    ModelInput initialInput = new ModelInput();
    initialInput.setText("Original Prompt");
    initialInput.setRole(InputRole.USER);

    ModelResponse initialResponse = new ModelResponse();
    initialResponse.setText("Original Response");
    initialResponse.setUser(testUser);
    initialResponse.setModel(model);
    initialResponse.setContainer(dataSet);

    ChatTurn chatTurn = new ChatTurn();
    chatTurn.setChat(chat);
    chatTurn.setUser(testUser);
    chatTurn.setSequenceId(1);
    chatTurn.setInputs(List.of(initialInput));
    chatTurn.setModelResponse(initialResponse);
    chatTurn = chatTurnRepository.save(chatTurn);

    DataSetRowUpdateRequest requestBody = new DataSetRowUpdateRequest();
    requestBody.setModelPrompt("Updated Prompt");
    requestBody.setModelResponse("Updated Response");
    requestBody.setModelId(model.getId());

    mockMvc
        .perform(
            patch("/datasets/{id}/rows/{rowId}", dataSet.getId(), chatTurn.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.chat_turn_id", equalTo(chatTurn.getId())))
        .andExpect(jsonPath("$.chat_id", equalTo(chat.getId())))
        .andExpect(jsonPath("$.model_output.text", equalTo("Updated Response")));

    ChatTurn updatedChatTurn = chatTurnRepository.findById(chatTurn.getId()).orElseThrow();
    ModelInput updatedInput = updatedChatTurn.getInputs().get(0);

    assertEquals("Updated Prompt", updatedInput.getText());
    assertEquals("Updated Response", updatedChatTurn.getModelResponse().getText());
  }

  @Test
  void deleteDatasetRow_shouldReturnNoContent_whenSuccessful() throws Exception {
    DataSet dataSet =
        dataSetRepository.save(
            DataSet.builder()
                .user(testUser)
                .name("Delete Test Dataset")
                .type(DataSetType.USER)
                .build());

    Chat chat = new Chat();
    chat.setUser(testUser);
    chat.setContainer(dataSet);
    chat = chatRepository.save(chat);

    ChatTurn chatTurn = new ChatTurn();
    chatTurn.setChat(chat);
    chatTurn.setUser(testUser);
    chatTurn.setSequenceId(1);
    chatTurn = chatTurnRepository.save(chatTurn);

    mockMvc
        .perform(
            delete("/datasets/{id}/rows/{rowId}", dataSet.getId(), chatTurn.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isNoContent());

    assertTrue(chatTurnRepository.findById(chatTurn.getId()).isEmpty());
  }

  @Test
  void getDatasetRows_shouldReturnPaginatedRows_whenSuccessful() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Pagination Test");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    for (int i = 1; i <= 50; i++) {
      Chat chat = new Chat();
      chat.setUser(testUser);
      chat.setContainer(dataSet);
      chat = chatRepository.save(chat);
      ChatTurn chatTurn = new ChatTurn();
      chatTurn.setChat(chat);
      chatTurn.setUser(testUser);
      chatTurn.setSequenceId(i);
      chatTurnRepository.save(chatTurn);
    }
    // First page
    mockMvc
        .perform(
            get("/datasets/{id}/rows", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .queryParam("page_size", "20")
                .queryParam("page_token", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workbook_rows.size()", equalTo(20)))
        .andExpect(jsonPath("$.next_page_token", equalTo("1")));

    // Second page
    mockMvc
        .perform(
            get("/datasets/{id}/rows", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .queryParam("page_size", "20")
                .queryParam("page_token", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workbook_rows.size()", equalTo(20)))
        .andExpect(jsonPath("$.next_page_token", equalTo("2")));

    // Last page
    mockMvc
        .perform(
            get("/datasets/{id}/rows", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .queryParam("page_size", "20")
                .queryParam("page_token", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workbook_rows.size()", equalTo(10)))
        .andExpect(jsonPath("$.next_page_token", nullValue()));
  }

  @Test
  void getDatasetRows_shouldReturnBadRequest_whenPageTokenIsInvalid() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Invalid Page Token");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    mockMvc
        .perform(
            get("/datasets/{id}/rows", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .queryParam("page_token", "invalid"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getDatasetRows_shouldReturnNotFound_ifDatasetDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/datasets/{id}/rows", "nonExistentId")
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isNotFound());
  }

  @Test
  void getDatasetRowsByChatId_shouldReturnRows_whenSuccessful() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Chat ID Test");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    Chat chat = new Chat();
    chat.setUser(testUser);
    chat.setContainer(dataSet);
    chat = chatRepository.save(chat);

    for (int i = 1; i <= 3; i++) {
      ChatTurn chatTurn = new ChatTurn();
      chatTurn.setChat(chat);
      chatTurn.setUser(testUser);
      chatTurn.setSequenceId(i);
      chatTurnRepository.save(chatTurn);
    }

    mockMvc
        .perform(
            get("/datasets/{id}/{chatId}/rows", dataSet.getId(), chat.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", equalTo(3)))
        .andExpect(jsonPath("$[0].chat_id", equalTo(chat.getId())))
        .andExpect(jsonPath("$[1].chat_id", equalTo(chat.getId())))
        .andExpect(jsonPath("$[2].chat_id", equalTo(chat.getId())));
  }

  @Test
  void getDatasetRowsByChatId_shouldReturnEmptyResponse_ifChatIdDoesNotExist() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Chat ID Test");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    mockMvc
        .perform(
            get("/datasets/{id}/{chatId}/rows", dataSet.getId(), "incorrectChatId")
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", equalTo(0)));
  }

  @Test
  void deleteDatasetRows_shouldReturnNoContent_whenSuccessful() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Dataset for Deletion");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    for (int i = 0; i < 3; i++) {
      Chat chat = new Chat();
      chat.setUser(testUser);
      chat.setContainer(dataSet);
      chatRepository.save(chat);
      ChatTurn chatTurn = new ChatTurn();
      chatTurn.setChat(chat);
      chatTurn.setUser(testUser);
      chatTurn.setSequenceId(i);
      chatTurnRepository.save(chatTurn);
    }
    assertEquals(3, chatTurnRepository.findAllByUserAndChatContainer(testUser, dataSet).size());

    mockMvc
        .perform(
            delete("/datasets/{id}/rows", dataSet.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isNoContent());

    assertEquals(0, chatTurnRepository.findAllByUserAndChatContainer(testUser, dataSet).size());
  }

  @Test
  void deleteDatasetRows_shouldReturnNotFound_ifDatasetDoesNotExist() throws Exception {
    mockMvc
        .perform(
            delete("/datasets/{id}/rows", "nonExistentId")
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isNotFound());
  }

  @Test
  void createDatasetRows_shouldReturnOkAndCreateRows_whenSuccessful() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Bulk Create Test");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    Chat chat1 = new Chat();
    chat1.setUser(testUser);
    chat1.setContainer(dataSet);
    chat1 = chatRepository.save(chat1);

    Chat chat2 = new Chat();
    chat2.setUser(testUser);
    chat2.setContainer(dataSet);
    chat2 = chatRepository.save(chat2);

    ChatTurnGroupCreateRequest request1 = new ChatTurnGroupCreateRequest();
    request1.setChatId(chat1.getId());

    RowCreateRequest row1 = new RowCreateRequest();
    request1.setRows(List.of(row1));

    ChatTurnGroupCreateRequest request2 = new ChatTurnGroupCreateRequest();
    request2.setChatId(chat2.getId());

    RowCreateRequest row2 = new RowCreateRequest();
    request2.setRows(List.of(row2));

    List<ChatTurnGroupCreateRequest> requestBody = List.of(request1, request2);

    mockMvc
        .perform(
            post("/datasets/{id}/rows", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", equalTo(2)))
        .andExpect(jsonPath("$[0].chat_id", equalTo(chat1.getId())))
        .andExpect(jsonPath("$[1].chat_id", equalTo(chat2.getId())));

    assertEquals(2, chatTurnRepository.findAllByUserAndChatContainer(testUser, dataSet).size());
  }

  @Test
  void deleteDatasetRowsBulk_shouldReturnNoContent_whenSuccessful() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Bulk Delete Test");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    List<String> chatTurnIdsToDelete = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      Chat chat = new Chat();
      chat.setUser(testUser);
      chat.setContainer(dataSet);
      chatRepository.save(chat);

      ChatTurn chatTurn = new ChatTurn();
      chatTurn.setChat(chat);
      chatTurn.setUser(testUser);
      chatTurn.setSequenceId(i);
      chatTurn = chatTurnRepository.save(chatTurn);
      if (i > 1) {
        chatTurnIdsToDelete.add(chatTurn.getId());
      }
    }

    assertEquals(5, chatTurnRepository.findAllByUserAndChatContainer(testUser, dataSet).size());

    DataSetRowDeleteRequest requestBody = new DataSetRowDeleteRequest();
    requestBody.setChatTurnIds(chatTurnIdsToDelete);

    mockMvc
        .perform(
            delete("/datasets/{id}/rows/bulk", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isNoContent());

    assertEquals(2, chatTurnRepository.findAllByUserAndChatContainer(testUser, dataSet).size());
  }

  @Test
  void deleteDatasetRowsBulk_shouldReturnNotFound_ifDatasetDoesNotExist() throws Exception {
    DataSetRowDeleteRequest requestBody = new DataSetRowDeleteRequest();

    mockMvc
        .perform(
            delete("/datasets/{id}/rows/bulk", "nonExistentId")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteDatasetRowsBulkByChatId_shouldReturnNoContent_whenSuccessful() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Bulk Delete by Chat ID Test");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    // Setup Chat 1 (to be deleted)
    Chat chat1 = new Chat();
    chat1.setUser(testUser);
    chat1.setContainer(dataSet);
    chat1 = chatRepository.save(chat1);

    ChatTurn turn1_1 = new ChatTurn();
    turn1_1.setChat(chat1);
    turn1_1.setUser(testUser);
    turn1_1.setSequenceId(1);
    chatTurnRepository.save(turn1_1);

    ChatTurn turn1_2 = new ChatTurn();
    turn1_2.setChat(chat1);
    turn1_2.setUser(testUser);
    turn1_2.setSequenceId(2);
    chatTurnRepository.save(turn1_2);

    // Setup Chat 2 (to be deleted)
    Chat chat2 = new Chat();
    chat2.setUser(testUser);
    chat2.setContainer(dataSet);
    chat2 = chatRepository.save(chat2);

    ChatTurn turn2_1 = new ChatTurn();
    turn2_1.setChat(chat2);
    turn2_1.setUser(testUser);
    turn2_1.setSequenceId(1);
    chatTurnRepository.save(turn2_1);

    // Setup Chat 3 (to remain)
    Chat chat3 = new Chat();
    chat3.setUser(testUser);
    chat3.setContainer(dataSet);
    chat3 = chatRepository.save(chat3);

    ChatTurn turn3_1 = new ChatTurn();
    turn3_1.setChat(chat3);
    turn3_1.setUser(testUser);
    turn3_1.setSequenceId(1);
    chatTurnRepository.save(turn3_1);

    // Initial check (3 unique chat IDs)
    assertEquals(3, chatTurnRepository.findAllChatIdsByUserAndContainer(testUser, dataSet).size());

    DataSetRowDeleteByChatIdRequest requestBody = new DataSetRowDeleteByChatIdRequest();
    requestBody.setChatIds(List.of(chat1.getId(), chat2.getId()));

    mockMvc
        .perform(
            delete("/datasets/{id}/rows/bulk-by-chat-id", dataSet.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isNoContent());

    // Final check (1 unique chat ID remaining: chat3)
    assertEquals(1, chatTurnRepository.findAllChatIdsByUserAndContainer(testUser, dataSet).size());
    // Check remaining chat turn (only chat 3's turn)
    assertTrue(chatTurnRepository.findById(turn3_1.getId()).isPresent());
    // Check deleted chat turns
    assertTrue(chatTurnRepository.findById(turn1_1.getId()).isEmpty());
    assertTrue(chatTurnRepository.findById(turn1_2.getId()).isEmpty());
    assertTrue(chatTurnRepository.findById(turn2_1.getId()).isEmpty());
  }

  @Test
  void deleteDatasetRowsBulkByChatId_shouldReturnNotFound_ifDatasetDoesNotExist() throws Exception {
    mockMvc
        .perform(
            delete("/datasets/{id}/rows/bulk-by-chat-id", "nonExistentId")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DataSetRowDeleteByChatIdRequest())))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void exportChats_shouldReturnExportedChats_whenSuccessful() throws Exception {
    DataSet dataSet = new DataSet();
    dataSet.setUser(testUser);
    dataSet.setName("Export Test Dataset");
    dataSet.setType(DataSetType.USER);
    dataSetRepository.save(dataSet);

    Chat chat1 = new Chat();
    chat1.setUser(testUser);
    chat1.setContainer(dataSet);
    chat1 = chatRepository.save(chat1);

    ChatTurn turn1 = new ChatTurn();
    turn1.setChat(chat1);
    turn1.setUser(testUser);
    turn1.setSequenceId(1);
    chatTurnRepository.save(turn1);

    Chat chat2 = new Chat();
    chat2.setUser(testUser);
    chat2.setContainer(dataSet);
    chat2 = chatRepository.save(chat2);

    ChatTurn turn2 = new ChatTurn();
    turn2.setChat(chat2);
    turn2.setUser(testUser);
    turn2.setSequenceId(1);
    chatTurnRepository.save(turn2);

    Chat chat3 = new Chat();
    chat3.setUser(testUser);
    chat3.setContainer(dataSet);
    chat3 = chatRepository.save(chat3);

    ChatTurn turn3 = new ChatTurn();
    turn3.setChat(chat3);
    turn3.setUser(testUser);
    turn3.setSequenceId(1);
    chatTurnRepository.save(turn3);

    mockMvc
        .perform(
            get("/datasets/{id}/export", dataSet.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", equalTo(3)))
        // .andExpect(jsonPath("$[0].turns", equalTo(1)))
        .andExpect(jsonPath("$[1].turns", equalTo(1)))
        .andExpect(jsonPath("$[2].turns", equalTo(1)));
  }

  @Test
  void exportChats_shouldReturnNotFound_ifDatasetDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/datasets/{id}/export", "nonExistentId")
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isNotFound());
  }

  @Test
  void importChats_shouldReturnImportResult_whenInputColumnIsProvided() throws Exception {
    DataSet dataSet =
        dataSetService.createDataSet(testUser, "input-column-import", "Input column test", null);

    String csvContent =
        """
                                        input_col,output_col
                                        Hello,"Hi there!"
                                        """;

    MockMultipartFile mockFile =
        new MockMultipartFile("file", "input.csv", "text/csv", csvContent.getBytes());

    mockMvc
        .perform(
            multipart("/datasets/{id}/import", dataSet.getId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("input_column_name", "input_col")
                .param("output_column_name", "output_col"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successfulRows", equalTo(1)));
  }

  @Test
  void importChats_shouldReturnImportResult_whenMultipleRowsAreProvided() throws Exception {
    DataSet dataSet =
        dataSetService.createDataSet(testUser, "multi-row-import", "Multi-row test", null);
    String csvContent =
        """
                                        input_col,output_col
                                        Hello,"Hi there!"
                                        How are you?,"I am good, thanks."
                                        """;

    MockMultipartFile mockFile =
        new MockMultipartFile("file", "multi.csv", "text/csv", csvContent.getBytes());

    mockMvc
        .perform(
            multipart("/datasets/{id}/import", dataSet.getId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("input_column_name", "input_col")
                .param("output_column_name", "output_col"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successfulRows", equalTo(2)));
  }

  @Test
  void importChats_shouldReturnImportResult_whenVariablesColumnNamesAreBlank() throws Exception {
    DataSet dataSet =
        dataSetService.createDataSet(
            testUser, "blank-variables-test", "Blank variables test", null);

    String csvContent =
        """
                                        input_col,output_col
                                        Hello,"Hi there!"
                                        """;

    MockMultipartFile mockFile =
        new MockMultipartFile("file", "blank_var.csv", "text/csv", csvContent.getBytes());

    mockMvc
        .perform(
            multipart("/datasets/{id}/import", dataSet.getId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("input_column_name", "input_col")
                // Pass an explicit blank string for the variable column names
                .param("variables_column_names", " "))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successfulRows", equalTo(1)));
  }
}
