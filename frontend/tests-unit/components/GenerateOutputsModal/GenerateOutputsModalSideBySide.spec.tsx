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

import GenerateOutputsModalSideBySide from "@/components/GenerateOutputsModal/GenerateOutputsModalSideBySide";
import { Model } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { GenerateOutputsSxSMode, GenerateOutputsType, Provider } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(() => ({
    defaultProjectId: "default-project-id",
  })),
}));

const mockGenerateOutputsSxS = jest.fn().mockResolvedValue({ success: true });
const mockGenerateOutputsSxSAll = jest
  .fn()
  .mockResolvedValue({ success: true });

jest.mock("@/queries/clientQueries", () => ({
  generateOutputsSxSQuery: jest.fn((projectId, data) =>
    mockGenerateOutputsSxS(projectId, data),
  ),
  generateOutputsSxSAllQuery: jest.fn((projectId, data) =>
    mockGenerateOutputsSxSAll(projectId, data),
  ),
}));

jest.mock("@tanstack/react-query", () => ({
  ...jest.requireActual("@tanstack/react-query"),
  useMutation: jest.fn().mockImplementation(({ mutationFn }) => {
    return {
      mutate: mutationFn,
      isPending: false,
    };
  }),
}));

const MockIcon = ({ size }: { size?: number }) => (
  <div data-testid="model-icon" data-size={size}>
    MockIcon
  </div>
);

const models: Model[] = [
  {
    id: "model-1",
    label: "Model One",
    icon: MockIcon,
    provider: Provider.OPENAI,
    properties: {
      max_tokens: undefined,
      n: undefined,
      temperature: undefined,
      top_p: undefined,
      top_k: undefined,
      seed: undefined,
    },
    name: "OPENAI",
    version: "",
    url: "",
    tag: "",
    model_type: "",
    additional_headers: {},
  },
  {
    id: "model-2",
    label: "Model Two",
    icon: MockIcon,
    provider: Provider.OPENAI,
    properties: {
      max_tokens: undefined,
      n: undefined,
      temperature: undefined,
      top_p: undefined,
      top_k: undefined,
      seed: undefined,
    },
    name: "",
    version: "",
    url: "",
    tag: "",
    model_type: "",
    additional_headers: {},
  },
];

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(() => ({
    allModels: models,
  })),
}));

// Create a mock for the ModelCard component
jest.mock(
  "@/components/GenerateOutputsModal/GenerateOutputsModalSideBySideModelCard",
  () => {
    return {
      __esModule: true,
      default: ({
        model,
        label,
        onModelSelect,
      }: {
        model: Model | Record<string, never>;
        label: string;
        onModelSelect: (model: Model) => void;
      }) => {
        return (
          <div data-testid={`model-card-${label}`}>
            <div>{label}</div>
            <button
              onClick={() => {
                const selectedModel = label === "A" ? models[0] : models[1];
                onModelSelect(selectedModel);
              }}
              data-testid={`select-model-${label}`}
            >
              Select Model {label}
            </button>
            <div data-testid={`model-status-${label}`}>
              {model?.id ? `Selected: ${model.label}` : "No model selected"}
            </div>
          </div>
        );
      },
    };
  },
);

describe("GenerateOutputsModalSideBySide", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const baseProps = {
    isOpened: true,
    onClose: jest.fn(),
    setRowSelection: jest.fn(),
    setActiveRows: jest.fn(),
    chatTurnIds: ["1", "2"],
    onGenerateOutputsSuccess: jest.fn(),
    onGenerateOutputsStart: jest.fn(),
    sxsPairIds: ["pair-1"],
    type: GenerateOutputsType.SELECTED_ROWS,
    projectId: "test-project-id",
    selectAllRowsInProject: false,
    isThereChatWithModel: true,
    isThereChatWithoutModel: false,
  };

  it("renders modal with correct title and content", () => {
    testRender(<GenerateOutputsModalSideBySide {...baseProps} />);

    expect(
      screen.getByText("Choose the model(s) you want to run on your input on."),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Select models to generate output"),
    ).toBeInTheDocument();
    expect(screen.getByTestId("model-card-A")).toBeInTheDocument();
    expect(screen.getByTestId("model-card-B")).toBeInTheDocument();
  });

  it("allows model selection and updates the UI", () => {
    testRender(<GenerateOutputsModalSideBySide {...baseProps} />);

    const selectModelButtonA = screen.getByTestId("select-model-A");
    fireEvent.click(selectModelButtonA);

    expect(screen.getByTestId("model-status-A")).toHaveTextContent(/Selected:/);
  });

  it("disables generate button when no models selected", () => {
    testRender(
      <GenerateOutputsModalSideBySide
        {...baseProps}
        isThereChatWithModel={false}
      />,
    );

    const button = screen.getByRole("button", { name: /generate outputs/i });
    expect(button).toBeDisabled();
  });

  it("generates outputs when button is clicked", () => {
    testRender(<GenerateOutputsModalSideBySide {...baseProps} />);

    const selectModelButtonA = screen.getByTestId("select-model-A");
    fireEvent.click(selectModelButtonA);

    const generateButton = screen.getByRole("button", {
      name: /generate outputs/i,
    });
    fireEvent.click(generateButton);

    expect(baseProps.onGenerateOutputsStart).toHaveBeenCalled();
    expect(baseProps.onClose).toHaveBeenCalled();
    expect(mockGenerateOutputsSxS).toHaveBeenCalledWith("test-project-id", {
      sxsPairIds: ["pair-1"],
      modelA: "model-1",
      modelB: null,
      mode: GenerateOutputsSxSMode.AUTORESOLVE,
    });
  });

  it("calls generateOutputsSxSAllQuery when type is ALL", () => {
    testRender(
      <GenerateOutputsModalSideBySide
        {...baseProps}
        type={GenerateOutputsType.ALL}
      />,
    );

    const selectModelButtonA = screen.getByTestId("select-model-A");
    const selectModelButtonB = screen.getByTestId("select-model-B");
    fireEvent.click(selectModelButtonA);
    fireEvent.click(selectModelButtonB);

    const generateButton = screen.getByRole("button", {
      name: /generate outputs/i,
    });
    fireEvent.click(generateButton);

    expect(mockGenerateOutputsSxSAll).toHaveBeenCalledWith("test-project-id", {
      modelA: "model-1",
      modelB: "model-2",
      mode: GenerateOutputsSxSMode.AUTORESOLVE,
    });
  });

  it("resets model selections when clicking reset", () => {
    testRender(<GenerateOutputsModalSideBySide {...baseProps} />);

    const selectModelButtonA = screen.getByTestId("select-model-A");
    fireEvent.click(selectModelButtonA);

    expect(screen.getByTestId("model-status-A")).toHaveTextContent(/Selected:/);

    const resetButton = screen.getByText("Reset");
    fireEvent.click(resetButton);

    expect(screen.getAllByText("No model selected")).toHaveLength(2);
  });

  it("closes the modal and resets selections when calling onClose", () => {
    testRender(<GenerateOutputsModalSideBySide {...baseProps} />);

    const closeButton = screen.getByRole("button", { name: "" });
    fireEvent.click(closeButton);

    expect(baseProps.onClose).toHaveBeenCalled();
    expect(baseProps.setRowSelection).toHaveBeenCalledWith({});
    expect(baseProps.setActiveRows).toHaveBeenCalledWith([]);
  });
});
