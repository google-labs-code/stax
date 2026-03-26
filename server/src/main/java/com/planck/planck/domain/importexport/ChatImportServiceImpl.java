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

package com.planck.planck.domain.importexport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.evaluator.llm.service.NewLLMEvaluatorService;
import com.planck.planck.domain.importexport.dto.ChatImportRequest;
import com.planck.planck.domain.importexport.dto.ImportContext;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.importexport.dto.TurnCreationResult;
import com.planck.planck.domain.importexport.dto.record.DataRecord;
import com.planck.planck.domain.importexport.dto.record.RecordProvider;
import com.planck.planck.domain.model.service.ModelServiceImpl;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.domain.tags.TagService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.ImportValidationException;
import com.planck.planck.util.ChatCreationHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatImportServiceImpl extends BaseImportService<TurnCreationResult, ImportContext>
    implements ChatImportService {

  private final ChatService chatService;
  private final TagService tagService;
  private final TagLinkService tagLinkService;
  private final ModelServiceImpl modelService;
  private final HumanEvaluatorService humanEvaluatorService;
  private final ApplicationLimits applicationLimits;
  private final NewLLMEvaluatorService llmEvaluatorService;
  private final EvaluationContainerService evaluationContainerService;
  private final List<RecordProvider> recordProviders;

  public ChatImportServiceImpl(
      ChatService chatService,
      TagService tagService,
      TagLinkService tagLinkService,
      ModelServiceImpl modelService,
      HumanEvaluatorService humanEvaluatorService,
      ApplicationLimits applicationLimits,
      NewLLMEvaluatorService llmEvaluatorService,
      EvaluationContainerService evaluationContainerService,
      List<RecordProvider> recordProviders) {
    super(
        tagService,
        tagLinkService,
        modelService,
        humanEvaluatorService,
        llmEvaluatorService,
        evaluationContainerService,
        recordProviders);
    this.chatService = chatService;
    this.tagService = tagService;
    this.tagLinkService = tagLinkService;
    this.modelService = modelService;
    this.humanEvaluatorService = humanEvaluatorService;
    this.applicationLimits = applicationLimits;
    this.llmEvaluatorService = llmEvaluatorService;
    this.evaluationContainerService = evaluationContainerService;
    this.recordProviders = recordProviders;
  }

  @Override
  @Transactional
  public <T extends EvaluationContainer> ImportResultDTO importChatsFromFile(
      ChatImportRequest request) {

    ImportContext context = prepareImportContext(request.user(), request.sourceId(), request);

    return super.importFromFile(request.file(), context);
  }

  @Override
  protected TurnCreationResult processRecord(DataRecord record, ImportContext context)
      throws IOException {

    ChatImportRequest request = (ChatImportRequest) context.request();

    Chat chat = chatService.createChat(context.user(), context.sourceContainer());
    Map<String, String> metadata = parseMetadata(record, request.metadataColumnNames());

    if (metadata.size() > applicationLimits.getMaxVariableKeysPerChat()) {
      throw new IllegalStateException(
          "Exceeded metadata key limit. Maximum allowed is "
              + applicationLimits.getMaxVariableKeysPerChat()
              + ", but found "
              + metadata.size());
    }
    chat.setVariables(metadata);

    List<ChatTurn> turnsForChat = new ArrayList<>();
    Map<Integer, String> tagsForChat = new HashMap<>();

    boolean useColumnsForLastTurn =
        record.getValue(request.inputColumnName()) != null
            || record.getValue(request.outputColumnName()) != null
            || record.getValue(request.expectedOutputColumnName()) != null;

    String chatJson = record.getValue(request.chatColumnName());

    List<LLMEvaluator> evaluators = llmEvaluatorService.getAllLLMScorer(context.user());

    if (chatJson != null && !chatJson.isBlank()) {

      TurnCreationResult jsonTurnsResult =
          ChatCreationHelper.createTurnsFromJson(
              chatJson,
              chat,
              context.user(),
              context.modelMap(),
              context.defaultHumanEvaluator(),
              useColumnsForLastTurn,
              context.sourceContainer(),
              evaluators);

      turnsForChat.addAll(jsonTurnsResult.turns());
      tagsForChat.putAll(jsonTurnsResult.turnIndexToTagsMap());
    }

    if (useColumnsForLastTurn) {
      InferenceMonitoring previousInferenceMonitoring =
          turnsForChat.stream()
              .map(ChatTurn::getModelResponse)
              .filter(Objects::nonNull)
              .map(ModelResponse::getInferenceMonitoring)
              .filter(Objects::nonNull)
              .reduce((first, second) -> second)
              .orElse(null);

      TurnCreationResult columnTurnResult =
          createTurnFromRowColumns(
              record, chat, context, request, previousInferenceMonitoring, evaluators);

      if (!columnTurnResult.turns().isEmpty()) {
        if (columnTurnResult.turnIndexToTagsMap().containsKey(0)) {
          tagsForChat.put(turnsForChat.size(), columnTurnResult.turnIndexToTagsMap().get(0));
        }
        turnsForChat.addAll(columnTurnResult.turns());
      }
    }

    if (turnsForChat.isEmpty()) {
      return null;
    }

    IntStream.range(0, turnsForChat.size()).forEach(i -> turnsForChat.get(i).setSequenceId(i));

    return new TurnCreationResult(turnsForChat, tagsForChat);
  }

  @Override
  @Transactional
  protected void saveBatch(List<TurnCreationResult> batchData, ImportContext context) {
    List<ChatTurn> allTurnsInBatch =
        batchData.stream().flatMap(result -> result.turns().stream()).toList();
    if (allTurnsInBatch.isEmpty()) return;
    List<ChatTurn> savedTurns = chatService.saveChat(allTurnsInBatch);
    Map<ChatTurn, ChatTurn> savedTurnsMap =
        IntStream.range(0, allTurnsInBatch.size())
            .boxed()
            .collect(Collectors.toMap(allTurnsInBatch::get, savedTurns::get));

    List<TagLink> allLinksToCreate = new ArrayList<>();
    for (TurnCreationResult result : batchData) {
      List<ChatTurn> savedTurnsForResult = result.turns().stream().map(savedTurnsMap::get).toList();
      allLinksToCreate.addAll(
          prepareTagLinks(result.turnIndexToTagsMap(), savedTurnsForResult, context));
    }
    if (!allLinksToCreate.isEmpty()) tagLinkService.saveAll(allLinksToCreate);
    if (!context.tagCache().isEmpty())
      tagService.saveAll(new ArrayList<>(context.tagCache().values()));
  }

  private Map<String, String> parseMetadata(DataRecord record, List<String> metadataColumnNames) {
    if (metadataColumnNames == null || metadataColumnNames.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, String> combinedMetadata = new HashMap<>();
    for (String columnName : metadataColumnNames) {
      String columnValue = record.getValue(columnName);
      if (columnValue == null || columnValue.isBlank()) continue;
      try {
        Map<String, String> jsonMeta =
            OBJECT_MAPPER.readValue(columnValue, new TypeReference<>() {});
        combinedMetadata.putAll(jsonMeta);
      } catch (IOException e) {
        combinedMetadata.put(columnName, columnValue);
      }
    }
    return combinedMetadata;
  }

  private TurnCreationResult createTurnFromRowColumns(
      DataRecord record,
      Chat chat,
      ImportContext context,
      ChatImportRequest request,
      InferenceMonitoring previousInferenceMonitoring,
      List<LLMEvaluator> evaluators) {

    String inputContent = record.getValue(request.inputColumnName());
    String systemInstruction = record.getValue(request.systemInstructionColumnName());
    String expectedOutput = record.getValue(request.expectedOutputColumnName());
    String tags = record.getValue(request.tagsColumnName());

    List<ModelInput> inputs = new ArrayList<>();
    if (systemInstruction != null && !systemInstruction.isBlank()) {
      inputs.add(
          ChatCreationHelper.createModelInput(
              systemInstruction, InputRole.SYSTEM, null, null, context.user()));
    }
    inputs.add(
        ChatCreationHelper.createModelInput(
            inputContent, InputRole.USER, expectedOutput, null, context.user()));

    ChatTurn turn = new ChatTurn();
    turn.setChat(chat);
    turn.setUser(context.user());
    turn.setInputs(inputs);

    addModelResponse(turn, record, context, request, previousInferenceMonitoring, evaluators);

    Map<Integer, String> turnIndexToTagsMap = new HashMap<>();
    if (tags != null && !tags.isBlank()) {
      turnIndexToTagsMap.put(0, tags);
    }

    return new TurnCreationResult(Collections.singletonList(turn), turnIndexToTagsMap);
  }

  private void addModelResponse(
      ChatTurn turn,
      DataRecord record,
      ImportContext context,
      ChatImportRequest request,
      InferenceMonitoring previousInferenceMonitoring,
      List<LLMEvaluator> evaluators) {

    String outputContent = record.getValue(request.outputColumnName());
    if (outputContent == null || outputContent.isBlank()) {}

    String modelLabel = record.getValue(request.modelLabelColumnName());
    String humanEvalScoreStr = record.getValue(request.humanEvalScoreColumnName());
    String inferenceAnalyticsStr = record.getValue(request.inferenceAnalyticsColumnName());
    String llmEvaluationsStr = record.getValue(request.llmEvaluationsColumnName());

    ModelResponse response = new ModelResponse();
    response.setText(outputContent);
    response.setUser(context.user());

    response.setContainer(context.sourceContainer());

    Model model = null;
    if (modelLabel != null && context.modelMap().containsKey(modelLabel)) {
      model = context.modelMap().get(modelLabel);
      response.setModel(model);
    }

    addHumanEvaluation(
        response, humanEvalScoreStr, context.defaultHumanEvaluator(), context.user());
    addInferenceAnalytics(
        response, turn, inferenceAnalyticsStr, model, context.user(), previousInferenceMonitoring);
    addScores(response, llmEvaluationsStr, context.user(), evaluators);

    turn.setModelResponse(response);
  }

  private void addHumanEvaluation(
      ModelResponse response,
      String humanEvalScoreStr,
      HumanEvaluator defaultHumanEvaluator,
      User user) {
    Integer evalValue = null;
    if (humanEvalScoreStr != null && !humanEvalScoreStr.isBlank()) {
      try {
        evalValue = Integer.parseInt(humanEvalScoreStr.trim());
      } catch (NumberFormatException e) {
        throw new ImportValidationException(
            "HumanEvalScore is not a valid integer: '" + humanEvalScoreStr + "'");
      }
    }
    if (evalValue != null) {
      if (defaultHumanEvaluator == null) {
        throw new ImportValidationException(
            "Cannot assign HumanEvalScore because a default HumanEvaluator is not configured.");
      }
      response.setHumanEvalScores(
          new ArrayList<>(
              Collections.singletonList(
                  new HumanEvalScore((double) evalValue, response, defaultHumanEvaluator, user))));
    }
  }
}
