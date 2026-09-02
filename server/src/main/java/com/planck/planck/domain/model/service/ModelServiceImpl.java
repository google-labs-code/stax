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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.domain.apikeys.service.ApiKeysService;
import com.planck.planck.domain.model.ModelRepository;
import com.planck.planck.domain.model.dto.CustomEndpointRegistrationRequest;
import com.planck.planck.domain.model.dto.ModelDTO;
import com.planck.planck.domain.model.dto.NewModelRegistrationWithCustomProperitesRequest;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.enums.ModelType;
import com.planck.planck.enums.MonitoringType;
import com.planck.planck.exceptions.IllegalArgumentException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.exceptions.ResourceAlreadyExistsException;
import com.planck.planck.exceptions.UserQuotaExceededException;
import com.planck.planck.util.EncryptionUtil;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.PlanckConstants;
import com.planck.planck.util.UrlSecurityUtil;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

  private static final int NUM_OF_MODELS_PER_USER = 50;

  @Autowired private ModelRepository modelRepository;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ApiKeysService apiKeysService;

  @Transactional()
  @Override
  public ModelDTO deprecateModel(String modelId, User user) {

    // check if model type is user
    Model model = modelRepository.findModelByIdAndUser(modelId, user);
    if (model == null) throw new NotFoundException("Model id does not exist for this user");
    if (model.getType() != ModelType.USER)
      throw new IllegalArgumentException("No User model found for this id"); // sanity check
    model.setDeprecated(true);

    return new ModelDTO(modelRepository.saveAndFlush(model));
  }

  @Transactional(readOnly = true)
  @Override
  public Map<String, Model> buildModelMap(User user, Function<Model, String> keyExtractor) {
    return getModelsByUser(user).stream()
        .collect(Collectors.toMap(keyExtractor, Function.identity(), this::resolveModelMerge));
  }

  private Model resolveModelMerge(Model existing, Model incoming) {
    if (ModelType.USER.equals(existing.getType())) {
      return existing;
    }
    if (ModelType.USER.equals(incoming.getType())) {
      return incoming;
    }
    return ModelType.SYSTEM.equals(existing.getType()) ? existing : incoming;
  }

  @Transactional(readOnly = true)
  @Override
  public List<Model> getModelsByUser(User user) {

    return modelRepository.findNonDeprecatedUserAndSystemModels(user);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ModelDTO> getModelsList(User user, String projectId, MonitoringType monitoringType) {
    List<Model> models = modelRepository.findWithDynamicFilters(user, projectId, monitoringType);
    // fetch API keys present for that user
    Set<ModelProvider> apiKeysPresentSet = apiKeysService.getApiKeyPresentSet(user);

    List<ModelDTO> modelResponseList = new ArrayList<>();
    for (Model model : models) {
      Map<String, Object> pricing = fetchPricing(user, model);
      ModelDTO modelResponse = new ModelDTO(model, apiKeysPresentSet, pricing);
      modelResponseList.add(modelResponse);
    }

    return modelResponseList;
  }

  private Map<String, Object> fetchPricing(User user, Model model) {
    return null; // pricing feature to be implemented
  }

  @Transactional(readOnly = false)
  @Override
  public ModelDTO updateModel(
      String modelId,
      User user,
      String label,
      String description,
      String comments,
      Map<String, Object> descriptors,
      String apiKey,
      String apiUrl,
      Map<String, String> additionalHeaders) {
    Model model = modelRepository.findModelByIdAndUser(modelId, user);
    if (model == null) throw new NotFoundException("Model id does not exist for this user");

    if (label != null) model.setLabel(label);
    if (description != null) model.setDescription(description);
    if (comments != null) model.setComments(comments);
    if (descriptors != null)
      model.setDescriptors(ObjectMapperUtil.convertMapToJsonString(descriptors));

    if (model.isCustomEndpoint()) {
      updateCustomModel(apiKey, apiUrl, additionalHeaders, model);
    }

    Model updatedModel = modelRepository.saveAndFlush(model);
    return new ModelDTO(updatedModel);
  }

  @Transactional(readOnly = false)
  @Override
  public ModelDTO registerNewModelWithCustomProperties(
      NewModelRegistrationWithCustomProperitesRequest request, String modelId, User user) {

    Model currentModel = getModelForUser(user, modelId);

    validateRegisterNewModelInput(request, user, currentModel);

    Model newModel = constructModelPayload(request, user, currentModel);

    Model savedModel = modelRepository.save(newModel);
    return new ModelDTO(savedModel);
  }

  @Transactional(readOnly = true)
  @Override
  public Model getModelForUser(User user, String modelId) {

    // check if a system mode exist for this
    Model model = modelRepository.findModelByIdAndTypeAndNotDeprecated(modelId, ModelType.SYSTEM);
    if (model != null) return model;

    // check if a user model exist for this
    model = modelRepository.findModelByIdAndUser(modelId, user);

    if (model == null) throw new NotFoundException("No Model found");

    return model;
  }

  private String generateDefaultLabelName(
      NewModelRegistrationWithCustomProperitesRequest request, Model currentModel) {
    StringBuilder label = new StringBuilder(currentModel.getLabel());

    Map<String, Object> newModelProperties = request.getNewModelproperties();
    Map<String, Object> existingDescriptors = null;

    // Parse existing descriptors if available
    if (currentModel.getDescriptors() != null && !currentModel.getDescriptors().isEmpty()) {
      try {
        existingDescriptors =
            ObjectMapperUtil.convertJsonStringToMap(currentModel.getDescriptors());
      } catch (Exception e) {
        log.warn(
            "Failed to parse descriptors for model {}: {}", currentModel.getId(), e.getMessage());
      }
    }

    // Process each property
    for (String property : newModelProperties.keySet()) {
      Object propertyValue = newModelProperties.get(property);

      // Skip properties that are null
      if (propertyValue == null || existingDescriptors == null) {
        continue;
      }

      // Skip properties that match the default value in the existing model's descriptors
      Object existingPropertyDefaultValue = existingDescriptors.getOrDefault(property, null);
      if (propertyValue.equals(existingPropertyDefaultValue)) {
        continue;
      }

      // Add the property to the label
      label.append(" ").append(capitalizeFirstLetter(property)).append("=").append(propertyValue);
    }

    return label.toString();
  }

  private String capitalizeFirstLetter(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    return input.substring(0, 1).toUpperCase() + input.substring(1);
  }

  private void validateRegisterNewModelInput(
      NewModelRegistrationWithCustomProperitesRequest request, User user, Model currentModel) {
    if (request.getNewModelproperties() == null)
      throw new IllegalArgumentException("Model properties is a required field");

    // checking if the user have quota to register a new model
    int count = modelRepository.countModelsByUserAndType(user, ModelType.USER);
    if (count >= NUM_OF_MODELS_PER_USER)
      throw new UserQuotaExceededException(
          "Custom model are limited to " + NUM_OF_MODELS_PER_USER + " per user");

    // Check if the (user, type, label) combination is unique
    if (request.getLabel() != null) {
      String requestedLabel = request.getLabel();
      if (requestedLabel == currentModel.getLabel() && currentModel.getType() == ModelType.USER) {
        throw new ResourceAlreadyExistsException(
            "A model with label '" + requestedLabel + "' already exists for this user.");
      }
    }
  }

  private Model constructModelPayload(
      NewModelRegistrationWithCustomProperitesRequest request, User user, Model currentModel) {
    Model newModel = new Model(currentModel);
    newModel.setUser(user);

    Map<String, Object> mergedProperties = new HashMap<>();

    if (newModel.isCustomEndpoint()) {
      Map<String, Object> existingProperties =
          ObjectMapperUtil.convertJsonStringToMap(currentModel.getProperties());
      if (existingProperties != null) {
        if (existingProperties.containsKey(PlanckConstants.API_KEY_PROPERTY_NAME)) {
          mergedProperties.put(
              PlanckConstants.API_KEY_PROPERTY_NAME,
              existingProperties.get(PlanckConstants.API_KEY_PROPERTY_NAME));
        }
        if (existingProperties.containsKey(PlanckConstants.ADDITIONAL_HEADERS)) {
          mergedProperties.put(
              PlanckConstants.ADDITIONAL_HEADERS,
              existingProperties.get(PlanckConstants.ADDITIONAL_HEADERS));
        }
      }
    }
    if (request.getNewModelproperties() != null) {
      mergedProperties.putAll(request.getNewModelproperties());
    }
    newModel.setProperties(ObjectMapperUtil.convertMapToJsonString(mergedProperties));

    newModel.setLabel(request.getLabel());

    if (newModel.getLabel() == null) {
      newModel.setLabel(generateDefaultLabelName(request, currentModel));
    }

    if (request.getDescription() != null) newModel.setDescription(request.getDescription());

    if (request.getComments() != null) newModel.setComments(request.getComments());

    if (request.getDescriptors() != null) {
      try {
        newModel.setDescriptors(objectMapper.writeValueAsString(request.getDescriptors()));
      } catch (Exception e) {
        log.error("Error serializing model descriptors: {}", e.getMessage());
        throw new RuntimeException("Failed to process model descriptors", e);
      }
    }

    return newModel;
  }

  @Transactional(readOnly = false)
  @Override
  public Model registerCustomEndpoint(CustomEndpointRegistrationRequest request, User user) {
    validateCustomEndpointInput(request, user);

    // Create a new model entity for the custom endpoint
    Model customModel = new Model();
    customModel.setUser(user);
    customModel.setName(request.getName());
    customModel.setLabel(request.getLabel());
    customModel.setProvider(request.getSupportedProvider());
    customModel.setUrl(request.getUrl());
    customModel.setType(ModelType.USER);
    customModel.setDescription(request.getDescription());
    customModel.setComments(request.getComments());
    customModel.setReleaseDate(new Timestamp(System.currentTimeMillis())); // Set to current date

    // Store properties including the encrypted API key and additional headers
    Map<String, Object> properties = new HashMap<>();
    if (request.getProperties() != null) {
      properties.putAll(request.getProperties());
    }

    // Encrypt and store the API key only if provided
    if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
      try {
        String encryptedApiKey = EncryptionUtil.encrypt(request.getApiKey());
        properties.put(PlanckConstants.API_KEY_PROPERTY_NAME, encryptedApiKey);
      } catch (Exception e) {
        log.error("Failed to encrypt API key", e);
        throw new RuntimeException("Failed to encrypt API key", e);
      }
    }

    // Store additional headers if provided
    if (request.getAdditionalHeaders() != null) {
      properties.put(PlanckConstants.ADDITIONAL_HEADERS, request.getAdditionalHeaders());
    }

    customModel.setProperties(ObjectMapperUtil.convertMapToJsonString(properties));

    return modelRepository.save(customModel);
  }

  private void validateCustomEndpointInput(CustomEndpointRegistrationRequest request, User user) {
    UrlSecurityUtil.validateExternalUrl(request.getUrl());

    // Check if the user has quota to register a new model
    int count = modelRepository.countModelsByUserAndType(user, ModelType.USER);
    if (count >= NUM_OF_MODELS_PER_USER) {
      throw new UserQuotaExceededException(
          "Custom models are limited to " + NUM_OF_MODELS_PER_USER + " per user");
    }

    // Check if a model with the same label already exists for this user
    List<Model> existingModels = modelRepository.findModelsByUserAndNotDeprecated(user);
    for (Model existingModel : existingModels) {
      if (existingModel.getLabel().equals(request.getLabel())) {
        throw new ResourceAlreadyExistsException(
            "A model with label '" + request.getLabel() + "' already exists for this user.");
      }
    }
  }

  private void updateCustomModel(
      String apiKey, String apiUrl, Map<String, String> customHeaders, Model model) {
    if (apiUrl != null) {
      UrlSecurityUtil.validateExternalUrl(apiUrl);
      model.setUrl(apiUrl);
    }
    Map<String, Object> properties = ObjectMapperUtil.convertJsonStringToMap(model.getProperties());
    if (properties == null) properties = new java.util.HashMap<>();

    if (apiKey != null) {
      try {
        String encryptedApiKey = EncryptionUtil.encrypt(apiKey);
        properties.put(PlanckConstants.API_KEY_PROPERTY_NAME, encryptedApiKey);
      } catch (Exception e) {
        log.error("Failed to encrypt API key", e);
        throw new RuntimeException("Failed to encrypt API key", e);
      }
    }
    if (customHeaders != null) {
      properties.put(PlanckConstants.ADDITIONAL_HEADERS, customHeaders);
    }
    model.setProperties(ObjectMapperUtil.convertMapToJsonString(properties));
  }
}
