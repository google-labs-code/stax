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

package com.planck.planck.domain.inferencemonitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.entitities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InferenceMonitoringServiceImplTest {

  @Mock private InferenceMonitoringRepository inferenceMonitoringRepository;

  @InjectMocks private InferenceMonitoringServiceImpl inferenceMonitoringService;

  private User user;
  private String projectId;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId("user-1");
    projectId = "project-1";
  }

  @Test
  void testGetProjectInferenceMonitoringSummary_NormalCase() {
    ProjectInferenceMonitoringSummaryDTO expectedSummary =
        ProjectInferenceMonitoringSummaryDTO.builder()
            .totalInferences(5L)
            .averageTurnTimeTaken(123.45)
            .totalPromptTokens(1000L)
            .totalCompletionTokens(500L)
            .totalTokens(1500L)
            .build();
    when(inferenceMonitoringRepository.getProjectInferenceMonitoringSummary(projectId, user))
        .thenReturn(expectedSummary);

    ProjectInferenceMonitoringSummaryDTO summary =
        inferenceMonitoringService.getProjectInferenceMonitoringSummary(user, projectId);
    assertNotNull(summary);
    assertEquals(5L, summary.getTotalInferences());
    assertEquals(123.45, summary.getAverageTurnTimeTaken());
    assertEquals(1000L, summary.getTotalPromptTokens());
    assertEquals(500L, summary.getTotalCompletionTokens());
    assertEquals(1500L, summary.getTotalTokens());
  }

  @Test
  void testGetProjectInferenceMonitoringSummary_EmptyResult() {
    ProjectInferenceMonitoringSummaryDTO emptyRepoResult =
        new ProjectInferenceMonitoringSummaryDTO(0L, 0.0, 0L, 0L, 0L);
    when(inferenceMonitoringRepository.getProjectInferenceMonitoringSummary(projectId, user))
        .thenReturn(emptyRepoResult);
    ProjectInferenceMonitoringSummaryDTO summary =
        inferenceMonitoringService.getProjectInferenceMonitoringSummary(user, projectId);
    assertNotNull(summary);
    assertEquals(0L, summary.getTotalInferences());
    assertEquals(0.0, summary.getAverageTurnTimeTaken());
    assertEquals(0L, summary.getTotalPromptTokens());
    assertEquals(0L, summary.getTotalCompletionTokens());
    assertEquals(0L, summary.getTotalTokens());
  }

  @Test
  void testGetProjectInferenceMonitoringSummary_NullResult() {
    when(inferenceMonitoringRepository.getProjectInferenceMonitoringSummary(projectId, user))
        .thenReturn(new ProjectInferenceMonitoringSummaryDTO());
    ProjectInferenceMonitoringSummaryDTO summary =
        inferenceMonitoringService.getProjectInferenceMonitoringSummary(user, projectId);
    assertNotNull(summary);
    assertEquals(0L, summary.getTotalInferences());
    assertEquals(0.0, summary.getAverageTurnTimeTaken());
    assertEquals(0L, summary.getTotalPromptTokens());
    assertEquals(0L, summary.getTotalCompletionTokens());
    assertEquals(0L, summary.getTotalTokens());
  }

  @Test
  void testGetProjectInferenceMonitoringSummary_NullFields() {
    when(inferenceMonitoringRepository.getProjectInferenceMonitoringSummary(projectId, user))
        .thenReturn(new ProjectInferenceMonitoringSummaryDTO());
    ProjectInferenceMonitoringSummaryDTO summary =
        inferenceMonitoringService.getProjectInferenceMonitoringSummary(user, projectId);
    assertNotNull(summary);
    assertEquals(0L, summary.getTotalInferences());
    assertEquals(0.0, summary.getAverageTurnTimeTaken());
    assertEquals(0L, summary.getTotalPromptTokens());
    assertEquals(0L, summary.getTotalCompletionTokens());
    assertEquals(0L, summary.getTotalTokens());
  }
}
