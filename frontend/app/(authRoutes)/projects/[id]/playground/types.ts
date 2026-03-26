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

import { InferenceChatCompletionPromptRole } from "@/types";

export interface OutputMessageType {
  readonly text?: string;
  readonly chatTurnId?: string;
  readonly latency?: number;
  readonly tokens?: number;
  readonly outputName: string;
}

export interface InputsType {
  readonly id: string;
  readonly role?: InferenceChatCompletionPromptRole;
  readonly text?: string;
}
export interface InputsPlaygroundType {
  readonly id?: string;
  role: InferenceChatCompletionPromptRole;
  text?: string;
  readonly promptName: PROMPTS;
  hidden?: boolean;
  attachment?: File | null;
}

export enum PROMPTS {
  PROMPT_A = "A",
  PROMPT_B = "B",
  COMMON = "COMMON",
}

export interface InputMessageBoxProps {
  readonly id: string;
  readonly role: InferenceChatCompletionPromptRole;
  readonly text?: string;
  readonly isSystemInstruction?: boolean;
  readonly onInstructionsChange?: (text: string) => void;
  readonly filteredInputs?: InputsPlaygroundType[];
  readonly onDeleteInput?: (id: string) => void;
  readonly attachment?: File | null;
  isExpandCard?: boolean;
  isClosing?: boolean;
  onCollapse?: (() => void) | null;
}

export interface InputMessageBoxAttachmentProps {
  readonly id: string;
  readonly role: InferenceChatCompletionPromptRole;
  readonly attachment?: File | null;
}

export interface OutputCardItemProps {
  text: string;
  readonly tokens: number;
  readonly latency: number;
  readonly isLoading?: boolean;
  readonly promptName?: string;
  error?: string;
  readonly hasExpandButton?: boolean;
}

export interface InputCardItemProps {
  readonly id: string;
  readonly text: string;
  readonly role: InferenceChatCompletionPromptRole;
}

export interface OutputCardProps {
  readonly setExpandedOutputName?: React.Dispatch<
    React.SetStateAction<string | undefined>
  >;
}

export interface PromptCleaningModalProps {
  readonly isOpened: boolean;
  readonly onClose: () => void;
}
