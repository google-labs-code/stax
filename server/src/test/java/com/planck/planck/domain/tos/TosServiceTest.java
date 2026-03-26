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

package com.planck.planck.domain.tos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.domain.authentication.service.JwtServiceImpl;
import com.planck.planck.domain.user.UserServiceImpl;
import com.planck.planck.entitities.Tos;
import com.planck.planck.entitities.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TosServiceTest {

  @Mock private TosRepository tosRepository;

  @Mock private UserServiceImpl userService;

  @Mock private JwtServiceImpl jwtService;

  @Mock private TosTokenProviderImpl tokenProvider;

  @InjectMocks private TosServiceImpl tosService;

  @Mock private User mockUser;

  @Mock private UserDetails mockUserDetails;

  @BeforeEach
  void setUp() {
    // Set the TOS_TYPE value for testing
    ReflectionTestUtils.setField(tosService, "tosType", "tt");
  }

  @Test
  @DisplayName("getActiveTos should return active ToS when one exists")
  void getActiveTos_whenActiveExists_returnsOptionalOfTos() {
    Tos activeTos = new Tos("Active Content");
    activeTos.setId("tos-123");
    activeTos.setIsActive(true);
    when(tosRepository.findFirstByIsActiveTrueAndTypeOrderByCreatedAtDesc("tt"))
        .thenReturn(Optional.of(activeTos));

    Optional<Tos> result = tosService.getActiveTos();

    assertTrue(result.isPresent());
    assertEquals(activeTos, result.get());
    verify(tosRepository, times(1)).findFirstByIsActiveTrueAndTypeOrderByCreatedAtDesc("tt");
  }

  @Test
  @DisplayName("getActiveTos should return empty Optional when no active ToS exists")
  void getActiveTos_whenNoneActive_returnsEmptyOptional() {
    when(tosRepository.findFirstByIsActiveTrueAndTypeOrderByCreatedAtDesc("tt"))
        .thenReturn(Optional.empty());

    Optional<Tos> result = tosService.getActiveTos();

    assertFalse(result.isPresent());
    verify(tosRepository, times(1)).findFirstByIsActiveTrueAndTypeOrderByCreatedAtDesc("tt");
  }

  @Test
  @DisplayName("getActiveTos should throw exception when TOS_TYPE is not configured")
  void getActiveTos_whenTosTypeNotConfigured_throwsException() {
    // Set tosType to null to simulate missing configuration
    ReflectionTestUtils.setField(tosService, "tosType", null);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> tosService.getActiveTos());
    assertEquals("TOS_TYPE environment variable is not configured", exception.getMessage());
  }

  @Test
  @DisplayName("getActiveTos should throw exception when TOS_TYPE is empty")
  void getActiveTos_whenTosTypeEmpty_throwsException() {
    // Set tosType to empty string to simulate empty configuration
    ReflectionTestUtils.setField(tosService, "tosType", "");

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> tosService.getActiveTos());
    assertEquals("TOS_TYPE environment variable is not configured", exception.getMessage());
  }

  @Test
  @DisplayName("createNewTos should save new active ToS and deactivate old active ToS")
  void createNewTos_whenOldActiveExists_savesNewAndDeactivatesOld() {
    String newContent = "New Terms v2";
    String type = "String";
    Tos newTosSaved = new Tos(newContent);
    newTosSaved.setId("tos-new");
    newTosSaved.setIsActive(true);

    ArgumentCaptor<Tos> tosCaptor = ArgumentCaptor.forClass(Tos.class);
    when(tosRepository.save(any(Tos.class)))
        .thenAnswer(
            invocation -> {
              Tos tosToSave = invocation.getArgument(0);
              if (newContent.equals(tosToSave.getContent())) {
                tosToSave.setId("tos-new-generated");
                return tosToSave;
              } else {
                return tosToSave;
              }
            });

    Tos result = tosService.createNewTos(newContent, type);

    assertNotNull(result);
    assertEquals(newContent, result.getContent());
    assertTrue(result.getIsActive());
    assertNotNull(result.getId());

    verify(tosRepository, times(1)).setPreviousTosAsInactive(type);
    verify(tosRepository, times(1)).save(tosCaptor.capture());

    Tos capturedNewTos = tosCaptor.getValue();
    assertTrue(capturedNewTos.getIsActive());
    assertEquals(newContent, capturedNewTos.getContent());
  }

  @Test
  @DisplayName("createNewTos should just save new active ToS if no old active ToS exists")
  void createNewTos_whenNoOldActive_savesNew() {
    String newContent = "First Ever Terms";
    String type = "String";
    Tos newTosSaved = new Tos(newContent);
    newTosSaved.setId("tos-first");
    newTosSaved.setIsActive(true);

    ArgumentCaptor<Tos> tosCaptor = ArgumentCaptor.forClass(Tos.class);
    when(tosRepository.save(tosCaptor.capture())).thenReturn(newTosSaved);

    Tos result = tosService.createNewTos(newContent, type);

    assertNotNull(result);
    assertEquals(newContent, result.getContent());
    assertTrue(result.getIsActive());
    assertEquals(newTosSaved.getId(), result.getId());

    verify(tosRepository, times(1)).setPreviousTosAsInactive(type);
    verify(tosRepository, times(1)).save(any(Tos.class));

    Tos capturedTos = tosCaptor.getValue();
    assertTrue(capturedTos.getIsActive());
    assertEquals(newContent, capturedTos.getContent());
  }

  @Test
  @DisplayName("deactivateTos should set active to false and save when ToS is found and active")
  void deactivateTos_whenFoundAndActive_setsInactiveAndSaves() {
    String tosId = "tos-to-deactivate";
    Tos activeTos = new Tos();
    activeTos.setId(tosId);
    activeTos.setIsActive(true);

    when(tosRepository.findById(tosId)).thenReturn(Optional.of(activeTos));
    ArgumentCaptor<Tos> tosCaptor = ArgumentCaptor.forClass(Tos.class);
    when(tosRepository.save(tosCaptor.capture())).thenReturn(activeTos);

    tosService.deactivateTos(tosId);

    verify(tosRepository, times(1)).findById(tosId);
    verify(tosRepository, times(1)).save(any(Tos.class));

    Tos capturedTos = tosCaptor.getValue();
    assertFalse(capturedTos.getIsActive());
    assertEquals(tosId, capturedTos.getId());
  }

  @Test
  @DisplayName("deactivateTos should do nothing if ToS is found but already inactive")
  void deactivateTos_whenFoundAndInactive_doesNothing() {
    String tosId = "tos-already-inactive";
    Tos inactiveTos = new Tos();
    inactiveTos.setId(tosId);
    inactiveTos.setIsActive(false);

    when(tosRepository.findById(tosId)).thenReturn(Optional.of(inactiveTos));

    tosService.deactivateTos(tosId);

    verify(tosRepository, times(1)).findById(tosId);
    verify(tosRepository, never()).save(any(Tos.class));
  }

  @Test
  @DisplayName("deactivateTos should throw RuntimeException when ToS is not found")
  void deactivateTos_whenNotFound_throwsRuntimeException() {
    String tosId = "tos-not-found";
    when(tosRepository.findById(tosId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> tosService.deactivateTos(tosId));
    assertEquals("Tos Not found", exception.getMessage());
    verify(tosRepository, times(1)).findById(tosId);
    verify(tosRepository, never()).save(any(Tos.class));
  }

  @Test
  @DisplayName(
      "acceptTos should update user, generate JWT and return response when ToS is valid and active")
  void acceptTos_validAndActiveTos_updatesUserAndReturnsResponse() {
    String tosId = "valid-tos-1";
    String userEmail = "accept@test.com";
    String generatedJwt = "mock.jwt.token";

    when(mockUser.getEmail()).thenReturn(userEmail);
    when(mockUser.getFirstName()).thenReturn("Accept");
    when(mockUser.getLastName()).thenReturn("User");

    Tos activeTos = new Tos();
    activeTos.setId(tosId);
    activeTos.setIsActive(true);
    activeTos.setContent("Terms V3");

    when(tosRepository.findById(tosId)).thenReturn(Optional.of(activeTos));
    doNothing().when(userService).addTosIdToUser(mockUser, tosId);
    when(jwtService.generateToken(mockUser)).thenReturn(generatedJwt);

    JwtAuthenticationResponse response = tosService.acceptTos(mockUser, tosId);

    assertNotNull(response);
    assertEquals(generatedJwt, response.getToken());
    assertEquals(userEmail, response.getEmail());
    assertEquals("Accept", response.getFirstName());
    assertEquals("User", response.getLastName());

    verify(tosRepository, times(1)).findById(tosId);
    verify(userService, times(1)).addTosIdToUser(mockUser, tosId);
    verify(jwtService, times(1)).generateToken(mockUser);
  }

  @Test
  @DisplayName("acceptTos should throw RuntimeException when ToS is not found")
  void acceptTos_tosNotFound_throwsRuntimeException() {
    String tosId = "non-existent-tos";
    when(tosRepository.findById(tosId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> tosService.acceptTos(mockUser, tosId));
    assertEquals("Invalid TOS version", exception.getMessage());
    verify(tosRepository, times(1)).findById(tosId);
    verify(userService, never()).addTosIdToUser(any(User.class), anyString());
    verify(jwtService, never()).generateToken(any(User.class));
  }

  @Test
  @DisplayName("acceptTos should throw RuntimeException when ToS is found but inactive")
  void acceptTos_tosFoundButInactive_throwsRuntimeException() {
    String tosId = "inactive-tos-2";
    Tos inactiveTos = new Tos();
    inactiveTos.setId(tosId);
    inactiveTos.setIsActive(false);

    when(tosRepository.findById(tosId)).thenReturn(Optional.of(inactiveTos));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> tosService.acceptTos(mockUser, tosId));
    assertEquals("Invalid TOS version", exception.getMessage());
    verify(tosRepository, times(1)).findById(tosId);
    verify(userService, never()).addTosIdToUser(any(User.class), anyString());
    verify(jwtService, never()).generateToken(any(User.class));
  }

  @Test
  @DisplayName("extractUserName should delegate to tokenProvider")
  void extractUserName_delegatesTotokenProvider() {
    String token = "test-token";
    String expectedUserName = "test-user";
    when(tokenProvider.extractUserName(token)).thenReturn(expectedUserName);

    String result = tosService.extractUserName(token);

    assertEquals(expectedUserName, result);
    verify(tokenProvider, times(1)).extractUserName(token);
  }

  @Test
  @DisplayName("generateToken should delegate to tokenProvider")
  void generateToken_delegatesTotokenProvider() {
    String expectedToken = "generated-token";
    when(tokenProvider.generateToken(any(), eq(mockUserDetails))).thenReturn(expectedToken);

    String result = tosService.generateToken(mockUserDetails);

    assertEquals(expectedToken, result);
    verify(tokenProvider, times(1)).generateToken(any(), eq(mockUserDetails));
  }
}
