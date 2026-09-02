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
  # TODO: change this to your email
  # member                  = "user:you@gmail.com"
  member                  = "user:xinyij@google.com"
  default_service_account = "serviceAccount:${data.google_project.default_project.number}-compute@developer.gserviceaccount.com"
}

resource "google_project_iam_member" "admin" {
  project = data.google_project.default_project.project_id
  role    = "roles/admin"

  member = local.member
}

resource "google_project_iam_binding" "storage_admin" {
  project = data.google_project.default_project.project_id
  role    = "roles/storage.objectAdmin"

  members = [local.member, "serviceAccount:${local.service_account}"]
}

resource "google_project_iam_binding" "sql_client" {
  project = data.google_project.default_project.project_id
  role    = "roles/cloudsql.client"

  members = [local.member, "serviceAccount:${local.service_account}"]
}

resource "google_project_iam_binding" "iam_id_token_creator" {
  project = data.google_project.default_project.project_id
  role    = "roles/iam.serviceAccountOpenIdTokenCreator"

  members = [local.member, "serviceAccount:${local.service_account}"]
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
