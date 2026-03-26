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

import com.planck.planck.domain.importexport.dto.SxsEvaluationPairExportDTO;
import com.planck.planck.entitities.User;
import java.util.List;

public interface SxsExportService {

  SxsEvaluationPairExportDTO exportSxsPair(String pairId, User user, String projectId);

  List<SxsEvaluationPairExportDTO> exportSxsPairsForProject(String projectId, User user);

  List<SxsEvaluationPairExportDTO> exportSxsPairsByIds(
      String projectId, List<String> pairIds, User user);
}
