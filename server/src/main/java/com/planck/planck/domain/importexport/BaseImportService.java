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

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.evaluator.llm.service.NewLLMEvaluatorService;
import com.planck.planck.domain.importexport.dto.ImportContext;
import com.planck.planck.domain.importexport.dto.ImportRequest;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.importexport.dto.InferenceAnalyticsDTO;
import com.planck.planck.domain.importexport.dto.record.DataRecord;
import com.planck.planck.domain.importexport.dto.record.RecordProvider;
import com.planck.planck.domain.model.service.ModelServiceImpl;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.domain.tags.TagService;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.enums.TagType;
import com.planck.planck.exceptions.ImportValidationException;
import com.planck.planck.util.ChatCreationHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseImportService<T, C> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int BATCH_SIZE = 50;

  protected final TagService tagService;
  protected final TagLinkService tagLinkService;
  protected final ModelServiceImpl modelService;
  protected final HumanEvaluatorService humanEvaluatorService;
  protected final NewLLMEvaluatorService llmEvaluatorService;
  protected final EvaluationContainerService evaluationContainerService;
  private final List<RecordProvider> recordProviders;
  protected static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP));

  protected BaseImportService(
      TagService tagService,
      TagLinkService tagLinkService,
      ModelServiceImpl modelService,
      HumanEvaluatorService humanEvaluatorService,
      NewLLMEvaluatorService llmEvaluatorService,
      EvaluationContainerService evaluationContainerService,
      List<RecordProvider> recordProviders) {
    this.tagService = tagService;
    this.tagLinkService = tagLinkService;
    this.modelService = modelService;
    this.humanEvaluatorService = humanEvaluatorService;
    this.llmEvaluatorService = llmEvaluatorService;
    this.evaluationContainerService = evaluationContainerService;
    this.recordProviders = recordProviders;
  }

  public ImportResultDTO importFromFile(MultipartFile file, C context) {
    logger.info(
        "Starting import for file: {}, service: {}",
        file.getOriginalFilename(),
        this.getClass().getSimpleName());

    String contentType = file.getContentType();
    RecordProvider provider =
        recordProviders.stream()
            .filter(p -> p.supports(contentType))
            .findFirst()
            .orElseThrow(
                () -> new UnsupportedOperationException("Unsupported file type: " + contentType));

    logger.info("Using record provider: {}", provider.getClass().getSimpleName());

    final ImportResultDTO result = new ImportResultDTO();
    List<T> batchData = new ArrayList<>();
    final AtomicInteger recordCounter = new AtomicInteger(0);

    try (Stream<DataRecord> records = provider.getRecords(file.getInputStream())) {
      records.forEach(
          record -> {
            result.incrementTotalRows();
            try {
              T processedData = processRecord(record, context);
              if (processedData != null) {
                batchData.add(processedData);
                result.incrementSuccessfulRows();
              } else {
                result.addFailure(
                    "No processable content found in the record.", record.getRecordNumber());
              }

              if (recordCounter.incrementAndGet() % BATCH_SIZE == 0 && !batchData.isEmpty()) {
                logger.info("Saving batch of {} items...", batchData.size());
                saveBatch(batchData, context);
                batchData.clear();
              }
            } catch (Exception e) {
              result.addFailure(
                  "Record " + record.getRecordNumber() + ": " + e.getMessage(),
                  record.getRecordNumber());
              logger.error(
                  "Failed to process record {} with an error: {}",
                  record.getRecordNumber(),
                  e.getMessage(),
                  e);
            }
          });

      if (!batchData.isEmpty()) {
        logger.info("Saving final batch of {} items...", batchData.size());
        saveBatch(batchData, context);
      }

    } catch (IOException e) {
      throw new RuntimeException("Critical error during file reading: " + e.getMessage(), e);
    }

    logger.info(
        "Import finished. Total records: {}, Successful: {}, Failed: {}",
        result.getTotalRows(),
        result.getSuccessfulRows(),
        result.getFailedRows());
    return result;
  }

  protected List<TagLink> prepareTagLinks(
      Map<Integer, String> turnIndexToTagsMap, List<ChatTurn> savedTurns, ImportContext context) {
    List<TagLink> allLinksToCreate = new ArrayList<>();
    for (Map.Entry<Integer, String> entry : turnIndexToTagsMap.entrySet()) {
      if (entry.getKey() >= savedTurns.size()) continue;
      ChatTurn targetTurn = savedTurns.get(entry.getKey());
      List<String> tagNames =
          Arrays.stream(entry.getValue().split(","))
              .map(String::trim)
              .filter(name -> !name.isEmpty())
              .distinct()
              .toList();
      if (tagNames.isEmpty()) continue;

      List<String> tagsToCreateNames =
          tagNames.stream().filter(name -> !context.tagCache().containsKey(name)).toList();
      if (!tagsToCreateNames.isEmpty()) {
        List<Tag> newTags =
            tagsToCreateNames.stream()
                .map(name -> new Tag(name, context.user(), TagType.USER))
                .toList();
        tagService.saveAll(newTags).forEach(tag -> context.tagCache().put(tag.getTagName(), tag));
      }

      for (String tagName : tagNames) {
        Tag tag = context.tagCache().get(tagName);
        if (tag != null) {
          allLinksToCreate.add(
              new TagLink(tag, targetTurn.getId(), TagLinkTargetType.CHAT_TURN, context.user()));
          tag.setUsedCount(tag.getUsedCount() + 1);
        }
      }
    }
    return allLinksToCreate;
  }

  public ImportContext prepareImportContext(User user, String sourceId, ImportRequest request) {

    List<LLMEvaluator> evaluators = llmEvaluatorService.getAllLLMScorer(user);

    EvaluationContainer sourceContainer =
        evaluationContainerService.getContainerForUser(user, sourceId);

    Map<String, Model> modelMap = modelService.buildModelMap(user, Model::getLabel);
    Map<String, Tag> tagCache =
        tagService.findAllUserTags(user).stream()
            .collect(Collectors.toMap(Tag::getTagName, Function.identity(), (a, b) -> a));
    HumanEvaluator defaultHumanEvaluator =
        humanEvaluatorService
            .getEvaluators(user, ScopeType.USER, ScoringMechanismType.CATEGORY)
            .stream()
            .findFirst()
            .orElse(null);

    return new ImportContext(
        request, user, sourceContainer, tagCache, modelMap, defaultHumanEvaluator, evaluators);
  }

  protected abstract T processRecord(DataRecord record, C context) throws IOException;

  protected abstract void saveBatch(List<T> batchData, C context);

  protected void addScores(
      ModelResponse response, String llmEvaluationsStr, User user, List<LLMEvaluator> evaluators) {
    if (llmEvaluationsStr == null || llmEvaluationsStr.isBlank()) {
      return;
    }
    Map<String, LLMEvaluationDTO> evaluationsMap;
    try {
      evaluationsMap = OBJECT_MAPPER.readValue(llmEvaluationsStr, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new ImportValidationException(
          "LLM Evaluations is not a valid object: '" + llmEvaluationsStr + "'");
    }
    if (evaluationsMap.isEmpty()) {
      return;
    }

    response.setScores(
        evaluationsMap.entrySet().stream()
            .map(
                evalDto ->
                    ChatCreationHelper.createScore(
                        evalDto, user, response.getContainer(), response, evaluators))
            .flatMap(Optional::stream)
            .collect(Collectors.toList()));
  }

  protected void addInferenceAnalytics(
      ModelResponse response,
      ChatTurn turn,
      String inferenceAnalyticsStr,
      Model model,
      User user,
      InferenceMonitoring previousInferenceMonitoring) {
    if (inferenceAnalyticsStr == null || inferenceAnalyticsStr.isBlank()) {
      return;
    }
    InferenceAnalyticsDTO inferenceAnalyticsDTO;
    try {
      inferenceAnalyticsDTO =
          OBJECT_MAPPER.readValue(inferenceAnalyticsStr, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new ImportValidationException(
          "Inference Analytics is not a valid object: '" + inferenceAnalyticsStr + "'");
    }
    InferenceStatus status =
        ChatCreationHelper.createInferenceStatus(user, response.getContainer());
    status.setChatTurn(turn);
    turn.setInferenceStatus(status);

    InferenceMonitoring inferenceMonitoring =
        new InferenceMonitoring(model, user, response.getContainer(), previousInferenceMonitoring);
    if (previousInferenceMonitoring != null) {
      inferenceMonitoring.setCompletionTokens(
          inferenceAnalyticsDTO.getTotalChatCompletionTokens().intValue()
              - previousInferenceMonitoring.getChatTotalCompletionTokens());
      inferenceMonitoring.setPromptTokens(
          inferenceAnalyticsDTO.getTotalChatPromptTokens().intValue()
              - previousInferenceMonitoring.getChatTotalPromptTokens());
      inferenceMonitoring.setTimetaken(
          inferenceAnalyticsDTO.getTotalChatLatency().intValue()
              - previousInferenceMonitoring.getTurnTimeTaken());
      inferenceMonitoring.setTotalTokens(
          inferenceAnalyticsDTO.getTotalChatTokens().intValue()
              - previousInferenceMonitoring.getChatTotalTokens());
    } else {
      inferenceMonitoring.setCompletionTokens(
          inferenceAnalyticsDTO.getTotalChatCompletionTokens().intValue());
      inferenceMonitoring.setPromptTokens(
          inferenceAnalyticsDTO.getTotalChatPromptTokens().intValue());
      inferenceMonitoring.setTimetaken(inferenceAnalyticsDTO.getTotalChatLatency());
      inferenceMonitoring.setTotalTokens(inferenceAnalyticsDTO.getTotalChatTokens().intValue());
    }

    response.setInferenceMonitoring(inferenceMonitoring);
  }
}
