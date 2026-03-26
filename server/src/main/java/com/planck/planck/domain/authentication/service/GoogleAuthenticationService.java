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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.domain.evaluator.heuristic.service.PointwiseHeuristicEvaluatorService;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.domain.user.UserService;
// import com.planck.planck.entitities.Tos;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.Role;
import com.planck.planck.enums.UserStatus;
import com.planck.planck.exceptions.GoogleAuthCodeException;
import com.planck.planck.exceptions.PermissionException;
// import com.planck.planck.service.tos.TosService;
import com.planck.planck.util.Util;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Service
@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "auth.google", name = "enabled", havingValue = "true")
public class GoogleAuthenticationService {

  @Value("${auth.google.client-id}")
  private String GOOGLE_CLIENT_ID;

  @Value("${auth.google.client-secret}")
  private String GOOGLE_CLIENT_SECRET;

  @Autowired private UserService userService;

  @Autowired private ProjectService projectService;

  @Autowired private JwtService jwtService;

  @Autowired private MembershipService membershipService;

  @Autowired private PointwiseHeuristicEvaluatorService heuristicEvaluatorService;

  @Autowired private HumanEvaluatorService humanEvaluatorService;

  // @Autowired private TosService tosService;

  // @Autowired private TosRepository tosRepository; // temp

  public JwtAuthenticationResponse validateGoogleAuthCode(String authCode, String redirectUrl) {
    Payload payload = exchangePayloadFromGoogleAuthCode(authCode, redirectUrl);
    String email = Util.normalizeEmail(payload.getEmail());
    // Check if email is allowed by membership rules
    boolean isAllowed = membershipService.checkEmailIsAllowed(email);
    if (!isAllowed) {
      throw new PermissionException("Access denied. Email address is not allowed.");
    }

    Optional<User> userOpt = userService.findByEmail(email);
    if (userOpt.isPresent()) {
      return doSignin(userOpt.get());
    } else {
      String name = (String) payload.get("name");
      return doSignup(name, email);
    }
  }

  public JwtAuthenticationResponse doSignin(User user) {

    user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
    user.setIsGoogleSignin(true);

    // Commented out TOS-related code
    // if (user.getAcceptedTosDetailsList() == null || user.getAcceptedTosDetailsList().isEmpty()) {
    //   return generateTosAuthenticationResponse(user);
    // }

    // if (tosService.getActiveTos().isEmpty()) {
    //   throw new GoogleAuthCodeException("No Active ToS Found");
    // }

    if (!user.isEnabled()) {
      throw new GoogleAuthCodeException("Your Stax account is disabled.");
    }

    if (user.getStatus() == UserStatus.PENDING_DELETION) {
      throw new GoogleAuthCodeException("Your Stax account is pending deletion.");
    }

    userService.save(user);
    return generateJwtAuthenticationResponse(user);
  }

  public JwtAuthenticationResponse doSignup(String firstName, String email) {

    // Handle null or empty name
    if (firstName == null || firstName.trim().isEmpty()) {
      firstName = "User"; // Default fallback name
    }

    String[] parts = firstName.trim().split(" ");
    String actualFirstName;
    String actualLastName;

    if (parts.length == 0 || parts[0].isEmpty()) {
      actualFirstName = "User";
      actualLastName = "";
    } else if (parts.length == 1) {
      actualFirstName = parts[0];
      actualLastName = "";
    } else {
      actualFirstName = parts[0];
      StringBuilder lastName = new StringBuilder();
      for (int i = 1; i < parts.length; i++) {
        lastName.append(parts[i]).append(" ");
      }
      actualLastName = lastName.toString().trim();
    }

    User user = new User(actualFirstName, actualLastName, email, Role.USER, true);
    userService.save(user);
    humanEvaluatorService.createUserThumbsEvaluator(user);
    heuristicEvaluatorService.createDefaultHeuristicEvaluators(user);
    projectService.createDefaultProject(user);
    return doSignin(user);
  }

  // Commented out TOS-related method
  // public JwtAuthenticationResponse generateTosAuthenticationResponse(User user) {
  //   var jwt = tosService.generateToken(user);
  //   Tos tos = null;
  //   Optional<Tos> tosOpt = tosService.getActiveTos();
  //   if (tosOpt.isPresent()) {
  //     tos = tosOpt.get();
  //   }

  //   if (tos == null) {
  //     throw new GoogleAuthCodeException("No Active Terms of Service Found");
  //   }
  //   return JwtAuthenticationResponse.builder()
  //       .token(jwt)
  //       .firstName(user.getFirstName())
  //       .lastName(user.getLastName())
  //       .email(user.getEmail())
  //       .tosContent(tos.getContent())
  //       .tosId(tos.getId())
  //       .status(403)
  //       .build();
  // }

  public JwtAuthenticationResponse generateJwtAuthenticationResponse(User user) {
    var jwt = jwtService.generateToken(user);
    return JwtAuthenticationResponse.builder()
        .token(jwt)
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .build();
  }

  private Payload exchangePayloadFromGoogleAuthCode(String authCode, String redirectUrl) {
    GoogleClientSecrets clientSecrets =
        new GoogleClientSecrets()
            .setInstalled(
                new GoogleClientSecrets.Details()
                    .setClientId(GOOGLE_CLIENT_ID)
                    .setClientSecret(GOOGLE_CLIENT_SECRET));

    try {
      GoogleTokenResponse tokenResponse =
          new GoogleAuthorizationCodeTokenRequest(
                  new NetHttpTransport(),
                  JacksonFactory.getDefaultInstance(),
                  "https://oauth2.googleapis.com/token",
                  clientSecrets.getDetails().getClientId(),
                  clientSecrets.getDetails().getClientSecret(),
                  authCode,
                  redirectUrl)
              .set("access_type", "online")
              .execute();

      GoogleIdToken idToken = tokenResponse.parseIdToken();
      return idToken.getPayload();
    } catch (IOException e) {
      // Log the full exception message and stack trace
      log.error("Error exchanging Google Auth Code: {}", e.getMessage(), e);
      throw new GoogleAuthCodeException("INVALID GOOGLE AUTHCODE");
    }
  }
}
