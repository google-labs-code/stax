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

module "cloud_run_backend" {
  source     = "../modules/gcp_run"
  project_id = local.project_id
  region     = local.region

  cloud_run_image             = "${local.region}-docker.pkg.dev/${local.project_id}/stax-backend"
  cloud_run_service_name      = "stax-backend"
  cloud_run_cpu_limit         = 4
  cloud_run_memory_limit      = "16Gi"
  cloud_run_startup_cpu_boost = true

  allow_unauthenticated = true

  cloud_run_min_instances            = 1
  cloud_run_max_instances            = 20
  cloud_run_max_instance_concurrency = 80

  cloud_run_env_vars = [
    {
      name  = "PROFILE_ACTIVE",
      value = "prod"
    },
    {
      name  = "AUTH_ALLOWLIST_GROUP_IDS",
      value = "AllUsers"
    },
    {
      name  = "AUTH_ALLOWLIST_DOMAINS",
      value = "" # Empty string means no domain restriction.
    },
    {
      name  = "TOS_TYPE",
      value = "tt"
    },
    {
      name  = "SYSTEM_USER_FIRST_NAME",
      value = "Stax"
    },
    {
      name  = "SYSTEM_USER_LAST_NAME",
      value = "System User"
    },
    {
      name  = "SYSTEM_USER_EMAIL",
      value = "stax-java-server@google.com"
    },
    {
      name  = "GCP_PROJECT_ID",
      value = local.project_id
    },
    {
      name  = "GCP_BUCKET_ID",
      value = "stax-prod-project-bucket"
    },
    # This is needed for using Google OAuth authentication. As a prerequisite, configure a Google oauth client.
    # TODO: Uncomment the following lines and replace with the real oauth client id here
    # {
    #   name  = "GOOGLE_CLIENT_ID",
    #   value = "12344556-abcde12345abcde12345.apps.googleusercontent.com"
    # },
    # TODO: Uncomment the following lines when you have configured a Google oauth client
    # {
    #   name = "GOOGLE_CLIENT_SECRET",
    #   value_from_secret = {
    #     secret  = google_secret_manager_secret.google_oauth_client_secret.secret_id
    #     version = "latest"
    #   }
    # },
    {
      name = "AES_SECRET_KEY",
      value_from_secret = {
        secret  = google_secret_manager_secret.aes_secret_key.id
        version = "latest"
      }
    },
    {
      name = "TOKEN_SIGNING_KEY",
      value_from_secret = {
        secret  = google_secret_manager_secret.token_signing_key.secret_id
        version = "latest"
      }
    },
    {
      name = "TOS_TOKEN_SIGNING_KEY",
      value_from_secret = {
        secret  = google_secret_manager_secret.tos_token_signing_key.secret_id
        version = "latest"
      }
    },
    {
      name  = "JDBC_DATABASE_USERNAME",
      value = module.cloudsql_mysql_instance.database_user_name
    },
    {
      name = "JDBC_DATABASE_PASSWORD",
      value_from_secret = {
        secret  = google_secret_manager_secret.db_password.secret_id
        version = "latest"
      }
    },
    {
      name = "JDBC_DATABASE_URL",
      # https://cloud.google.com/sql/docs/mysql/connect-run#java_2
      value = "jdbc:mysql://${module.cloudsql_mysql_instance.private_ip_address}:3306/${module.cloudsql_mysql_instance.database_name}"
    }
  ]

  ingress_type_cloud_run = "INGRESS_TRAFFIC_ALL"

  volumes = [
    {
      name = "cloudsql",
      cloud_sql_instance = {
        instances = [module.cloudsql_mysql_instance.connection_name]
      }
    }
  ]

  volume_mounts_map = {
    "cloudsql" = "/cloudsql"
  }

  depends_on = [ google_secret_manager_secret.aes_secret_key, google_secret_manager_secret.token_signing_key, google_secret_manager_secret.tos_token_signing_key ]
}