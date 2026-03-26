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

import { Provider } from "@/types";

import { KeyDetails } from "../types";

export const InitialKeys: KeyDetails[] = [
  {
    label: "Google Gemini",
    provider: Provider.GOOGLE,
    apiKey: "googleKeyPresent",
    isKeyPresent: false,
    link: "https://aistudio.google.com/app/apikey",
  },
  {
    label: "OpenAI",
    provider: Provider.OPENAI,
    apiKey: "openaiKeyPresent",
    isKeyPresent: false,
    link: "https://platform.openai.com/api-keys",
  },
  {
    label: "Claude",
    provider: Provider.ANTHROPIC,
    apiKey: "anthropicKeyPresent",
    isKeyPresent: false,
    link: "https://console.anthropic.com/settings/keys",
  },
  {
    label: "Mistral",
    provider: Provider.MISTRAL,
    apiKey: "mistralKeyPresent",
    isKeyPresent: false,
    link: "https://console.mistral.ai/api-keys",
  },
  {
    label: "Grok",
    provider: Provider.GROK,
    apiKey: "grokKeyPresent",
    isKeyPresent: false,
    link: "https://console.x.ai/team/default/api-keys",
  },
  {
    label: "Deepseek",
    provider: Provider.DEEPSEEK,
    apiKey: "deepseekKeyPresent",
    isKeyPresent: false,
    link: "https://platform.deepseek.com/api_keys",
  },
  {
    label: "Llama",
    provider: Provider.LLAMA,
    apiKey: "llamaKeyPresent",
    isKeyPresent: false,
    link: "https://llama.developer.meta.com/docs/api-keys",
  },
];
