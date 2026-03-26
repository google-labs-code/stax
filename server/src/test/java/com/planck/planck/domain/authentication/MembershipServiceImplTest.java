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

import static com.planck.planck.util.PlanckConstants.ALLOW_ALL_ALLOWLIST_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.planck.planck.config.WebClientConfig;
import com.planck.planck.domain.authentication.service.MembershipServiceImpl;
import com.planck.planck.util.PlanckEnvironmentUtil;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("MembershipServiceImpl Tests")
class MembershipServiceImplTest {

  @Mock private WebClientConfig webClientConfig;
  @Mock private RestTemplate restTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MembershipServiceImpl membershipService;

  private MockedStatic<PlanckEnvironmentUtil> mockedEnvUtil;
  private MockedStatic<GoogleCredentials> mockedGoogleCredentials;

  private static final String TEST_EMAIL = "user@example.com";
  private static final String TEST_EMAIL_DOMAIN = "example.com";
  private static final String TEST_GROUP_ID = "group-123@domain.com";
  private static final String TEST_PROJECT_ID = "test-gcp-project";

  @BeforeEach
  void setUp() {

    membershipService = new MembershipServiceImpl(webClientConfig, objectMapper);

    mockedEnvUtil = Mockito.mockStatic(PlanckEnvironmentUtil.class);
    mockedGoogleCredentials = Mockito.mockStatic(GoogleCredentials.class);

    lenient().when(webClientConfig.restTemplate()).thenReturn(restTemplate);
  }

  @AfterEach
  void tearDown() {

    mockedEnvUtil.close();
    mockedGoogleCredentials.close();
  }

  @Test
  @DisplayName("Should return false if allowlist group IDs and domains are not configured")
  void checkEmailIsAllowed_ReturnsFalse_WhenAllowlistNotConfigured() {

    mockedEnvUtil
        .when(() -> PlanckEnvironmentUtil.getProperty("auth.allowlist.group_ids", String[].class))
        .thenReturn(null);

    boolean isAllowed = membershipService.checkEmailIsAllowed(TEST_EMAIL);

    assertFalse(isAllowed);

    verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
  }

  @Test
  @DisplayName("Should return true if allowlist contains the wildcard character (*)")
  void checkEmailIsAllowed_ReturnsTrue_ForWildcardAllowlist() {

    String[] allowlist = {"some-group", " " + ALLOW_ALL_ALLOWLIST_STRING + " "};
    mockedEnvUtil
        .when(() -> PlanckEnvironmentUtil.getProperty("auth.allowlist.group_ids", String[].class))
        .thenReturn(allowlist);

    boolean isAllowed = membershipService.checkEmailIsAllowed(TEST_EMAIL);

    assertTrue(isAllowed);
    verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
  }

  @Test
  @DisplayName("Should return true when user is a member of the allowed group")
  void checkEmailIsAllowed_ReturnsTrue_WhenUserIsInGroup() throws IOException {

    setupPositiveMembershipCheck();

    boolean isAllowed = membershipService.checkEmailIsAllowed(TEST_EMAIL);

    assertTrue(isAllowed);

    ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate)
        .exchange(anyString(), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(String.class));

