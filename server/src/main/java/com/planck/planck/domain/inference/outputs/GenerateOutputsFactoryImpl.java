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
import com.planck.planck.domain.inference.dto.SXSGenerateOutputsResponse;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.pubsub.InferencePublisherService;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.SXSGenerateOutputsMode;
import com.planck.planck.exceptions.IllegalInputException;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link GenerateOutputsFactory} that creates appropriate output generation
 * strategies based on project type and mode.
 *
 * <p>This factory supports different project types (SxS, Pointwise) and various output generation
 * modes. It handles model validation, strategy instantiation, and dependency injection for all
 * required services.
 *
 * <p>The factory ensures that:
 *
 * <ul>
 *   <li>Models are validated for the specific user
 *   <li>Deprecated models are rejected
 *   <li>All strategies receive their required dependencies
 *   <li>Appropriate strategies are created for each project type
 * </ul>
 *
 * @author Planck Team
 */
@Component
public class GenerateOutputsFactoryImpl implements GenerateOutputsFactory {
  /** Service for model operations and validation */
  private final ModelService modelService;

  /** Service for chat operations and duplication */
  private final ChatService chatService;

  /** Repository for SxS evaluation pair operations */
  private final SxsEvaluationPairRepository sxsEvaluationPairRepository;

  /** Service for publishing inference requests */
  private final InferencePublisherService publisherService;

  /** Service for job status management */
  private final JobStatusService jobStatusService;

  /**
   * Constructs a new factory with all required dependencies.
   *
   * @param modelService service for model operations and validation
   * @param chatService service for chat operations and duplication
   * @param sxsEvaluationPairRepository repository for SxS evaluation pair operations
   * @param publisherService service for publishing inference requests
   * @param jobStatusService service for job status management
   */
  public GenerateOutputsFactoryImpl(
      ModelService modelService,
      ChatService chatService,
      SxsEvaluationPairRepository sxsEvaluationPairRepository,
      InferencePublisherService publisherService,
      JobStatusService jobStatusService) {
    this.modelService = modelService;
    this.chatService = chatService;
    this.sxsEvaluationPairRepository = sxsEvaluationPairRepository;
    this.publisherService = publisherService;
    this.jobStatusService = jobStatusService;
  }

  /**
   * Creates and configures an appropriate output generation strategy based on the project type and
   * specified mode.
   *
   * <p>This method routes the request to the appropriate strategy factory method based on the
   * project type. Currently supports SxS and Pointwise project types.
   *
   * @param modelA the ID of model A (can be null)
   * @param modelB the ID of model B (can be null)
   * @param mode the output generation mode to use
   * @param project the project for which to generate outputs
   * @return a configured strategy instance ready for use
   * @throws IllegalArgumentException if the project type is not supported
   * @throws IllegalInputException if model validation fails (deprecated models, etc.)
   */
  @Override
  public GenerateOutputsStrategy<?> getStrategy(
      String modelA, String modelB, SXSGenerateOutputsMode mode, Project project) {
    switch (project.getEvaluationType()) {
      case SXS:
        return getSXSGenerateOutputsStrategy(modelA, modelB, mode, project);
      case POINTWISE:
        return getPointwiseGenerateOutputsStrategy(modelA, modelB, mode, project);
      default:
        throw new IllegalArgumentException("Invalid project type: " + project.getEvaluationType());
    }
  }

