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

import Autorater from "@/app/(authRoutes)/autorater/page";
import { useGlobalContext } from "@/hooks/useGlobalContext";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/useGlobalContext", () => ({
  useGlobalContext: jest.fn(),
}));

jest.mock("@/components/BadgeWithIcon", () => ({
  __esModule: true,
  default: ({ title, className }: { title: string; className?: string }) => (
    <span data-testid={`badge-${title}`} className={className || ""}>
      {title}
    </span>
  ),
}));

jest.mock("@/components/CircularEvaluatorButton", () => ({
  CircularEvaluatorButton: ({
    isLoading,
    onClick,
    disabled,
  }: {
    isLoading: boolean;
    onClick: () => void;
    disabled: boolean;
  }) => (
    <button
      data-testid="circular-evaluator-button"
      disabled={disabled}
      onClick={onClick}
    >
      {isLoading ? "Loading..." : "Evaluate"}
    </button>
  ),
}));

jest.mock("@/components/MaterialIcon", () => ({
  __esModule: true,
  default: ({
    name,
    size,
    className,
  }: {
    name: string;
    size?: number;
    className?: string;
  }) => (
    <span
      data-testid={`icon-${name}`}
      className={className || ""}
      style={{ fontSize: size }}
    >
      {name}
    </span>
  ),
}));

jest.mock("@/components/Page", () => ({
  __esModule: true,
  default: ({
    children,
    className,
    fullHeight,
  }: {
    children: React.ReactNode;
    className?: string;
    fullHeight?: boolean;
  }) => (
    <div
      data-testid="page-component"
      className={className || ""}
      data-fullheight={fullHeight ? "true" : "false"}
    >
      {children}
    </div>
  ),
}));

const mockUser = {
  firstName: "Test",
  lastName: "User",
  email: "test@example.com",
};

describe("Autorater Page", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useGlobalContext as jest.Mock).mockReturnValue({
      userDetails: mockUser,
    });
  });

  it("renders with user's first name", () => {
    testRender(<Autorater />);
    expect(screen.getByText(/Hello Test welcome to/)).toBeInTheDocument();
  });

  it("renders badges with correct titles", () => {
    testRender(<Autorater />);
    expect(screen.getByTestId("badge-Stax")).toBeInTheDocument();
    expect(screen.getByTestId("badge-Autorater")).toBeInTheDocument();
    expect(screen.getByTestId("badge-Judge")).toBeInTheDocument();
    expect(screen.getByTestId("badge-Quickstart")).toBeInTheDocument();
    expect(screen.getByTestId("badge-Documentation")).toBeInTheDocument();
  });

  it("has a disabled button when input is empty", () => {
    testRender(<Autorater />);
    const button = screen.getByTestId("circular-evaluator-button");
    expect(button).toBeDisabled();
  });

  it("enables the button when input has content", () => {
    testRender(<Autorater />);
    const input = screen.getByPlaceholderText(
      "Rotating descriptions of evaluation goals go here..",
    );
    fireEvent.change(input, { target: { value: "Test input" } });
    const button = screen.getByTestId("circular-evaluator-button");
    expect(button).not.toBeDisabled();
  });

  it("handles input changes correctly", () => {
    testRender(<Autorater />);
    const input = screen.getByPlaceholderText(
      "Rotating descriptions of evaluation goals go here..",
    );
    fireEvent.change(input, { target: { value: "Test input value" } });
    expect(input).toHaveValue("Test input value");
  });

  it("renders attach button with file icon", () => {
    testRender(<Autorater />);
    const attachButton = screen.getByText("Attach");
    expect(attachButton).toBeInTheDocument();
    const fileIcon = screen.getByTestId("icon-attach_file");
    expect(fileIcon).toBeInTheDocument();
  });

  it("shows info section with links at the bottom", () => {
    testRender(<Autorater />);
    expect(screen.getByTestId("icon-info")).toBeInTheDocument();
    expect(
      screen.getByText(/For superior autorater performance, attach a dataset/),
    ).toBeInTheDocument();
    expect(screen.getByText("Dataset Structuring")).toBeInTheDocument();
    expect(screen.getByText("Sample Dataset")).toBeInTheDocument();
  });

  it("renders with full height Page component", () => {
    testRender(<Autorater />);
    const pageComponent = screen.getByTestId("page-component");
    expect(pageComponent).toHaveAttribute("data-fullheight", "true");
  });

  it("applies correct width classes based on navigation state", () => {
    testRender(<Autorater />);
    expect(screen.getByText(/Hello Test welcome to/).parentElement).toHaveClass(
      `mx-auto mt-16 max-w-[888px]`,
    );

    const bottomContainer = screen
      .getByText(/For superior autorater performance/)
      .closest("div");
    
    expect(bottomContainer?.parentElement).toHaveClass("mx-auto");
    expect(bottomContainer?.parentElement).toHaveClass("max-w-[888px]");
    const hasCorrectMargin = 
      bottomContainer?.parentElement?.classList.contains("mb-4") || 
      bottomContainer?.parentElement?.classList.contains("mb-8");
    expect(hasCorrectMargin).toBeTruthy();
  });

  it("renders with correct styling for input field", () => {
    testRender(<Autorater />);
    const input = screen.getByPlaceholderText(
      "Rotating descriptions of evaluation goals go here..",
    );
    const inputContainer = input.parentElement;
    expect(inputContainer).toHaveClass("border-0");
    expect(input).toHaveAttribute(
      "class",
      expect.stringContaining("placeholder:text-title-22"),
    );
    expect(input).toHaveAttribute(
      "class",
      expect.stringContaining("placeholder:text-resting"),
    );
  });
});