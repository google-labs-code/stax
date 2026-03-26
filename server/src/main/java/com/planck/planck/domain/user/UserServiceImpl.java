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

package com.planck.planck.domain.user;

import com.planck.planck.entitities.User;
import com.planck.planck.exceptions.UserNotFoundException;
import com.planck.planck.util.TosAcceptance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  @PersistenceContext private EntityManager entityManager;

  @Lazy @Autowired private UserRepository userRepository;

  @Override
  public UserDetailsService userDetailsService() {
    return new UserDetailsService() {
      @Override
      public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException());
      }
    };
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public UserDetails loadUserById(String userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
  }

  @Override
  @Transactional
  public void save(User user) {
    userRepository.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  public User findByUserId(String userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
  }

  public void setLastLoginTime(User user) {
    user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
    userRepository.save(user);
  }

  @Transactional(readOnly = false)
  public void addTosIdToUser(User user, String tosId) {
    if (user != null) {
      List<TosAcceptance> currentTosDetails = user.getAcceptedTosDetailsList();
      TosAcceptance newAcceptance =
          new TosAcceptance(tosId, new Timestamp(System.currentTimeMillis()));
      currentTosDetails.add(newAcceptance);
      user.setAcceptedTosDetailsList(currentTosDetails);
      userRepository.save(user);
    }
  }

  public List<TosAcceptance> getTosDetailsForUser(User user) {
    if (user != null) {
      return user.getAcceptedTosDetailsList();
    }
    return null;
  }
}
