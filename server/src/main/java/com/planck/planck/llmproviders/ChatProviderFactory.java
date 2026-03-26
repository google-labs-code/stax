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

package com.planck.planck.llmproviders;

import com.planck.planck.domain.apikeys.service.ApiKeysService;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.User;
import com.planck.planck.exceptions.IllegalInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatProviderFactory {

  private final ApplicationContext context;
  private final ApiKeysService apiKeysService;

  public ChatProviderFactory(ApplicationContext context, ApiKeysService apiKeysService) {
    this.context = context;
    this.apiKeysService = apiKeysService;
  }

  public ChatProviderStrategy getStrategy(Model model, User user) {
    ChatProviderStrategy provider;
    try {
      provider =
          context.getBean(model.getProvider().toString().toLowerCase(), ChatProviderStrategy.class);
    } catch (NoSuchBeanDefinitionException e) {
      log.error("No bean found for provider: {}", model.getProvider());
      throw new IllegalInputException("No bean found for provider: " + model.getProvider());
    }

    provider.setModel(model);

    String apiKey = apiKeysService.getApiKeyForUser(model.getProvider(), user);
    provider.setApiKey(apiKey);

    return provider;
  }
}
