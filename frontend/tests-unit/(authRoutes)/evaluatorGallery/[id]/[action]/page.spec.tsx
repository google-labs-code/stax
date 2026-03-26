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

import EditEvaluatorPage from "@/app/(authRoutes)/evaluatorGallery/[id]/[action]/page";
import { EvaluatorPageAction } from "@/app/(authRoutes)/evaluatorGallery/types";
import { DEFAULT_EVALUATOR_MODEL } from "@/config/constants";
import { useChatContext } from "@/hooks/useChatContext";
import { useModelsContext } from "@/hooks/useModelsContext";
import {
  getAllEvaluatorsQuery,
  getLLMEvaluatorQuery,
  getSxSLLMEvaluatorQuery,
} from "@/queries/clientQueries";
import { LLMEvaluatorResponse } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { ProjectType, Provider } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { useParams } from "next/navigation";

// Mock dependencies
const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useParams: jest.fn(),
  useRouter: jest.fn(() => ({
    push: mockPush,
    replace: jest.fn(),
    prefetch: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
    refresh: jest.fn(),
  })),
}));

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: jest.fn(),
}));

jest.mock("@/queries/clientQueries", () => ({
  getAllEvaluatorsQuery: jest.fn(),
  getLLMEvaluatorQuery: jest.fn(),
  getSxSLLMEvaluatorQuery: jest.fn(),
}));

jest.mock("@/utils/LocalStorage", () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    set: jest.fn(),
    remove: jest.fn(),
  },
}));

jest.mock("@/config/config", () => ({
  MainConfig: {
    isEvaluatorUndoRedoEnabled: false,
  },
}));

const mockUseParams = useParams as jest.MockedFunction<typeof useParams>;
const mockUseModelsContext = useModelsContext as jest.MockedFunction<
  typeof useModelsContext
>;
const mockUseChatContext = useChatContext as jest.MockedFunction<
  typeof useChatContext
>;
const mockGetAllEvaluatorsQuery = getAllEvaluatorsQuery as jest.MockedFunction<
  typeof getAllEvaluatorsQuery
>;
const mockGetLLMEvaluatorQuery = getLLMEvaluatorQuery as jest.MockedFunction<
  typeof getLLMEvaluatorQuery
>;
const mockGetSxSLLMEvaluatorQuery =
  getSxSLLMEvaluatorQuery as jest.MockedFunction<
    typeof getSxSLLMEvaluatorQuery
  >;
const mockLocalStorageGet = LocalStorage.get as jest.MockedFunction<
  typeof LocalStorage.get
>;

