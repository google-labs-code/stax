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

import AnthropicsAiIcon from "./icons/AnthropicsAiIcon";
import DeepseekAiIcon from "./icons/DeepseekAiIcon";
import GoogleIcon from "./icons/GoogleIcon";
import GrokAiIcon from "./icons/GrokAiIcon";
import HugginFaceAiIcon from "./icons/HugginFaceAiIcon";
import MetaIcon from "./icons/MetaIcon";
import MistralAiIcon from "./icons/MistralAiIcon";
import OllamaAiIcon from "./icons/OllamaAiIcon";
import OpenAiIconNew from "./icons/OpenAiIconNew";

export const ModelIcon = ({ provider }: { provider: Provider }): any => {
  switch (provider) {
    case Provider.GOOGLE:
      return <GoogleIcon />;
    case Provider.OPENAI:
      return <OpenAiIconNew />;
    case Provider.ANTHROPIC:
      return <AnthropicsAiIcon />;
    case Provider.MISTRAL:
      return <MistralAiIcon />;
    case Provider.GROK:
      return <GrokAiIcon />;
    case Provider.DEEPSEEK:
      return <DeepseekAiIcon />;
    case Provider.LLAMA:
      return <MetaIcon />;
    case Provider.OLLAMA:
      return <OllamaAiIcon />;
    case Provider.HUGGINGFACE:
      return <HugginFaceAiIcon />;
  }
};
