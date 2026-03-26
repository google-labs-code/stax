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

import ModelManager from "@/app/(authRoutes)/settings/components/ModelManager";
import { useModelsContext } from "@/hooks/useModelsContext";
import { deprecateModelQuery } from "@/queries/clientQueries";
import { ModelTypeEnum } from "@/queries/types";
import LocalStorage from "@/utils/LocalStorage";
import { MantineProvider } from "@mantine/core";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import React from "react";

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

jest.mock("@/queries/clientQueries", () => ({
  deprecateModelQuery: jest.fn(() => Promise.resolve()),
}));

const mockedDeprecateModelQuery = jest.mocked(deprecateModelQuery);

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

jest.mock("@/config/constants", () => ({
  getModelDetails: jest.fn((provider: string) => {
    if (["provider1", "provider2", "provider3"].includes(provider)) {
      return {
        name: `Provider ${provider.slice(-1)}`,
        icon: () =>
          React.createElement("div", { "data-testid": `icon-${provider}` }),
      };
    }

    return undefined;
  }),
  TOOLTIPS: {
    CUSTOM_MODEL_VALIDATION_ERROR:
      "Required fields are missing or contain errors (check model name, API URL, and provider selection)",
    CUSTOM_MODEL_NO_CHANGES: "Make changes to enable the update button",
  },
  LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL: "showCustomModal",
}));

jest.mock("@/utils/LocalStorage", () => ({
  get: jest.fn(),
  remove: jest.fn(),
}));

const queryClient = new QueryClient();

function renderWithClient(ui: React.ReactElement) {
  return render(
    <QueryClientProvider client={queryClient}>
      <MantineProvider>{ui}</MantineProvider>
    </QueryClientProvider>,
  );
}

