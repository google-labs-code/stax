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

package com.planck.planck.domain.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import com.planck.planck.domain.evaluator.human.dto.HumanEvalScoreDTO;
import com.planck.planck.domain.model.dto.ModelTokens;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.Taggable;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.util.PromptUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SXSChatTurnWorkbookRowDTO implements Taggable {

  @JsonProperty("input")
  @Schema(description = "Input text for the model", example = "Hello, how are you?")
  private String modelInputText;

  @JsonProperty("raw_input")
  @Schema(
      description = "Raw input text for the model, without any variables reformatting",
      example = "Hello {{provider}}")
  private String rawModelInputText;

  @JsonProperty("output")
  @Schema(description = "Output text from the model", example = "I'm doing well, thank you!")
  private String modelOutputText;

  @JsonProperty("expected_output")
  @Schema(description = "Expected output text", example = "I'm doing great, thank you!")
  private String expectedOutput;

  @JsonProperty("model_response_id")
  @Schema(description = "Response id", example = "uuid4")
  private String response_id;

  @JsonProperty("chat_id")
  @Schema(description = "Chat id", example = "uuid4")
  private String chat_id;

  @JsonProperty("chat_turn_id")
  @Schema(description = "Chat turn id", example = "uuid4")
  private String chat_turn_id;

  @JsonProperty("created_at")
  @Schema(description = "Created at timestamp", example = "")
  private Long createdAt;

  @JsonProperty("updated_at")
  @Schema(description = "Updated at timestamp", example = "")
  private Long updatedAt;

  @JsonProperty("model_id")
  @Schema(description = "ID of the model", example = "uuid4")
  private String modelId;

  @JsonProperty("model_name")
  @Schema(description = "Name of the model", example = "gemini-2.5-flash")
  private String modelName;

  @Deprecated
  @JsonProperty("model_label")
  @Schema(description = "Label of the model", example = "Gemini 2.5 Flash")
  private String modelLabel;

  @JsonProperty("model_nickname")
  @Schema(description = "Nickname of the model", example = "Gemini 2.5 Flash")
  private String modelNickname;

  @JsonProperty("model_provider")
  @Schema(description = "Provider of the model", example = "GOOGLE")
  private String modelProvider;

  @JsonProperty("model_properties")
  @Schema(description = "Properties of the model", example = "")
  private String modelProperties;

  @JsonProperty("is_chat")
  @Schema(description = "Is this a chat", example = "true")
  private boolean isChat;

  @JsonProperty("inference_latency")
  @Schema(description = "Show latency of the model to run inference", example = "1000 ms")
  private Double inferenceLatency;

  @JsonProperty("inference_tokens")
  @Schema(
      description = "Show count of input , output and total tokens used for inference",
      example = "")
  private ModelTokens inferenceTokens;

  @JsonProperty("inference_status")
  @Schema(description = "Status of inference job related to the chat turn")
  private Integer inferenceStatus;

  @JsonProperty("inference_reason")
  @Schema(description = "Reason of inference job related to the chat turn")
  private String inferenceReason;

  @JsonProperty("tags")
  @Schema(description = "List of tags associated with the row")
  private List<TagDTO> tags;

  @JsonProperty("sequence")
  @Schema(description = "Chat Turn Sequence")
  private int sequence;

  @JsonProperty("human_eval_scores")
  @Schema(description = "List of human evaluation scores for this chat turn")
  private List<HumanEvalScoreDTO> humanEvalScores;

  @JsonProperty("llm_evaluations")
  @Schema(description = "LLM Scores grouped by evaluator name")
  private Map<String, LLMEvaluationDTO> llmEvaluations;

  @JsonProperty("system_instructions")
  @Schema(description = "System Instructions")
  private String systemInstructions;

  @JsonProperty("raw_system_instructions")
  @Schema(description = "Raw system instructions, without any variables reformatting")
  private String rawSystemInstruction;

  @JsonProperty("variables")
  @Schema(description = "Chat Variables")
  private Map<String, String> variables;

  public SXSChatTurnWorkbookRowDTO(ChatTurn chatTurn) {
    if (chatTurn == null) {
      return;
    }
    List<ModelInput> modelInputs = chatTurn.getInputs();

    setUserInputTexts(chatTurn);

    this.systemInstructions = chatTurn.getEffectiveSystemInstruction();
    if (this.systemInstructions == null && modelInputs != null) {
      ModelInput systemInput = getLastInputByRole(modelInputs, InputRole.SYSTEM);
      if (systemInput != null) {
        this.rawSystemInstruction = systemInput.getText();
        this.systemInstructions =
            PromptUtil.enrichTextForResponse(systemInput.getText(), systemInput.getVariables());
      }
    }

    this.modelOutputText =
        chatTurn.getModelResponse() == null ? "" : chatTurn.getModelResponse().getText();

    this.expectedOutput =
        (modelInputs == null || modelInputs.isEmpty())
            ? null
            : modelInputs.get(modelInputs.size() - 1).getExpectedOutput();

    this.chat_id = chatTurn.getChat().getId();
    this.variables = chatTurn.getChat().getVariables();
    this.chat_turn_id = chatTurn.getId();
    this.createdAt = chatTurn.getCreatedAt() == null ? null : chatTurn.getCreatedAt().getTime();
    this.updatedAt = chatTurn.getUpdatedAt() == null ? null : chatTurn.getUpdatedAt().getTime();

    if (chatTurn.getModelResponse() != null) {
      this.response_id = chatTurn.getModelResponse().getId();
      ModelResponse modelResponse = chatTurn.getModelResponse();
      Model model = modelResponse.getModel();
      if (model != null) {
        this.modelId = model.getId();
        this.modelName = model.getName();
        this.modelLabel = model.getLabel();
        this.modelNickname = model.getLabel();
        this.modelProvider = model.getProvider().toString();
        this.modelProperties = model.getProperties();
      }

      if (modelResponse.getInferenceMonitoring() != null) {
        InferenceMonitoring inferenceMonitoring = modelResponse.getInferenceMonitoring();
        this.inferenceLatency = inferenceMonitoring.getTurnTimeTaken();
        this.inferenceTokens =
            new ModelTokens(
                inferenceMonitoring.getTurnPromptTokens(),
                inferenceMonitoring.getTurnCompletionTokens(),
                inferenceMonitoring.getTurnTotalTokens());
      }

      if (modelResponse.getHumanEvalScores() != null
          && !modelResponse.getHumanEvalScores().isEmpty()) {
        this.humanEvalScores =
            modelResponse.getHumanEvalScores().stream()
                .map(HumanEvalScoreDTO::new)
                .collect(Collectors.toList());
      }
    }
    isChat = chatTurn.getSequenceId() >= 1;
    this.sequence = chatTurn.getSequenceId();
    if (chatTurn.getInferenceStatus() != null) {
      this.inferenceStatus = chatTurn.getInferenceStatus().getStatus();
      this.inferenceReason = chatTurn.getInferenceStatus().getReason();
    }
  }

  private void setUserInputTexts(ChatTurn chatTurn) {
    if (chatTurn == null) {
      return;
    }

    this.rawModelInputText = chatTurn.getRawInputText();
    this.modelInputText = chatTurn.getEnrichedInputText();
  }

  private ModelInput getLastInputByRole(List<ModelInput> modelInputs, InputRole role) {
    if (modelInputs == null) {
      return null;
    }
    return modelInputs.stream()
        .filter(input -> input.getRole().equals(role))
        .reduce((first, second) -> second)
        .orElse(null);
  }

  @Override
  public String getId() {
    return getChat_turn_id();
  }

  @Override
  public TagLinkTargetType getTagLinkTargetType() {
    return TagLinkTargetType.CHAT_TURN;
  }
}
