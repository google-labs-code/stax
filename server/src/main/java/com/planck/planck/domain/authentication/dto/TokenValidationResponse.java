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

package com.planck.planck.domain.authentication.dto;

/** DTO for JWT token validation response */
public class TokenValidationResponse {
  private boolean valid;
  private String message;

  public TokenValidationResponse(boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }

  // Default constructor for JSON serialization
  public TokenValidationResponse() {}

  // Getters
  public boolean isValid() {
    return valid;
  }

  public String getMessage() {
    return message;
  }

  // Setters
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
