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

package com.planck.planck.domain.datatransfer.service;

import com.planck.planck.domain.datatransfer.dto.CopyChatsResponseDTO;
import com.planck.planck.domain.datatransfer.dto.MoveChatsResponseDTO;
import com.planck.planck.entitities.User;
import java.util.List;

/**
 * Service interface for operations related to copying chats within and between different entities ,
 * such as copying chats between projects.
 */
public interface DataTransferOperationsService {

  /**
   * Copies all chats from a source container to a target container for the authenticated user.
   *
   * @param sourceType The type of the source container.
   * @param sourceId The ID of the source container.
   * @param targetType The type of the target container.
   * @param targetId The ID of the target container.
   * @param user The user performing the operation.
   * @return A DTO summarizing the result of the copy operation, including successes and failures.
   */
  CopyChatsResponseDTO copyAllChatsFromProjectToProject(
      String sourceId, String targetId, User user);

  /**
   * Copies a list of specified chats from a source container to a target container for the
   * authenticated user.
   *
   * @param sourceType The type of the source container.
   * @param sourceId The ID of the source container.
   * @param targetType The type of the target container.
   * @param targetId The ID of the target container.
   * @param chatIds The list of chat IDs to copy.
   * @param user The user performing the operation.
   * @return A DTO summarizing the result of the copy operation, including successes and failures.
   */
  CopyChatsResponseDTO copySelectedChatsFromProjectToProject(
      String sourceId, String targetId, List<String> chatIds, User user);

  /**
   * Moves the specified chats from a source to a target for the authenticated user.
   *
   * @param sourceId The ID of the source container.
   * @param sourceType The type of the source container.
   * @param targetId The ID of the target container.
   * @param targetType The type of the target container.
   * @param chats The list of chat IDs to move.
   * @param user The user performing the operation.
   * @return A DTO summarizing the result of the move operation, including successes and failures.
   */
  MoveChatsResponseDTO moveChatsFromSourceToTarget(
      String sourceId, String targetId, List<String> chats, User user);
}
