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
# External MySQL (Aiven free tier). Cloud SQL has no free tier.
# Password lives in Secret Manager secret "db-password" (set out of band, see secrets.tf).
variable "db_host" { type = string }
variable "db_port" {
  type    = number
  default = 3306
}
variable "db_name" { type = string }
variable "db_user" { type = string }
