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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluation.HumanEvalScoreRepository;
import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringService;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringService;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.domain.project.dto.CreateProjectCommand;
import com.planck.planck.domain.project.dto.ListProjectsResponse;
import com.planck.planck.domain.project.dto.ProjectDTO;
import com.planck.planck.domain.project.dto.UpdateProjectCommand;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.exceptions.NotFoundException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

  private static final String PROJECT_NAME = "project name";

  @Mock private ProjectRepository projectRepository;
  @Mock private EvaluationContainerRepository evaluationContainerRepository;
  @Mock private EvaluationContainerService evaluationContainerService;
  @Mock private JobStatusService jobStatusService;
  @Mock private EvaluationMonitoringService evaluationMonitoringService;
  @Mock private InferenceMonitoringService inferenceMonitoringService;
  @Mock private ModelResponseService modelResponseService;
  @Mock private ChatService chatService;
  @Mock private HumanEvalScoreRepository humanEvalScoreRepository;
  @Mock private LLMEvaluatorRepository llmEvaluatorRepository;
  @Mock private ScoreV2Repository scoreV2Repository;

  private User mockUser;

  @InjectMocks private ProjectServiceImpl projectService;

  @BeforeEach
  void setUp() {

    mockUser = new User();
    mockUser.setId("user-id");
  }

  @Test
  void testCreateProjectSuccess() {
    var command = new CreateProjectCommand("temp-id", "Test Project", "project desc", false, null);

    Project savedProject = getProject("Test Project");

    when(projectRepository.countByUser(mockUser)).thenReturn(0L);
    when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

    ProjectDTO projectCreated = projectService.createProject(mockUser, command);

    assertEquals(savedProject.getId(), projectCreated.getProjectId());
    assertEquals(savedProject.getName(), projectCreated.getName());
    assertEquals(savedProject.getDescription(), projectCreated.getDescription());
  }

  @Test
  void testCreateProject_NoName_Success() {
    var commandWithNoName = new CreateProjectCommand("temp-id", null, "project desc", false, null);

    Project projectWithDefaultName = getProject("Default 1");

    when(projectRepository.countByUser(mockUser)).thenReturn(0L);
    when(projectRepository.save(any(Project.class))).thenReturn(projectWithDefaultName);

    ProjectDTO projectCreated = projectService.createProject(mockUser, commandWithNoName);

    assertEquals("Default 1", projectCreated.getName());
    assertEquals("project-id-inserted-by-db", projectCreated.getProjectId());
  }

  @Test
  void testListProjectsSuccess() {
    List<Project> mockReturnedProjects = List.of(new Project());
    when(projectRepository.findAllByUserOrderByIsDefaultDescUpdatedAtDesc(any(), any()))
        .thenReturn(mockReturnedProjects);

    ListProjectsResponse listProjectsResponse =
        projectService.getAllProjectsSortedByIsDefaultAndUpdatedAt(mockUser, 3, 4, null, null);
    verify(projectRepository, times(1))
        .findAllByUserOrderByIsDefaultDescUpdatedAtDesc(eq(mockUser), any());
    assertEquals(listProjectsResponse.getProjects().size(), 1);
  }

  @Test
  void testGetProjectSuccess() {
    Optional<Project> mockReturnedProject = Optional.of(new Project());
    when(projectRepository.findByUserAndId(any(), any())).thenReturn(mockReturnedProject);

    ProjectDTO getProjectResponse =
        projectService.getProject(mockUser, "project-some-id-123", null);
    verify(projectRepository, times(1)).findByUserAndId(mockUser, "project-some-id-123");
    assertNotNull(getProjectResponse);
  }

  @Test
  void testGetProjectNotFound() {
    when(projectRepository.findByUserAndId(any(), any())).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> projectService.getProject(mockUser, "project-some-id-123", null));
    verify(projectRepository, times(1)).findByUserAndId(mockUser, "project-some-id-123");
  }

  @Test
  void testUpdateProjectSuccess() {
    String projectId = "project-id-inserted-by-db";
    var command = new UpdateProjectCommand("project name new", "project desc new");

    Project existingProject = getProject(PROJECT_NAME);

    when(projectRepository.findByUserAndId(mockUser, projectId))
        .thenReturn(Optional.of(existingProject));
    when(projectRepository.save(any(Project.class))).thenReturn(existingProject);

    projectService.updateProjectIgnoreOutputOnlyFields(projectId, command, mockUser);

    ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

    verify(projectRepository).save(projectCaptor.capture());

    Project capturedProject = projectCaptor.getValue();

    assertEquals("project name new", capturedProject.getName());
    assertEquals("project desc new", capturedProject.getDescription());
    assertEquals(existingProject.getCreatedAt(), capturedProject.getCreatedAt());
  }

  @Test
  void testUpdateProjectNotFound() {
    String projectId = "project-some-id-123";
    var command = new UpdateProjectCommand("any name", "any desc");

    when(projectRepository.findByUserAndId(mockUser, projectId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> projectService.updateProjectIgnoreOutputOnlyFields(projectId, command, mockUser));
  }

  @Test
  void testGetDistinctModelProvidersByProject() {
    String projectId = "project-123";
    List<ModelProvider> expectedProviders =
        Arrays.asList(ModelProvider.OPENAI, ModelProvider.GOOGLE);

    when(chatService.getDistinctModelProvidersByProject(mockUser, projectId))
        .thenReturn(expectedProviders);

    List<ModelProvider> result =
        projectService.getDistinctModelProvidersByProject(mockUser, projectId);

    assertEquals(expectedProviders, result);
    verify(chatService).getDistinctModelProvidersByProject(mockUser, projectId);
  }

  @Test
  void testGetProjectWithProviders() {
    String projectId = "project-123";
    Project project = getProject(PROJECT_NAME);
    project.setId(projectId);
    project.setUser(mockUser);

    List<ModelProvider> expectedProviders =
        Arrays.asList(ModelProvider.OPENAI, ModelProvider.GOOGLE);

    when(projectRepository.findByUserAndId(mockUser, projectId)).thenReturn(Optional.of(project));
    when(chatService.getDistinctModelProvidersByProject(mockUser, projectId))
        .thenReturn(expectedProviders);

    ProjectDTO result = projectService.getProject(mockUser, projectId, "modelProviders");

    assertNotNull(result);
    assertEquals(expectedProviders, result.getProviders());
    verify(chatService).getDistinctModelProvidersByProject(mockUser, projectId);
  }

  @Test
  void testGetProject_WithInferenceMonitoringSummary() {
    String projectId = "project-123";
    Project project = getProject(PROJECT_NAME);
    project.setId(projectId);
    project.setUser(mockUser);

    ProjectInferenceMonitoringSummaryDTO summary =
        ProjectInferenceMonitoringSummaryDTO.builder()
            .totalInferences(10L)
            .averageTurnTimeTaken(123.4)
            .totalPromptTokens(1000L)
            .totalCompletionTokens(500L)
            .totalTokens(1500L)
            .build();

    when(projectRepository.findByUserAndId(mockUser, projectId)).thenReturn(Optional.of(project));
    when(inferenceMonitoringService.getProjectInferenceMonitoringSummary(mockUser, projectId))
        .thenReturn(summary);

    ProjectDTO result = projectService.getProject(mockUser, projectId, "inferenceMetrics");
    assertNotNull(result.getInferenceMonitoringSummary());
    assertEquals(10L, result.getInferenceMonitoringSummary().getTotalInferences());
    assertEquals(123.4, result.getInferenceMonitoringSummary().getAverageTurnTimeTaken());
    assertEquals(1000L, result.getInferenceMonitoringSummary().getTotalPromptTokens());
    assertEquals(500L, result.getInferenceMonitoringSummary().getTotalCompletionTokens());
    assertEquals(1500L, result.getInferenceMonitoringSummary().getTotalTokens());
  }

  @Test
  void testGetProject_WithEmptyInferenceMonitoringSummary() {
    String projectId = "project-123";
    Project project = getProject(PROJECT_NAME);
    project.setId(projectId);
    project.setUser(mockUser);

    ProjectInferenceMonitoringSummaryDTO emptySummary =
        ProjectInferenceMonitoringSummaryDTO.builder()
            .totalInferences(0L)
            .averageTurnTimeTaken(0.0)
            .totalPromptTokens(0L)
            .totalCompletionTokens(0L)
            .totalTokens(0L)
            .build();

    when(projectRepository.findByUserAndId(mockUser, projectId)).thenReturn(Optional.of(project));
    when(inferenceMonitoringService.getProjectInferenceMonitoringSummary(mockUser, projectId))
        .thenReturn(emptySummary);

    ProjectDTO result = projectService.getProject(mockUser, projectId, "inferenceMetrics");
    assertNotNull(result.getInferenceMonitoringSummary());
    assertEquals(0L, result.getInferenceMonitoringSummary().getTotalInferences());
    assertEquals(0.0, result.getInferenceMonitoringSummary().getAverageTurnTimeTaken());
    assertEquals(0L, result.getInferenceMonitoringSummary().getTotalPromptTokens());
    assertEquals(0L, result.getInferenceMonitoringSummary().getTotalCompletionTokens());
    assertEquals(0L, result.getInferenceMonitoringSummary().getTotalTokens());
  }

  @Test
  void testGetProject_WithAllFields() {
    String projectId = "project-123";
    Project project = getProject(PROJECT_NAME);
    project.setId(projectId);
    project.setUser(mockUser);

    List<ModelProvider> expectedProviders =
        Arrays.asList(ModelProvider.OPENAI, ModelProvider.GOOGLE);

    ProjectInferenceMonitoringSummaryDTO summary =
        ProjectInferenceMonitoringSummaryDTO.builder()
            .totalInferences(10L)
            .averageTurnTimeTaken(123.4)
            .totalPromptTokens(1000L)
            .totalCompletionTokens(500L)
            .totalTokens(1500L)
            .build();

    LLMEvaluator evaluator = new LLMEvaluator();
    evaluator.setId("evaluator-123");
    evaluator.setName("evaluator-name");

    when(projectRepository.findByUserAndId(mockUser, projectId)).thenReturn(Optional.of(project));
    when(jobStatusService.findAllJobStatusGroupedByProject(any(), any()))
        .thenReturn(Collections.emptyMap());
    when(chatService.getDistinctModelProvidersByProject(mockUser, projectId))
        .thenReturn(expectedProviders);
    when(inferenceMonitoringService.getProjectInferenceMonitoringSummary(mockUser, projectId))
        .thenReturn(summary);
    when(humanEvalScoreRepository.findAllByContainerId(projectId)).thenReturn(List.of());

    when(llmEvaluatorRepository.findLatestRunEvaluatorByContainerId(projectId))
        .thenReturn(Optional.of(evaluator));
    when(scoreV2Repository.findAvgScoreByEvaluatorIdAndProjectId(evaluator.getId(), projectId))
        .thenReturn(1.0);

    ProjectDTO result = projectService.getProject(mockUser, projectId, "all");

    assertNotNull(result);
    assertEquals(expectedProviders, result.getProviders());
    assertNotNull(result.getInferenceMonitoringSummary());
    assertEquals(10L, result.getInferenceMonitoringSummary().getTotalInferences());
    assertNotNull(result.getHumanEvalMetrics());
    assertNotNull(result.getLatestEvalScore());
  }

  @Test
  void testGetProject_WithMultipleFields() {
    String projectId = "project-123";
    Project project = getProject(PROJECT_NAME);
    project.setId(projectId);
    project.setUser(mockUser);

    List<ModelProvider> expectedProviders =
        Arrays.asList(ModelProvider.OPENAI, ModelProvider.GOOGLE);

    when(projectRepository.findByUserAndId(mockUser, projectId)).thenReturn(Optional.of(project));
    when(chatService.getDistinctModelProvidersByProject(mockUser, projectId))
        .thenReturn(expectedProviders);
    when(humanEvalScoreRepository.findAllByContainerId(projectId)).thenReturn(List.of());

    ProjectDTO result =
        projectService.getProject(mockUser, projectId, "modelProviders,humanEvalMetrics");

    assertNotNull(result);
    assertEquals(expectedProviders, result.getProviders());
    assertNotNull(result.getHumanEvalMetrics());
    // These should be null since not requested
    assertNull(result.getInferenceMonitoringSummary());
    assertNull(result.getJobStatuses());
  }

  @Test
  void testListProjects_WithIncludeFields() {
    List<Project> mockReturnedProjects = List.of(new Project());
    when(projectRepository.findAllByUserOrderByIsDefaultDescUpdatedAtDesc(any(), any()))
        .thenReturn(mockReturnedProjects);

    ListProjectsResponse listProjectsResponse =
        projectService.getAllProjectsSortedByIsDefaultAndUpdatedAt(
            mockUser, 3, 4, null, "modelProviders");
    verify(projectRepository, times(1))
        .findAllByUserOrderByIsDefaultDescUpdatedAtDesc(eq(mockUser), any());
    assertEquals(listProjectsResponse.getProjects().size(), 1);
  }

  private Project getProject(String name) {
    Project project =
        Project.builder()
            .id("project-id-inserted-by-db")
            .user(mockUser)
            .name(name)
            .description("project desc")
            .createdAt(Timestamp.from(Instant.now()))
            .updatedAt(Timestamp.from(Instant.now()))
            .build();
    return project;
  }
}
