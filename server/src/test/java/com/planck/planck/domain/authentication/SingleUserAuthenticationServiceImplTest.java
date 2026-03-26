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

package com.planck.planck.domain.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.domain.authentication.dto.SignUpRequest;
import com.planck.planck.domain.authentication.service.JwtService;
import com.planck.planck.domain.authentication.service.SingleUserAuthenticationServiceImpl;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.domain.user.UserServiceImpl;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.Role;
import com.planck.planck.exceptions.EmailAlreadyExistsException;
import com.planck.planck.exceptions.UserNotFoundException;
import com.planck.planck.util.PlanckConstants;
import java.security.InvalidParameterException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

@ExtendWith(MockitoExtension.class)
public class SingleUserAuthenticationServiceImplTest {

  @Mock private UserRepository userRepository;

  @Mock private JwtService jwtService;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private UserServiceImpl userService;

  @InjectMocks private SingleUserAuthenticationServiceImpl authenticationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    authenticationService =
        new SingleUserAuthenticationServiceImpl(userRepository, jwtService, userService);
  }

  @Test
  void testSignup_Success() {

    SignUpRequest request =
        new SignUpRequest(
            PlanckConstants.DEFAULT_USER_FIRSTNAME,
            PlanckConstants.DEFAULT_USER_LASTNAME,
            PlanckConstants.DEFAULT_USER);
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

    JwtAuthenticationResponse response =
        authenticationService.signup(
            request.getEmail(), request.getFirstName(), request.getLastName());

    assertEquals(PlanckConstants.DEFAULT_USER_FIRSTNAME, response.getFirstName());
    assertEquals(PlanckConstants.DEFAULT_USER_LASTNAME, response.getLastName());
    assertEquals(PlanckConstants.DEFAULT_USER, response.getEmail());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testSignup_withNoDummyData() {

    SignUpRequest request =
        new SignUpRequest(
            PlanckConstants.DEFAULT_USER_FIRSTNAME,
            PlanckConstants.DEFAULT_USER_LASTNAME,
            PlanckConstants.DEFAULT_USER);
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

    JwtAuthenticationResponse response =
        authenticationService.signup(
            request.getEmail(), request.getFirstName(), request.getLastName());

    assertEquals(PlanckConstants.DEFAULT_USER_FIRSTNAME, response.getFirstName());
    assertEquals(PlanckConstants.DEFAULT_USER_LASTNAME, response.getLastName());
    assertEquals(PlanckConstants.DEFAULT_USER, response.getEmail());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testSignup_EmailAlreadyExists() {
    SignUpRequest request =
        new SignUpRequest(
            PlanckConstants.DEFAULT_USER_FIRSTNAME,
            PlanckConstants.DEFAULT_USER_LASTNAME,
            PlanckConstants.DEFAULT_USER);
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

    assertThrows(
        EmailAlreadyExistsException.class,
        () ->
            authenticationService.signup(
                request.getEmail(), request.getFirstName(), request.getLastName()));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testSignup_NonDefaultUser() {
    SignUpRequest request = new SignUpRequest("John", "Doe", "john.doe@example.com");

    assertThrows(
        InvalidParameterException.class,
        () ->
            authenticationService.signup(
                request.getEmail(), request.getFirstName(), request.getLastName()));
  }

  @Test
  void testSignin_Success() {
    User user =
        new User(
            PlanckConstants.DEFAULT_USER_FIRSTNAME,
            PlanckConstants.DEFAULT_USER_LASTNAME,
            PlanckConstants.DEFAULT_USER,
            Role.USER);
    when(userRepository.findByEmail(PlanckConstants.DEFAULT_USER)).thenReturn(Optional.of(user));
    when(jwtService.generateToken(user)).thenReturn("jwtToken");

    JwtAuthenticationResponse response = authenticationService.signin(PlanckConstants.DEFAULT_USER);

    assertEquals("jwtToken", response.getToken());
    verify(userService).setLastLoginTime(user);
  }

  @Test
  void testSignin_UserNotFound() {
    String defaultEmail = PlanckConstants.DEFAULT_USER;

    when(userRepository.existsByEmail(defaultEmail)).thenReturn(true);

    when(userRepository.findByEmail(defaultEmail)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> authenticationService.signin(defaultEmail));

    verify(userRepository).existsByEmail(defaultEmail);
    verify(userRepository).findByEmail(defaultEmail);
  }
}
