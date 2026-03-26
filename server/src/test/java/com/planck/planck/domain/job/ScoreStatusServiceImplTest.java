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

package com.planck.planck.domain.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.evaluationstatus.EvaluationStatusDTO;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.ScoreStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.exceptions.CustomRuntimeException;
import com.planck.planck.util.PlanckConstants;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoreStatusServiceImplTest {

  @Mock private ScoreStatusRepository scorerStatusRepository;

  @Mock private EvaluationStatusRepository evaluationStatusRepository;

  @Mock private EntityManager entityManager;

  @InjectMocks private ScoreStatusServiceImpl scoreStatusService;

  private User testUser;
  private JobStatus testJobStatus;
  private EvaluationStatus testEvalStatus1;
  private EvaluationStatus testEvalStatus2;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-123");

    testJobStatus = new JobStatus();
    testJobStatus.setId("job-123");
    testJobStatus.setPending(1);
    testJobStatus.setInProgress(2);
    testJobStatus.setTotal(10);
    testJobStatus.setFailed(3);
    testJobStatus.setSuccessful(4);

    testEvalStatus1 = new EvaluationStatus();
    testEvalStatus1.setId("eval-1");
    testEvalStatus1.setStatus(EvaluationStatusEnum.SUCCESSFUL.getKey());
    testEvalStatus1.setEvaluatorId("evaluator-id-1");
    testEvalStatus1.setChatTurnId("ct-1");
    testEvalStatus1.setUserId("user-123");
    testEvalStatus1.setJobId("job-123");

    testEvalStatus2 = new EvaluationStatus();
    testEvalStatus2.setId("eval-2");
    testEvalStatus2.setStatus(EvaluationStatusEnum.FAILED.getKey());
    testEvalStatus2.setUserId("user-123");
    testEvalStatus2.setJobId("job-123");
  }

  @Test
  void findEvalByJobId_Success() {
    List<EvaluationStatus> statuses = List.of(testEvalStatus1, testEvalStatus2);
    when(evaluationStatusRepository.findAllByJobIdAndUserId("job-123", "user-123"))
        .thenReturn(statuses);

    // Act
    Map<String, Object> result =
        scoreStatusService.findEvalByJobId("job-123", testUser, testJobStatus);

    // Assert
    assertNotNull(result);
    assertTrue(result.containsKey(PlanckConstants.AGGREGATE));
    assertTrue(result.containsKey(PlanckConstants.JOBS_DETAILS));

    JobStatusDTO aggregate = (JobStatusDTO) result.get(PlanckConstants.AGGREGATE);
    assertEquals(10, aggregate.getTotal());
    assertEquals(4, aggregate.getSuccessful());
    assertEquals(3, aggregate.getFailed());
    assertEquals(1, aggregate.getPending());
    assertEquals(2, aggregate.getInProgress());

    List<EvaluationStatusDTO> details =
        (List<EvaluationStatusDTO>) result.get(PlanckConstants.JOBS_DETAILS);
    assertEquals(2, details.size());

    EvaluationStatusDTO dto1 = details.get(0);
    assertEquals("eval-1", dto1.getId());
    assertEquals(EvaluationStatusEnum.SUCCESSFUL.getValue(), dto1.getStatus());
    assertEquals("evaluator-id-1", dto1.getEvaluatorId());
    assertEquals("ct-1", dto1.getChatTurnId());

    EvaluationStatusDTO dto2 = details.get(1);
    assertEquals("eval-2", dto2.getId());
    assertEquals(EvaluationStatusEnum.FAILED.getValue(), dto2.getStatus());

    verify(evaluationStatusRepository).findAllByJobIdAndUserId("job-123", "user-123");
  }

  @Test
  void findEvalByJobId_NoStatusesFound_ThrowsException() {
    when(evaluationStatusRepository.findAllByJobIdAndUserId("job-123", "user-123"))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    CustomRuntimeException ex =
        assertThrows(
            CustomRuntimeException.class,
            () -> {
              scoreStatusService.findEvalByJobId("job-123", testUser, testJobStatus);
            });

    assertTrue(ex.getMessage().contains("Job Id not found"));
    verify(evaluationStatusRepository).findAllByJobIdAndUserId("job-123", "user-123");
  }

  @Test
  void deleteByUserAndJobId_Success() {
    List<ScoreStatus> scoreStatuses = List.of(new ScoreStatus(), new ScoreStatus());
    when(scorerStatusRepository.findAllByUserAndJobId("job-123", "user-123"))
        .thenReturn(scoreStatuses);
    doNothing().when(scorerStatusRepository).deleteAll(scoreStatuses);

    // Act
    scoreStatusService.deleteByUserAndJobId("job-123", "user-123");

    // Assert
    verify(scorerStatusRepository).findAllByUserAndJobId("job-123", "user-123");
    verify(scorerStatusRepository).deleteAll(scoreStatuses);
  }

  @Test
  void deleteByUserAndJobId_NoStatusesFound_DeletesNothing() {
    List<ScoreStatus> emptyList = Collections.emptyList();
    when(scorerStatusRepository.findAllByUserAndJobId("job-123", "user-123")).thenReturn(emptyList);

    // Act
    scoreStatusService.deleteByUserAndJobId("job-123", "user-123");

    // Assert
    verify(scorerStatusRepository).findAllByUserAndJobId("job-123", "user-123");
    verify(scorerStatusRepository).deleteAll(emptyList);
  }
}
