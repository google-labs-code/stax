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

# --- Variables ---

variable "project_id" {
  description = "The GCP project ID."
  type        = string
}

variable "region" {
  description = "The GCP region where resources will be deployed (e.g., us-central1)."
  type        = string
  default     = "us-central1"
}

variable "allow_unauthenticated" {
  description = "Allow Unauthenticated invocations for the Cloud Run Service. This means to be publicly available. DRS exemption required (go/cute-drs)"
  type        = bool
  default     = false
}

variable "cloud_run_service_name" {
  description = "The name of the Cloud Run service."
  type        = string
  default     = "my-public-cloud-run-service-v2"
}

variable "cloud_run_image" {
  description = "The Docker image for the Cloud Run service (e.g., gcr.io/cloudrun/hello)."
  type        = string
  default     = "gcr.io/cloudrun/hello" # Example image
}


variable "cloud_run_cpu_limit" {
  description = "(Optional) CPU limit for the container (e.g., 1, 2, 4). Defaults to 1 CPU core."
  type        = number
  default     = 1
}

variable "cloud_run_memory_limit" {
  description = "(Optional) Memory limit for the container (e.g., '512Mi', '1Gi', '16Gi'). Defaults to '512Mi'."
  type        = string
  default     = "512Mi"
}

variable "cloud_run_startup_cpu_boost" {
  description = "(Optional) Enable or disable startup CPU boost for the container. Defaults to false."
  type        = bool
  default     = false
}

variable "cloud_run_env_vars" {
  description = "A list of environment variables to set for the container, supporting plain values or secrets."
  type = list(object({
    name  = string
    value = optional(string)
    value_from_secret = optional(object({
      secret  = string # The Secret Manager secret resource name (e.g., 'my-secret')
      version = string # The secret version (e.g., '1', 'latest', 'ABCDEFG')
    }))
  }))
  default = []
  validation {
    condition = alltrue([
      for env_var in var.cloud_run_env_vars :
      (lookup(env_var, "value", null) != null && lookup(env_var, "value_from_secret", null) == null) ||
      (lookup(env_var, "value", null) == null && lookup(env_var, "value_from_secret", null) != null)
    ])
    error_message = "Each environment variable must specify either 'value' or 'value_from_secret', but not both."
  }
}

variable "cloud_run_min_instances" {
  description = "The minimum number of instances to run for the Cloud Run service."
  type        = number
  default     = 1 # Use 1 instead of 0 as default
}

variable "cloud_run_max_instances" {
  description = "The maximum number of instances to run for the Cloud Run service."
  type        = number
  default     = 20
}

variable "cloud_run_max_instance_concurrency" {
  description = "The maximum number of requests that each serving instance can receive."
  type        = number
  default     = 80
}
variable "deletion_protection_enabled" {
  description = "Prevent the service to be deleted"
  type        = bool
  default     = false
}

variable "ingress_type_cloud_run" {
  description = "The ingress setting for the Cloud Run service. Valid values: INGRESS_TRAFFIC_ALL, INGRESS_TRAFFIC_INTERNAL_ONLY, INGRESS_TRAFFIC_INTERNAL_LOAD_BALANCER."
  type        = string
  default     = "INGRESS_TRAFFIC_ALL"
  validation {
    condition     = contains(["INGRESS_TRAFFIC_ALL", "INGRESS_TRAFFIC_INTERNAL_ONLY", "INGRESS_TRAFFIC_INTERNAL_LOAD_BALANCER"], var.ingress_type_cloud_run)
    error_message = "Valid values for ingress_type_cloud_run are INGRESS_TRAFFIC_ALL, INGRESS_TRAFFIC_INTERNAL_ONLY, or INGRESS_TRAFFIC_INTERNAL_LOAD_BALANCER."
  }
}

variable "volumes" {
  description = "A list of maps, where each map defines a volume configuration. Each map must have a 'name' key and can optionally include keys for 'secret', 'empty_dir', or 'cloud_sql_instance'."
  type = list(object({
    name = string
    secret = optional(object({
      secret = string
      items = optional(list(object({
        path = string
        mode = optional(number)
      })), [])
    }))
    empty_dir = optional(object({
      medium     = optional(string)
      size_limit = optional(string)
    }))
    cloud_sql_instance = optional(object({
      instances = optional(list(string))
    }))
  }))
  default = []
}

variable "volume_mounts_map" {
  description = "A map where keys are volume names and values are their mount paths within the container. (e.g., {my-secret = \"/etc/secrets/my-secret-file\"})."
  type        = map(string)
  default     = {}
}

variable "enable_ignores" {
  description = "Set to true to ignore all commonly changed attributes on the Cloud Run service."
  type        = bool
  default     = false
}
