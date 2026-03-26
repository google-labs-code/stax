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

package com.planck.planck.domain.tos;

import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.entitities.Tos;
import com.planck.planck.entitities.User;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;

public interface TosService {

  Optional<Tos> getActiveTos();

  Tos createNewTos(String content, String type);

  void deactivateTos(String tosId);

  String extractUserName(String token);

  String generateToken(UserDetails userDetails);

  JwtAuthenticationResponse acceptTos(User user, String tosId);
}