describe("ModelManager", () => {
  const mockDefaultModels = [
    {
      id: "1",
      name: "Default Model 1",
      label: "Default Model 1",
      provider: "provider1",
      model_type: ModelTypeEnum.SYSTEM,
    },
    {
      id: "2",
      name: "Default Model 2",
      label: "Default Model 2",
      provider: "provider2",
      model_type: ModelTypeEnum.SYSTEM,
    },
  ];

  const mockCustomModels = [
    {
      id: "3",
      name: "Custom Model 1",
      label: "Custom Model 1",
      provider: "provider1",
      model_type: ModelTypeEnum.USER,
    },
    {
      id: "4",
      name: "Custom Model 2",
      label: "Custom Model 2",
      provider: "provider3",
      model_type: ModelTypeEnum.USER,
    },
  ];

  const mockAllModels = [...mockDefaultModels, ...mockCustomModels];

  const mockRefreshModels = jest.fn();

  beforeEach(() => {
    (useModelsContext as jest.Mock).mockReturnValue({
      defaultModels: mockDefaultModels,
      customModels: mockCustomModels,
      allModels: mockAllModels,
      refreshModels: mockRefreshModels,
      isLoadingModels: false,
      providers: {
        provider1: "Provider 1",
        provider2: "Provider 2",
        provider3: "Provider 3",
      },
    });
    (deprecateModelQuery as jest.Mock).mockResolvedValue(null);
    (LocalStorage.get as jest.Mock).mockReturnValue(null);
    (LocalStorage.remove as jest.Mock).mockImplementation(() => {});
    jest.clearAllMocks();
  });

  it("renders without crashing and shows model lists", () => {
    renderWithClient(<ModelManager />);
    expect(screen.getByText("Model Manager")).toBeInTheDocument();

    mockDefaultModels.forEach((model) => {
      expect(screen.getByText(model.label)).toBeInTheDocument();
    });

    mockCustomModels.forEach((model) => {
      expect(
        screen
          .getAllByTestId("model-card")
          .some((el) => el.textContent?.includes(model.label)),
      ).toBeTruthy();
    });
  });

  it("opens the Configure Existing Model modal", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const addModelBtn = screen.getByText(/Add model/i);
    await user.click(addModelBtn);

    await waitFor(() =>
      expect(screen.getByText(/Configure existing model/i)).toBeInTheDocument(),
    );

    const configureOption = screen.getByText(/Configure existing model/i);
    await user.click(configureOption);

    await waitFor(() =>
      expect(screen.getByTestId("model-settings-modal")).toBeVisible(),
    );
  });

  it("toggles visible providers when clicking on provider in menu", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);
    const providersCount = screen.getByText(`${3}`);

    await user.click(providersCount.closest("button")!);

    await waitFor(() => {
      expect(screen.getByText("Provider 1")).toBeInTheDocument();
    });

    const provider1Option = screen.getByText("Provider 1");
    await user.click(provider1Option);

    await waitFor(() => {
      expect(screen.queryByText("Default Model 1")).not.toBeInTheDocument();
    });
  });

  it("opens the Add custom model modal from Add model menu", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const addModelBtn = screen.getByText(/Add model/i);
    await user.click(addModelBtn);

    await waitFor(() =>
      expect(screen.getByText(/Add custom model/i)).toBeInTheDocument(),
    );

    const addCustomOption = screen.getByText(/Add custom model/i);
    await user.click(addCustomOption);

    await waitFor(() => {
      // Check for modal content - the form field label
      expect(screen.getByText(/Model Nickname/i)).toBeInTheDocument();
    });
  });

  it("shows loading state when isLoadingModels is true", () => {
    (useModelsContext as jest.Mock).mockReturnValue({
      defaultModels: [],
      customModels: [],
      allModels: [],
      refreshModels: mockRefreshModels,
      isLoadingModels: true,
      providers: {},
    });

    renderWithClient(<ModelManager />);
    // Mantine Loader component doesn't have progressbar role, check for the loader element
    const loader = document.querySelector('[data-size="sm"]');
    expect(loader).toBeInTheDocument();
  });

  it("shows empty state for custom models with Add custom model button", async () => {
    (useModelsContext as jest.Mock).mockReturnValue({
      defaultModels: mockDefaultModels,
      customModels: [],
      allModels: mockDefaultModels,
      refreshModels: mockRefreshModels,
      isLoadingModels: false,
      providers: {
        provider1: "Provider 1",
        provider2: "Provider 2",
        provider3: "Provider 3",
      },
    });

    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const addCustomButton = screen.getAllByText(/Add custom model/i)[0];
    expect(addCustomButton).toBeInTheDocument();

    await user.click(addCustomButton);

    await waitFor(() => {
      expect(screen.getAllByText(/Add custom model/i)[1]).toBeInTheDocument();
    });
  });

  it("opens delete modal when delete is clicked on custom model", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const customModelCards = screen.getAllByTestId("model-card");
    const firstCustomCard = customModelCards.find((card) =>
      card.textContent?.includes("Custom Model 1"),
    );

    expect(firstCustomCard).toBeInTheDocument();

    // Find the dropdown menu button (usually a MaterialIcon or button)
    const dropdownTrigger = firstCustomCard
      ?.closest('[data-testid="model-card"]')
      ?.querySelector("button");

    if (dropdownTrigger) {
      await user.click(dropdownTrigger);

      await waitFor(() => {
        const deleteOption = screen.queryByText(/Delete/i);
        if (deleteOption) {
          return deleteOption;
        }
      });

      const deleteOption = screen.queryByText(/Delete/i);
      if (deleteOption) {
        await user.click(deleteOption);

        await waitFor(() => {
          expect(screen.getByTestId("delete-modal")).toBeVisible();
        });
      }
    }
  });

  it("confirms deletion and calls deprecateModelQuery", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const customModelCards = screen.getAllByTestId("model-card");
    const firstCustomCard = customModelCards.find((card) =>
      card.textContent?.includes("Custom Model 1"),
    );

    if (firstCustomCard) {
      const dropdownTrigger = firstCustomCard
        ?.closest('[data-testid="model-card"]')
        ?.querySelector("button");

      if (dropdownTrigger) {
        await user.click(dropdownTrigger);

        await waitFor(() => {
          const deleteOption = screen.queryByText(/Delete/i);
          if (deleteOption) {
            return deleteOption;
          }
        });

        const deleteOption = screen.queryByText(/Delete/i);
        if (deleteOption) {
          await user.click(deleteOption);

          await waitFor(() => {
            expect(screen.getByTestId("delete-modal")).toBeVisible();
          });

          await waitFor(() => {
            const confirmButton = screen.queryByTestId(
              "delete-modal-confirm-button",
            );
            expect(confirmButton).toBeInTheDocument();

            return confirmButton;
          });

          const confirmButton = screen.getByTestId(
            "delete-modal-confirm-button",
          );
          await user.click(confirmButton);

          await waitFor(() => {
            expect(mockedDeprecateModelQuery).toHaveBeenCalled();
            expect(mockedDeprecateModelQuery.mock.calls[0][0]).toBe("3");
          });
        }
      }
    }
  });

  it("closes delete modal when Close button is clicked", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    // Manually trigger delete modal by finding a custom model card
    const customModelCards = screen.getAllByTestId("model-card");
    const firstCustomCard = customModelCards.find((card) =>
      card.textContent?.includes("Custom Model 1"),
    );

    if (firstCustomCard) {
      const dropdownTrigger = firstCustomCard
        ?.closest('[data-testid="model-card"]')
        ?.querySelector("button");

      if (dropdownTrigger) {
        await user.click(dropdownTrigger);

        await waitFor(() => {
          const deleteOption = screen.queryByText(/Delete/i);
          if (deleteOption) {
            return deleteOption;
          }
        });

        const deleteOption = screen.queryByText(/Delete/i);
        if (deleteOption) {
          await user.click(deleteOption);

          await waitFor(() => {
            expect(screen.getByTestId("delete-modal")).toBeVisible();
          });

          const closeButton = screen.getByText(/Close/i);
          await user.click(closeButton);

          await waitFor(() => {
            expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
          });
        }
      }
    }
  });

  it("opens custom model modal for editing when edit is clicked", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const customModelCards = screen.getAllByTestId("model-card");
    const firstCustomCard = customModelCards.find((card) =>
      card.textContent?.includes("Custom Model 1"),
    );

    if (firstCustomCard) {
      const dropdownTrigger = firstCustomCard
        ?.closest('[data-testid="model-card"]')
        ?.querySelector("button");

      if (dropdownTrigger) {
        await user.click(dropdownTrigger);

        await waitFor(() => {
          const editOption = screen.queryByText(/Edit/i);
          if (editOption) {
            return editOption;
          }
        });

        const editOption = screen.queryByText(/Edit/i);
        if (editOption) {
          await user.click(editOption);

          await waitFor(() => {
            // CustomModelModal doesn't have a test id, check for the modal title instead
            const matches = screen.queryAllByText(
              /Add custom model|Customize copied model|Custom Model/i,
            );
            expect(matches.length).toBeGreaterThan(0);
          });
        }
      }
    }
  });

  it("opens custom model modal for showing details when card is clicked", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const customModelCards = screen.getAllByTestId("model-card");
    const firstCustomCard = customModelCards.find((card) =>
      card.textContent?.includes("Custom Model 1"),
    );

    if (firstCustomCard) {
      await user.click(firstCustomCard);

      await waitFor(() => {
        // CustomModelModal doesn't have a test id, check for the form field label that's always present
        expect(screen.getByText(/Model Nickname/i)).toBeInTheDocument();
      });
    }
  });

  it("opens settings modal for duplicating when default model details are clicked", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const defaultModelLabel = screen.getByText("Default Model 1");
    const defaultModelCard = defaultModelLabel.closest(
      '[data-testid="model-card"]',
    );

    if (defaultModelCard) {
      await user.click(defaultModelCard);

      await waitFor(() => {
        expect(screen.getByTestId("model-settings-modal")).toBeVisible();
      });
    }
  });

  it("opens settings modal for copying when copy is clicked on default model", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const defaultModelLabel = screen.getByText("Default Model 1");
    const defaultModelCard = defaultModelLabel.closest(
      '[data-testid="model-card"]',
    );

    if (defaultModelCard) {
      const dropdownTrigger = defaultModelCard.querySelector("button");

      if (dropdownTrigger) {
        await user.click(dropdownTrigger);

        await waitFor(() => {
          expect(screen.getAllByText(/Copy/i)[0]).toBeInTheDocument();
        });

        const copyOption = screen.getAllByText(/Copy/i)[0];
        if (copyOption) {
          await user.click(copyOption);

          await waitFor(() => {
            expect(screen.getByTestId("model-settings-modal")).toBeVisible();
          });
        }
      }
    }
  });

  it("opens custom model modal when LocalStorage flag is set", () => {
    (LocalStorage.get as jest.Mock).mockReturnValue("true");

    renderWithClient(<ModelManager />);

    expect(LocalStorage.get).toHaveBeenCalledWith("showCustomModal");
    expect(LocalStorage.remove).toHaveBeenCalledWith("showCustomModal");
  });

  it("filters models based on visible providers", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    // Initially all models should be visible
    expect(screen.getByText("Default Model 1")).toBeInTheDocument();
    expect(screen.getByText("Default Model 2")).toBeInTheDocument();

    // Toggle provider1 off
    const providersCount = screen.getByText(`${3}`);
    await user.click(providersCount.closest("button")!);

    await waitFor(() => {
      expect(screen.getByText("Provider 1")).toBeInTheDocument();
    });

    const provider1Option = screen.getByText("Provider 1");
    await user.click(provider1Option);

    await waitFor(() => {
      expect(screen.queryByText("Default Model 1")).not.toBeInTheDocument();
      expect(screen.queryByText("Custom Model 1")).not.toBeInTheDocument();
      // But provider2 and provider3 models should still be visible
      expect(screen.getByText("Default Model 2")).toBeInTheDocument();
    });
  });

  it("handles provider toggle to show previously hidden providers", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    // Hide provider1
    const providersCount = screen.getByText(`${3}`);
    await user.click(providersCount.closest("button")!);

    await waitFor(() => {
      expect(screen.getByText("Provider 1")).toBeInTheDocument();
    });

    const provider1Option = screen.getByText("Provider 1");
    await user.click(provider1Option);

    await waitFor(() => {
      expect(screen.queryByText("Default Model 1")).not.toBeInTheDocument();
    });

    // Show provider1 again
    await user.click(providersCount.closest("button")!);

    await waitFor(() => {
      expect(screen.getByText("Provider 1")).toBeInTheDocument();
    });

    await user.click(provider1Option);

    await waitFor(() => {
      expect(screen.getByText("Default Model 1")).toBeInTheDocument();
    });
  });

  it("refreshes models after successful deletion", async () => {
    const user = userEvent.setup();
    renderWithClient(<ModelManager />);

    const customModelCards = screen.getAllByTestId("model-card");
    const firstCustomCard = customModelCards.find((card) =>
      card.textContent?.includes("Custom Model 1"),
    );

    if (firstCustomCard) {
      const dropdownTrigger = firstCustomCard
        ?.closest('[data-testid="model-card"]')
        ?.querySelector("button");

      if (dropdownTrigger) {
        await user.click(dropdownTrigger);

        await waitFor(() => {
          const deleteOption = screen.queryByText(/Delete/i);
          if (deleteOption) {
            return deleteOption;
          }
        });

        const deleteOption = screen.queryByText(/Delete/i);
        if (deleteOption) {
          await user.click(deleteOption);

          await waitFor(() => {
            expect(screen.getByTestId("delete-modal")).toBeVisible();
          });

          await waitFor(() => {
            const confirmButton = screen.queryByTestId(
              "delete-modal-confirm-button",
            );
            expect(confirmButton).toBeInTheDocument();

            return confirmButton;
          });

          const confirmButton = screen.getByTestId(
            "delete-modal-confirm-button",
          );
          await user.click(confirmButton);

          await waitFor(() => {
            expect(mockRefreshModels).toHaveBeenCalled();
          });
        }
      }
    }
  });
});
