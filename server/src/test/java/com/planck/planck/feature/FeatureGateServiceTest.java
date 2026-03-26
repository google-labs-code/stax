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

package com.planck.planck.feature;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.planck.planck.feature.dto.Metadata;
import com.planck.planck.feature.service.FeatureGateService;
import com.planck.planck.feature.service.FeatureGateStrategy;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureGateServiceTest {

  @Mock private FeatureGateStrategy mockActiveStrategy;

  @InjectMocks private FeatureGateService featureGateService;

  @Test
  void initialLoad_shouldCallInitializeOnStrategy() {

    featureGateService.initialLoad();

    verify(mockActiveStrategy, times(1)).initialize();
  }

  @Test
  void isFeatureEnabled_shouldDelegateToStrategy() {

    String featureKey = "test-feature";
    when(mockActiveStrategy.isFeatureEnabled(featureKey)).thenReturn(true);

    boolean result = featureGateService.isFeatureEnabled(featureKey);

    assertTrue(result);
    verify(mockActiveStrategy, times(1)).isFeatureEnabled(featureKey);
  }

  @Test
  void getCurrentMetadata_shouldDelegateToStrategy() {

    Metadata mockMetadata = new Metadata("commit123", "author", "time");
    when(mockActiveStrategy.getCurrentMetadata()).thenReturn(Optional.of(mockMetadata));

    Optional<Metadata> result = featureGateService.getCurrentMetadata();

    assertTrue(result.isPresent());
    assertEquals("commit123", result.get().commitHash());
    verify(mockActiveStrategy, times(1)).getCurrentMetadata();
  }

  @Test
  void forceSynchronize_shouldDelegateToStrategy() {

    doNothing().when(mockActiveStrategy).forceSynchronize();

    featureGateService.forceSynchronize();

    verify(mockActiveStrategy, times(1)).forceSynchronize();
  }
}
