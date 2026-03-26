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

import jakarta.persistence.AttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base converter class for storing enums as strings in the database. This avoids using
 * {@code @Enumerated}, which can generate unwanted constraints in some databases (e.g., CHECK
 * constraints). To use this converter, extend it for your specific enum and apply the converter to
 * the field using {@code @Convert}.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @Converter(autoApply = false)
 * public class MyEnumConverter extends AbstractEnumConverter<MyEnum> {
 *     public MyEnumConverter() {
 *         super(MyEnum.class);
 *     }
 * }
 * }</pre>
 *
 * @param <E> the enum type
 */
public abstract class AbstractEnumConverter<E extends Enum<E>>
    implements AttributeConverter<E, String> {

  private static final Logger logger = LoggerFactory.getLogger(AbstractEnumConverter.class);

  private final Class<E> enumClass;

  public AbstractEnumConverter(Class<E> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public String convertToDatabaseColumn(E attribute) {
    return attribute != null ? attribute.name() : null;
  }

  @Override
  public E convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }

    try {
      return Enum.valueOf(enumClass, dbData);
    } catch (IllegalArgumentException e) {
      logger.error(
          "Error while converting database value '{}' to enum of type '{}'",
          dbData,
          enumClass.getSimpleName(),
          e);
      return null;
    }
  }
}
