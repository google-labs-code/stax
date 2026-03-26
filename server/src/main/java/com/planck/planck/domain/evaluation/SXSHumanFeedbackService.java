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

package com.planck.planck.domain.evaluation;

import com.planck.planck.domain.project.dto.SXSChatRowDTO;
import com.planck.planck.domain.project.dto.SXSHumanFeedbackRequestDTO;
import com.planck.planck.domain.project.dto.SXSRatingCount;
import com.planck.planck.entitities.SXSHumanFeedback;
import com.planck.planck.entitities.User;
import java.util.List;

public interface SXSHumanFeedbackService {

  SXSHumanFeedback submitHumanFeedback(
      User user, String projectId, String pairId, SXSHumanFeedbackRequestDTO request);

  void deleteHumanFeedback(User user, String projectId, String pairId);

  void deleteHumanFeedbackForChatTurn(
      User user, String projectId, String pairId, String chatTurnA, String chatTurnB);

  SXSHumanFeedback getHumanFeedback(User user, String pairId);

  void setHumanFeedbackForSxsRows(List<SXSChatRowDTO> rows, User user);

  List<SXSRatingCount> getHumanEvalMetricsByProject(String projectId, User user);
}
