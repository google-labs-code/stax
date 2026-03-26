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

import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.domain.authentication.service.JwtServiceImpl;
import com.planck.planck.domain.user.UserServiceImpl;
import com.planck.planck.entitities.Tos;
import com.planck.planck.entitities.User;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TosServiceImpl implements TosService {

  private final TosRepository tosRepository;

  private final UserServiceImpl userService;

  private final JwtServiceImpl jwtService;

  private final TosTokenProviderImpl tokenProvider;

  @Value("${auth.tos.type}")
  private String tosType;

  public TosServiceImpl(
      TosRepository tosRepository,
      UserServiceImpl userService,
      JwtServiceImpl jwtService,
      TosTokenProviderImpl tokenProvider) {
    this.tosRepository = tosRepository;
    this.userService = userService;
    this.jwtService = jwtService;
    this.tokenProvider = tokenProvider;
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Tos> getActiveTos() {
    // Validate that TOS_TYPE is configured
    if (tosType == null || tosType.trim().isEmpty()) {
      throw new IllegalStateException("TOS_TYPE environment variable is not configured");
    }

    // Filter by TOS_TYPE environment variable to ensure we get the correct TOS for the application
    return tosRepository.findFirstByIsActiveTrueAndTypeOrderByCreatedAtDesc(tosType);
  }

  @Transactional
  @Override
  public Tos createNewTos(String content, String type) {
    // Create new TOS with the required content parameter
    Tos newTos = new Tos(content);
    newTos.setType(type);
    newTos.setIsActive(true);

    // Deactivate all existing active TOS entries of the same type
    tosRepository.setPreviousTosAsInactive(type);

    return tosRepository.save(newTos);
  }

  @Transactional
  @Override
  public void deactivateTos(String tosId) {
    Tos tos =
        tosRepository.findById(tosId).orElseThrow(() -> new RuntimeException("Tos Not found"));
    if (tos.getIsActive()) {
      tos.setIsActive(false);
      tosRepository.save(tos);
    }
  }

  @Override
  public String extractUserName(String token) {
    return tokenProvider.extractUserName(token);
  }

  @Override
  public String generateToken(UserDetails userDetails) {
    return tokenProvider.generateToken(new HashMap<>(), userDetails);
  }

  @Transactional
  @Override
  public JwtAuthenticationResponse acceptTos(User user, String tosId) {
    Optional<Tos> tos = tosRepository.findById(tosId);
    if (!tos.isEmpty() && tos.get().getIsActive()) {
      userService.addTosIdToUser(user, tosId);
      var jwt = jwtService.generateToken(user);

      return JwtAuthenticationResponse.builder()
          .token(jwt)
          .firstName(user.getFirstName())
          .lastName(user.getLastName())
          .email(user.getEmail())
          .build();
    }
    throw new RuntimeException("Invalid TOS version");
  }
}
