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

package com.planck.planck.domain.modelinput.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.enums.InputRole;
import com.planck.planck.util.PromptUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(name = "ModelInput", description = "Data Transfer Object for a Model Input")
@JsonInclude(Include.NON_NULL)
public class ModelInputDTO implements Serializable {

  @JsonProperty("id")
  @Schema(description = "Unique identifier of the model input", required = true)
  private String id;

  @JsonProperty("parent_input_id")
  @Schema(description = "Unique identifier of the parent model input")
  private String parentInputId;

  @JsonProperty("text")
  @Schema(description = "Text content of the model input", required = true)
  private String text;

  @JsonProperty("raw_text")
  @Schema(
      description = "Raw text of the model input, without variable reformatting",
      required = false)
  private String rawText;

  @JsonProperty("role")
  @Schema(
      description = "Role of the model input",
      required = true,
      allowableValues = {"user", "system", "assistant", "function", "function_call", "tool"})
  private InputRole role;

  @JsonProperty("expected_output")
  @Schema(description = "Expected output for the model input")
  private String expectedOutput;

  @JsonProperty("variables")
  @Schema(description = "Variables associated with the model input")
  private Map<String, String> variables;

  @JsonProperty("created_at")
  @Schema(description = "Timestamp of when the model input was created", required = true)
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  @Schema(description = "Timestamp of when the model input was last updated", required = true)
  private Timestamp updatedAt;

  public ModelInputDTO(ModelInput modelInput) {
    this.id = modelInput.getId();
    ModelInput parentModelInput = modelInput.getModelInput();
    if (parentModelInput != null) {
      this.parentInputId = parentModelInput.getId();
    }
    this.rawText = modelInput.getText();
    this.text = PromptUtil.enrichTextForResponse(modelInput.getText(), modelInput.getVariables());
    this.role = modelInput.getRole();
    this.expectedOutput = modelInput.getExpectedOutput();
    this.variables = modelInput.getVariables();
    this.createdAt = modelInput.getCreatedAt();
    this.updatedAt = modelInput.getUpdatedAt();
  }
}
