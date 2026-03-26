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
 * Access to the requested Language Model (LLM) resource or action is denied due to insufficient
 * permissions or policy violations.
 */
public class LlmForbiddenException extends RuntimeException {

  private static final String DEFAULT_MESSAGE =
      "Access Denied: You don't have permission. Check access permissions with the model provider.";

  public LlmForbiddenException() {
    super(DEFAULT_MESSAGE);
  }

  public LlmForbiddenException(String message) {
    super(message);
  }
}
