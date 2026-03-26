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

import OutputCardItemHeader from "@/app/(authRoutes)/projects/[id]/playground/components/Output/OutputCardItemHeader";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/components/MaterialIcon", () => {
  const MaterialIcon = ({ name, tooltipLabel, className, onClick }: any) => (
    <span
      data-testid={`material-icon-${name}`}
      className={className}
      onClick={onClick}
    >
      {tooltipLabel || name}
    </span>
  );

  return MaterialIcon;
});

jest.mock("@/utils/helpers", () => ({
  convertMsToS: jest.fn((ms) => `${ms / 1000}s`),
  handleCopy: jest.fn(),
}));

describe("OutputCardItemHeader", () => {
  const mockHandleCopy = jest.requireMock("@/utils/helpers").handleCopy;

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

  const defaultProps = {
    text: "Sample Output",
    isLoading: false,
    promptName: "TestPrompt",
    latency: 2000,
    tokens: 300,
    hasExpandButton: true,
    onClose: jest.fn(),
  };

  it("renders all the key parts of the header", () => {
    testRender(<OutputCardItemHeader {...defaultProps} />);

    expect(screen.getByText(defaultProps.promptName)).toBeInTheDocument();

    expect(screen.getByText("Output")).toBeInTheDocument();

    expect(screen.getByText("COMPLETED")).toBeInTheDocument();
    expect(screen.getByText("2s")).toBeInTheDocument();
    expect(screen.getByText(`${defaultProps.tokens}`)).toBeInTheDocument();
    expect(screen.queryByText("IN PROGRESS")).not.toBeInTheDocument();
  });

  it("displays 'IN PROGRESS' state when loading", () => {
    testRender(<OutputCardItemHeader {...defaultProps} isLoading={true} />);

    expect(screen.getByText("IN PROGRESS")).toBeInTheDocument();
    expect(screen.queryByText("COMPLETED")).not.toBeInTheDocument();
  });

  it("disables interaction when text is empty or loading", () => {
    const { rerender } = testRender(
      <OutputCardItemHeader {...defaultProps} text="" />,
    );

    const copyIcon = screen.getByTestId("material-icon-content_copy");
    fireEvent.click(copyIcon);
    expect(mockHandleCopy).not.toHaveBeenCalled();

    rerender(<OutputCardItemHeader {...defaultProps} isLoading={true} />);
    fireEvent.click(copyIcon);
    expect(mockHandleCopy).not.toHaveBeenCalled();
  });

  it("calls handleCopy when copy icon is clicked", () => {
    testRender(<OutputCardItemHeader {...defaultProps} />);

    const copyIcon = screen.getByTestId("material-icon-content_copy");
    fireEvent.click(copyIcon);

    expect(mockHandleCopy).toHaveBeenCalledWith(defaultProps.text);
  });

  it("renders appropriate styles when disabled", () => {
    const { rerender } = testRender(
      <OutputCardItemHeader {...defaultProps} isLoading={true} />,
    );

    const copyIcon = screen.getByTestId("material-icon-content_copy");

    expect(copyIcon).toHaveClass("!cursor-default text-disabled");

    rerender(<OutputCardItemHeader {...defaultProps} text="" />);
    expect(copyIcon).toHaveClass("!cursor-default text-disabled");
  });
});
