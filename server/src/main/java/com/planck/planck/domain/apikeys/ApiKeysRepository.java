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

package com.planck.planck.domain.apikeys;

import com.planck.planck.entitities.ApiKeys;
import com.planck.planck.entitities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeysRepository extends JpaRepository<ApiKeys, String> {

  Optional<ApiKeys> findByUser(User user);

  @Query("SELECT ak.openaiKey FROM ApiKeys ak WHERE ak.user = :user")
  String findOpenAIKeyByUser(User user);

  @Query("SELECT ak.mistralKey FROM ApiKeys ak WHERE ak.user = :user")
  String findMistralKeyByUser(User user);

  @Query("SELECT ak.anthropicKey FROM ApiKeys ak WHERE ak.user = :user")
  String findAnthropicKeyByUser(User user);

  @Query("SELECT ak.googleKey FROM ApiKeys ak WHERE ak.user = :user")
  String findGoogleKeyByUser(User user);

  @Query("SELECT ak.grokKey FROM ApiKeys ak WHERE ak.user = :user")
  String findGrokKeyByUser(User user);

  @Query("SELECT ak.ollamaKey FROM ApiKeys ak WHERE ak.user = :user")
  String findOllamaKeyByUser(User user);

  @Query("SELECT ak.deepseekKey FROM ApiKeys ak WHERE ak.user = :user")
  String findDeepseekKeyByUser(User user);

  @Query("SELECT ak.huggingfaceKey FROM ApiKeys ak WHERE ak.user = :user")
  String findHuggingfaceKeyByUser(User user);

  @Query("SELECT ak.llamaKey FROM ApiKeys ak WHERE ak.user = :user")
  String findLlamaKeyByUser(User user);

  @Modifying
  int deleteByUser(User user);
}
