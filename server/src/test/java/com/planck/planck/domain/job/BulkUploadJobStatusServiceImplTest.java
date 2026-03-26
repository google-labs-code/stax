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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.entitities.BulkUploadJobStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.BulkStatusEnum;
import com.planck.planck.exceptions.CustomRuntimeException;
import com.planck.planck.util.PlanckConstants;
import java.util.ArrayList;
import java.util.Arrays;
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
public class BulkUploadJobStatusServiceImplTest {

  @Mock private BulkUploadJobStatusRepository bulkUploadJobStatusRepository;

  @InjectMocks private BulkUploadJobStatusServiceImpl bulkUploadJobStatusService;

  private User user;
  private String jobId;
  private String userId;
  private JobStatus jobStatus;
  private List<BulkUploadJobStatus> bulkStatuses;

  @BeforeEach
  void setUp() {

    user = new User();
    user.setId("userId");
    jobId = "jobId";
    userId = "userId";
    jobStatus = new JobStatus();
    jobStatus.setInProgress(2);
    jobStatus.setTotal(5);
    jobStatus.setFailed(1);
    jobStatus.setSuccessful(2);

    bulkStatuses = new ArrayList<>();
    BulkUploadJobStatus bulkStatus = new BulkUploadJobStatus();
    bulkStatus.setStatus(BulkStatusEnum.IN_PROGRESS.getKey());
    bulkStatuses.add(bulkStatus);
  }

  @Test
  void testFindAllByUserAndJobId() {
    when(bulkUploadJobStatusRepository.findAllByUserAndJobId(anyString(), anyString()))
        .thenReturn(bulkStatuses);

    Map<String, Integer> response = bulkUploadJobStatusService.findAllByUserAndJobId(jobId, userId);

    verify(bulkUploadJobStatusRepository, times(1)).findAllByUserAndJobId(anyString(), anyString());
    assertNotNull(response);
    assertTrue(response.containsKey("Jobs-In-Progress"));
  }

  @Test
  void testFindAllByUserAndJobId_Coverbranches() {

    BulkUploadJobStatus bulkStatus2 = new BulkUploadJobStatus();
    bulkStatus2.setStatus(BulkStatusEnum.PENDING.getKey());

    BulkUploadJobStatus bulkStatus3 = new BulkUploadJobStatus();
    bulkStatus3.setStatus(BulkStatusEnum.SUCCESSFUL.getKey());

    BulkUploadJobStatus bulkStatus4 = new BulkUploadJobStatus();
    bulkStatus4.setStatus(BulkStatusEnum.FAILED.getKey());

    List<BulkUploadJobStatus> bulkStatuses = Arrays.asList(bulkStatus2, bulkStatus3, bulkStatus4);

    when(bulkUploadJobStatusRepository.findAllByUserAndJobId(anyString(), anyString()))
        .thenReturn(bulkStatuses);

    Map<String, Integer> response = bulkUploadJobStatusService.findAllByUserAndJobId(jobId, userId);

    verify(bulkUploadJobStatusRepository, times(1)).findAllByUserAndJobId(anyString(), anyString());
    assertNotNull(response);
    assertTrue(response.containsKey("Jobs-In-Progress"));
  }

  @Test
  void testFindAllByUserAndJobId_NotFound() {
    when(bulkUploadJobStatusRepository.findAllByUserAndJobId(anyString(), anyString()))
        .thenReturn(Collections.emptyList());

    CustomRuntimeException exception =
        assertThrows(
            CustomRuntimeException.class,
            () -> {
              bulkUploadJobStatusService.findAllByUserAndJobId(jobId, userId);
            });

    assertEquals("Job Id not found :" + userId + " and User :" + userId, exception.getMessage());
  }

  @Test
  void testGetBulkDetailByJob() {
    when(bulkUploadJobStatusRepository.findAllByUserAndJobStatusId(anyString(), anyString()))
        .thenReturn(bulkStatuses);

    Map<String, Object> response =
        bulkUploadJobStatusService.getBulkDetailByJob(jobId, user, jobStatus);

    verify(bulkUploadJobStatusRepository, times(1))
        .findAllByUserAndJobStatusId(anyString(), anyString());
    assertNotNull(response);
    assertTrue(response.containsKey(PlanckConstants.AGGREGATE));
  }

  @Test
  void testDeleteByUserIdAndJobStatusId() {
    when(bulkUploadJobStatusRepository.findAllByUserAndJobStatusId(anyString(), anyString()))
        .thenReturn(bulkStatuses);

    bulkUploadJobStatusService.deleteByUserIdAndJobStatusId(jobId, userId);

    verify(bulkUploadJobStatusRepository, times(1)).deleteAll(anyList());
  }

  @Test
  void testDeleteByUserId() {
    doNothing().when(bulkUploadJobStatusRepository).deleteByUserId(anyString());

    bulkUploadJobStatusService.deleteByUserId(userId);

    verify(bulkUploadJobStatusRepository, times(1)).deleteByUserId(anyString());
  }
}
