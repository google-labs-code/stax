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

import ModelConfigurationModal from "@/components/ModelConfigurationModal";
import { useChatContext } from "@/hooks/useChatContext";
import { useModelsContext } from "@/hooks/useModelsContext";
import { duplicateModelQuery } from "@/queries/clientQueries";
import { notifications } from "@mantine/notifications";
import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { useRouter } from "next/navigation";

import { testRender } from "../render";

jest.mock("@/hooks/useModelsContext");
jest.mock("@/hooks/useChatContext");
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));
jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
    hide: jest.fn(),
  },
}));

jest.mock("@/queries/clientQueries", () => ({
  duplicateModelQuery: jest.fn(),
}));

const mockRefreshModels = jest.fn();
const mockUpdateChat = jest.fn();
const mockPush = jest.fn();
const mockOnClose = jest.fn();

const openedModelMock = {
  id: "model1",
  label: "Test Model",
  properties: {
    temperature: 0.5,
    max_tokens: 100,
    top_p: 0.9,
    seed: 123,
  },
  descriptors: {
    temperature: {
      defaultValue: 0.7,
      description: "temp desc",
      minValue: 0,
      maxValue: 2,
    },
    max_output_tokens: {
      defaultValue: 200,
      description: "max tokens desc",
      minValue: 1,
      maxValue: 500,
    },
    top_p: {
      defaultValue: 0.8,
      description: "top p desc",
      minValue: 0,
      maxValue: 1,
    },
    seed: {
      defaultValue: 42,
      description: "seed desc",
      minValue: 0,
      maxValue: 1000,
    },
  },
};

