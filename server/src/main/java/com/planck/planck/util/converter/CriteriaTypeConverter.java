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

package com.planck.planck.util.converter;

import com.planck.planck.enums.CriteriaType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class CriteriaTypeConverter implements AttributeConverter<CriteriaType, String> {

  @Override
  public String convertToDatabaseColumn(CriteriaType attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.name();
  }

  @Override
  public CriteriaType convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    return Stream.of(CriteriaType.values())
        .filter(c -> c.name().equals(dbData))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
