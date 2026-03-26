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

package com.planck.planck.domain.dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.dataset.dto.DataSetDTO;
import com.planck.planck.domain.dataset.dto.DataSetListResponse;
import com.planck.planck.domain.dataset.dto.DataSetUpdateRequest;
import com.planck.planck.domain.dataset.service.DataSetServiceImpl;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringService;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringService;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.domain.project.EvaluationContainerRepository;
import com.planck.planck.domain.project.EvaluationContainerServiceImpl;
import com.planck.planck.domain.workbook.WorkbookService;
import com.planck.planck.entitities.DataSet;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.DataSetType;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.exceptions.NotFoundException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataSetServiceTest {
  @Mock private DataSetRepository dataSetRepository;
  @Mock private EvaluationContainerRepository evaluationContainerRepository;
  @Mock private WorkbookService workbookService;
  @Mock private ChatTurnRepository chatTurnRepository;
  @Mock private InferenceMonitoringService inferenceMonitoringService;
  @Mock private EvaluationMonitoringService evaluationMonitoringService;

  @Mock private JobStatusService jobStatusService;
  @Mock private ModelService modelService;

  @Mock private ModelResponseService modelResponseService;

  @Mock private ModelInputService modelInputService;

  @Mock private ApplicationLimits applicationLimits;

  @Mock private HumanEvaluatorService humanEvaluatorService;

  @Mock private ChatService chatService;

  private final String dataSetId = "dataset-123";

  @InjectMocks private DataSetServiceImpl dataSetService;

  @InjectMocks private EvaluationContainerServiceImpl evaluationContainerService;
  private User user;

  @BeforeEach
  public void setUp() {
    // Initialize user or other required objects for testing
    user = new User();
    user.setId("testUserId");
  }

  @Test
  public void testGetAllDataSets() {
    DataSet dataSet1 = new DataSet();
    dataSet1.setId("uid");
    dataSet1.setName("dataset1");
    dataSet1.setDescription("desc1");
    dataSet1.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    dataSet1.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    dataSet1.setUser(user);
    dataSet1.setType(DataSetType.USER);

    DataSet dataSet2 = new DataSet();
    dataSet2.setId("uid2");
    dataSet2.setName("dataset2");
    dataSet2.setDescription("desc2");
    dataSet2.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    dataSet2.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    dataSet2.setUser(user);
    dataSet2.setType(DataSetType.SYSTEM);

    List<DataSet> mockDataSets = Arrays.asList(dataSet1, dataSet2);
    when(dataSetRepository.findAllByUser(user)).thenReturn(mockDataSets);

    // When
    DataSetListResponse response = dataSetService.getAllDataSets(user, false);

    // Then
    assertNotNull(response);
    assertEquals(1, response.getUserDataSets().size());
    assertEquals(1, response.getSystemDataSets().size());
  }

  @Test
  public void testCreateDataSet() {
    // Given
    DataSetDTO dataSetDTO =
        new DataSetDTO(
            "uid",
            "New Dataset",
            EvaluationType.POINTWISE,
            "A new dataset",
            new Timestamp(System.currentTimeMillis()),
            new Timestamp(System.currentTimeMillis()));
    DataSet dataSet = new DataSet();
    dataSet.setId("uid");
    dataSet.setName("New Dataset");
    dataSet.setDescription("A new dataset");
    dataSet.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    dataSet.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    dataSet.setUser(user);
    dataSet.setType(DataSetType.USER);

    when(dataSetRepository.countByUser(user)).thenReturn(0L);
    when(dataSetRepository.save(any(DataSet.class))).thenReturn(dataSet);
    when(applicationLimits.getMaxDataSets()).thenReturn(10);
    // When
    DataSetDTO result =
        new DataSetDTO(
            dataSetService.createDataSet(
                user, dataSetDTO.getName(), dataSetDTO.getDescription(), null));

    // Then
    assertNotNull(result);
    assertEquals(dataSetDTO.getName(), result.getName());
    verify(dataSetRepository, times(1)).save(any(DataSet.class));
  }

  @Test
  public void testUpdateDataset() {
    // Given
    String dataSetId = "existingDatasetId";
    DataSetUpdateRequest updateRequest =
        new DataSetUpdateRequest("Updated Dataset", "Updated description");
    DataSet mockDataSet = new DataSet();
    mockDataSet.setId(dataSetId);
    mockDataSet.setName("Old Dataset");
    mockDataSet.setDescription("Old description");
    when(dataSetRepository.findUserDataSetById(dataSetId, user)).thenReturn(mockDataSet);
    when(dataSetRepository.save(mockDataSet)).thenReturn(mockDataSet);

    // When
    DataSetDTO updatedDataSet =
        dataSetService.updateDataset(
            user, dataSetId, updateRequest.getName(), updateRequest.getDescription());

    // Then
    assertNotNull(updatedDataSet);
    assertEquals(updateRequest.getName(), updatedDataSet.getName());
    assertEquals(updateRequest.getDescription(), updatedDataSet.getDescription());
    verify(dataSetRepository, times(1)).save(mockDataSet);
  }

  @Test
  void testDeleteDataSetById() {
    // Given
    String dataSetId = "datasetToDelete";
    DataSet mockDataSet = new DataSet();
    mockDataSet.setId(dataSetId);
    mockDataSet.setUser(user);

    when(evaluationContainerRepository.findById(dataSetId)).thenReturn(Optional.of(mockDataSet));

    when(chatService.findAllByUserAndChatContainer(user, mockDataSet))
        .thenReturn(Collections.emptyList());
    when(evaluationContainerRepository.deleteByUserAndId(user, dataSetId)).thenReturn(1L);

    evaluationContainerService.deleteContainer(user, dataSetId);

    verify(chatService, times(1)).findAllByUserAndChatContainer(user, mockDataSet);
    verify(chatService, times(1)).delete(anyList());
    verify(modelResponseService, times(1)).deleteByContainer(mockDataSet);
    verify(evaluationContainerRepository, times(1)).deleteByUserAndId(user, dataSetId);
  }

  @Test
  public void testGetDataSetById_NotFound() {
    // Given
    String dataSetId = "nonExistentId";
    when(dataSetRepository.findById(dataSetId, user)).thenReturn(null);

    // When / Then
    assertThrows(NotFoundException.class, () -> dataSetService.getDataSetById(user, dataSetId));
  }

  @Test
  void testGetDataSetForUser_returnsDataSet() {
    DataSet dataSet = new DataSet();
    dataSet.setId(dataSetId);
    when(dataSetRepository.findById(dataSetId, user)).thenReturn(dataSet);
    assertEquals(new DataSetDTO(dataSet), dataSetService.getDataSetById(user, dataSetId));
  }
}
