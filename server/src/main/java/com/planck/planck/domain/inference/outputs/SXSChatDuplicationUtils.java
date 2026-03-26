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
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for handling chat duplication logic in SxS strategies.
 *
 * <p>This class provides common functionality for detecting model mismatches and creating new SxS
 * pairs with duplicated chats. It's used by strategies that need to handle model mismatches by
 * creating new pairs rather than overwriting existing ones.
 *
 * @author Planck Team
 * @since 1.0
 */
public class SXSChatDuplicationUtils {

  /**
   * Finds SxS pairs that have model mismatches.
   *
   * <p>This method identifies pairs where the existing model responses don't match the expected
   * input models. These pairs are candidates for duplication.
   *
   * @param sxsPairs the list of SxS pairs to check
   * @param modelA the expected model for side A (can be null)
   * @param modelB the expected model for side B (can be null)
   * @param failedPairIds list of pair IDs that have already failed validation
   * @return a list of SxS pairs that have model mismatches
   */
  public static List<SxsEvaluationPair> findPairsWithModelMismatch(
      List<SxsEvaluationPair> sxsPairs, Model modelA, Model modelB, List<String> failedPairIds) {
    List<SxsEvaluationPair> mismatchedPairs = new ArrayList<>();

    for (SxsEvaluationPair sxsPair : sxsPairs) {
      if (failedPairIds.contains(sxsPair.getId())) {
        continue;
      }

      if (hasModelMismatch(sxsPair, modelA, modelB)) {
        mismatchedPairs.add(sxsPair);
      }
    }

    return mismatchedPairs;
  }

  /**
   * Checks if an SxS pair has a model mismatch on either side.
   *
   * @param sxsPair the SxS pair to check
   * @param modelA the expected model for side A (can be null)
   * @param modelB the expected model for side B (can be null)
   * @return true if there's a model mismatch on either side, false otherwise
   */
  public static boolean hasModelMismatch(SxsEvaluationPair sxsPair, Model modelA, Model modelB) {
    return hasSideModelMismatch(modelA, sxsPair.getChatA())
        || hasSideModelMismatch(modelB, sxsPair.getChatB());
  }

  /**
   * Checks if a specific side has a model mismatch.
   *
   * @param expectedModel the expected model for this side (can be null)
   * @param chat the chat for this side
   * @return true if there's a model mismatch, false otherwise
   */
  public static boolean hasSideModelMismatch(Model expectedModel, Chat chat) {
    if (expectedModel == null || chat == null) {
      return false;
    }

    ModelResponse response = chat.getLatestTurn().getModelResponse();
    return response != null
        && response.getModel() != null
        && !response.getModel().getId().equals(expectedModel.getId());
  }

  /**
   * Creates new SxS pairs for mismatched pairs by duplicating their chats.
   *
   * <p>This method duplicates both chat A and chat B for each mismatched pair, creates new SxS
   * pairs, and saves them to the database.
   *
   * @param mismatchedPairs the pairs that need new versions created
   * @param chatService the chat service for duplication operations
   * @param sxsEvaluationPairRepository the repository for saving new pairs
   * @param user the user who owns the project
   * @return a list of newly created SxS pairs
   */
  public static List<SxsEvaluationPair> createNewPairsForMismatches(
      List<SxsEvaluationPair> mismatchedPairs,
      ChatService chatService,
      SxsEvaluationPairRepository sxsEvaluationPairRepository,
      User user) {

    if (mismatchedPairs.isEmpty()) {
      return new ArrayList<>();
    }

    List<Chat> allChatsToDuplicate = collectAllChatsForDuplication(mismatchedPairs);
    Map<String, Chat> duplicatedChats =
        chatService.duplicateChatsForSideB(allChatsToDuplicate, user);

    List<SxsEvaluationPair> newPairs =
        createNewPairsFromDuplicatedChats(mismatchedPairs, duplicatedChats);

    if (!newPairs.isEmpty()) {
      List<SxsEvaluationPair> savedPairs = sxsEvaluationPairRepository.saveAll(newPairs);
      return savedPairs;
    }

    return newPairs;
  }

  /**
   * Collects all chats that need to be duplicated from the mismatched pairs.
   *
   * @param mismatchedPairs the pairs containing chats to duplicate
   * @return a list of all chats that need duplication
   */
  private static List<Chat> collectAllChatsForDuplication(List<SxsEvaluationPair> mismatchedPairs) {
    return mismatchedPairs.stream()
        .flatMap(pair -> List.of(pair.getChatA(), pair.getChatB()).stream())
        .collect(Collectors.toList());
  }

  /**
   * Creates new SxS pairs from the duplicated chats.
   *
   * @param mismatchedPairs the original mismatched pairs
   * @param duplicatedChats mapping of original chat IDs to duplicated chats
   * @return a list of newly created SxS pairs
   */
  private static List<SxsEvaluationPair> createNewPairsFromDuplicatedChats(
      List<SxsEvaluationPair> mismatchedPairs, Map<String, Chat> duplicatedChats) {
    return mismatchedPairs.stream()
        .map(originalPair -> createNewPair(originalPair, duplicatedChats))
        .collect(Collectors.toList());
  }

  /**
   * Creates a single new SxS pair from an original pair and duplicated chats.
   *
   * @param originalPair the original SxS pair
   * @param duplicatedChats mapping of original chat IDs to duplicated chats
   * @return a new SxS pair with duplicated chats
   */
  private static SxsEvaluationPair createNewPair(
      SxsEvaluationPair originalPair, Map<String, Chat> duplicatedChats) {
    SxsEvaluationPair newPair = new SxsEvaluationPair();
    newPair.setContainer(originalPair.getContainer());
    newPair.setUser(originalPair.getUser());

    Chat newChatA = duplicatedChats.get(originalPair.getChatA().getId());
    newPair.setChatA(newChatA);
    newPair.setChatTurnA(newChatA.getLatestTurn());

    Chat newChatB = duplicatedChats.get(originalPair.getChatB().getId());
    newPair.setChatB(newChatB);
    newPair.setChatTurnB(newChatB.getLatestTurn());

    return newPair;
  }
}
