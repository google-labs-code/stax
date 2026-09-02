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

package com.planck.planck.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.domain.apikeys.service.ApiKeysService;
import com.planck.planck.domain.model.dto.CustomEndpointRegistrationRequest;
import com.planck.planck.domain.model.dto.ModelDTO;
import com.planck.planck.domain.model.dto.ModelPropertyDescriptor;
import com.planck.planck.domain.model.dto.ModelPropertyDescriptor.PropertyType;
import com.planck.planck.domain.model.dto.ModelUpdateRequest;
import com.planck.planck.domain.model.dto.NewModelRegistrationWithCustomProperitesRequest;
import com.planck.planck.domain.model.service.ModelServiceImpl;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.enums.ModelType;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.exceptions.ResourceAlreadyExistsException;
import com.planck.planck.exceptions.UserQuotaExceededException;
import com.planck.planck.util.EncryptionUtil;
import com.planck.planck.util.ObjectMapperUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModelServiceTest {

  @Mock private ModelRepository modelRepository;

  @Mock private ApiKeysService apiKeysService;

  @Mock private ObjectMapper objectMapper;

  @InjectMocks private ModelServiceImpl modelService;

  private User testUser;
  private Model systemModel;
  private Model userModel;
  private Model userModelToUpdate;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-123");
    testUser.setEmail("test@example.com");

    systemModel = new Model();
    systemModel.setId("system-model-1");
    systemModel.setLabel("System Model A");
    systemModel.setType(ModelType.SYSTEM);
    systemModel.setProvider(ModelProvider.GOOGLE);
    systemModel.setName("Gemini 2.5 Flash");
    systemModel.setVersion("gemini-2.5-flash");
    systemModel.setDescriptors("{\"temperature\": 0.7}");
    systemModel.setProperties("{\"temperature\": 0.7}");
    systemModel.setDeprecated(false);

    userModel = new Model();
    userModel.setId("user-model-1");
    userModel.setLabel("User Model B");
    userModel.setType(ModelType.USER);
    userModel.setUser(testUser);
    userModel.setProvider(ModelProvider.OPENAI);
    userModel.setName("GPT-4");
    userModel.setVersion("gpt-4");
    userModel.setDescriptors("{\"max_tokens\": 1000}");
    userModel.setProperties("{\"max_tokens\": 1000}");
    userModel.setDeprecated(false);

    userModelToUpdate = new Model();
    userModelToUpdate.setId("user-model-update");
    userModelToUpdate.setLabel("Old Label");
    userModelToUpdate.setType(ModelType.USER);
    userModelToUpdate.setUser(testUser);
    userModelToUpdate.setProvider(ModelProvider.OPENAI);
    userModelToUpdate.setName("GPT-3.5");
    userModelToUpdate.setVersion("gpt-3.5-turbo");
    userModelToUpdate.setDescriptors("{}");
    userModelToUpdate.setProperties("{}");
    userModelToUpdate.setDeprecated(false);
  }

  @Test
  void getModelForUser_shouldReturnSystemModel_whenSystemModelExists() {
    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(
            systemModel.getId(), ModelType.SYSTEM))
        .thenReturn(systemModel);

    Model foundModel = modelService.getModelForUser(testUser, systemModel.getId());

    assertNotNull(foundModel);
    assertEquals(systemModel.getId(), foundModel.getId());
    assertEquals(ModelType.SYSTEM, foundModel.getType());
    verify(modelRepository, never()).findModelByIdAndUser(anyString(), any(User.class));
  }

  @Test
  void getModelForUser_shouldReturnUserModel_whenSystemModelNotFound() {
    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(userModel.getId(), ModelType.SYSTEM))
        .thenReturn(null);
    when(modelRepository.findModelByIdAndUser(userModel.getId(), testUser)).thenReturn(userModel);

    Model foundModel = modelService.getModelForUser(testUser, userModel.getId());

    assertNotNull(foundModel);
    assertEquals(userModel.getId(), foundModel.getId());
    assertEquals(ModelType.USER, foundModel.getType());
    assertEquals(testUser, foundModel.getUser());
  }

  @Test
  void getModelForUser_shouldThrowNotFoundException_whenNoModelFound() {
    String nonExistentModelId = "non-existent-id";
    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(nonExistentModelId, ModelType.SYSTEM))
        .thenReturn(null);
    when(modelRepository.findModelByIdAndUser(nonExistentModelId, testUser)).thenReturn(null);

    assertThrows(
        NotFoundException.class,
        () -> {
          modelService.getModelForUser(testUser, nonExistentModelId);
        });
  }

  @Test
  void deprecateModel_shouldDeprecateUserModelSuccessfully() {
    when(modelRepository.findModelByIdAndUser(userModel.getId(), testUser)).thenReturn(userModel);
    when(modelRepository.saveAndFlush(any(Model.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ModelDTO result = modelService.deprecateModel(userModel.getId(), testUser);

    assertNotNull(result);
    assertTrue(result.isDeprecated());
    verify(modelRepository).saveAndFlush(userModel);
    assertTrue(userModel.isDeprecated());
  }

  @Test
  void deprecateModel_shouldThrowNotFoundException_whenModelNotFoundForUser() {
    String nonExistentModelId = "non-existent-id";
    when(modelRepository.findModelByIdAndUser(nonExistentModelId, testUser)).thenReturn(null);

    assertThrows(
        NotFoundException.class,
        () -> {
          modelService.deprecateModel(nonExistentModelId, testUser);
        });
    verify(modelRepository, never()).saveAndFlush(any());
  }

  @Test
  void deprecateModel_shouldThrowIllegalArgumentException_whenModelIsNotUserType() {
    // Need a model that findModelByIdAndUser returns but is not USER type
    // This scenario is tricky because findModelByIdAndUser implies it's the user's model.
    // Let's simulate finding a model that somehow isn't USER type (though repository logic might
    // prevent this)
    Model systemModelFoundByUser = new Model();
    systemModelFoundByUser.setId("system-but-found");
    systemModelFoundByUser.setType(ModelType.SYSTEM);
    systemModelFoundByUser.setUser(testUser);

    when(modelRepository.findModelByIdAndUser(systemModelFoundByUser.getId(), testUser))
        .thenReturn(systemModelFoundByUser);

    assertThrows(
        com.planck.planck.exceptions.IllegalArgumentException.class,
        () -> {
          modelService.deprecateModel(systemModelFoundByUser.getId(), testUser);
        },
        "No User model found for this id");
    verify(modelRepository, never()).saveAndFlush(any());
  }

  @Test
  void getModelsList_shouldReturnUserAndSystemModels() throws IOException {
    List<Model> modelsFromRepo = Arrays.asList(systemModel, userModel);

    when(modelRepository.findWithDynamicFilters(eq(testUser), any(), any()))
        .thenReturn(modelsFromRepo);
    Set<ModelProvider> keysPresent = Set.of(ModelProvider.GOOGLE, ModelProvider.OPENAI);
    when(apiKeysService.getApiKeyPresentSet(testUser)).thenReturn(keysPresent);

    List<ModelDTO> result = modelService.getModelsList(testUser, null, null);

    assertNotNull(result);
    assertEquals(2, result.size());

    ModelDTO systemDto =
        result.stream().filter(m -> m.getId().equals(systemModel.getId())).findFirst().orElse(null);
    assertNotNull(systemDto);
    assertEquals(systemModel.getLabel(), systemDto.getLabel());
    assertTrue(systemDto.isApiKeyPresent());

    ModelDTO userDto =
        result.stream().filter(m -> m.getId().equals(userModel.getId())).findFirst().orElse(null);
    assertNotNull(userDto);
    assertEquals(userModel.getLabel(), userDto.getLabel());
    assertTrue(userDto.isApiKeyPresent());
  }

  @Test
  void getModelsList_shouldHandleMissingApiKeys() throws IOException {
    List<Model> modelsFromRepo = Arrays.asList(systemModel, userModel);

    when(modelRepository.findWithDynamicFilters(eq(testUser), any(), any()))
        .thenReturn(modelsFromRepo);
    Set<ModelProvider> keysPresent = Set.of(ModelProvider.GOOGLE);
    when(apiKeysService.getApiKeyPresentSet(testUser)).thenReturn(keysPresent);

    List<ModelDTO> result = modelService.getModelsList(testUser, null, null);

    assertNotNull(result);
    assertEquals(2, result.size());

    ModelDTO systemDto =
        result.stream().filter(m -> m.getId().equals(systemModel.getId())).findFirst().orElse(null);
    assertNotNull(systemDto);
    assertTrue(systemDto.isApiKeyPresent());

    ModelDTO userDto =
        result.stream().filter(m -> m.getId().equals(userModel.getId())).findFirst().orElse(null);
    assertNotNull(userDto);
    assertFalse(userDto.isApiKeyPresent());
  }

  @Test
  void updateModel_shouldUpdateFieldsSuccessfully_withoutLabelChange()
      throws JsonProcessingException {
    when(modelRepository.findModelByIdAndUser(userModelToUpdate.getId(), testUser))
        .thenReturn(userModelToUpdate);
    when(modelRepository.saveAndFlush(any(Model.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ModelUpdateRequest request = new ModelUpdateRequest();
    request.setDescription("New Description");
    request.setComments("New Comments");
    Map<String, Object> newDescriptors = Map.of("temperature", 0.9);
    request.setDescriptors(newDescriptors);

    ModelDTO result =
        modelService.updateModel(
            userModelToUpdate.getId(),
            testUser,
            request.getLabel(),
            request.getDescription(),
            request.getComments(),
            request.getDescriptors(),
            null, // apiKey
            null, // apiUrl
            null // customHeaders
            );

    assertNotNull(result);
    assertEquals(userModelToUpdate.getId(), result.getId());
    assertEquals("New Description", result.getDescription());
    assertEquals("New Comments", result.getComments());

    assertEquals(
        ObjectMapperUtil.convertMapToJsonString(newDescriptors),
        userModelToUpdate.getDescriptors());

    verify(modelRepository).saveAndFlush(userModelToUpdate);
  }

  @Test
  void updateModel_shouldUpdateFieldsSuccessfully_withLabelChange() throws JsonProcessingException {
    String newLabel = "New Label";

    when(modelRepository.findModelByIdAndUser(userModelToUpdate.getId(), testUser))
        .thenReturn(userModelToUpdate);
    when(modelRepository.saveAndFlush(any(Model.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ModelUpdateRequest request = new ModelUpdateRequest();
    request.setLabel(newLabel);
    request.setDescription("Another Description");

    ModelDTO result =
        modelService.updateModel(
            userModelToUpdate.getId(),
            testUser,
            request.getLabel(),
            request.getDescription(),
            request.getComments(),
            request.getDescriptors(),
            null, // apiKey
            null, // apiUrl
            null // customHeaders
            );

    assertNotNull(result);
    assertEquals(newLabel, result.getLabel());
    assertEquals("Another Description", result.getDescription());

    verify(modelRepository).saveAndFlush(userModelToUpdate);
  }

  @Test
  void updateModel_shouldHandleLabelChange_whenTagNotFound() throws JsonProcessingException {
    String newLabel = "New Label Tag Not Found";

    when(modelRepository.findModelByIdAndUser(userModelToUpdate.getId(), testUser))
        .thenReturn(userModelToUpdate);
    when(modelRepository.saveAndFlush(any(Model.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ModelUpdateRequest request = new ModelUpdateRequest();
    request.setLabel(newLabel);

    ModelDTO result =
        modelService.updateModel(
            userModelToUpdate.getId(),
            testUser,
            request.getLabel(),
            request.getDescription(),
            request.getComments(),
            request.getDescriptors(),
            null, // apiKey
            null, // apiUrl
            null // customHeaders
            );

    assertNotNull(result);
    assertEquals(newLabel, result.getLabel());

    verify(modelRepository).saveAndFlush(userModelToUpdate);
  }

  @Test
  void updateModel_shouldThrowNotFoundException_whenModelNotFound() {
    String nonExistentModelId = "non-existent-update";
    when(modelRepository.findModelByIdAndUser(nonExistentModelId, testUser)).thenReturn(null);

    ModelUpdateRequest request = new ModelUpdateRequest();
    request.setLabel("Doesn't matter");

    assertThrows(
        NotFoundException.class,
        () -> {
          modelService.updateModel(
              nonExistentModelId,
              testUser,
              request.getLabel(),
              request.getDescription(),
              request.getComments(),
              request.getDescriptors(),
              null, // apiKey
              null, // apiUrl
              null // customHeaders
              );
        });
    verify(modelRepository, never()).saveAndFlush(any());
  }

  @Test
  void updateModel_shouldUpdateCustomEndpointFields() throws JsonProcessingException {
    Model customModel = new Model();
    customModel.setId("custom-model-1");
    customModel.setLabel("Custom Model");
    customModel.setType(ModelType.USER);
    customModel.setProvider(ModelProvider.OPENAI);
    customModel.setUser(testUser);
    customModel.setUrl("https://old-url.com");
    customModel.setProperties("{}\n");
    // Make isCustomEndpoint() return true
    customModel.setUrl("https://custom-endpoint.com");

    when(modelRepository.findModelByIdAndUser(customModel.getId(), testUser))
        .thenReturn(customModel);
    when(modelRepository.saveAndFlush(any(Model.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    String newApiKey = "new-api-key";
    String newApiUrl = "https://new-endpoint.com";
    Map<String, String> newHeaders = Map.of("X-Test-Header", "header-value");

    try (MockedStatic<EncryptionUtil> encryptionUtilMock = mockStatic(EncryptionUtil.class)) {
      encryptionUtilMock
          .when(() -> EncryptionUtil.encrypt(newApiKey))
          .thenReturn("encrypted-new-api-key");

      ModelDTO result =
          modelService.updateModel(
              customModel.getId(),
              testUser,
              null, // label
              null, // description
              null, // comments
              null, // descriptors
              newApiKey,
              newApiUrl,
              newHeaders);

      assertNotNull(result);
      assertEquals(newApiUrl, customModel.getUrl());
      Map<String, Object> props =
          ObjectMapperUtil.convertJsonStringToMap(customModel.getProperties());
      assertTrue(props.containsKey("encrypted_api_key"));
      assertTrue(props.containsKey("additional_headers"));
      assertEquals(newHeaders, props.get("additional_headers"));
      assertEquals("encrypted-new-api-key", props.get("encrypted_api_key"));
      verify(modelRepository).saveAndFlush(customModel);
    }
  }

  @Test
  void registerNewModel_shouldRegisterSuccessfully_withProvidedLabel() {
    String newModelId = "new-model-123";
    String providedLabel = "My Custom Model";
    Map<String, Object> properties = Map.of("temperature", 0.8, "max_tokens", 500);
    NewModelRegistrationWithCustomProperitesRequest request =
        new NewModelRegistrationWithCustomProperitesRequest();
    request.setLabel(providedLabel);
    request.setNewModelproperties(properties);

    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(
            systemModel.getId(), ModelType.SYSTEM))
        .thenReturn(systemModel);

    when(modelRepository.countModelsByUserAndType(testUser, ModelType.USER)).thenReturn(0);

    when(modelRepository.save(any(Model.class)))
        .thenAnswer(
            invocation -> {
              Model saved = invocation.getArgument(0);
              saved.setId(newModelId);
              return saved;
            });

    ModelDTO result =
        modelService.registerNewModelWithCustomProperties(request, systemModel.getId(), testUser);

    assertNotNull(result);
    assertEquals(newModelId, result.getId());
    assertEquals(providedLabel, result.getLabel());
    assertEquals(ModelType.USER, result.getModelType());
    assertEquals(properties, result.getProperties());
    verify(modelRepository).save(any(Model.class));
  }

  @Test
  void registerNewModel_shouldRegisterSuccessfully_withGeneratedLabel()
      throws JsonProcessingException {
    String newModelId = "new-model-gen-label";
    Map<String, Object> properties = Map.of("temperature", 0.9, "max_tokens", 1000);
    NewModelRegistrationWithCustomProperitesRequest request =
        new NewModelRegistrationWithCustomProperitesRequest();
    request.setNewModelproperties(properties);

    Model baseModel = new Model();
    baseModel.setId("base-model-desc");
    baseModel.setLabel("Base Model");
    baseModel.setType(ModelType.SYSTEM);
    baseModel.setDescriptors("{\"temperature\": 0.7, \"max_tokens\": 1000}");
    baseModel.setProperties("{}");

    String expectedGeneratedLabel = "Base Model Temperature=0.9";

    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(baseModel.getId(), ModelType.SYSTEM))
        .thenReturn(baseModel);

    when(modelRepository.countModelsByUserAndType(testUser, ModelType.USER)).thenReturn(0);

    when(modelRepository.save(any(Model.class)))
        .thenAnswer(
            invocation -> {
              Model saved = invocation.getArgument(0);
              saved.setId(newModelId);
              return saved;
            });

    ModelDTO result =
        modelService.registerNewModelWithCustomProperties(request, baseModel.getId(), testUser);

    assertNotNull(result);
    assertEquals(newModelId, result.getId());
    assertEquals(expectedGeneratedLabel, result.getLabel());
    assertEquals(ModelType.USER, result.getModelType());
    assertEquals(properties, result.getProperties());

    verify(modelRepository).save(any(Model.class));
  }

  @Test
  void registerNewModel_shouldRegisterSuccessfully_withOptionalFields() {
    String newModelId = "new-model-123";
    String providedLabel = "My Custom Model";
    Map<String, Object> properties = Map.of("temperature", 0.8, "max_tokens", 500);
    NewModelRegistrationWithCustomProperitesRequest request =
        new NewModelRegistrationWithCustomProperitesRequest();
    request.setLabel(providedLabel);
    request.setNewModelproperties(properties);
    request.setDescription("Custom Description");
    request.setComments("Custom Comments");
    Map<String, ModelPropertyDescriptor> descriptors = new HashMap<>();
    ModelPropertyDescriptor descriptor =
        new ModelPropertyDescriptor(
            "descriptor",
            PropertyType.INTEGER,
            "0",
            "0",
            "1000",
            false,
            "custom descriptor",
            "custom descriptor description");
    descriptors.put("descriptor", descriptor);
    request.setDescriptors(descriptors);

    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(
            systemModel.getId(), ModelType.SYSTEM))
        .thenReturn(systemModel);

    when(modelRepository.countModelsByUserAndType(testUser, ModelType.USER)).thenReturn(0);

    when(modelRepository.save(any(Model.class)))
        .thenAnswer(
            invocation -> {
              Model saved = invocation.getArgument(0);
              saved.setId(newModelId);
              return saved;
            });

    try {
      when(objectMapper.writeValueAsString(anyMap())).thenReturn("{\"some\": \"json\"}");
    } catch (JsonProcessingException e) {
    }

    ModelDTO result =
        modelService.registerNewModelWithCustomProperties(request, systemModel.getId(), testUser);

    assertNotNull(result);
    assertEquals(newModelId, result.getId());
    assertEquals(providedLabel, result.getLabel());
    assertEquals(ModelType.USER, result.getModelType());
    assertEquals(properties, result.getProperties());
    verify(modelRepository).save(any(Model.class));
  }

  @Test
  void registerNewModel_shouldThrowUserQuotaExceededException() {
    NewModelRegistrationWithCustomProperitesRequest request =
        new NewModelRegistrationWithCustomProperitesRequest();
    request.setNewModelproperties(Map.of("temperature", 0.8));

    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(
            systemModel.getId(), ModelType.SYSTEM))
        .thenReturn(systemModel);
    when(modelRepository.countModelsByUserAndType(testUser, ModelType.USER)).thenReturn(50);

    assertThrows(
        UserQuotaExceededException.class,
        () -> {
          modelService.registerNewModelWithCustomProperties(request, systemModel.getId(), testUser);
        });

    verify(modelRepository, never()).save(any());
  }

  @Test
  void registerNewModel_shouldThrowIllegalArgumentException_whenPropertiesNull() {
    NewModelRegistrationWithCustomProperitesRequest request =
        new NewModelRegistrationWithCustomProperitesRequest();
    request.setNewModelproperties(null);

    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(
            systemModel.getId(), ModelType.SYSTEM))
        .thenReturn(systemModel);

    assertThrows(
        com.planck.planck.exceptions.IllegalArgumentException.class,
        () -> {
          modelService.registerNewModelWithCustomProperties(request, systemModel.getId(), testUser);
        },
        "Model properties is a required field");

    verify(modelRepository, never()).save(any());
  }

  @Test
  void
      registerNewModel_shouldThrowResourceAlreadyExistsException_whenLabelMatchesExistingUserLabel() {
    String existingLabel = "Existing User Model Label";
    NewModelRegistrationWithCustomProperitesRequest request =
        new NewModelRegistrationWithCustomProperitesRequest();
    request.setLabel(existingLabel);
    request.setNewModelproperties(Map.of("temperature", 0.8));

    Model baseSystemModel = new Model();
    baseSystemModel.setId("base-system-model");
    baseSystemModel.setLabel("Base System");
    baseSystemModel.setType(ModelType.SYSTEM);

    Model existingUserModel = new Model();
    existingUserModel.setId("existing-user-model");
    existingUserModel.setLabel(existingLabel);
    existingUserModel.setType(ModelType.USER);
    existingUserModel.setUser(testUser);

    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(
            existingUserModel.getId(), ModelType.SYSTEM))
        .thenReturn(null);
    when(modelRepository.findModelByIdAndUser(existingUserModel.getId(), testUser))
        .thenReturn(existingUserModel);

    when(modelRepository.countModelsByUserAndType(testUser, ModelType.USER)).thenReturn(0);

    assertThrows(
        ResourceAlreadyExistsException.class,
        () -> {
          modelService.registerNewModelWithCustomProperties(
              request, existingUserModel.getId(), testUser);
        },
        "A model with label '" + existingLabel + "' already exists for this user");

    verify(modelRepository, never()).save(any());
  }

  private CustomEndpointRegistrationRequest getCustomEndpointRegistrationRequest() {
    CustomEndpointRegistrationRequest request = new CustomEndpointRegistrationRequest();
    request.setName("test-custom-model");
    request.setLabel("Test Custom Model");
    request.setUrl("https://api.example.com/v1/chat/completions");
    request.setApiKey("sk-test123456789");
    request.setSupportedProvider(ModelProvider.OPENAI);
    request.setDescription("A test custom model");
    request.setComments("Test comments");

    Map<String, String> headers = new HashMap<>();
    headers.put("X-Custom-Header", "test-value");
    request.setAdditionalHeaders(headers);

    Map<String, Object> properties = new HashMap<>();
    properties.put("temperature", 0.7);
    properties.put("max_tokens", 1000);
    request.setProperties(properties);
    return request;
  }

  @Test
  void registerCustomEndpoint_Success() {
    // Given
    when(modelRepository.countModelsByUserAndType(testUser, ModelType.USER)).thenReturn(0);
    when(modelRepository.findModelsByUserAndNotDeprecated(testUser)).thenReturn(new ArrayList<>());
    when(modelRepository.save(any(Model.class)))
        .thenAnswer(
            invocation -> {
              Model saved = invocation.getArgument(0);
              saved.setId("model-test-uuid");
              return saved;
            });

    // Mock EncryptionUtil.encrypt
    try (MockedStatic<com.planck.planck.util.EncryptionUtil> mockedEncryptionUtil =
        mockStatic(com.planck.planck.util.EncryptionUtil.class)) {
      mockedEncryptionUtil
          .when(() -> com.planck.planck.util.EncryptionUtil.encrypt(anyString()))
          .thenReturn("encrypted-api-key");

      // When
      CustomEndpointRegistrationRequest request = getCustomEndpointRegistrationRequest();
      Model result = modelService.registerCustomEndpoint(request, testUser);

      // Then
      assertNotNull(result);
      assertEquals("model-test-uuid", result.getId());
      assertEquals("test-custom-model", result.getName());
      assertEquals("Test Custom Model", result.getLabel());
      assertEquals(ModelProvider.OPENAI, result.getProvider());
      assertEquals("https://api.example.com/v1/chat/completions", result.getUrl());
      assertEquals(ModelType.USER, result.getType());
      assertEquals("A test custom model", result.getDescription());
      assertEquals("Test comments", result.getComments());
      assertNotNull(result.getReleaseDate());
    }
  }

  @Test
  void registerCustomEndpoint_PublicEndpoint_NoApiKey() {
    // Given
    CustomEndpointRegistrationRequest request = getCustomEndpointRegistrationRequest();

    request.setApiKey(""); // No API key for public endpoint
    when(modelRepository.countModelsByUserAndType(testUser, ModelType.USER)).thenReturn(0);
    when(modelRepository.findModelsByUserAndNotDeprecated(testUser)).thenReturn(new ArrayList<>());
    when(modelRepository.save(any(Model.class)))
        .thenAnswer(
            invocation -> {
              Model saved = invocation.getArgument(0);
              saved.setId("model-public-uuid");
              return saved;
            });

    // When
    Model result = modelService.registerCustomEndpoint(request, testUser);

    // Then
    assertNotNull(result);
    assertEquals("model-public-uuid", result.getId());
    assertEquals("test-custom-model", result.getName());
    assertEquals("Test Custom Model", result.getLabel());
    assertEquals(ModelProvider.OPENAI, result.getProvider());
    assertEquals("https://api.example.com/v1/chat/completions", result.getUrl());
    assertEquals(ModelType.USER, result.getType());
    assertEquals("A test custom model", result.getDescription());
    assertEquals("Test comments", result.getComments());
    assertNotNull(result.getReleaseDate());
  }

  @Test
  void registerNewModel_shouldMergeCustomEndpointProperties() {
    // Existing model is a custom endpoint with API key and headers
    Model existingCustomModel = new Model();
    existingCustomModel.setId("existing-custom-model");
    existingCustomModel.setLabel("Existing Custom Model");
    existingCustomModel.setType(ModelType.USER);
    existingCustomModel.setProvider(ModelProvider.OPENAI);
    existingCustomModel.setUser(testUser);
    existingCustomModel.setUrl("https://custom-endpoint.com");
    Map<String, Object> existingProps = new HashMap<>();
    existingProps.put("encrypted_api_key", "existing-encrypted-key");
    existingProps.put("additional_headers", Map.of("X-Old-Header", "old-value"));
    existingProps.put("temperature", 0.5);
    existingCustomModel.setProperties(ObjectMapperUtil.convertMapToJsonString(existingProps));

    // New request with new properties (should override temperature, add max_tokens)
    NewModelRegistrationWithCustomProperitesRequest request =
        new NewModelRegistrationWithCustomProperitesRequest();
    request.setLabel("Merged Custom Model");
    Map<String, Object> newProps = new HashMap<>();
    newProps.put("temperature", 0.9); // override
    newProps.put("max_tokens", 1000); // new
    request.setNewModelproperties(newProps);

    when(modelRepository.findModelByIdAndTypeAndNotDeprecated(
            existingCustomModel.getId(), ModelType.SYSTEM))
        .thenReturn(null);
    when(modelRepository.findModelByIdAndUser(existingCustomModel.getId(), testUser))
        .thenReturn(existingCustomModel);
    when(modelRepository.countModelsByUserAndType(testUser, ModelType.USER)).thenReturn(0);
    when(modelRepository.save(any(Model.class)))
        .thenAnswer(
            invocation -> {
              Model saved = invocation.getArgument(0);
              saved.setId("merged-model-id");
              return saved;
            });

    ModelDTO result =
        modelService.registerNewModelWithCustomProperties(
            request, existingCustomModel.getId(), testUser);

    assertNotNull(result);
    assertEquals("merged-model-id", result.getId());
    assertEquals("Merged Custom Model", result.getLabel());
    assertEquals(ModelType.USER, result.getModelType());
    Map<String, Object> merged = (Map<String, Object>) result.getProperties();
    // API key and headers from existing model should be present
    assertEquals("existing-encrypted-key", merged.get("encrypted_api_key"));
    assertEquals(Map.of("X-Old-Header", "old-value"), merged.get("additional_headers"));
    // New/overridden properties from payload
    assertEquals(0.9, merged.get("temperature"));
    assertEquals(1000, merged.get("max_tokens"));
  }
}
