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

import InputMessageBox from "@/app/(authRoutes)/projects/[id]/playground/components/Input/InputMessageBox";
import { PROMPTS } from "@/app/(authRoutes)/projects/[id]/playground/types";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { testRender } from "@/tests-unit/render";
import { InferenceChatCompletionPromptRole } from "@/types";
import { handleCopy } from "@/utils/helpers";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/utils/helpers", () => ({
  handleCopy: jest.fn(),
}));

describe("InputMessageBox Component", () => {
  const mockedPlaygroundContext = {
    variables: { key1: "Value1", key2: "Value2" },
    expandedInputCard: null,
    setExpandedInputCard: jest.fn(),
    setInputs: jest.fn(),
  };

  const mockedProjectContext = {
    projectState: {
      project: {
        type: "POINTWISE",
      },
    },
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (usePlaygroundContext as jest.Mock).mockReturnValue(
      mockedPlaygroundContext,
    );
    (useProjectContext as jest.Mock).mockReturnValue(mockedProjectContext);
  });

  it("renders with default props and checks MaterialIcon elements", () => {
    testRender(
      <InputMessageBox
        id="input-1"
        role={InferenceChatCompletionPromptRole.USER}
        text="Hello, world!"
        isSystemInstruction={false}
        onInstructionsChange={jest.fn()}
        filteredInputs={[
          {
            id: "input-1",
            role: InferenceChatCompletionPromptRole.USER,
            text: "Hello, world!",
            promptName: PROMPTS.PROMPT_A,
          },
        ]}
      />,
    );

    expect(screen.getByTestId("copy-button")).toBeInTheDocument();
    expect(screen.getByTestId("expand-button")).toBeInTheDocument();
    expect(screen.getByTestId("dropdown-trigger")).toBeInTheDocument();
  });

  it("handles clicking the copy button to trigger handleCopy", () => {
    testRender(
      <InputMessageBox
        id="input-1"
        role={InferenceChatCompletionPromptRole.USER}
        text="Copy this text"
        isSystemInstruction={false}
        onInstructionsChange={jest.fn()}
        filteredInputs={[]}
      />,
    );

    const copyButton = screen.getByTestId("copy-button");
    fireEvent.click(copyButton);

    expect(handleCopy).toHaveBeenCalledWith("Copy this text");
  });
});
