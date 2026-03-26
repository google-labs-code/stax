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

package com.planck.planck.domain.importexport.dto.record;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class JsonRecordProvider implements RecordProvider {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public boolean supports(String contentType) {
    return "application/json".equalsIgnoreCase(contentType);
  }

  @Override
  public Stream<DataRecord> getRecords(InputStream inputStream) throws IOException {
    List<Map<String, Object>> data = OBJECT_MAPPER.readValue(inputStream, new TypeReference<>() {});
    return IntStream.range(0, data.size()).mapToObj(i -> new JsonDataRecord(data.get(i), i + 1));
  }

  private static class JsonDataRecord implements DataRecord {
    private final Map<String, Object> data;
    private final int recordNumber;

    public JsonDataRecord(Map<String, Object> data, int recordNumber) {
      this.data = data;
      this.recordNumber = recordNumber;
    }

    @Override
    public String getValue(String fieldName) {
      Object value = data.get(fieldName);
      if (value == null) return null;

      if (value instanceof Map || value instanceof List) {
        try {
          return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          return value.toString();
        }
      }
      return String.valueOf(value);
    }

    @Override
    public int getRecordNumber() {
      return recordNumber;
    }
  }
}