  /**
   * Creates and configures an SxS-specific output generation strategy.
   *
   * <p>This method validates the provided model IDs for the project user, creates the appropriate
   * strategy based on the mode, and injects all required dependencies.
   *
   * <p>Supported modes:
   *
   * <ul>
   *   <li>{@link SXSGenerateOutputsMode#MISSING_ONLY} - generates outputs only for missing turns
   *   <li>{@link SXSGenerateOutputsMode#MISSING_AND_MATCHING} - generates outputs for missing turns
   *       and reruns matching models
   *   <li>{@link SXSGenerateOutputsMode#RUN_OR_RERUN} - runs inference with provided models,
   *       clearing existing outputs
   *   <li>{@link SXSGenerateOutputsMode#DUPLICATE} - duplicates pairs with mismatched models and
   *       generates outputs
   *   <li>{@link SXSGenerateOutputsMode#AUTORESOLVE} - automatically resolves the best action for
   *       each scenario
   * </ul>
   *
   * @param modelIdA the ID of model A (can be null)
   * @param modelIdB the ID of model B (can be null)
   * @param mode the output generation mode
   * @param project the SxS project
   * @return a configured SxS strategy instance
   * @throws IllegalArgumentException if the mode is not supported
   * @throws IllegalInputException if model validation fails
   */
  private GenerateOutputsStrategy<SXSGenerateOutputsResponse> getSXSGenerateOutputsStrategy(
      String modelIdA, String modelIdB, SXSGenerateOutputsMode mode, Project project) {
    Model modelA = getModelForUser(project.getUser(), modelIdA);
    Model modelB = getModelForUser(project.getUser(), modelIdB);
    SXSGenerateOutputsStrategy strategy = null;
    switch (mode) {
      case MISSING_ONLY:
        strategy = new SXSGenerateOutputsStrategyMissingOnly(modelA, modelB, project);
        break;
      case MISSING_AND_MATCHING:
        strategy = new SXSGenerateOutputsStrategyMissingAndMatching(modelA, modelB, project);
        break;
      case RUN_OR_RERUN:
        strategy = new SXSGenerateOutputsStrategyRunOrRerun(modelA, modelB, project);
        break;
      case DUPLICATE:
        strategy = new SXSGenerateOutputsStrategyDuplicate(modelA, modelB, project);
        break;
      case AUTORESOLVE:
        strategy = new SXSGenerateOutputsStrategyAutoResolve(modelA, modelB, project);
        break;
      default:
        throw new IllegalArgumentException("Invalid mode: " + mode);
    }

    strategy.setChatService(chatService);
    strategy.setSxsEvaluationPairRepository(sxsEvaluationPairRepository);
    strategy.setPublisherService(publisherService);
    strategy.setJobStatusService(jobStatusService);
    return strategy;
  }

  /**
   * Creates and configures a Pointwise-specific output generation strategy.
   *
   * <p>This method is a placeholder for future Pointwise project support. Currently throws an
   * {@link UnsupportedOperationException} as Pointwise output generation is not yet implemented.
   *
   * @param modelA the ID of model A (can be null)
   * @param modelB the ID of model B (can be null)
   * @param mode the output generation mode
   * @param project the Pointwise project
   * @return a configured Pointwise strategy instance
   * @throws UnsupportedOperationException always, as Pointwise is not yet implemented
   */
  private GenerateOutputsStrategy<?> getPointwiseGenerateOutputsStrategy(
      String modelA, String modelB, SXSGenerateOutputsMode mode, Project project) {
    // TODO: Implement Pointwise strategy when ready
    // This will return a PointwiseGenerateOutputsStrategy with appropriate response type
    throw new UnsupportedOperationException("Pointwise output generation not yet implemented");
  }

  /**
   * Retrieves and validates a model for a specific user.
   *
   * <p>This method fetches the model by ID for the given user and performs validation to ensure the
   * model is not deprecated. If the model ID is null, returns null. If the model is deprecated,
   * throws an {@link IllegalInputException}.
   *
   * @param user the user for whom to retrieve the model
   * @param modelId the ID of the model to retrieve (can be null)
   * @return the validated model instance, or null if modelId is null
   * @throws IllegalInputException if the model is deprecated
   * @throws IllegalArgumentException if the model is not found or not accessible to the user
   */
  private Model getModelForUser(User user, String modelId) {
    if (modelId == null) {
      return null;
    }

    Model model = modelService.getModelForUser(user, modelId);
    if (model.isDeprecated()) {
      throw new IllegalInputException("Unable to run inference with deprecated model");
    }
    return model;
  }
}
