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

package com.planck.planck.config;

import com.planck.planck.domain.authentication.service.JwtService;
import com.planck.planck.domain.tos.TosService;
import com.planck.planck.domain.user.UserService;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.UserStatus;
import com.planck.planck.exceptions.UserAccessDeniedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  @Autowired @Lazy private JwtService jwtService;

  @Autowired private TosService tosService;

  @Value("${auth.enabled}")
  public Boolean authEnabled;

  @Value("${user.email}")
  public String defaultUser;

  private final UserService userService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (!authEnabled) {
      handleDefaultAuth(request);
      filterChain.doFilter(request, response);
      return;
    }

    // Skip JWT processing for /auth/validate-token endpoint to allow proper validation
    if (request.getRequestURI().equals("/auth/validate-token")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");
    if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring("Bearer".length() + 1);
      if (token.startsWith("tos-")) {
        // tos- token handling
        if (request.getRequestURI().startsWith("/tos/")) {
          processTosToken(token, request);
        } else {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          response.getWriter().write("Invalid endpoint for tos- token.");
          return; // Stop processing
        }
      } else {
        // Handle JWT tokens gracefully - don't throw exceptions for invalid tokens
        try {
          processJwtToken(token, request);
        } catch (Exception e) {
          // Log the error but don't block the request
          // The controller will handle invalid tokens appropriately
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  private void processJwtToken(String jwt, HttpServletRequest request) {
    final String userEmail = jwtService.extractUserName(jwt);
    if (StringUtils.isNotEmpty(userEmail)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

      checkUserStatus(userDetails);

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new CustomAuthority("AUTH_JWT"));
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }
  }

  private void processTosToken(String tosToken, HttpServletRequest request) {
    final String userEmail = tosService.extractUserName(tosToken);
    if (StringUtils.isNotEmpty(userEmail)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

      checkUserStatus(userDetails);

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new CustomAuthority("AUTH_TOS"));
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }
  }

  private void handleDefaultAuth(HttpServletRequest request) {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userService.userDetailsService().loadUserByUsername(defaultUser);

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new CustomAuthority("AUTH_JWT"));
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }
  }

  private void checkUserStatus(UserDetails userDetails) {
    if (userDetails instanceof User) {
      User user = (User) userDetails;
      if (UserStatus.PENDING_DELETION.equals(user.getStatus())) {
        throw new UserAccessDeniedException(
            user.getId(), "Account is pending deletion and access is not allowed");
      }
    }
  }
}
