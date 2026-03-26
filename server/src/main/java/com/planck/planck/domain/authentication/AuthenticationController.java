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

import com.planck.planck.domain.authentication.dto.GoogleAuthCodeRequest;
import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.domain.authentication.dto.TokenValidationResponse;
import com.planck.planck.domain.authentication.service.AuthenticationService;
import com.planck.planck.domain.authentication.service.GoogleAuthenticationService;
import com.planck.planck.util.JwtTokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Authentication",
    description = "Operations related to user authentication and authorization")
public class AuthenticationController {
  private Optional<GoogleAuthenticationService> googleAuthenticationService;
  private final JwtTokenUtils jwtTokenUtils;

  @Value("${auth.jwt.signingKey}")
  private String jwtSigningKey;

  @Autowired
  public AuthenticationController(
      Optional<AuthenticationService> authenticationService,
      Optional<GoogleAuthenticationService> googleAuthenticationService,
      JwtTokenUtils jwtTokenUtils) {
    this.googleAuthenticationService = googleAuthenticationService;
    this.jwtTokenUtils = jwtTokenUtils;
  }

  @PostMapping({"/google/auth-code"})
  @ConditionalOnBean(GoogleAuthenticationService.class)
  @Operation(
      summary = "Authenticate via Google OAuth Code",
      description =
          "Validates a Google OAuth 2.0 code and the redirect URI. Returns a JWT. Available only if GoogleAuthenticationService is configured.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Google authentication successful, JWT returned",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JwtAuthenticationResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Failed to validate Google auth code"),
        @ApiResponse(responseCode = "403", description = "Forbidden Access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<JwtAuthenticationResponse> validateGoogleToken(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Google authorization code and redirect URI",
              required = true,
              content = @Content(schema = @Schema(implementation = GoogleAuthCodeRequest.class)))
          @RequestBody
          @Validated
          GoogleAuthCodeRequest googleTokenRequest) {

    return ResponseEntity.ok(
        googleAuthenticationService
            .get()
            .validateGoogleAuthCode(
                googleTokenRequest.getAuthCode(), googleTokenRequest.getRedirectUrl()));
  }

  @GetMapping("/health")
  @Operation(
      summary = "Check service health",
      description = "A simple endpoint to verify that the service is running and responding.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Server is healthy",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(type = "string", example = "Server is healthy!"))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Server is healthy!");
  }

  @GetMapping("/validate-token")
  @Operation(
      summary = "Validate JWT Token",
      description =
          "Checks if a JWT token is valid and not expired. Returns token validation status.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Token validation completed - returns validation result",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TokenValidationResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - Invalid authorization header format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<TokenValidationResponse> validateToken(
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

    if (authorizationHeader == null
        || authorizationHeader.trim().isEmpty()
        || !authorizationHeader.startsWith("Bearer ")) {
      return ResponseEntity.badRequest()
          .body(new TokenValidationResponse(false, "Invalid authorization header format"));
    }

    String token = authorizationHeader.substring("Bearer ".length());

    try {
      // Get the signing key
      var signingKey = jwtTokenUtils.getSigningKey(jwtSigningKey);

      // Check if token is expired
      boolean isExpired = jwtTokenUtils.isTokenExpired(token, signingKey);

      if (isExpired) {
        return ResponseEntity.ok().body(new TokenValidationResponse(false, "Token is expired"));
      }

      // Token is valid and not expired
      return ResponseEntity.ok().body(new TokenValidationResponse(true, "Token is valid"));

    } catch (Exception e) {
      // Log the error for debugging purposes
      return ResponseEntity.ok().body(new TokenValidationResponse(false, "Invalid token"));
    }
  }
}
