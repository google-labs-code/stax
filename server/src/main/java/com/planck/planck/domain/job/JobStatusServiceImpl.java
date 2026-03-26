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

import com.planck.planck.domain.evaluationstatus.EvaluationStatusService;
import com.planck.planck.domain.inferencestatus.InferenceStatusService;
import com.planck.planck.domain.job.dto.GridResponse;
import com.planck.planck.domain.job.dto.InputStatusDTO;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.domain.job.dto.JobStatusPage;
import com.planck.planck.entitities.InputStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.enums.InputStatusEnum;
import com.planck.planck.enums.JobStatusEnum;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.util.PlanckConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service("jobStatusService")
public class JobStatusServiceImpl implements JobStatusService {

  @Autowired private JobStatusRepository jobStatusRepository;

  @Autowired private InputStatusService inputStatusService;

  @Autowired private ScoreStatusService scoreStatusService;

  @Autowired private BulkUploadJobStatusService bulkUploadJobStatusService;

  @Autowired private InferenceStatusService inferenceStatusService;

  @Autowired private EvaluationStatusService evaluationStatusService;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public JobStatus createJobStatus(
      String type,
      String subType,
      User user,
      String projectId,
      JobStatusEnum inputStatusEnum,
      Integer total) {
    JobStatus jobStatus = new JobStatus();
    jobStatus.setStatus(inputStatusEnum.getKey());
    jobStatus.setUser(user);
    Project project =
        projectId != null ? entityManager.getReference(Project.class, projectId) : null;
    jobStatus.setProject(project);
    jobStatus.setType(type);
    if (StringUtils.isNoneBlank(subType)) {
      jobStatus.setSubType(subType);
    }
    jobStatus.setTotal(total);
    jobStatus.setInProgress(total);
    jobStatus.setSuccessful(0);
    jobStatus.setFailed(0);
    jobStatus.setComments(type + " is " + inputStatusEnum.getValue() + " stat.");
    jobStatus = jobStatusRepository.save(jobStatus);
    log.info(
        "Job Status created successfully id: {}, status:{}, user:{}, project:{}",
        jobStatus.getId(),
        inputStatusEnum.getValue(),
        user,
        project);
    return jobStatus;
  }

  @Override
  public JobStatus createScorerJobStatus(
      String type,
      String subType,
      User user,
      JobStatusEnum inputStatusEnum,
      Integer total,
      Project project) {
    JobStatus jobStatus = new JobStatus();
    jobStatus.setStatus(inputStatusEnum.getKey());
    jobStatus.setUser(user);
    jobStatus.setType(type);
    if (StringUtils.isNoneBlank(subType)) {
      jobStatus.setSubType(subType);
    }
    jobStatus.setTotal(total);
    jobStatus.setPending(total);
    jobStatus.setInProgress(0);
    jobStatus.setSuccessful(0);
    jobStatus.setFailed(0);
    jobStatus.setProject(project);
    jobStatus.setComments("Score is " + inputStatusEnum.getValue() + " stat.");
    jobStatus = jobStatusRepository.save(jobStatus);
    return jobStatus;
  }

  @Override
  public void updateJobStatus(
      Integer pending,
      Integer inprogress,
      Integer failed,
      Integer stopped,
      Integer successful,
      JobStatusEnum inputStatusEnum,
      String jobId) {
    log.info(
        "Update template Status :: inprogress: {} failed: {} successful: {} inputStatusEnum:{} ",
        pending,
        inprogress,
        failed,
        successful,
        inputStatusEnum.getValue());

    jobStatusRepository.updateStatusById(
        inputStatusEnum, pending, inprogress, successful, failed, stopped, jobId);
    Set<Integer> finalStatuses =
        Set.of(InputStatusEnum.SUCCESSFUL.getKey(), InputStatusEnum.FAILED.getKey());

    if (finalStatuses.contains(inputStatusEnum.getKey())) {
      log.info("Updated Job Status for :: Id: {} ", jobId);
      jobStatusRepository.updateEndTimeByJobStatusId(new Date(), jobId);
    }
  }

