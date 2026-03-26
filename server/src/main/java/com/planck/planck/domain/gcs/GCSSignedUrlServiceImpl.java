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

package com.planck.planck.domain.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.planck.planck.domain.gcs.dto.GCSSignedUrlResponseDTO;
import com.planck.planck.entitities.User;
import com.planck.planck.util.PlanckEnvironmentUtil;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GCSSignedUrlServiceImpl implements GCSSignedUrlService {

  private static final Logger logger = LoggerFactory.getLogger(GCSSignedUrlServiceImpl.class);

  private static final int SIGNED_URL_EXPIRATION_MINUTES = 15;

  private String projectId;
  private String bucketId;

  @Override
  public GCSSignedUrlResponseDTO generateSignedUrl(
      String objectName,
      User user,
      String inputColumn,
      String outputColumn,
      String expectedOutputColumn) {
    try {
      bucketId = PlanckEnvironmentUtil.getProperty("spring.cloud.gcp.bucketId");
      projectId = PlanckEnvironmentUtil.getProperty("spring.cloud.gcp.projectId");

      Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
      Map<String, String> metadata = new HashMap<>();
      metadata.put("userId", user.getId());
      metadata.put("inputColumn", inputColumn);
      metadata.put("outputColumn", outputColumn);
      metadata.put("expectedOutputColumn", expectedOutputColumn);

      String objectPath = user.getId() + "/" + objectName;

      BlobId blobId = BlobId.of(bucketId, objectPath);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setMetadata(metadata).build();
      Map<String, String> extensionHeaders = new HashMap<>();
      extensionHeaders.put("Content-Type", "application/octet-stream");
      extensionHeaders.put("x-goog-meta-userId", user.getId());
      extensionHeaders.put("x-goog-meta-inputColumn", inputColumn);
      extensionHeaders.put("x-goog-meta-outputColumn", outputColumn);
      extensionHeaders.put("x-goog-meta-expectedOutputColumn", expectedOutputColumn);

      URL signedUrl =
          storage.signUrl(
              blobInfo,
              SIGNED_URL_EXPIRATION_MINUTES,
              TimeUnit.MINUTES,
              Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
              Storage.SignUrlOption.withExtHeaders(extensionHeaders),
              Storage.SignUrlOption.withV4Signature());

      GCSSignedUrlResponseDTO responseDTO = new GCSSignedUrlResponseDTO();
      responseDTO.setUrl(signedUrl.toString());

      return responseDTO;

    } catch (StorageException e) {
      logger.error("GCS Storage Exception. Type: {}", e.getClass().getName());
      GCSSignedUrlResponseDTO errorDTO = new GCSSignedUrlResponseDTO();
      errorDTO.setError("GCS Storage Exception: " + e.getMessage());
      return errorDTO;
    } catch (Exception e) {
      logger.error("Error generating signed URL. Type: {}", e.getClass().getName());
      GCSSignedUrlResponseDTO errorDTO = new GCSSignedUrlResponseDTO();
      errorDTO.setError("Error generating signed URL: " + e.getMessage());
      return errorDTO;
    }
  }
}
