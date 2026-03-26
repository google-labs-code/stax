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

package com.planck.planck.domain.chatturn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.evaluation.EvaluationScoreHelper;
import com.planck.planck.domain.evaluation.dto.EvaluationPointwiseHeuristicScoreHelper;
import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import com.planck.planck.domain.evaluation.dto.PointwiseHeuristicEvaluationDTO;
import com.planck.planck.domain.model.dto.ModelDTO;
import com.planck.planck.domain.modelinput.dto.ModelInputDTO;
import com.planck.planck.domain.modelresponse.ModelResponseDTO;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.Taggable;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.enums.TagLinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for a single Chat Turn")
@NoArgsConstructor
public class ChatTurnDTO implements Taggable {

  @JsonProperty("model_inputs")
  @Schema(description = "List of model inputs for this chat turn", required = true)
  private List<ModelInputDTO> inputs;

  @JsonProperty("model_output")
  @Schema(description = "Model response for this chat turn", required = true)
  private ModelResponseDTO response;

  @JsonProperty("model")
  @Schema(description = "Model used for this chat turn", required = true)
  private ModelDTO model;

  @JsonProperty("chat_turn_id")
  @Schema(description = "Unique identifier of the chat turn", required = true)
  private String id;

  @JsonProperty("created_at")
  @Schema(description = "Timestamp of when the chat turn was created", required = true)
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  @Schema(description = "Timestamp of when the chat turn was last updated", required = true)
  private Timestamp updatedAt;

  @JsonProperty("chat_id")
  @Schema(description = "Unique identifier of the chat this turn belongs to", required = true)
  private String chatId;

  @JsonProperty("sequence_id")
  @Schema(description = "Sequence number of the chat turn within the chat", required = true)
  private Integer sequenceId;

  @JsonProperty("evaluation_scores")
  @Schema(description = "Map of scores for different evaluators")
  private Map<String, String> scores = new HashMap<>();

  @JsonProperty("evaluation_statuses")
  @Schema(description = "Map of evaluation statuses for different evaluators")
  private Map<String, String> evaluationStatuses = new HashMap<>();

  @JsonProperty("inference_status")
  @Schema(description = "Status of inference job related to the chat turn")
  private Integer inferenceStatus;

  @JsonProperty("inference_reason")
  @Schema(description = "Reason of inference job related to the chat turn")
  private String inferenceReason;

  @JsonProperty("tags")
  @Schema(description = "List of tags associated with this chat turn")
  private List<TagDTO> tags;

  @JsonProperty("llm_evaluations")
  @Schema(description = "LLM Evaluation scores grouped by evaluator name")
  private Map<String, LLMEvaluationDTO> llmScores;

  @JsonProperty("pointwise_heuristic_evaluations")
  @Schema(description = "Pointwise Heuristic Evaluation scores grouped by evaluator name")
  private Map<String, PointwiseHeuristicEvaluationDTO> pointwiseHeuristicScores;

  public ChatTurnDTO(ChatTurn chatTurn) {
    if (chatTurn.getInputs() != null)
      this.inputs = chatTurn.getInputs().stream().map(ModelInputDTO::new).toList();

    if (chatTurn.getModelResponse() != null) {
      ModelResponse modelResponse = chatTurn.getModelResponse();
      this.response = new ModelResponseDTO(modelResponse);

      if (modelResponse.getModel() != null) this.model = new ModelDTO(modelResponse.getModel());

      if (modelResponse.getScores() != null && !modelResponse.getScores().isEmpty()) {
        EvaluationScoreHelper helper = new EvaluationScoreHelper(modelResponse.getScores());
        this.scores = helper.getScores();
        this.evaluationStatuses = helper.getEvaluationStatuses();
        this.llmScores = helper.getLLMScores();
      }

      if (modelResponse.getPointwiseHeuristicEvaluationScores() != null
          && !modelResponse.getPointwiseHeuristicEvaluationScores().isEmpty()) {
        EvaluationPointwiseHeuristicScoreHelper heuristicScoreHelper =
            new EvaluationPointwiseHeuristicScoreHelper(
                modelResponse.getPointwiseHeuristicEvaluationScores());
        this.evaluationStatuses.putAll(heuristicScoreHelper.getEvaluationStatuses());
        this.pointwiseHeuristicScores = heuristicScoreHelper.getPointwiseHeuristicScores();
      }
    }

    this.id = chatTurn.getId();
    this.createdAt = chatTurn.getCreatedAt();
    this.updatedAt = chatTurn.getUpdatedAt();
    this.chatId = chatTurn.getChat().getId();
    this.sequenceId = chatTurn.getSequenceId();

    if (chatTurn.getInferenceStatus() != null) {
      this.inferenceStatus = chatTurn.getInferenceStatus().getStatus();
      this.inferenceReason = chatTurn.getInferenceStatus().getReason();
    }
  }

  @Override
  public TagLinkTargetType getTagLinkTargetType() {
    return TagLinkTargetType.CHAT_TURN;
  }
}