  @Transactional
  @Override
  public void stopJobStatus(String jobStatusId, Date endTime, String projectId) {
    Optional<JobStatus> jobStatusOpt = jobStatusRepository.findById(jobStatusId);
    if (jobStatusOpt.isEmpty()) {
      throw new NotFoundException("JobStatus not found for id: " + jobStatusId);
    }
    JobStatus jobStatus = jobStatusOpt.get();
    if (!jobStatus.getProject().getId().equals(projectId)) {
      throw new NotFoundException("JobStatus not found for id: " + jobStatusId);
    }

    JobStatusEnum currentStatusEnum = JobStatusEnum.getJobStatusEnumByKey(jobStatus.getStatus());
    if (currentStatusEnum == JobStatusEnum.STOPPED
        || currentStatusEnum == JobStatusEnum.COMPLETED) {
      throw new IllegalStateException("JobStatus already STOPPED or COMPLETED");
    }

    int stoppedStatuses = 0;

    if (PlanckConstants.INFERENCE.equalsIgnoreCase(jobStatus.getType())) {
      stoppedStatuses =
          inferenceStatusService.updateInferenceStatus(
              InferenceStatusEnum.STOPPED,
              jobStatusId,
              "Job has been stopped manually.",
              InferenceStatusEnum.PENDING);
    } else if (PlanckConstants.EVAL.equalsIgnoreCase(jobStatus.getType())) {
      stoppedStatuses =
          evaluationStatusService.updateEvaluationStatus(
              EvaluationStatusEnum.STOPPED,
              jobStatusId,
              "Job has been stopped manually.",
              EvaluationStatusEnum.PENDING);
    }

    jobStatus.setStatus(JobStatusEnum.STOPPED.getKey());
    jobStatus.setEndTime(endTime);
    jobStatus.setStopped(stoppedStatuses);
    jobStatusRepository.save(jobStatus);
  }

  @Override
  public GridResponse findAllJobStatusByUser(User user, JobStatusPage promptJobStatusPage) {
    log.info("Get job status by userId {} ", user.getId());
    GridResponse gridResponse = new GridResponse();
    gridResponse.setTotalRecords(jobStatusRepository.findCountAllByUser(user));
    // create pagerequest object
    Pageable pageable =
        PageRequest.of(
            promptJobStatusPage.getPageIndex() - 1,
            promptJobStatusPage.getPageSize(),
            Sort.by("startTime").descending());

    List<JobStatusDTO> templateStatusdtos = new ArrayList<>();
    List<JobStatus> jobStatuses =
        jobStatusRepository.findResultsAllByUserAndPageable(user, pageable);
    if (!CollectionUtils.isEmpty(jobStatuses)) {
      jobStatuses.forEach(
          jobStaus -> {
            JobStatusDTO promptJobStatusDTO = convertJobStatusToDTO(user.getId(), jobStaus);
            templateStatusdtos.add(promptJobStatusDTO);
          });
    }
    gridResponse.setResults(templateStatusdtos);

    log.info("Get prompt job status gridResponse {} ", gridResponse);
    return gridResponse;
  }

