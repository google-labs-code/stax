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

import ExportEvaluatorsButton from "@/components/Evaluation/ExportEvaluatorsButton";
import { testRenderLite } from "@/tests-unit/render";
import {
  EvaluatorTab,
  EvaluatorType,
  InferenceChatCompletionPromptRole,
  LLMEvaluatorItem,
  ProjectType,
  Provider,
} from "@/types";
import * as csvExportUtils from "@/utils/jsonToCsvExport";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/utils/jsonToCsvExport", () => ({
  jsonToCsvExport: jest.fn(),
}));

describe("ExportEvaluatorsButton", () => {
  const systemEvaluators: LLMEvaluatorItem[] = [
    {
      id: "1",
      name: "System Eval 1",
      description: "Description 1",
      type: EvaluatorType.SYSTEM,
      evaluation_type: ProjectType.POINTWISE,
      prompts: [
        {
          id: "123",
          text: "abc",
          role: InferenceChatCompletionPromptRole.ASSISTANT,
          created_at: "",
          updated_at: "",
        },
      ],
      model: {
        properties: {
          max_tokens: undefined,
          n: undefined,
          temperature: undefined,
          top_p: undefined,
          top_k: undefined,
          seed: undefined,
        },
        id: "",
        name: "",
        version: "",
        label: "",
        url: "",
        tag: "",
        provider: Provider.OPENAI,
        model_type: "",
        additional_headers: {},
      },
      output_categories: [],
      output_format_type: "",
      variables: [],
      created_at: "",
      updated_at: "",
    },
    {
      id: "2",
      name: "System Eval 2",
      description: "Description 2",
      type: EvaluatorType.SYSTEM,
      evaluation_type: ProjectType.POINTWISE,
      prompts: [
        {
          id: "123",
          text: "abc",
          role: InferenceChatCompletionPromptRole.ASSISTANT,
          created_at: "",
          updated_at: "",
        },
      ],
      model: {
        properties: {
          max_tokens: undefined,
          n: undefined,
          temperature: undefined,
          top_p: undefined,
          top_k: undefined,
          seed: undefined,
        },
        id: "",
        name: "",
        version: "",
        label: "",
        url: "",
        tag: "",
        provider: Provider.OPENAI,
        model_type: "",
        additional_headers: {},
      },
      output_categories: [],
      output_format_type: "",
      variables: [],
      created_at: "",
      updated_at: "",
    },
  ];

  const userEvaluators: LLMEvaluatorItem[] = [
    {
      id: "3",
      name: "User Eval 1",
      description: "User Description 1",
      type: EvaluatorType.USER,
      evaluation_type: ProjectType.POINTWISE,
      prompts: [
        {
          id: "123",
          text: "abc",
          role: InferenceChatCompletionPromptRole.ASSISTANT,
          created_at: "",
          updated_at: "",
        },
      ],
      model: {
        properties: {
          max_tokens: undefined,
          n: undefined,
          temperature: undefined,
          top_p: undefined,
          top_k: undefined,
          seed: undefined,
        },
        id: "",
        name: "",
        version: "",
        label: "",
        url: "",
        tag: "",
        provider: Provider.OPENAI,
        model_type: "",
        additional_headers: {},
      },
      output_categories: [],
      output_format_type: "",
      variables: [],
      created_at: "",
      updated_at: "",
    },
  ];

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("renders enabled button for system tab when data exists", () => {
    testRenderLite(
      <ExportEvaluatorsButton
        data={systemEvaluators}
        activeTab={EvaluatorTab.DEFAULT}
      />,
    );

    const button = screen.getByRole("button", { name: /export evaluators/i });
    expect(button).toBeEnabled();
  });

  it("renders disabled button for user tab when no matching data exists", () => {
    testRenderLite(
      <ExportEvaluatorsButton
        data={systemEvaluators}
        activeTab={EvaluatorTab.MY_EVALUATORS}
      />,
    );

    const button = screen.getByRole("button", { name: /export evaluators/i });
    expect(button).toBeDisabled();
  });

  it("calls jsonToCsvExport with correct data and filename when clicked", () => {
    const mockExport = jest.spyOn(csvExportUtils, "jsonToCsvExport");

    testRenderLite(
      <ExportEvaluatorsButton
        data={userEvaluators}
        activeTab={EvaluatorTab.MY_EVALUATORS}
      />,
    );

    const button = screen.getByRole("button", { name: /export evaluators/i });
    fireEvent.click(button);

    expect(mockExport).toHaveBeenCalledTimes(1);

    const expectedData = [
      {
        id: "3",
        name: "User Eval 1",
        description: "User Description 1",
        type: EvaluatorType.USER,
        prompt: [
          {
            created_at: "",
            id: "123",
            role: "ASSISTANT",
            text: "abc",
            updated_at: "",
          },
        ],
      },
    ];

    const filenameRegex = /^custom-evaluators-\d{4}-\d{2}-\d{2}\.csv$/;

    expect(mockExport).toHaveBeenCalledWith(
      expectedData,
      expect.stringMatching(filenameRegex),
    );
  });
});