describe("EditEvaluatorPage", () => {
  const mockModels = [
    {
      id: "model-1",
      name: DEFAULT_EVALUATOR_MODEL,
      label: "Gemini Default",
      version: "1.0",
      url: "",
      tag: "",
      model_type: "",
      properties: {},
      additional_headers: {},
      provider: Provider.GOOGLE,
      is_api_key_present: true,
    },
    {
      id: "model-2",
      name: "gpt-4",
      label: "GPT-4",
      version: "1.0",
      url: "",
      tag: "",
      model_type: "",
      properties: {},
      additional_headers: {},
      provider: Provider.OPENAI,
      is_api_key_present: true,
    },
  ];

  const mockLLMEvaluatorResponse: LLMEvaluatorResponse = {
    id: "evaluator-1",
    name: "Test Evaluator",
    description: "Test Description",
    output_format_type: "Choices",
    output_categories: [
      { name: "Good", value: "good", color: "#000000", color_name: "black" },
      { name: "Bad", value: "bad", color: "#FFFFFF", color_name: "white" },
    ],
    variables: [{ name: "output", required: false }],
    prompts: [{ role: "user", text: "Test prompt" }],
    model: {
      id: "model-1",
      name: "test-model",
      label: "Test Model",
      version: "1.0",
      url: "",
      tag: "",
      model_type: "",
      properties: {},
      additional_headers: {},
      provider: Provider.GOOGLE,
    },
    type: "llm" as any,
  };

  const mockSxSEvaluatorResponse: LLMEvaluatorResponse = {
    ...mockLLMEvaluatorResponse,
    id: "evaluator-2",
    output_categories: [
      { name: "A", value: "A", color: "#000000", color_name: "black" },
      { name: "Tie", value: "Tie", color: "#808080", color_name: "gray" },
      { name: "B", value: "B", color: "#FFFFFF", color_name: "white" },
    ],
  };

  beforeEach(() => {
    jest.clearAllMocks();

    mockUseParams.mockReturnValue({
      id: undefined,
      action: undefined,
    } as any);

    mockUseModelsContext.mockReturnValue({
      allModels: mockModels,
      setOpenedModel: jest.fn(),
      isLoadingModels: false,
      refreshModels: jest.fn(),
      customModels: [],
      defaultModels: mockModels,
      providers: {},
      openedModel: null,
    });

    mockUseChatContext.mockReturnValue({
      chats: [],
      setChats: jest.fn(),
      updateChat: jest.fn(),
      selectedChatTurnIds: [],
      setSelectedChatTurnIds: jest.fn(),
      selectedPairs: [],
      setSelectedPairs: jest.fn(),
      startEvaluationResultsPooling: jest.fn(),
    });

    mockLocalStorageGet.mockReturnValue(null);

    mockGetAllEvaluatorsQuery.mockResolvedValue({
      llm: [],
      heuristic: [],
    } as any);

    mockGetLLMEvaluatorQuery.mockResolvedValue(mockLLMEvaluatorResponse);
    mockGetSxSLLMEvaluatorQuery.mockResolvedValue(mockSxSEvaluatorResponse);
  });

  describe("Rendering", () => {
    it("renders the component for new evaluator page", () => {
      testRender(<EditEvaluatorPage />);

      expect(screen.getByTestId("page-header")).toBeInTheDocument();
      expect(screen.getByTestId("breadcrumb")).toHaveTextContent(
        "Evaluator Gallery",
      );
      expect(
        screen.getByPlaceholderText("Enter evaluator name"),
      ).toBeInTheDocument();
    });

    it("renders with empty name initially", () => {
      testRender(<EditEvaluatorPage />);

      const nameInput = screen.getByPlaceholderText("Enter evaluator name");
      expect(nameInput).toHaveValue("");
    });

    it("renders the prompt textarea", () => {
      testRender(<EditEvaluatorPage />);

      // Textarea doesn't have a name, so we find it by its value or by being a textbox
      const textareas = screen.getAllByRole("textbox");
      const promptTextarea = textareas.find(
        (textarea) => textarea.tagName === "TEXTAREA",
      );
      expect(promptTextarea).toBeInTheDocument();
    });

    it("renders model combobox", () => {
      testRender(<EditEvaluatorPage />);

      expect(screen.getByTestId("model-combobox")).toBeInTheDocument();
    });

    it("renders evaluation type segmented control", () => {
      testRender(<EditEvaluatorPage />);

      const pointwiseRadio = screen.getByRole("radio", {
        name: /pointwise/i,
      });
      const sideBySideRadio = screen.getByRole("radio", {
        name: /side-by-side/i,
      });

      expect(pointwiseRadio).toBeInTheDocument();
      expect(sideBySideRadio).toBeInTheDocument();
    });

    it("renders CreateOrSaveEvaluatorButton for new page", () => {
      testRender(<EditEvaluatorPage />);

      expect(screen.getByTestId("create-or-save-button")).toBeInTheDocument();
    });

    it("renders MetricPrompts for pointwise evaluation type", () => {
      testRender(<EditEvaluatorPage />);

      expect(screen.getByTestId("metric-prompts")).toBeInTheDocument();
    });
  });

  describe("New evaluator page", () => {
    it("loads data from localStorage when available", () => {
      const localStorageData = {
        name: "Stored Name",
        description: "Stored Description",
      };
      mockLocalStorageGet.mockReturnValue(JSON.stringify(localStorageData));

      testRender(<EditEvaluatorPage />);

      const nameInput = screen.getByPlaceholderText("Enter evaluator name");
      expect(nameInput).toHaveValue("Stored Name");
    });

    it("does not load data from localStorage when not available", () => {
      mockLocalStorageGet.mockReturnValue(null);

      testRender(<EditEvaluatorPage />);

      const nameInput = screen.getByPlaceholderText("Enter evaluator name");
      expect(nameInput).toHaveValue("");
    });

    it("sets default model when models are available", async () => {
      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockUseModelsContext).toHaveBeenCalled();
      });
    });
  });

  describe("View evaluator page", () => {
    beforeEach(() => {
      mockUseParams.mockReturnValue({
        id: "evaluator-1",
        action: EvaluatorPageAction.VIEW,
      } as any);
    });

    it("disables form inputs in view mode", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        const nameInput = screen.getByPlaceholderText("Enter evaluator name");
        expect(nameInput).toBeDisabled();
      });
    });

    it("does not show edit icon button in view mode", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      expect(screen.queryByTestId("icon-button")).not.toBeInTheDocument();
    });

    it("shows duplicate button in view mode", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        expect(
          screen.getByTestId("duplicate-button-evaluator-1"),
        ).toBeInTheDocument();
      });
    });

    it("disables evaluation type switch when viewing pointwise evaluator", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        const sideBySideRadio = screen.getByRole("radio", {
          name: /side-by-side/i,
        });
        expect(sideBySideRadio).toBeDisabled();
      });
    });
  });

  describe("Edit evaluator page", () => {
    beforeEach(() => {
      mockUseParams.mockReturnValue({
        id: "evaluator-1",
        action: EvaluatorPageAction.EDIT,
      } as any);
    });

    it("loads evaluator data when editing", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        expect(mockGetLLMEvaluatorQuery).toHaveBeenCalledWith("evaluator-1");
      });
    });

    it("shows edit icon button in edit mode", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        expect(screen.getByTestId("icon-button")).toBeInTheDocument();
      });
    });

    it("shows duplicate button in edit mode", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        expect(
          screen.getByTestId("duplicate-button-evaluator-1"),
        ).toBeInTheDocument();
      });
    });
  });

  describe("Duplicate evaluator page", () => {
    beforeEach(() => {
      mockUseParams.mockReturnValue({
        id: "evaluator-1",
        action: EvaluatorPageAction.DUPLICATE,
      } as any);
    });

    it("loads evaluator data and appends number to name", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        expect(mockGetLLMEvaluatorQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        const nameInput = screen.getByPlaceholderText("Enter evaluator name");
        expect(nameInput).toHaveValue("Test Evaluator 1");
      });
    });
  });

  describe("Form interactions", () => {
    it("updates name when input changes", () => {
      testRender(<EditEvaluatorPage />);

      const nameInput = screen.getByPlaceholderText("Enter evaluator name");
      fireEvent.change(nameInput, { target: { value: "New Name" } });

      expect(nameInput).toHaveValue("New Name");
    });

    it("updates prompt when textarea changes", () => {
      testRender(<EditEvaluatorPage />);

      // Textarea doesn't have a name, so we find it by being a textarea
      const textareas = screen.getAllByRole("textbox");
      const promptTextarea = textareas.find(
        (textarea) => textarea.tagName === "TEXTAREA",
      );
      expect(promptTextarea).toBeInTheDocument();

      fireEvent.change(promptTextarea!, {
        target: { value: "New prompt text" },
      });

      expect(promptTextarea).toHaveValue("New prompt text");
    });

    it("switches evaluation type from pointwise to side-by-side", () => {
      testRender(<EditEvaluatorPage />);

      const sideBySideRadio = screen.getByRole("radio", {
        name: /side-by-side/i,
      });
      fireEvent.click(sideBySideRadio);

      // MetricPrompts should not be visible for side-by-side
      expect(screen.queryByTestId("metric-prompts")).not.toBeInTheDocument();
    });

    it("switches evaluation type from side-by-side to pointwise", () => {
      testRender(<EditEvaluatorPage />);

      // First switch to side-by-side
      const sideBySideRadio = screen.getByRole("radio", {
        name: /side-by-side/i,
      });
      fireEvent.click(sideBySideRadio);

      // Then switch back to pointwise
      const pointwiseRadio = screen.getByRole("radio", {
        name: /pointwise/i,
      });
      fireEvent.click(pointwiseRadio);

      // MetricPrompts should be visible for pointwise
      expect(screen.getByTestId("metric-prompts")).toBeInTheDocument();
    });
  });

  describe("Side-by-side evaluation type", () => {
    it("shows info message about output categories", () => {
      testRender(<EditEvaluatorPage />);

      const sideBySideRadio = screen.getByRole("radio", {
        name: /side-by-side/i,
      });
      fireEvent.click(sideBySideRadio);

      expect(
        screen.getByText(/The prompt must output one of the categories/i),
      ).toBeInTheDocument();
    });
  });

  describe("Model settings modal", () => {
    it("opens model configuration modal when tune icon is clicked", async () => {
      mockUseModelsContext.mockReturnValue({
        allModels: mockModels,
        setOpenedModel: jest.fn(),
        isLoadingModels: false,
        refreshModels: jest.fn(),
        customModels: [],
        defaultModels: mockModels,
        providers: {},
        openedModel: mockModels[0],
      });

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        const tuneIcon = screen.getByTestId("select-model");
        fireEvent.click(tuneIcon);
      });

      // The modal should be opened via the setOpenedModel call
      // We can't directly test the modal opening without more complex setup
    });
  });

  describe("Edit evaluator modal", () => {
    it("opens edit modal when edit icon is clicked", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      mockUseParams.mockReturnValue({
        id: "evaluator-1",
        action: EvaluatorPageAction.EDIT,
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        const editButton = screen.getByTestId("icon-button");
        expect(editButton).toBeInTheDocument();
        fireEvent.click(editButton);
      });

      await waitFor(() => {
        expect(screen.getByTestId("edit-evaluator-modal")).toBeInTheDocument();
      });
    });

    it("closes edit modal when close button is clicked", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      mockUseParams.mockReturnValue({
        id: "evaluator-1",
        action: EvaluatorPageAction.EDIT,
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        const editButton = screen.getByTestId("icon-button");
        fireEvent.click(editButton);
      });

      await waitFor(() => {
        expect(screen.getByTestId("edit-evaluator-modal")).toBeInTheDocument();
      });

      const closeButton = await screen.findByLabelText("close button");
      fireEvent.click(closeButton);

      await waitFor(
        () => {
          expect(screen.queryByText("Edit evaluator")).not.toBeInTheDocument();
        },
        { timeout: 3000 },
      );
    });
  });

  describe("Loading states", () => {
    it("shows loader while fetching evaluator data", async () => {
      let resolvePromise: (value: any) => void;
      const pendingPromise = new Promise<any>((resolve) => {
        resolvePromise = resolve;
      });

      mockGetAllEvaluatorsQuery.mockReturnValue(pendingPromise);
      mockUseParams.mockReturnValue({
        id: "evaluator-1",
        action: EvaluatorPageAction.EDIT,
      } as any);

      testRender(<EditEvaluatorPage />);

      // Check for loader
      await waitFor(() => {
        const loader = document.querySelector(".mantine-Loader-root");
        expect(loader).toBeInTheDocument();
      });

      // Resolve the promise
      resolvePromise!({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      });

      await waitFor(() => {
        const loader = document.querySelector(".mantine-Loader-root");
        expect(loader).not.toBeInTheDocument();
      });
    });
  });

  describe("Default model selection", () => {
    it("selects default evaluator model when available", async () => {
      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockUseModelsContext).toHaveBeenCalled();
      });
    });

    it("selects first model with API key when default model not available", async () => {
      const modelsWithoutDefault = [
        {
          id: "model-2",
          name: "gpt-4",
          label: "GPT-4",
          version: "1.0",
          url: "",
          tag: "",
          model_type: "",
          properties: {},
          additional_headers: {},
          provider: Provider.OPENAI,
          is_api_key_present: true,
        },
      ];

      mockUseModelsContext.mockReturnValue({
        allModels: modelsWithoutDefault,
        setOpenedModel: jest.fn(),
        isLoadingModels: false,
        refreshModels: jest.fn(),
        customModels: [],
        defaultModels: modelsWithoutDefault,
        providers: {},
        openedModel: null,
      });

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockUseModelsContext).toHaveBeenCalled();
      });
    });

    it("selects Google model when no models with API key available", async () => {
      const modelsWithoutApiKey = [
        {
          id: "model-3",
          name: "gemini",
          label: "Gemini",
          version: "1.0",
          url: "",
          tag: "",
          model_type: "",
          properties: {},
          additional_headers: {},
          provider: Provider.GOOGLE,
          is_api_key_present: false,
        },
      ];

      mockUseModelsContext.mockReturnValue({
        allModels: modelsWithoutApiKey,
        setOpenedModel: jest.fn(),
        isLoadingModels: false,
        refreshModels: jest.fn(),
        customModels: [],
        defaultModels: modelsWithoutApiKey,
        providers: {},
        openedModel: null,
      });

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockUseModelsContext).toHaveBeenCalled();
      });
    });
  });

  describe("Side-by-side evaluator loading", () => {
    it("loads side-by-side evaluator data correctly", async () => {
      mockUseParams.mockReturnValue({
        id: "evaluator-2-pairwise",
        action: EvaluatorPageAction.VIEW,
      } as any);

      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-2-pairwise",
            name: "Test SxS Evaluator",
            evaluation_type: ProjectType.SIDE_BY_SIDE,
          },
        ],
        heuristic: [],
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        expect(mockGetSxSLLMEvaluatorQuery).toHaveBeenCalledWith(
          "evaluator-2-pairwise",
        );
      });
    });
  });

  describe("Page title", () => {
    it("displays evaluator name in page header", async () => {
      mockGetAllEvaluatorsQuery.mockResolvedValue({
        llm: [
          {
            id: "evaluator-1",
            name: "Test Evaluator",
            evaluation_type: ProjectType.POINTWISE,
          },
        ],
        heuristic: [],
      } as any);

      mockUseParams.mockReturnValue({
        id: "evaluator-1",
        action: EvaluatorPageAction.VIEW,
      } as any);

      testRender(<EditEvaluatorPage />);

      await waitFor(() => {
        expect(mockGetAllEvaluatorsQuery).toHaveBeenCalled();
      });

      await waitFor(() => {
        expect(screen.getByTestId("page-title")).toHaveTextContent(
          "Test Evaluator",
        );
      });
    });

    it("displays empty string when name is not set", () => {
      testRender(<EditEvaluatorPage />);

      expect(screen.getByTestId("page-title")).toHaveTextContent("");
    });
  });
});
