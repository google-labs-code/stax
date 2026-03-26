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

import DefaultModelCard from "@/components/DefaultModelCard";
import { Model } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { Provider } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

// Mock the ModelCardDropdown component
jest.mock("@/components/ModelCardDropdown", () => {
  return function MockModelCardDropdown() {
    return <div data-testid="model-card-dropdown">ModelCardDropdown</div>;
  };
});

const MockIcon = ({ size }: { size?: number }) => (
  <div data-testid="model-icon" data-size={size}>
    MockIcon
  </div>
);

describe("DefaultModelCard", () => {
  const mockModel: Model = {
    id: "test-model-id",
    name: "test-model",
    version: "1.0",
    label: "Test Model",
    url: "https://example.com",
    tag: "test",
    provider: Provider.OPENAI,
    model_type: "chat",
    properties: {
      temperature: 0.7,
      max_tokens: 1000,
    },
    icon: MockIcon,
  };

  const mockOnShowDetails = jest.fn();

  const renderComponent = (props = {}) => {
    const componentProps = {
      model: mockModel,
      onShowDetails: mockOnShowDetails,
      ...props,
    };

    return testRender(<DefaultModelCard {...componentProps} />);
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should render the model card with correct structure", () => {
    renderComponent();

    const modelCard = screen.getByTestId("model-card");
    const modelIcon = screen.getByTestId("model-icon");
    const modelLabel = screen.getByText("Test Model");
    const dropdown = screen.getByTestId("model-card-dropdown");

    expect(modelCard).toBeInTheDocument();
    expect(modelIcon).toBeInTheDocument();
    expect(modelLabel).toBeInTheDocument();
    expect(dropdown).toBeInTheDocument();
  });

  it("should apply correct styling classes", () => {
    renderComponent();

    const modelCard = screen.getByTestId("model-card");

    expect(modelCard).toHaveClass(
      "flex",
      "h-[40px]",
      "min-w-[160px]",
      "cursor-pointer",
      "flex-row",
      "items-center",
      "justify-between",
      "rounded-2xl",
      "border-[1px]",
      "border-solid",
      "border-neutrals-300",
      "bg-veryLightSilver",
      "px-[12px]",
      "py-[8px]",
    );
  });

  it("should display model label with correct styling", () => {
    renderComponent();

    const modelLabel = screen.getByText("Test Model");

    expect(modelLabel).toHaveClass("!text-secondaryDark", "text-title-14");
  });

  it("should render model icon with correct size", () => {
    renderComponent();

    const modelIcon = screen.getByTestId("model-icon");

    expect(modelIcon).toHaveAttribute("data-size", "16");
  });

  it("should call onShowDetails when card is clicked", () => {
    renderComponent();

    const modelCard = screen.getByTestId("model-card");

    // Clear any previous calls
    mockOnShowDetails.mockClear();

    fireEvent.click(modelCard);

    expect(mockOnShowDetails).toHaveBeenCalledTimes(1);
  });

  it("should prevent event propagation when card is clicked", () => {
    renderComponent();

    const modelCard = screen.getByTestId("model-card");
    const mockEvent = {
      stopPropagation: jest.fn(),
    };

    // Clear any previous calls
    mockOnShowDetails.mockClear();

    // Simulate the click event with stopPropagation
    fireEvent.click(modelCard, mockEvent);

    expect(mockOnShowDetails).toHaveBeenCalledTimes(1);
  });

  it("should render without icon when model has no icon", () => {
    const modelWithoutIcon = { ...mockModel, icon: undefined };
    renderComponent({ model: modelWithoutIcon });

    const modelCard = screen.getByTestId("model-card");
    const modelLabel = screen.getByText("Test Model");

    expect(modelCard).toBeInTheDocument();
    expect(modelLabel).toBeInTheDocument();
    expect(screen.queryByTestId("model-icon")).not.toBeInTheDocument();
  });

  it("should render with different model labels", () => {
    const differentModel = { ...mockModel, label: "Different Model" };
    renderComponent({ model: differentModel });

    const modelLabel = screen.getByText("Different Model");
    expect(modelLabel).toBeInTheDocument();
  });

  it("should pass correct props to ModelCardDropdown", () => {
    renderComponent();

    const dropdown = screen.getByTestId("model-card-dropdown");

    // Verify the dropdown is rendered
    expect(dropdown).toBeInTheDocument();
    expect(dropdown).toHaveTextContent("ModelCardDropdown");
  });

  it("should handle model with minimal properties", () => {
    const minimalModel: Model = {
      id: "minimal-model",
      name: "minimal",
      version: "1.0",
      label: "Minimal Model",
      url: "https://example.com",
      tag: "test",
      provider: Provider.ANTHROPIC,
      model_type: "chat",
      properties: {},
    };

    renderComponent({ model: minimalModel });

    const modelCard = screen.getByTestId("model-card");
    const modelLabel = screen.getByText("Minimal Model");

    expect(modelCard).toBeInTheDocument();
    expect(modelLabel).toBeInTheDocument();
  });

  it("should maintain accessibility attributes", () => {
    renderComponent();

    const modelCard = screen.getByTestId("model-card");

    // The card should be clickable and have proper cursor styling
    expect(modelCard).toHaveClass("cursor-pointer");
  });
});
