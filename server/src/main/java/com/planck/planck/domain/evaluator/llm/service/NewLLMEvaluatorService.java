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

package com.planck.planck.domain.evaluator.llm.service;

import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorUpdateDTO;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.User;
import java.util.List;

public interface NewLLMEvaluatorService {
  LLMEvaluatorResponseDTO createLLMEvaluator(LLMEvaluatorRequestDTO request, User user);

  boolean checkIfLLMEvaluatorNameExists(String name, User user);

  List<LLMEvaluator> getAllLLMScorer(User user);

  LLMEvaluator getById(User user, String id);

  LLMEvaluatorResponseDTO getEvaluatorById(User user, String evaluatorId);

  LLMEvaluator getByName(User user, String name);

  LLMEvaluatorResponseDTO updateLLMEvaluator(
      LLMEvaluatorUpdateDTO request, String evaluatorId, User user);

  List<LLMEvaluatorResponseDTO> getEvaluatorByUserOrSystemType(User user);

  List<LLMEvaluatorResponseDTO> getEvaluatorBySystemType();

  List<LLMEvaluatorResponseDTO> getEvaluatorByUser(User user);

  void removeLLMEvaluatorById(User user, String evaluatorId);
}
