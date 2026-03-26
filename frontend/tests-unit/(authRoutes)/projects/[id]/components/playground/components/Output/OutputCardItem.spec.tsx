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

import OutputCardItem from "@/app/(authRoutes)/projects/[id]/playground/components/Output/OutputCardItem";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

jest.mock("@/utils/GetChatMessageWithMarkup", () => {
  return jest.fn(({ message }) => (
    <span data-testid="chat-message">{message}</span>
  ));
});

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

describe("OutputCardItem", () => {
  const defaultProps = {
    text: "Sample Output",
    tokens: 300,
    latency: 2000,
    isLoading: false,
    promptName: "TestPrompt",
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (usePlaygroundContext as jest.Mock).mockReturnValue({
      setIsOutputExpanded: jest.fn(),
      isOutputExpanded: false,
    });
    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: false,
    });
  });

  it("renders OutputCardItemHeader with correct props", () => {
    testRender(<OutputCardItem {...defaultProps} />);

    expect(screen.getByText(defaultProps.promptName)).toBeInTheDocument();

    expect(screen.getByText("Output")).toBeInTheDocument();
  });

  it("renders GetChatMessageWithMarkup when `text` is provided", () => {
    testRender(<OutputCardItem {...defaultProps} />);

    expect(screen.getByTestId("chat-message")).toHaveTextContent(
      defaultProps.text,
    );

    expect(
      screen.queryByText("Model output will appear here."),
    ).not.toBeInTheDocument();
  });

  it("renders fallback message when text is empty", () => {
    testRender(<OutputCardItem {...defaultProps} text="" />);

    expect(
      screen.getByText("Model output will appear here."),
    ).toBeInTheDocument();

    expect(screen.queryByTestId("chat-message")).not.toBeInTheDocument();
  });

  it("handles undefined promptName correctly", () => {
    testRender(<OutputCardItem {...defaultProps} promptName={undefined} />);

    expect(screen.getByText("Output")).toBeInTheDocument();
  });

  it("renders properly while loading", () => {
    testRender(<OutputCardItem {...defaultProps} isLoading={true} text="" />);

    expect(
      screen.getByText("Model output will appear here."),
    ).toBeInTheDocument();

    expect(screen.getByText("Output")).toBeInTheDocument();
  });
});
