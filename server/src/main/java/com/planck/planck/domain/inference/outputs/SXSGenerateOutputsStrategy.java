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

package com.planck.planck.domain.inference.outputs;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inference.dto.SXSGenerateOutputsResponse;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.pubsub.InferencePublisherService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.JobStatusEnum;
import com.planck.planck.util.PlanckConstants;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base class for Side-by-Side (SxS) output generation strategies.
 *
 * <p>This class provides the common infrastructure and shared functionality for all SxS output
 * generation strategies. It handles the core workflow of generating outputs for SxS evaluation
 * pairs, including data initialization, validation, chat duplication, and queue management.
 *
 * <p>The strategy follows a template method pattern with the following workflow:
 *
 * <ol>
 *   <li><strong>Initialize Data</strong> - Load SxS pairs and set up internal state
 *   <li><strong>Validate Inputs</strong> - Check for required data and mark failed pairs
 *   <li><strong>Populate Missing Side Chats</strong> - Create side B chats if needed
 *   <li><strong>Duplicate Chats</strong> - Strategy-specific duplication logic
 *   <li><strong>Send to Queue</strong> - Strategy-specific queue processing
 * </ol>
 *
 * <p>This class manages all SxS-specific dependencies and provides protected access to services
 * through getter methods, ensuring proper encapsulation while allowing subclasses to access
 * required functionality.
 *
 * @author Planck Team
 */
