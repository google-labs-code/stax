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
 * An attempt was made to create an 'Evaluator' resource with a name that is already in use by
 * another evaluator.
 */
@SuppressWarnings("serial")
public class EvaluatorNameExistException extends RuntimeException {

  private static final String DEFAULT_MESSAGE =
      "Name Exists: An evaluator with this name already exists. Please choose a different name.";

  public EvaluatorNameExistException() {
    super(DEFAULT_MESSAGE);
  }

  public EvaluatorNameExistException(String scorerName) {
    super(scorerName);
  }
}
