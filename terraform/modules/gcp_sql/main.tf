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

# Create the Cloud SQL instance with Private Services Access enabled and no public IP.
resource "google_sql_database_instance" "main_instance" {
  project          = var.project_id
  database_version = var.database_version
  name             = var.instance_name
  region           = var.region

  settings {
    tier              = var.database_tier
    edition           = var.cloudsql_edition
    availability_type = var.availability_type # Use the availability_type variable
    disk_size         = var.disk_size
    disk_type         = var.disk_type

    backup_configuration {
      enabled            = var.enable_backup_configuration # Use the new variable
      binary_log_enabled = var.enable_backup_configuration # Binary logs enabled if backups are enabled
      start_time         = var.backup_start_time
    }

    dynamic "database_flags" {
      for_each = var.mysql_database_flags
      content {
        name  = database_flags.key   # Use the key as the flag name
        value = database_flags.value # Use the value as the flag value
      }
    }
  }

  root_password       = var.root_password
  deletion_protection = var.enable_deletion_protection # Use the new variable
}

# Create a database within the Cloud SQL instance
resource "google_sql_database" "app_database" {
  project   = var.project_id
  instance  = google_sql_database_instance.main_instance.name
  name      = var.database_name
  charset   = var.database_charset
  collation = var.database_collation
}

# Create a database user
resource "google_sql_user" "app_user" {
  project  = var.project_id
  instance = google_sql_database_instance.main_instance.name
  name     = var.database_user_name
  password = var.database_user_password
  host     = "%"
}