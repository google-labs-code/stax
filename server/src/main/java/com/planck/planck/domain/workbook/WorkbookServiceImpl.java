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

package com.planck.planck.domain.workbook;

import com.planck.planck.Status200Response;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.domain.workbook.dto.ChatWorkbookRowDTO;
import com.planck.planck.domain.workbook.dto.GetWorkbookResponseDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.exceptions.NotFoundException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class WorkbookServiceImpl implements WorkbookService {

  @Autowired private ChatService chatService;

  @Autowired private ProjectService projectService;
  @Autowired private EvaluationContainerService evaluationContainerService;

  @Override
  public GetWorkbookResponseDTO getData(
      User user,
      String evaluationContainerId,
      int pageSize,
      int pageToken,
      String orderBy,
      String filter,
      List<String> tagIds) {
    EvaluationContainer container =
        evaluationContainerService.getContainerForUser(user, evaluationContainerId);
    if (container == null) {
      throw new NotFoundException("Container not found for this user");
    }
    // orderBy and filter are currently ignored
    int effectivePageSize = Math.min(pageSize < 1 ? 10000 : pageSize, 10000);

    Pageable pageable =
        PageRequest.of(pageToken, effectivePageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Chat> chatPage =
        chatService.fetchLastTurns(user, container, tagIds, pageable).map(ChatTurn::getChat);

    List<ChatWorkbookRowDTO> workbookDTO =
        chatPage.getContent().stream().map(ChatWorkbookRowDTO::new).toList();

    long totalElements = chatPage.getTotalElements();
    String nextPageToken = chatPage.hasNext() ? String.valueOf(chatPage.getNumber() + 1) : null;

    return new GetWorkbookResponseDTO(workbookDTO, nextPageToken, (int) totalElements);
  }

  @Transactional
  @Override
  public Object getDeleteChats(User user, String projectId, List<String> chatIds) {
    chatService.deleteChats(chatIds, user, projectId);
    return new Status200Response("Chat rows deleted successfully");
  }

  @Override
  public Object deleteChatsByProject(User user, String projectId) {
    Project project = projectService.getProjectForUser(user, projectId);
    List<ChatTurn> chatTurns = chatService.findAllByUserAndChatContainer(user, project);
    chatService.delete(chatTurns);
    return new Status200Response("Chat rows deleted successfully");
  }
}
