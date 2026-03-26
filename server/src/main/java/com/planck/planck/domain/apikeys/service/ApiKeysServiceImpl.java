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
import com.planck.planck.domain.apikeys.ApiKeysRepository;
import com.planck.planck.domain.apikeys.dto.ApiKeysResponseDTO;
import com.planck.planck.entitities.ApiKeys;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.exceptions.ApiKeyIsMissingException;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.util.EncryptionUtil;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ApiKeysServiceImpl implements ApiKeysService {

  private ApiKeysRepository apiKeysRepository;

  public ApiKeysServiceImpl(ApiKeysRepository apiKeysRepository) {
    this.apiKeysRepository = apiKeysRepository;
  }

  @Override
  public ApiKeysResponseDTO getApiKeysStatus(User user) {
    Optional<ApiKeys> apiKeysOptional = apiKeysRepository.findByUser(user);
    if (apiKeysOptional.isPresent()) {
      ApiKeys apiKeys = apiKeysOptional.get();
      return ApiKeysResponseDTO.builder()
          .isOpenaiKeyPresent(apiKeys.getOpenaiKey() != null)
          .isMistralKeyPresent(apiKeys.getMistralKey() != null)
          .isGoogleKeyPresent(apiKeys.getGoogleKey() != null)
          .isAnthropicKeyPresent(apiKeys.getAnthropicKey() != null)
          .isGrokKeyPresent(apiKeys.getGrokKey() != null)
          .isOllamaKeyPresent(apiKeys.getOllamaKey() != null)
          .isDeepseekKeyPresent(apiKeys.getDeepseekKey() != null)
          .isHuggingfaceKeyPresent(apiKeys.getHuggingfaceKey() != null)
          .isLlamaKeyPresent(apiKeys.getLlamaKey() != null)
          .build();
    } else {
      return new ApiKeysResponseDTO(); // Or handle the absence differently
    }
  }

  @Override
  public String getApiKeyForUser(ModelProvider type, User user) {
    try {
      return switch (type) {
        case OPENAI -> apiKeysRepository.findOpenAIKeyByUser(user);
        case MISTRAL -> apiKeysRepository.findMistralKeyByUser(user);
        case ANTHROPIC -> apiKeysRepository.findAnthropicKeyByUser(user);
        case GOOGLE -> apiKeysRepository.findGoogleKeyByUser(user);
        case GROK -> apiKeysRepository.findGrokKeyByUser(user);
        // case OLLAMA -> apiKeysRepository.findOllamaKeyByUser(user);
        case DEEPSEEK -> apiKeysRepository.findDeepseekKeyByUser(user);
        // case HUGGINGFACE -> apiKeysRepository.findHuggingfaceKeyByUser(user);
        case LLAMA -> apiKeysRepository.findLlamaKeyByUser(user);
        default -> {
          log.info("Invalid API key type.");
          throw new ApiKeyIsMissingException("Invalid API key type.");
        }
      };
    } catch (Exception e) {
      log.info("Error fetching API key. Error :: {}", e);
      throw new ApiKeyIsMissingException("Error fetching API key.");
    }
  }

  @Transactional
  @Override
  public Status200Response setApiKey(User user, ModelProvider modelProvider, String key) {
    String encryptedKey = null;
    try {
      encryptedKey = EncryptionUtil.encrypt(key);
    } catch (Exception e) {
      log.error("Error while encrypting key. ERROR:: {}", e);
      throw new RuntimeException("Error while encrypting key. ERROR:: " + e.getMessage());
    }
    saveApiKeys(encryptedKey, modelProvider, user);
    log.info("Api Key Set successfully");
    return new Status200Response("Success");
  }

  @Override
  public Status200Response deleteApiKey(User user, ModelProvider type) {
    saveApiKeys(null, type, user);
    log.info("API Key deleted successfully");
    return new Status200Response("API Key deleted successfully");
  }

  @Override
  public Set<ModelProvider> getApiKeyPresentSet(User user) {

    Set<ModelProvider> apiKeyPresentSet = new HashSet<>();
    Optional<ApiKeys> apiKeysOptional = apiKeysRepository.findByUser(user);
    if (apiKeysOptional.isPresent()) {
      ApiKeys apiKeys = apiKeysOptional.get();
      if (apiKeys.getOpenaiKey() != null) apiKeyPresentSet.add(ModelProvider.OPENAI);
      if (apiKeys.getMistralKey() != null) apiKeyPresentSet.add(ModelProvider.MISTRAL);
      if (apiKeys.getGoogleKey() != null) apiKeyPresentSet.add(ModelProvider.GOOGLE);
      if (apiKeys.getAnthropicKey() != null) apiKeyPresentSet.add(ModelProvider.ANTHROPIC);
      if (apiKeys.getGrokKey() != null) apiKeyPresentSet.add(ModelProvider.GROK);
      // if (apiKeys.getOllamaKey() != null) apiKeyPresentSet.add(ModelProvider.OLLAMA);
      if (apiKeys.getDeepseekKey() != null) apiKeyPresentSet.add(ModelProvider.DEEPSEEK);
      // if (apiKeys.getHuggingfaceKey() != null) apiKeyPresentSet.add(ModelProvider.HUGGINGFACE);
      if (apiKeys.getLlamaKey() != null) apiKeyPresentSet.add(ModelProvider.LLAMA);
    }
    return apiKeyPresentSet;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void deleteByUser(User user) {
    apiKeysRepository.deleteByUser(user);
  }

  private ApiKeys saveApiKeys(String apiKey, ModelProvider type, User user) {
    ApiKeys apiKeys;
    Optional<ApiKeys> apiKeysOptional = apiKeysRepository.findByUser(user);
    if (apiKeysOptional.isPresent()) {
      apiKeys = apiKeysOptional.get();
    } else {
      apiKeys = new ApiKeys();
      apiKeys.setUser(user);
    }
    setKeyForProvider(apiKeys, type, apiKey);
    return apiKeysRepository.saveAndFlush(apiKeys);
  }

  private void setKeyForProvider(ApiKeys apiKeys, ModelProvider type, String apiKey) {
    switch (type) {
      case OPENAI:
        apiKeys.setOpenaiKey(apiKey);
        break;
      case MISTRAL:
        apiKeys.setMistralKey(apiKey);
        break;
      case GOOGLE:
        apiKeys.setGoogleKey(apiKey);
        break;
      case ANTHROPIC:
        apiKeys.setAnthropicKey(apiKey);
        break;
      case GROK:
        apiKeys.setGrokKey(apiKey);
        break;
      // case OLLAMA:
      //   apiKeys.setOllamaKey(apiKey);
      //   break;
      case DEEPSEEK:
        apiKeys.setDeepseekKey(apiKey);
        break;
      // case HUGGINGFACE:
      //   apiKeys.setHuggingfaceKey(apiKey);
      //   break;
      case LLAMA:
        apiKeys.setLlamaKey(apiKey);
        break;
      default:
        log.error("API Key type is not valid :: {}", type);
        throw new IllegalInputException("API Key type is not valid:  " + type);
    }
  }

  @Override
  public String getApiKeyForUser(String apiKeyType, User user) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getApiKeyForUser'");
  }
}
