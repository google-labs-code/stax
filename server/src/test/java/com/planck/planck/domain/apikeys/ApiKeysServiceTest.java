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

package com.planck.planck.domain.apikeys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.Status200Response;
import com.planck.planck.domain.apikeys.dto.ApiKeysRequestDTO;
import com.planck.planck.domain.apikeys.dto.ApiKeysResponseDTO;
import com.planck.planck.domain.apikeys.service.ApiKeysServiceImpl;
import com.planck.planck.entitities.ApiKeys;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.exceptions.ApiKeyIsMissingException;
import com.planck.planck.util.EncryptionUtil;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeysServiceTest {
  @Mock private ApiKeysRepository apiKeysRepository;

  @InjectMocks private ApiKeysServiceImpl apiKeysService;

  private User testUser;
  private ApiKeys testApiKeys;
  private MockedStatic<EncryptionUtil> mockedEncryptionUtil;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-123");
    testUser.setFirstName("Test");

    testApiKeys = new ApiKeys();
    testApiKeys.setUser(testUser);
    testApiKeys.setId("api-keys-abc");

    // Mock static EncryptionUtil
    mockedEncryptionUtil = Mockito.mockStatic(EncryptionUtil.class);
    mockedEncryptionUtil
        .when(() -> EncryptionUtil.encrypt(anyString()))
        .thenAnswer(invocation -> "encrypted-" + invocation.getArgument(0));
    mockedEncryptionUtil
        .when(() -> EncryptionUtil.decrypt(anyString()))
        .thenAnswer(invocation -> ((String) invocation.getArgument(0)).replace("encrypted-", ""));
  }

  @AfterEach
  void tearDown() {
    // Close the static mock
    mockedEncryptionUtil.close();
  }

  @Test
  void getApiKeysStatus_whenKeysExist_allPresent() {
    testApiKeys.setOpenaiKey("encrypted-key1");
    testApiKeys.setMistralKey("encrypted-key2");
    testApiKeys.setGoogleKey("encrypted-key3");
    testApiKeys.setAnthropicKey("encrypted-key4");
    testApiKeys.setGrokKey("encrypted-key5");
    testApiKeys.setOllamaKey("encrypted-key6");
    testApiKeys.setDeepseekKey("encrypted-key7");
    testApiKeys.setHuggingfaceKey("encrypted-key8");
    testApiKeys.setLlamaKey("encrypted-key9");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));

    ApiKeysResponseDTO response = apiKeysService.getApiKeysStatus(testUser);

    assertTrue(response.isOpenaiKeyPresent());
    assertTrue(response.isMistralKeyPresent());
    assertTrue(response.isGoogleKeyPresent());
    assertTrue(response.isAnthropicKeyPresent());
    assertTrue(response.isGrokKeyPresent());
    assertTrue(response.isOllamaKeyPresent());
    assertTrue(response.isDeepseekKeyPresent());
    assertTrue(response.isHuggingfaceKeyPresent());
    assertTrue(response.isLlamaKeyPresent());
    verify(apiKeysRepository).findByUser(testUser);
  }

  @Test
  void getApiKeysStatus_whenKeysExist_somePresent() {
    testApiKeys.setOpenaiKey("encrypted-key1");
    testApiKeys.setGrokKey("encrypted-key5");
    testApiKeys.setDeepseekKey("encrypted-key7");
    // Mistral, Google, Anthropic, Ollama, HuggingFace are null
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));

    ApiKeysResponseDTO response = apiKeysService.getApiKeysStatus(testUser);

    assertTrue(response.isOpenaiKeyPresent());
    assertFalse(response.isMistralKeyPresent());
    assertFalse(response.isGoogleKeyPresent());
    assertFalse(response.isAnthropicKeyPresent());
    assertTrue(response.isGrokKeyPresent());
    assertFalse(response.isOllamaKeyPresent());
    assertTrue(response.isDeepseekKeyPresent());
    assertFalse(response.isHuggingfaceKeyPresent());
    assertFalse(response.isLlamaKeyPresent());
    verify(apiKeysRepository).findByUser(testUser);
  }

  @Test
  void getApiKeysStatus_whenKeysDoNotExist() {
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.empty());

    ApiKeysResponseDTO response = apiKeysService.getApiKeysStatus(testUser);

    assertFalse(response.isOpenaiKeyPresent());
    assertFalse(response.isMistralKeyPresent());
    assertFalse(response.isGoogleKeyPresent());
    assertFalse(response.isAnthropicKeyPresent());
    assertFalse(response.isGrokKeyPresent());
    assertFalse(response.isOllamaKeyPresent());
    assertFalse(response.isDeepseekKeyPresent());
    assertFalse(response.isHuggingfaceKeyPresent());
    assertFalse(response.isLlamaKeyPresent());
    verify(apiKeysRepository).findByUser(testUser);
  }

  @Test
  void getApiKeyForUser_openAi_success() {
    String expectedKey = "encrypted-openai-key";
    when(apiKeysRepository.findOpenAIKeyByUser(testUser)).thenReturn(expectedKey);

    String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.OPENAI, testUser);

    assertEquals(expectedKey, actualKey);
    verify(apiKeysRepository).findOpenAIKeyByUser(testUser);
  }

  @Test
  void getApiKeyForUser_mistral_success() {
    String expectedKey = "encrypted-mistral-key";
    when(apiKeysRepository.findMistralKeyByUser(testUser)).thenReturn(expectedKey);

    String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.MISTRAL, testUser);

    assertEquals(expectedKey, actualKey);
    verify(apiKeysRepository).findMistralKeyByUser(testUser);
  }

  @Test
  void getApiKeyForUser_anthropic_success() {
    String expectedKey = "encrypted-anthropic-key";
    when(apiKeysRepository.findAnthropicKeyByUser(testUser)).thenReturn(expectedKey);

    String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.ANTHROPIC, testUser);

    assertEquals(expectedKey, actualKey);
    verify(apiKeysRepository).findAnthropicKeyByUser(testUser);
  }

  @Test
  void getApiKeyForUser_google_success() {
    String expectedKey = "encrypted-google-key";
    when(apiKeysRepository.findGoogleKeyByUser(testUser)).thenReturn(expectedKey);

    String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.GOOGLE, testUser);

    assertEquals(expectedKey, actualKey);
    verify(apiKeysRepository).findGoogleKeyByUser(testUser);
  }

  @Test
  void getApiKeyForUser_grok_success() {
    String expectedKey = "encrypted-grok-key";
    when(apiKeysRepository.findGrokKeyByUser(testUser)).thenReturn(expectedKey);

    String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.GROK, testUser);

    assertEquals(expectedKey, actualKey);
    verify(apiKeysRepository).findGrokKeyByUser(testUser);
  }

  // @Test
  // void getApiKeyForUser_ollama_success() {
  //   String expectedKey = "encrypted-ollama-key";
  //   when(apiKeysRepository.findOllamaKeyByUser(testUser)).thenReturn(expectedKey);

  //   String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.OLLAMA, testUser);

  //   assertEquals(expectedKey, actualKey);
  //   verify(apiKeysRepository).findOllamaKeyByUser(testUser);
  // }

  @Test
  void getApiKeyForUser_deepseek_success() {
    String expectedKey = "encrypted-deepseek-key";
    when(apiKeysRepository.findDeepseekKeyByUser(testUser)).thenReturn(expectedKey);

    String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.DEEPSEEK, testUser);

    assertEquals(expectedKey, actualKey);
    verify(apiKeysRepository).findDeepseekKeyByUser(testUser);
  }

  // @Test
  // void getApiKeyForUser_huggingface_success() {
  //   String expectedKey = "encrypted-huggingface-key";
  //   when(apiKeysRepository.findHuggingfaceKeyByUser(testUser)).thenReturn(expectedKey);

  //   String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.HUGGINGFACE, testUser);

  //   assertEquals(expectedKey, actualKey);
  //   verify(apiKeysRepository).findHuggingfaceKeyByUser(testUser);
  // }

  @Test
  void getApiKeyForUser_llama_success() {
    String expectedKey = "encrypted-llama-key";
    when(apiKeysRepository.findLlamaKeyByUser(testUser)).thenReturn(expectedKey);

    String actualKey = apiKeysService.getApiKeyForUser(ModelProvider.LLAMA, testUser);

    assertEquals(expectedKey, actualKey);
    verify(apiKeysRepository).findLlamaKeyByUser(testUser);
  }

  @Test
  void getApiKeyForUser_repositoryThrowsException_throwsApiKeyIsMissingException() {
    when(apiKeysRepository.findOpenAIKeyByUser(testUser))
        .thenThrow(new RuntimeException("Database error"));

    ApiKeyIsMissingException exception =
        assertThrows(
            ApiKeyIsMissingException.class,
            () -> apiKeysService.getApiKeyForUser(ModelProvider.OPENAI, testUser));

    assertEquals("Error fetching API key.", exception.getMessage());
    verify(apiKeysRepository).findOpenAIKeyByUser(testUser);
  }

  @Test
  void setApiKey_newUser_createsApiKeys() {
    String rawKey = "test-key";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.OPENAI);
    request.setKey(rawKey);

    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.empty());
    // Capture the argument passed to saveAndFlush
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class)))
        .thenAnswer(
            invocation -> {
              ApiKeys savedKeys = invocation.getArgument(0);
              savedKeys.setId("api-keys-new"); // Simulate ID generation
              return savedKeys;
            });

    Status200Response response =
        apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    assertEquals("Success", response.getMessage());
    verify(apiKeysRepository).findByUser(testUser);
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }

  @Test
  void setApiKey_existingUser_updatesApiKeys_openAi() {
    String rawKey = "new-openai-key";
    String encryptedKey = "encrypted-new-openai-key";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.OPENAI);
    request.setKey(rawKey);

    testApiKeys.setMistralKey("existing-mistral"); // Ensure other keys are preserved
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response =
        apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    assertEquals("Success", response.getMessage());
    verify(apiKeysRepository).findByUser(testUser);
    verify(apiKeysRepository)
        .saveAndFlush(
            argThat(
                apiKeys ->
                    apiKeys.getId().equals(testApiKeys.getId())
                        && apiKeys.getOpenaiKey().equals(encryptedKey)
                        && apiKeys.getMistralKey().equals("existing-mistral"))); // Check update
    // and preservation
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }

  @Test
  void setApiKey_existingUser_updatesApiKeys_mistral() {
    String rawKey = "new-mistral-key";
    String encryptedKey = "encrypted-new-mistral-key";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.MISTRAL);
    request.setKey(rawKey);

    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response =
        apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    assertEquals("Success", response.getMessage());
    verify(apiKeysRepository)
        .saveAndFlush(argThat(apiKeys -> apiKeys.getMistralKey().equals(encryptedKey)));
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }

  @Test
  void setApiKey_existingUser_updatesApiKeys_google() {
    String rawKey = "new-google-key";
    String encryptedKey = "encrypted-new-google-key";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.GOOGLE);
    request.setKey(rawKey);

    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response =
        apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    assertEquals("Success", response.getMessage());
    verify(apiKeysRepository)
        .saveAndFlush(argThat(apiKeys -> apiKeys.getGoogleKey().equals(encryptedKey)));
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }

  @Test
  void setApiKey_existingUser_updatesApiKeys_anthropic() {
    String rawKey = "new-anthropic-key";
    String encryptedKey = "encrypted-new-anthropic-key";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.ANTHROPIC);
    request.setKey(rawKey);

    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response =
        apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    assertEquals("Success", response.getMessage());
    verify(apiKeysRepository)
        .saveAndFlush(argThat(apiKeys -> apiKeys.getAnthropicKey().equals(encryptedKey)));
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }

  @Test
  void setApiKey_existingUser_updatesApiKeys_grok() {
    String rawKey = "new-grok-key";
    String encryptedKey = "encrypted-new-grok-key";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.GROK);
    request.setKey(rawKey);

    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response =
        apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    assertEquals("Success", response.getMessage());
    verify(apiKeysRepository)
        .saveAndFlush(argThat(apiKeys -> apiKeys.getGrokKey().equals(encryptedKey)));
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }

  // @Test
  // void setApiKey_existingUser_updatesApiKeys_ollama() {
  //   String rawKey = "new-ollama-key";
  //   String encryptedKey = "encrypted-new-ollama-key";
  //   ApiKeysRequestDTO request = new ApiKeysRequestDTO();
  //   request.setProvider(ModelProvider.OLLAMA);
  //   request.setKey(rawKey);

  //   when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
  //   when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

  //   Status200Response response =
  //       apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

  //   assertEquals("Success", response.getMessage());
  //   verify(apiKeysRepository)
  //       .saveAndFlush(argThat(apiKeys -> apiKeys.getOllamaKey().equals(encryptedKey)));
  //   mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  // }

  @Test
  void setApiKey_existingUser_updatesApiKeys_deepseek() {
    String rawKey = "new-deepseek-key";
    String encryptedKey = "encrypted-new-deepseek-key";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.DEEPSEEK);
    request.setKey(rawKey);

    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response =
        apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    assertEquals("Success", response.getMessage());
    verify(apiKeysRepository)
        .saveAndFlush(argThat(apiKeys -> apiKeys.getDeepseekKey().equals(encryptedKey)));
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }

  // @Test
  // void setApiKey_existingUser_updatesApiKeys_huggingface() {
  //   String rawKey = "new-huggingface-key";
  //   String encryptedKey = "encrypted-new-huggingface-key";
  //   ApiKeysRequestDTO request = new ApiKeysRequestDTO();
  //   request.setProvider(ModelProvider.HUGGINGFACE);
  //   request.setKey(rawKey);

  //   when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
  //   when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

  //   Status200Response response =
  //       apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

  //   assertEquals("Success", response.getMessage());
  //   verify(apiKeysRepository)
  //       .saveAndFlush(argThat(apiKeys -> apiKeys.getHuggingfaceKey().equals(encryptedKey)));
  //   mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  // }

  @Test
  void setApiKey_existingUser_updatesApiKeys_llama() {
    String rawKey = "new-llama-key";
    String encryptedKey = "encrypted-new-llama-key";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.LLAMA);
    request.setKey(rawKey);

    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response =
        apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    assertEquals("Success", response.getMessage());
    verify(apiKeysRepository)
        .saveAndFlush(argThat(apiKeys -> apiKeys.getLlamaKey().equals(encryptedKey)));
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }

  @Test
  void setApiKey_encryptionFails_throwsException() {
    String rawKey = "key-that-fails-encryption";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.OPENAI);
    request.setKey(rawKey);

    // Mock encryption to throw a RuntimeException
    mockedEncryptionUtil
        .when(() -> EncryptionUtil.encrypt(rawKey))
        .thenThrow(new RuntimeException("Encryption failed"));

    // We expect the service to throw the RuntimeException
    assertThrows(
        RuntimeException.class,
        () -> apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey()));

    // Verify that saveAndFlush was NOT called, as the exception should occur before it
    verify(apiKeysRepository, never()).saveAndFlush(any(ApiKeys.class));
  }

  @Test
  void deleteApiKey_existingUser_setsKeyToNull_openAi() {
    testApiKeys.setOpenaiKey("encrypted-key-to-delete");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.OPENAI);

    assertEquals("API Key deleted successfully", response.getMessage());
    verify(apiKeysRepository).findByUser(testUser);
    verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getOpenaiKey() == null));
  }

  @Test
  void deleteApiKey_existingUser_setsKeyToNull_mistral() {
    testApiKeys.setMistralKey("encrypted-key-to-delete");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.MISTRAL);

    assertEquals("API Key deleted successfully", response.getMessage());
    verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getMistralKey() == null));
  }

  @Test
  void deleteApiKey_existingUser_setsKeyToNull_google() {
    testApiKeys.setGoogleKey("encrypted-key-to-delete");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.GOOGLE);

    assertEquals("API Key deleted successfully", response.getMessage());
    verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getGoogleKey() == null));
  }

  @Test
  void deleteApiKey_existingUser_setsKeyToNull_anthropic() {
    testApiKeys.setAnthropicKey("encrypted-key-to-delete");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.ANTHROPIC);

    assertEquals("API Key deleted successfully", response.getMessage());
    verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getAnthropicKey() == null));
  }

  @Test
  void deleteApiKey_existingUser_setsKeyToNull_grok() {
    testApiKeys.setGrokKey("encrypted-key-to-delete");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.GROK);

    assertEquals("API Key deleted successfully", response.getMessage());
    verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getGrokKey() == null));
  }

  // @Test
  // void deleteApiKey_existingUser_setsKeyToNull_ollama() {
  //   testApiKeys.setOllamaKey("encrypted-key-to-delete");
  //   when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
  //   when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

  //   Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.OLLAMA);

  //   assertEquals("API Key deleted successfully", response.getMessage());
  //   verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getOllamaKey() == null));
  // }

  @Test
  void deleteApiKey_existingUser_setsKeyToNull_deepseek() {
    testApiKeys.setDeepseekKey("encrypted-key-to-delete");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.DEEPSEEK);

    assertEquals("API Key deleted successfully", response.getMessage());
    verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getDeepseekKey() == null));
  }

  // @Test
  // void deleteApiKey_existingUser_setsKeyToNull_huggingface() {
  //   testApiKeys.setHuggingfaceKey("encrypted-key-to-delete");
  //   when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
  //   when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

  //   Status200Response response = apiKeysService.deleteApiKey(testUser,
  // ModelProvider.HUGGINGFACE);

  //   assertEquals("API Key deleted successfully", response.getMessage());
  //   verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getHuggingfaceKey() ==
  // null));
  // }

  @Test
  void deleteApiKey_existingUser_setsKeyToNull_llama() {
    testApiKeys.setLlamaKey("encrypted-key-to-delete");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class))).thenReturn(testApiKeys);

    Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.LLAMA);

    assertEquals("API Key deleted successfully", response.getMessage());
    verify(apiKeysRepository).saveAndFlush(argThat(apiKeys -> apiKeys.getLlamaKey() == null));
  }

  @Test
  void deleteApiKey_newUser_createsAndSetsNull() {
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.empty());
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class)))
        .thenAnswer(
            invocation -> {
              ApiKeys savedKeys = invocation.getArgument(0);
              savedKeys.setId("api-keys-new-for-delete");
              return savedKeys;
            });

    Status200Response response = apiKeysService.deleteApiKey(testUser, ModelProvider.OPENAI);

    assertEquals("API Key deleted successfully", response.getMessage());
    verify(apiKeysRepository).findByUser(testUser);
    // Verifies that a new ApiKeys object is created and saved with the specific key as null
    verify(apiKeysRepository)
        .saveAndFlush(
            argThat(
                apiKeys -> apiKeys.getUser().equals(testUser) && apiKeys.getOpenaiKey() == null));
  }

  @Test
  void getApiKeyPresentSet_allPresent() {
    testApiKeys.setOpenaiKey("encrypted-key1");
    testApiKeys.setMistralKey("encrypted-key2");
    testApiKeys.setGoogleKey("encrypted-key3");
    testApiKeys.setAnthropicKey("encrypted-key4");
    testApiKeys.setGrokKey("encrypted-key5");
    // testApiKeys.setOllamaKey("encrypted-key6");
    testApiKeys.setDeepseekKey("encrypted-key7");
    // testApiKeys.setHuggingfaceKey("encrypted-key8");
    testApiKeys.setLlamaKey("encrypted-key9");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));

    Set<ModelProvider> presentKeys = apiKeysService.getApiKeyPresentSet(testUser);

    assertEquals(7, presentKeys.size());
    assertTrue(presentKeys.contains(ModelProvider.OPENAI));
    assertTrue(presentKeys.contains(ModelProvider.MISTRAL));
    assertTrue(presentKeys.contains(ModelProvider.GOOGLE));
    assertTrue(presentKeys.contains(ModelProvider.ANTHROPIC));
    assertTrue(presentKeys.contains(ModelProvider.GROK));
    // assertTrue(presentKeys.contains(ModelProvider.OLLAMA));
    assertTrue(presentKeys.contains(ModelProvider.DEEPSEEK));
    // assertTrue(presentKeys.contains(ModelProvider.HUGGINGFACE));
    assertTrue(presentKeys.contains(ModelProvider.LLAMA));
    verify(apiKeysRepository).findByUser(testUser);
  }

  @Test
  void getApiKeyPresentSet_somePresent() {
    testApiKeys.setOpenaiKey("encrypted-key1");
    testApiKeys.setGoogleKey("encrypted-key3");
    testApiKeys.setGrokKey("encrypted-key5");
    // testApiKeys.setHuggingfaceKey("encrypted-key8");
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));

    Set<ModelProvider> presentKeys = apiKeysService.getApiKeyPresentSet(testUser);

    assertEquals(3, presentKeys.size());
    assertTrue(presentKeys.contains(ModelProvider.OPENAI));
    assertFalse(presentKeys.contains(ModelProvider.MISTRAL));
    assertTrue(presentKeys.contains(ModelProvider.GOOGLE));
    assertFalse(presentKeys.contains(ModelProvider.ANTHROPIC));
    assertTrue(presentKeys.contains(ModelProvider.GROK));
    // assertFalse(presentKeys.contains(ModelProvider.OLLAMA));
    assertFalse(presentKeys.contains(ModelProvider.DEEPSEEK));
    // assertTrue(presentKeys.contains(ModelProvider.HUGGINGFACE));
    assertFalse(presentKeys.contains(ModelProvider.LLAMA));
    verify(apiKeysRepository).findByUser(testUser);
  }

  @Test
  void getApiKeyPresentSet_nonePresent() {
    // All keys are null by default in testApiKeys
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.of(testApiKeys));

    Set<ModelProvider> presentKeys = apiKeysService.getApiKeyPresentSet(testUser);

    assertTrue(presentKeys.isEmpty());
    verify(apiKeysRepository).findByUser(testUser);
  }

  @Test
  void getApiKeyPresentSet_noApiKeysRecord() {
    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.empty());

    Set<ModelProvider> presentKeys = apiKeysService.getApiKeyPresentSet(testUser);

    assertTrue(presentKeys.isEmpty());
    verify(apiKeysRepository).findByUser(testUser);
  }

  @Test
  void deleteByUser() {
    apiKeysService.deleteByUser(testUser);
    verify(apiKeysRepository).deleteByUser(testUser);
  }

  @Test
  void getApiKeyForUser_stringType_throwsUnsupportedOperationException() {
    UnsupportedOperationException exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> apiKeysService.getApiKeyForUser("OPENAI", testUser));

    assertEquals("Unimplemented method 'getApiKeyForUser'", exception.getMessage());
  }

  @Test
  void setApiKey_newUser_setsKeyCorrectly() {
    String rawKey = "test-key-new-user";
    ApiKeysRequestDTO request = new ApiKeysRequestDTO();
    request.setProvider(ModelProvider.GOOGLE);
    request.setKey(rawKey);
    String encryptedKey = "encrypted-" + rawKey;

    when(apiKeysRepository.findByUser(testUser)).thenReturn(Optional.empty());
    when(apiKeysRepository.saveAndFlush(any(ApiKeys.class)))
        .thenAnswer(
            invocation -> {
              ApiKeys savedKeys = invocation.getArgument(0);
              // Simulate ID generation if needed, or just return the argument
              return savedKeys;
            });

    apiKeysService.setApiKey(testUser, request.getProvider(), request.getKey());

    verify(apiKeysRepository)
        .saveAndFlush(
            argThat(
                apiKeys ->
                    apiKeys.getUser().equals(testUser)
                        && encryptedKey.equals(apiKeys.getGoogleKey())
                        && apiKeys.getOpenaiKey() == null
                        && apiKeys.getMistralKey() == null
                        && apiKeys.getAnthropicKey() == null));
    mockedEncryptionUtil.verify(() -> EncryptionUtil.encrypt(rawKey));
  }
}
