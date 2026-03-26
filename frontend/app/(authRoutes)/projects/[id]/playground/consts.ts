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

import { HUMAN_SXS_RATING } from "@/queries/types";
import { InferenceChatCompletionPromptRole } from "@/types";

export const CHAT_THUMBS = [
  {
    value: 1,
    icon: "thumb_up",
    name: "PASS",
    activeStyle: "!text-lime !bg-limeBg !border-lime",
    tooltipLabel: "Good response",
  },
  {
    value: -1,
    icon: "thumb_down",
    name: "FAIL",
    activeStyle: "!text-pink !bg-pinkBg !border-pink",
    tooltipLabel: "Bad response",
  },
];

export const SIDE_BY_SIDE_EVAL_OPTIONS = [
  {
    value: HUMAN_SXS_RATING.A_IS_BETTER,
    icon: "arrow_back",
    name: "LEFT IS BETTER",
    activeStyle: "!text-lime !bg-limeBg !border-lime",
  },
  {
    value: HUMAN_SXS_RATING.BOTH_ARE_GOOD,
    icon: "handshake",
    name: "IT'S A TIE",
    activeStyle: "!text-brand !bg-lightBlue !border-brand",
  },
  {
    value: HUMAN_SXS_RATING.BOTH_ARE_BAD,
    icon: "do_not_disturb_on",
    name: "BOTH ARE BAD",
    activeStyle: "!text-pink !bg-pinkBg !border-pink",
  },
  {
    value: HUMAN_SXS_RATING.B_IS_BETTER,
    icon: "arrow_forward",
    name: "RIGHT IS BETTER",
    activeStyle: "!text-lime !bg-limeBg !border-lime",
  },
];

export const ROLE_OPTIONS_MAP = {
  [InferenceChatCompletionPromptRole.SYSTEM]: {
    value: "System",
    label: "System Instructions",
    placehodler: "Enter optional system instructions",
  },
  [InferenceChatCompletionPromptRole.USER]: {
    value: "User",
    label: "User Input",
    placehodler: "Enter user input",
  },
  [InferenceChatCompletionPromptRole.ASSISTANT]: {
    value: "Assistant",
    label: "Assistant",
    placehodler: "Enter optional assistant instructions",
  },
};

export const PLAYGROUND_ONBOARDING = [
  {
    title: "Select an AI model",
    body: "Start by selecting or searching for the model you'd like to test.",
  },
  {
    title: "Enter user input",
    body: "Enter the user prompt you want the AI to respond to. You can also add system instructions to guide its behavior or more messages to simulate a conversation.",
  },
  {
    title: "Generate AI model output",
    body: "Once your prompt is ready, generate output to see the AI's response.",
  },
  {
    title: "Rate output",
    body: "Finally, rate the AI's output to quantify quality and see how well evaluators align with your judgment.",
  },
  {
    title: "Return to project",
    body: "Once you have completed adding your data, simply close the playground to return to your project.",
  },
];

export const PROJECT_ONBOARDING = [
  {
    title: "Add data",
    body: "Start by populating your project. You can add data manually using the playground or upload an entire dataset at once.",
  },
  {
    title: "Generate AI model outputs",
    body: "Once your data is added, generate outputs to see  AI responses on selected inputs for evaluation.",
  },
  {
    title: "Evaluate how your AI performs",
    body: "Choose evaluators to rate AI output to determine if it meets your quality standards on key criteria.",
  },
];
export const SHOW_PROJECT_ONBOARDING = "projectOnboarding";
export const SHOW_PLAYGROUND_ONBOARDING = "playgroundOnboarding";
export const SHOW_PLAYGROUND_INFO_BAR = "playgroundInfoBar";
