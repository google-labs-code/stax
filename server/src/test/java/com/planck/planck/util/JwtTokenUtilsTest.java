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

package com.planck.planck.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class JwtTokenUtilsTest {

  private JwtTokenUtils jwtTokenUtils;
  private Key signingKey;
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    jwtTokenUtils = new JwtTokenUtils();
    signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("testuser");
  }

  @Test
  void testGenerateAndValidateToken() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "USER");

    String token = jwtTokenUtils.generateToken(claims, userDetails, 3600, signingKey);

    assertNotNull(token);
    assertTrue(jwtTokenUtils.isTokenValid(token, userDetails, signingKey));
    assertEquals("testuser", jwtTokenUtils.extractUserName(token, signingKey));
  }

  @Test
  void testExtractClaim() {
    String token = jwtTokenUtils.generateToken(new HashMap<>(), userDetails, 3600, signingKey);
    String subject = jwtTokenUtils.extractClaim(token, Claims::getSubject, signingKey);

    assertEquals("testuser", subject);
  }

  @Test
  void testIsTokenExpired() {
    Claims claims = mock(Claims.class);
    when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() - 1000)); // expired

    JwtTokenUtils utils = spy(jwtTokenUtils);
    doReturn(claims).when(utils).extractAllClaims(anyString(), eq(signingKey));

    boolean result = utils.isTokenExpired("dummy-token", signingKey);

    assertTrue(result);
  }

  @Test
  void testExtractExpiration() {
    String token = jwtTokenUtils.generateToken(new HashMap<>(), userDetails, 3600, signingKey);
    Date expiration = jwtTokenUtils.extractExpiration(token, signingKey);

    assertNotNull(expiration);
    assertTrue(expiration.after(new Date()));
  }

  @Test
  void testExtractAllClaims() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("customClaim", "customValue");

    String token = jwtTokenUtils.generateToken(claims, userDetails, 3600, signingKey);
    Claims extractedClaims = jwtTokenUtils.extractAllClaims(token, signingKey);

    assertEquals("customValue", extractedClaims.get("customClaim"));
  }

  @Test
  void testGetSigningKey() {
    String rawKey = "01234567890123456789012345678901";
    String base64Key = java.util.Base64.getEncoder().encodeToString(rawKey.getBytes());

    Key key = jwtTokenUtils.getSigningKey(base64Key);

    assertNotNull(key);
    assertEquals(SignatureAlgorithm.HS256.getJcaName(), key.getAlgorithm());
  }
}
