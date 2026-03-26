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

package com.planck.planck.domain.evaluationstatus;

import com.planck.planck.domain.evaluation.dto.EvaluationDTO;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.exceptions.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service("evaluationStatusService")
@Slf4j
public class EvaluationStatusServiceImpl implements EvaluationStatusService {

  private static final String JOBS_FAILED = "Jobs-Failed";
  private static final String JOBS_STOPPED = "Jobs-Stopped";
  private static final String JOBS_SUCCESSFUL = "Jobs-Successful";
  private static final String JOBS_IN_PROGRESS = "Jobs-In-Progress";
  private static final String JOBS_PENDING = "Jobs-Pending";

  @Autowired private EvaluationStatusRepository evaluationStatusRepository;

  @Override
  /**
   * Creates or retrieves an existing EvaluationStatus for the given evaluatorId, chatTurnId,
   * userId, and projectId. If an existing EvaluationStatus is found, it is returned; otherwise, a
   * new one is created with status PENDING.
   */
  public EvaluationStatus createEvaluationStatus(
      String evaluatorId, String chatTurnId, String userId, String projectId) {
    log.info(
        "Create evaluation status request :: evaluatorId={}, chatTurnId={}, userId={}, projectId={}",
        evaluatorId,
        chatTurnId,
        userId,
        projectId);

    EvaluationStatus evaluationStatus =
        evaluationStatusRepository.findByEvaluatorIdAndChatTurnIdAndUserId(
            evaluatorId, chatTurnId, userId);

    if (evaluationStatus == null) {
      evaluationStatus = new EvaluationStatus();
    }
    evaluationStatus.setEvaluatorId(evaluatorId);
    evaluationStatus.setChatTurnId(chatTurnId);
    evaluationStatus.setUserId(userId);
    evaluationStatus.setStatus(EvaluationStatusEnum.PENDING.getKey());
    evaluationStatus.setContainerId(projectId);
    log.info("Creating new evaluation status response :: {} ", evaluationStatus);
    return evaluationStatusRepository.saveAndFlush(evaluationStatus);
  }

  @Override
  public void updateEvaluationStatus(
      EvaluationDTO EvaluationDTO, EvaluationStatusEnum evaluatorStatusEnum, String comment) {
    log.info(
        "Update evaluation status request :: EvaluationDTO={}, statusEnum={}, comment={}",
        EvaluationDTO,
        evaluatorStatusEnum,
        comment);
    evaluationStatusRepository.updateStatusByEvaluatorIdAndChatTurnIdAndJobIdAndUserId(
        evaluatorStatusEnum.getKey(),
        EvaluationDTO.getEvaluatorId(),
        EvaluationDTO.getChatTurnId(),
        EvaluationDTO.getJobId(),
        EvaluationDTO.getUserId(),
        comment,
        EvaluationDTO.getReason());
    log.info("Evaluation status updated successfully");
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateEvaluationStatus(
      EvaluationStatus evaluationStatus, EvaluationStatusEnum status, String comment) {
    log.info(
        "Update evaluation status request :: EvaluationStatus={}, statusEnum={}, comment={}",
        evaluationStatus,
        status,
        comment);
    evaluationStatus.setStatus(status.getKey());
    evaluationStatus.setComments(comment);
    evaluationStatusRepository.saveAndFlush(evaluationStatus);
    log.info("Evaluation status updated successfully");
  }

  @Override
  public int updateEvaluationStatus(
      EvaluationStatusEnum newEvaluationStatus,
      String jobId,
      String comment,
      EvaluationStatusEnum oldEvaluationStatus) {

    return evaluationStatusRepository.updateStatusByJobIdAndUserId(
        newEvaluationStatus.getKey(), jobId, comment, oldEvaluationStatus.getKey());
  }

  @Override
  public Map<String, Integer> findAllByUserAndJobId(String jobId, String userId) {
    log.info("Get evaluation status by userId {} and jobId :: {} ", userId, jobId);
    Map<String, Integer> responseMatrics = new HashMap<>();
    List<EvaluationStatus> evaluationStatuses =
        evaluationStatusRepository.findAllByJobIdAndUserId(jobId, userId);
    if (!CollectionUtils.isEmpty(evaluationStatuses)) {
      Map<Integer, List<EvaluationStatus>> groupedResults =
          evaluationStatuses.stream().collect(Collectors.groupingBy(EvaluationStatus::getStatus));

      List<EvaluationStatus> inProgressMetrics =
          groupedResults.get(EvaluationStatusEnum.IN_PROGRESS.getKey());
      int inProgress = !CollectionUtils.isEmpty(inProgressMetrics) ? inProgressMetrics.size() : 0;
      responseMatrics.put(JOBS_IN_PROGRESS, inProgress);

      List<EvaluationStatus> pendingMetrics =
          groupedResults.get(EvaluationStatusEnum.PENDING.getKey());
      int pending = !CollectionUtils.isEmpty(pendingMetrics) ? pendingMetrics.size() : 0;
      responseMatrics.put(JOBS_PENDING, pending);

      List<EvaluationStatus> successfulMetrics =
          groupedResults.get(EvaluationStatusEnum.SUCCESSFUL.getKey());
      responseMatrics.put(
          JOBS_SUCCESSFUL,
          !CollectionUtils.isEmpty(successfulMetrics) ? successfulMetrics.size() : 0);

      List<EvaluationStatus> failedMetrics =
          groupedResults.get(EvaluationStatusEnum.FAILED.getKey());
      responseMatrics.put(
          JOBS_FAILED, !CollectionUtils.isEmpty(failedMetrics) ? failedMetrics.size() : 0);

      List<EvaluationStatus> stoppedMetrics =
          groupedResults.get(EvaluationStatusEnum.STOPPED.getKey());
      responseMatrics.put(
          JOBS_STOPPED, !CollectionUtils.isEmpty(stoppedMetrics) ? stoppedMetrics.size() : 0);

      return responseMatrics;
    }
    log.info("No Evaluation Status found for jobId: {} and userId: {}", jobId, userId);
    return responseMatrics; // Return empty map if no records found, or throw exception if needed
  }

  @Override
  public EvaluationStatus findById(String evaluationStatusId) {
    return evaluationStatusRepository
        .findById(evaluationStatusId)
        .orElseThrow(() -> new NotFoundException("EvaluationStatus not found"));
  }

  @Transactional
  @Override
  public void stopEvaluation(String evaluationStatusId, String projectId, User user) {

    EvaluationStatus evaluationStatus =
        evaluationStatusRepository
            .findById(evaluationStatusId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "EvaluationStatus not found for id: " + evaluationStatusId));

    if (!evaluationStatus.getUserId().equals(user.getId())) {
      throw new NotFoundException("EvaluationStatus not found.");
    }

    EvaluationStatusEnum currentStatus =
        EvaluationStatusEnum.getEvaluationStatusEnumByKey(evaluationStatus.getStatus());
    Set<EvaluationStatusEnum> finalStatuses =
        Set.of(
            EvaluationStatusEnum.SUCCESSFUL,
            EvaluationStatusEnum.FAILED,
            EvaluationStatusEnum.STOPPED);

    if (finalStatuses.contains(currentStatus)) {
      throw new IllegalStateException(
          "Evaluation task is already in a final state: " + currentStatus.getValue());
    }

    evaluationStatus.setStatus(EvaluationStatusEnum.STOPPED.getKey());
    evaluationStatus.setComments("Evaluation has been stopped manually by user.");
    evaluationStatusRepository.save(evaluationStatus);
    log.info("EvaluationStatus with id: {} has been stopped.", evaluationStatusId);
  }
}
