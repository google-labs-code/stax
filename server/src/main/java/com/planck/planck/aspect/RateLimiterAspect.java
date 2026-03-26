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

package com.planck.planck.aspect;

import com.planck.planck.annotation.RateLimited;
import com.planck.planck.entitities.User;
import com.planck.planck.exceptions.RateLimitExceedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RateLimiterAspect {

  private final Map<String, UserRequestInfo> requestCounts = new ConcurrentHashMap<>();

  @Pointcut("@annotation(com.planck.planck.annotation.RateLimited)")
  public void callAtRateLimited() {}

  @Around("callAtRateLimited()")
  public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("Rate limit Start");
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    RateLimited rateLimited = signature.getMethod().getAnnotation(RateLimited.class);
    int maxRequests = rateLimited.maxRequests();
    long windowSize = rateLimited.windowSizeInSeconds() * 1000; // Convert to milliseconds

    String emailId = getCurrentUserId();
    if (emailId == null) {
      log.info("User authentication failed.");
      throw new SecurityException("User authentication failed.");
    }

    String methodName = signature.getMethod().getName();
    String key = emailId + "-" + methodName;

    UserRequestInfo info = requestCounts.getOrDefault(key, new UserRequestInfo());

    long currentTime = System.currentTimeMillis();
    if (currentTime - info.getFirstRequestTime() > windowSize) {
      info.reset(currentTime);
    }

    if (info.getRequestCount() >= maxRequests) {
      log.info(
          "User not allowed more than {} requests in {} seconds", maxRequests, windowSize / 1000);
      throw new RateLimitExceedException(
          "User not allowed more than "
              + maxRequests
              + " requests in "
              + windowSize / 1000
              + " seconds");
    }
    info.incrementRequestCount();
    requestCounts.put(key, info);
    return joinPoint.proceed();
  }

  private String getCurrentUserId() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof User) {
      return ((User) principal).getEmail();
    } else {
      return principal.toString(); // This will always return String "anonymous user"
    }
  }

  @Getter
  private static class UserRequestInfo {
    private int requestCount;
    private long firstRequestTime;

    public UserRequestInfo() {
      this.requestCount = 0;
      this.firstRequestTime = System.currentTimeMillis();
    }

    public void incrementRequestCount() {
      this.requestCount++;
    }

    public void reset(long currentTime) {
      this.firstRequestTime = currentTime;
      this.requestCount = 1;
    }
  }
}