  @Override
  public Map<String, List<JobStatusDTO>> findAllJobStatusGroupedByProject(
      User user, List<String> projectIds) {

    List<JobStatus> jobStatuses = jobStatusRepository.findAllByUserAndProjectIds(user, projectIds);

    Map<String, List<JobStatus>> grouped =
        jobStatuses.stream().collect(Collectors.groupingBy(js -> js.getProject().getId()));

    return grouped.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry ->
                    entry.getValue().stream()
                        .map(js -> convertJobStatusToDTO(user.getId(), js))
                        .toList()));
  }

  @Override
  public GridResponse findAllJobStatusByProject(
      User user, String projectId, JobStatusPage promptJobStatusPage) {
    log.info("Get job status by userId {} and project {}", user.getId(), projectId);
    GridResponse gridResponse = new GridResponse();
    gridResponse.setTotalRecords(
        jobStatusRepository.findCountAllByUserAndProjectId(user, projectId));
    // create pagerequest object
    Pageable pageable =
        PageRequest.of(
            promptJobStatusPage.getPageIndex() - 1,
            promptJobStatusPage.getPageSize(),
            Sort.by("startTime").descending());

    List<JobStatusDTO> templateStatusdtos = new ArrayList<>();
    List<JobStatus> jobStatuses =
        jobStatusRepository.findResultsAllByUserAndProjectIdAndPageable(user, projectId, pageable);
    if (!CollectionUtils.isEmpty(jobStatuses)) {
      jobStatuses.forEach(
          jobStaus -> {
            JobStatusDTO promptJobStatusDTO = convertJobStatusToDTO(user.getId(), jobStaus);
            templateStatusdtos.add(promptJobStatusDTO);
          });
    }
    gridResponse.setResults(templateStatusdtos);
    log.info("Get prompt job status gridResponse {} ", gridResponse);
    return gridResponse;
  }

  private JobStatusDTO convertJobStatusToDTO(String userId, JobStatus jobStaus) {
    JobStatusDTO jobStatusDTO = new JobStatusDTO();
    BeanUtils.copyProperties(jobStaus, jobStatusDTO);
    jobStatusDTO.setJobId(jobStaus.getId());
    jobStatusDTO.setStatus(JobStatusEnum.getValueByKey(jobStaus.getStatus()));
    jobStatusDTO.setInputIds(fetchInputStatus(userId, jobStaus));
    return jobStatusDTO;
  }

  private List<InputStatusDTO> fetchInputStatus(String userId, JobStatus jobStaus) {
    List<InputStatus> inputStatuses =
        inputStatusService.findByUserIdAndJobStatusId(jobStaus.getId(), userId);
    if (!CollectionUtils.isEmpty(inputStatuses)) {
      return inputStatuses.stream()
          .map(
              inputStatus -> {
                InputStatusDTO inputStatusDTO = new InputStatusDTO();
                inputStatusDTO.setStatus(InputStatusEnum.getValueByKey(inputStatus.getStatus()));
                inputStatusDTO.setStartTime(inputStatus.getCreatedAt());
                inputStatusDTO.setEndTime(inputStatus.getUpdatedAt());
                inputStatusDTO.setMessage(inputStatus.getComment());
                inputStatusDTO.setModelName(inputStatus.getModelName());
                inputStatusDTO.setModelVersion(inputStatus.getModelVersion());
                return inputStatusDTO;
              })
          .toList();
    }
    return Collections.emptyList();
  }

  @Override
  public JobStatus findById(String jobId) {
    Optional<JobStatus> jobStaus = jobStatusRepository.findById(jobId);
    if (jobStaus.isPresent()) {
      return jobStaus.get();
    }
    throw new NotFoundException("Job Status not fount for " + jobId);
  }

  @Override
  public Map<String, Object> fetchDetailByJob(String jobStatusIdId, User user) {
    JobStatus jobStatus = findById(jobStatusIdId);
    if (PlanckConstants.INFERENCE.equalsIgnoreCase(jobStatus.getType())) {
      return inputStatusService.getInferenceDetailByJob(jobStatusIdId, user, jobStatus);
    } else if (PlanckConstants.EVAL.equalsIgnoreCase(jobStatus.getType())) {
      return scoreStatusService.findEvalByJobId(jobStatusIdId, user, jobStatus);
    } else if (PlanckConstants.BULK.equalsIgnoreCase(jobStatus.getType())) {
      return bulkUploadJobStatusService.getBulkDetailByJob(jobStatusIdId, user, jobStatus);
    }
    throw new NotFoundException("Job Status not found for :" + jobStatusIdId);
  }

  @Override
  public void deleteJobStatusByJobId(String jobStatusIdId, User user) {
    JobStatus jobStatus = findById(jobStatusIdId);
    if (PlanckConstants.INFERENCE.equalsIgnoreCase(jobStatus.getType())) {
      inputStatusService.deleteByUserIdAndJobStatusId(jobStatusIdId, user.getId());
      jobStatusRepository.delete(jobStatus);
    } else if (PlanckConstants.EVAL.equalsIgnoreCase(jobStatus.getType())) {
      scoreStatusService.deleteByUserAndJobId(jobStatusIdId, user.getId());
      jobStatusRepository.delete(jobStatus);
    } else if (PlanckConstants.BULK.equalsIgnoreCase(jobStatus.getType())) {
      bulkUploadJobStatusService.deleteByUserIdAndJobStatusId(jobStatusIdId, user.getId());
      jobStatusRepository.delete(jobStatus);
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteByUser(User user) {
    jobStatusRepository.deleteByUser(user);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<JobStatus> deleteByProject(User user, String projectId) {
    return jobStatusRepository.deleteByUserAndProjectId(user, projectId);
  }

  @Transactional
  @Override
  public Integer deleteByProjectIdAndUserId(String projectId, User user) {
    List<JobStatus> jobsToBeDeleted =
        jobStatusRepository.findAllByUserAndProjectIds(user, Collections.singletonList(projectId));
    if (jobsToBeDeleted == null || jobsToBeDeleted.isEmpty()) {
      return 0;
    }
    return bulkDeleteJobStatusByJobId(jobsToBeDeleted, user);
  }

  @Transactional
  @Override
  public Integer bulkDeleteJobStatusByJobId(Collection<JobStatus> jobStatusSet, User user) {

    if (CollectionUtils.isEmpty(jobStatusSet)) {
      throw new NotFoundException("Jobs not found by user");
    }

    int count = 0;
    for (JobStatus jobStatus : jobStatusSet) {
      if (PlanckConstants.INFERENCE.equalsIgnoreCase(jobStatus.getType())) {
        inputStatusService.deleteByUserIdAndJobStatusId(jobStatus.getId(), user.getId());
        jobStatusRepository.delete(jobStatus);
        count++;
      } else if (PlanckConstants.EVAL.equalsIgnoreCase(jobStatus.getType())) {
        scoreStatusService.deleteByUserAndJobId(jobStatus.getId(), user.getId());
        jobStatusRepository.delete(jobStatus);
        count++;
      } else if (PlanckConstants.BULK.equalsIgnoreCase(jobStatus.getType())) {
        bulkUploadJobStatusService.deleteByUserIdAndJobStatusId(jobStatus.getId(), user.getId());
        jobStatusRepository.delete(jobStatus);
        count++;
      }
    }
    return count;
  }

  @Transactional
  @Override
  public Integer bulkDeleteJobStatusByJobId(Set<String> jobsIdSet, User user) {

    List<JobStatus> allJobs = jobStatusRepository.findAllById(jobsIdSet);
    return bulkDeleteJobStatusByJobId(allJobs, user);
  }

  @Override
  public boolean hasRemainingJobsByProject(User user, String projectId) {
    List<Integer> statuses =
        List.of(JobStatusEnum.PENDING.getKey(), JobStatusEnum.IN_PROGRESS.getKey());
    return jobStatusRepository.existsByUserAndProjectIdAndStatusIn(user, projectId, statuses);
  }

  @Override
  public Map<JobStatusEnum, List<String>> getJobIdsGroupedByStatus(
      User user, String projectId, List<JobStatusEnum> statuses) {

    List<JobStatusEnum> statusList =
        (statuses == null || statuses.isEmpty()) ? List.of(JobStatusEnum.values()) : statuses;

    Map<JobStatusEnum, List<String>> result = new LinkedHashMap<>();

    for (JobStatusEnum statusEnum : statusList) {
      List<String> ids =
          jobStatusRepository.findIdsByUserAndProjectIdAndStatus(
              user, projectId, statusEnum.getKey());
      result.put(statusEnum, ids);
    }

    return result;
  }

  @Override
  public void trackJobStatusCommon(
      String jobId, Function<String, Map<String, Integer>> statFetcher) {
    Map<String, Integer> stats = statFetcher.apply(jobId);

    JobStatus jobStatus =
        jobStatusRepository
            .findById(jobId)
            .orElseThrow(() -> new NotFoundException("JobStatus not found for id: " + jobId));

    JobStatusEnum currentStatus = JobStatusEnum.getJobStatusEnumByKey(jobStatus.getStatus());
    if (currentStatus == JobStatusEnum.STOPPED) {
      return;
    }

    int pending = stats.getOrDefault(PlanckConstants.JOBS_PENDING, 0);
    int inProgress = stats.getOrDefault(PlanckConstants.JOBS_IN_PROGRESS, 0);
    int failed = stats.getOrDefault(PlanckConstants.JOBS_FAILED, 0);
    int stopped = stats.getOrDefault(PlanckConstants.JOBS_STOPPED, 0);
    int successful = stats.getOrDefault(PlanckConstants.JOBS_SUCCESSFUL, 0);
    int total = pending + inProgress + failed + successful;

    JobStatusEnum status =
        determineStatusMain(total, inProgress, failed, pending, successful, stopped);
    updateJobStatus(
        pending,
        inProgress,
        failed,
        stopped,
        successful,
        JobStatusEnum.getJobStatusEnumByKey(status.getKey()),
        jobId);
  }

  private JobStatusEnum determineStatusMain(
      int total, int inProgress, int failed, int pending, int successful, int stopped) {
    if (pending == total) return JobStatusEnum.PENDING;

    if (failed == total) return JobStatusEnum.FAILED;

    if ((successful + failed + stopped) == total) return JobStatusEnum.COMPLETED;

    return JobStatusEnum.IN_PROGRESS;
  }

  @Override
  @Transactional()
  public void stopAllJobsByUser(User user) {
    jobStatusRepository.stopAllOngoingJobsByUser(user);
  }
}
