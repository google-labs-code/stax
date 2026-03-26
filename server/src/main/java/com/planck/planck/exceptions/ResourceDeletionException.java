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

package com.planck.planck.exceptions;

public class ResourceDeletionException extends RuntimeException {
  private final Class<?> resourceClass;
  private final String resourceId;

  public ResourceDeletionException(
      Class<?> resourceClass, String resourceId, String message, Throwable cause) {
    super(message, cause);
    this.resourceClass = resourceClass;
    this.resourceId = resourceId;
  }

  public Class<?> getResourceClass() {
    return resourceClass;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getResourceName() {
    return resourceClass.getSimpleName();
  }
}