public abstract class SXSGenerateOutputsStrategy
    extends GenerateOutputsStrategy<SXSGenerateOutputsResponse> {
  /** Model A for side-by-side comparison (can be null) */
  protected Model modelA;

  /** Model B for side-by-side comparison (can be null) */
  protected Model modelB;

  /** The project containing the SxS evaluation pairs */
  protected Project project;

  /** The user who owns the project */
  protected User user;

  /** List of SxS evaluation pairs to process */
  protected List<SxsEvaluationPair> sxsPairs;

  /** List of SxS pair IDs to process */
  protected List<String> sxsPairIds;

  /** IDs of newly created SxS pairs (for duplication strategies) */
  protected List<String> newSxsPairIds = new ArrayList<>();

  /** IDs of SxS pairs that failed validation */
  protected List<String> failedSxsPairIds = new ArrayList<>();

  /** IDs of SxS pairs that were skipped during processing */
  protected List<String> skippedSxsPairIds = new ArrayList<>();

  /**
   * Constructs a new SxS output generation strategy with the specified models and project.
   *
   * @param modelA the model for side A (can be null)
   * @param modelB the model for side B (can be null)
   * @param project the project containing the SxS evaluation pairs
   */
  public SXSGenerateOutputsStrategy(Model modelA, Model modelB, Project project) {
    super();
    this.modelA = modelA;
    this.modelB = modelB;
    this.project = project;
    this.user = project.getUser();
  }

  /** Service for chat operations and duplication */
  private ChatService chatService;

  /** Repository for SxS evaluation pair operations */
  private SxsEvaluationPairRepository sxsEvaluationPairRepository;

  /** Service for publishing inference requests */
  private InferencePublisherService publisherService;

  /** Service for job status management */
  private JobStatusService jobStatusService;

  /**
   * Sets the chat service dependency.
   *
   * @param chatService the chat service to use for operations
   */
  public void setChatService(ChatService chatService) {
    this.chatService = chatService;
  }

  /**
   * Sets the SxS evaluation pair repository dependency.
   *
   * @param sxsEvaluationPairRepository the repository to use for SxS pair operations
   */
  public void setSxsEvaluationPairRepository(
      SxsEvaluationPairRepository sxsEvaluationPairRepository) {
    this.sxsEvaluationPairRepository = sxsEvaluationPairRepository;
  }

  /**
   * Sets the inference publisher service dependency.
   *
   * @param publisherService the service to use for publishing inference requests
   */
  public void setPublisherService(InferencePublisherService publisherService) {
    this.publisherService = publisherService;
  }

  /**
   * Sets the job status service dependency.
   *
   * @param jobStatusService the service to use for job status management
   */
  public void setJobStatusService(JobStatusService jobStatusService) {
    this.jobStatusService = jobStatusService;
  }

  /**
   * Gets the chat service dependency.
   *
   * @return the chat service instance
   */
  protected ChatService getChatService() {
    return chatService;
  }

  /**
   * Gets the SxS evaluation pair repository dependency.
   *
   * @return the SxS evaluation pair repository instance
   */
  protected SxsEvaluationPairRepository getSxsEvaluationPairRepository() {
    return sxsEvaluationPairRepository;
  }

  /**
   * Gets the inference publisher service dependency.
   *
   * @return the inference publisher service instance
   */
  protected InferencePublisherService getPublisherService() {
    return publisherService;
  }

  /**
   * Gets the job status service dependency.
   *
   * @return the job status service instance
   */
  protected JobStatusService getJobStatusService() {
    return jobStatusService;
  }

  /**
   * Generates outputs for the specified SxS evaluation pairs.
   *
   * <p>This method orchestrates the complete output generation workflow:
   *
   * <ol>
   *   <li>Initializes data by loading SxS pairs
   *   <li>Validates inputs and marks failed pairs
   *   <li>Populates missing side B chats if needed
   *   <li>Executes strategy-specific duplication logic
   *   <li>Sends inference requests to the queue
   * </ol>
   *
   * <p>The response includes lists of new, failed, and skipped pair IDs to provide comprehensive
   * feedback about the operation's results.
   *
   * @param sxsPairIds the list of SxS pair IDs to process
   * @return a response containing the results of the output generation operation
   */
  @Override
  public SXSGenerateOutputsResponse generateOutputs(List<String> sxsPairIds) {
    initializeData(sxsPairIds);
    validateInputs();
    populateMissingSideChats();
    duplicateChats();
    sendToQueue();
    return SXSGenerateOutputsResponse.builder()
        .newSxsPairIds(newSxsPairIds)
        .failedSxsPairIds(failedSxsPairIds)
        .skippedSxsPairIds(skippedSxsPairIds)
        .build();
  }

  /**
   * Initializes the strategy's internal data structures.
   *
   * <p>This method sets up the strategy's internal state by storing the provided SxS pair IDs and
   * loading the corresponding SxS evaluation pairs from the database with eager fetching of chat A
   * data for optimal performance.
   *
   * @param sxsPairIds the list of SxS pair IDs to process
   */
  private void initializeData(List<String> sxsPairIds) {
    this.sxsPairIds = sxsPairIds;
    this.sxsPairs = getSxsEvaluationPairRepository().findByIdInWithChatAEagerFetching(sxsPairIds);
  }

  /**
   * Validates the input data and marks pairs that fail validation.
   *
   * <p>This method performs basic validation on all SxS pairs. Currently, it checks that each pair
   * has a valid chat A. Pairs that fail validation are added to the {@link #failedSxsPairIds} list
   * and will be excluded from further processing.
   *
   * <p>Subclasses can override this method to add additional validation logic specific to their
   * requirements.
   */
  @Override
  protected void validateInputs() {
    for (SxsEvaluationPair sxsPair : sxsPairs) {
      if (sxsPair.getChatA() == null) {
        failedSxsPairIds.add(sxsPair.getId());
      }
    }
  }

  /**
   * Populates missing side B chats for SxS pairs that need them.
   *
   * <p>This method creates side B chats for pairs that have a chat A but are missing chat B. It
   * only operates when model B is provided, as side B chats are only needed when there's a model to
   * run inference with.
   *
   * <p>The operation is transactional to ensure data consistency when creating new chats and
   * updating existing pairs.
   */
  @Transactional
  protected void populateMissingSideChats() {
    if (modelB == null) {
      return;
    }

    List<SxsEvaluationPair> pairsNeedingSideB = findPairsMissingSideB();
    if (pairsNeedingSideB.isEmpty()) {
      return;
    }

    createMissingSideBChats(pairsNeedingSideB);
    saveUpdatedPairs(pairsNeedingSideB);
  }

  /**
   * Finds SxS pairs that are missing side B chats.
   *
   * <p>This method filters the list of SxS pairs to find those that have a chat A but are missing
   * chat B. It excludes pairs that have already failed validation to avoid processing invalid data.
   *
   * @return a list of SxS pairs that need side B chats created
   */
  protected List<SxsEvaluationPair> findPairsMissingSideB() {
    return sxsPairs.stream()
        .filter(pair -> pair.getChatB() == null && !isPairFailed(pair))
        .collect(Collectors.toList());
  }

  /**
   * Checks if an SxS pair has failed validation.
   *
   * @param pair the SxS pair to check
   * @return true if the pair has failed validation, false otherwise
   */
  protected boolean isPairFailed(SxsEvaluationPair pair) {
    return failedSxsPairIds.contains(pair.getId());
  }

  /**
   * Creates missing side B chats for the specified SxS pairs.
   *
   * <p>This method duplicates the side A chats to create corresponding side B chats. It uses the
   * chat service's {@code duplicateChatsForSideB} method which handles the duplication logic
   * including skipping the last model output as appropriate for side B chats.
   *
   * <p>After creating the new chats, it updates each SxS pair to reference the new side B chat and
   * its latest turn.
   *
   * @param pairsNeedingSideB the SxS pairs that need side B chats created
   */
  protected void createMissingSideBChats(List<SxsEvaluationPair> pairsNeedingSideB) {
    List<Chat> sideAChats =
        pairsNeedingSideB.stream().map(SxsEvaluationPair::getChatA).collect(Collectors.toList());

    Map<String, Chat> newSideBChatsMap = getChatService().duplicateChatsForSideB(sideAChats, user);

    for (SxsEvaluationPair pair : pairsNeedingSideB) {
      String chatAId = pair.getChatA().getId();
      Chat newSideBChat = newSideBChatsMap.get(chatAId);

      pair.setChatB(newSideBChat);
      pair.setChatTurnB(newSideBChat.getLatestTurn());
    }
  }

  /**
   * Saves updated SxS pairs and refreshes the internal list.
   *
   * <p>This method persists the updated SxS pairs to the database and then updates the internal
   * {@link #sxsPairs} list to reflect the saved state. This ensures that subsequent operations work
   * with the most current data.
   *
   * @param updatedPairs the SxS pairs that have been modified and need to be saved
   */
  protected void saveUpdatedPairs(List<SxsEvaluationPair> updatedPairs) {
    List<SxsEvaluationPair> savedPairs = getSxsEvaluationPairRepository().saveAll(updatedPairs);

    for (SxsEvaluationPair savedPair : savedPairs) {
      for (int i = 0; i < sxsPairs.size(); i++) {
        if (sxsPairs.get(i).getId().equals(savedPair.getId())) {
          sxsPairs.set(i, savedPair);
          break;
        }
      }
    }
  }

  /**
   * Creates a job status for tracking the inference operation.
   *
   * <p>This method creates a job status entry to track the progress of the inference operation. The
   * job is created with a PENDING status and includes the total number of chat turns to be
   * processed.
   *
   * @param totalChatTurns the total number of chat turns to be processed
   * @return a job status instance for tracking the operation
   */
  protected JobStatus createJobStatus(int totalChatTurns) {
    return getJobStatusService()
        .createScorerJobStatus(
            PlanckConstants.INFERENCE, null, user, JobStatusEnum.PENDING, totalChatTurns, project);
  }

  /**
   * Creates an inference DTO for a specific chat turn.
   *
   * <p>This method creates a data transfer object that contains all the necessary information for
   * processing an inference request for a specific chat turn.
   *
   * @param job the job status associated with this inference request
   * @param chatTurn the chat turn to process
   * @return an inference DTO configured for the specified chat turn
   */
  protected InferenceDTO createInferenceDTO(JobStatus job, ChatTurn chatTurn) {
    InferenceDTO dto = new InferenceDTO();
    dto.setUserId(user.getId());
    dto.setProjectId(project.getId());
    dto.setJobId(job.getId());
    dto.setChatTurnId(chatTurn.getId());
    return dto;
  }

  /**
   * Sends inference requests for new model processing.
   *
   * <p>This method iterates through the provided chat turns and sends inference requests using the
   * specified model IDs. It's commonly used across multiple strategies.
   *
   * @param chatTurns the chat turns to process
   * @param turnToModelId mapping of chat turn IDs to model IDs
   * @param job the job status for tracking
   * @param isBulk whether this is a bulk operation
   */
  protected void sendNewModelInferences(
      List<ChatTurn> chatTurns, Map<String, String> turnToModelId, JobStatus job, boolean isBulk) {
    chatTurns.forEach(
        chatTurn -> {
          InferenceDTO dto = createInferenceDTO(job, chatTurn);
          getPublisherService()
              .sendInferenceWithModel(
                  chatTurn, dto, turnToModelId.get(chatTurn.getId()), false, isBulk);
        });
  }

  /**
   * Sends inference requests using existing models.
   *
   * <p>This method iterates through the provided chat turns and sends inference requests using the
   * existing model associated with each chat turn. It's commonly used across multiple strategies.
   *
   * @param chatTurns the chat turns to process
   * @param job the job status for tracking
   */
  protected void sendExistingModelInferences(List<ChatTurn> chatTurns, JobStatus job) {
    chatTurns.forEach(
        chatTurn -> {
          InferenceDTO dto = createInferenceDTO(job, chatTurn);
          getPublisherService().sendInferenceUsingExistingModel(chatTurn, dto);
        });
  }

  /**
   * Clears model outputs from the specified chat turns and saves the updated pairs.
   *
   * <p>This method removes model responses from chat turns and persists the changes to the
   * database. It's commonly used across multiple strategies.
   *
   * @param chatTurnsToClear the chat turns whose model outputs should be cleared
   */
  protected void clearModelOutputs(List<ChatTurn> chatTurnsToClear) {
    chatTurnsToClear.forEach(chatTurn -> chatTurn.setModelResponse(null));

    List<SxsEvaluationPair> pairsToSave = findPairsContainingChatTurns(chatTurnsToClear);
    getSxsEvaluationPairRepository().saveAll(pairsToSave);
  }

  /**
   * Finds SxS pairs that contain the specified chat turns.
   *
   * <p>This method searches through the internal sxsPairs list to find pairs that contain any of
   * the specified chat turns, either in chat A or chat B.
   *
   * @param chatTurnsToClear the chat turns to search for
   * @return a list of SxS pairs that contain the specified chat turns
   */
  private List<SxsEvaluationPair> findPairsContainingChatTurns(List<ChatTurn> chatTurnsToClear) {
    return sxsPairs.stream()
        .filter(
            pair ->
                chatTurnsToClear.stream()
                    .anyMatch(
                        chatTurn ->
                            (pair.getChatA() != null
                                    && pair.getChatA()
                                        .getLatestTurn()
                                        .getId()
                                        .equals(chatTurn.getId()))
                                || (pair.getChatB() != null
                                    && pair.getChatB()
                                        .getLatestTurn()
                                        .getId()
                                        .equals(chatTurn.getId()))))
        .collect(Collectors.toList());
  }
}
