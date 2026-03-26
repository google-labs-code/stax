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

package com.planck.planck.domain.dataset.service;

import com.planck.planck.domain.dataset.dto.DataSetDTO;
import com.planck.planck.domain.dataset.dto.DataSetListResponse;
import com.planck.planck.entitities.DataSet;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;

public interface DataSetService {
  DataSetListResponse getAllDataSets(User user, Boolean includeHidden);

  DataSetDTO getDataSetById(User user, String id);

  DataSet createDataSet(User user, String name, String description, EvaluationType type);

  DataSetDTO updateDataset(User user, String id, String name, String description);
}
