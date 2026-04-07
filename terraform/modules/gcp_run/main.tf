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

/* Cloud Run Module
*
*   This module simplifies the deployment of a Cloud Run Service
*/

resource "google_cloud_run_v2_service" "default" {
  project             = var.project_id
  name                = var.cloud_run_service_name
  location            = var.region
  deletion_protection = var.deletion_protection_enabled
  ingress             = var.ingress_type_cloud_run

  template {
    containers {
      image = var.cloud_run_image
      # Set environment variables
      # Resource limits and CPU boost
      resources {
        limits = {
          cpu    = var.cloud_run_cpu_limit
          memory = var.cloud_run_memory_limit
        }
        startup_cpu_boost = var.cloud_run_startup_cpu_boost
      }

      # Dynamically configure environment variables
      dynamic "env" {
        for_each = var.cloud_run_env_vars
        content {
          name = env.value.name

          # Set value if it's a plain string
          value = lookup(env.value, "value", null)

          # Set value_source if it refers to a secret
          dynamic "value_source" {
            for_each = lookup(env.value, "value_from_secret", null) != null ? [env.value.value_from_secret] : []
            content {
              secret_key_ref {
                secret  = value_source.value.secret
                version = value_source.value.version
              }
            }
          }
        }
      }

      # Dynamically configure volume mounts based on the provided map
      dynamic "volume_mounts" {
        for_each = var.volume_mounts_map
        content {
          name       = volume_mounts.key
          mount_path = volume_mounts.value
        }
      }
    }

    # Configure scaling settings
    scaling {
      min_instance_count = var.cloud_run_min_instances
      max_instance_count = var.cloud_run_max_instances
    }

    # Configure maximum number of requests per instance
    max_instance_request_concurrency = var.cloud_run_max_instance_concurrency

    # Dynamically configure volumes based on the provided list of maps
    dynamic "volumes" {
      for_each = var.volumes
      content {
        name = volumes.value.name

        # Configure secret volume type if specified
        dynamic "secret" {
          for_each = lookup(volumes.value, "secret", null) != null ? [volumes.value.secret] : []
          content {
            secret = secret.value.secret
            # Configure secret items if specified
            dynamic "items" {
              for_each = lookup(secret.value, "items", null) != null ? secret.value.items : []
              content {
                path    = items.value.path
                mode    = items.value.mode
                version = "latest"
              }
            }
          }
        }
        # Configure empty_dir volumes type if specified
        dynamic "empty_dir" {
          for_each = lookup(volumes.value, "empty_dir", null) != null ? [volumes.value.empty_dir] : []
          content {
            medium     = lookup(empty_dir.value, "medium", null)
            size_limit = lookup(empty_dir.value, "size_limit", null)
          }
        }

        # Configure cloud_sql_instance volumes type if specified
        dynamic "cloud_sql_instance" {
          for_each = lookup(volumes.value, "cloud_sql_instance", null) != null ? [volumes.value.cloud_sql_instance] : []
          content {
            instances = lookup(cloud_sql_instance.value, "instances", null)
          }
        }

      }
    }
  }
  scaling {
    min_instance_count = var.cloud_run_min_instances
  }

  # Traffic management for Cloud Run v2
  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  lifecycle {
    ignore_changes = [
      template.0.containers.0.image,
      client,
      client_version,
      build_config
    ]
  }
}

#--- IAM Policy for Cloud Run v2 ---
# This makes the Cloud Run service publicly accessible
# This requires DRS Exemption in case it is used in the google.com org
# go/cute-drs
resource "google_cloud_run_v2_service_iam_member" "noauth" {
  count    = var.allow_unauthenticated == true ? 1 : 0
  location = google_cloud_run_v2_service.default.location
  project  = google_cloud_run_v2_service.default.project
  name     = google_cloud_run_v2_service.default.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
