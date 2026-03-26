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

package com.planck.planck.util;

public interface PlanckConstants {

  String DEFAULT_PROJECT_NAME_FORMAT = "Evaluation %d";
  String DEFAULT_DATASET_NAME_FORMAT = "Untitled %d";
  String DEFAULT_EVALUATOR_CATEGORY_COLOR = "var(--color-secondary)";

  String JOBS_FAILED = "Jobs-Failed";
  String JOBS_STOPPED = "Jobs-Stopped";
  String JOBS_SUCCESSFUL = "Jobs-Successful";
  String JOBS_IN_PROGRESS = "Jobs-In-Progress";
  String JOBS_PENDING = "Jobs-Pending";

  String INFERENCE = "inference";
  String EVAL = "eval";
  String JOBS_DETAILS = "jobs_details";
  String AGGREGATE = "aggregate";
  String BULK = "bulk";
  String DEFAULT_USER_FIRSTNAME = "Default";
  String DEFAULT_USER_LASTNAME = "User";
  String DEFAULT_USER = "default@user.email";

  long MAX_PROJECTS_PER_USER = 50;

  // https://cloud.google.com/identity/docs/reference/rest/v1/groups.memberships/checkTransitiveMembership
  String GROUPS_API_URL =
      "https://cloudidentity.googleapis.com/v1/groups/%s/memberships:checkTransitiveMembership";

  String ALLOW_ALL_ALLOWLIST_STRING = "AllUsers";

  String CLOUD_IDENTITY_SCOPE = "https://www.googleapis.com/auth/cloud-identity.groups.readonly";

  String ADDITIONAL_HEADERS = "additional_headers";
  String API_KEY_PROPERTY_NAME = "encrypted_api_key";
}
