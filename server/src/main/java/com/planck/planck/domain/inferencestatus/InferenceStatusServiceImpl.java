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

package com.planck.planck.domain.inferencestatus;

import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service("inferenceStatusService")
@Slf4j
public class InferenceStatusServiceImpl implements InferenceStatusService {

  private static final String INFERENCES_FAILED = "Jobs-Failed";
  private static final String INFERENCES_STOPPED = "Jobs-Stopped";
  private static final String INFERENCES_SUCCESSFUL = "Jobs-Successful";
  private static final String INFERENCES_IN_PROGRESS = "Jobs-In-Progress";
  private static final String INFERENCES_PENDING = "Jobs-Pending";

  @Autowired private InferenceStatusRepository inferenceStatusRepository;

  @Autowired private ChatTurnRepository chatTurnRepository;

  @Override
  public InferenceStatus createOrUpdateInferenceStatus(
      InferenceDTO inferenceDTO, InferenceStatusEnum inferenceStatusEnum) {
    log.info("Create inference status request :: {} ", inferenceDTO);
    Optional<InferenceStatus> existingInferenceStatus =
        inferenceStatusRepository.getByChatTurnId(
            inferenceDTO.getChatTurnId(), inferenceDTO.getUserId());

    InferenceStatus inferenceStatus;
    if (existingInferenceStatus.isPresent()) {
      log.info("Updating existing inference status");
      inferenceStatus = existingInferenceStatus.get();
      inferenceStatus.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    } else {
      log.info("Using new inference status");
      inferenceStatus = new InferenceStatus();
      Optional<ChatTurn> chatTurn = chatTurnRepository.findById(inferenceDTO.getChatTurnId());
      if (!chatTurn.isPresent()) {
        throw new IllegalInputException("Chat turn not found");
      }
      inferenceStatus.setChatTurn(chatTurn.get());
      inferenceStatus.setUserId(inferenceDTO.getUserId());
      inferenceStatus.setContainerId(inferenceDTO.getProjectId());
    }

    inferenceStatus.setStatus(inferenceStatusEnum.getKey());
    inferenceStatus.setJobId(inferenceDTO.getJobId());
    inferenceStatus.setComments(inferenceDTO.getComments());
    if (inferenceDTO.getReason() != null && !inferenceDTO.getReason().isEmpty()) {
      inferenceStatus.setReason(getTrimmedReason(inferenceDTO.getReason()));
    }
    log.info("Create inference status response :: {} ", inferenceStatus);
    return inferenceStatusRepository.save(inferenceStatus);
  }

  private String getTrimmedReason(String reason) {
    if (reason == null) {
      return null;
    }
    return reason.length() > 3999 ? reason.substring(0, 3999) : reason;
  }

  @Override
  public void updateInferenceStatus(
      InferenceDTO inferenceDTO, InferenceStatusEnum inferenceStatusEnum, String comment) {
    log.info(
        "Update inference status request :: InferenceDTO={}, statusEnum={}, comment={}",
        inferenceDTO,
        inferenceStatusEnum,
        comment);
    inferenceStatusRepository.updateStatusByChatTurnIdAndJobIdAndUserId(
        inferenceStatusEnum.getKey(),
        inferenceDTO.getChatTurnId(),
        inferenceDTO.getJobId(),
        inferenceDTO.getUserId(),
        comment,
        getTrimmedReason(inferenceDTO.getReason()));
    log.info("Inference status updated successfully");
  }

  @Override
  public int updateInferenceStatus(
      InferenceStatusEnum newInferenceStatus,
      String jobId,
      String comment,
      InferenceStatusEnum oldInferenceStatus) {

    return inferenceStatusRepository.updateStatusByJobIdAndUserId(
        newInferenceStatus.getKey(), jobId, comment, oldInferenceStatus.getKey());
  }

  // public Map<String, Integer> findAllByUserAndJobId(String jobId, String userId) {
  //   log.info("Get inference status by userId {} and jobId :: {} ", userId, jobId);
  //   Map<String, Integer> responseMetrics = new HashMap<>();
  //   List<InferenceStatus> inferenceStatuses =
  //       inferenceStatusRepository.findAllByJobIdAndUserId(jobId, userId);
  //   if (!CollectionUtils.isEmpty(inferenceStatuses)) {
  //     Map<Integer, List<InferenceStatus>> groupedResults =
  //         inferenceStatuses.stream().collect(Collectors.groupingBy(InferenceStatus::getStatus));

  //     List<InferenceStatus> metrics;
  //     int inProgress;
  //     int pending; // Assuming PENDING maps to IN_PROGRESS for inferences

  //     metrics = groupedResults.get(InferenceStatusEnum.IN_PROGRESS.getKey());
  //     inProgress = !CollectionUtils.isEmpty(metrics) ? metrics.size() : 0;
  //     metrics = groupedResults.get(InferenceStatusEnum.PENDING.getKey());
  //     pending = !CollectionUtils.isEmpty(metrics) ? metrics.size() : 0;
  //     responseMetrics.put(INFERENCES_IN_PROGRESS, (inProgress + pending));

