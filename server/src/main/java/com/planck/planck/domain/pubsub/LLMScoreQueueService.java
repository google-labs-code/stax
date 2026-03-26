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

package com.planck.planck.domain.pubsub;

import com.planck.planck.annotation.CheckJobActive;
import com.planck.planck.domain.evaluation.dto.EvaluationDTO;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusService;
import com.planck.planck.domain.job.AbstractScoreQueueService;
import com.planck.planck.domain.user.UserService;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.QueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Service;

@Slf4j
@Service("llmScoreQueueService")
public class LLMScoreQueueService extends AbstractScoreQueueService implements IScoreService {
  @Autowired protected UserService userService;

  @Autowired private PubsubOutboundEvalGateway messagingEvalGateway;

  @Autowired private EvaluationStatusService evaluationStatusService;

  @MessagingGateway
  public interface PubsubOutboundEvalGateway {

    @Gateway(requestChannel = QueueConstants.OPEN_AI_EVAL_PUB_CHANNEL)
    void sendToOpenAiEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.GEMINI_EVAL_PUB_CHANNEL)
    void sendToGeminiEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.MISTRAL_EVAL_PUB_CHANNEL)
    void sendToMistralEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.ANTHROPIC_EVAL_PUB_CHANNEL)
    void sendToAnthropicEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.GROK_EVAL_PUB_CHANNEL)
    void sendToGrokEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.OLLAMA_EVAL_PUB_CHANNEL)
    void sendToOllamaEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.DEEPSEEK_EVAL_PUB_CHANNEL)
    void sendToDeepseekEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.HUGGINGFACE_EVAL_PUB_CHANNEL)
    void sendToHuggingfaceEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.LLAMA_EVAL_PUB_CHANNEL)
    void sendToLlamaEval(String evaluationDTOJson);

    // BULK GATEWAYS
    @Gateway(requestChannel = QueueConstants.OPEN_AI_EVAL_PUB_CHANNEL_BULK)
    void sendToOpenAiEvalBulk(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.GEMINI_EVAL_PUB_CHANNEL_BULK)
    void sendToGeminiEvalBulk(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.MISTRAL_EVAL_PUB_CHANNEL_BULK)
    void sendToMistralEvalBulk(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.ANTHROPIC_EVAL_PUB_CHANNEL_BULK)
    void sendToAnthropicEvalBulk(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.GROK_EVAL_PUB_CHANNEL_BULK)
    void sendToGrokEvalBulk(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.OLLAMA_EVAL_PUB_CHANNEL_BULK)
    void sendToOllamaEvalBulk(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.DEEPSEEK_EVAL_PUB_CHANNEL_BULK)
    void sendToDeepseekEvalBulk(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.HUGGINGFACE_EVAL_PUB_CHANNEL_BULK)
    void sendToHuggingfaceEvalBulk(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.LLAMA_EVAL_PUB_CHANNEL_BULK)
    void sendToLlamaEvalBulk(String evaluationDTOJson);

    // ADD HERE FOR EVAL MODELS
  }

  @Override
  @CheckJobActive
  public void process(EvaluationDTO evaluationDTO) {
    try {
      EvaluationStatus currentEvaluationStatus =
          evaluationStatusService.findById(evaluationDTO.getEvaluationStatusId());

      if (EvaluationStatusEnum.getEvaluationStatusEnumByKey(currentEvaluationStatus.getStatus())
          == EvaluationStatusEnum.STOPPED) {
        log.info(
            "Skipping processing for Evaluation (id: {}), because its status is STOPPED.",
            currentEvaluationStatus.getId());
        return;
      }
      User user = userService.findByUserId(evaluationDTO.getUserId());

      evaluationService.generateScore(
          evaluationDTO.getPairId(),
          evaluationDTO.getChatTurnId(),
          evaluationDTO.getChatturnIdB(),
          evaluationDTO.getEvaluatorId(),
          evaluationDTO.getContainerId(),
          currentEvaluationStatus,
          user);

      log.info(
          "LLM Scorer Processing successfully completed. Chat turn Id: {} and Evaluation Status Id:{}",
          evaluationDTO.getChatTurnId(),
          evaluationDTO.getEvaluationStatusId());
      trackJobStatus(evaluationDTO);
    } catch (Exception e) {
      log.error("LLM Scorer Calculation failed due to Exception. Exception: {}", e);
      trackJobStatus(evaluationDTO);
    }
  }

  public void sendToQueue(
      EvaluationDTO evaluationDTO, ModelProvider modelProvider, boolean isBulk) {
    String evaluationDTOJson = ObjectMapperUtil.toJsonString(evaluationDTO);

    if (isBulk) {
      switch (modelProvider) {
        case OPENAI -> this.messagingEvalGateway.sendToOpenAiEvalBulk(evaluationDTOJson);
        case MISTRAL -> this.messagingEvalGateway.sendToMistralEvalBulk(evaluationDTOJson);
        case GOOGLE -> this.messagingEvalGateway.sendToGeminiEvalBulk(evaluationDTOJson);
        case ANTHROPIC -> this.messagingEvalGateway.sendToAnthropicEvalBulk(evaluationDTOJson);
        case GROK -> this.messagingEvalGateway.sendToGrokEvalBulk(evaluationDTOJson);
        // case OLLAMA -> this.messagingEvalGateway.sendToOllamaEvalBulk(evaluationDTOJson);
        case DEEPSEEK -> this.messagingEvalGateway.sendToDeepseekEvalBulk(evaluationDTOJson);
        // case HUGGINGFACE ->
        // this.messagingEvalGateway.sendToHuggingfaceEvalBulk(evaluationDTOJson);
        case LLAMA -> this.messagingEvalGateway.sendToLlamaEvalBulk(evaluationDTOJson);
        default ->
            throw new IllegalArgumentException(
                "Unsupported Model Provider: "
                    + modelProvider
                    + ". "
                    + "Valid providers are: "
                    + java.util.Arrays.asList(ModelProvider.values()));
      }
    } else {
      switch (modelProvider) {
        case OPENAI -> this.messagingEvalGateway.sendToOpenAiEval(evaluationDTOJson);
        case MISTRAL -> this.messagingEvalGateway.sendToMistralEval(evaluationDTOJson);
        case GOOGLE -> this.messagingEvalGateway.sendToGeminiEval(evaluationDTOJson);
        case ANTHROPIC -> this.messagingEvalGateway.sendToAnthropicEval(evaluationDTOJson);
        case GROK -> this.messagingEvalGateway.sendToGrokEval(evaluationDTOJson);
        // case OLLAMA -> this.messagingEvalGateway.sendToOllamaEval(evaluationDTOJson);
        case DEEPSEEK -> this.messagingEvalGateway.sendToDeepseekEval(evaluationDTOJson);
        // case HUGGINGFACE -> this.messagingEvalGateway.sendToHuggingfaceEval(evaluationDTOJson);
        case LLAMA -> this.messagingEvalGateway.sendToLlamaEval(evaluationDTOJson);
        default ->
            throw new IllegalArgumentException(
                "Unsupported Model Provider: "
                    + modelProvider
                    + ". "
                    + "Valid providers are: "
                    + java.util.Arrays.asList(ModelProvider.values()));
      }
    }
  }
}
