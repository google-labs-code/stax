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

/** Exception thrown when an API key is missing and cannot be used for inference. */
public class ApiKeyIsMissingException extends RuntimeException {

  public ApiKeyIsMissingException(String message) {
    super(message);
  }

  public ApiKeyIsMissingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ApiKeyIsMissingException() {
    super("Api Key is Missing");
  }
}