    HttpHeaders headers = httpEntityCaptor.getValue().getHeaders();
    assertEquals("Bearer fake-access-token", headers.getFirst("Authorization"));
    assertEquals(TEST_PROJECT_ID, headers.getFirst("x-goog-user-project"));
  }

  @Test
  @DisplayName("Should return false when user is not a member of any allowed group")
  void checkEmailIsAllowed_ReturnsFalse_WhenUserIsNotInGroup() throws IOException {

    setupNegativeMembershipCheck();

    boolean isAllowed = membershipService.checkEmailIsAllowed(TEST_EMAIL);

    assertFalse(isAllowed);
  }

  @Test
  @DisplayName("Should return true if the domain is in the allowlisted domains")
  void checkEmailIsAllowed_ReturnsTrue_WhenDomainIsAllowed() {

    mockedEnvUtil
        .when(() -> PlanckEnvironmentUtil.getProperty("auth.allowlist.domains", String[].class))
        .thenReturn(new String[] {"google.com", TEST_EMAIL_DOMAIN, "bing.com"});

    boolean isAllowed = membershipService.checkEmailIsAllowed(TEST_EMAIL);

    assertTrue(isAllowed);
  }

  @Test
  @DisplayName("Should return true if the domain is not in the allowlisted domains")
  void checkEmailIsAllowed_ReturnsFalse_WhenDomainIsNotAllowed() {

    mockedEnvUtil
        .when(() -> PlanckEnvironmentUtil.getProperty("auth.allowlist.domains", String[].class))
        .thenReturn(new String[] {"google.com", "bing.com"});

    boolean isAllowed = membershipService.checkEmailIsAllowed(TEST_EMAIL);

    assertFalse(isAllowed);

    verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
  }

  @Test
  @DisplayName("Should return false when membership API call fails with 4xx error")
  void checkEmailIsAllowed_ReturnsFalse_WhenApiCallFailsWithClientError() throws IOException {

    setupApiFailureCheck(HttpStatus.FORBIDDEN);

    boolean isAllowed = membershipService.checkEmailIsAllowed(TEST_EMAIL);

    assertFalse(isAllowed);
  }

  @Test
  @DisplayName("Should return false when Google Credentials cannot be obtained")
  void checkEmailIsAllowed_ReturnsFalse_WhenCredentialsFail() throws IOException {

    String[] allowlist = {TEST_GROUP_ID};
    mockedEnvUtil
        .when(() -> PlanckEnvironmentUtil.getProperty("auth.allowlist.group_ids", String[].class))
        .thenReturn(allowlist);
    mockedEnvUtil
        .when(() -> PlanckEnvironmentUtil.getProperty("spring.cloud.gcp.projectId"))
        .thenReturn(TEST_PROJECT_ID);

    mockedGoogleCredentials
        .when(GoogleCredentials::getApplicationDefault)
        .thenThrow(new IOException("ADC not found"));

    boolean isAllowed = membershipService.checkEmailIsAllowed(TEST_EMAIL);

    assertFalse(isAllowed);

    verify(webClientConfig, never()).restTemplate();
  }

  private void setupCommonMocks() throws IOException {
    String[] allowlist = {TEST_GROUP_ID};
    mockedEnvUtil
        .when(() -> PlanckEnvironmentUtil.getProperty("auth.allowlist.group_ids", String[].class))
        .thenReturn(allowlist);
    mockedEnvUtil
        .when(() -> PlanckEnvironmentUtil.getProperty("spring.cloud.gcp.projectId"))
        .thenReturn(TEST_PROJECT_ID);

    GoogleCredentials credentials = mock(GoogleCredentials.class);
    AccessToken accessToken = mock(AccessToken.class);
    mockedGoogleCredentials.when(GoogleCredentials::getApplicationDefault).thenReturn(credentials);
    when(credentials.createScoped(anyCollection())).thenReturn(credentials);
    when(credentials.getAccessToken()).thenReturn(accessToken);
    when(accessToken.getTokenValue()).thenReturn("fake-access-token");
  }

  private void setupPositiveMembershipCheck() throws IOException {
    setupCommonMocks();
    String jsonResponse = "{\"hasMembership\": true}";
    ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(responseEntity);
  }

  private void setupNegativeMembershipCheck() throws IOException {
    setupCommonMocks();
    String jsonResponse = "{\"hasMembership\": false}";
    ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(responseEntity);
  }

  private void setupApiFailureCheck(HttpStatus status) throws IOException {
    setupCommonMocks();
    when(restTemplate.exchange(
            anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenThrow(new HttpClientErrorException(status, "API Error"));
  }
}
