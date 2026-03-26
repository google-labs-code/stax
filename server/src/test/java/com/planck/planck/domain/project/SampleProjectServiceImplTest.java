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

package com.planck.planck.domain.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.evaluator.heuristic.service.PointwiseHeuristicEvaluatorService;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.importexport.ChatImportService;
import com.planck.planck.domain.importexport.dto.ChatImportRequest;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.project.dto.ProjectDTO;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ChatTurnContainerType;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SampleProjectServiceImplTest {

  @Mock private ProjectRepository projectRepository;

  @Mock private ChatImportService chatImportService;

  @Mock private UserRepository userRepository;

  @Mock private PointwiseHeuristicEvaluatorService heuristicEvaluatorService;

  @Mock private HumanEvaluatorService humanEvaluatorService;

  @InjectMocks private SampleProjectServiceImpl sampleProjectService;

  private User defaultUser;
  private final String defaultUserEmail = "default@example.com";
  private final String sampleProjectName = "My Sample Project";
  private final String sampleProjectDesc = "Sample Description";

  @BeforeEach
  void setUp() {
    defaultUser = new User();
    defaultUser.setId("user-123");
    defaultUser.setEmail(defaultUserEmail);

    ReflectionTestUtils.setField(sampleProjectService, "authEnabled", false);
    ReflectionTestUtils.setField(sampleProjectService, "defaultUser", defaultUserEmail);
    ReflectionTestUtils.setField(sampleProjectService, "sampleProjectName", sampleProjectName);
    ReflectionTestUtils.setField(
        sampleProjectService, "sampleProjectDescription", sampleProjectDesc);

    ReflectionTestUtils.setField(sampleProjectService, "sampleCsvFilePath", "sample-data.csv");
    ReflectionTestUtils.setField(sampleProjectService, "inputColumnName", "input");
    ReflectionTestUtils.setField(sampleProjectService, "outputColumnName", "output");
    ReflectionTestUtils.setField(
        sampleProjectService, "expectedOutputColumnName", "expected_output");
    ReflectionTestUtils.setField(sampleProjectService, "tagsColumnName", "tags");
    ReflectionTestUtils.setField(
        sampleProjectService, "systemInstructionColumnName", "system_instruction");
    ReflectionTestUtils.setField(sampleProjectService, "modelLabelColumnName", "model_nickname");
    ReflectionTestUtils.setField(
        sampleProjectService, "humanEvalScoreColumnName", "human_evaluation");
    ReflectionTestUtils.setField(
        sampleProjectService, "humanEvalNotesColumnName", "human_evaluation_notes");
    ReflectionTestUtils.setField(
        sampleProjectService, "inferenceAnalyticsColumnName", "inference_analytics");
    ReflectionTestUtils.setField(
        sampleProjectService, "llmEvaluationColumnName", "llm_evaluations");
    ReflectionTestUtils.setField(sampleProjectService, "metadataColumns", "meta1,meta2");
  }

  @Test
  @DisplayName("init: Should do nothing when auth is enabled")
  void testInit_shouldDoNothing_whenAuthEnabled() {
    ReflectionTestUtils.setField(sampleProjectService, "authEnabled", true);
    sampleProjectService.initiateSampleProjectForDefaultAuth();
    verify(userRepository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("init: Should do nothing when default user is not found")
  void testInit_shouldDoNothing_whenNoDefaultUserFound() {
    when(userRepository.findByEmail(defaultUserEmail)).thenReturn(Optional.empty());
    sampleProjectService.initiateSampleProjectForDefaultAuth();
    verify(userRepository, times(1)).findByEmail(defaultUserEmail);
    verify(projectRepository, never()).countByUser(any(User.class));
  }

  @Test
  @DisplayName("init: Should do nothing when user already has projects")
  void testInit_shouldDoNothing_whenUserAlreadyHasProjects() {
    when(userRepository.findByEmail(defaultUserEmail)).thenReturn(Optional.of(defaultUser));
    when(projectRepository.countByUser(defaultUser)).thenReturn(1L);
    sampleProjectService.initiateSampleProjectForDefaultAuth();
    verify(projectRepository, times(1)).countByUser(defaultUser);
    verify(projectRepository, never()).saveAndFlush(any(Project.class));
  }

  @Test
  @DisplayName("init: Should create project and evaluators if user has none")
  void testInit_shouldCreateProjectAndEvaluators_whenUserHasNoProjectsOrEvaluators()
      throws IOException {
    when(userRepository.findByEmail(defaultUserEmail)).thenReturn(Optional.of(defaultUser));
    when(projectRepository.countByUser(defaultUser)).thenReturn(0L);
    when(humanEvaluatorService.getEvaluators(defaultUser)).thenReturn(Collections.emptyList());

    Project savedProject = Project.builder().id("proj-123").user(defaultUser).build();
    when(projectRepository.saveAndFlush(any(Project.class))).thenReturn(savedProject);

    ImportResultDTO mockedImportResult = mock(ImportResultDTO.class);
    when(mockedImportResult.getSuccessfulRows()).thenReturn(10);
    when(mockedImportResult.getFailedRows()).thenReturn(0);
    when(chatImportService.importChatsFromFile(any(ChatImportRequest.class)))
        .thenReturn(mockedImportResult);

    sampleProjectService.initiateSampleProjectForDefaultAuth();

    verify(humanEvaluatorService, times(1)).createUserThumbsEvaluator(defaultUser);
    verify(heuristicEvaluatorService, times(1)).createDefaultHeuristicEvaluators(defaultUser);
    verify(projectRepository, times(1)).saveAndFlush(any(Project.class));
    verify(chatImportService, times(1)).importChatsFromFile(any(ChatImportRequest.class));
  }

  @Test
  @DisplayName("init: Should create only project if evaluators already exist")
  void testInit_shouldCreateOnlyProject_whenUserHasNoProjectsButHasEvaluators() throws IOException {
    when(userRepository.findByEmail(defaultUserEmail)).thenReturn(Optional.of(defaultUser));
    when(projectRepository.countByUser(defaultUser)).thenReturn(0L);

    List mockedList = mock(List.class);
    when(mockedList.size()).thenReturn(1);
    when(humanEvaluatorService.getEvaluators(defaultUser)).thenReturn(mockedList);

    Project savedProject = Project.builder().id("proj-123").user(defaultUser).build();
    when(projectRepository.saveAndFlush(any(Project.class))).thenReturn(savedProject);

    ImportResultDTO mockedImportResult = mock(ImportResultDTO.class);
    when(mockedImportResult.getSuccessfulRows()).thenReturn(10);
    when(mockedImportResult.getFailedRows()).thenReturn(0);
    when(chatImportService.importChatsFromFile(any(ChatImportRequest.class)))
        .thenReturn(mockedImportResult);

    sampleProjectService.initiateSampleProjectForDefaultAuth();

    verify(humanEvaluatorService, never()).createUserThumbsEvaluator(any(User.class));
    verify(heuristicEvaluatorService, never()).createDefaultHeuristicEvaluators(any(User.class));
    verify(projectRepository, times(1)).saveAndFlush(any(Project.class));
    verify(chatImportService, times(1)).importChatsFromFile(any(ChatImportRequest.class));
  }

  @Test
  @DisplayName("create: Should successfully create project and import data")
  void testCreateSampleProject_shouldCreateProjectAndImportData_whenSuccessful()
      throws IOException {
    String newProjectId = "new-proj-id";
    Project savedProject =
        Project.builder()
            .id(newProjectId)
            .user(defaultUser)
            .name(sampleProjectName)
            .description(sampleProjectDesc)
            .isDefault(true)
            .build();

    when(projectRepository.saveAndFlush(any(Project.class))).thenReturn(savedProject);

    ImportResultDTO mockedImportResult = mock(ImportResultDTO.class);
    when(mockedImportResult.getSuccessfulRows()).thenReturn(50);
    when(mockedImportResult.getFailedRows()).thenReturn(0);
    when(chatImportService.importChatsFromFile(any(ChatImportRequest.class)))
        .thenReturn(mockedImportResult);

    ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
    ArgumentCaptor<ChatImportRequest> importRequestCaptor =
        ArgumentCaptor.forClass(ChatImportRequest.class);

    ProjectDTO result = sampleProjectService.createSampleProjectForUser(defaultUser);

    assertNotNull(result);
    assertEquals(newProjectId, result.getProjectId());
    assertEquals(sampleProjectName, result.getName());

    verify(projectRepository, times(1)).saveAndFlush(projectCaptor.capture());
    Project capturedProject = projectCaptor.getValue();
    assertEquals(defaultUser, capturedProject.getUser());
    assertEquals(sampleProjectName, capturedProject.getName());
    assertTrue(capturedProject.getIsDefault());

    verify(chatImportService, times(1)).importChatsFromFile(importRequestCaptor.capture());
    ChatImportRequest capturedRequest = importRequestCaptor.getValue();
    assertEquals(defaultUser, capturedRequest.user());
    assertEquals(newProjectId, capturedRequest.sourceId());
    assertEquals(ChatTurnContainerType.PROJECT, capturedRequest.sourceType());
  }

  @Test
  @DisplayName("create: Should create project but handle import exception")
  void testCreateSampleProject_shouldCreateProjectAndHandleImportException() throws IOException {
    String newProjectId = "proj-456";
    Project savedProject =
        Project.builder().id(newProjectId).user(defaultUser).name(sampleProjectName).build();

    when(projectRepository.saveAndFlush(any(Project.class))).thenReturn(savedProject);

    when(chatImportService.importChatsFromFile(any(ChatImportRequest.class)))
        .thenThrow(new RuntimeException("Test import error"));

    ProjectDTO result = null;
    Exception thrownException = null;

    try {
      result = sampleProjectService.createSampleProjectForUser(defaultUser);
    } catch (Exception e) {
      thrownException = e;
    }

    assertNull(thrownException, "createSampleProjectForUser method should not throw an exception");
    assertNotNull(result);
    assertEquals(newProjectId, result.getProjectId());
    verify(projectRepository, times(1)).saveAndFlush(any(Project.class));
    verify(chatImportService, times(1)).importChatsFromFile(any(ChatImportRequest.class));
  }
}
