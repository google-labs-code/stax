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

package com.planck.planck.exceptions;

import com.planck.planck.feature.exception.FeatureDisabledException;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Global exception handler to manage application-specific exceptions. */
@ControllerAdvice
public class GlobalExceptionHandler {

  @Data
  @AllArgsConstructor
  @ResponseBody
  private static class ErrorResponse {

    private String error;
    private String type;
  }

  @Data
  @ResponseBody
  private static class ErrorResponseNotFound {

    private boolean success;
    private String type;
    private String message;

    public ErrorResponseNotFound(String message) {
      this.success = false;
      this.type = "error";
      this.message = message;
    }
  }

  /** Handles all exceptions and returns a standard error response. */
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
    ex.printStackTrace();
    String combinedMessage = "Unexpected Error: Something went wrong.";
    if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
      combinedMessage += " Details: " + ex.getMessage();
    }
    return buildErrorResponse(
        combinedMessage, "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Handles email-related exceptions. */
  @ExceptionHandler(EmailAlreadyExistsException.class)
  protected ResponseEntity<Object> handleEmailAlreadyExists(
      EmailAlreadyExistsException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "EMAIL_EXIST", HttpStatus.CONFLICT);
  }

  /** Handles input validation and authentication exceptions. */
  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<Object> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "ILLEGAL_ARGUMENT", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalInputException.class)
  protected ResponseEntity<Object> handleIllegalInputException(
      IllegalInputException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "ILLEGAL_INPUT", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {

    String parameterName = ex.getName();
    String invalidValue = ex.getValue() == null ? "null" : ex.getValue().toString();
    Class<?> requiredType = ex.getRequiredType();

    String errorMessage = String.format("Invalid value for parameter '%s'", parameterName);

    if (requiredType != null && requiredType.isEnum()) {
      String allowedValues =
          Arrays.stream(requiredType.getEnumConstants())
              .map(Object::toString)
              .collect(Collectors.joining(", "));
      errorMessage =
          String.format(
              "Invalid value '%s' for parameter '%s'. Allowed values are: [%s]",
              invalidValue, parameterName, allowedValues);
    }

    return buildErrorResponse(errorMessage, "ILLEGAL_INPUT", HttpStatus.BAD_REQUEST);
  }

  /** Handles user-related exceptions. */
  @ExceptionHandler(UserAccountDisabledException.class)
  protected ResponseEntity<Object> handleUserAccountDisabledException(
      UserAccountDisabledException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "USER_ACCOUNT_DISABLED", HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(UserNotFoundException.class)
  protected ResponseEntity<Object> handleUserNotFoundException(
      UserNotFoundException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "USER_NOT_FOUND", HttpStatus.UNAUTHORIZED);
  }

  /** Handles rate limiting exceptions. */
  @ExceptionHandler(RateLimitExceedException.class)
  protected ResponseEntity<Object> handleRateLimitExceedException(
      RateLimitExceedException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "RATE_LIMIT_EXCEED", HttpStatus.TOO_MANY_REQUESTS);
  }

  /** Handles resource limiting exceptions. */
  @ExceptionHandler(ResourceLimitExceedException.class)
  protected ResponseEntity<Object> handleResourceLimitExceedException(
      ResourceLimitExceedException ex, WebRequest request) {
    return buildErrorResponse(
        ex.getMessage(), "RESOURCE_LIMIT_EXCEED", HttpStatus.TOO_MANY_REQUESTS);
  }

  @ExceptionHandler(ApiKeyIsMissingException.class)
  protected ResponseEntity<Object> handleApiKeyIsMissingException(
      ApiKeyIsMissingException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "API_KEY_MISSING", HttpStatus.BAD_REQUEST);
  }

  /** Central method to build error responses. */
  private ResponseEntity<Object> buildErrorResponse(
      String message, String errorType, HttpStatus status) {
    ErrorResponse errorResponse = new ErrorResponse(message, errorType);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(DuplicateRecordException.class)
  public ResponseEntity<Object> handleDuplicateTemplateException(
      DuplicateRecordException duplicateRecordException, WebRequest webRequest) {
    return buildErrorResponse(
        duplicateRecordException.getMessage(), "RECORD ALREADY EXISTS", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(GoogleAuthCodeException.class)
  public ResponseEntity<Object> handleGoogleTokenException(
      GoogleAuthCodeException googleAuthCodeException, WebRequest webRequest) {
    return buildErrorResponse(
        googleAuthCodeException.getMessage(), "INVALID GOOGLE AUTHCODE", HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(LlmEngineOverloadedException.class)
  protected ResponseEntity<Object> handleLLMEngineOverloadedException(
      LlmEngineOverloadedException ex, WebRequest request) {
    return buildErrorResponse(
        ex.getMessage(), "LLM_ENGINE_OVERLOADED", HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(LlmForbiddenException.class)
  public ResponseEntity<Object> handleLLMForbiddenException(
      LlmForbiddenException llmForbiddenException, WebRequest webRequest) {
    return buildErrorResponse(
        llmForbiddenException.getMessage(), "LLM_FORBIDDEN_EXCEPTION", HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(LlmProviderException.class)
  public ResponseEntity<Object> handleLLMProviderException(
      LlmProviderException llmProviderException, WebRequest webRequest) {
    return buildErrorResponse(
        llmProviderException.getMessage(), "LLM_PROVIDER_EXCEPTION", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UserQuotaExceededException.class)
  public ResponseEntity<Object> handleUserQoutaExceededException(
      UserQuotaExceededException userQuotaExceededException, WebRequest webRequest) {
    return buildErrorResponse(
        userQuotaExceededException.getMessage(), "USER_QUOTA_EXCEEDED", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(LlmRateLimitException.class)
  protected ResponseEntity<Object> handleLLMRateLimitException(
      LlmRateLimitException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "LLM_RATE_LIMIT", HttpStatus.TOO_MANY_REQUESTS);
  }

  @ExceptionHandler(LlmServerException.class)
  protected ResponseEntity<Object> handleLLMServerException(
      LlmServerException ex, WebRequest request) {
    return buildErrorResponse(
        ex.getMessage(), "LLM_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ModelNotFoundException.class)
  public ResponseEntity<Object> handleModelNotFoundException(
      ModelNotFoundException modelNotFoundException, WebRequest webRequest) {
    return buildErrorResponse(
        modelNotFoundException.getMessage(), "MODEL_NOT_FOUND", HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodNotSupportException.class)
  public ResponseEntity<Object> handleMethodNotSupportException(
      MethodNotSupportException methodNotSupportException, WebRequest webRequest) {
    return buildErrorResponse(
        methodNotSupportException.getMessage(),
        "METHOD_NOT_SUPPORT",
        HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Object> handleNotFoundException(
      NotFoundException notFoundException, WebRequest webRequest) {
    ErrorResponseNotFound errorResponseNotFound =
        new ErrorResponseNotFound(notFoundException.getMessage());
    return new ResponseEntity<>(errorResponseNotFound, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
    ex.printStackTrace();
    return buildErrorResponse(ex.getMessage(), "RUNTIME_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(EvaluatorNameExistException.class)
  protected ResponseEntity<Object> handleScorerNameException(
      EvaluatorNameExistException evaluatorNameExistException, WebRequest webRequest) {
    return buildErrorResponse(
        evaluatorNameExistException.getMessage(), "EVALUATOR_NAME_EXIST", HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Object> handleValidationException(
      ValidationException ex, WebRequest request) {
    List<Map<String, String>> errors = ex.getErrors();
    BadRequestResponse response = new BadRequestResponse(errors);
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ResourceAlreadyExistsException.class)
  protected ResponseEntity<Object> handleResourceAlreadyExistsException(
      ResourceAlreadyExistsException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "RESOURCE_ALREADY_EXISTS", HttpStatus.CONFLICT);
  }

  @ExceptionHandler(NotImplementedException.class)
  protected ResponseEntity<Object> handleNotImplementedException(
      NotImplementedException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "NOT_IMPLEMENTED", HttpStatus.NOT_IMPLEMENTED);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<BadRequestResponse> handleConstraintViolation(
      ConstraintViolationException ex) {
    List<Map<String, String>> errors =
        ex.getConstraintViolations().stream().map(cv -> Map.of("error", cv.getMessage())).toList();

    return ResponseEntity.badRequest().body(new BadRequestResponse(errors));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationError(
      MethodArgumentNotValidException ex, WebRequest request) {
    List<String> errors = new ArrayList<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.add(error.getField() + ": " + error.getDefaultMessage()));
    ex.getBindingResult()
        .getGlobalErrors()
        .forEach(error -> errors.add(error.getObjectName() + ": " + error.getDefaultMessage()));

    String message =
        "Invalid Data: Please check the format and values of the data you submitted. Correct the"
            + " highlighted fields. Details: "
            + String.join(", ", errors);
    return buildErrorResponse(message, "INVALID_DATA", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceDeletionException.class)
  protected ResponseEntity<Object> handleResourceDeletionException(
      ResourceDeletionException ex, WebRequest request) {

    String message =
        String.format(
            "Failed to delete resource of type %s with ID %s. %s",
            ex.getResourceClass().getSimpleName(), ex.getResourceId(), ex.getMessage());

    return buildErrorResponse(
        message, "RESOURCE_DELETION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(UserAccessDeniedException.class)
  protected ResponseEntity<Object> handleUserAccessDeniedException(
      UserAccessDeniedException ex, WebRequest request) {
    return buildErrorResponse(ex.getMessage(), "USER_ACCESS_DENIED", HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex) {
    return buildErrorResponse(
        "Path not found: " + ex.getResourcePath(), "PATH_NOT_FOUND", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(FeatureDisabledException.class)
  public ResponseEntity<Object> handleFeatureDisabledException(FeatureDisabledException ex) {
    return buildErrorResponse("Path not found", "PATH_NOT_FOUND", HttpStatus.NOT_FOUND);
  }
}
