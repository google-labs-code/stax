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

import com.planck.planck.domain.job.dto.BulkInputStatusDTO;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.entitities.BulkUploadJobStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.BulkStatusEnum;
import com.planck.planck.enums.InputStatusEnum;
import com.planck.planck.enums.ScorerStatusEnum;
import com.planck.planck.exceptions.CustomRuntimeException;
import com.planck.planck.util.PlanckConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service("bulkUploadJobStatusService")
@Slf4j
public class BulkUploadJobStatusServiceImpl implements BulkUploadJobStatusService {

  private static final String JOBS_FAILED = "Jobs-failed";
  private static final String JOBS_SUCCESSFUL = "Jobs-Successful";
  private static final String JOBS_IN_PROGRESS = "Jobs-In-Progress";

  @Autowired private BulkUploadJobStatusRepository bulkUploadJobStatusRepository;

  @Override
  public Map<String, Integer> findAllByUserAndJobId(String jobId, String userId) {
    log.info("Get bulk status by userid {} and jobid :: {} ", userId, jobId);
    Map<String, Integer> responseMatrics = new HashMap<>();
    List<BulkUploadJobStatus> bulkStatuses =
        bulkUploadJobStatusRepository.findAllByUserAndJobId(jobId, userId);
    if (!CollectionUtils.isEmpty(bulkStatuses)) {
      Map<Integer, List<BulkUploadJobStatus>> groupedResults =
          bulkStatuses.stream().collect(Collectors.groupingBy(BulkUploadJobStatus::getStatus));
      List<BulkUploadJobStatus> matrics = groupedResults.get(BulkStatusEnum.IN_PROGRESS.getKey());
      int inprogress = !CollectionUtils.isEmpty(matrics) ? matrics.size() : 0;
      matrics = groupedResults.get(ScorerStatusEnum.PENDING.getKey());
      int pending = !CollectionUtils.isEmpty(matrics) ? matrics.size() : 0;

      responseMatrics.put(JOBS_IN_PROGRESS, (inprogress + pending));
      matrics = groupedResults.get(ScorerStatusEnum.SUCCESSFUL.getKey());
      responseMatrics.put(JOBS_SUCCESSFUL, !CollectionUtils.isEmpty(matrics) ? matrics.size() : 0);
      matrics = groupedResults.get(ScorerStatusEnum.FAILED.getKey());
      responseMatrics.put(JOBS_FAILED, !CollectionUtils.isEmpty(matrics) ? matrics.size() : 0);
      return responseMatrics;
    }
    log.info("Get bulk status response :: {} ", bulkStatuses);
    throw new CustomRuntimeException("Job Id not found :" + userId + " and User :" + userId);
  }

  @Override
  public Map<String, Object> getBulkDetailByJob(
      String jobStatusId, User userId, JobStatus jobStatus) {
    Map<String, Object> jobStausDetail = new HashMap<>();
    jobStausDetail.put(PlanckConstants.AGGREGATE, getJobStatusDTO(jobStatus));
    log.info("Get inputStatuses by job status Id :: {} userId :: {} ", jobStatusId, userId.getId());
    List<BulkUploadJobStatus> bulkStatuses =
        findByUserIdAndJobStatusId(jobStatusId, userId.getId());
    jobStausDetail.put(PlanckConstants.JOBS_DETAILS, convertToDTO(bulkStatuses, userId.getId()));
    return jobStausDetail;
  }

  private JobStatusDTO getJobStatusDTO(JobStatus jobStatus) {
    JobStatusDTO jobStatusDTO = new JobStatusDTO();
    jobStatusDTO.setInProgress(jobStatus.getInProgress());
    jobStatusDTO.setTotal(jobStatus.getTotal());
    jobStatusDTO.setFailed(jobStatus.getFailed());
    jobStatusDTO.setSuccessful(jobStatus.getSuccessful());
    return jobStatusDTO;
  }

  @Override
  public List<BulkUploadJobStatus> findByUserIdAndJobStatusId(String jobStatusId, String userId) {
    return bulkUploadJobStatusRepository.findAllByUserAndJobStatusId(jobStatusId, userId);
  }

  private List<BulkInputStatusDTO> convertToDTO(
      List<BulkUploadJobStatus> bulkStatuses, String userId) {
    return bulkStatuses.stream()
        .map(
            inputStatus -> {
              BulkInputStatusDTO bulkStatusDTO = new BulkInputStatusDTO();
              BeanUtils.copyProperties(inputStatus, bulkStatusDTO);
              bulkStatusDTO.setId(inputStatus.getId());
              bulkStatusDTO.setStatusId(inputStatus.getStatus());
              bulkStatusDTO.setStatus(InputStatusEnum.getValueByKey(inputStatus.getStatus()));
              bulkStatusDTO.setStartTime(inputStatus.getCreatedAt());
              bulkStatusDTO.setEndTime(inputStatus.getUpdatedAt());
              bulkStatusDTO.setMessage(inputStatus.getComment());
              bulkStatusDTO.setUserId(inputStatus.getUserId());
              return bulkStatusDTO;
            })
        .toList();
  }

  @Override
  public void deleteByUserIdAndJobStatusId(String jobStatusId, String userId) {
    List<BulkUploadJobStatus> bulkUploadJobStatus =
        bulkUploadJobStatusRepository.findAllByUserAndJobStatusId(jobStatusId, userId);
    bulkUploadJobStatusRepository.deleteAll(bulkUploadJobStatus);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteByUserId(String userId) {

    bulkUploadJobStatusRepository.deleteByUserId(userId);
  }
}
