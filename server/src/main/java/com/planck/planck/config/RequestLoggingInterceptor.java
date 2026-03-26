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

package com.planck.planck.config;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    logRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    logResponse(response);
    return response;
  }

  private void logRequest(HttpRequest request, byte[] body) {
    log.debug("===========================request begin========================================");
    log.debug("URI         : {}", request.getURI());
    log.debug("Method      : {}", request.getMethod());
    // log.debug("Headers     : {}", request.getHeaders());
    // Avoid logging body for GET, potentially large/binary for others unless needed
    // if (body.length > 0) {
    log.debug("==========================request end===========================================");
  }

  private void logResponse(ClientHttpResponse response) throws IOException {
    // Log details *after* receiving the response
    log.debug("============================response begin========================================");
    log.debug("Status code  : {}", response.getStatusCode());
    log.debug("Status text  : {}", response.getStatusText());
    // log.debug("Headers      : {}", response.getHeaders());
    // Note: Reading the response body here might consume it, preventing downstream processing
    // Only read if absolutely necessary for debugging and if you handle potential side effects.
    // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(),
    // StandardCharsets.UTF_8));
    // String line = bufferedReader.readLine();
    // StringBuilder responseBody = new StringBuilder();
    // while (line != null) {
    //     responseBody.append(line);
    //     line = bufferedReader.readLine();
    // }
    // log.debug("Response body: {}", responseBody.toString());
    log.debug("=======================response end==============================================");
  }
}
