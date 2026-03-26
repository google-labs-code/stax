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

package com.planck.planck.domain.inference.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.job.dto.JobIdentifiable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceDTO implements JobIdentifiable, Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("chat_turn_id")
  private String chatTurnId;

  @JsonProperty("job_id")
  private String jobId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("project_id")
  private String projectId;

  @JsonProperty("comments")
  private String comments;

  @JsonProperty("inference_status_id")
  private String inferenceStatusId;

  @JsonProperty("model_id")
  private String modelId;

  @JsonProperty("reason")
  private String reason;

  @Override
  public String toString() {
    return String.format(
        "InferenceDTO [chatTurnId=%s, jobId=%s, userId=%s, projectId=%s, comments=%s, inferenceStatusId=%s]",
        chatTurnId, jobId, userId, projectId, comments, inferenceStatusId);
  }
}
