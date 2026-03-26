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
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("scoreStatusService")
@Slf4j
public class ScoreStatusServiceImpl implements ScoreStatusService {

  @Autowired private ScoreStatusRepository scorerStatusRepository;

  @Autowired private EvaluationStatusRepository evaluationStatusRepository;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Map<String, Object> findEvalByJobId(String jobId, User user, JobStatus jobStatus) {
    String userId = user.getId();
    Map<String, Object> jobStatusDetail = new HashMap<>();
    jobStatusDetail.put(PlanckConstants.AGGREGATE, getJobStatusDTO(jobStatus));
    log.info("Get Eval detail by userid {} and jobid :: {} ", userId, jobId);
    List<EvaluationStatus> evaluationStatuses =
        evaluationStatusRepository.findAllByJobIdAndUserId(jobId, userId);
    if (!CollectionUtils.isEmpty(evaluationStatuses)) {
      List<EvaluationStatusDTO> evaluationStatusDTOs =
          evaluationStatuses.stream()
              .map(
                  evaluationStatus -> {
                    EvaluationStatusDTO evaluationStatusDTO = convertToDTO(evaluationStatus);
                    // setScoreAttributes(userId, scoreStatusDTO);
                    return evaluationStatusDTO;
                  })
              .toList();

      jobStatusDetail.put(PlanckConstants.JOBS_DETAILS, evaluationStatusDTOs);
      return jobStatusDetail;
    }
    log.info("Get score status response :: {} ", evaluationStatuses);
    throw new CustomRuntimeException("Job Id not found :" + userId + " and User :" + userId);
  }

  private JobStatusDTO getJobStatusDTO(JobStatus jobStatus) {
    JobStatusDTO jobStatusDTO = new JobStatusDTO();
    jobStatusDTO.setPending(jobStatus.getPending());
    jobStatusDTO.setInProgress(jobStatus.getInProgress());
    jobStatusDTO.setTotal(jobStatus.getTotal());
    jobStatusDTO.setFailed(jobStatus.getFailed());
    jobStatusDTO.setSuccessful(jobStatus.getSuccessful());
    return jobStatusDTO;
  }

  protected EvaluationStatusDTO convertToDTO(EvaluationStatus evaluationStatus) {
    EvaluationStatusDTO evaluationStatusDTO = new EvaluationStatusDTO();
    evaluationStatusDTO.setId(evaluationStatus.getId());
    evaluationStatusDTO.setStatus(EvaluationStatusEnum.getValueByKey(evaluationStatus.getStatus()));
    evaluationStatusDTO.setStartTime(evaluationStatus.getCreatedAt());
    evaluationStatusDTO.setEndTime(evaluationStatus.getUpdatedAt());
    evaluationStatusDTO.setEvaluatorId(evaluationStatus.getEvaluatorId());
    evaluationStatusDTO.setUserId(evaluationStatus.getUserId());
    evaluationStatusDTO.setJobId(evaluationStatus.getJobId());
    evaluationStatusDTO.setProjectId(evaluationStatus.getContainerId());
    evaluationStatusDTO.setChatTurnId(evaluationStatus.getChatTurnId());
    evaluationStatusDTO.setJobStatusId(evaluationStatus.getJobStatusId());
    return evaluationStatusDTO;
  }

  @Override
  public void deleteByUserAndJobId(String jobId, String userId) {
    List<ScoreStatus> scoreStatuses = scorerStatusRepository.findAllByUserAndJobId(jobId, userId);
    scorerStatusRepository.deleteAll(scoreStatuses);
  }
}
