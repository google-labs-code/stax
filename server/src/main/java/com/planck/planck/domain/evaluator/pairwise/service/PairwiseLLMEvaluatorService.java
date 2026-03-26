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

package com.planck.planck.domain.evaluator.pairwise.service;

import com.planck.planck.domain.evaluator.pairwise.dto.PairwiseLLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.pairwise.dto.PairwiseLLMEvaluatorResponseDTO;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.User;
import java.util.List;

public interface PairwiseLLMEvaluatorService {
  PairwiseLLMEvaluatorResponseDTO createPairwiseLLMEvaluator(
      PairwiseLLMEvaluatorRequestDTO request, User user);

  List<PairwiseLLMEvaluator> getAllPairwiseLLMEvaluators(User user);

  PairwiseLLMEvaluatorResponseDTO getEvaluatorById(User user, String evaluatorId);

  List<PairwiseLLMEvaluatorResponseDTO> getEvaluatorByUserOrSystemType(User user);

  void removePairwiseLLMEvaluatorById(User user, String evaluatorId);

  PairwiseLLMEvaluatorResponseDTO updatePairwiseLLMEvaluator(
      User user, String evaluatorId, PairwiseLLMEvaluatorRequestDTO request);
}
