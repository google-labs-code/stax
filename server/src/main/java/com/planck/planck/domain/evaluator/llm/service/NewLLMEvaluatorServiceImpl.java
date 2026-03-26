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

import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorUpdateDTO;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
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
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class NewLLMEvaluatorServiceImpl implements NewLLMEvaluatorService {

  @Autowired private LLMEvaluatorRepository llmEvaluatorRepository;

  @Autowired private ModelService modelService;

  @Autowired private ModelInputService modelInputService;

  @Autowired private EvaluationStatusRepository evaluationStatusRepository;

  @Override
  public LLMEvaluatorResponseDTO createLLMEvaluator(LLMEvaluatorRequestDTO request, User user) {

    Model model = getAndValidateModel(request.getModelId(), user);

    validateEvaluatorNameUniqueness(request.getName(), user);

    return new LLMEvaluatorResponseDTO(
        llmEvaluatorRepository.save(createLLMEvaluatorObject(request, model, user)));
  }

  @Override
  public boolean checkIfLLMEvaluatorNameExists(String name, User user) {

    return llmEvaluatorRepository.checkIfLLMEvaluatorNameExists(name, user, ScopeType.SYSTEM);
  }

  @Transactional(readOnly = false)
  private LLMEvaluator createLLMEvaluatorObject(
      LLMEvaluatorRequestDTO request, Model model, User user) {

    LLMEvaluator llmEvaluator = new LLMEvaluator();

    // pre-fills
    llmEvaluator.setType(ScopeType.USER);

    llmEvaluator.setDescription(request.getDescription());
    llmEvaluator.setName(request.getName());
    llmEvaluator.setUser(user);
    String variables = ObjectMapperUtil.toJsonString(request.getVariables());
    String outPutCategories = ObjectMapperUtil.toJsonString(request.getOutputCategories());
    llmEvaluator.setOutputCategories(outPutCategories);
    llmEvaluator.setOutputFormatType(request.getOutputFormateType());
    llmEvaluator.setModel(model);
    llmEvaluator.setVariables(variables);

    List<ModelInput> modelInputs = createModelInputsFromPrompts(request.getPrompts(), user);
    llmEvaluator.setInputs(modelInputs);

    return llmEvaluator;
  }

  @Override
  public List<LLMEvaluator> getAllLLMScorer(User user) {
    return llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM);
  }

  @Override
  public LLMEvaluator getById(User user, String id) {
    log.info("Get Evalutor by id:: {} ", id);
    return llmEvaluatorRepository
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(id, user)
        .orElseThrow(() -> new NotFoundException("evaluator: " + id + " does not exists"));
  }

  @Transactional(readOnly = false)
  @Override
  public LLMEvaluatorResponseDTO getEvaluatorById(User user, String evaluatorId) {
    log.info("Get evaluator by evaluatorId:: {} ", evaluatorId);
    LLMEvaluator lLMEvaluator =
        llmEvaluatorRepository
            .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, user)
            .orElseThrow(
                () -> new NotFoundException("evaluator: " + evaluatorId + " does not exists"));
    return new LLMEvaluatorResponseDTO(lLMEvaluator);
  }

  @Override
  public LLMEvaluator getByName(User user, String name) {
    log.info("Getting LLM Evaluator by name:: {} ", name);
    LLMEvaluator llmEvaluator =
        llmEvaluatorRepository
            .findByNameAndUserOrType(name, user, ScopeType.SYSTEM)
            .orElseThrow(() -> new NotFoundException("evaluator: " + name + " does not exists"));
    return llmEvaluator;
  }

  @Transactional(readOnly = false)
  @Override
  public LLMEvaluatorResponseDTO updateLLMEvaluator(
      LLMEvaluatorUpdateDTO request, String evaluatorId, User user) {

    LLMEvaluator llmEvaluator =
        llmEvaluatorRepository
            .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, user)
            .orElseThrow(
                () -> new NotFoundException("Evaluator: " + evaluatorId + " does not exists"));
    if (llmEvaluator.getType() == ScopeType.SYSTEM) {
      log.info("System Evaluator cannot be updated");
      throw new IllegalInputException("System Evaluator cannot be updated");
    }
    return new LLMEvaluatorResponseDTO(updateLLMEvaluator(llmEvaluator, request, user));
  }

  @Transactional(readOnly = false)
  private LLMEvaluator updateLLMEvaluator(
      LLMEvaluator llMEvaluator, LLMEvaluatorUpdateDTO request, User user) {

    if (request.getName() != null && !llMEvaluator.getName().equals(request.getName())) {
      validateEvaluatorNameUniqueness(request.getName(), user);
      llMEvaluator.setName(request.getName());
    }

    if (request.getDescription() != null) {
      llMEvaluator.setDescription(request.getDescription());
    }

    if (request.getOutputFormateType() != null) {
      llMEvaluator.setOutputFormatType(request.getOutputFormateType());
    }

    if (request.getOutputCategories() != null) {
      llMEvaluator.setOutputCategories(
          ObjectMapperUtil.toJsonString(request.getOutputCategories()));
    }

    if (request.getVariables() != null) {
      llMEvaluator.setVariables(ObjectMapperUtil.toJsonString(request.getVariables()));
    }

    if (request.getModelId() != null) {
      llMEvaluator.setModel(getAndValidateModel(request.getModelId(), user));
    }

    if (request.getPrompts() != null) {
      llMEvaluator.setInputs(createModelInputsFromPrompts(request.getPrompts(), user));
    }

    return llmEvaluatorRepository.save(llMEvaluator);
  }

  @Override
  public List<LLMEvaluatorResponseDTO> getEvaluatorByUserOrSystemType(User user) {
    List<LLMEvaluator> llmEvaluators =
        llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM);

    List<LLMEvaluatorResponseDTO> llmEvaluatorResponseDTOs = new ArrayList<>();
    for (LLMEvaluator llmEvaluator : llmEvaluators) {
      llmEvaluatorResponseDTOs.add(new LLMEvaluatorResponseDTO(llmEvaluator));
    }
    return llmEvaluatorResponseDTOs;
  }

  @Override
  public List<LLMEvaluatorResponseDTO> getEvaluatorBySystemType() {
    List<LLMEvaluator> llmEvaluators =
        llmEvaluatorRepository.findAllNonDeprecatedByType(ScopeType.SYSTEM);
    List<LLMEvaluatorResponseDTO> llmEvaluatorResponseDTOs = new ArrayList<>();
    for (LLMEvaluator llmEvaluator : llmEvaluators) {
      llmEvaluatorResponseDTOs.add(new LLMEvaluatorResponseDTO(llmEvaluator));
    }
    return llmEvaluatorResponseDTOs;
  }

  @Override
  public List<LLMEvaluatorResponseDTO> getEvaluatorByUser(User user) {
    List<LLMEvaluator> llmEvaluators = llmEvaluatorRepository.findAllNonDeprecatedByUser(user);
    List<LLMEvaluatorResponseDTO> llmEvaluatorResponseDTOs = new ArrayList<>();
    for (LLMEvaluator llmEvaluator : llmEvaluators) {
      llmEvaluatorResponseDTOs.add(new LLMEvaluatorResponseDTO(llmEvaluator));
    }
    return llmEvaluatorResponseDTOs;
  }

  @Override
  public void removeLLMEvaluatorById(User user, String evaluatorId) {
    Optional<LLMEvaluator> evaluator = llmEvaluatorRepository.findByIdAndUser(evaluatorId, user);
    if (!evaluator.isPresent()) {
      throw new NotFoundException("Evaluator not found for user");
    }

    LLMEvaluator eval = evaluator.get();

    boolean isUsedInEvaluation = evaluationStatusRepository.existsByEvaluatorId(evaluatorId);

    if (isUsedInEvaluation) {
      eval.setDeprecated(true);
      llmEvaluatorRepository.save(eval);
    } else {
      llmEvaluatorRepository.delete(eval);
    }
  }

  private Model getAndValidateModel(String modelId, User user) {
    Model model = modelService.getModelForUser(user, modelId);
    if (model.isDeprecated()) throw new NotFoundException("Model is deprecated");
    return model;
  }

  private List<ModelInput> createModelInputsFromPrompts(List<Prompt> request, User user) {
    List<ModelInput> modelInputs = new ArrayList<>();
    for (Prompt prompt : request) modelInputs.add(modelInputService.savePrompt(user, prompt));
    return modelInputs;
  }

  private void validateEvaluatorNameUniqueness(String name, User user) {
    boolean existLLMEvaluator = this.checkIfLLMEvaluatorNameExists(name, user);
    if (existLLMEvaluator) {
      log.info("LLm scorer already exist with name {}", name);
      throw new EvaluatorNameExistException(
          CustomScorerStringConstant
              .SCRORE_NAME_ALREADY_EXIST); // Need to update the nameing in future
    }
  }
}
