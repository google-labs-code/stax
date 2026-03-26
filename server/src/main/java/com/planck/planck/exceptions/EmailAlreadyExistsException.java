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
 * Exception thrown when an attempt is made to register an email that already exists. The email
 * address provided during registration or update is already associated with an existing user
 * account.
 */
public class EmailAlreadyExistsException extends RuntimeException {

  public EmailAlreadyExistsException(String message) {
    super(message);
  }

  public EmailAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailAlreadyExistsException() {
    super(
        "Email Exists: This email is already registered. Please log in or try a different email.");
  }
}
