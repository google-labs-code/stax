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

package com.planck.planck.domain.tags.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "TagUpdateRequest")
public class TagUpdateRequest {
  @Schema(description = "Name of the tag", example = "MyTag")
  @Length(max = 20, message = "Tag size can not be more than 20 characters")
  @Pattern(
      regexp = "^[a-zA-Z0-9@_-]+$",
      message = "Only alphanumeric and @, _, - speical characters are allowed")
  private String name;

  @Schema(description = "Color of the tag", example = "#FFFFFF")
  private String color;
}
