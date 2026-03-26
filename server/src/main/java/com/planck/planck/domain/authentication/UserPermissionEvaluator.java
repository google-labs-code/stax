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

package com.planck.planck.domain.authentication;

import com.planck.planck.domain.project.ProjectRepository;
import com.planck.planck.entitities.Project;
import java.io.Serializable;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserPermissionEvaluator implements PermissionEvaluator {
  @Autowired private ProjectRepository projectRepository;

  @Override
  public boolean hasPermission(
      Authentication authentication, Object targetDomainObject, Object permission) {
    if ((authentication == null)
        || (targetDomainObject == null)
        || !(permission instanceof String)) {
      return false;
    }
    String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();
    log.info(
        "Checking permission for {} to {} {}",
        authentication.getPrincipal(),
        permission,
        targetType);

    return hasPrivilege(authentication, targetType, permission.toString().toUpperCase());
  }

  @Override
  public boolean hasPermission(
      Authentication authentication, Serializable targetId, String targetType, Object permission) {
    if ((authentication == null) || (targetType == null) || !(permission instanceof String)) {
      return false;
    }
    log.info(
        "Checking permission for {} to {} {} {}",
        authentication.getPrincipal(),
        permission,
        targetType,
        targetId);
    // Use targetId to look up the entity in the database
    if ("PROJECT".equals(targetType.toUpperCase())) {
      if (!(targetId instanceof String)) {
        return false; // Or throw an appropriate exception
      }
      Optional<Project> project = projectRepository.findById((String) targetId);
      if (project.isPresent()) {
        // Perform permission check based on project data
        return hasPrivilege(authentication, project.get(), permission.toString().toUpperCase());
      }
    }
    return hasPrivilege(
        authentication, targetType.toUpperCase(), permission.toString().toUpperCase());
  }

  private boolean hasPrivilege(Authentication auth, String targetType, String permission) {
    for (GrantedAuthority grantedAuth : auth.getAuthorities()) {
      if (grantedAuth.getAuthority().startsWith(targetType)
          && grantedAuth.getAuthority().contains(permission)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasPrivilege(Authentication auth, Project project, String permission) {
    UserDetails userDetails = (UserDetails) auth.getPrincipal();
    return project.getUser().getUsername().equals(userDetails.getUsername());
  }
}
