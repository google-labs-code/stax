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

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
public class GCSFileProcessingController {

  private final Storage storage = StorageOptions.getDefaultInstance().getService();

  @PostMapping({"/process"})
  ResponseEntity<String> handleCloudEvent(@RequestBody CloudEvent cloudEvent)
      throws InvalidProtocolBufferException {

    // CloudEvent information
    log.info("Id: {}", cloudEvent.getId());
    log.info("Source: {}", cloudEvent.getSource());
    log.info("Type: {}", cloudEvent.getType());

    String json = new String(cloudEvent.getData().toBytes());
    StorageObjectData.Builder builder = StorageObjectData.newBuilder();

    // If you do not ignore unknown fields, then JsonFormat.Parser returns an
    // error when encountering a new or unknown field. Note that you might lose
    // some event data in the unmarshaling process by ignoring unknown fields.
    JsonFormat.Parser parser = JsonFormat.parser().ignoringUnknownFields();
    parser.merge(json, builder);
    StorageObjectData data = builder.build();

    // Convert protobuf timestamp to java Instant
    Timestamp ts = data.getUpdated();
    Instant updated = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());

    // Get the file from Cloud Storage
    Blob blob = storage.get(data.getBucket(), data.getName());

    // Print detailed blob metadata
    log.info("\n=== Blob Metadata ===");
    log.info("Bucket: {}", blob.getBucket());
    log.info("Name: {}", blob.getName());
    log.info("Generation: {}", blob.getGeneration());
    log.info("Size: {} bytes", blob.getSize());
    log.info("Content Type: {}", blob.getContentType());
    log.info("MD5 Hash: {}", blob.getMd5());
    log.info("Created: {}", blob.getCreateTime());
    log.info("Updated: {}", blob.getUpdateTime());
    log.info("Custom Metadata: {}", blob.getMetadata());
    log.info("===================\n");

    // Read file contents
    String content = new String(blob.getContent(), StandardCharsets.UTF_8);

    // Print first line of content
    String firstLine = content.split("\n")[0];
    log.info("=== First Line of Content ===");
    log.info(firstLine);
    log.info("============================\n");

    String msg =
        String.format(
            "Cloud Storage object changed: %s/%s modified at %s%n"
                + "Content Type: %s%n"
                + "Size: %d bytes%n"
                + "MD5 Hash: %s%n"
                + "Content: %s%n",
            data.getBucket(),
            data.getName(),
            updated,
            blob.getContentType(),
            blob.getSize(),
            blob.getMd5(),
            content);

    log.info(msg);

    return ResponseEntity.ok().body(msg);
  }

  // Handle exceptions from CloudEvent Message Converter
  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid CloudEvent received")
  public void noop() {
    return;
  }
}
