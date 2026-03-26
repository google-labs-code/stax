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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.authentication.service.JwtTokenProviderImpl;
import com.planck.planck.util.JwtTokenUtils;
import java.security.Key;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderImplTest {

  @Mock private JwtTokenUtils jwtTokenUtils;

  @Mock private UserDetails userDetails;

  @InjectMocks private JwtTokenProviderImpl jwtTokenProvider;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(
        jwtTokenProvider,
        "jwtSigningKey",
        Base64.getEncoder().encodeToString("test-signing-key-256-bits-long!".getBytes()));
    ReflectionTestUtils.setField(jwtTokenProvider, "jwtTokenValidity", 3600L); // 1 hour
  }

  @Test
  void testExtractUserName() {
    String token = "test-token";
    String expectedUsername = "user123";
    Key mockKey = mock(Key.class);

    when(jwtTokenUtils.getSigningKey(anyString())).thenReturn(mockKey);
    when(jwtTokenUtils.extractUserName(token, mockKey)).thenReturn(expectedUsername);

    String actual = jwtTokenProvider.extractUserName(token);

    assertEquals(expectedUsername, actual);
    verify(jwtTokenUtils).getSigningKey(anyString());
    verify(jwtTokenUtils).extractUserName(token, mockKey);
  }

  @Test
  void testGenerateToken() {
    Map<String, Object> claims = Map.of("claim1", "value1");
    String expectedToken = "jwt-token";
    Key mockKey = mock(Key.class);

    when(jwtTokenUtils.getSigningKey(anyString())).thenReturn(mockKey);
    when(jwtTokenUtils.generateToken(claims, userDetails, 3600L, mockKey))
        .thenReturn(expectedToken);

    String actual = jwtTokenProvider.generateToken(claims, userDetails);

    assertEquals(expectedToken, actual);
    verify(jwtTokenUtils).getSigningKey(anyString());
    verify(jwtTokenUtils).generateToken(claims, userDetails, 3600L, mockKey);
  }

  @Test
  void testIsTokenValid() {
    String token = "test-token";
    Key mockKey = mock(Key.class);

    when(jwtTokenUtils.getSigningKey(anyString())).thenReturn(mockKey);
    when(jwtTokenUtils.isTokenValid(token, userDetails, mockKey)).thenReturn(true);

    boolean valid = jwtTokenProvider.isTokenValid(token, userDetails);

    assertTrue(valid);
    verify(jwtTokenUtils).getSigningKey(anyString());
    verify(jwtTokenUtils).isTokenValid(token, userDetails, mockKey);
  }
}
