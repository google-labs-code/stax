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

/**
 * The user has exceeded their allocated usage quota for a service (e.g., number of API calls, LLM
 * tokens).
 */
public class UserQuotaExceededException extends RuntimeException {

  private static final String DEFAULT_MESSAGE =
      "Quota Exceeded: You've used up your allowed usage for this model. Update quote with model"
          + " provider.";

  public UserQuotaExceededException() {
    super(DEFAULT_MESSAGE);
  }

  public UserQuotaExceededException(String message) {
    super(message);
  }
}
