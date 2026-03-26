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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.authentication.service.JwtServiceImpl;
import com.planck.planck.domain.authentication.service.JwtTokenProviderImpl;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

  @Mock private JwtTokenProviderImpl tokenProvider;

  @InjectMocks private JwtServiceImpl jwtService;

  @Mock private UserDetails mockUserDetails;

  @BeforeEach
  void setUp() {}

  @Test
  @DisplayName("extractUserName should delegate to tokenProvider")
  void extractUserName_delegatesTotokenProvider() {
    String token = "test-token";
    String expectedUserName = "test-user";
    when(tokenProvider.extractUserName(token)).thenReturn(expectedUserName);

    String result = jwtService.extractUserName(token);

    assertEquals(expectedUserName, result);
    verify(tokenProvider, times(1)).extractUserName(token);
  }

  @Test
  @DisplayName("generateToken should delegate to tokenProvider")
  void generateToken_delegatesTotokenProvider() {
    String expectedToken = "generated-token";
    when(tokenProvider.generateToken(any(HashMap.class), eq(mockUserDetails)))
        .thenReturn(expectedToken);

    String result = jwtService.generateToken(mockUserDetails);

    assertEquals(expectedToken, result);
    verify(tokenProvider, times(1)).generateToken(any(HashMap.class), eq(mockUserDetails));
  }

  @Test
  @DisplayName("isTokenValid should delegate to tokenProvider")
  void isTokenValid_delegatesTotokenProvider() {
    String token = "test-token";
    boolean expectedResult = true;
    when(tokenProvider.isTokenValid(token, mockUserDetails)).thenReturn(expectedResult);

    boolean result = jwtService.isTokenValid(token, mockUserDetails);

    assertEquals(expectedResult, result);
    verify(tokenProvider, times(1)).isTokenValid(token, mockUserDetails);
  }
}
