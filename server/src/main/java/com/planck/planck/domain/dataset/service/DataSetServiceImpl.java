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

import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.dataset.DataSetRepository;
import com.planck.planck.domain.dataset.dto.DataSetDTO;
import com.planck.planck.domain.dataset.dto.DataSetListResponse;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.domain.tags.TagService;
import com.planck.planck.entitities.DataSet;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.DataSetType;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.exceptions.ResourceLimitExceedException;
import com.planck.planck.util.PlanckConstants;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataSetServiceImpl implements DataSetService {

  private static final Logger log = LoggerFactory.getLogger(DataSetServiceImpl.class);

  @Autowired private DataSetRepository dataSetRepository;

  @Autowired private ChatTurnRepository chatTurnRepository;

  @Autowired private ModelService modelService;

  @Autowired private ModelResponseService modelResponseService;

  @Autowired private ModelInputService modelInputservice;

  @Autowired private HumanEvaluatorService humanEvaluatorService;

  @Autowired private ApplicationLimits applicationLimits;

  @Autowired private ChatService chatService;
  @Autowired private TagService tagService;
  @Autowired private TagLinkService tagLinkService;

  private static final Pattern MISSING_COLUMN_PATTERN =
      Pattern.compile("Mapping for (.+?) not found, expected one of (\\[.+\\])");

  @Override
  public DataSetListResponse getAllDataSets(User user, Boolean includeHidden) {
    List<DataSet> datasets = dataSetRepository.findAllByUser(user);
    DataSetListResponse response = new DataSetListResponse();
    List<DataSetDTO> systemDatasets = getDataSetList(datasets, DataSetType.SYSTEM);
    List<DataSetDTO> userDatasets = getDataSetList(datasets, DataSetType.USER);
    List<DataSetDTO> communityDatasets = getDataSetList(datasets, DataSetType.COMMUNITY);

    response.setSystemDataSets(systemDatasets);
    response.setUserDataSets(userDatasets);
    response.setCommunityDataSets(communityDatasets);
    return response;
  }

  private List<DataSetDTO> getDataSetList(List<DataSet> datasets, DataSetType type) {
    return datasets.stream()
        .filter(dataset -> dataset.getType() == type)
        .map(DataSetDTO::new)
        .toList();
  }

  @Override
  public DataSetDTO getDataSetById(User user, String id) {
    DataSet dataSet = dataSetRepository.findById(id, user);
    if (dataSet == null) {
      throw new NotFoundException("Dataset not found for id: " + id);
    }

    return new DataSetDTO(dataSet);
  }

  @Transactional
  @Override
  public DataSet createDataSet(User user, String name, String description, EvaluationType type) {
    DataSet dataSet = new DataSet();
    dataSet.setUser(user);
    dataSet.setName(name);
    dataSet.setDescription(description);
    dataSet.setType(DataSetType.USER);
    dataSet.setEvaluationType(type);

    long num_datasets = dataSetRepository.countByUser(user);

    if (num_datasets + 1 > applicationLimits.getMaxDataSets())
      throw new ResourceLimitExceedException(
          String.format(
              "Creation would result in exceeding the limit of %d data sets.",
              applicationLimits.getMaxDataSets()));

    if (name == null || name.isEmpty()) {
      dataSet.setName(String.format(PlanckConstants.DEFAULT_DATASET_NAME_FORMAT, num_datasets + 1));
    }

    return dataSetRepository.save(dataSet);
  }

  @Transactional
  @Override
  public DataSetDTO updateDataset(User user, String id, String name, String description) {
    DataSet dataSet = getUserDataSetById(user, id);

    if (name != null && !name.isEmpty()) {
      dataSet.setName(name);
    }
    if (description != null) {
      dataSet.setDescription(description);
    }

    DataSet updatedDataSet = dataSetRepository.save(dataSet);

    return new DataSetDTO(updatedDataSet);
  }

  private DataSet getUserDataSetById(User user, String id) {
    DataSet dataSet = dataSetRepository.findUserDataSetById(id, user);
    if (dataSet == null) {
      throw new NotFoundException("Dataset not found for id: " + id);
    }

    return dataSet;
  }
}
