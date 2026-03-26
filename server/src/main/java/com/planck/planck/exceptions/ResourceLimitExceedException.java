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
 * The user or system has exceeded a specific resource limit (e.g., storage space, number of created
 * items).
 */
public class ResourceLimitExceedException extends RuntimeException {

  public ResourceLimitExceedException(String message) {
    super(message);
  }

  public ResourceLimitExceedException() {
    super("Unexpected Error: Something went wrong.");
  }
}