describe("ModelConfigurationModal", () => {
  beforeEach(() => {
    (useModelsContext as jest.Mock).mockReturnValue({
      openedModel: openedModelMock,
      refreshModels: mockRefreshModels,
    });
    (useChatContext as jest.Mock).mockReturnValue({
      updateChat: mockUpdateChat,
    });
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });
    jest.clearAllMocks();
  });

  it("renders modal with model label", () => {
    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );
    expect(screen.getByText("Test Model")).toBeInTheDocument();
  });

  it("resets form values when Reset button clicked", () => {
    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );
    const resetButton = screen.getByRole("button", { name: /reset/i });
    fireEvent.click(resetButton);
    expect(resetButton).toBeEnabled();
  });

  it("disables Save button if form is not dirty or has errors", () => {
    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );
    const saveButton = screen.getByRole("button", { name: /save/i });
    expect(saveButton).toBeDisabled();
  });

  it("does not crash if no openedModel is present", () => {
    (useModelsContext as jest.Mock).mockReturnValue({
      openedModel: undefined,
      refreshModels: mockRefreshModels,
    });

    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );
    expect(screen.queryByText("Test Model")).not.toBeInTheDocument();
  });

  it("enables the Save button when form values are changed and valid", async () => {
    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );

    const temperatureText = screen.getByText("Temperature");
    const temperatureContainer = temperatureText.closest(
      ".flex.flex-col.gap-2",
    );
    if (!temperatureContainer)
      throw new Error("Temperature container not found");
    const temperatureInput = within(
      temperatureContainer as HTMLElement,
    ).getByRole("textbox");

    fireEvent.change(temperatureInput, { target: { value: "1.2" } });

    await waitFor(() => {
      expect(screen.getByRole("button", { name: /save/i })).toBeEnabled();
    });
  });

  it("disables the Save button when form has validation errors", async () => {
    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );

    const temperatureText = screen.getByText("Temperature");
    const temperatureContainer = temperatureText.closest(
      ".flex.flex-col.gap-2",
    );
    if (!temperatureContainer)
      throw new Error("Temperature container not found");
    const temperatureInput = within(
      temperatureContainer as HTMLElement,
    ).getByRole("textbox");

    fireEvent.change(temperatureInput, { target: { value: "3" } });

    await waitFor(() => {
      expect(screen.getByRole("button", { name: /save/i })).toBeDisabled();
    });
  });

  it("handles the seed field correctly", async () => {
    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );

    const seedText = screen.getByText("Seed");
    const seedContainer = seedText.closest(".flex.flex-col.gap-2");
    if (!seedContainer) throw new Error("Seed container not found");
    const seedInput = within(seedContainer as HTMLElement).getByRole("textbox");

    fireEvent.change(seedInput, { target: { value: "500" } });
    expect(seedInput).toHaveValue("500");

    await waitFor(() => {
      expect(screen.getByRole("button", { name: /save/i })).toBeEnabled();
    });

    fireEvent.change(seedInput, { target: { value: "1500" } });
    fireEvent.blur(seedInput);

    expect(seedInput).toHaveValue("1500");
  });

  it("submits the form and calls duplicateModelQuery when Save is clicked", async () => {
    (duplicateModelQuery as jest.Mock).mockResolvedValue({
      id: "new-model-id",
      label: "New Custom Model",
    });

    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );

    const temperatureText = screen.getByText("Temperature");
    const temperatureContainer = temperatureText.closest(
      ".flex.flex-col.gap-2",
    );
    if (!temperatureContainer)
      throw new Error("Temperature container not found");
    const temperatureInput = within(
      temperatureContainer as HTMLElement,
    ).getByRole("textbox");

    fireEvent.change(temperatureInput, { target: { value: "1.0" } });

    const saveButton = screen.getByRole("button", { name: /save/i });
    expect(saveButton).toBeEnabled();
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(duplicateModelQuery).toHaveBeenCalled();
      expect(mockRefreshModels).toHaveBeenCalled();
    });
  });

  it("shows success notification and updates chat when model is duplicated successfully", async () => {
    const newModel = {
      id: "new-model-id",
      label: "New Custom Model",
      name: "test-model",
      version: "1.0",
      properties: {},
      descriptors: {},
      url: "https://api.example.com",
      tag: "test-tag",
      provider: "test-provider",
      model_type: "test-type",
      additional_headers: {},
    };
    (duplicateModelQuery as jest.Mock).mockResolvedValue(newModel);

    const mockChat = {
      id: "chat-1",
      model: null,
      messages: [],
    };

    testRender(
      <ModelConfigurationModal
        chat={mockChat}
        isOpened={true}
        onClose={mockOnClose}
      />,
    );

    const temperatureText = screen.getByText("Temperature");
    const temperatureContainer = temperatureText.closest(
      ".flex.flex-col.gap-2",
    );
    if (!temperatureContainer)
      throw new Error("Temperature container not found");
    const temperatureInput = within(
      temperatureContainer as HTMLElement,
    ).getByRole("textbox");

    fireEvent.change(temperatureInput, { target: { value: "1.0" } });

    const saveButton = screen.getByRole("button", { name: /save/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(mockUpdateChat).toHaveBeenCalledWith(
        mockChat.id,
        "model",
        newModel,
      );
      expect(notifications.show).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  it("shows error notification when model duplication fails", async () => {
    const errorMessage = "Failed to duplicate model";
    (duplicateModelQuery as jest.Mock).mockRejectedValue(
      new Error(errorMessage),
    );

    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );

    const temperatureText = screen.getByText("Temperature");
    const temperatureContainer = temperatureText.closest(
      ".flex.flex-col.gap-2",
    );
    if (!temperatureContainer)
      throw new Error("Temperature container not found");
    const temperatureInput = within(
      temperatureContainer as HTMLElement,
    ).getByRole("textbox");

    fireEvent.change(temperatureInput, { target: { value: "1.0" } });

    const saveButton = screen.getByRole("button", { name: /save/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  it("updates form fields when using Reset button", async () => {
    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );

    const temperatureText = screen.getByText("Temperature");
    const temperatureContainer = temperatureText.closest(
      ".flex.flex-col.gap-2",
    );
    if (!temperatureContainer)
      throw new Error("Temperature container not found");
    const temperatureInput = within(
      temperatureContainer as HTMLElement,
    ).getByRole("textbox");

    fireEvent.change(temperatureInput, { target: { value: "1.5" } });

    await waitFor(() => {
      expect(screen.getByRole("button", { name: /save/i })).toBeEnabled();
    });

    const resetButton = screen.getByRole("button", { name: /reset/i });
    fireEvent.click(resetButton);

    await waitFor(() => {
      expect(screen.getByRole("button", { name: /save/i })).toBeDisabled();
    });
  });

  it("navigates to settings when Settings button in notification is clicked", async () => {
    const newModel = {
      id: "new-model-id",
      label: "New Custom Model",
      name: "test-model",
      version: "1.0",
      properties: {},
      descriptors: {},
      url: "https://api.example.com",
      tag: "test-tag",
      provider: "test-provider",
      model_type: "test-type",
      additional_headers: {},
    };
    (duplicateModelQuery as jest.Mock).mockResolvedValue(newModel);

    testRender(
      <ModelConfigurationModal isOpened={true} onClose={mockOnClose} />,
    );

    const temperatureText = screen.getByText("Temperature");
    const temperatureContainer = temperatureText.closest(
      ".flex.flex-col.gap-2",
    );
    if (!temperatureContainer)
      throw new Error("Temperature container not found");
    const temperatureInput = within(
      temperatureContainer as HTMLElement,
    ).getByRole("textbox");

    fireEvent.change(temperatureInput, { target: { value: "1.0" } });

    const saveButton = screen.getByRole("button", { name: /save/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalled();
    });

    const notificationConfig = (notifications.show as jest.Mock).mock
      .calls[0][0];
    const onClickHandler =
      notificationConfig.message.props.children[1].props.onClick;
    onClickHandler();

    expect(mockPush).toHaveBeenCalledWith("/settings?tab=modelManager");
    expect(notifications.hide).toHaveBeenCalled();
  });
});
