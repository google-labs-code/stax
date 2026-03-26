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

import java.util.HashMap;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Setter
public class JwtServiceImpl implements JwtService {

  private final JwtTokenProviderImpl tokenProvider;

  public JwtServiceImpl(JwtTokenProviderImpl tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public String extractUserName(String token) {
    return tokenProvider.extractUserName(token);
  }

  @Override
  public String generateToken(UserDetails userDetails) {
    return tokenProvider.generateToken(new HashMap<>(), userDetails);
  }

  @Override
  public boolean isTokenValid(String token, UserDetails userDetails) {
    return tokenProvider.isTokenValid(token, userDetails);
  }
}
