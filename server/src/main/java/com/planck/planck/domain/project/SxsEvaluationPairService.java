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

package com.planck.planck.domain.project;

import com.planck.planck.domain.project.dto.SXSChatRowDTO;
import com.planck.planck.domain.workbook.dto.SXSWorkbookDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.List;
import java.util.Map;

public interface SxsEvaluationPairService {

  SxsEvaluationPair createSxsPair(
      User user, String containerId, Prompt prompt, String expectedOutput);

  SxsEvaluationPair getSxsPairById(String pairId, User user);

  void deleteSxsPair(String pairId, User user);

  SxsEvaluationPair generateAndSetTurnB(
      User user,
      String containerId,
      String pairId,
      String modelId,
      boolean createEmptyLastResponse);

  SXSWorkbookDTO getSXSRows(
      User user,
      String containerId,
      int pageSize,
      int pageToken,
      String orderBy,
      String filter,
      List<String> tagIds);

  SxsEvaluationPair updateSxsPair(
      String pairId,
      String containerId,
      User user,
      String input,
      String expectedOutput,
      Map<String, String> metadata,
      List<String> tags);

  void deleteSxsPairsByIds(List<String> pairIds, User user);

  void deleteSxsPairsByContainer(String containerId, User user);

  List<SXSChatRowDTO> getSXSRowsForPair(User user, String containerId, String pairId);

  SxsEvaluationPair synchronizeSxsPairChats(
      SxsEvaluationPair pair, User user, String modelIdAFromRequest, String modelIdBFromRequest);

  void createSxsEvaluationPairs(User user, List<Chat> targetChats, EvaluationContainer container);
}
