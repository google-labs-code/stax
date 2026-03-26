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

package com.planck.planck.annotation;

import jakarta.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * Validator to check that at least one out of a list of fields is provided. Useful when there's
 * multiple input fields listed as optional, however a minimum of one must be provided.
 *
 * <p>Parameters:
 *
 * <ul>
 *   <li>fields: list of fields to check. (the java object field names)
 *   <li>message: the error message to be thrown in case validation doesn't pass. Default: 'At least
 *       one of the listed fields must be provided' Only checks for null. Be careful with using
 *       NotEmpty or NotBlank
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldValidator.class)
public @interface AtLeastOneFieldRequired {
  String message() default "At least one of the listed fields must be provided";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] fields(); // list of fields to check
}
