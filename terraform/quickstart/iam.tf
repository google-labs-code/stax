# Copyright 2025 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

locals {
  admin_member            = var.admin_email != "" ? (startswith(var.admin_email, "user:") || startswith(var.admin_email, "serviceAccount:") || startswith(var.admin_email, "group:") ? var.admin_email : "user:${var.admin_email}") : null
  default_service_account = "serviceAccount:${data.google_project.default_project.number}-compute@developer.gserviceaccount.com"
}

resource "google_project_iam_member" "admin" {
  count   = local.admin_member != null ? 1 : 0
  project = data.google_project.default_project.project_id
  role    = "roles/resourcemanager.projectIamAdmin"
  member  = local.admin_member
}

resource "google_project_iam_member" "storage_admin_user" {
  count   = local.admin_member != null ? 1 : 0
  project = data.google_project.default_project.project_id
  role    = "roles/storage.objectAdmin"
  member  = local.admin_member
}

resource "google_project_iam_member" "storage_admin_sa" {
  project = data.google_project.default_project.project_id
  role    = "roles/storage.objectAdmin"
  member  = "serviceAccount:${local.service_account}"
}

resource "google_project_iam_member" "sql_client_user" {
  count   = local.admin_member != null ? 1 : 0
  project = data.google_project.default_project.project_id
  role    = "roles/cloudsql.client"
  member  = local.admin_member
}

resource "google_project_iam_member" "sql_client_sa" {
  project = data.google_project.default_project.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${local.service_account}"
}

resource "google_project_iam_member" "iam_id_token_creator_user" {
  count   = local.admin_member != null ? 1 : 0
  project = data.google_project.default_project.project_id
  role    = "roles/iam.serviceAccountOpenIdTokenCreator"
  member  = local.admin_member
}

resource "google_project_iam_member" "iam_id_token_creator_sa" {
  project = data.google_project.default_project.project_id
  role    = "roles/iam.serviceAccountOpenIdTokenCreator"
  member  = "serviceAccount:${local.service_account}"
}


resource "google_project_iam_member" "compute_service_agent" {
  project = data.google_project.default_project.project_id
  role    = "roles/compute.serviceAgent"
  member  = "serviceAccount:${local.service_account}"
}

resource "google_project_iam_member" "cloudbuild_builder" {
  project = data.google_project.default_project.project_id
  role    = "roles/cloudbuild.builds.builder"
  member  = "serviceAccount:${local.service_account}"
}

resource "google_project_iam_member" "run_service_agent" {
  project = data.google_project.default_project.project_id
  role    = "roles/run.serviceAgent"
  member  = "serviceAccount:${local.service_account}"
}

resource "google_project_iam_member" "artifactregistry_admin" {
  project = data.google_project.default_project.project_id
  role    = "roles/artifactregistry.admin"
  member  = "serviceAccount:${local.service_account}"
}

resource "google_project_iam_member" "serviceusage_consumer" {
  project = data.google_project.default_project.project_id
  role    = "roles/serviceusage.serviceUsageConsumer"
  member  = "serviceAccount:${local.service_account}"
}
