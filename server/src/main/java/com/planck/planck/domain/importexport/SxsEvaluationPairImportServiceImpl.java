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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluation.SXSHumanFeedbackRepository;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.evaluator.llm.service.NewLLMEvaluatorService;
import com.planck.planck.domain.importexport.dto.ImportContext;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.importexport.dto.SXSHumanEvalRatingExportDTO;
import com.planck.planck.domain.importexport.dto.SxsImportRequest;
import com.planck.planck.domain.importexport.dto.SxsProcessingResult;
import com.planck.planck.domain.importexport.dto.TurnCreationResult;
import com.planck.planck.domain.importexport.dto.record.DataRecord;
import com.planck.planck.domain.importexport.dto.record.RecordProvider;
import com.planck.planck.domain.model.service.ModelServiceImpl;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.domain.tags.TagService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.SXSHumanFeedback;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.enums.HumanSxsRating;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.ImportValidationException;
import com.planck.planck.util.ChatCreationHelper;
import com.planck.planck.util.ObjectMapperUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SxsEvaluationPairImportServiceImpl
    extends BaseImportService<SxsProcessingResult, ImportContext>
    implements SxsEvaluationPairImportService {

  private final ChatService chatService;
  private final SxsEvaluationPairRepository sxsEvaluationPairRepository;
  private final SXSHumanFeedbackRepository sxsHumanFeedbackRepository;
  private final TagLinkService tagLinkService;
  private final TagService tagService;
  private final NewLLMEvaluatorService llmEvaluatorService;
  private final EvaluationContainerService evaluationContainerService;
  private final List<RecordProvider> recordProviders;

  public SxsEvaluationPairImportServiceImpl(
      ChatService chatService,
      SxsEvaluationPairRepository sxsEvaluationPairRepository,
      SXSHumanFeedbackRepository sxsHumanFeedbackRepository,
      ModelServiceImpl modelService,
      HumanEvaluatorService humanEvaluatorService,
      TagService tagService,
      TagLinkService tagLinkService,
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
    this.sxsEvaluationPairRepository = sxsEvaluationPairRepository;
    this.sxsHumanFeedbackRepository = sxsHumanFeedbackRepository;
    this.tagService = tagService;
    this.tagLinkService = tagLinkService;
    this.llmEvaluatorService = llmEvaluatorService;
    this.evaluationContainerService = evaluationContainerService;
    this.recordProviders = recordProviders;
  }

  @Override
  @Transactional
  public ImportResultDTO importSxsPairs(SxsImportRequest request) {
    ImportContext context = prepareImportContext(request.user(), request.projectId(), request);
    return super.importFromFile(request.file(), context);
  }

  @Override
  protected SxsProcessingResult processRecord(DataRecord record, ImportContext context)
      throws IOException {

    SxsImportRequest request = (SxsImportRequest) context.request();

    String chatAJson = record.getValue(request.chatAColumnName());
    String chatBJson = record.getValue(request.chatBColumnName());

    if (chatAJson == null || chatAJson.isBlank()) {
      return createPairFromColumns(record, context);
    }

    TurnCreationResult resultA;
    Optional<TurnCreationResult> resultB;

    String rating = record.getValue(request.ratingColumnName());
    String expectedOutput = record.getValue(request.expectedOutputColumnName());
    Map<String, String> csvVariables =
        parseVariables(record.getValue(request.variablesColumnName()));

    resultA = createChatAndTurns(chatAJson, context, request.chatAColumnName());
    resultB = processOptionalChat(chatBJson, context);

    Chat chatA = resultA.turns().get(0).getChat();
    Optional<Chat> chatB = resultB.map(res -> res.turns().get(0).getChat());

    updateExpectedOutputForChats(expectedOutput, chatA, chatB);
    mergeVariablesIntoChats(csvVariables, chatA, chatB);

    SxsEvaluationPair pair =
        buildSxsEvaluationPair(context, expectedOutput, csvVariables, chatA, chatB);

    List<TurnCreationResult> allResults = new ArrayList<>();
    allResults.add(resultA);
    resultB.ifPresent(allResults::add);

    List<SXSHumanEvalRatingExportDTO> humanSxsRatings = parseHumanSxsRatings(rating);

    return new SxsProcessingResult(pair, allResults, humanSxsRatings);
  }

  @Override
  @Transactional
  protected void saveBatch(List<SxsProcessingResult> batchData, ImportContext context) {
    if (batchData.isEmpty()) {
      return;
    }

    List<ChatTurn> allTurnsInBatch =
        batchData.stream()
            .flatMap(result -> result.turnResults().stream())
            .flatMap(turnResult -> turnResult.turns().stream())
            .toList();
    List<ChatTurn> savedTurns = chatService.saveChat(allTurnsInBatch);

    List<SxsEvaluationPair> pairsToSave =
        batchData.stream().map(SxsProcessingResult::pair).toList();
    List<SxsEvaluationPair> savedPairs = sxsEvaluationPairRepository.saveAll(pairsToSave);

    Map<ChatTurn, ChatTurn> savedTurnsMap =
        IntStream.range(0, allTurnsInBatch.size())
            .boxed()
            .collect(Collectors.toMap(allTurnsInBatch::get, savedTurns::get));
    List<TagLink> allLinksToCreate = prepareTagLinksForBatch(batchData, savedTurnsMap, context);

    if (!allLinksToCreate.isEmpty()) {
      tagLinkService.saveAll(allLinksToCreate);
    }
    if (!context.tagCache().isEmpty()) {
      tagService.saveAll(new ArrayList<>(context.tagCache().values()));
    }

    List<SXSHumanFeedback> humanFeedbackToSave =
        createHumanFeedbackForBatch(batchData, savedPairs, savedTurnsMap, context);
    if (!humanFeedbackToSave.isEmpty()) {
      sxsHumanFeedbackRepository.saveAll(humanFeedbackToSave);
    }
  }

  private TurnCreationResult createChatAndTurns(
      String chatJson, ImportContext context, String columnName) throws IOException {
    if (chatJson == null || chatJson.isBlank()) {
      throw new IOException("Column '" + columnName + "' is mandatory and is missing or empty.");
    }

    List<LLMEvaluator> evaluators = context.evaluators();

    Chat chat = chatService.createChat(context.user(), context.sourceContainer());
    TurnCreationResult result =
        ChatCreationHelper.createTurnsFromJson(
            chatJson,
            chat,
            context.user(),
            context.modelMap(),
            context.defaultHumanEvaluator(),
            false,
            context.sourceContainer(),
            evaluators);

    if (result.turns().isEmpty()) {
      throw new IOException("Could not create turns from JSON for column '" + columnName + "'.");
    }
    IntStream.range(0, result.turns().size()).forEach(i -> result.turns().get(i).setSequenceId(i));
    chat.setTurns(result.turns());
    return result;
  }

  private Optional<TurnCreationResult> processOptionalChat(String chatJson, ImportContext context)
      throws IOException {
    if (chatJson == null || chatJson.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(createChatAndTurns(chatJson, context, "chatB"));
  }

  private void updateExpectedOutputForChats(
      String expectedOutput, Chat chatA, Optional<Chat> chatB) {
    if (expectedOutput != null && !expectedOutput.isBlank()) {
      updateLastTurnExpectedOutput(chatA, expectedOutput);
      chatB.ifPresent(b -> updateLastTurnExpectedOutput(b, expectedOutput));
    }
  }

  private void mergeVariablesIntoChats(
      Map<String, String> csvVariables, Chat chatA, Optional<Chat> chatB) {
    csvVariables.forEach(
        (key, value) -> {
          if (chatA.getVariables() != null) chatA.getVariables().putIfAbsent(key, value);
          chatB.ifPresent(
              b -> {
                if (b.getVariables() != null) b.getVariables().putIfAbsent(key, value);
              });
        });
  }

  private SxsEvaluationPair buildSxsEvaluationPair(
      ImportContext context,
      String expectedOutput,
      Map<String, String> csvVariables,
      Chat chatA,
      Optional<Chat> chatB) {
    SxsEvaluationPair pair = new SxsEvaluationPair();
    pair.setChatA(chatA);
    pair.setChatTurnA(chatA.getLatestTurn());
    chatB.ifPresent(
        b -> {
          pair.setChatB(b);
          pair.setChatTurnB(b.getLatestTurn());
        });

    Map<String, String> pairVariables = new HashMap<>();
    if (chatA.getVariables() != null) {
      pairVariables.putAll(chatA.getVariables());
    }
    pairVariables.putAll(csvVariables);

    pair.setContainer(context.sourceContainer());
    pair.setExpectedOutput(expectedOutput);
    pair.setVariables(pairVariables);
    pair.setUser(context.user());
    return pair;
  }

  private List<TagLink> prepareTagLinksForBatch(
      List<SxsProcessingResult> batchData,
      Map<ChatTurn, ChatTurn> savedTurnsMap,
      ImportContext context) {
    List<TagLink> allLinks = new ArrayList<>();
    for (SxsProcessingResult result : batchData) {
      for (TurnCreationResult turnResult : result.turnResults()) {
        List<ChatTurn> savedTurnsForResult =
            turnResult.turns().stream().map(savedTurnsMap::get).toList();
        allLinks.addAll(
            prepareTagLinks(turnResult.turnIndexToTagsMap(), savedTurnsForResult, context));
      }
    }
    return allLinks;
  }

  private List<SXSHumanFeedback> createHumanFeedbackForBatch(
      List<SxsProcessingResult> batchData,
      List<SxsEvaluationPair> savedPairs,
      Map<ChatTurn, ChatTurn> savedTurnsMap,
      ImportContext context) {
    List<SXSHumanFeedback> humanFeedbackToSave = new ArrayList<>();
    for (SxsProcessingResult result : batchData) {
      SxsEvaluationPair savedPair = savedPairs.get(batchData.indexOf(result));
      List<SXSHumanEvalRatingExportDTO> humanSxsRatings = result.humanSxsRatings();

      if (humanSxsRatings == null || humanSxsRatings.isEmpty()) {
        continue;
      }

      List<ChatTurn> sideATurns = result.turnResults().get(0).turns();
      List<ChatTurn> sideBTurns =
          result.turnResults().size() > 1 ? result.turnResults().get(1).turns() : new ArrayList<>();

      Map<Integer, ChatTurn> sequenceIdToSideATurn =
          sideATurns.stream()
              .collect(Collectors.toMap(ChatTurn::getSequenceId, savedTurnsMap::get));

      Map<Integer, ChatTurn> sequenceIdToSideBTurn =
          sideBTurns.stream()
              .collect(Collectors.toMap(ChatTurn::getSequenceId, savedTurnsMap::get));

      for (SXSHumanEvalRatingExportDTO rating : humanSxsRatings) {
        Integer sequenceId = rating.getSequenceId();
        ChatTurn sideATurn = sequenceIdToSideATurn.get(sequenceId);
        ChatTurn sideBTurn = sequenceIdToSideBTurn.get(sequenceId);

        if (sideATurn != null && sideBTurn != null) {
          SXSHumanFeedback feedback =
              SXSHumanFeedback.builder()
                  .pairId(savedPair.getId())
                  .projectId(savedPair.getContainer().getId())
                  .chatTurnA(sideATurn.getId())
                  .chatTurnB(sideBTurn.getId())
                  .humanSxsRating(rating.getRating())
                  .humanSxsNotes(rating.getNotes())
                  .user(context.user())
                  .build();

          humanFeedbackToSave.add(feedback);
        }
      }
    }
    return humanFeedbackToSave;
  }

  private void updateLastTurnExpectedOutput(Chat chat, String expectedOutput) {
    Optional.ofNullable(chat)
        .map(Chat::getLatestTurn)
        .map(ChatTurn::getLastUserInput)
        .ifPresent(userInput -> userInput.setExpectedOutput(expectedOutput));
  }

  private Map<String, String> parseVariables(String variablesJson) {
    if (variablesJson == null || variablesJson.isBlank()) {
      return Collections.emptyMap();
    }
    try {
      return new ObjectMapper().readValue(variablesJson, new TypeReference<>() {});
    } catch (IOException e) {
      log.warn("Could not parse variables JSON: {}", variablesJson, e);
      return Collections.emptyMap();
    }
  }

  private List<SXSHumanEvalRatingExportDTO> parseHumanSxsRatings(String humanSxsRatingsJson) {
    if (humanSxsRatingsJson == null || humanSxsRatingsJson.isBlank()) {
      return new ArrayList<>();
    }

    return ObjectMapperUtil.convertJsonStringToList(
        humanSxsRatingsJson, SXSHumanEvalRatingExportDTO.class);
  }

  private SxsProcessingResult createPairFromColumns(DataRecord record, ImportContext context) {

    SxsImportRequest request = (SxsImportRequest) context.request();
    String input = record.getValue(request.inputColumnName());
    String expectedOutput = record.getValue(request.expectedOutputColumnName());
    String commonTags = record.getValue(request.tagsColumnName());
    Map<String, String> csvVariables =
        parseVariables(record.getValue(request.variablesColumnName()));

    String systemInstructionA = record.getValue(request.systemInstructionAColumnName());
    String outputA = record.getValue(request.outputAColumnName());
    String modelLabelA = record.getValue(request.modelLabelAColumnName());
    String llmEvalA = record.getValue(request.llmEvaluationsAColumnName());
    String inferenceA = record.getValue(request.inferenceAnalyticsAColumnName());

    String systemInstructionB = record.getValue(request.systemInstructionBColumnName());
    String outputB = record.getValue(request.outputBColumnName());
    String modelLabelB = record.getValue(request.modelLabelBColumnName());
    String llmEvalB = record.getValue(request.llmEvaluationsBColumnName());
    String inferenceB = record.getValue(request.inferenceAnalyticsBColumnName());

    TurnCreationResult resultA =
        createSingleTurnChat(
            input,
            systemInstructionA,
            outputA,
            modelLabelA,
            expectedOutput,
            commonTags,
            llmEvalA,
            inferenceA,
            csvVariables,
            context);

    Optional<TurnCreationResult> resultB =
        (outputB == null || outputB.isBlank())
            ? Optional.empty()
            : Optional.of(
                createSingleTurnChat(
                    input,
                    systemInstructionB,
                    outputB,
                    modelLabelB,
                    expectedOutput,
                    commonTags,
                    llmEvalB,
                    inferenceB,
                    csvVariables,
                    context));

    Chat chatA = resultA.turns().get(0).getChat();
    Optional<Chat> chatB = resultB.map(res -> res.turns().get(0).getChat());

    mergeVariablesIntoChats(csvVariables, chatA, chatB);

    SxsEvaluationPair pair =
        buildSxsEvaluationPair(context, expectedOutput, csvVariables, chatA, chatB);

    List<TurnCreationResult> allResults = new ArrayList<>();
    allResults.add(resultA);
    resultB.ifPresent(allResults::add);

    String ratingStr = record.getValue(request.ratingColumnName());
    String notesStr = record.getValue(request.notesColumnName());
    List<SXSHumanEvalRatingExportDTO> humanSxsRatings =
        parseSingleHumanSxsRating(ratingStr, notesStr);

    return new SxsProcessingResult(pair, allResults, humanSxsRatings);
  }

  private TurnCreationResult createSingleTurnChat(
      String input,
      String systemInstruction,
      String output,
      String modelLabel,
      String expectedOutput,
      String tags,
      String llmEvaluationsJson,
      String inferenceAnalyticsJson,
      Map<String, String> variables,
      ImportContext context) {

    Chat chat = chatService.createChat(context.user(), context.sourceContainer());
    ChatTurn turn = new ChatTurn();
    turn.setChat(chat);
    turn.setUser(context.user());

    List<ModelInput> inputs = new ArrayList<>();
    if (systemInstruction != null && !systemInstruction.isBlank()) {
      inputs.add(
          ChatCreationHelper.createModelInput(
              systemInstruction, InputRole.SYSTEM, null, variables, context.user()));
    }
    inputs.add(
        ChatCreationHelper.createModelInput(
            input, InputRole.USER, expectedOutput, variables, context.user()));
    turn.setInputs(inputs);

    if (output != null && !output.isBlank()) {
      ModelResponse response = new ModelResponse();
      response.setText(output);
      response.setUser(context.user());
      Model model = null;
      if (modelLabel != null && context.modelMap().containsKey(modelLabel)) {
        model = context.modelMap().get(modelLabel);
        response.setModel(model);
      }

      addScores(response, llmEvaluationsJson, context.user(), context.evaluators());
      addInferenceAnalytics(response, turn, inferenceAnalyticsJson, model, context.user(), null);

      turn.setModelResponse(response);
    }

    turn.setSequenceId(0);
    chat.setTurns(List.of(turn));

    Map<Integer, String> turnIndexToTagsMap = new HashMap<>();
    if (tags != null && !tags.isBlank()) {
      turnIndexToTagsMap.put(0, tags);
    }

    return new TurnCreationResult(List.of(turn), turnIndexToTagsMap);
  }

  private List<SXSHumanEvalRatingExportDTO> parseSingleHumanSxsRating(
      String ratingStr, String notesStr) {
    if (ratingStr == null || ratingStr.isBlank()) {
      return Collections.emptyList();
    }

    try {
      HumanSxsRating ratingEnum = HumanSxsRating.valueOf(ratingStr.trim().toUpperCase());

      SXSHumanEvalRatingExportDTO ratingDto = new SXSHumanEvalRatingExportDTO();
      ratingDto.setRating(ratingEnum);
      ratingDto.setNotes(notesStr);
      ratingDto.setSequenceId(0);

      return Collections.singletonList(ratingDto);
    } catch (IllegalArgumentException e) {
      throw new ImportValidationException(
          "Invalid human rating value: '"
              + ratingStr
              + "'. Allowed values are: "
              + Arrays.toString(HumanSxsRating.values()));
    }
  }
}
