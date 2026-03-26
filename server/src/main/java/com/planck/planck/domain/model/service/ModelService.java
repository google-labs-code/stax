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

package com.planck.planck.domain.model.service;

import com.planck.planck.domain.model.dto.CustomEndpointRegistrationRequest;
import com.planck.planck.domain.model.dto.ModelDTO;
import com.planck.planck.domain.model.dto.NewModelRegistrationWithCustomProperitesRequest;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.MonitoringType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface ModelService {

  ModelDTO deprecateModel(String modelId, User user);

  Map<String, Model> buildModelMap(User user, Function<Model, String> keyExtractor);

  List<Model> getModelsByUser(User user);

  List<ModelDTO> getModelsList(User user, String projectId, MonitoringType monitoringType);

  ModelDTO updateModel(
      String modelId,
      User user,
      String label,
      String description,
      String comments,
      Map<String, Object> descriptors,
      String apiKey,
      String apiUrl,
      Map<String, String> customHeaders);

  ModelDTO registerNewModelWithCustomProperties(
      NewModelRegistrationWithCustomProperitesRequest request, String modelId, User user);

  Model getModelForUser(User user, String modelId);

  Model registerCustomEndpoint(CustomEndpointRegistrationRequest request, User user);
}
