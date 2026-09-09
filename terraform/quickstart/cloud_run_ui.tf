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

module "ui_service" {

  source     = "../modules/gcp_run"
  project_id = local.project_id
  region     = local.region

  cloud_run_service_name = "stax-ui"
  cloud_run_image        = "${local.region}-docker.pkg.dev/${local.project_id}/${var.artifact_registry_repo}/stax-ui"
  cloud_run_cpu_limit    = 4
  cloud_run_memory_limit = "2Gi"
  allow_unauthenticated  = true

  cloud_run_min_instances            = 1
  cloud_run_max_instances            = 20
  cloud_run_max_instance_concurrency = 80

  cloud_run_env_vars = [
    {
      name  = "PROTECTED_ROUTES",
      value = "pipeline,rag,datasets"
    },
    {
      name  = "DISABLE_ESLINT_PLUGIN",
      value = "true"
    },
    {
      name  = "TSC_COMPILE_ON_ERROR",
      value = "true"
    },
  ]
}
