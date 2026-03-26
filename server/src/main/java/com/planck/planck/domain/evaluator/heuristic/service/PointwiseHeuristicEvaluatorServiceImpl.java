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

import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.evaluator.dto.PointwiseHeuristicEvaluatorUpdateDTO;
import com.planck.planck.domain.evaluator.heuristic.PointwiseHeuristicEvaluatorRepository;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorResponseDTO;
import com.planck.planck.entitities.PointwiseHeuristicEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.CriteriaType;
import com.planck.planck.exceptions.EvaluatorNameExistException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.util.CustomScorerStringConstant;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PointwiseHeuristicEvaluatorServiceImpl implements PointwiseHeuristicEvaluatorService {

  public static final String DEFAULT_CONTAINS_EVALUATOR_NAME = "Contains Stax";
  public static final String DEFAULT_CONTAINS_EVALUATOR_CRITERIA = "Stax";
  public static final String DEFAULT_REGEX_EVALUATOR_NAME = "Regex Stax";
  public static final String DEFAULT_REGEX_EVALUATOR_CRITERIA = ".*Stax.*";

  private final PointwiseHeuristicEvaluatorRepository pointwiseHeuristicEvaluatorRepository;

  private final EvaluationStatusRepository evaluationStatusRepository;

  @Override
  public PointwiseHeuristicEvaluatorResponseDTO createEvaluator(
      PointwiseHeuristicEvaluatorRequestDTO request, User user) {

    validateEvaluatorNameUniqueness(request.getName(), user);

    return new PointwiseHeuristicEvaluatorResponseDTO(
        savePointwiseHeuristicEvaluator(
            user, request.getName(), request.getCriteria(), request.getCriteriaType()));
  }

  @Override
  public List<PointwiseHeuristicEvaluatorResponseDTO> getEvaluatorsByUser(User user) {
    return pointwiseHeuristicEvaluatorRepository.findAllNonDeprecatedByUser(user).stream()
        .map(PointwiseHeuristicEvaluatorResponseDTO::new)
        .toList();
  }

  @Override
  public PointwiseHeuristicEvaluatorResponseDTO getEvaluatorById(String id, User user) {
    PointwiseHeuristicEvaluator evaluator =
        pointwiseHeuristicEvaluatorRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Evaluator not found with id: " + id));

    return new PointwiseHeuristicEvaluatorResponseDTO(evaluator);
  }

  @Override
  public PointwiseHeuristicEvaluatorResponseDTO updateEvaluator(
      String id, PointwiseHeuristicEvaluatorUpdateDTO request, User user) {

    PointwiseHeuristicEvaluator evaluator =
        pointwiseHeuristicEvaluatorRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Evaluator not found with id: " + id));

    if (request.getName() != null && !evaluator.getName().equals(request.getName())) {
      validateEvaluatorNameUniqueness(request.getName(), user);
      evaluator.setName(request.getName());
    }

    return new PointwiseHeuristicEvaluatorResponseDTO(
        pointwiseHeuristicEvaluatorRepository.save(evaluator));
  }

  @Override
  public void removePointwiseHeuristicEvaluatorById(String evaluatorId, User user) {
    Optional<PointwiseHeuristicEvaluator> evaluator =
        pointwiseHeuristicEvaluatorRepository.findByIdAndUser(evaluatorId, user);
    if (evaluator.isEmpty()) {
      throw new NotFoundException("Evaluator not found for user");
    }

    PointwiseHeuristicEvaluator eval = evaluator.get();

    boolean isUsedInEvaluation = evaluationStatusRepository.existsByEvaluatorId(evaluatorId);

    if (isUsedInEvaluation) {
      eval.setDeprecated(true);
      pointwiseHeuristicEvaluatorRepository.save(eval);
    } else {
      pointwiseHeuristicEvaluatorRepository.delete(eval);
    }
  }

  @Override
  public void createDefaultHeuristicEvaluators(User user) {
    savePointwiseHeuristicEvaluator(
        user,
        DEFAULT_CONTAINS_EVALUATOR_NAME,
        DEFAULT_CONTAINS_EVALUATOR_CRITERIA,
        CriteriaType.CONTAINS);

    savePointwiseHeuristicEvaluator(
        user, DEFAULT_REGEX_EVALUATOR_NAME, DEFAULT_REGEX_EVALUATOR_CRITERIA, CriteriaType.REGEX);
  }

  private PointwiseHeuristicEvaluator savePointwiseHeuristicEvaluator(
      User user, String name, String criteria, CriteriaType type) {
    PointwiseHeuristicEvaluator evaluator = new PointwiseHeuristicEvaluator();
    evaluator.setName(name);
    evaluator.setUser(user);
    evaluator.setCriteria(criteria);
    evaluator.setCriteriaType(type);

    return pointwiseHeuristicEvaluatorRepository.save(evaluator);
  }

  private void validateEvaluatorNameUniqueness(String name, User user) {
    boolean existPointwiseHeuristicEvaluator =
        pointwiseHeuristicEvaluatorRepository.checkIfPointwiseHeuristicEvaluatorNameExists(
            name, user);

    if (existPointwiseHeuristicEvaluator) {
      log.info("Pointwise Heuristic evaluator already exist with name {}", name);
      throw new EvaluatorNameExistException(CustomScorerStringConstant.SCRORE_NAME_ALREADY_EXIST);
    }
  }
}