  //     metrics = groupedResults.get(InferenceStatusEnum.SUCCESSFULL.getKey());
  //     responseMetrics.put(
  //         INFERENCES_SUCCESSFULL, !CollectionUtils.isEmpty(metrics) ? metrics.size() : 0);

  //     metrics = groupedResults.get(InferenceStatusEnum.FAILED.getKey());
  //     responseMetrics.put(
  //         INFERENCES_FAILED, !CollectionUtils.isEmpty(metrics) ? metrics.size() : 0);

  //     metrics = groupedResults.get(InferenceStatusEnum.IN_PROGRESS.getKey());
  //     responseMetrics.put(
  //         INFERENCES_IN_PROGRESS, !CollectionUtils.isEmpty(metrics) ? metrics.size() : 0);

  //     return responseMetrics;
  //   }

  //   log.info("No Inference Status found for jobId: {} and userId: {}", jobId, userId);
  //   return responseMetrics; // Return empty map if no records found
  // }

  @Override
  public Map<String, Integer> findAllByUserAndJobId(String jobId, String userId) {
    log.info("Get inference status by userId {} and jobId :: {} ", userId, jobId);
    Map<String, Integer> responseMetrics = new HashMap<>();
    List<InferenceStatus> inferenceStatuses =
        inferenceStatusRepository.findAllByJobIdAndUserId(jobId, userId);
    if (!CollectionUtils.isEmpty(inferenceStatuses)) {
      Map<Integer, List<InferenceStatus>> groupedResults =
          inferenceStatuses.stream().collect(Collectors.groupingBy(InferenceStatus::getStatus));

      List<InferenceStatus> inProgressMetrics =
          groupedResults.get(InferenceStatusEnum.IN_PROGRESS.getKey());
      int inProgress = !CollectionUtils.isEmpty(inProgressMetrics) ? inProgressMetrics.size() : 0;
      responseMetrics.put(INFERENCES_IN_PROGRESS, inProgress);

      List<InferenceStatus> pendingMetrics =
          groupedResults.get(InferenceStatusEnum.PENDING.getKey());
      int pending = !CollectionUtils.isEmpty(pendingMetrics) ? pendingMetrics.size() : 0;
      responseMetrics.put(INFERENCES_PENDING, pending);

      List<InferenceStatus> successfulMetrics =
          groupedResults.get(InferenceStatusEnum.SUCCESSFUL.getKey());
      responseMetrics.put(
          INFERENCES_SUCCESSFUL,
          !CollectionUtils.isEmpty(successfulMetrics) ? successfulMetrics.size() : 0);

      List<InferenceStatus> failedMetrics = groupedResults.get(InferenceStatusEnum.FAILED.getKey());
      responseMetrics.put(
          INFERENCES_FAILED, !CollectionUtils.isEmpty(failedMetrics) ? failedMetrics.size() : 0);

      List<InferenceStatus> stoppedMetrics =
          groupedResults.get(InferenceStatusEnum.STOPPED.getKey());
      responseMetrics.put(
          INFERENCES_STOPPED, !CollectionUtils.isEmpty(stoppedMetrics) ? stoppedMetrics.size() : 0);

      return responseMetrics;
    }
    log.info("No Inference Status found for jobId: {} and userId: {}", jobId, userId);
    return responseMetrics;
  }

  @Override
  public InferenceStatus findByChatTurnIdAndUser(String chatTurnId, String userId) {
    return inferenceStatusRepository
        .getByChatTurnId(chatTurnId, userId)
        .orElseThrow(
            () -> new NotFoundException("InferenceStatus not found for chatTurnId: " + chatTurnId));
  }

  @Transactional
  @Override
  public void stopInferenceStatusByChatTurnIdAndProjectId(
      String chatTurnId, String containerId, User user) {

    InferenceStatus inferenceStatus =
        inferenceStatusRepository
            .getByChatTurnId(chatTurnId, user.getId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "InferenceStatus not found for chatTurnId: " + chatTurnId));

    if (!inferenceStatus.getContainerId().equals(containerId)) {
      throw new NotFoundException("InferenceStatus not found");
    }

    InferenceStatusEnum currentStatusEnum =
        InferenceStatusEnum.getInferenceStatusEnumByKey(inferenceStatus.getStatus());
    Set<InferenceStatusEnum> finalStatuses =
        Set.of(
            InferenceStatusEnum.SUCCESSFUL,
            InferenceStatusEnum.FAILED,
            InferenceStatusEnum.STOPPED);

    if (finalStatuses.contains(currentStatusEnum)) {
      throw new IllegalStateException("Inference task is already in a final state");
    }

    inferenceStatus.setStatus(InferenceStatusEnum.STOPPED.getKey());
    inferenceStatus.setComments("Inference has been stopped manually");
    inferenceStatusRepository.save(inferenceStatus);
  }
}
