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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.planck.planck.Status200Response;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.workbook.dto.GetWorkbookResponseDTO;
import com.planck.planck.domain.workbook.dto.WorkBookDeleteRowsRequest;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.NotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class WorkbookServiceTest {

  @Mock private ChatService chatService;

  @Mock private ProjectService projectService;
  @Mock private EvaluationContainerService evaluationContainerService;

  @InjectMocks private WorkbookServiceImpl workbookService;

  private User testUser;
  private Project testProject;
  private String projectId = "proj-123";
  private String chatId1 = "chat-abc";
  private String chatId2 = "chat-def";

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-xyz");

    testProject = new Project();
    testProject.setId(projectId);
    testProject.setUser(testUser); // Ensure project is associated with the user
  }

  // --- Tests for getData ---

  @Test
  void getData_Success_ReturnsWorkbookRows() {

    Chat chat = new Chat();
    chat.setId(chatId1);
    List<ModelInput> inputs = new ArrayList<>();
    ModelInput mi = new ModelInput();
    mi.setRole(InputRole.USER);
    mi.setText("text");
    inputs.add(mi);

    ChatTurn turn1 =
        new ChatTurn(); // Populate with necessary data if WorkbookRowDTO constructor uses it
    turn1.setId("turn-1");
    turn1.setChat(chat);
    turn1.setInputs(inputs);
    turn1.setSequenceId(1);
    // Add other relevant fields to turn1 if needed for WorkbookRowDTO

    ChatTurn turn2 = new ChatTurn();
    turn2.setId("turn-2");
    turn2.setChat(chat);
    turn2.setInputs(inputs);
    turn2.setSequenceId(2);
    // Add other relevant fields to turn2 if needed
    chat.setTurns(Arrays.asList(turn1, turn2));
    List<ChatTurn> chatTurns = Arrays.asList(turn1, turn2);
    Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatTurn> page = new PageImpl<>(chatTurns, pageable, chatTurns.size());

    Map<String, List<TagDTO>> tagDtoMap = new HashMap<>();
    TagDTO tagDto = new TagDTO();
    tagDto.setId("tag-1");
    tagDto.setName("tag1");
    tagDto.setColor("#FFFFFF");
    tagDtoMap.put("turn-1", List.of(tagDto));

    when(evaluationContainerService.getContainerForUser(testUser, projectId))
        .thenReturn(testProject);
    when(chatService.fetchLastTurns(testUser, testProject, null, pageable)).thenReturn(page);

    // Act
    GetWorkbookResponseDTO result =
        workbookService.getData(testUser, projectId, 20, 0, null, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getWorkbookRows().size());
    assertNull(result.getNextPageToken());
    assertEquals(2, page.getTotalElements());
    verify(chatService).fetchLastTurns(testUser, testProject, null, pageable);
  }

  @Test
  void getData_ProjectNotFound_ThrowsNotFoundException() {
    // Arrange
    when(evaluationContainerService.getContainerForUser(testUser, projectId)).thenReturn(null);

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              workbookService.getData(testUser, projectId, 20, 0, null, null, null);
            });

    assertEquals("Container not found for this user", exception.getMessage());
    verify(chatService, never())
        .fetchLastTurns(any(), any(), any(), any()); // Ensure chatService is not called
  }

  @Test
  void getData_NoChatTurns_ReturnsEmptyList() {
    // Arrange
    when(evaluationContainerService.getContainerForUser(testUser, projectId))
        .thenReturn(testProject);
    Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    when(chatService.fetchLastTurns(eq(testUser), eq(testProject), isNull(), eq(pageable)))
        .thenReturn(Page.empty(pageable));

    // Act
    GetWorkbookResponseDTO result =
        workbookService.getData(testUser, projectId, 20, 0, null, null, null);

    // Assert
    assertNotNull(result);
    assertTrue(result.getWorkbookRows().isEmpty());

    verify(chatService).fetchLastTurns(eq(testUser), eq(testProject), isNull(), eq(pageable));
  }

  // --- Tests for getDeteteChats ---

  @Test
  void getDeleteChats_Success_ReturnsStatus200() {
    // Arrange
    WorkBookDeleteRowsRequest request = new WorkBookDeleteRowsRequest();
    List<String> chatIdsToDelete = Arrays.asList(chatId1, chatId2);
    request.setChatIds(chatIdsToDelete);

    // Mock chatService.deleteChats to do nothing (successful deletion)
    doNothing().when(chatService).deleteChats(chatIdsToDelete, testUser, projectId);

    // Act
    Object result = workbookService.getDeleteChats(testUser, projectId, request.getChatIds());

    // Assert
    assertNotNull(result);
    assertTrue(result instanceof Status200Response);
    assertEquals("Chat rows deleted successfully", ((Status200Response) result).getMessage());

    verify(chatService).deleteChats(chatIdsToDelete, testUser, projectId);
  }

  @Test
  void getDeleteChats_ChatServiceThrowsException_RethrowsException() {
    // Arrange
    WorkBookDeleteRowsRequest request = new WorkBookDeleteRowsRequest();
    List<String> chatIdsToDelete = Arrays.asList(chatId1);
    request.setChatIds(chatIdsToDelete);

    // Mock chatService.deleteChats to throw an exception
    NotFoundException chatServiceException = new NotFoundException("Chat not found");
    doThrow(chatServiceException)
        .when(chatService)
        .deleteChats(chatIdsToDelete, testUser, projectId);

    // Act & Assert
    NotFoundException thrown =
        assertThrows(
            NotFoundException.class,
            () -> {
              workbookService.getDeleteChats(testUser, projectId, request.getChatIds());
            });

    assertEquals(
        "Chat not found", thrown.getMessage()); // Ensure the original exception is rethrown
    verify(chatService).deleteChats(chatIdsToDelete, testUser, projectId);
  }

  @Test
  void getDeleteChats_EmptyChatIdList_CallsDeleteWithEmptyList() {
    // Arrange
    WorkBookDeleteRowsRequest request = new WorkBookDeleteRowsRequest();
    List<String> chatIdsToDelete = Collections.emptyList();
    request.setChatIds(chatIdsToDelete);

    doNothing().when(chatService).deleteChats(chatIdsToDelete, testUser, projectId);

    // Act
    Object result = workbookService.getDeleteChats(testUser, projectId, request.getChatIds());

    // Assert
    assertNotNull(result);
    assertTrue(result instanceof Status200Response);
    assertEquals("Chat rows deleted successfully", ((Status200Response) result).getMessage());

    // Verify deleteChats was called with an empty list
    verify(chatService).deleteChats(eq(Collections.emptyList()), eq(testUser), eq(projectId));
  }

  @Test
  void getData_WithValidPageToken_ParsesPageCorrectly() {
    // Arrange
    Chat chat = new Chat();
    chat.setId(Chat.ID_PREFIX + UUID.randomUUID());
    List<ModelInput> inputs = new ArrayList<>();
    ModelInput input = new ModelInput();
    input.setText("example input");
    input.setRole(InputRole.USER);
    inputs.add(input);

    ChatTurn turn1 = new ChatTurn();
    turn1.setId("turn-1");
    turn1.setChat(chat);
    turn1.setInputs(inputs);
    turn1.setSequenceId(1);

    ChatTurn turn2 = new ChatTurn();
    turn2.setId("turn-2");
    turn2.setChat(chat);
    turn2.setInputs(inputs);
    turn2.setSequenceId(2);

    List<ChatTurn> chatTurns = Arrays.asList(turn1, turn2);

    chat.setTurns(chatTurns);
    Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatTurn> page = new PageImpl<>(chatTurns, pageable, chatTurns.size());

    when(evaluationContainerService.getContainerForUser(testUser, projectId))
        .thenReturn(testProject);
    when(chatService.fetchLastTurns(testUser, testProject, null, pageable)).thenReturn(page);

    // Act
    GetWorkbookResponseDTO result =
        workbookService.getData(testUser, projectId, 20, 0, null, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getWorkbookRows().size());
    assertNull(result.getNextPageToken());
    assertEquals(2, result.getTotalSize());
    verify(chatService).fetchLastTurns(testUser, testProject, null, pageable);
  }
}
