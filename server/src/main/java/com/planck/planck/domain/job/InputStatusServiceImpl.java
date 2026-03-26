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

import com.planck.planck.domain.job.dto.InputStatusDTO;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.entitities.InputStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputStatusEnum;
import com.planck.planck.util.PlanckConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("inputStatusService")
@Slf4j
public class InputStatusServiceImpl implements InputStatusService {

  private static final String JOBS_FAILED = "Jobs-failed";
  private static final String JOBS_SUCCESSFUL = "Jobs-Successful";
  private static final String JOBS_IN_PROGRESS = "Jobs-In-Progress";

  @Autowired private InputStatusRepository inputStatusRepository;

  @PersistenceContext private EntityManager entityManager;

  /** TODO: Re-factor this to make the total count more accurate. */
  @Override
  public Map<String, Integer> findAllByJobStatusIdAndUser(String templateId, String userId) {
    log.info("Get inputStatuses by templateId :: {} userId :: {} ", templateId, userId);
    List<InputStatus> inputStatuses = findByUserIdAndJobStatusId(templateId, userId);
    // TODO: validation for template
    List<InputStatusDTO> inputStatusesDTO = convertToDTO(inputStatuses);
    Map<Integer, List<InputStatusDTO>> groupedResults =
        inputStatusesDTO.stream().collect(Collectors.groupingBy(InputStatusDTO::getStatusId));
    Map<String, Integer> responseMatrics = new HashMap<>();
    List<InputStatusDTO> matrics = groupedResults.get(InputStatusEnum.IN_PROGRESS.getKey());
    int inprogress = !CollectionUtils.isEmpty(matrics) ? matrics.size() : 0;
    matrics = groupedResults.get(InputStatusEnum.PENDING.getKey());
    int pending = !CollectionUtils.isEmpty(matrics) ? matrics.size() : 0;

    responseMatrics.put(JOBS_IN_PROGRESS, (inprogress + pending));
    matrics = groupedResults.get(InputStatusEnum.SUCCESSFUL.getKey());
    responseMatrics.put(JOBS_SUCCESSFUL, !CollectionUtils.isEmpty(matrics) ? matrics.size() : 0);
    matrics = groupedResults.get(InputStatusEnum.FAILED.getKey());
    responseMatrics.put(JOBS_FAILED, !CollectionUtils.isEmpty(matrics) ? matrics.size() : 0);
    log.info("ResponseMatrics is ::{} ", responseMatrics);
    return responseMatrics;
  }

  @Override
  public Map<String, Object> getInferenceDetailByJob(
      String jobStatusId, User userId, JobStatus jobStatus) {
    Map<String, Object> jobStausDetail = new HashMap<>();
    jobStausDetail.put(PlanckConstants.AGGREGATE, getJobStatusDTO(jobStatus));
    log.info("Get inputStatuses by job status Id :: {} userId :: {} ", jobStatusId, userId.getId());
    List<InputStatus> inputStatuses = findByUserIdAndJobStatusId(jobStatusId, userId.getId());
    jobStausDetail.put(PlanckConstants.JOBS_DETAILS, convertToDTO(inputStatuses, userId.getId()));
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
  public List<InputStatus> findByUserIdAndJobStatusId(String templateId, String userId) {
    return inputStatusRepository.findAllByUserAndJobStatusId(templateId, userId);
  }

  private List<InputStatusDTO> convertToDTO(List<InputStatus> inputStatuses) {
    return inputStatuses.stream()
        .map(
            inputStatus -> {
              InputStatusDTO inputStatusDTO = new InputStatusDTO();
              inputStatusDTO.setStatusId(inputStatus.getStatus());
              inputStatusDTO.setStatus(InputStatusEnum.getValueByKey(inputStatus.getStatus()));
              return inputStatusDTO;
            })
        .toList();
  }

  private List<InputStatusDTO> convertToDTO(List<InputStatus> inputStatuses, String userId) {
    return inputStatuses.stream()
        .map(
            inputStatus -> {
              InputStatusDTO inputStatusDTO = new InputStatusDTO();
              BeanUtils.copyProperties(inputStatus, inputStatusDTO);
              inputStatusDTO.setStatusId(inputStatus.getStatus());
              inputStatusDTO.setStatus(InputStatusEnum.getValueByKey(inputStatus.getStatus()));
              inputStatusDTO.setStartTime(inputStatus.getCreatedAt());
              inputStatusDTO.setEndTime(inputStatus.getUpdatedAt());
              inputStatusDTO.setMessage(inputStatus.getComment());
              inputStatusDTO.setModelName(inputStatus.getModelName());
              inputStatusDTO.setModelVersion(inputStatus.getModelVersion());
              inputStatusDTO.setInputId(inputStatus.getInputId());
              inputStatusDTO.setUserId(inputStatus.getUser().getId());
              inputStatusDTO.setInput(""); // TODO: Get from inputId if needed
              setResponseAttributes(inputStatusDTO, inputStatus, userId);
              return inputStatusDTO;
            })
        .toList();
  }

  private void setResponseAttributes(
      InputStatusDTO inputStatusDTO, InputStatus inputStatus, String userId) {
    // TODO: Implement response lookup without CandidateModel
    // For now, set default values
    inputStatusDTO.setResponseId(null);
    inputStatusDTO.setOutput(null);
  }

  @Override
  public void deleteByUserIdAndJobStatusId(String templateId, String userId) {
    List<InputStatus> inputStatus =
        inputStatusRepository.findAllByUserAndJobStatusId(templateId, userId);
    inputStatusRepository.deleteAll(inputStatus);
  }
}
