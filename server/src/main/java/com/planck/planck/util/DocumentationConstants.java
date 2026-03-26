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

public class DocumentationConstants {
  public static final String MODEL_PROPERTIES_EXAMPLE_VALUE =
      """
        {
            "seed": 0,
            "top_k": 0,
            "top_p": 0.95,
            "max_tokens": 8192,
            "temperature": 1
        }
    """;
  public static final String MODEL_DESCRIPTORS_EXAMPLE_VALUE =
      """
            {
            "seed": {
                "type": "integer",
                "label": "Seed",
                "editable": true,
                "max_value": 2147483647,
                "min_value": -2147483648,
                "description": "Seed for the model.",
                "default_value": null
            },
            "top_p": {
                "type": "double",
                "label": "Top P",
                "editable": false,
                "max_value": 1.0,
                "min_value": 0.0,
                "description": "Top P for the model.",
                "default_value": 0.95
            },
            "temperature": {
                "type": "double",
                "label": "Temperature",
                "editable": false,
                "max_value": 2.0,
                "min_value": 0.0,
                "description": "Temperature for the model.",
                "default_value": 1.0
            },
            "stop_sequences": {
                "type": "string_array",
                "label": "Stop Sequences",
                "editable": true,
                "max_value": null,
                "min_value": null,
                "description": "Stop Sequences for the model.",
                "default_value": null
            },
            "max_output_tokens": {
                "type": "integer",
                "label": "Max Output Tokens",
                "editable": true,
                "max_value": 8192,
                "min_value": 1,
                "description": "Max Output Tokens for the model.",
                "default_value": 8192
            }
            }
            """;

  // SXS Generate Outputs Documentation
  public static final String SXS_GENERATE_OUTPUTS_DESCRIPTION =
      """
      Generate outputs for Side-by-Side (SxS) evaluation pairs using different strategies.

      This endpoint processes SxS pairs and generates model outputs based on the specified mode.
      Each mode handles different scenarios for when and how to generate outputs.

      **Available Modes:**

      **MISSING_ONLY**: Generates outputs only for chat turns that don't have any model response.
      - Skips pairs that already have outputs
      - Requires at least one model (modelA or modelB) to be specified
      - Ideal for initial output generation without affecting existing results

      **MISSING_AND_MATCHING**: Generates outputs for missing turns AND reruns matching models.
      - Generates outputs for turns without responses
      - Reruns inference for turns where the existing model matches the input model
      - Preserves existing outputs for non-matching models
      - Useful for updating specific model outputs while keeping others intact

      **RUN_OR_RERUN**: Always runs inference with the provided models, clearing existing outputs.
      - Clears all existing model outputs before running inference
      - Always uses the explicitly provided models (modelA/modelB)
      - Overwrites any previous outputs regardless of model match
      - Best for complete regeneration with new models or parameters

      **DUPLICATE**: Creates new SxS pairs for model mismatches and generates outputs.
      - Creates duplicate pairs when existing model doesn't match input model
      - Generates outputs for the new duplicate pairs
      - Preserves original pairs with their existing outputs
      - Useful for comparing different models without losing previous results

      **AUTORESOLVE**: Automatically determines the best action for each scenario.
      - **Missing outputs**: Generates with input model if provided, otherwise skips
      - **Model matches**: Clears and reruns with input model
      - **Model mismatches**: Creates duplicate pairs and runs with input model
      - **No input model but existing model**: Runs with existing model if model response is empty (e.g. it previously failed)
      - Most intelligent mode that handles all scenarios automatically

      **Response includes:**
      - `newSxsPairIds`: IDs of newly created pairs (for DUPLICATE and AUTORESOLVE modes)
      - `failedSxsPairIds`: IDs of pairs that failed validation
      - `skippedSxsPairIds`: IDs of pairs that were skipped during processing
      """;
}
