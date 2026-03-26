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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.evaluation.dto.ScoreDTO;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusService;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inferencestatus.InferenceStatusService;
import com.planck.planck.domain.job.dto.GridResponse;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.domain.job.dto.JobStatusPage;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.entitities.InputStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.enums.JobStatusEnum;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.util.PlanckConstants;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class JobStatusServiceImplTest {

  @InjectMocks private JobStatusServiceImpl jobStatusService;

  @Mock private JobStatusRepository jobStatusRepository;

  @Mock private InputStatusService inputStatusService;

  @Mock private ScoreStatusService scoreStatusService;

  @Mock private BulkUploadJobStatusService bulkUploadJobStatusService;

  @Mock private InferenceStatusService inferenceStatusService;
  @Mock private EvaluationStatusService evaluationStatusService;

  @Mock private EntityManager entityManager;
  @Mock private ProjectService projectService;

  private JobStatus jobStatus;
  private InferenceDTO inferenceDTO;
  private ScoreDTO scoreDTO;

  private User fakeUser;

  private Project fakeProject;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    fakeUser = new User();
    fakeUser.setId("user123");

    fakeProject = new Project();
    fakeProject.setId("project123");
    fakeProject.setUser(fakeUser);

    jobStatus = new JobStatus();
    jobStatus.setId("job123");
    jobStatus.setStatus(JobStatusEnum.IN_PROGRESS.getKey());
    jobStatus.setUser(fakeUser);
    jobStatus.setProject(fakeProject);
    jobStatus.setType("type1");
    jobStatus.setTotal(10);
    jobStatus.setInProgress(10);
    jobStatus.setSuccessful(0);
    jobStatus.setFailed(0);
    jobStatus.setComments("Job in progress");

    inferenceDTO = new InferenceDTO();
    inferenceDTO.setJobId("job123");

    scoreDTO = new ScoreDTO();
    scoreDTO.setJobId("job123");
  }

  @Test
  void testCreateJobStatus() {
    when(jobStatusRepository.save(any(JobStatus.class))).thenReturn(jobStatus);

    JobStatus createdJobStatus =
        jobStatusService.createJobStatus(
            "type1", "subType1", fakeUser, "project123", JobStatusEnum.IN_PROGRESS, 10);

    assertNotNull(createdJobStatus);
    assertEquals("job123", createdJobStatus.getId());
    assertEquals(JobStatusEnum.IN_PROGRESS.getKey(), createdJobStatus.getStatus());
    assertEquals("user123", createdJobStatus.getUser().getId());
    assertEquals("project123", createdJobStatus.getProject().getId());
  }

  @Test
  void testCreateJobStatus_subTypeNull() {
    when(jobStatusRepository.save(any(JobStatus.class))).thenReturn(jobStatus);

    JobStatus createdJobStatus =
        jobStatusService.createJobStatus(
            "type1", null, fakeUser, null, JobStatusEnum.IN_PROGRESS, 10);
    when(projectService.getProjectReference(null)).thenReturn(null);

    assertNotNull(createdJobStatus);
    assertEquals("job123", createdJobStatus.getId());
    assertEquals(JobStatusEnum.IN_PROGRESS.getKey(), createdJobStatus.getStatus());
    assertEquals("user123", createdJobStatus.getUser().getId());
  }

  @Test
  void testCreateScorerJobStatus() {
    when(jobStatusRepository.save(any(JobStatus.class))).thenReturn(jobStatus);

    JobStatus createdJobStatus =
        jobStatusService.createScorerJobStatus(
            "type1", "subType1", fakeUser, JobStatusEnum.IN_PROGRESS, 10, null);

    assertNotNull(createdJobStatus);
    assertEquals("job123", createdJobStatus.getId());
    assertEquals(JobStatusEnum.IN_PROGRESS.getKey(), createdJobStatus.getStatus());
    assertEquals("user123", createdJobStatus.getUser().getId());
  }

  @Test
  void testCreateScorerJobStatus_subTypeNull() {
    when(jobStatusRepository.save(any(JobStatus.class))).thenReturn(jobStatus);

    JobStatus createdJobStatus =
        jobStatusService.createScorerJobStatus(
            "type1", null, fakeUser, JobStatusEnum.IN_PROGRESS, 10, null);

    assertNotNull(createdJobStatus);
    assertEquals("job123", createdJobStatus.getId());
    assertEquals(JobStatusEnum.IN_PROGRESS.getKey(), createdJobStatus.getStatus());
    assertEquals("user123", createdJobStatus.getUser().getId());
  }

  @Test
  void testUpdateJobStatus() {
    doNothing()
        .when(jobStatusRepository)
        .updateStatusById(
            any(JobStatusEnum.class),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString());
    doNothing().when(jobStatusRepository).updateEndTimeByJobStatusId(any(), anyString());

    jobStatusService.updateJobStatus(
        5, 5, 2, 3, 0, JobStatusEnum.COMPLETED, inferenceDTO.getJobId());

    verify(jobStatusRepository, times(1))
        .updateStatusById(
            any(JobStatusEnum.class),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString());
    verify(jobStatusRepository, times(1)).updateEndTimeByJobStatusId(any(), anyString());
  }

  @Test
  void testUpdateJobStatus_NotSuccessful() {

    doNothing()
        .when(jobStatusRepository)
        .updateStatusById(
            any(JobStatusEnum.class),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString());

    doNothing().when(jobStatusRepository).updateEndTimeByJobStatusId(any(), anyString());

    jobStatusService.updateJobStatus(5, 5, 2, 3, 0, JobStatusEnum.FAILED, inferenceDTO.getJobId());

    verify(jobStatusRepository, times(1))
        .updateStatusById(
            any(JobStatusEnum.class),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString());

    verify(jobStatusRepository, times(1)).updateEndTimeByJobStatusId(any(), anyString());
  }

  @Test
  void testFindAllJobStatusByUser() {
    List<JobStatus> jobStatuses = Collections.singletonList(jobStatus);

    when(jobStatusRepository.findResultsAllByUserAndPageable(any(), any(Pageable.class)))
        .thenReturn(jobStatuses);
    when(jobStatusRepository.findCountAllByUser(any())).thenReturn(1);

    JobStatusPage jobStatusPage = new JobStatusPage();
    jobStatusPage.setPageIndex(1);
    jobStatusPage.setPageSize(10);

    GridResponse gridResponse = jobStatusService.findAllJobStatusByUser(fakeUser, jobStatusPage);

    assertNotNull(gridResponse);
    assertEquals(1, gridResponse.getTotalRecords());
  }

  @Test
  void testFindAllJobStatusByUser_withInputStatus() {

    InputStatus inputStatus = new InputStatus();
    inputStatus.setComment("comment");

    List<JobStatus> jobStatuses = Collections.singletonList(jobStatus);

    when(jobStatusRepository.findResultsAllByUserAndPageable(any(), any(Pageable.class)))
        .thenReturn(jobStatuses);
    when(jobStatusRepository.findCountAllByUser(any())).thenReturn(1);
    when(inputStatusService.findByUserIdAndJobStatusId(jobStatus.getId(), fakeUser.getId()))
        .thenReturn(Collections.singletonList(inputStatus));

    JobStatusPage jobStatusPage = new JobStatusPage();
    jobStatusPage.setPageIndex(1);
    jobStatusPage.setPageSize(10);

    GridResponse gridResponse = jobStatusService.findAllJobStatusByUser(fakeUser, jobStatusPage);

    assertNotNull(gridResponse);
    assertEquals(1, gridResponse.getTotalRecords());
    assertEquals(
        "comment",
        ((List<JobStatusDTO>) gridResponse.getResults()).get(0).getInputIds().get(0).getMessage());
  }

  @Test
  void testFindAllJobStatusByUser_jobstatusEmpty() {

    when(jobStatusRepository.findResultsAllByUserAndPageable(any(), any(Pageable.class)))
        .thenReturn(Collections.emptyList());
    when(jobStatusRepository.findCountAllByUser(any())).thenReturn(0);

    JobStatusPage jobStatusPage = new JobStatusPage();
    jobStatusPage.setPageIndex(1);
    jobStatusPage.setPageSize(10);

    GridResponse gridResponse = jobStatusService.findAllJobStatusByUser(fakeUser, jobStatusPage);

    assertNotNull(gridResponse);
    assertEquals(0, gridResponse.getTotalRecords());
    assertEquals(0, ((List<JobStatusDTO>) gridResponse.getResults()).size());
  }

  @Test
  void testFindAllJobStatusByProject() {
    List<JobStatus> jobStatuses = Collections.singletonList(jobStatus);

    when(jobStatusRepository.findResultsAllByUserAndProjectIdAndPageable(
            any(), anyString(), any(Pageable.class)))
        .thenReturn(jobStatuses);
    when(jobStatusRepository.findCountAllByUserAndProjectId(any(), anyString())).thenReturn(1);

    JobStatusPage jobStatusPage = new JobStatusPage();
    jobStatusPage.setPageIndex(1);
    jobStatusPage.setPageSize(10);

    GridResponse gridResponse =
        jobStatusService.findAllJobStatusByProject(fakeUser, fakeProject.getId(), jobStatusPage);

    assertNotNull(gridResponse);
    assertEquals(1, gridResponse.getTotalRecords());
    assertEquals(1, ((List<JobStatusDTO>) gridResponse.getResults()).size());
  }

  @Test
  void testFindAllJobStatusByProject_jobStatusEmpty() {

    when(jobStatusRepository.findResultsAllByUserAndProjectIdAndPageable(
            any(), anyString(), any(Pageable.class)))
        .thenReturn(Collections.emptyList());
    when(jobStatusRepository.findCountAllByUserAndProjectId(any(), anyString())).thenReturn(0);

    JobStatusPage jobStatusPage = new JobStatusPage();
    jobStatusPage.setPageIndex(1);
    jobStatusPage.setPageSize(10);

    GridResponse gridResponse =
        jobStatusService.findAllJobStatusByProject(fakeUser, fakeProject.getId(), jobStatusPage);

    assertNotNull(gridResponse);
    assertEquals(0, gridResponse.getTotalRecords());
    assertEquals(0, ((List<JobStatusDTO>) gridResponse.getResults()).size());
  }

  @Test
  void testFindById() {
    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));

    JobStatus foundJobStatus = jobStatusService.findById("job123");

    assertNotNull(foundJobStatus);
    assertEquals("job123", foundJobStatus.getId());
  }

  @Test
  void testFindByIdNotFound() {
    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> jobStatusService.findById("job123"));
  }

  @Test
  void testFetchDetailByJobInference() {
    User user = new User();
    user.setId("user123");
    jobStatus.setType(PlanckConstants.INFERENCE);

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));
    when(inputStatusService.getInferenceDetailByJob(
            anyString(), any(User.class), any(JobStatus.class)))
        .thenReturn(new HashMap<>());

    Map<String, Object> details = jobStatusService.fetchDetailByJob("job123", user);

    assertNotNull(details);
    verify(inputStatusService, times(1))
        .getInferenceDetailByJob(anyString(), any(User.class), any(JobStatus.class));
  }

  @Test
  void testFetchDetailByJobEval() {
    User user = new User();
    user.setId("user123");
    jobStatus.setType(PlanckConstants.EVAL);

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));
    when(scoreStatusService.findEvalByJobId(anyString(), any(User.class), any(JobStatus.class)))
        .thenReturn(new HashMap<>());

    Map<String, Object> details = jobStatusService.fetchDetailByJob("job123", user);

    assertNotNull(details);
    verify(scoreStatusService, times(1))
        .findEvalByJobId(anyString(), any(User.class), any(JobStatus.class));
  }

  @Test
  void testFetchDetailByJobBulk() {
    User user = new User();
    user.setId("user123");
    jobStatus.setType(PlanckConstants.BULK);

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));
    when(bulkUploadJobStatusService.getBulkDetailByJob(
            anyString(), any(User.class), any(JobStatus.class)))
        .thenReturn(new HashMap<>());

    Map<String, Object> details = jobStatusService.fetchDetailByJob("job123", user);

    assertNotNull(details);
    verify(bulkUploadJobStatusService, times(1))
        .getBulkDetailByJob(anyString(), any(User.class), any(JobStatus.class));
  }

  @Test
  void testFetchDetailByJob_NotFoundException() {
    User user = new User();
    user.setId("user123");
    jobStatus.setType(PlanckConstants.AGGREGATE);

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));

    assertThrows(NotFoundException.class, () -> jobStatusService.fetchDetailByJob("job123", user));
  }

  @Test
  void testFetchDetailByJobNotFound() {
    User user = new User();
    user.setId("user123");

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> jobStatusService.fetchDetailByJob("job123", user));
  }

  @Test
  void testDeleteJobStatusByJobIdInference() {
    User user = new User();
    user.setId("user123");
    jobStatus.setType(PlanckConstants.INFERENCE);

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));
    doNothing().when(inputStatusService).deleteByUserIdAndJobStatusId(anyString(), any());
    doNothing().when(jobStatusRepository).delete(any(JobStatus.class));

    jobStatusService.deleteJobStatusByJobId("job123", user);

    verify(inputStatusService, times(1)).deleteByUserIdAndJobStatusId(anyString(), any());
    verify(jobStatusRepository, times(1)).delete(any(JobStatus.class));
  }

  @Test
  void testDeleteJobStatusByJobIdBulk() {
    User user = new User();
    user.setId("user123");
    jobStatus.setType(PlanckConstants.BULK);

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));
    doNothing()
        .when(bulkUploadJobStatusService)
        .deleteByUserIdAndJobStatusId(anyString(), anyString());
    doNothing().when(jobStatusRepository).delete(any(JobStatus.class));

    jobStatusService.deleteJobStatusByJobId("job123", user);

    verify(bulkUploadJobStatusService, times(1))
        .deleteByUserIdAndJobStatusId(anyString(), anyString());
    verify(jobStatusRepository, times(1)).delete(any(JobStatus.class));
  }

  @Test
  void testDeleteJobStatusByJobIdEval() {
    User user = new User();
    user.setId("user123");
    jobStatus.setType(PlanckConstants.EVAL);

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));
    doNothing().when(scoreStatusService).deleteByUserAndJobId(anyString(), anyString());
    doNothing().when(jobStatusRepository).delete(any(JobStatus.class));

    jobStatusService.deleteJobStatusByJobId("job123", user);

    verify(scoreStatusService, times(1)).deleteByUserAndJobId(anyString(), anyString());
    verify(jobStatusRepository, times(1)).delete(any(JobStatus.class));
  }

  @Test
  void testDeleteJobStatusByJobId_NoDelete() {
    User user = new User();
    user.setId("user123");
    jobStatus.setType(PlanckConstants.AGGREGATE);

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.of(jobStatus));

    jobStatusService.deleteJobStatusByJobId("job123", user);

    verify(scoreStatusService, times(0)).deleteByUserAndJobId(anyString(), anyString());
    verify(jobStatusRepository, times(0)).delete(any(JobStatus.class));
  }

  @Test
  void testDeleteJobStatusByJobIdNotFound() {
    User user = new User();
    user.setId("user123");

    when(jobStatusRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> jobStatusService.deleteJobStatusByJobId("job123", user));
  }

  @Test
  void deleteByUserIdTest() {

    doNothing().when(jobStatusRepository).deleteByUser(any());

    jobStatusService.deleteByUser(fakeUser);
    verify(jobStatusRepository, times(1)).deleteByUser(any());
  }

  @Test
  void deleteByProjectIdTest() {
    when(jobStatusRepository.deleteByUserAndProjectId(any(), anyString()))
        .thenReturn(Collections.singletonList(jobStatus));

    assertEquals(jobStatusService.deleteByProject(fakeUser, fakeProject.getId()).size(), 1);
    verify(jobStatusRepository, times(1)).deleteByUserAndProjectId(any(), anyString());
  }

  @Test
  void testbulkDeleteJobStatusByJobId_NotFoundException() {
    User user = new User();
    user.setId("user123");

    Set<String> jobIds = Set.of("id");

    when(jobStatusRepository.findAllById(jobIds)).thenReturn(Collections.emptyList());
    assertThrows(
        NotFoundException.class, () -> jobStatusService.bulkDeleteJobStatusByJobId(jobIds, user));
  }

  @Test
  void testbulkDeleteJobStatusByJobId_success() {

    User user = new User();
    user.setId("user123");

    Set<String> jobIds = Set.of("id");

    JobStatus jobStatus1 = new JobStatus();
    jobStatus1.setId("id");
    jobStatus1.setType("inference");

    JobStatus jobStatus2 = new JobStatus();
    jobStatus2.setId("id");
    jobStatus2.setType("eval");

    JobStatus jobStatus3 = new JobStatus();
    jobStatus3.setId("id");
    jobStatus3.setType("bulk");

    JobStatus jobStatus4 = new JobStatus();
    jobStatus4.setId("id");
    jobStatus4.setType("test");

    when(jobStatusRepository.findAllById(jobIds))
        .thenReturn(List.of(jobStatus1, jobStatus2, jobStatus3, jobStatus4));
    Integer result = jobStatusService.bulkDeleteJobStatusByJobId(jobIds, user);
    assertEquals(3, result);
  }

  @Test
  void updateJobStatusForScorer() {

    doNothing()
        .when(jobStatusRepository)
        .updateStatusById(
            any(JobStatusEnum.class),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString());
    doNothing().when(jobStatusRepository).updateEndTimeByJobStatusId(any(), anyString());

    jobStatusService.updateJobStatus(5, 5, 2, 3, 0, JobStatusEnum.COMPLETED, scoreDTO.getJobId());

    verify(jobStatusRepository, times(1))
        .updateStatusById(
            any(JobStatusEnum.class),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString());
    verify(jobStatusRepository, times(1)).updateEndTimeByJobStatusId(any(), anyString());
  }

  @Test
  void updateJobStatusForScorer_NotSuccessful() {

    doNothing()
        .when(jobStatusRepository)
        .updateStatusById(
            any(JobStatusEnum.class),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString());

    doNothing().when(jobStatusRepository).updateEndTimeByJobStatusId(any(), anyString());

    jobStatusService.updateJobStatus(5, 5, 2, 3, 0, JobStatusEnum.FAILED, scoreDTO.getJobId());

    verify(jobStatusRepository, times(1))
        .updateStatusById(
            any(JobStatusEnum.class),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString());

    verify(jobStatusRepository, times(1)).updateEndTimeByJobStatusId(any(), anyString());
  }

  @Test
  void stopJobStatus_InferenceJob_Success() {
    jobStatus.setType(PlanckConstants.INFERENCE);
    jobStatus.setStatus(JobStatusEnum.PENDING.getKey()); // Работает

    when(jobStatusRepository.findById("job123")).thenReturn(Optional.of(jobStatus));
    when(inferenceStatusService.updateInferenceStatus(
            eq(InferenceStatusEnum.STOPPED),
            eq("job123"),
            anyString(),
            eq(InferenceStatusEnum.PENDING)))
        .thenReturn(5); // 5 статусов были остановлены
    when(jobStatusRepository.save(any(JobStatus.class))).thenReturn(jobStatus);

    // Act
    jobStatusService.stopJobStatus("job123", new Date(), "project123");

    // Assert
    verify(jobStatusRepository).findById("job123");
    verify(inferenceStatusService)
        .updateInferenceStatus(
            eq(InferenceStatusEnum.STOPPED),
            eq("job123"),
            anyString(),
            eq(InferenceStatusEnum.PENDING));
    verify(evaluationStatusService, never()).updateEvaluationStatus(any(), any(), any(), any());
    verify(jobStatusRepository).save(any(JobStatus.class));
    assertEquals(JobStatusEnum.STOPPED.getKey(), jobStatus.getStatus());
    assertEquals(5, jobStatus.getStopped());
    assertNotNull(jobStatus.getEndTime());
  }

  @Test
  void stopJobStatus_EvaluationJob_Success() {
    jobStatus.setType(PlanckConstants.EVAL);
    jobStatus.setStatus(JobStatusEnum.IN_PROGRESS.getKey()); // Работает

    when(jobStatusRepository.findById("job123")).thenReturn(Optional.of(jobStatus));
    when(evaluationStatusService.updateEvaluationStatus(
            eq(EvaluationStatusEnum.STOPPED),
            eq("job123"),
            anyString(),
            eq(EvaluationStatusEnum.PENDING)))
        .thenReturn(3); // 3 статуса были остановлены
    when(jobStatusRepository.save(any(JobStatus.class))).thenReturn(jobStatus);

    // Act
    jobStatusService.stopJobStatus("job123", new Date(), "project123");

    // Assert
    verify(jobStatusRepository).findById("job123");
    verify(inferenceStatusService, never()).updateInferenceStatus(any(), any(), any(), any());
    verify(evaluationStatusService)
        .updateEvaluationStatus(
            eq(EvaluationStatusEnum.STOPPED),
            eq("job123"),
            anyString(),
            eq(EvaluationStatusEnum.PENDING));
    verify(jobStatusRepository).save(any(JobStatus.class));
    assertEquals(JobStatusEnum.STOPPED.getKey(), jobStatus.getStatus());
    assertEquals(3, jobStatus.getStopped());
  }

  @Test
  void stopJobStatus_JobAlreadyStopped_ThrowsException() {
    jobStatus.setStatus(JobStatusEnum.STOPPED.getKey()); // Уже остановлен

    when(jobStatusRepository.findById("job123")).thenReturn(Optional.of(jobStatus));

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> jobStatusService.stopJobStatus("job123", new Date(), "project123"));
  }

  @Test
  void stopJobStatus_JobNotFound_ThrowsException() {
    when(jobStatusRepository.findById("job123")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        NotFoundException.class,
        () -> jobStatusService.stopJobStatus("job123", new Date(), "project123"));
  }

  @Test
  void stopJobStatus_WrongProject_ThrowsException() {
    when(jobStatusRepository.findById("job123")).thenReturn(Optional.of(jobStatus));

    // Act & Assert
    assertThrows(
        NotFoundException.class,
        () -> jobStatusService.stopJobStatus("job123", new Date(), "wrong-project-id"));
  }

  @Test
  void findAllJobStatusGroupedByProject_Success() {
    // Этот тест также покрывает лямбды $3 и $2
    JobStatus job2 = new JobStatus();
    job2.setId("job456");
    job2.setProject(fakeProject);
    job2.setStatus(JobStatusEnum.COMPLETED.getKey());

    when(jobStatusRepository.findAllByUserAndProjectIds(fakeUser, List.of("project123")))
        .thenReturn(List.of(jobStatus, job2));
    when(inputStatusService.findByUserIdAndJobStatusId(anyString(), eq(fakeUser.getId())))
        .thenReturn(Collections.emptyList());

    // Act
    Map<String, List<JobStatusDTO>> result =
        jobStatusService.findAllJobStatusGroupedByProject(fakeUser, List.of("project123"));

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey("project123"));
    assertEquals(2, result.get("project123").size());
    assertEquals("job123", result.get("project123").get(0).getJobId());
    assertEquals(JobStatusEnum.IN_PROGRESS.getValue(), result.get("project123").get(0).getStatus());
    assertEquals(JobStatusEnum.COMPLETED.getValue(), result.get("project123").get(1).getStatus());
  }

  @Test
  void hasRemainingJobsByProject_ReturnsTrue() {
    when(jobStatusRepository.existsByUserAndProjectIdAndStatusIn(
            eq(fakeUser), eq("project123"), anyList()))
        .thenReturn(true);

    // Act
    boolean result = jobStatusService.hasRemainingJobsByProject(fakeUser, "project123");

    // Assert
    assertTrue(result);
  }

  @Test
  void getJobIdsGroupedByStatus_Success() {
    when(jobStatusRepository.findIdsByUserAndProjectIdAndStatus(
            fakeUser, "project123", JobStatusEnum.IN_PROGRESS.getKey()))
        .thenReturn(List.of("job123"));
    when(jobStatusRepository.findIdsByUserAndProjectIdAndStatus(
            fakeUser, "project123", JobStatusEnum.COMPLETED.getKey()))
        .thenReturn(List.of("job456"));

    // Act
    Map<JobStatusEnum, List<String>> result =
        jobStatusService.getJobIdsGroupedByStatus(
            fakeUser, "project123", List.of(JobStatusEnum.IN_PROGRESS, JobStatusEnum.COMPLETED));

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(List.of("job123"), result.get(JobStatusEnum.IN_PROGRESS));
    assertEquals(List.of("job456"), result.get(JobStatusEnum.COMPLETED));
  }

  @Test
  void trackJobStatusCommon_UpdatesToCompleted() {
    // Этот тест также покрывает `determineStatusMain`
    jobStatus.setStatus(JobStatusEnum.IN_PROGRESS.getKey());
    when(jobStatusRepository.findById("job123")).thenReturn(Optional.of(jobStatus));

    // Функция-поставщик статистики
    Function<String, Map<String, Integer>> statFetcher =
        (jobId) ->
            Map.of(
                PlanckConstants.JOBS_PENDING, 0,
                PlanckConstants.JOBS_IN_PROGRESS, 0,
                PlanckConstants.JOBS_FAILED, 1,
                PlanckConstants.JOBS_STOPPED, 1,
                PlanckConstants.JOBS_SUCCESSFUL, 8); // 1 + 1 + 8 = 10 (Total)

    // Act
    jobStatusService.trackJobStatusCommon("job123", statFetcher);

    // Assert
    // Проверяем, что сервис (неправильно) установил IN_PROGRESS
    verify(jobStatusRepository)
        .updateStatusById(
            eq(JobStatusEnum.IN_PROGRESS), eq(0), eq(0), eq(8), eq(1), eq(1), eq("job123"));
    // Поскольку статус IN_PROGRESS, endTime НЕ должен был установиться
    verify(jobStatusRepository, never()).updateEndTimeByJobStatusId(any(Date.class), eq("job123"));
  }

  @Test
  void trackJobStatusCommon_UpdatesToInProgress() {
    // Этот тест также покрывает `determineStatusMain`
    jobStatus.setStatus(JobStatusEnum.PENDING.getKey());
    when(jobStatusRepository.findById("job123")).thenReturn(Optional.of(jobStatus));

    Function<String, Map<String, Integer>> statFetcher =
        (jobId) ->
            Map.of(
                PlanckConstants.JOBS_PENDING, 2,
                PlanckConstants.JOBS_IN_PROGRESS, 5,
                PlanckConstants.JOBS_FAILED, 1,
                PlanckConstants.JOBS_STOPPED, 0,
                PlanckConstants.JOBS_SUCCESSFUL, 2); // Total 10

    // Act
    jobStatusService.trackJobStatusCommon("job123", statFetcher);

    // Assert
    verify(jobStatusRepository)
        .updateStatusById(
            eq(JobStatusEnum.IN_PROGRESS), eq(2), eq(5), eq(2), eq(1), eq(0), eq("job123"));
    verify(jobStatusRepository, never()).updateEndTimeByJobStatusId(any(Date.class), eq("job123"));
  }

  @Test
  void trackJobStatusCommon_JobAlreadyStopped_DoesNothing() {
    jobStatus.setStatus(JobStatusEnum.STOPPED.getKey());
    when(jobStatusRepository.findById("job123")).thenReturn(Optional.of(jobStatus));

    Function<String, Map<String, Integer>> statFetcher = (jobId) -> Map.of();

    // Act
    jobStatusService.trackJobStatusCommon("job123", statFetcher);

    // Assert
    verify(jobStatusRepository, never())
        .updateStatusById(any(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
  }

  @Test
  void stopAllJobsByUser_CallsRepository() {
    doNothing().when(jobStatusRepository).stopAllOngoingJobsByUser(fakeUser);
    // Act
    jobStatusService.stopAllJobsByUser(fakeUser);
    // Assert
    verify(jobStatusRepository).stopAllOngoingJobsByUser(fakeUser);
  }
}
