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

package com.planck.planck.domain.evaluator.heuristic.service;

import com.planck.planck.domain.evaluator.dto.PointwiseHeuristicEvaluatorUpdateDTO;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorResponseDTO;
import com.planck.planck.entitities.User;
import java.util.List;

public interface PointwiseHeuristicEvaluatorService {

  PointwiseHeuristicEvaluatorResponseDTO createEvaluator(
      PointwiseHeuristicEvaluatorRequestDTO request, User user);

  PointwiseHeuristicEvaluatorResponseDTO getEvaluatorById(String id, User user);

  List<PointwiseHeuristicEvaluatorResponseDTO> getEvaluatorsByUser(User user);

  PointwiseHeuristicEvaluatorResponseDTO updateEvaluator(
      String id, PointwiseHeuristicEvaluatorUpdateDTO request, User user);

  void removePointwiseHeuristicEvaluatorById(String evaluatorId, User user);

  void createDefaultHeuristicEvaluators(User user);
}
