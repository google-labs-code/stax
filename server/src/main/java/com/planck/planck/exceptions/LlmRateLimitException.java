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
 * Too many requests have been sent specifically to the Language Model (LLM) service in a short
 * period.
 */
public class LlmRateLimitException extends RuntimeException {

  private static final String DEFAULT_MESSAGE =
      "Provider Rate Limit: Too many requests sent to the model provider. Please wait a moment and"
          + " try again.";

  public LlmRateLimitException() {
    super(DEFAULT_MESSAGE);
  }

  public LlmRateLimitException(String message) {
    super(message);
  }
}
