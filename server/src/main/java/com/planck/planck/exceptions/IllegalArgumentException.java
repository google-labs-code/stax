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
 * An invalid value was provided for an expected input parameter, often caught during internal
 * processing.
 */
public class IllegalArgumentException extends RuntimeException {

  public IllegalArgumentException(String message) {
    super(message);
  }

  public IllegalArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalArgumentException() {
    super("Format Error: The input was not valid. Please check your input and try again.");
  }
}
