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
import java.util.Collection;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validator to check that at least one out of a list of fields is provided. Useful when there's
 * multiple input fields listed as optional, however a minimum of one must be provided.
 *
 * <p>Checks for null and if the field is a collection, ensures it has size > 0. Other validators
 * provided by the framework already have logic to not run against null. For example @Size of an
 * array, or @NotEmpty for a string.
 */
public class AtLeastOneFieldValidator
    implements ConstraintValidator<AtLeastOneFieldRequired, Object> {

  private String[] fields;

  @Override
  public void initialize(AtLeastOneFieldRequired constraintAnnotation) {
    this.fields = constraintAnnotation.fields();
  }

  @Override
  public boolean isValid(Object obj, ConstraintValidatorContext context) {
    try {
      for (String field : fields) {
        Object value = new BeanWrapperImpl(obj).getPropertyValue(field);
        if (value != null) {
          if (value instanceof Collection) {
            if (!((Collection<?>) value).isEmpty()) {
              return true;
            }
          } else {
            return true;
          }
        }
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }
}
