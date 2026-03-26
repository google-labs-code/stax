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

package com.planck.planck.domain.pubsub;

import com.planck.planck.domain.user.UserDataDeletionRepository;
import com.planck.planck.domain.user.UserDeletionStatusRepository;
import com.planck.planck.domain.user.dto.UserDeletionDTO;
import com.planck.planck.entitities.User;
import com.planck.planck.entitities.UserDeletionStatus;
import com.planck.planck.util.ObjectMapperUtil;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("userJobQueueService")
public class UserDeletionJobQueueService {

  private static final int MAX_STEP_RETRIES = 10;

  private final UserDataDeletionRepository userDataDeletionRepository;
  private final PubsubOutboundUserDeletionGateway messagingUserDeletionGateway;
  private final UserDeletionStatusRepository userDeletionStatusRepository;

  public UserDeletionJobQueueService(
      UserDataDeletionRepository userDataDeletionRepository,
      PubsubOutboundUserDeletionGateway messagingUserDeletionGateway,
      UserDeletionStatusRepository userDeletionStatusRepository) {
    this.userDataDeletionRepository = userDataDeletionRepository;
    this.messagingUserDeletionGateway = messagingUserDeletionGateway;
    this.userDeletionStatusRepository = userDeletionStatusRepository;
  }

  @MessagingGateway(name = "pubsubOutboundUserDeletionGateway")
  public interface PubsubOutboundUserDeletionGateway {

    @Gateway(requestChannel = "userDeletionChannel")
    void sendToUserDeletion(String userDeletionDTOJson);
  }

  public void publishUserDeletion(User user) {
    try {
      UserDeletionDTO userDeletionDTO = createUserDeletionMessage(user);
      sendUserDeletionMessage(userDeletionDTO);
    } catch (Exception e) {
      log.error("Failed to publish user deletion message for user: {}", user.getId(), e);
      throw new RuntimeException("Failed to publish user deletion message", e);
    }
  }

  public UserDeletionDTO createUserDeletionMessage(User user) {
    List<UserDeletionDTO.DeletionStep> deletionSteps = createDeletionSteps();

    UserDeletionStatus userDeletionStatus = getUserDeletionStatus(user.getId());
    userDeletionStatus.setCompletedSteps(0);
    userDeletionStatus.setTotalSteps(deletionSteps.size());
    userDeletionStatus.setLastStepRetries(0);
    userDeletionStatusRepository.save(userDeletionStatus);

    return UserDeletionDTO.builder()
        .requestId(userDeletionStatus.getRequestId())
        .userId(user.getId())
        .deletionSteps(deletionSteps)
        .build();
  }

  private void sendUserDeletionMessage(UserDeletionDTO userDeletionDTO) {
    String userDeletionDTOJson = ObjectMapperUtil.convertObjectToJsonString(userDeletionDTO);
    messagingUserDeletionGateway.sendToUserDeletion(userDeletionDTOJson);

    log.info(
        "User deletion message published for user: {}, requestId: {}",
        userDeletionDTO.getUserId(),
        userDeletionDTO.getRequestId());
  }

  public List<UserDeletionDTO.DeletionStep> createDeletionSteps() {
    try {
      Resource resource = new ClassPathResource("entity_deletion_order.json");
      InputStream inputStream = resource.getInputStream();

      String jsonContent = new String(inputStream.readAllBytes());

      List<UserDeletionDTO.DeletionStep> steps =
          ObjectMapperUtil.convertJsonStringToList(jsonContent, UserDeletionDTO.DeletionStep.class);

      log.info("Loaded {} deletion steps from entity_deletion_order.json", steps.size());
      return steps;

    } catch (Exception e) {
      log.error("Failed to parse entity_deletion_order.json", e);
      throw new RuntimeException("Failed to parse entity_deletion_order.json", e);
    }
  }

