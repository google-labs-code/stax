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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.planck.planck.util.Util;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SignUpRequest {

  @NotEmpty(message = "First name can not be empty")
  @Length(min = 1, max = 40, message = "First name length should be between 1 and 40 characters")
  @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "First name should be alphanumeric")
  private String firstName;

  @NotEmpty(message = "Last name can not be empty")
  @Length(min = 1, max = 40, message = "Last name length should be between 1 and 40 characters")
  @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Last name should be alphanumeric")
  private String lastName;

  @NotEmpty(message = "Email can not be empty")
  @Length(min = 1, max = 255, message = "Email length should be between 1 and 255 characters")
  @NotNull(message = "Email can not be empty")
  private String email;

  public SignUpRequest() {}

  public SignUpRequest(String firstName, String lastName, String email) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = Util.normalizeEmail(email);
  }
}
