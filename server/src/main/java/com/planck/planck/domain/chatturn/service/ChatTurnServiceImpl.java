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

package com.planck.planck.domain.chatturn.service;

import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.User;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ChatTurnServiceImpl implements ChatTurnService {

  @Autowired private ChatTurnRepository chatTurnRepository;

  @Autowired private ModelInputService modelInputService;

  @Transactional(readOnly = true)
  @Override
  public ChatTurn getChatTurn(String id, User user) {
    return chatTurnRepository.findByIdAndUser(id, user);
  }

  @Override
  public void updateExpectedOutput(String chatTurnId, User user, String expectedOutput) {
    ChatTurn chatTurn = getChatTurn(chatTurnId, user);
    if (chatTurn == null) throw new IllegalArgumentException("Chat turn not found");
    List<ModelInput> modelInputs = chatTurn.getInputs();
    if (modelInputs == null || modelInputs.isEmpty())
      throw new IllegalArgumentException("No inputs found");
    ModelInput modelInput = modelInputs.get(modelInputs.size() - 1);
    modelInput.setExpectedOutput(expectedOutput);
    modelInputService.save(modelInput);
  }

  @Override
  public List<ChatTurn> findByChatIn(List<Chat> chat) {
    return chatTurnRepository.findByChatIn(chat);
  }
}