  public void processUserDeletion(UserDeletionDTO userDeletionDTO) {
    UserDeletionStatus userDeletionStatus = getUserDeletionStatus(userDeletionDTO.getUserId());

    UserDeletionDTO.DeletionStep currentStep = getCurrentStep(userDeletionDTO);

    if (currentStep == null) {
      log.info(
          "All deletion steps completed for user: {}, requestId: {}",
          userDeletionDTO.getUserId(),
          userDeletionDTO.getRequestId());
      return;
    }

    try {
      processDeletionStep(userDeletionDTO, currentStep, userDeletionStatus);
      updateDeletionStatusOnSuccess(userDeletionStatus, userDeletionDTO);
      publishNextDeletionMessage(userDeletionDTO);
    } catch (Exception e) {
      String currentTableName = currentStep != null ? currentStep.getTableName() : "unknown";
      log.error(
          "Failed to process user deletion for user: {}, requestId: {}, table: {}",
          userDeletionDTO.getUserId(),
          userDeletionDTO.getRequestId(),
          currentTableName,
          e);

      int currentRetryCount =
          updateDeletionStatusOnStepFailure(
              userDeletionStatus, userDeletionDTO, currentTableName, e);

      if (currentRetryCount >= MAX_STEP_RETRIES) {
        log.error(
            "Maximum retries ({}) reached for user deletion. Stopping processing for user: {}, requestId: {}",
            MAX_STEP_RETRIES,
            userDeletionDTO.getUserId(),
            userDeletionDTO.getRequestId());

        handleMaxRetriesReached(userDeletionStatus, userDeletionDTO);
        return;
      }

      throw new RuntimeException("Failed to process user deletion", e);
    }
  }

  private void handleMaxRetriesReached(
      UserDeletionStatus userDeletionStatus, UserDeletionDTO userDeletionDTO) {
    // In the future, we might want to add extra handling such as:
    // - Sending notifications to administrators
    // - Logging to external monitoring systems
    // - Creating alerts or tickets

    log.error(
        "User deletion processing stopped due to maximum retries reached for user: {}, requestId: {}. "
            + "Manual intervention may be required.",
        userDeletionDTO.getUserId(),
        userDeletionDTO.getRequestId());
  }

  private void processDeletionStep(
      UserDeletionDTO userDeletionDTO,
      UserDeletionDTO.DeletionStep currentStep,
      UserDeletionStatus userDeletionStatus) {

    int stepId = userDeletionStatus.getCompletedSteps() + 1;
    log.info(
        "Processing deletion step: {} (stepId: {}) for user: {}, requestId: {}",
        currentStep.getTableName(),
        stepId,
        userDeletionDTO.getUserId(),
        userDeletionDTO.getRequestId());

    Long count = getRecordCountForTable(currentStep, userDeletionDTO.getUserId());

    if (count > 0) {
      deleteRecordsInBatches(currentStep, userDeletionDTO.getUserId(), count);
    }
  }

  private Long getRecordCountForTable(UserDeletionDTO.DeletionStep currentStep, String userId) {
    Long count = userDataDeletionRepository.countByTableAndUser(currentStep.getTableName(), userId);

    log.info(
        "Found {} records to delete for table {} and user: {}",
        count,
        currentStep.getTableName(),
        userId);

    return count;
  }

