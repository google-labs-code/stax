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

package com.planck.planck.domain.chat;

import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {
  Optional<Chat> findByIdAndUser(String id, User user);

  List<Chat> findAllByUser(User user);

  List<Chat> findByUserAndIdIn(User user, Collection<String> ids);

  Optional<Chat> findByIdAndContainer(String id, EvaluationContainer container);

  List<Chat> findAllByContainer(EvaluationContainer container);

  @Query(
      "SELECT DISTINCT c FROM Chat c LEFT JOIN FETCH c.turns WHERE c.id IN :ids AND c.user = :user")
  List<Chat> findChatsByList(Collection<String> ids, User user);
}
