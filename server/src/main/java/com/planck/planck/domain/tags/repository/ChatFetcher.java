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

package com.planck.planck.domain.tags.repository;

import com.planck.planck.annotation.TaggableEntityFetcher;
import com.planck.planck.domain.chat.ChatRepository;
import com.planck.planck.domain.chat.dto.ChatDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.TagLinkTargetType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
@TaggableEntityFetcher(TagLinkTargetType.CHAT)
public class ChatFetcher implements EntityFetcher<ChatDTO> {
  private final ChatRepository chatRepository;

  public ChatFetcher(ChatRepository chatRepository) {
    this.chatRepository = chatRepository;
  }

  @Override
  public List<ChatDTO> findAllByIds(List<String> ids, User user) {
    List<Chat> chats = chatRepository.findByUserAndIdIn(user, ids);
    return chats.stream().map(ChatDTO::new).toList();
  }
}
