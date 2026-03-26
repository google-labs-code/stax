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

package com.planck.planck.domain.user;

import com.planck.planck.domain.apikeys.service.ApiKeysService;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.pubsub.UserDeletionJobQueueService;
import com.planck.planck.entitities.User;
import com.planck.planck.entitities.UserDeletionStatus;
import com.planck.planck.enums.UserStatus;
import com.planck.planck.exceptions.ResourceDeletionException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionServiceImpl implements UserDeletionService {

  private final ApiKeysService apiKeysService;
  private final JobStatusService jobStatusService;
  private final UserRepository userRepository;
  private final UserDeletionJobQueueService userJobQueueService;
  private final UserDeletionStatusRepository userDeletionStatusRepository;

  @Transactional
  @Override
  public void deleteUserAndAllData(User user) {
    UserDeletionStatus userDeletionStatus = null;

    try {
      log.info("Deleting user {} and all related data", user.getId());

      // Create user deletion status first
      userDeletionStatus = new UserDeletionStatus();
      userDeletionStatus.setUserId(user.getId());
      userDeletionStatus.setRequestTimestamp(new Timestamp(System.currentTimeMillis()));
      userDeletionStatusRepository.saveAndFlush(userDeletionStatus);

      updateUserStatusToPendingDeletion(user);
      apiKeysService.deleteByUser(user);
      jobStatusService.stopAllJobsByUser(user);

      userJobQueueService.publishUserDeletion(user);
      log.info("User {} scheduled for deletion", user.getId());

    } catch (Exception e) {
      log.error("Error deleting user {}: {}", user.getId(), e.getMessage(), e);

      if (userDeletionStatus != null) {
        updateFailureStatus(userDeletionStatus, "INITIALIZATION", e.getMessage());
      }

      throw new ResourceDeletionException(
          User.class, user.getId(), "Failed to trigger user deletion", e);
    }
  }

  @Override
  public void publishUserDeletion(User user) {
    userJobQueueService.publishUserDeletion(user);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateFailureStatus(
      UserDeletionStatus userDeletionStatus, String failureStep, String failureMessage) {
    try {
      userDeletionStatus.setLastFailureStep(failureStep);
      userDeletionStatus.setLastFailureMessage(failureMessage);
      userDeletionStatusRepository.save(userDeletionStatus);

      log.error(
          "Updated failure status for user: {}, step: {}, error: {}",
          userDeletionStatus.getUserId(),
          failureStep,
          failureMessage);
    } catch (Exception saveException) {
      log.error(
          "Failed to update failure status for user: {}",
          userDeletionStatus.getUserId(),
          saveException);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateUserStatusToPendingDeletion(User user) {
    log.info("Updating user {} status to PENDING_DELETION", user.getId());
    user.setStatus(UserStatus.PENDING_DELETION);
    userRepository.saveAndFlush(user);
  }

  @Override
  public Integer getStaleUserDeletionCount() {
    return getStaleUserDeletions().size();
  }

  private List<UserDeletionStatus> getStaleUserDeletions() {
    return userDeletionStatusRepository.findStaleUserDeletions(
        Date.valueOf(LocalDate.now().minus(7, ChronoUnit.DAYS)));
  }

  @Override
  @Transactional
  public void retryStaleUserDeletions() {
    List<UserDeletionStatus> staleDeletions = getStaleUserDeletions();
    for (UserDeletionStatus status : staleDeletions) {
      try {
        log.info("Retrying deletion for user: {}", status.getUserId());
        User user = userRepository.findById(status.getUserId()).orElse(null);
        if (user == null) {
          log.warn("User with id {} was not found", status.getUserId());
          continue;
        }
        userJobQueueService.publishUserDeletion(user);
      } catch (Exception e) {
        log.error("Failed to retry deletion for user: {}", status.getUserId(), e);
      }
    }
  }
}
