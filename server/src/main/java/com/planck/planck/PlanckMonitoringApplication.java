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

package com.planck.planck;

import com.planck.planck.config.MonitoringScheduleConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Profile("monitoring")
public class PlanckMonitoringApplication {

  private static final Logger logger = LoggerFactory.getLogger(PlanckMonitoringApplication.class);

  @Autowired private MonitoringScheduleConfig scheduleConfig;

  @PostConstruct
  public void initialize() {
    logger.info("PlanckMonitoringApplication is initializing...");
    logger.info(
        "Monitoring schedule enabled: {}, cron: {}",
        scheduleConfig.isEnabled(),
        scheduleConfig.getCron());
    logger.info("MonitoringSchedulerService will handle startup and scheduled tasks");
  }

  public static void main(String[] args) {
    System.setProperty("spring.liquibase.enabled", "false");
    SpringApplication app = new SpringApplication(PlanckMonitoringApplication.class);
    app.setAdditionalProfiles("monitoring");
    app.run(args);
  }
}
