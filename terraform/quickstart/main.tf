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

provider "google" {
  # TODO: Prerequisite - change this to your project ID.
  project = "planck-opensource-test-769621"
}

data "google_project" "default_project" {}
data "google_compute_default_service_account" "default" {}

locals {
  project_id      = data.google_project.default_project.project_id
  region          = "us-central1"
  service_account = data.google_compute_default_service_account.default.email
}
