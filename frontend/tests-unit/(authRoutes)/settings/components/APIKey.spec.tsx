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

import APIKey from "@/app/(authRoutes)/settings/components/APIKey";
import { testRender } from "@/tests-unit/render";
import { GAevents, Provider } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { notifications } from "@mantine/notifications";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { act, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// Mock the queries
jest.mock("@/app/(authRoutes)/settings/state/queries", () => ({
  addUserKeyQuery: jest.fn(),
  deleteUserKeyQuery: jest.fn(),
}));

// Mock the notifications
jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

// Mock useQueryClient
jest.mock("@tanstack/react-query", () => ({
  ...jest.requireActual("@tanstack/react-query"),
  useMutation: jest.fn(),
  useQueryClient: jest.fn(),
}));

// Mock useModelsContext
jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

// Mock logGAevent
jest.mock("@/utils/logGAevent", () => jest.fn());

const mockNotifications = notifications as jest.Mocked<typeof notifications>;
const mockUseMutation = useMutation as jest.MockedFunction<typeof useMutation>;
const mockUseQueryClient = useQueryClient as jest.MockedFunction<
  typeof useQueryClient
>;
const mockLogGAevent = logGAevent as jest.MockedFunction<typeof logGAevent>;

describe("APIKey", () => {
  const mockKeyData = {
    label: "OpenAI",
    provider: Provider.OPENAI,
    apiKey: "openaiKeyPresent" as const,
    isKeyPresent: false,
    link: "https://platform.openai.com/api-keys",
  };

  const mockKeyDataWithKey = {
    ...mockKeyData,
    isKeyPresent: true,
  };

  const mockOnChange = jest.fn();
  const mockInvalidateQueries = jest.fn();
  const mockRefreshModels = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockOnChange.mockClear();
    mockInvalidateQueries.mockClear();
    mockRefreshModels.mockClear();

    // Mock useQueryClient
    mockUseQueryClient.mockReturnValue({
      invalidateQueries: mockInvalidateQueries,
    } as any);

    // Mock useModelsContext
    const { useModelsContext } = require("@/hooks/useModelsContext");
    useModelsContext.mockReturnValue({
      refreshModels: mockRefreshModels,
    });

    // Set up default useMutation mock
    mockUseMutation.mockReturnValue({
      mutate: jest.fn(),
      isPending: false,
    } as any);
  });

  const renderComponent = (keyData: any = mockKeyData) => {
    return testRender(<APIKey keyData={keyData} onChange={mockOnChange} />);
  };

  describe("Rendering", () => {
    it("should render the component with correct label", () => {
      renderComponent();

      expect(screen.getByText("OpenAI")).toBeInTheDocument();
    });

    it("should render input field with placeholder", () => {
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");
      expect(input).toBeInTheDocument();
      expect(input).toHaveAttribute("placeholder", "Add your key");
    });

    it("should render 'Add key' button when no key is present", () => {
      renderComponent();

      expect(screen.getByTestId("add-key-button")).toBeInTheDocument();
      expect(screen.getByText("Add key")).toBeInTheDocument();
    });

    it("should render 'Delete key' button when key is present", () => {
      renderComponent(mockKeyDataWithKey);

      expect(screen.getByTestId("delete-key-button")).toBeInTheDocument();
      expect(screen.getByText("Delete key")).toBeInTheDocument();
    });

    it("should render link to get API key", () => {
      renderComponent();

      const link = screen.getByText("Get OpenAI API Key");
      expect(link).toBeInTheDocument();
      expect(link).toHaveAttribute(
        "href",
        "https://platform.openai.com/api-keys",
      );
      expect(link).toHaveAttribute("target", "_blank");
    });

    it("should display masked key when key is present", () => {
      renderComponent(mockKeyDataWithKey);

      const input = screen.getByTestId("api-key-input-field");
      expect(input).toHaveValue("openai-****");
      expect(input).toBeDisabled();
    });

    it("should have correct data-testid for container", () => {
      renderComponent();

      expect(screen.getByTestId("OPENAI-key-container")).toBeInTheDocument();
    });
  });

  describe("Adding API Key", () => {
    const mockAddMutation = {
      mutate: jest.fn(),
      isPending: false,
    };

    beforeEach(() => {
      mockUseMutation.mockReturnValue(mockAddMutation as any);
    });

    it("should call addUserKeyQuery when adding a valid key", async () => {
      const user = userEvent.setup();
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");
      const addButton = screen.getByTestId("add-key-button");

      await user.type(input, "test-api-key");
      await user.click(addButton);

      expect(mockAddMutation.mutate).toHaveBeenCalledWith({
        provider: Provider.OPENAI,
        key: "test-api-key",
      });
    });

    it("should show error notification when trying to add empty key", async () => {
      const user = userEvent.setup();
      renderComponent();

      const addButton = screen.getByTestId("add-key-button");

      await user.click(addButton);

      expect(mockNotifications.show).toHaveBeenCalledWith(
        expect.objectContaining({
          id: "OPENAI-key-empty",
          color: "transparent",
        }),
      );
    });

    it("should show error notification when trying to add whitespace-only key", async () => {
      const user = userEvent.setup();
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");
      const addButton = screen.getByTestId("add-key-button");

      await user.type(input, "   ");
      await user.click(addButton);

      expect(mockNotifications.show).toHaveBeenCalledWith(
        expect.objectContaining({
          id: "OPENAI-key-empty",
          color: "transparent",
        }),
      );
    });

    it("should log GA event when adding key", async () => {
      const user = userEvent.setup();
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");
      const addButton = screen.getByTestId("add-key-button");

      await user.type(input, "test-api-key");
      await user.click(addButton);

      expect(mockLogGAevent).toHaveBeenCalledWith(GAevents.ADDS_API_KEY, {
        provider: Provider.OPENAI,
      });
    });

    it("should handle Enter key press to add key", async () => {
      const user = userEvent.setup();
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");

      await user.type(input, "test-api-key{enter}");

      expect(mockAddMutation.mutate).toHaveBeenCalledWith({
        provider: Provider.OPENAI,
        key: "test-api-key",
      });
    });

    it("should show loading state on add button when mutation is pending", () => {
      mockUseMutation.mockReturnValue({
        ...mockAddMutation,
        isPending: true,
      } as any);

      renderComponent();

      const addButton = screen.getByTestId("add-key-button");
      expect(addButton).toHaveAttribute("data-loading", "true");
    });

    it("should handle successful key addition", async () => {
      const user = userEvent.setup();
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");
      const addButton = screen.getByTestId("add-key-button");

      await user.type(input, "test-api-key");
      await user.click(addButton);

      // Simulate successful mutation with act
      const onSuccess = mockUseMutation.mock.calls[0][0].onSuccess;
      if (onSuccess) {
        await act(async () => {
          onSuccess({}, { provider: Provider.OPENAI, key: "test-api-key" }, {});
        });
      }

      await waitFor(() => {
        expect(mockNotifications.show).toHaveBeenCalledWith(
          expect.objectContaining({
            message: "API key added successfully.",
            color: "transparent",
          }),
        );
      });

      expect(mockInvalidateQueries).toHaveBeenCalledWith({
        queryKey: ["user-api-keys"],
      });

      expect(mockRefreshModels).toHaveBeenCalled();
      expect(mockOnChange).toHaveBeenCalled();
    });
  });

  describe("Deleting API Key", () => {
    const mockDeleteMutation = {
      mutate: jest.fn(),
      isPending: false,
    };

    beforeEach(() => {
      mockUseMutation.mockReturnValue(mockDeleteMutation as any);
    });

    it("should call deleteUserKeyQuery when deleting key", async () => {
      const user = userEvent.setup();
      renderComponent(mockKeyDataWithKey);

      const deleteButton = screen.getByTestId("delete-key-button");

      await user.click(deleteButton);

      expect(mockDeleteMutation.mutate).toHaveBeenCalledWith({
        provider: Provider.OPENAI,
      });
    });

    it("should show loading state on delete button when mutation is pending", () => {
      mockUseMutation.mockReturnValue({
        ...mockDeleteMutation,
        isPending: true,
      } as any);

      renderComponent(mockKeyDataWithKey);

      const deleteButton = screen.getByTestId("delete-key-button");
      expect(deleteButton).toHaveAttribute("data-loading", "true");
    });

    it("should handle successful key deletion", async () => {
      const user = userEvent.setup();
      renderComponent(mockKeyDataWithKey);

      const deleteButton = screen.getByTestId("delete-key-button");

      await user.click(deleteButton);

      // Simulate successful mutation with act
      const onSuccess = mockUseMutation.mock.calls[1][0].onSuccess;
      if (onSuccess) {
        await act(async () => {
          onSuccess({}, { provider: Provider.OPENAI }, {});
        });
      }

      await waitFor(() => {
        expect(mockNotifications.show).toHaveBeenCalledWith(
          expect.objectContaining({
            message: "API key deleted successfully.",
            color: "transparent",
          }),
        );
      });

      expect(mockInvalidateQueries).toHaveBeenCalledWith({
        queryKey: ["user-api-keys"],
      });

      expect(mockRefreshModels).toHaveBeenCalled();
      expect(mockOnChange).toHaveBeenCalled();
    });
  });

  describe("Mutation Success Handlers", () => {
    const mockAddMutation = {
      mutate: jest.fn(),
      isPending: false,
    };

    const mockDeleteMutation = {
      mutate: jest.fn(),
      isPending: false,
    };

    beforeEach(() => {
      mockUseMutation.mockReturnValueOnce(mockAddMutation as any);
      mockUseMutation.mockReturnValueOnce(mockDeleteMutation as any);
    });

    it("should handle successful key addition", async () => {
      const user = userEvent.setup();
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");
      const addButton = screen.getByTestId("add-key-button");

      await user.type(input, "test-api-key");
      await user.click(addButton);

      // Simulate successful mutation with act
      const onSuccess = mockUseMutation.mock.calls[0][0].onSuccess;
      if (onSuccess) {
        await act(async () => {
          onSuccess({}, { provider: Provider.OPENAI, key: "test-api-key" }, {});
        });
      }

      await waitFor(() => {
        expect(mockNotifications.show).toHaveBeenCalledWith(
          expect.objectContaining({
            message: "API key added successfully.",
            color: "transparent",
          }),
        );
      });

      expect(mockInvalidateQueries).toHaveBeenCalledWith({
        queryKey: ["user-api-keys"],
      });

      expect(mockRefreshModels).toHaveBeenCalled();
      expect(mockOnChange).toHaveBeenCalled();
    });

    it("should handle successful key deletion", async () => {
      const user = userEvent.setup();
      renderComponent(mockKeyDataWithKey);

      const deleteButton = screen.getByTestId("delete-key-button");

      await user.click(deleteButton);

      // Simulate successful mutation with act
      const onSuccess = mockUseMutation.mock.calls[1][0].onSuccess;
      if (onSuccess) {
        await act(async () => {
          onSuccess({}, { provider: Provider.OPENAI }, {});
        });
      }

      await waitFor(() => {
        expect(mockNotifications.show).toHaveBeenCalledWith(
          expect.objectContaining({
            message: "API key deleted successfully.",
            color: "transparent",
          }),
        );
      });

      expect(mockInvalidateQueries).toHaveBeenCalledWith({
        queryKey: ["user-api-keys"],
      });

      expect(mockRefreshModels).toHaveBeenCalled();
      expect(mockOnChange).toHaveBeenCalled();
    });
  });

  describe("Input Field Behavior", () => {
    it("should update input value when typing", async () => {
      const user = userEvent.setup();
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");

      await user.type(input, "test-key");

      expect(input).toHaveValue("test-key");
    });

    it("should be disabled when key is present", () => {
      renderComponent(mockKeyDataWithKey);

      const input = screen.getByTestId("api-key-input-field");
      expect(input).toBeDisabled();
    });

    it("should be enabled when key is not present", () => {
      renderComponent();

      const input = screen.getByTestId("api-key-input-field");
      expect(input).not.toBeDisabled();
    });
  });

  describe("Different Provider Types", () => {
    it("should handle different provider labels correctly", () => {
      const anthropicKeyData = {
        ...mockKeyData,
        label: "Anthropic Claude",
        provider: Provider.ANTHROPIC,
        apiKey: "anthropicKeyPresent" as const,
        isKeyPresent: true,
        link: "https://console.anthropic.com/settings/keys",
      };

      renderComponent(anthropicKeyData);

      expect(screen.getByText("Anthropic Claude")).toBeInTheDocument();
      expect(screen.getByTestId("ANTHROPIC-key-container")).toBeInTheDocument();

      const input = screen.getByTestId("api-key-input-field");
      expect(input).toHaveValue("anthropic-claude-****");
    });

    it("should handle provider with spaces in label", () => {
      const googleKeyData = {
        ...mockKeyData,
        label: "Google Gemini",
        provider: Provider.GOOGLE,
        apiKey: "googleKeyPresent" as const,
        isKeyPresent: true,
        link: "https://aistudio.google.com/app/apikey",
      };

      renderComponent(googleKeyData);

      const input = screen.getByTestId("api-key-input-field");
      expect(input).toHaveValue("google-gemini-****");
    });
  });
});
