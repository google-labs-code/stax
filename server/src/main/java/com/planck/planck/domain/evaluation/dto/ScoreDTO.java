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

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreDTO implements Serializable {

  private static final long serialVersionUID = -1131388837722270123L;

  private String scoreStatusId;

  private String responseId;

  private String scorerId;

  private String userId;

  private String inputId;

  private String scorer;

  private String sourceAPI;

  private String comments;

  private String jobId;

  @Override
  public String toString() {
    return String.format(
        "ScoreDTO [scoreStatusId=%s, responseId=%s, scorerId=%s, userId=%s, inputId=%s, scorer=%s,]",
        scoreStatusId, responseId, scorerId, userId, inputId, scorer);
  }
}
