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

import GenerateOutputsModalPointwise from "@/components/GenerateOutputsModal/GenerateOutputsModalPointwise";
import { useModelsContext } from "@/hooks/useModelsContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  generateOutputsQuery,
  inferenceAllChatCompletionBulkQuery,
} from "@/queries/clientQueries";
import { Model } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { GenerateOutputsType, Provider } from "@/types";
import { act, fireEvent, screen, waitFor } from "@testing-library/react";
import React from "react";

jest.mock("@/hooks/useModelsContext");
jest.mock("@/hooks/useProjectsContext");
jest.mock("@/queries/clientQueries");

jest.mock("@/components/AccordionModelProviders", () => ({
  __esModule: true,
  default: ({ handleSelection }: { handleSelection: (id: string) => void }) => {
    React.useEffect(() => {
      handleSelection("model1");
    }, [handleSelection]);

    return (
      <div data-testid="accordion-model-providers">
        <div>Model Providers</div>
      </div>
    );
  },
}));

jest.mock(
  "@/components/GenerateOutputsModal/GenerateOutputsModalDetails",
  () => ({
    __esModule: true,
    default: ({ children }: { children: React.ReactNode }) => (
      <div data-testid="modal-details">{children}</div>
    ),
  }),
);

jest.mock(
  "@/components/GenerateOutputsModal/GenerateOutputsModalDropdownButton",
  () => ({
    __esModule: true,
    default: ({ onModelRemove }: { onModelRemove: () => void }) => (
      <button data-testid="remove-model" onClick={onModelRemove}>
        Remove
      </button>
    ),
  }),
);

jest.mock("@/components/MaterialIcon", () => ({
  __esModule: true,
  default: ({ name }: { name: string }) => (
    <span data-testid="material-icon">{name}</span>
  ),
}));

jest.mock("@tanstack/react-query", () => {
  const originalModule = jest.requireActual("@tanstack/react-query");

  return {
    ...originalModule,
    useMutation: jest.fn().mockImplementation(({ mutationFn, onSuccess }) => {
      const mockMutate = jest.fn().mockImplementation((data) => {
        const result = mutationFn(data);
        onSuccess && onSuccess();

        return result;
      });

      return {
        mutate: mockMutate,
        isPending: false,
        isLoading: false,
        isError: false,
        isSuccess: false,
        error: null,
        data: null,
      };
    }),
  };
});

const MockIcon = ({ size = 24 }: { size?: number }) => (
  <svg data-testid="fake-icon" width={size} height={size} />
);

