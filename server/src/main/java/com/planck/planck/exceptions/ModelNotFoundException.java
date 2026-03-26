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

/**
 * The specific AI model requested (e.g., for text generation, analysis) could not be found or is
 * unavailable.
 */
public class ModelNotFoundException extends RuntimeException {

  private static final String DEFAULT_MESSAGE =
      "Model Not Found: The requested AI model is unavailable. Please try a different model.";

  public ModelNotFoundException() {
    super(DEFAULT_MESSAGE);
  }

  public ModelNotFoundException(String message) {
    super(message);
  }
}
