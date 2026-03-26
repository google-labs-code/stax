/**
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export const backendEndpoints = {
  AUTH: {
    DELETE_USER_DATA: "user/delete-user-data",
    GOOGLE_AUTH_CODE: "auth/google/auth-code",
    TOS_ACCEPT: "tos/accept",
    VALIDATE_TOKEN: "auth/validate-token",
  },
  API_KEYS: {
    USER: {
      SET_KEY: "api-keys/set-key",
      DELETE_KEY: "api-keys/delete-key",
      GET_KEYS: "api-keys/get-keys-by-user",
    },
  },
  FEEDBACK: "feedback",
  TAGS: "tags",
  INFERENCE: {
    QUICK_COMPARE_CHAT_COMPLETION: "chat",
    CHAT_COMPLETION_STREAMING: "chat/stream",
  },
  MODELS: {
    ROOT: "model",
    INDEX: "model/list",
    DUPLICATE: "duplicate_and_modify",
    DEPRECATE: "model/deprecate",
    CUSTOM_MODEL: "model/custom-endpoint",
    MODEL_PROVIDERS: "model/providers",
  },
  PROJECTS: {
    INDEX: "projects",
    EVAL_ANALYTICS: "eval-analytics",
  },
  CONTAINERS: "containers",
  CHAT: {
    INDEX: "chat",
    CHAT_TURN: "chat-turn",
    VARIABLES: "variables",
  },
  EVALUATIONS: {
    CHAT_TURN: "chat-turn",
    CHAT_TURNS: "chat-turns",
    PROJECT: "evaluations/projects",
  },
  LLM_EVALUATOR: {
    ROOT: "llm_evaluator",
    CUSTOM: "llm_evaluator/custom",
    SYSTEM: "llm_evaluator/system",
  },
  LLM_PAIRWISE_EVALUATOR: "pairwise-llm-evaluators",
  ANALYTICS: {
    INFERENCE: "analytics/inference-monitoring",
  },
  DATASETS: {
    ROOT: "datasets",
    UPLOAD_CSV: "datasets/upload/csv",
    UPLOAD_CSV_TO_EXISTING_DATASET: "datasets/upload/csv-to-existing",
  },
  DATA_TRANSFER: {
    ROOT: "data-transfer",
    ALL: "data-transfer/all",
  },
  WORKBOOKS: "workbooks",
  TAG_LINKS: {
    ROOT: "tag-links",
    REMOVE: "tag-links/remove",
  },
  HUMAN_EVALUATORS: {
    ROOT: "human-evaluators",
    FEEDBACK: "human-evaluators/feedback",
    PASS_RATE: "human-eval-pass-rate",
  },
  USER_EVALUATION_MONITORING: {
    ANALYTICS: "analytics/user-eval-monitoring",
  },
  METRICS: {
    ROOT: "metrics-summary",
  },
  EVALUATOR: {
    ALL: "evaluator/all",
  },
};

export const nextEndpoints = {
  AUTH: {
    SIGNIN: "login",
  },
};
