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

import com.planck.planck.domain.modelinput.dto.ModelInputDTO;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.List;
import java.util.Map;

public interface ModelInputService {
  ModelInput save(ModelInput input);

  ModelInput savePrompt(User user, Prompt prompt);

  ModelInput savePrompt(User user, Prompt prompt, Map<String, String> variables);

  ModelInputDTO getModelInput(User user, String modelInputId);

  List<ModelInput> saveAll(List<ModelInput> inputs);

  List<ModelInput> saveAll(User user, List<Prompt> prompts, Map<String, String> variables);
}
