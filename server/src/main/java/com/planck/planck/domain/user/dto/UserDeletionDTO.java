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

package com.planck.planck.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletionDTO {

  @JsonProperty("request_id")
  private String requestId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("deletion_steps")
  private List<DeletionStep> deletionSteps;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeletionStep {
    @JsonProperty("table_name")
    private String tableName;
  }
}
