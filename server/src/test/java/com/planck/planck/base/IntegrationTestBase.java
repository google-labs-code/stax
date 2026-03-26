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

package com.planck.planck.base;

import com.planck.planck.domain.authentication.service.JwtService;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.user.UserService;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.Role;
import com.planck.planck.util.PlanckConstants;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest()
@ActiveProfiles("integration_test")
public abstract class IntegrationTestBase {
  @Autowired protected MockMvc mockMvc;

  @Autowired protected JwtService jwtService;
  @Autowired protected UserService userService;
  @Autowired protected HumanEvaluatorService humanEvaluatorService;

  protected String jwtToken;

  @BeforeEach
  public void setUp() {
    login();
  }

  protected String getBearerJwtToken() {
    return "Bearer " + jwtToken;
  }

  protected void login() {
    String email = PlanckConstants.DEFAULT_USER;
    String firstName = PlanckConstants.DEFAULT_USER_FIRSTNAME;
    String lastName = PlanckConstants.DEFAULT_USER_LASTNAME;
    User user =
        userService
            .findByEmail(email)
            .orElseGet(
                () -> {
                  User newUser = new User(firstName, lastName, email, Role.USER);
                  userService.save(newUser);
                  humanEvaluatorService.createUserThumbsEvaluator(newUser);
                  return newUser;
                });
    jwtToken = jwtService.generateToken(user);
  }
}
