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

package com.planck.planck.entitities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.enums.Role;
import com.planck.planck.enums.UserStatus;
import com.planck.planck.util.TosAcceptance;
import com.planck.planck.util.converter.RoleConverter;
import com.planck.planck.util.converter.UserStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "user")
@NoArgsConstructor
@Getter
@Setter
public class User implements UserDetails {

  @Transient private static final String ID_PREFIX = "user-";

  @Id
  @Column(unique = true)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  private String firstName;
  private String lastName;

  @Column(unique = true)
  private String email;

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  private Timestamp lastLoginTime;

  private Boolean isGoogleSignup = false;

  private Boolean isGoogleSignin = false;

  @Column(name = "accepted_tos_details", columnDefinition = "TEXT")
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private String acceptedTosDetails;

  @Column(name = "is_enabled")
  private Boolean isEnabled = true;

  @CreationTimestamp private Timestamp enabledAt;

  @Column(name = "status")
  @Convert(converter = UserStatusConverter.class)
  private UserStatus status;

  public User(String firstName, String lastName, String email, Role role) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.role = role;
    this.isGoogleSignup = false;
    this.status = UserStatus.ACTIVE;
  }

  public User(String firstName, String lastName, String email, Role role, Boolean isGoogleSignup) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.role = role;
    this.isGoogleSignup = isGoogleSignup;
    this.status = UserStatus.ACTIVE;
  }

  @Convert(converter = RoleConverter.class)
  private Role role;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getUsername() {
    // email in our case
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {

    return isEnabled;
  }

  public List<TosAcceptance> getAcceptedTosDetailsList() {
    if (acceptedTosDetails == null || acceptedTosDetails.isEmpty()) {
      return new ArrayList<>();
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(acceptedTosDetails, new TypeReference<List<TosAcceptance>>() {});
    } catch (IOException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public void setAcceptedTosDetailsList(List<TosAcceptance> tosAcceptances) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      this.acceptedTosDetails = mapper.writeValueAsString(tosAcceptances);
    } catch (IOException e) {
      e.printStackTrace();
      // Need to add throw exception here
    }
  }

  @Override
  public String getPassword() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPassword'");
  }
}
