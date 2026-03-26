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

package com.planck.planck.domain.apikeys.service;

import com.planck.planck.Status200Response;
import com.planck.planck.domain.apikeys.dto.ApiKeysResponseDTO;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import java.util.Set;

public interface ApiKeysService {
  ApiKeysResponseDTO getApiKeysStatus(User user);

  String getApiKeyForUser(ModelProvider type, User user);

  Status200Response setApiKey(User user, ModelProvider modelProvider, String key);

  Status200Response deleteApiKey(User user, ModelProvider type);

  Set<ModelProvider> getApiKeyPresentSet(User user);

  void deleteByUser(User user);

  String getApiKeyForUser(String apiKeyType, User user);
}
