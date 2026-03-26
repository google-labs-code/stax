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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TagFindResponse", description = "Response object for finding tags")
public class TagFindResponse {
  @Schema(description = "List of user tags", example = "[]")
  @JsonProperty("user_tags")
  private List<TagDTO> userTags;

  @Schema(description = "List of model tags", example = "[]")
  @JsonProperty("model_tags")
  private List<TagDTO> modelTags;

  @Schema(description = "List of dataset tags", example = "[]")
  @JsonProperty("dataset_tags")
  private List<TagDTO> dataSetTags;
}
