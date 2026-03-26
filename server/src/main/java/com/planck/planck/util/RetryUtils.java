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

import com.planck.planck.config.RetryConfig;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RetryUtils {
  public static <T> T executeWithRetry(SupplierWithStatus<T> action, RetryConfig config) {
    try {
      return executeWithRetryAsync(action, config).get();
    } catch (Exception e) {
      if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      }
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private static <T> CompletableFuture<T> executeWithRetryAsync(
      SupplierWithStatus<T> action, RetryConfig config) {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    RetryContext<T> context = new RetryContext<>(action, config, scheduler);

    return executeWithRetryInternal(context)
        .whenComplete(
            (result, throwable) -> {
              scheduler.shutdown();
            });
  }

  private static class RetryContext<T> {
    final SupplierWithStatus<T> action;
    final RetryConfig config;
    final ScheduledExecutorService scheduler;
    int attempt;
    long wait;
    Integer lastStatusCode;
    String errorMessage;

    RetryContext(
        SupplierWithStatus<T> action, RetryConfig config, ScheduledExecutorService scheduler) {
      this.action = action;
      this.config = config;
      this.scheduler = scheduler;
      this.attempt = 0;
      this.wait = config.getInitialInterval();
      this.lastStatusCode = null;
      this.errorMessage = null;
    }
  }

  private static <T> CompletableFuture<T> executeWithRetryInternal(RetryContext<T> context) {

    return CompletableFuture.supplyAsync(
            () -> {
              try {
                return context.action.get();
              } catch (RuntimeException e) {
                throw e;
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            },
            context.scheduler)
        .thenCompose(
            result -> {
              if (result.statusCode == 200) {
                return CompletableFuture.completedFuture(result.value);
              }

              context.lastStatusCode = result.statusCode;
              context.errorMessage = result.errorMessage;
              return CompletableFuture.failedFuture(new RuntimeException());
            })
        .handle(
            (result, throwable) -> {
              if (throwable != null) {
                if (!shouldRetry(context.lastStatusCode, context.attempt, context.config)) {
                  String errorMessage = "";
                  if (context.errorMessage != null) {
                    errorMessage = context.errorMessage;
                  } else {
                    errorMessage += throwable.getMessage();
                  }
                  throw new RuntimeException(errorMessage, throwable);
                }

                return retryInternal(context);
              }
              return CompletableFuture.completedFuture(result);
            })
        .thenCompose(future -> future);
  }

  private static <T> CompletableFuture<T> retryInternal(RetryContext<T> context) {
    context.attempt++;
    context.wait =
        Math.min(
            (long) (context.wait * context.config.getMultiplier()),
            context.config.getMaxInterval());

    return CompletableFuture.supplyAsync(
            () -> null,
            CompletableFuture.delayedExecutor(
                context.wait, TimeUnit.MILLISECONDS, context.scheduler))
        .thenCompose(ignored -> executeWithRetryInternal(context));
  }

  private static boolean shouldRetry(Integer statusCode, int attempt, RetryConfig config) {
    if (attempt + 1 >= config.getMaxAttempts()) return false;
    if (statusCode == null) return true;
    if (statusCode == 200) return false;
    if (config.getExcludeStatusCodes() != null
        && config.getExcludeStatusCodes().contains(statusCode)) {
      return false;
    }
    if (config.getIncludeStatusCodes() == null) {
      return true;
    }
    return config.getIncludeStatusCodes().contains(statusCode);
  }

  public interface SupplierWithStatus<T> {
    StatusResult<T> get() throws Exception;
  }

  public static class StatusResult<T> {
    public final T value;
    public final int statusCode;
    public final String errorMessage;

    public StatusResult(T value, int statusCode) {
      this.value = value;
      this.statusCode = statusCode;
      this.errorMessage = null;
    }

    public StatusResult(T value, int statusCode, String errorMessage) {
      this.value = value;
      this.statusCode = statusCode;
      this.errorMessage = errorMessage;
    }
  }
}
