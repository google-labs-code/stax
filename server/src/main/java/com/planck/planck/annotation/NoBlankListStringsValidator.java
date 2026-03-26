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
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoBlankListStringsValidator
    implements ConstraintValidator<NoBlankListStrings, List<String>> {

  @Override
  public void initialize(NoBlankListStrings constraintAnnotation) {}

  @Override
  public boolean isValid(List<String> value, ConstraintValidatorContext context) {
    log.info("Going to validate String Start {} ", value);
    if (value == null || value.isEmpty()) {
      return true; // Empty list is considered valid
    }
    for (String str : value) {
      if (str == null || str.trim().isEmpty()) {
        return false; // Found a blank string, validation fails
      }
    }
    log.info("Going to validate String End {} ", value);
    return true; // All strings are non-blank
  }
}
