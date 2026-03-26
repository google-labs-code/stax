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

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;

@Component
public class CsvRecordProvider implements RecordProvider {

  @Override
  public boolean supports(String contentType) {
    return "text/csv".equalsIgnoreCase(contentType);
  }

  @Override
  public Stream<DataRecord> getRecords(InputStream inputStream) throws IOException {
    var csvParser =
        new CSVParserBuilder()
            .withSeparator(',')
            .withQuoteChar('"')
            .withEscapeChar(ICSVParser.NULL_CHARACTER)
            .build();

    var reader =
        new CSVReaderBuilder(new InputStreamReader(inputStream)).withCSVParser(csvParser).build();

    try {
      String[] headers = reader.readNext();
      if (headers == null || headers.length == 0) {
        return Stream.empty();
      }

      Map<String, Integer> headerMap =
          IntStream.range(0, headers.length)
              .boxed()
              .collect(Collectors.toMap(i -> headers[i].trim(), i -> i, (a, b) -> b));

      return StreamSupport.stream(new CsvSpliterator(reader, headerMap), false)
          .onClose(
              () -> {
                try {
                  reader.close();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    } catch (CsvValidationException e) {
      throw new IOException("Failed to validate CSV headers", e);
    }
  }

  private static class CsvDataRecord implements DataRecord {
    private final String[] line;
    private final Map<String, Integer> headerMap;
    private final int recordNumber;

    public CsvDataRecord(String[] line, Map<String, Integer> headerMap, int recordNumber) {
      this.line = line;
      this.headerMap = headerMap;
      this.recordNumber = recordNumber;
    }

    @Override
    public String getValue(String fieldName) {
      if (fieldName == null || !headerMap.containsKey(fieldName)) return null;
      Integer index = headerMap.get(fieldName);
      return (index != null && index < line.length) ? line[index] : null;
    }

    @Override
    public int getRecordNumber() {
      return this.recordNumber;
    }
  }

  private static class CsvSpliterator extends Spliterators.AbstractSpliterator<DataRecord> {
    private final CSVReader reader;
    private final Map<String, Integer> headerMap;
    private int rowCounter = 1;

    protected CsvSpliterator(CSVReader reader, Map<String, Integer> headerMap) {
      super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
      this.reader = reader;
      this.headerMap = headerMap;
    }

    @Override
    public boolean tryAdvance(Consumer<? super DataRecord> action) {
      try {
        String[] line = reader.readNext();
        if (line == null) {
          return false;
        }
        action.accept(new CsvDataRecord(line, headerMap, ++rowCounter));
        return true;
      } catch (IOException | CsvValidationException e) {
        throw new RuntimeException("Failed to read or parse CSV row", e);
      }
    }
  }
}
