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

import com.planck.planck.domain.importexport.dto.ChatExportDTO;
import com.planck.planck.domain.importexport.dto.message.BaseMessageExportDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.User;
import java.util.List;

public interface ChatExportService {
  ChatExportDTO exportChat(String chatId, User user);

  List<ChatExportDTO> exportChats(List<String> chatIds, User user);

  List<ChatExportDTO> exportAllChats(String sourceId, User user);

  List<BaseMessageExportDTO> getChatHistory(Chat chat, User user);
}
