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

package com.planck.planck.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Defines the mode for generating outputs in side-by-side project")
public enum SXSGenerateOutputsMode {
  @Schema(
      description =
          "Generate outputs only for missing turns. Safe mode that won't affect existing results.")
  MISSING_ONLY,

  @Schema(
      description =
          "Generate outputs for missing turns AND rerun matching models. Preserves non-matching outputs.")
  MISSING_AND_MATCHING,

  @Schema(
      description =
          "Always clear existing outputs and run with provided models. Complete regeneration.")
  RUN_OR_RERUN,

  @Schema(
      description =
          "Create duplicate pairs for model mismatches. Preserves original pairs with existing outputs.")
  DUPLICATE,

  @Schema(
      description =
          "Automatically determine best action: generate missing, clear matches, duplicate mismatches.")
  AUTORESOLVE;
}
