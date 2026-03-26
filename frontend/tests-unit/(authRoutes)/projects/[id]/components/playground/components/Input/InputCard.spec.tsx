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

import InputCard from "@/app/(authRoutes)/projects/[id]/playground/components/Input/InputCard";
import { PROMPTS } from "@/app/(authRoutes)/projects/[id]/playground/types";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { useModelsContext } from "@/hooks/useModelsContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { Model } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { InferenceChatCompletionPromptRole, Provider } from "@/types";
import { screen } from "@testing-library/react";

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: () => ({
    updateChat: jest.fn(),
  }),
}));

const mockSystemInstruction = {
  id: "sys-1",
  text: "Mock System Instruction",
  role: InferenceChatCompletionPromptRole.SYSTEM,
  promptName: PROMPTS.PROMPT_A,
};

const mockModel: Model = {
  id: "model-1",
  label: "Test Model",
  name: "Mock Model",
  version: "1.0.0",
  url: "https://example.com",
  tag: "mock-tag",
  model_type: "text-generation",
  provider: Provider.OPENAI,
  properties: {},
  additional_headers: {},
  is_api_key_present: true,
  is_deprecated: false,
};

const mockFilteredInputs = [
  {
    id: "input-1",
    text: "User Input 1",
    role: InferenceChatCompletionPromptRole.USER,
    promptName: PROMPTS.PROMPT_A,
  },
  {
    id: "input-2",
    text: "Assistant Input",
    role: InferenceChatCompletionPromptRole.ASSISTANT,
    promptName: PROMPTS.PROMPT_A,
  },
];

const mockOnDeleteInput = jest.fn();
const mockOnSystemInstructionChange = jest.fn();
const mockSetModel = jest.fn();

const defaultPlaygroundContext = {
  inputs: mockFilteredInputs,
  setExpandedInputCard: jest.fn(),
  filteredOptions: [mockModel],
  isSaving: false,
  isSaved: true,
  isHumanEvalReady: false,
  hasShownCleaningBar: true,
  onboardingIndex: null,
  setOnboardingIndex: jest.fn(),
};

const defaultProjectContext = {
  projectState: {
    project: { type: "SIDE_BY_SIDE" },
  },
};

const defaultModelsContext = {
  allModels: [
    {
      deprecated: false,
      apiKeyPresent: false,
      id: "model_c4c8bdca-7424-11f0-aa6e-3eece6bcfab1",
      name: "gpt-5",
      provider: "OPENAI",
      label: "GPT-5",
      nickname: "GPT-5",
      url: "https://api.openai.com/v1/chat/completions",
      tag: null,
      description:
        "GPT-5 is OpenAI's most intelligent model yet, trained to be especially proficient in code generation, bug fixing, refactoring, instruction following, long context and tool calling. Best for complex reasoning, broad world knowledge, and code-heavy or multi-step agentic tasks.",
      comments: "",
      properties: {
        seed: null,
        top_p: 1,
        temperature: 1,
        stop_sequences: [],
        max_output_tokens: 100000,
      },
      pricing: null,
      is_api_key_present: false,
      is_deprecated: false,
      model_type: "SYSTEM",
      release_date: "2025-08-07T00:00:00.000+00:00",
      is_custom_endpoint: false,
      api_key: null,
      additional_headers: null,
    },
    {
      deprecated: false,
      apiKeyPresent: false,
      id: "model_c4c52624-7424-11f0-aa6e-3eece6bcfab1",
      name: "gpt-5-mini",
      provider: "OPENAI",
      label: "GPT-5 Mini",
      nickname: "GPT-5 Mini",
      url: "https://api.openai.com/v1/chat/completions",
      tag: null,
      description:
        "Cost-optimized reasoning and chat model that balances speed, cost, and capability. Ideal for cost-optimized reasoning and chat applications.",
      comments: "",
      properties: {
        seed: null,
        top_p: 1,
        temperature: 1,
        stop_sequences: [],
        max_output_tokens: 100000,
      },
      pricing: null,
      is_api_key_present: false,
      is_deprecated: false,
      model_type: "SYSTEM",
      release_date: "2025-08-07T00:00:00.000+00:00",
      is_custom_endpoint: false,
      api_key: null,
      additional_headers: null,
    },
  ],
  selectedModel: mockModel,
  setSelectedModel: mockSetModel,
};

const setup = ({
  playgroundOverrides = {},
  projectOverrides = {},
}: {
  playgroundOverrides?: Partial<typeof defaultPlaygroundContext>;
  projectOverrides?: Partial<typeof defaultProjectContext>;
} = {}) => {
  (usePlaygroundContext as jest.Mock).mockReturnValue({
    ...defaultPlaygroundContext,
    ...playgroundOverrides,
  });

  (useProjectContext as jest.Mock).mockReturnValue({
    ...defaultProjectContext,
    ...projectOverrides,
  });

  (useModelsContext as jest.Mock).mockReturnValue(defaultModelsContext);

  return testRender(
    <InputCard
      promptName={PROMPTS.PROMPT_A}
      onDeleteInput={mockOnDeleteInput}
      systemInstruction={mockSystemInstruction}
      onSystemInstructionChange={mockOnSystemInstructionChange}
      model={mockModel}
      setModel={mockSetModel}
    />,
  );
};

describe("InputCard", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders each user and assistant input", () => {
    setup();

    expect(screen.getByText("Prompt")).toBeInTheDocument();
  });

  it("does NOT render cleaning bar if already shown", () => {
    setup({
      playgroundOverrides: {
        isHumanEvalReady: true,
        hasShownCleaningBar: true,
      },
    });

    expect(
      screen.queryByTestId("mock-prompt-cleaning-info-bar"),
    ).not.toBeInTheDocument();
  });

  it("does NOT render cleaning bar if not human eval ready", () => {
    setup({
      playgroundOverrides: {
        isHumanEvalReady: false,
        hasShownCleaningBar: false,
      },
    });

    expect(
      screen.queryByTestId("mock-prompt-cleaning-info-bar"),
    ).not.toBeInTheDocument();
  });
});
