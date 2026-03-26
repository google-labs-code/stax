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
import static org.mockito.Mockito.when;

import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.domain.authentication.service.GoogleAuthenticationService;
import com.planck.planck.domain.authentication.service.JwtService;
import com.planck.planck.domain.evaluator.heuristic.service.PointwiseHeuristicEvaluatorService;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.domain.user.UserServiceImpl;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthenticationServiceTest {

  @Mock private JwtService jwtService;

  @Mock private UserServiceImpl userService;

  @Mock private ProjectService projectService;

  @Mock private PointwiseHeuristicEvaluatorService heuristicEvaluatorService;

  @Mock private HumanEvaluatorService humanEvaluatorService;

  @InjectMocks private GoogleAuthenticationService googleAuthenticationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void doSigninTest() {

    String firstName = "firstName";
    String lastName = "lastName";
    String email = "abc@gmail.com";
    String jwt = "token";

    User user = new User(firstName, lastName, email, Role.USER);

    when(jwtService.generateToken(user)).thenReturn(jwt);

    JwtAuthenticationResponse result = googleAuthenticationService.doSignin(user);
    assertEquals(firstName, result.getFirstName());
    assertEquals(lastName, result.getLastName());
    assertEquals(email, result.getEmail());
    assertEquals(jwt, result.getToken());
  }

  @Test
  public void doSignupTest() {

    String firstName = "firstName";
    String lastName = "lastName";
    String fullName = firstName + " " + lastName;
    String email = "abc@gmail.com";

    JwtAuthenticationResponse result = googleAuthenticationService.doSignup(fullName, email);
    assertEquals(firstName, result.getFirstName());
    assertEquals(lastName, result.getLastName());
    assertEquals(email, result.getEmail());
  }

  @Test
  public void generateJwtAuthenticationResponseTest() {

    String firstName = "firstName";
    String lastName = "lastName";
    String email = "abc@gmail.com";
    String jwt = "token";

    User user = new User(firstName, lastName, email, Role.USER);

    when(jwtService.generateToken(user)).thenReturn(jwt);

    JwtAuthenticationResponse result =
        googleAuthenticationService.generateJwtAuthenticationResponse(user);
    assertEquals(firstName, result.getFirstName());
    assertEquals(lastName, result.getLastName());
    assertEquals(email, result.getEmail());
    assertEquals(jwt, result.getToken());
  }
}