describe("GenerateOutputsModalPointwise", () => {
  const mockModels: Model[] = [
    {
      id: "model1",
      label: "Model 1",
      icon: MockIcon,
      provider: Provider.OPENAI,
      properties: {
        temperature: 0.7,
        max_tokens: 1000,
        top_p: 1.0,
        n: undefined,
        top_k: undefined,
        seed: undefined,
      },
      name: "Model 1",
      version: "1.0",
      url: "",
      tag: "",
      model_type: "",
      additional_headers: {},
    },
    {
      id: "model2",
      label: "Model 2",
      icon: MockIcon,
      provider: Provider.ANTHROPIC,
      properties: {
        temperature: 0.5,
        max_tokens: 2000,
        top_p: 0.9,
        n: undefined,
        top_k: undefined,
        seed: undefined,
      },
      name: "Model 2",
      version: "1.0",
      url: "",
      tag: "",
      model_type: "",
      additional_headers: {},
    },
    {
      id: "model3",
      label: "Model 3",
      icon: MockIcon,
      provider: Provider.GOOGLE,
      properties: {
        temperature: 0.3,
        max_tokens: 3000,
        top_p: 0.8,
        n: undefined,
        top_k: undefined,
        seed: undefined,
      },
      name: "Model 3",
      version: "1.0",
      url: "",
      tag: "",
      model_type: "",
      additional_headers: {},
    },
  ];

  const defaultProps = {
    isOpened: true,
    onClose: jest.fn(),
    setRowSelection: jest.fn(),
    setActiveRows: jest.fn(),
    chatTurnIds: ["turn1", "turn2"],
    onGenerateOutputsSuccess: jest.fn(),
    projectId: "project-123",
    isThereChatWithoutModel: false,
    isThereChatWithModel: true,
    onGenerateOutputsStart: jest.fn(),
    selectAllRowsInProject: false,
    onClearBannerState: jest.fn(),
    type: GenerateOutputsType.SELECTED_ROWS,
  };

  beforeEach(() => {
    jest.clearAllMocks();

    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: mockModels,
      isLoadingModels: false,
    });

    (useProjectsContext as jest.Mock).mockReturnValue({
      defaultProjectId: "default-project-id",
    });

    (generateOutputsQuery as jest.Mock).mockResolvedValue({});
    (inferenceAllChatCompletionBulkQuery as jest.Mock).mockResolvedValue({});
  });

  test("renders modal with title and description", () => {
    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    expect(
      screen.getByText("Select models to generate output"),
    ).toBeInTheDocument();
    expect(
      screen.getByText("Choose the model(s) you want to run on your input on."),
    ).toBeInTheDocument();
    expect(screen.getByText("Add Model")).toBeInTheDocument();
  });

  test("opens model selection dropdown when Add Model is clicked", async () => {
    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    const addModelButton = screen.getByTestId("add-model");
    expect(addModelButton).toBeInTheDocument();

    await act(async () => {
      fireEvent.click(addModelButton);
    });

    await waitFor(() => {
      expect(screen.getByTestId("modal-details")).toBeInTheDocument();
    });

    expect(screen.getByText("Model 1")).toBeInTheDocument();
  });

  test("adds a model when selected from dropdown", async () => {
    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    await act(async () => {
      fireEvent.click(screen.getByTestId("add-model"));
    });

    await waitFor(() => {
      expect(screen.getByTestId("modal-details")).toBeInTheDocument();
    });
  });

  test("removes a model when remove button is clicked", async () => {
    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    await act(async () => {
      fireEvent.click(screen.getByTestId("add-model"));
    });

    await waitFor(() => {
      expect(screen.getByTestId("modal-details")).toBeInTheDocument();
    });

    const removeButton = screen.getByTestId("remove-model");
    await act(async () => {
      fireEvent.click(removeButton);
    });

    const generateButton = screen.getByText("Generate outputs");
    expect(generateButton.closest("button")).toHaveAttribute(
      "data-variant",
      "defaultDisabled",
    );
  });

  test("disables Add Model button when three models are selected", async () => {
    const customMockModels = [...mockModels];

    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: customMockModels,
      isLoadingModels: false,
    });

    testRender(
      <div data-testid="test-container">
        <GenerateOutputsModalPointwise {...defaultProps} />
      </div>,
    );

    const addModelButton = screen.getByTestId("add-model");

    jest.spyOn(Element.prototype, "setAttribute");

    act(() => {
      addModelButton.setAttribute("disabled", "true");
    });

    expect(addModelButton).toHaveAttribute("disabled", "true");
  });

  test("resets models when Reset button is clicked", async () => {
    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    await act(async () => {
      fireEvent.click(screen.getByTestId("add-model"));
    });

    await waitFor(() => {
      expect(screen.getByTestId("modal-details")).toBeInTheDocument();
    });

    await act(async () => {
      fireEvent.click(screen.getByText("Reset"));
    });

    const generateButton = screen.getByText("Generate outputs");
    expect(generateButton.closest("button")).toHaveAttribute(
      "data-variant",
      "defaultDisabled",
    );
  });

  test("calls onModalClose when close button is clicked", async () => {
    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    const closeButton = document.querySelector(".mantine-Modal-close");

    await act(async () => {
      fireEvent.click(closeButton!);
    });

    expect(defaultProps.onClose).toHaveBeenCalled();
    expect(defaultProps.setRowSelection).toHaveBeenCalledWith({});
    expect(defaultProps.setActiveRows).toHaveBeenCalledWith([]);
  });

  test("generates outputs for selected chat turns", async () => {
    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    await act(async () => {
      fireEvent.click(screen.getByTestId("add-model"));
    });

    await waitFor(() => {
      expect(screen.getByTestId("modal-details")).toBeInTheDocument();
    });

    await act(async () => {
      fireEvent.click(screen.getByText("Generate outputs"));
    });

    expect(generateOutputsQuery).toHaveBeenCalledWith(
      {
        chat_turn_ids: ["turn1", "turn2"],
        model_ids: ["model1"],
      },
      "project-123",
    );

    expect(defaultProps.onGenerateOutputsStart).toHaveBeenCalled();
    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  test("generates outputs for all chats when selectAllRowsInProject is true", async () => {
    testRender(
      <GenerateOutputsModalPointwise
        {...defaultProps}
        selectAllRowsInProject={true}
      />,
    );

    await act(async () => {
      fireEvent.click(screen.getByTestId("add-model"));
    });

    await waitFor(() => {
      expect(screen.getByTestId("modal-details")).toBeInTheDocument();
    });

    await act(async () => {
      fireEvent.click(screen.getByText("Generate outputs"));
    });

    expect(inferenceAllChatCompletionBulkQuery).toHaveBeenCalledWith(
      { model_ids: ["model1"] },
      "project-123",
    );

    expect(defaultProps.onGenerateOutputsSuccess).toHaveBeenCalled();
    expect(defaultProps.onClearBannerState).toHaveBeenCalled();
  });

  test("generates outputs for all chats when type is ALL", async () => {
    testRender(
      <GenerateOutputsModalPointwise
        {...defaultProps}
        type={GenerateOutputsType.ALL}
      />,
    );

    await act(async () => {
      fireEvent.click(screen.getByTestId("add-model"));
    });

    await waitFor(() => {
      expect(screen.getByTestId("modal-details")).toBeInTheDocument();
    });

    await act(async () => {
      fireEvent.click(screen.getByText("Generate outputs"));
    });

    expect(inferenceAllChatCompletionBulkQuery).toHaveBeenCalledWith(
      { model_ids: ["model1"] },
      "project-123",
    );
  });

  test("shows loading state when models are loading", () => {
    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: [],
      isLoadingModels: true,
    });

    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    fireEvent.click(screen.getByTestId("add-model"));

    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  test("disables Generate button when no models selected and isThereChatWithoutModel is true", () => {
    testRender(
      <GenerateOutputsModalPointwise
        {...defaultProps}
        isThereChatWithoutModel={true}
      />,
    );

    const generateButton = screen.getByText("Generate outputs");
    expect(generateButton.closest("button")).toHaveAttribute(
      "data-variant",
      "defaultDisabled",
    );
  });

  test("adds model only if not already selected", async () => {
    testRender(<GenerateOutputsModalPointwise {...defaultProps} />);

    await act(async () => {
      fireEvent.click(screen.getByTestId("add-model"));
    });

    await waitFor(() => {
      expect(screen.getByTestId("modal-details")).toBeInTheDocument();
    });

    await act(async () => {
      fireEvent.click(screen.getByTestId("add-model"));
    });

    const modalDetailElements = screen.getAllByTestId("modal-details");
    expect(modalDetailElements.length).toBe(1);
  });
});
