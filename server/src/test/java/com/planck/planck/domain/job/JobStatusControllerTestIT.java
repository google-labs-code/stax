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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.job.dto.DeleteJobRequest;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.domain.project.dto.CreateProjectCommand;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.*;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.JobStatusEnum;
import com.planck.planck.util.PlanckConstants;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class JobStatusControllerTestIT extends IntegrationTestBase {

  @Autowired private JobStatusRepository jobStatusRepository;
  @Autowired private InputStatusRepository inputStatusRepository;
  @Autowired private EvaluationStatusRepository evaluationStatusRepository;
  @Autowired private BulkUploadJobStatusRepository bulkUploadJobStatusRepository;
  @Autowired private ProjectService projectService;
  @Autowired private UserRepository userRepository;
  @Autowired private ObjectMapper objectMapper;

  private User testUser;
  private Project testProject;
  private JobStatus jobInference;
  private JobStatus jobEval;
  private JobStatus jobBulk;

  @BeforeEach
  public void setUp() {
    super.setUp();
    testUser = userRepository.findByEmail(PlanckConstants.DEFAULT_USER).orElseThrow();
    testProject =
        projectService.createProject(
            new CreateProjectCommand(
                "job-project", "Job Project", "Desc", false, EvaluationType.POINTWISE),
            testUser);

    jobInference =
        createJob("job-inf", PlanckConstants.INFERENCE, JobStatusEnum.IN_PROGRESS, testProject);
    jobEval = createJob("job-eval", PlanckConstants.EVAL, JobStatusEnum.PENDING, testProject);
    jobBulk = createJob("job-bulk", PlanckConstants.BULK, JobStatusEnum.COMPLETED, testProject);
  }

  private JobStatus createJob(String id, String type, JobStatusEnum status, Project project) {
    JobStatus job = new JobStatus();
    job.setId(id);
    job.setUser(testUser);
    job.setProject(project);
    job.setType(type);
    job.setStatus(status.getKey());
    job.setTotal(10);
    job.setPending(status == JobStatusEnum.PENDING ? 10 : 0);
    job.setInProgress(status == JobStatusEnum.IN_PROGRESS ? 10 : 0);
    job.setSuccessful(status == JobStatusEnum.COMPLETED ? 10 : 0);
    job.setFailed(0);
    job.setStartTime(new java.sql.Timestamp(System.currentTimeMillis() - 10000));
    return jobStatusRepository.save(job);
  }

  @Test
  void getInferenceDetailByJob_BulkType_Success() throws Exception {
    // Setup
    BulkUploadJobStatus bus1 = new BulkUploadJobStatus();
    bus1.setJobStatusId(jobBulk.getId());
    bus1.setUserId(testUser.getId());
    bus1.setStatus(JobStatusEnum.COMPLETED.getKey());
    bus1.setComment("Bulk done");
    bulkUploadJobStatusRepository.save(bus1);

    // Act & Assert
    mockMvc
        .perform(
            get("/job/status/{jobStatusId}", jobBulk.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.aggregate.total", equalTo(10)))
        .andExpect(jsonPath("$.aggregate.successful", equalTo(10)));
  }

  @Test
  void getInferenceDetailByJob_NotFound() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/job/status/{jobStatusId}", "non-existent-job")
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteJobStatusByJobId_Success() throws Exception {
    assertTrue(jobStatusRepository.findById(jobInference.getId()).isPresent());
    InputStatus is1 = new InputStatus();
    is1.setJobStatus(jobInference);
    is1.setUser(testUser);
    inputStatusRepository.save(is1);

    // Act & Assert
    mockMvc
        .perform(
            delete("/job/status/job/{jobId}", jobInference.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", equalTo(true)))
        .andExpect(jsonPath("$.message", containsString("Job is successfully deleted")));

    assertFalse(jobStatusRepository.findById(jobInference.getId()).isPresent());
    assertTrue(
        inputStatusRepository
            .findAllByUserAndJobStatusId(jobInference.getId(), testUser.getId())
            .isEmpty());
  }

  @Test
  void deleteBulkJobsByUser_Success() throws Exception {
    assertTrue(jobStatusRepository.findById(jobInference.getId()).isPresent());
    assertTrue(jobStatusRepository.findById(jobEval.getId()).isPresent());
    assertTrue(jobStatusRepository.findById(jobBulk.getId()).isPresent());

    DeleteJobRequest request = new DeleteJobRequest();
    request.setJobsId(List.of(jobInference.getId(), jobBulk.getId()));

    // Act & Assert
    mockMvc
        .perform(
            delete("/job/status/bulk")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", equalTo(true)))
        .andExpect(jsonPath("$.message", equalTo("2 Jobs is successfully deleted.")));

    assertFalse(jobStatusRepository.findById(jobInference.getId()).isPresent());
    assertFalse(jobStatusRepository.findById(jobBulk.getId()).isPresent());
    assertTrue(jobStatusRepository.findById(jobEval.getId()).isPresent());
  }

  @Test
  void hasPendingJobs_ReturnsTrue() throws Exception {

    // Act & Assert
    mockMvc
        .perform(
            get("/job/projects/{projectId}/has-remaining", testProject.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void hasPendingJobs_ReturnsFalse() throws Exception {
    jobInference.setStatus(JobStatusEnum.COMPLETED.getKey());
    jobEval.setStatus(JobStatusEnum.FAILED.getKey());
    jobBulk.setStatus(JobStatusEnum.COMPLETED.getKey());
    jobStatusRepository.saveAll(List.of(jobInference, jobEval, jobBulk));

    // Act & Assert
    mockMvc
        .perform(
            get("/job/projects/{projectId}/has-remaining", testProject.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  void getJobIdsByStatus_Success() throws Exception {

    // Act & Assert
    mockMvc
        .perform(
            get("/job/projects/{projectId}/ids-by-status", testProject.getId())
                .header("Authorization", getBearerJwtToken())
                .param("status", "PENDING,COMPLETED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.PENDING.size()", equalTo(1)))
        .andExpect(jsonPath("$.PENDING[0]", equalTo(jobEval.getId())))
        .andExpect(jsonPath("$.COMPLETED.size()", equalTo(1)))
        .andExpect(jsonPath("$.COMPLETED[0]", equalTo(jobBulk.getId())))
        .andExpect(jsonPath("$.IN_PROGRESS").doesNotExist());
  }

  @Test
  void getJobIdsByStatus_NoParams_ReturnsAll() throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/job/projects/{projectId}/ids-by-status", testProject.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.PENDING[0]", equalTo(jobEval.getId())))
        .andExpect(jsonPath("$.IN_PROGRESS[0]", equalTo(jobInference.getId())))
        .andExpect(jsonPath("$.COMPLETED[0]", equalTo(jobBulk.getId())));
  }
}
