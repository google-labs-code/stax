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

################################## db_password #################################
resource "random_password" "db_user_password" {
  length      = 16
  min_lower   = 1
  min_numeric = 1
  min_upper   = 1
  min_special = 1
  # removes % and & which are special characters in uri
  override_special = "!@#$*()-_=+[]{}<>:?"
}
################################ root_password ####################################
resource "random_password" "db_root_password" {
  length      = 16
  min_lower   = 1
  min_numeric = 1
  min_upper   = 1
  min_special = 1
}
######################### Secret for AES_SECRET_KEY ##############################
resource "random_password" "aes_secret_key" {
  length      = 16
  special     = false
  min_lower   = 1
  min_numeric = 1
  min_upper   = 1
}
resource "google_secret_manager_secret" "aes_secret_key" {
  project   = local.project_id
  secret_id = "aes-secret-key"
  replication {
    auto {}
  }
}
# Attaches secret data for aes_secret_key secret
resource "google_secret_manager_secret_version" "aes_secret_key" {
  secret      = google_secret_manager_secret.aes_secret_key.id
  secret_data = random_password.aes_secret_key.result

  lifecycle {
    ignore_changes = all
  }
}
# Update service account for aes_secret_key secret
resource "google_secret_manager_secret_iam_member" "secretaccess_aes_secret_key" {
  secret_id = google_secret_manager_secret.aes_secret_key.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${local.service_account}"
}
######################### Secret for TOKEN_SIGNING_KEY ##############################
resource "random_password" "token_signing_key" {
  length      = 92
  special     = false
  min_lower   = 1
  min_numeric = 1
  min_upper   = 1
}
resource "google_secret_manager_secret" "token_signing_key" {
  project   = local.project_id
  secret_id = "token-signing-key"
  replication {
    auto {}
  }
}
# Attaches secret data for token_signing_key secret
resource "google_secret_manager_secret_version" "token_signing_key" {
  secret      = google_secret_manager_secret.token_signing_key.id
  secret_data = random_password.token_signing_key.result

  lifecycle {
    ignore_changes = all
  }
}
# Update service account for token_signing_key secret
resource "google_secret_manager_secret_iam_member" "secretaccess_token_signing_key" {
  secret_id = google_secret_manager_secret.token_signing_key.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${local.service_account}"
}
######################### Secret for TOS_TOKEN_SIGNING_KEY ##############################
resource "random_password" "tos_token_signing_key" {
  length      = 92
  special     = false
  min_lower   = 1
  min_numeric = 1
  min_upper   = 1
}
resource "google_secret_manager_secret" "tos_token_signing_key" {
  project   = local.project_id
  secret_id = "tos-token-signing-key"
  replication {
    auto {}
  }
}
# Attaches secret data for tos_token_signing_key secret
resource "google_secret_manager_secret_version" "tos_token_signing_key" {
  secret      = google_secret_manager_secret.tos_token_signing_key.id
  secret_data = random_password.tos_token_signing_key.result

  lifecycle {
    ignore_changes = all
  }
}
# Update service account for tos_token_signing_key secret
resource "google_secret_manager_secret_iam_member" "secretaccess_tos_token_signing_key" {
  secret_id = google_secret_manager_secret.tos_token_signing_key.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${local.service_account}"
}
###################### google_oauth_client_secret ################################
# This is needed for using Google OAuth authentication.
# TODO: as a prerequisite, configure a Google oauth client, and set the client secret in Secret Manager
# Uncomment the following lines when you have configured a Google oauth client
# resource "google_secret_manager_secret" "google_oauth_client_secret" {
#   project   = local.project_id
#   secret_id = "google_oauth_client_secret"
#   replication {
#     auto {}
#   }
# }
# # Update service account for google_oauth_client_secret secret
# resource "google_secret_manager_secret_iam_member" "secretaccess_google_oauth_client_secret" {
#   secret_id = google_secret_manager_secret.google_oauth_client_secret.id
#   role      = "roles/secretmanager.secretAccessor"
#   member    = "serviceAccount:${local.service_account}"
# }