  private void deleteRecordsInBatches(
      UserDeletionDTO.DeletionStep currentStep, String userId, Long totalCount) {

    int batchSize = 1000;
    int totalBatches = (int) Math.ceil((double) totalCount / batchSize);

    log.info(
        "Will delete in {} batches for table {} and user: {}",
        totalBatches,
        currentStep.getTableName(),
        userId);

    int totalDeleted = 0;
    for (int batchNum = 1; batchNum <= totalBatches; batchNum++) {
      try {
        int recordsInThisBatch = Math.min(batchSize, (int) (totalCount - totalDeleted));

        Integer deletedInBatch =
            executeBatchDeletionInTransaction(
                currentStep.getTableName(), userId, recordsInThisBatch, batchNum, totalBatches);

        totalDeleted += deletedInBatch;
        log.debug(
            "Completed batch {}/{} for table {} and user: {}, deleted {} records",
            batchNum,
            totalBatches,
            currentStep.getTableName(),
            userId,
            deletedInBatch);

      } catch (Exception e) {
        log.error(
            "Failed to delete batch {}/{} for table {} and user: {}",
            batchNum,
            totalBatches,
            currentStep.getTableName(),
            userId,
            e);

        throw new RuntimeException(
            String.format(
                "Failed to delete batch %d/%d for table %s: %s",
                batchNum, totalBatches, currentStep.getTableName(), e.getMessage()),
            e);
      }
    }

    log.info(
        "Successfully deleted {} records from table {} for user: {}",
        totalDeleted,
        currentStep.getTableName(),
        userId);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Integer executeBatchDeletionInTransaction(
      String tableName, String userId, int batchSize, int batchNum, int totalBatches) {

    log.debug(
        "Executing batch {}/{} deletion for table {} and user {} in separate transaction",
        batchNum,
        totalBatches,
        tableName,
        userId);

    return userDataDeletionRepository.deleteByTableAndUserWithLimit(tableName, userId, batchSize);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateDeletionStatusOnSuccess(
      UserDeletionStatus userDeletionStatus, UserDeletionDTO userDeletionDTO) {

    userDeletionStatus.setCompletedSteps(userDeletionStatus.getCompletedSteps() + 1);
    userDeletionStatus.setLastStepRetries(0);
    userDeletionStatusRepository.save(userDeletionStatus);

    log.info(
        "Updated deletion status for user: {}, requestId: {}, completed steps: {}",
        userDeletionDTO.getUserId(),
        userDeletionDTO.getRequestId(),
        userDeletionStatus.getCompletedSteps());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int updateDeletionStatusOnStepFailure(
      UserDeletionStatus userDeletionStatus,
      UserDeletionDTO userDeletionDTO,
      String failedTableName,
      Exception e) {

    userDeletionStatus.setLastStepRetries(userDeletionStatus.getLastStepRetries() + 1);
    int currentRetryCount = userDeletionStatus.getLastStepRetries();

    userDeletionStatus.setLastFailureStep(failedTableName);
    userDeletionStatus.setLastFailureMessage(
        String.format("Retry %d/%d: %s", currentRetryCount, MAX_STEP_RETRIES, e.getMessage()));
    userDeletionStatusRepository.save(userDeletionStatus);

    return currentRetryCount;
  }

  private void publishNextDeletionMessage(UserDeletionDTO userDeletionDTO) {
    List<UserDeletionDTO.DeletionStep> remainingSteps =
        new ArrayList<>(userDeletionDTO.getDeletionSteps());
    if (!remainingSteps.isEmpty()) {
      remainingSteps.remove(0);
    }

    if (remainingSteps.isEmpty()) {
      log.info(
          "All deletion steps completed for user: {}, requestId: {}. No more messages to send.",
          userDeletionDTO.getUserId(),
          userDeletionDTO.getRequestId());
      return;
    }

    UserDeletionDTO nextDeletionDTO =
        UserDeletionDTO.builder()
            .requestId(userDeletionDTO.getRequestId())
            .userId(userDeletionDTO.getUserId())
            .deletionSteps(remainingSteps)
            .build();

    messagingUserDeletionGateway.sendToUserDeletion(
        ObjectMapperUtil.convertObjectToJsonString(nextDeletionDTO));

    log.info(
        "Published next deletion message for user: {}, requestId: {}, remaining steps: {}",
        userDeletionDTO.getUserId(),
        userDeletionDTO.getRequestId(),
        remainingSteps.size());
  }

  private UserDeletionStatus getUserDeletionStatus(String userId) {
    UserDeletionStatus userDeletionStatus = userDeletionStatusRepository.findByUserId(userId);

    if (userDeletionStatus == null) {
      log.info("Creating new UserDeletionStatus for user: {}", userId);
      userDeletionStatus = new UserDeletionStatus();
      userDeletionStatus.setUserId(userId);
      userDeletionStatus.setRequestTimestamp(new Timestamp(System.currentTimeMillis()));

      userDeletionStatusRepository.saveAndFlush(userDeletionStatus);
      log.info(
          "Created UserDeletionStatus with requestId: {} for user: {}",
          userDeletionStatus.getRequestId(),
          userId);
    }

    return userDeletionStatus;
  }

  private UserDeletionDTO.DeletionStep getCurrentStep(UserDeletionDTO userDeletionDTO) {
    if (userDeletionDTO.getDeletionSteps().isEmpty()) {
      return null;
    }

    return userDeletionDTO.getDeletionSteps().get(0);
  }
}
