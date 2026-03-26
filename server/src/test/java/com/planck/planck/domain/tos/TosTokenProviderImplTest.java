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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.planck.planck.util.JwtTokenUtils;
import java.security.Key;
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
class TosTokenProviderImplTest {

  @Mock private JwtTokenUtils jwtTokenUtils;

  @Mock private UserDetails userDetails;

  @InjectMocks private TosTokenProviderImpl tosTokenProvider;

  private final String signingKey = "superSecretSigningKeyWhichIsVeryLongAndSafeBase64";
  private final long validity = 3600L;
  private final String PREFIX = "tos-";

  private Key mockKey;

  @BeforeEach
  void setUp() {
    mockKey = mock(Key.class);

    ReflectionTestUtils.setField(tosTokenProvider, "tosSigningKey", signingKey);
    ReflectionTestUtils.setField(tosTokenProvider, "tosTokenValidity", validity);
  }

  @Test
  void testGenerateToken() {
    Map<String, Object> claims = Map.of("claim1", "value1");
    String expectedToken = "generated-jwt-token";

    when(jwtTokenUtils.getSigningKey(signingKey)).thenReturn(mockKey);
    when(jwtTokenUtils.generateToken(claims, userDetails, validity, mockKey))
        .thenReturn(expectedToken);

    String actualToken = tosTokenProvider.generateToken(claims, userDetails);

    assertEquals(PREFIX + expectedToken, actualToken);
    verify(jwtTokenUtils).generateToken(claims, userDetails, validity, mockKey);
  }

  @Test
  void testExtractUserName() {
    String rawToken = "raw-token";
    String fullToken = PREFIX + rawToken;
    String expectedUsername = "user123";

    when(jwtTokenUtils.getSigningKey(signingKey)).thenReturn(mockKey);
    when(jwtTokenUtils.extractUserName(rawToken, mockKey)).thenReturn(expectedUsername);

    String actualUsername = tosTokenProvider.extractUserName(fullToken);

    assertEquals(expectedUsername, actualUsername);
    verify(jwtTokenUtils).extractUserName(rawToken, mockKey);
  }

  @Test
  void testIsTokenValid() {
    String rawToken = "token-data";
    String fullToken = PREFIX + rawToken;

    when(jwtTokenUtils.getSigningKey(signingKey)).thenReturn(mockKey);
    when(jwtTokenUtils.isTokenValid(rawToken, userDetails, mockKey)).thenReturn(true);

    boolean result = tosTokenProvider.isTokenValid(fullToken, userDetails);

    assertTrue(result);
    verify(jwtTokenUtils).isTokenValid(rawToken, userDetails, mockKey);
  }
}
