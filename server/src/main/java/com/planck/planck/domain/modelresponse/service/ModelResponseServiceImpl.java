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

package com.planck.planck.domain.modelresponse.service;

import com.planck.planck.domain.modelresponse.ModelResponseDTO;
import com.planck.planck.domain.modelresponse.ModelResponseRepository;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.User;
import com.planck.planck.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelResponseServiceImpl implements ModelResponseService {

  @Autowired ModelResponseRepository modelResponseRepository;

  @Override
  public ModelResponse saveModelResponse(ModelResponse modelResponse) {
    return modelResponseRepository.saveAndFlush(modelResponse);
  }

  @Override
  public ModelResponseDTO getModelResponse(User user, String modelResponseId) {
    ModelResponse modelResponse =
        modelResponseRepository
            .findByUserAndId(user, modelResponseId)
            .orElseThrow(() -> new NotFoundException(modelResponseId + " can't be found"));
    return new ModelResponseDTO(modelResponse);
  }

  @Override
  public void deleteById(User user, String id) {
    ModelResponse modelResponse =
        modelResponseRepository
            .findByUserAndId(user, id)
            .orElseThrow(() -> new NotFoundException("ModelResponse not found"));

    modelResponseRepository.delete(modelResponse);
  }

  @Override
  public void deleteByContainer(EvaluationContainer container) {
    modelResponseRepository.deleteAllByContainer(container);
  }

  @Override
  public ModelResponse createModelResponse(User user, EvaluationContainer container, Model model) {
    ModelResponse modelResponse = new ModelResponse();
    modelResponse.setModel(model);
    modelResponse.setUser(user);
    modelResponse.setContainer(container);
    return saveModelResponse(modelResponse);
  }
}
