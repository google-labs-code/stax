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

import InputCardModelSelector from "@/app/(authRoutes)/projects/[id]/playground/components/Input/InputCardModelSelector";
import { useModelsContext } from "@/hooks/useModelsContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { testRender } from "@/tests-unit/render";
import { GAevents } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/utils/logGAevent", () => jest.fn());

jest.mock("@/components/ModelComboBox", () => {
  return function MockModelCombobox({ onSelect }: any) {
    return (
      <div data-testid="model-combobox">
        <button onClick={() => onSelect({ provider: "mockProvider" })}>
          Select Model
        </button>
      </div>
    );
  };
});

jest.mock("@/components/ModelConfigurationModal", () => {
  return function MockModelConfigurationModal({ isOpened, onClose }: any) {
    return isOpened ? (
      <div data-testid="model-configuration-modal">
        <button onClick={onClose}>Close Modal</button>
      </div>
    ) : null;
  };
});

jest.mock("@/components/MaterialIcon", () => {
  return function MockMaterialIcon({ name, className }: any) {
    return <span data-testid={`material-icon-${name}`} className={className} />;
  };
});

describe("@/app/(authRoutes)/projects/[id]/playground/components/Input/InputCardModelSelector", () => {
  const mockSetIsPlaygroundChanged = jest.fn();
  const mockSetOpenedModel = jest.fn();
  const mockSetModel = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (usePlaygroundContext as jest.Mock).mockReturnValue({
      setIsPlaygroundModified: mockSetIsPlaygroundChanged,
      setIsNewChat: jest.fn(),
    });

    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: [
        { provider: "mockProvider", id: "1" },
        { provider: "otherProvider", id: "2" },
      ],
      isLoadingModels: false,
      setOpenedModel: mockSetOpenedModel,
    });
  });

  const mockModel = { provider: "mockProvider", id: "1" };

  const setup = (componentProps: any = {}) =>
    testRender(
      <InputCardModelSelector
        model={mockModel}
        setModel={mockSetModel}
        {...componentProps}
      />,
    );

  it("renders the ModelCombobox and settings button", () => {
    setup();

    expect(screen.getByTestId("model-combobox")).toBeInTheDocument();
    expect(screen.getByTestId("material-icon-tune")).toBeInTheDocument();
  });

  it("calls onSelect when a model is selected", () => {
    setup();

    fireEvent.click(screen.getByText("Select Model"));

    expect(logGAevent).toHaveBeenCalledWith(GAevents.SELECT_MODEL, {
      source: "playground",
      provider: "mockProvider",
    });

    expect(mockSetIsPlaygroundChanged).toHaveBeenCalledWith(true);
    expect(mockSetOpenedModel).toHaveBeenCalledWith({
      provider: "mockProvider",
    });
    expect(mockSetModel).toHaveBeenCalledWith({ provider: "mockProvider" });
  });

  it("opens the configuration modal when the settings button is clicked and model provider exists", () => {
    setup();

    const settingsButton = screen.getByTestId("material-icon-tune");
    fireEvent.click(settingsButton);

    expect(screen.getByTestId("model-configuration-modal")).toBeInTheDocument();
  });

  it("does not open the configuration modal if the model provider is missing", () => {
    setup({ model: null });

    const settingsButton = screen.getByTestId("material-icon-tune");
    fireEvent.click(settingsButton);

    expect(
      screen.queryByTestId("model-configuration-modal"),
    ).not.toBeInTheDocument();
  });

  it("closes the configuration modal when the close button is clicked", () => {
    setup();

    const settingsButton = screen.getByTestId("material-icon-tune");
    fireEvent.click(settingsButton);

    expect(screen.getByTestId("model-configuration-modal")).toBeInTheDocument();

    fireEvent.click(screen.getByText("Close Modal"));

    expect(
      screen.queryByTestId("model-configuration-modal"),
    ).not.toBeInTheDocument();
  });
});
