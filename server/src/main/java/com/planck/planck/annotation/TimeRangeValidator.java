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

package com.planck.planck.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, Object> {

  @Override
  public void initialize(ValidTimeRange constraintAnnotation) {}

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext context) {
    log.info("Validating time range...");

    if (object == null) {
      return false;
    }

    try {
      Instant startTime = (Instant) object.getClass().getMethod("getStartTime").invoke(object);
      Instant endTime = (Instant) object.getClass().getMethod("getEndTime").invoke(object);

      if (startTime == null) {
        return false;
      }

      if (endTime == null) {
        endTime = Instant.now();
      }

      log.info("Start: {}, End: {}", startTime, endTime);
      return startTime.isBefore(endTime);
    } catch (Exception e) {
      log.error("Time validation error", e);
      return false;
    }
  }
}
