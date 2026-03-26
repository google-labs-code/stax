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

package com.planck.planck.exceptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Input data failed specific validation rules defined within the application's business logic. */
public class ValidationException extends RuntimeException {

  private static final String DEFAULT_MESSAGE =
      "Validation Failed: Please correct the highlighted errors in your input.";

  private final List<Map<String, String>> errors;

  public ValidationException() {
    super(DEFAULT_MESSAGE);
    this.errors = Collections.emptyList();
  }

  public ValidationException(String message) {
    super(message);
    this.errors = Collections.emptyList();
  }

  public ValidationException(List<Map<String, String>> errors) {
    super("Validation failed");
    this.errors = errors;
  }

  public List<Map<String, String>> getErrors() {
    return errors;
  }
}
