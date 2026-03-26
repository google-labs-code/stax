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

import com.planck.planck.domain.job.dto.InputStatusDTO;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.entitities.InputStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputStatusEnum;
import com.planck.planck.util.PlanckConstants;
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
class InputStatusServiceImplTest {

  @Mock private InputStatusRepository inputStatusRepository;

  @InjectMocks private InputStatusServiceImpl inputStatusService;

  private User testUser;
  private JobStatus testJobStatus;
  private InputStatus inputPending;
  private InputStatus inputInProgress;
  private InputStatus inputSuccess;
  private InputStatus inputFailed;
  private String testJobId;
  private String testUserId;

  @BeforeEach
  void setUp() {
    testUserId = "user-123";
    testJobId = "job-123";

    testUser = new User();
    testUser.setId(testUserId);

    testJobStatus = new JobStatus();
    testJobStatus.setId(testJobId);
    testJobStatus.setTotal(10);
    testJobStatus.setSuccessful(5);
    testJobStatus.setFailed(1);
    testJobStatus.setInProgress(4);
    testJobStatus.setPending(0);

    inputPending = new InputStatus();
    inputPending.setId("is-1");
    inputPending.setStatus(InputStatusEnum.PENDING.getKey());
    inputPending.setUser(testUser);

    inputInProgress = new InputStatus();
    inputInProgress.setId("is-2");
    inputInProgress.setStatus(InputStatusEnum.IN_PROGRESS.getKey());
    inputInProgress.setUser(testUser);

    inputSuccess = new InputStatus();
    inputSuccess.setId("is-3");
    inputSuccess.setStatus(InputStatusEnum.SUCCESSFUL.getKey());
    inputSuccess.setUser(testUser);
    inputSuccess.setComment("Success");
    inputSuccess.setModelName("gpt-4");

    inputFailed = new InputStatus();
    inputFailed.setId("is-4");
    inputFailed.setStatus(InputStatusEnum.FAILED.getKey());
    inputFailed.setUser(testUser);
  }

  @Test
  void findAllByJobStatusIdAndUser_Success() {
    List<InputStatus> list =
        List.of(inputPending, inputInProgress, inputSuccess, inputFailed, inputInProgress);
    when(inputStatusRepository.findAllByUserAndJobStatusId(testJobId, testUserId)).thenReturn(list);

    // Act
    Map<String, Integer> result =
        inputStatusService.findAllByJobStatusIdAndUser(testJobId, testUserId);

    // Assert
    assertEquals(3, result.get("Jobs-In-Progress"));
    assertEquals(1, result.get("Jobs-Successful"));
    assertEquals(1, result.get("Jobs-failed"));
    verify(inputStatusRepository).findAllByUserAndJobStatusId(testJobId, testUserId);
  }

  @Test
  void findAllByJobStatusIdAndUser_Empty() {
    when(inputStatusRepository.findAllByUserAndJobStatusId(testJobId, testUserId))
        .thenReturn(Collections.emptyList());

    // Act
    Map<String, Integer> result =
        inputStatusService.findAllByJobStatusIdAndUser(testJobId, testUserId);

    // Assert
    assertEquals(0, result.get("Jobs-In-Progress"));
    assertEquals(0, result.get("Jobs-Successful"));
    assertEquals(0, result.get("Jobs-failed"));
  }

  @Test
  void getInferenceDetailByJob_Success() {
    List<InputStatus> list = List.of(inputSuccess);
    when(inputStatusRepository.findAllByUserAndJobStatusId(testJobId, testUserId)).thenReturn(list);

    // Act
    Map<String, Object> result =
        inputStatusService.getInferenceDetailByJob(testJobId, testUser, testJobStatus);

    // Assert
    assertNotNull(result.get(PlanckConstants.AGGREGATE));
    assertNotNull(result.get(PlanckConstants.JOBS_DETAILS));

    JobStatusDTO aggregate = (JobStatusDTO) result.get(PlanckConstants.AGGREGATE);
    assertEquals(10, aggregate.getTotal());
    assertEquals(5, aggregate.getSuccessful());
    assertEquals(1, aggregate.getFailed());
    assertEquals(4, aggregate.getInProgress());

    List<InputStatusDTO> details = (List<InputStatusDTO>) result.get(PlanckConstants.JOBS_DETAILS);
    assertEquals(1, details.size());
    assertEquals(InputStatusEnum.SUCCESSFUL.getValue(), details.get(0).getStatus());
    assertEquals("Success", details.get(0).getMessage());
    assertEquals("gpt-4", details.get(0).getModelName());
    assertEquals(testUserId, details.get(0).getUserId());
  }

  @Test
  void getInferenceDetailByJob_Empty() {
    when(inputStatusRepository.findAllByUserAndJobStatusId(testJobId, testUserId))
        .thenReturn(Collections.emptyList());

    // Act
    Map<String, Object> result =
        inputStatusService.getInferenceDetailByJob(testJobId, testUser, testJobStatus);

    // Assert
    assertNotNull(result.get(PlanckConstants.AGGREGATE));
    List<InputStatusDTO> details = (List<InputStatusDTO>) result.get(PlanckConstants.JOBS_DETAILS);
    assertTrue(details.isEmpty());
  }

  @Test
  void findByUserIdAndJobStatusId_Success() {
    List<InputStatus> list = List.of(inputSuccess);
    when(inputStatusRepository.findAllByUserAndJobStatusId(testJobId, testUserId)).thenReturn(list);

    // Act
    List<InputStatus> result = inputStatusService.findByUserIdAndJobStatusId(testJobId, testUserId);

    // Assert
    assertEquals(list, result);
    verify(inputStatusRepository).findAllByUserAndJobStatusId(testJobId, testUserId);
  }

  @Test
  void deleteByUserIdAndJobStatusId_Success() {
    List<InputStatus> list = List.of(inputSuccess, inputFailed);
    when(inputStatusRepository.findAllByUserAndJobStatusId(testJobId, testUserId)).thenReturn(list);
    doNothing().when(inputStatusRepository).deleteAll(list);

    // Act
    inputStatusService.deleteByUserIdAndJobStatusId(testJobId, testUserId);

    // Assert
    verify(inputStatusRepository).findAllByUserAndJobStatusId(testJobId, testUserId);
    verify(inputStatusRepository).deleteAll(list);
  }

  @Test
  void deleteByUserIdAndJobStatusId_Empty() {
    List<InputStatus> emptyList = Collections.emptyList();
    when(inputStatusRepository.findAllByUserAndJobStatusId(testJobId, testUserId))
        .thenReturn(emptyList);
    doNothing().when(inputStatusRepository).deleteAll(emptyList);

    // Act
    inputStatusService.deleteByUserIdAndJobStatusId(testJobId, testUserId);

    // Assert
    verify(inputStatusRepository).findAllByUserAndJobStatusId(testJobId, testUserId);
    verify(inputStatusRepository).deleteAll(emptyList);
  }
}
