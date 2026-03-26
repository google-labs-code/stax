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

package com.planck.planck.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.entitities.Model;
import com.planck.planck.util.ObjectMapperUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Response for custom endpoint registration including curl command examples")
public class CustomEndpointRegistrationResponse extends ModelDTO {

  @Schema(description = "Example curl command for making requests to this custom endpoint")
  @JsonProperty("curl_example")
  private String curlExample;

  @Schema(
      description =
          "Example curl command without API key (if endpoint doesn't require authentication)")
  @JsonProperty("curl_example_no_auth")
  private String curlExampleNoAuth;

  public CustomEndpointRegistrationResponse(Model model) {
    super(model);
    generateCurlExamples(model);
  }

  @SuppressWarnings("unchecked")
  private void generateCurlExamples(Model model) {
    Map<String, Object> properties = ObjectMapperUtil.convertJsonStringToMap(model.getProperties());
    boolean hasApiKey = properties.containsKey("encrypted_api_key");
    Map<String, String> additionalHeaders = null;

    if (properties.containsKey("additional_headers")) {
      additionalHeaders = (Map<String, String>) properties.get("additional_headers");
    }

    // Generate curl command with API key
    StringBuilder curlWithAuth = new StringBuilder();
    curlWithAuth.append("curl -X POST \\\n");
    curlWithAuth.append("  '").append(model.getUrl()).append("' \\\n");
    curlWithAuth.append("  -H 'Content-Type: application/json' \\\n");

    if (hasApiKey) {
      curlWithAuth.append("  -H 'Authorization: Bearer YOUR_API_KEY' \\\n");
    }

    if (additionalHeaders != null) {
      for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
        curlWithAuth
            .append("  -H '")
            .append(header.getKey())
            .append(": ")
            .append(header.getValue())
            .append("' \\\n");
      }
    }

    // Add example request body based on provider
    curlWithAuth.append("  -d '{\n");
    curlWithAuth.append("    \"model\": \"").append(model.getName()).append("\",\n");
    curlWithAuth.append("    \"messages\": [\n");
    curlWithAuth.append("      {\n");
    curlWithAuth.append("        \"role\": \"user\",\n");
    curlWithAuth.append("        \"content\": \"Hello, how are you?\"\n");
    curlWithAuth.append("      }\n");
    curlWithAuth.append("    ],\n");

    // Add provider-specific properties
    if (properties.containsKey("temperature")) {
      curlWithAuth
          .append("    \"temperature\": ")
          .append(properties.get("temperature"))
          .append(",\n");
    }
    if (properties.containsKey("max_tokens")) {
      curlWithAuth
          .append("    \"max_tokens\": ")
          .append(properties.get("max_tokens"))
          .append(",\n");
    }

    curlWithAuth.append("    \"stream\": false\n");
    curlWithAuth.append("  }'");

    this.curlExample = curlWithAuth.toString();

    // Generate curl command without API key
    StringBuilder curlNoAuth = new StringBuilder();
    curlNoAuth.append("curl -X POST \\\n");
    curlNoAuth.append("  '").append(model.getUrl()).append("' \\\n");
    curlNoAuth.append("  -H 'Content-Type: application/json' \\\n");

    if (additionalHeaders != null) {
      for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
        curlNoAuth
            .append("  -H '")
            .append(header.getKey())
            .append(": ")
            .append(header.getValue())
            .append("' \\\n");
      }
    }

    // Add example request body (same as above)
    curlNoAuth.append("  -d '{\n");
    curlNoAuth.append("    \"model\": \"").append(model.getName()).append("\",\n");
    curlNoAuth.append("    \"messages\": [\n");
    curlNoAuth.append("      {\n");
    curlNoAuth.append("        \"role\": \"user\",\n");
    curlNoAuth.append("        \"content\": \"Hello, how are you?\"\n");
    curlNoAuth.append("      }\n");
    curlNoAuth.append("    ],\n");

    if (properties.containsKey("temperature")) {
      curlNoAuth
          .append("    \"temperature\": ")
          .append(properties.get("temperature"))
          .append(",\n");
    }
    if (properties.containsKey("max_tokens")) {
      curlNoAuth.append("    \"max_tokens\": ").append(properties.get("max_tokens")).append(",\n");
    }

    curlNoAuth.append("    \"stream\": false\n");
    curlNoAuth.append("  }'");

    this.curlExampleNoAuth = curlNoAuth.toString();
  }

  public String getCurlExample() {
    return curlExample;
  }

  public void setCurlExample(String curlExample) {
    this.curlExample = curlExample;
  }

  public String getCurlExampleNoAuth() {
    return curlExampleNoAuth;
  }

  public void setCurlExampleNoAuth(String curlExampleNoAuth) {
    this.curlExampleNoAuth = curlExampleNoAuth;
  }
}
