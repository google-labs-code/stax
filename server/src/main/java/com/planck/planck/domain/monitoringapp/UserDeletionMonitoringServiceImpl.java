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

package com.planck.planck.domain.monitoringapp;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.planck.planck.domain.monitoringapp.notifications.UserDeletionNotificationService;
import com.planck.planck.domain.pubsub.UserDeletionJobQueueService;
import com.planck.planck.domain.user.UserDeletionStatusRepository;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.domain.user.dto.UserDeletionDTO;
import com.planck.planck.entitities.User;
import com.planck.planck.entitities.UserDeletionStatus;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.QueueConstants;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("monitoring")
public class UserDeletionMonitoringServiceImpl implements UserDeletionMonitoringService {

  private static final Logger logger =
      LoggerFactory.getLogger(UserDeletionMonitoringServiceImpl.class);

  private final UserDeletionStatusRepository userDeletionStatusRepository;
  private final UserRepository userRepository;
  private final UserDeletionNotificationService notificationService;
  private final PubSubTemplate pubSubTemplate;
  private final UserDeletionJobQueueService userDeletionJobQueueService;

  public UserDeletionMonitoringServiceImpl(
      UserDeletionStatusRepository userDeletionStatusRepository,
      UserRepository userRepository,
      UserDeletionNotificationService notificationService,
      PubSubTemplate pubSubTemplate,
      UserDeletionJobQueueService userDeletionJobQueueService) {
    this.userDeletionStatusRepository = userDeletionStatusRepository;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
    this.pubSubTemplate = pubSubTemplate;
    this.userDeletionJobQueueService = userDeletionJobQueueService;
  }

  @Override
  public void checkForStaleUserDeletions() {
    logger.info("Checking for stale user deletions...");

    Date threshold = Date.valueOf(LocalDate.now().minus(7, ChronoUnit.DAYS));

    List<UserDeletionStatus> staleUserDeletions =
        userDeletionStatusRepository.findStaleUserDeletions(threshold);

    logger.info("Found {} stale user deletions", staleUserDeletions.size());

    sendStaleUserDeletionNotification(staleUserDeletions);

    logger.info("Stale user deletion check completed");
  }

  @Override
  public void retryStaleUserDeletions() {
    logger.info("Retrying stale user deletions...");

    List<UserDeletionStatus> staleUserDeletions =
        userDeletionStatusRepository.findStaleUserDeletions(
            Date.valueOf(LocalDate.now().minus(7, ChronoUnit.DAYS)));

    for (UserDeletionStatus staleUserDeletion : staleUserDeletions) {
      try {
        User user = userRepository.findById(staleUserDeletion.getUserId()).orElse(null);

        if (user == null) {
          logger.warn("User with id {} was not found", staleUserDeletion.getUserId());
          continue;
        }

        publishUserDeletionDirectly(user, staleUserDeletion);
      } catch (Exception e) {
        logger.error(
            "Failed to retry user deletion for user ID: {}", staleUserDeletion.getUserId(), e);
      }
    }
  }

  private void sendStaleUserDeletionNotification(List<UserDeletionStatus> staleUserDeletions) {
    logger.info(
        "Sending stale user deletion notification for {} user deletions",
        staleUserDeletions.size());
    notificationService.sendStaleUserDeletionNotification(staleUserDeletions);
  }

  private void publishUserDeletionDirectly(User user, UserDeletionStatus userDeletionStatus) {
    try {
      UserDeletionDTO userDeletionDTO = userDeletionJobQueueService.createUserDeletionMessage(user);

      sendUserDeletionMessageDirectly(userDeletionDTO);
    } catch (Exception e) {
      logger.error(
          "Failed to publish user deletion message directly to PubSub for user: {}",
          user.getId(),
          e);
      throw new RuntimeException("Failed to publish user deletion message", e);
    }
  }

  private void sendUserDeletionMessageDirectly(UserDeletionDTO userDeletionDTO) {
    String userDeletionDTOJson = ObjectMapperUtil.convertObjectToJsonString(userDeletionDTO);

    // Publish directly to PubSub topic
    pubSubTemplate.publish(QueueConstants.USER_DELETION_TOPIC, userDeletionDTOJson);

    logger.info(
        "User deletion message published directly to PubSub for user: {}, requestId: {}",
        userDeletionDTO.getUserId(),
        userDeletionDTO.getRequestId());
  }
}
