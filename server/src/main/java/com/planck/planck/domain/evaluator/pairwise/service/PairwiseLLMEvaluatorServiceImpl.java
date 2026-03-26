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

import com.planck.planck.domain.evaluation.PairwiseScoreRepository;
import com.planck.planck.domain.evaluator.pairwise.PairwiseLLMEvaluatorRepository;
import com.planck.planck.domain.evaluator.pairwise.dto.PairwiseLLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.pairwise.dto.PairwiseLLMEvaluatorResponseDTO;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.exceptions.EvaluatorNameExistException;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.util.CustomScorerStringConstant;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class PairwiseLLMEvaluatorServiceImpl implements PairwiseLLMEvaluatorService {

  private final PairwiseLLMEvaluatorRepository pairwiseLlmEvaluatorRepository;
  private final PairwiseScoreRepository pairwiseScoreRepository;
  private final ModelService modelService;
  private final ModelInputService modelInputService;

  @Override
  public PairwiseLLMEvaluatorResponseDTO createPairwiseLLMEvaluator(
      PairwiseLLMEvaluatorRequestDTO request, User user) {

    Model model = validateAndGetModel(user, request.getModelId());
    validateEvaluatorNameUniqueness(request.getName(), user);

    return new PairwiseLLMEvaluatorResponseDTO(
        pairwiseLlmEvaluatorRepository.save(
            createPairwiseLLMEvaluatorObject(request, model, user)));
  }

  @Override
  public List<PairwiseLLMEvaluator> getAllPairwiseLLMEvaluators(User user) {
    return pairwiseLlmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM);
  }

  @Transactional(readOnly = false)
  @Override
  public PairwiseLLMEvaluatorResponseDTO getEvaluatorById(User user, String evaluatorId) {
    log.info("Get pairwise evaluator by evaluatorId:: {} ", evaluatorId);
    PairwiseLLMEvaluator pairwiseLlmEvaluator =
        pairwiseLlmEvaluatorRepository
            .findById(evaluatorId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "pairwise evaluator: " + evaluatorId + " does not exists"));
    return new PairwiseLLMEvaluatorResponseDTO(pairwiseLlmEvaluator);
  }

  @Override
  public List<PairwiseLLMEvaluatorResponseDTO> getEvaluatorByUserOrSystemType(User user) {
    List<PairwiseLLMEvaluator> pairwiseLlmEvaluators =
        pairwiseLlmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM);

    List<PairwiseLLMEvaluatorResponseDTO> pairwiseLlmEvaluatorResponseDTOs = new ArrayList<>();
    for (PairwiseLLMEvaluator pairwiseLlmEvaluator : pairwiseLlmEvaluators) {
      pairwiseLlmEvaluatorResponseDTOs.add(
          new PairwiseLLMEvaluatorResponseDTO(pairwiseLlmEvaluator));
    }
    return pairwiseLlmEvaluatorResponseDTOs;
  }

  @Override
  public PairwiseLLMEvaluatorResponseDTO updatePairwiseLLMEvaluator(
      User user, String evaluatorId, PairwiseLLMEvaluatorRequestDTO request) {
    PairwiseLLMEvaluator evaluator = getAndValidateExistingEvaluator(evaluatorId);
    validateSystemEvaluator(evaluator);

    if (request.getName() != null && !evaluator.getName().equals(request.getName())) {
      validateEvaluatorNameUniqueness(request.getName(), user);
      evaluator.setName(request.getName());
    }

    if (request.getDescription() != null) {
      evaluator.setDescription(request.getDescription());
    }

    if (request.getModelId() != null && !request.getModelId().isEmpty()) {
      Model model = validateAndGetModel(user, request.getModelId());
      evaluator.setModel(model);
    }
    if (request.getVariables() != null && !request.getVariables().isEmpty()) {
      evaluator.setVariables(ObjectMapperUtil.toJsonString(request.getVariables()));
    }

    if (request.getOutputCategories() != null && !request.getOutputCategories().isEmpty()) {
      evaluator.setOutputCategories(ObjectMapperUtil.toJsonString(request.getOutputCategories()));
    }

    if (request.getOutputFormateType() != null) {
      evaluator.setOutputFormatType(request.getOutputFormateType());
    }

    if (request.getPrompts() != null && !request.getPrompts().isEmpty()) {
      setEvaluatorPrompts(evaluator, request.getPrompts(), user);
    }
    return new PairwiseLLMEvaluatorResponseDTO(pairwiseLlmEvaluatorRepository.save(evaluator));
  }

  @Override
  public void removePairwiseLLMEvaluatorById(User user, String evaluatorId) {
    PairwiseLLMEvaluator evaluator = getAndValidateExistingEvaluator(evaluatorId);
    validateSystemEvaluator(evaluator);

    if (pairwiseScoreRepository.existsByEvaluatorId(evaluator.getId(), user.getId())) {
      evaluator.setDeprecated(true);
      pairwiseLlmEvaluatorRepository.save(evaluator);
    } else {
      pairwiseLlmEvaluatorRepository.delete(evaluator);
    }
  }

  private boolean checkIfPairwiseLLMEvaluatorNameExists(String name, User user) {
    return pairwiseLlmEvaluatorRepository.checkIfPairwiseLLMEvaluatorNameExists(
        name, user, ScopeType.SYSTEM);
  }

  private PairwiseLLMEvaluator createPairwiseLLMEvaluatorObject(
      PairwiseLLMEvaluatorRequestDTO request, Model model, User user) {

    PairwiseLLMEvaluator pairwiseLlmEvaluator = initializeEvaluator(request, user);
    pairwiseLlmEvaluator.setModel(model);
    String variables = ObjectMapperUtil.toJsonString(request.getVariables());
    String outputCategories = ObjectMapperUtil.toJsonString(request.getOutputCategories());
    pairwiseLlmEvaluator.setOutputCategories(outputCategories);
    pairwiseLlmEvaluator.setOutputFormatType(request.getOutputFormateType());
    pairwiseLlmEvaluator.setVariables(variables);
    setEvaluatorPrompts(pairwiseLlmEvaluator, request.getPrompts(), user);

    return pairwiseLlmEvaluator;
  }

  private PairwiseLLMEvaluator initializeEvaluator(
      PairwiseLLMEvaluatorRequestDTO request, User user) {
    PairwiseLLMEvaluator evaluator = new PairwiseLLMEvaluator();
    evaluator.setType(ScopeType.USER);
    evaluator.setName(request.getName());
    evaluator.setDescription(request.getDescription());
    evaluator.setUser(user);
    return evaluator;
  }

  private void setEvaluatorPrompts(
      PairwiseLLMEvaluator evaluator, List<Prompt> prompts, User user) {
    List<ModelInput> modelInputs = new ArrayList<>();
    for (Prompt prompt : prompts) {
      modelInputs.add(modelInputService.savePrompt(user, prompt));
    }
    evaluator.setInputs(modelInputs);
  }

  private PairwiseLLMEvaluator getAndValidateExistingEvaluator(String evaluatorId) {
    return pairwiseLlmEvaluatorRepository
        .findById(evaluatorId)
        .orElseThrow(
            () -> new NotFoundException("Pairwise evaluator: " + evaluatorId + " does not exists"));
  }

  private Model validateAndGetModel(User user, String modelId) {
    Model model = modelService.getModelForUser(user, modelId);
    if (model.isDeprecated()) {
      throw new NotFoundException("Model is deprecated");
    }
    return model;
  }

  private void validateEvaluatorNameUniqueness(String name, User user) {
    boolean existPairwiseLLMEvaluator = checkIfPairwiseLLMEvaluatorNameExists(name, user);
    if (existPairwiseLLMEvaluator) {
      log.info("Pairwise LLM evaluator already exist with name {}", name);
      throw new EvaluatorNameExistException(CustomScorerStringConstant.SCRORE_NAME_ALREADY_EXIST);
    }
  }

  private void validateSystemEvaluator(PairwiseLLMEvaluator evaluator) {
    if (evaluator.getType() == ScopeType.SYSTEM) {
      log.info("System Pairwise Evaluator cannot be updated/removed");
      throw new IllegalInputException("System Pairwise Evaluator cannot be updated/removed");
    }
  }
}
