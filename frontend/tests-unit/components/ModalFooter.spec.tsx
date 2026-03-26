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

import ModalFooterButton from "@/components/ModalFooter";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

describe("ModalFooterButton", () => {
  const defaultProps = {
    onClick: jest.fn(),
    label: "Save",
  };

  const renderComponent = (props = {}) => {
    const componentProps = { ...defaultProps, ...props };

    return testRender(<ModalFooterButton {...componentProps} />);
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should render the component with required information text", () => {
    renderComponent();

    const requiredText = screen.getByText("Required information");
    const asterisk = screen.getByText("*");

    expect(requiredText).toBeInTheDocument();
    expect(asterisk).toBeInTheDocument();
    expect(requiredText).toHaveClass("text-body-11", "text-secondary");
    expect(asterisk).toHaveClass("text-red", "mb-[-2px]");
  });

  it("should render the button with the provided label", () => {
    renderComponent();

    const button = screen.getByRole("button");
    expect(button).toBeInTheDocument();
    expect(button).toHaveTextContent("Save");
  });

  it("should call onClick when button is clicked", () => {
    const mockOnClick = jest.fn();
    renderComponent({ onClick: mockOnClick });

    const button = screen.getByRole("button");
    fireEvent.click(button);

    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });

  it("should render button with correct default styling", () => {
    renderComponent();

    const button = screen.getByRole("button");
    expect(button).toHaveClass("mantine-Button-root");
  });

  it("should show loading state when isLoading is true", () => {
    renderComponent({ isLoading: true });

    const button = screen.getByRole("button");
    expect(button).toHaveAttribute("data-loading", "true");
  });

  it("should disable button when isDisabled is true", () => {
    renderComponent({ isDisabled: true });

    const button = screen.getByRole("button");
    expect(button).toBeDisabled();
  });

  it("should not call onClick when button is disabled", () => {
    const mockOnClick = jest.fn();
    renderComponent({ onClick: mockOnClick, isDisabled: true });

    const button = screen.getByRole("button");
    fireEvent.click(button);

    expect(mockOnClick).not.toHaveBeenCalled();
  });

  it("should not call onClick when button is loading", () => {
    const mockOnClick = jest.fn();
    renderComponent({ onClick: mockOnClick, isLoading: true });

    const button = screen.getByRole("button");
    fireEvent.click(button);

    expect(mockOnClick).not.toHaveBeenCalled();
  });

  it("should render with different label text", () => {
    renderComponent({ label: "Create Project" });

    const button = screen.getByRole("button");
    expect(button).toHaveTextContent("Create Project");
  });

  it("should render with empty label", () => {
    renderComponent({ label: "" });

    const button = screen.getByRole("button");
    expect(button).toHaveTextContent("");
  });

  it("should have correct layout structure", () => {
    renderComponent();

    const container = screen.getByRole("button").closest(".w-full");
    expect(container).toHaveClass("w-full", "flex", "justify-between");
  });

  it("should render required information group with correct styling", () => {
    renderComponent();

    const requiredText = screen.getByText("Required information");
    const asterisk = screen.getByText("*");

    // Check that both elements are present and have the correct styling
    expect(requiredText).toBeInTheDocument();
    expect(asterisk).toBeInTheDocument();
    expect(requiredText).toHaveClass("text-body-11", "text-secondary");
    expect(asterisk).toHaveClass("mantine-focus-auto", "text-red", "mb-[-2px]");

    // Check that they are siblings (part of the same group)
    const parent = requiredText.parentElement;
    expect(parent).toContainElement(asterisk);
  });

  it("should handle multiple rapid clicks correctly", () => {
    const mockOnClick = jest.fn();
    renderComponent({ onClick: mockOnClick });

    const button = screen.getByRole("button");

    fireEvent.click(button);
    fireEvent.click(button);
    fireEvent.click(button);

    expect(mockOnClick).toHaveBeenCalledTimes(3);
  });

  it("should maintain button state when disabled and loading", () => {
    renderComponent({ isDisabled: true, isLoading: true });

    const button = screen.getByRole("button");
    expect(button).toBeDisabled();
    expect(button).toHaveAttribute("data-loading", "true");
  });

  it("should render with all props combined", () => {
    const mockOnClick = jest.fn();
    renderComponent({
      onClick: mockOnClick,
      label: "Submit Form",
      isLoading: false,
      isDisabled: false,
    });

    const button = screen.getByRole("button");
    const requiredText = screen.getByText("Required information");

    expect(button).toBeInTheDocument();
    expect(button).toHaveTextContent("Submit Form");
    expect(button).not.toBeDisabled();
    expect(button).not.toHaveAttribute("data-loading", "true");
    expect(requiredText).toBeInTheDocument();
  });
});
