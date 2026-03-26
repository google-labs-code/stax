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

package com.planck.planck.domain.authentication.service;

import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.domain.user.UserServiceImpl;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.Role;
import com.planck.planck.exceptions.EmailAlreadyExistsException;
import com.planck.planck.exceptions.UserAccountDisabledException;
import com.planck.planck.exceptions.UserNotFoundException;
import com.planck.planck.util.PlanckConstants;
import com.planck.planck.util.Util;
import java.security.InvalidParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "auth.singleUser", name = "enabled", havingValue = "true")
public class SingleUserAuthenticationServiceImpl implements AuthenticationService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final UserServiceImpl userService;

  public SingleUserAuthenticationServiceImpl(
      UserRepository userRepository, JwtService jwtService, UserServiceImpl userService) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @Override
  public JwtAuthenticationResponse signup(String email, String firstName, String lastName) {

    email = Util.normalizeEmail(email);

    // Only supports authentication with a single user
    if (!email.equals(PlanckConstants.DEFAULT_USER)) {
      throw new InvalidParameterException("Please sign up with the default user account.");
    }

    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyExistsException();
    }

    User user = new User(firstName, lastName, email, Role.USER);
    userRepository.save(user);

    return JwtAuthenticationResponse.builder()
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .build();
  }

  @Override
  public JwtAuthenticationResponse signin(String email) {
    email = Util.normalizeEmail(email);

    // Only supports authentication with a single user
    if (!email.equals(PlanckConstants.DEFAULT_USER)) {
      throw new InvalidParameterException("Please sign in with the default user account.");
    }

    // Automatically sign up if user hasn't done so.
    if (!userRepository.existsByEmail(email)) {
      signup(
          PlanckConstants.DEFAULT_USER,
          PlanckConstants.DEFAULT_USER_FIRSTNAME,
          PlanckConstants.DEFAULT_USER_LASTNAME);
    }

    User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

    if (!user.isEnabled()) {
      log.info("User is disabled.");
      throw new UserAccountDisabledException();
    }

    var jwt = jwtService.generateToken(user);

    userService.setLastLoginTime(user);
    return JwtAuthenticationResponse.builder()
        .token(jwt)
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .build();
  }
}
