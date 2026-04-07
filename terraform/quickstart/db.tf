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

# Call the Cloud SQL MySQL module
module "cloudsql_mysql_instance" {

  source = "../modules/gcp_sql"

  project_id        = data.google_project.default_project.project_id
  region            = local.region
  instance_name     = "mysql-db"
  database_version  = "MYSQL_8_4"
  cloudsql_edition  = "ENTERPRISE_PLUS"
  availability_type = "REGIONAL" # Pass the desired availability type
  database_tier     = "db-perf-optimized-N-2"
  # disk_size         = 512        # SSD storage to 512 GB

  root_password          = random_password.db_root_password.result
  database_name          = "stax-db"
  database_user_name     = "user"
  database_user_password = random_password.db_user_password.result
  database_charset       = "utf8mb4"
  database_collation     = "utf8mb4_0900_ai_ci"

  enable_deletion_protection  = false
  enable_backup_configuration = true
  backup_start_time           = "20:55"

  mysql_database_flags = {
    "general_log"        = "on"
    "slow_query_log"     = "on"
    "log_output"         = "FILE"
    "skip_show_database" = "on"
  }
}