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

package com.planck.planck.domain.datatransfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.chat.ChatRepository;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.service.ChatTurnCopyService;
import com.planck.planck.domain.datatransfer.dto.CopyChatsResponseDTO;
import com.planck.planck.domain.datatransfer.dto.MoveChatsResponseDTO;
import com.planck.planck.domain.datatransfer.service.DataTransferOperationsServiceImpl;
import com.planck.planck.domain.project.EvaluationContainerRepository;
import com.planck.planck.domain.project.SxsEvaluationPairService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.entitities.*;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.exceptions.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataTransferOperationsServiceImplTest {

  @Mock private EvaluationContainerRepository evaluationContainerRepository;
  @Mock private SxsEvaluationPairService sxsEvaluationPairService;
  @Mock private ChatTurnRepository chatTurnRepository;
  @Mock private ChatTurnCopyService chatTurnCopyService;
  @Mock private TagLinkService tagLinkService;
  @Mock private ChatRepository chatRepository;

  @InjectMocks private DataTransferOperationsServiceImpl dataTransferOperationsService;

  private User user;
  private EvaluationContainer sourceContainer;
  private EvaluationContainer targetContainer;

  private static final String SOURCE_ID = "source-1";
  private static final String TARGET_ID = "target-1";
  private static final String USER_ID = "user-1";

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(USER_ID);

    sourceContainer = new Project();
    sourceContainer.setId(SOURCE_ID);
    sourceContainer.setUser(user);

    targetContainer = new Project();
    targetContainer.setId(TARGET_ID);
    targetContainer.setUser(user);
  }

  @Nested
  class CopyChatsTests {

    @Test
    void copyAllChats_shouldSucceed() {
      when(evaluationContainerRepository.findById(SOURCE_ID))
          .thenReturn(Optional.of(sourceContainer));
      when(evaluationContainerRepository.findById(TARGET_ID))
          .thenReturn(Optional.of(targetContainer));
      List<String> chatIds = List.of("chat1", "chat2");
      when(chatTurnRepository.findAllChatIdsByUserAndContainer(user, sourceContainer))
          .thenReturn(chatIds);

      Chat chat1 = new Chat();
      chat1.setId("chat1");
      chat1.setUser(user);
      chat1.setContainer(sourceContainer);

      Chat chat2 = new Chat();
      chat2.setId("chat2");
      chat2.setUser(user);
      chat2.setContainer(sourceContainer);

      ChatTurn turn1 = new ChatTurn();
      turn1.setId("t1");
      turn1.setChat(chat1);

      ChatTurn turn2 = new ChatTurn();
      turn2.setId("t2");
      turn2.setChat(chat2);

      chat1.setTurns(List.of(turn1));
      chat2.setTurns(List.of(turn2));
      List<ChatTurn> sourceTurns = List.of(turn1, turn2);

      when(chatTurnRepository.findForCopy(chatIds)).thenReturn(sourceTurns);
      when(chatRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
      targetContainer.setEvaluationType(EvaluationType.SXS);

      CopyChatsResponseDTO response =
          dataTransferOperationsService.copyAllChatsFromProjectToProject(
              SOURCE_ID, TARGET_ID, user);

      assertThat(response.getSuccessfullyCopiedCount()).isEqualTo(2);
      assertThat(response.getFailedToCopyCount()).isZero();
      verify(sxsEvaluationPairService)
          .createSxsEvaluationPairs(any(), anyList(), eq(targetContainer));
    }

    @Test
    void copy_shouldThrowNotFoundException_whenSourceContainerMissing() {
      when(evaluationContainerRepository.findById(SOURCE_ID)).thenReturn(Optional.empty());
      assertThrows(
          NotFoundException.class,
          () ->
              dataTransferOperationsService.copyAllChatsFromProjectToProject(
                  SOURCE_ID, TARGET_ID, user));
    }
  }

  @Nested
  class MoveChatsTests {

    @Test
    void moveChats_shouldSucceed() {
      List<String> chatIdsToMove = List.of("chat1");
      when(evaluationContainerRepository.findById(TARGET_ID))
          .thenReturn(Optional.of(targetContainer));

      Chat chatToMove = new Chat();
      chatToMove.setId("chat1");
      chatToMove.setUser(user);
      chatToMove.setContainer(sourceContainer);
      chatToMove.setTurns(List.of(new ChatTurn(), new ChatTurn()));
      List<Chat> foundChats = List.of(chatToMove);

      when(chatRepository.findByUserAndIdIn(user, chatIdsToMove)).thenReturn(foundChats);

      MoveChatsResponseDTO response =
          dataTransferOperationsService.moveChatsFromSourceToTarget(
              SOURCE_ID, TARGET_ID, chatIdsToMove, user);

      assertThat(response.getSuccessfullyMovedCount()).isEqualTo(1);
      assertThat(response.getFailedToMoveCount()).isZero();
      assertThat(response.getMessage()).contains("1 of 1 chats moved (total 2 turns)");

      ArgumentCaptor<List<Chat>> captor = ArgumentCaptor.forClass(List.class);
      verify(chatRepository).saveAll(captor.capture());
      assertThat(captor.getValue().get(0).getContainer().getId()).isEqualTo(TARGET_ID);
    }

    @Test
    void moveChats_shouldReturnEarly_whenChatIdsAreEmptyOrNull() {
      MoveChatsResponseDTO response =
          dataTransferOperationsService.moveChatsFromSourceToTarget(
              SOURCE_ID, TARGET_ID, null, user);
      assertThat(response.getMessage()).isEqualTo("No chat IDs provided to move.");
      verify(chatRepository, never()).saveAll(any());
    }

    @Test
    void moveChats_shouldReportError_whenChatInWrongSource() {
      List<String> chatIdsToMove = List.of("chat1");
      EvaluationContainer wrongSource = new Project();
      wrongSource.setId("wrong-source");

      when(evaluationContainerRepository.findById(TARGET_ID))
          .thenReturn(Optional.of(targetContainer));

      Chat chatToMove = new Chat();
      chatToMove.setId("chat1");
      chatToMove.setUser(user);
      chatToMove.setContainer(wrongSource);

      when(chatRepository.findByUserAndIdIn(user, chatIdsToMove)).thenReturn(List.of(chatToMove));

      MoveChatsResponseDTO response =
          dataTransferOperationsService.moveChatsFromSourceToTarget(
              SOURCE_ID, TARGET_ID, chatIdsToMove, user);

      assertThat(response.getSuccessfullyMovedCount()).isZero();
      assertThat(response.getFailedToMoveCount()).isEqualTo(1);
      verify(chatRepository, never()).saveAll(any());
    }
  }
}
