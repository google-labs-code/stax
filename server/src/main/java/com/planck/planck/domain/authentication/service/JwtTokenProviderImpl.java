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

import com.planck.planck.util.JwtTokenUtils;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProviderImpl implements TokenProvider {

  @Value("${auth.jwt.signingKey}")
  private String jwtSigningKey;

  @Value("${auth.jwt.validity}")
  private long jwtTokenValidity;

  private final JwtTokenUtils jwtTokenUtils;

  @Autowired
  public JwtTokenProviderImpl(JwtTokenUtils jwtTokenUtils) {
    this.jwtTokenUtils = jwtTokenUtils;
  }

  @Override
  public String extractUserName(String token) {
    return jwtTokenUtils.extractUserName(token, jwtTokenUtils.getSigningKey(jwtSigningKey));
  }

  @Override
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return jwtTokenUtils.generateToken(
        extraClaims, userDetails, jwtTokenValidity, jwtTokenUtils.getSigningKey(jwtSigningKey));
  }

  @Override
  public boolean isTokenValid(String token, UserDetails userDetails) {
    return jwtTokenUtils.isTokenValid(
        token, userDetails, jwtTokenUtils.getSigningKey(jwtSigningKey));
  }
}
