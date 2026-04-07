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

# variables.tf

# GCP Project ID where resources will be deployed
variable "project_id" {
  description = "The GCP project ID."
  type        = string
}

# GCP Region where the Cloud SQL instance will be created
variable "region" {
  description = "The GCP region for the Cloud SQL instance."
  type        = string
  default     = "us-central1" # Default region, can be overridden
}

# Name of the Cloud SQL instance
variable "instance_name" {
  description = "The name of the Cloud SQL instance."
  type        = string
}

# MySQL database version (e.g., MYSQL_8_0)
variable "database_version" {
  description = "The MySQL database version (e.g., MYSQL_8_0)."
  type        = string
  default     = "MYSQL_8_0"
}

# Cloud SQL Edition (Standard, Enterprise, Enterprise Plus)
variable "cloudsql_edition" {
  description = "The Cloud SQL edition for MySQL (STANDARD, ENTERPRISE, or ENTERPRISE_PLUS)."
  type        = string
  default     = "STANDARD"
  validation {
    condition     = contains(["STANDARD", "ENTERPRISE", "ENTERPRISE_PLUS"], upper(var.cloudsql_edition))
    error_message = "Cloud SQL edition must be 'STANDARD', 'ENTERPRISE', or 'ENTERPRISE_PLUS'."
  }
}

# Machine type (tier) for the Cloud SQL instance.
# If not specified, a default tier compatible with the chosen edition will be used.
variable "database_tier" {
  description = "The machine type (tier) for the Cloud SQL instance. Defaults based on edition if not set."
  type        = string
  default     = "db-custom-2-13312"
}

variable "database_charset" {
  description = "Theb charset to be used in the DB. Postgres databases only support 'UTF8' at creation time."
  type        = string
  default     = "UTF8"
}

variable "database_collation" {
  description = "The collation value. Postgress databases only support 'en_US.UTF8' at creation time"
  type        = string
  default     = "en_US.UTF8"
}

# Availability type for the Cloud SQL instance (REGIONAL or ZONAL).
variable "availability_type" {
  description = "The availability type for the Cloud SQL instance (REGIONAL or ZONAL). REGIONAL provides higher availability."
  type        = string
  default     = "REGIONAL"
  validation {
    condition     = contains(["REGIONAL", "ZONAL"], upper(var.availability_type))
    error_message = "Availability type must be 'REGIONAL' or 'ZONAL'."
  }
}

# Root password for the MySQL instance. Highly recommended to use a secrets manager.
variable "root_password" {
  description = "The root password for the MySQL instance. Highly recommended to use a secrets manager."
  type        = string
  sensitive   = true # Mark as sensitive to prevent outputting in logs
}

# Name of the database to be created
variable "database_name" {
  description = "The name of the database to create within the instance."
  type        = string
  default     = "app_database"
}

# Username for the application database user
variable "database_user_name" {
  description = "The username for the application database user."
  type        = string
  default     = "app_user"
}

# Password for the application database user. Highly recommended to use a secrets manager.
variable "database_user_password" {
  description = "The password for the application database user. Highly recommended to use a secrets manager."
  type        = string
  sensitive   = true # Mark as sensitive
}

# List of MySQL database flags to set.
# Map of MySQL database flags to set (name = value).
# Example: { "long_query_time" = "1.0", "default_authentication_plugin" = "mysql_native_password" }
variable "mysql_database_flags" {
  description = "A map of MySQL database flags to set on the instance (name = value)."
  type        = map(string)
  default = {
    "default_authentication_plugin" = "mysql_native_password"
  }
}

# Enable or disable deletion protection for the Cloud SQL instance.
variable "enable_deletion_protection" {
  description = "Set to true to enable deletion protection for the Cloud SQL instance. Defaults to false."
  type        = bool
  default     = false
}

# Enable or disable automated backups for the Cloud SQL instance.
variable "enable_backup_configuration" {
  description = "Set to true to enable automated backups for the Cloud SQL instance. Defaults to false."
  type        = bool
  default     = false
}

# Enable or disable automated backups for the Cloud SQL instance.
variable "backup_start_time" {
  description = "Time to start the backup of the database"
  type        = string
  default     = "02:00"
}

# The size of the data disk in GB.
variable "disk_size" {
  description = "The size of the data disk in GB."
  type        = number
  default     = 100
}

# The type of the data disk. Defaults to 'PD_SSD'.
variable "disk_type" {
  description = "The type of the data disk. Defaults to 'PD_SSD'."
  type        = string
  default     = "PD_SSD"
  validation {
    condition     = contains(["PD_SSD", "PD_HDD"], upper(var.disk_type))
    error_message = "Disk type must be 'PD_SSD' or 'PD_HDD'."
  }
}