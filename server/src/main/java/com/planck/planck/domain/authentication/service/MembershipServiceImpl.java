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

import static com.planck.planck.util.PlanckConstants.ALLOW_ALL_ALLOWLIST_STRING;
import static com.planck.planck.util.PlanckConstants.CLOUD_IDENTITY_SCOPE;
import static com.planck.planck.util.PlanckConstants.GROUPS_API_URL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.planck.planck.config.WebClientConfig;
import com.planck.planck.util.PlanckEnvironmentUtil;
import java.util.Collections;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MembershipServiceImpl implements MembershipService {

  // Manually declare the logger
  private static final Logger log = LoggerFactory.getLogger(MembershipServiceImpl.class);

  private final WebClientConfig webClientConfig;
  private final ObjectMapper objectMapper;

  public MembershipServiceImpl(WebClientConfig webClientConfig, ObjectMapper objectMapper) {
    this.webClientConfig = webClientConfig;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean checkEmailIsAllowed(String email) {
    String[] allowlistDomains =
        PlanckEnvironmentUtil.getProperty("auth.allowlist.domains", String[].class);
    String[] allowlistGroupIds =
        PlanckEnvironmentUtil.getProperty("auth.allowlist.group_ids", String[].class);

    if (allowlistDomains == null) {
      allowlistDomains = new String[0];
    }
    if (allowlistGroupIds == null) {
      allowlistGroupIds = new String[0];
    }
    if (allowlistDomains.length == 0 && allowlistGroupIds.length == 0) {
      log.warn(
          "At least one of auth.allowlist.domains and auth.allowlist.group_ids should be configured. Denying user access.");
      return false;
    }

    // 1 - does the group allowlist contains the magic string `ALLOW_ALL_ALLOWLIST_STRING`?
    for (String groupId : allowlistGroupIds) {
      if (groupId != null && ALLOW_ALL_ALLOWLIST_STRING.equals(groupId.trim())) {
        return true;
      }
    }

    // 2 - is the email domain is in the domain allowlist?
    if (email == null || email.isEmpty()) {
      log.warn("checkEmailIsAllowed(email) received an empty email. Denying user access.");
      return false;
    }
    int domainIndex = email.indexOf('@');
    if (domainIndex > 0 && domainIndex < email.length() - 1) {
      String domain = email.substring(domainIndex + 1);
      for (String allowedDomain : allowlistDomains) {
        if (domain.equalsIgnoreCase(allowedDomain)) {
          return true;
        }
      }
    }

    // 3 - is the email in one of the groups?
    for (String groupId : allowlistGroupIds) {
      if (checkGroupMembership(email, groupId)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean checkGroupMembership(String email, String groupId) {
    String google_cloud_project_id =
        PlanckEnvironmentUtil.getProperty("spring.cloud.gcp.projectId");

    ResponseEntity<String> response = null;
    String url = null;
    try {
      // Use UriComponentsBuilder without encoding the query param value initially
      // Let RestTemplate handle the final encoding
      url =
          UriComponentsBuilder.fromHttpUrl(String.format(GROUPS_API_URL, groupId))
              .queryParam("query", "member_key_id == " + email)
              .build(false)
              .toUriString();

      HttpHeaders headers = new HttpHeaders();

      // Get credentials from Application Default Credentials (ADC)
      GoogleCredentials credentials =
          GoogleCredentials.getApplicationDefault()
              .createScoped(Collections.singletonList(CLOUD_IDENTITY_SCOPE));

      // Force a refresh to get a valid access token
      credentials.refreshIfExpired();

      // Set the Authorization header with the access token
      String accessToken = credentials.getAccessToken().getTokenValue();
      headers.set("Authorization", "Bearer " + accessToken);

      // Add the quota project header
      headers.set("x-goog-user-project", google_cloud_project_id);

      // Make the API call
      response =
          webClientConfig
              .restTemplate()
              .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

      // Parse the response
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        MembershipResponse membershipResponse =
            objectMapper.readValue(response.getBody(), MembershipResponse.class);
        return membershipResponse.isMember();
      } else {
        log.debug(
            "Membership check API call failed with status: {}. Response body: {}",
            response.getStatusCode(),
            response.getBody());
        return false;
      }
    } catch (HttpClientErrorException e) {
      // Catch client errors (4xx)
      log.error(
          "Client error during membership check for URL [{}]. Status: {}, Response: {}",
          url,
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      return false;
    } catch (HttpStatusCodeException e) {
      // Catch other HTTP status code errors (5xx, etc.)
      log.error(
          "HTTP error during membership check for URL [{}]. Status: {}, Response: {}",
          url,
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      return false;
    } catch (Exception e) {
      log.error("Unexpected error during membership check for URL [{}]. Error: {}", url, e);
      return false;
    }
  }

  @Data
  private static class MembershipResponse {

    @JsonProperty("hasMembership")
    private boolean member;

    public boolean isMember() {
      return member;
    }
  }
}
