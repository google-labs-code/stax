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

variable "project_id" {
  type        = string
  description = "The GCP Project ID where Stax resources will be deployed."
  default     = ""
}

variable "region" {
  type        = string
  description = "The GCP region for resources."
  default     = "us-central1"
}

variable "admin_email" {
  type        = string
  description = "The email address of the administrator (e.g. user:you@example.com)."
  default     = ""
}

variable "artifact_registry_repo" {
  type        = string
  description = "The Artifact Registry repository name hosting the container images."
  default     = "cloud-run-source-deploy"
}

variable "gcs_bucket_name" {
  type        = string
  description = "The GCS bucket name for Stax project data storage."
  default     = "stax-project-bucket"
}
