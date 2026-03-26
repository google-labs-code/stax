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

package com.planck.planck.domain.evaluator.human.service;

import com.planck.planck.domain.evaluator.human.dto.HumanCategoryOption;
import com.planck.planck.domain.evaluator.human.dto.HumanRangeOption;
import com.planck.planck.domain.evaluator.human.dto.ScoreV2DTO;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.LinkedEntityType;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoringMechanismType;
import java.util.List;

public interface HumanEvaluatorService {

  HumanEvaluator createEvaluator(
      User user,
      String name,
      String description,
      ScoringMechanismType scoringMechanismType,
      String associatedEntityId,
      String entityType,
      List<HumanCategoryOption> categories,
      LinkedEntityType linkedEntityType);

  HumanEvaluator createUserThumbsEvaluator(User user);

  HumanEvalScore createChatTurnFeedback(
      ModelResponse modelResponse, HumanEvaluator evaluator, User user, Double score, String notes);

  void deleteHumanEvalScoreById(String humanEvalScoreId, User user);

  List<HumanEvaluator> getEvaluatorsByEntityId(String entityId);

  List<HumanEvaluator> getEvaluatorsByEntityType(LinkedEntityType entityType);

  List<HumanEvaluator> getUserEvaluators(User user);

  List<HumanEvaluator> getEvaluators(
      User user, ScopeType scopeType, ScoringMechanismType scoringMechanismType);

  List<ScoreV2DTO> getChatTurnFeedbacks(String chatTurnId);

  HumanEvaluator findByIdAndUser(String id, User user);

  HumanEvaluator updateEvaluator(
      String id,
      User user,
      String name,
      String description,
      String comments,
      ScoringMechanismType scoringMechanismType,
      LinkedEntityType linkedEntityType);

  HumanEvaluator deprecateEvaluator(String evaluatorId, User user);

  HumanEvaluator createRangeEvaluator(
      String name, String description, HumanRangeOption rangeOption, User user);

  List<HumanEvaluator> getEvaluators(User user);

  HumanEvaluator getEvaluator(String id, User user);

  void deleteEvaluator(String id, User user);

  void deleteAllByUser(User user);
}
