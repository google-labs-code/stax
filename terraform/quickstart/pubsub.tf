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

locals {
  pubsub_configs = {
    "open-ai-inference"          = "open-ai-inference-sub"
    "mistral-inference"          = "mistral-inference-sub"
    "anthropic-inference"        = "anthropic-inference-sub"
    "gemini-inference"           = "gemini-inference-sub"
    "grok-inference"             = "grok-inference-sub"
    "ollama-inference"           = "ollama-inference-sub"
    "deepseek-inference"         = "deepseek-inference-sub"
    "huggingface-inference"      = "huggingface-inference-sub"
    "open-ai-eval"               = "open-ai-eval-sub"
    "mistral-eval"               = "mistral-eval-sub"
    "anthropic-eval"             = "anthropic-eval-sub"
    "gemini-eval"                = "gemini-eval-sub"
    "deepseek-eval"              = "deepseek-eval-sub"
    "grok-eval"                  = "grok-eval-sub"
    "heuristic-eval"             = "heuristic-eval-sub"
    "open-ai-inference-bulk"     = "open-ai-inference-bulk-sub"
    "mistral-inference-bulk"     = "mistral-inference-bulk-sub"
    "anthropic-inference-bulk"   = "anthropic-inference-bulk-sub"
    "gemini-inference-bulk"      = "gemini-inference-bulk-sub"
    "grok-inference-bulk"        = "grok-inference-bulk-sub"
    "ollama-inference-bulk"      = "ollama-inference-bulk-sub"
    "deepseek-inference-bulk"    = "deepseek-inference-bulk-sub"
    "huggingface-inference-bulk" = "huggingface-inference-bulk-sub"
    "open-ai-eval-bulk"          = "open-ai-eval-bulk-sub"
    "mistral-eval-bulk"          = "mistral-eval-bulk-sub"
    "anthropic-eval-bulk"        = "anthropic-eval-bulk-sub"
    "gemini-eval-bulk"           = "gemini-eval-bulk-sub"
    "grok-eval-bulk"             = "grok-eval-bulk-sub"
    "ollama-eval-bulk"           = "ollama-eval-bulk-sub"
    "deepseek-eval-bulk"         = "deepseek-eval-bulk-sub"
    "huggingface-eval-bulk"      = "huggingface-eval-bulk-sub"
    "user-deletion"              = "user-deletion-sub"
    "gcs-feature-gate"           = "gcs-feature-gate-sub"
  }
}

module "pubsub" {
  source     = "../modules/gcp_pubsub"
  project_id = local.project_id
  for_each   = local.pubsub_configs

  topic_name        = each.key
  subscription_name = each.value
}