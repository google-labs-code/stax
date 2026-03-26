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

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("monitoring")
@DependsOn({"pubsubOutboundUserDeletionGateway"})
public class MonitoringSchedulerServiceImpl implements MonitoringSchedulerService {

  private static final Logger logger =
      LoggerFactory.getLogger(MonitoringSchedulerServiceImpl.class);

  @Autowired private UserDeletionMonitoringService userDeletionMonitoringService;

  @Override
  @PostConstruct
  public void initialize() {
    logger.info("Running initial stale user deletion checks on startup...");
    runStaleUserDeletionCheck();
    runStaleUserDeletionRetry();
    logger.info("Initial stale user deletion checks completed");
  }

  @Override
  @Scheduled(cron = "${monitoring.schedule.cron:0 0 9,21 * * ?}")
  public void scheduledMonitoring() {
    logger.info("Starting scheduled monitoring tasks...");

    runStaleUserDeletionCheck();
    runStaleUserDeletionRetry();

    logger.info("Scheduled monitoring tasks completed");
  }

  private void runStaleUserDeletionCheck() {
    try {
      logger.info("Running stale user deletion check...");
      userDeletionMonitoringService.checkForStaleUserDeletions();
      logger.info("Stale user deletion check completed successfully");
    } catch (Exception e) {
      logger.error("Error during stale user deletion check", e);
    }
  }

  private void runStaleUserDeletionRetry() {
    try {
      logger.info("Running stale user deletion retry...");
      userDeletionMonitoringService.retryStaleUserDeletions();
      logger.info("Stale user deletion retry completed successfully");
    } catch (Exception e) {
      logger.error("Error during stale user deletion retry", e);
    }
  }
}
