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

package com.planck.planck.aspect;

import com.planck.planck.annotation.CheckJobActive;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.job.dto.JobIdentifiable;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.enums.JobStatusEnum;
import com.planck.planck.exceptions.JobStatusStoppedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aspect for checking the active status of a job before proceeding with annotated method execution.
 *
 * <p>This aspect intercepts methods annotated with {@link CheckJobActive}. It checks the current
 * status of a job identified by a {@link JobIdentifiable} argument.
 *
 * <p>**Important Note on Pub/Sub Message Handling:** If the intercepted method is part of a Pub/Sub
 * message processing flow where the message has already been acknowledged (e.g., via {@code
 * originalMessage.ack()}) *before* this aspect throws {@link JobStatusStoppedException}, the
 * Pub/Sub message will be considered processed and removed from the subscription. In such cases,
 * the message will NOT be redelivered or routed to a Dead-Letter Topic, despite the processing
 * failure within the application.
 */
@Aspect
@Component
@Slf4j
public class JobStatusCheckAspect {

  @Autowired private JobStatusService jobStatusService;

  @Around("@annotation(com.planck.planck.annotation.CheckJobActive)")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    Object[] args = joinPoint.getArgs();

    for (Object arg : args) {
      if (arg instanceof JobIdentifiable) {
        String jobId = ((JobIdentifiable) arg).getJobId();

        JobStatus jobStatus = jobStatusService.findById(jobId);
        if (jobStatus == null) {
          log.info("Job {} not found, skipping processing.", jobId);
          throw new JobStatusStoppedException("Job not found: " + jobId);
        }

        JobStatusEnum currentStatusEnum =
            JobStatusEnum.getJobStatusEnumByKey(jobStatus.getStatus());
        if (currentStatusEnum == JobStatusEnum.STOPPED) {
          log.info("Job {} is stopped, skipping processing.", jobId);
          throw new JobStatusStoppedException("Job is stopped: " + jobId);
        }
      }
    }

    return joinPoint.proceed();
  }
}
