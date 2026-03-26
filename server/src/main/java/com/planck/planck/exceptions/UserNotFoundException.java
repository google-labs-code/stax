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
 * Thrown when the user account associated with the provided credentials (e.g., email, username)
 * could not be found.
 */
public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException() {
    super(
        "User Not Found: We couldn't find an account with those details. Please check your"
            + " credentials or sign up.");
  }

  public UserNotFoundException(String message) {
    super(message);
  }
}
