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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.entitities.User;
import com.planck.planck.enums.Role;
import com.planck.planck.exceptions.UserNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private UserServiceImpl userService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setEmail("user@example.com");
    user.setRole(Role.USER);
  }

  @Test
  void testFindByUserId_sucess() {

    when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
    User user = userService.findByUserId("1");
    assertNotNull(user);
  }

  @Test
  void testFindByUserId_UserNotFoundException() {

    when(userRepository.findById(anyString())).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.findByUserId("1"));
  }

  @Test
  void testExistsByEmail() {

    when(userRepository.existsByEmail(anyString())).thenReturn(true);
    boolean existsByEmail = userService.existsByEmail("test@gmail.com");
    assertEquals(true, existsByEmail);
  }

  @Test
  void testSave() {

    // userService.save(user);
    // verify(userRepository, times(1)).save(user);
  }

  @Test
  void testSetLastLoginTime() {

    userService.setLastLoginTime(user);
    verify(userRepository, times(1)).save(user);
  }

  @Test
  void testLoadUserById_NotFoundException() {

    when(userRepository.findById(anyString())).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> userService.loadUserById("userId"));
  }

  @Test
  void testLoadUserById_success() {

    when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
    UserDetails user = userService.loadUserById("userId");
    assertNotNull(user);
  }

  @Test
  void testFindByEmail() {

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    Optional<User> user = userService.findByEmail("gmail.com");
    assertNotNull(user);
  }

  @Test
  void testUserDetailsService_User() {

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    UserDetailsService userDetailsService = userService.userDetailsService();
    UserDetails userDetails = userDetailsService.loadUserByUsername("userName");
    assertNotNull(userDetailsService);
    assertNotNull(userDetails);
  }

  @Test
  void testUserDetailsService_UserNotFoundException() {

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    UserDetailsService userDetailsService = userService.userDetailsService();
    assertThrows(
        UserNotFoundException.class, () -> userDetailsService.loadUserByUsername("userName"));
  }
}
