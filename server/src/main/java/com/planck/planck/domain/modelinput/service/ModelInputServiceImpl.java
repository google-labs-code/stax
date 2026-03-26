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

package com.planck.planck.domain.modelinput.service;

import com.planck.planck.domain.modelinput.ModelInputRepository;
import com.planck.planck.domain.modelinput.dto.ModelInputDTO;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.User;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.util.PromptUtil;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelInputServiceImpl implements ModelInputService {

  @Autowired ModelInputRepository modelInputRepository;

  @Override
  public ModelInput save(ModelInput input) {
    return modelInputRepository.save(input);
  }

  // to be used only for saving data in Model Input Table.
  @Override
  public ModelInput savePrompt(User user, Prompt prompt) {
    return modelInputRepository.save(new ModelInput(prompt, user));
  }

  @Override
  public ModelInput savePrompt(User user, Prompt prompt, Map<String, String> variables) {
    return modelInputRepository.save(new ModelInput(prompt, user, variables));
  }

  @Override
  public ModelInputDTO getModelInput(User user, String modelInputId) {
    ModelInput modelInput =
        modelInputRepository
            .findByUserAndId(user, modelInputId)
            .orElseThrow(() -> new NotFoundException(modelInputId + " can't be found"));
    return new ModelInputDTO(modelInput);
  }

  @Override
  public List<ModelInput> saveAll(List<ModelInput> inputs) {
    return modelInputRepository.saveAllAndFlush(inputs);
  }

  @Override
  public List<ModelInput> saveAll(User user, List<Prompt> prompts, Map<String, String> variables) {
    List<ModelInput> inputs =
        prompts.stream()
            .map(
                prompt -> {
                  boolean needsVariables = PromptUtil.containsVariableInput(prompt.getText());

                  return new ModelInput(prompt, user, needsVariables ? variables : null);
                })
            .toList();
    List<ModelInput> saved = modelInputRepository.saveAll(inputs);
    modelInputRepository.flush();
    return saved;
  }
}
