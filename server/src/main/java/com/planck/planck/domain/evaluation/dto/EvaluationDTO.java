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

package com.planck.planck.domain.evaluation.dto;

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
public class EvaluationDTO implements JobIdentifiable, Serializable {
  private static final long serialVersionUID = 1L;

  private String evaluatorId;

  private String chatTurnId;

  private String chatturnIdB;

  private String pairId;

  private String modelResponseId;

  private String jobId;

  private String userId;

  private String comments;

  private String scoreId;

  private String evaluationStatusId;

  private String reason;

  private String containerId;

  @Override
  public String toString() {
    return String.format(
        "EvaluatorDTO [evaluatorId=%s, chatTurnId=%s, jobId=%s, userId=%s, , evaluationStatusId=%s]",
        evaluatorId, chatTurnId, jobId, userId, comments, evaluationStatusId);
  }
}
