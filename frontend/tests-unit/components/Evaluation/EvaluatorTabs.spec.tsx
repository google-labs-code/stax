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

import EvaluatorTabs from "@/components/Evaluation/EvaluatorTabs";
import { deleteLLMEvaluatorQuery } from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import {
  EvaluatorTab,
  EvaluatorType,
  LLMEvaluatorItem,
  ProjectType,
  ScorerCardsProps,
} from "@/types";
import { act, fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@/components/Evaluation/EvaluatorSearchBox", () => {
  return {
    __esModule: true,
    default: () => <div data-testid="mocked-search-box">Search Box</div>,
  };
});

jest.mock("@/components/Evaluation/ExportEvaluatorsButton", () => {
  return {
    __esModule: true,
    default: () => <div data-testid="export-button">Export</div>,
  };
});

jest.mock("@/components/Evaluation/EvaluationCard", () => {
  return {
    __esModule: true,
    default: ({
      card,
      onClick,
      openDeleteModal,
      isCardChecked,
    }: {
      card: any;
      onClick?: () => void;
      openDeleteModal?: () => void;
      isCardChecked?: boolean;
    }) => (
      <div data-testid={`card-${card.id}`} onClick={onClick}>
        <div>{card.title}</div>
        <div>{card.description}</div>
        {isCardChecked && <input type="checkbox" checked readOnly />}
        <button data-testid={`delete-btn-${card.id}`} onClick={openDeleteModal}>
          Delete
        </button>
      </div>
    ),
  };
});

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn() }),
}));

jest.mock("@/queries/clientQueries", () => ({
  deleteLLMEvaluatorQuery: jest.fn().mockResolvedValue({}),
}));

jest.mock("@/components/DeleteModal", () => {
  return {
    __esModule: true,
    default: ({
      isOpen,
      onConfirm,
      onClose,
    }: {
      isOpen: boolean;
      onConfirm: () => void;
      onClose: () => void;
    }) =>
      isOpen ? (
        <div data-testid="delete-modal">
          <div>Delete Evaluator</div>
          <div>
            Are you sure you want to delete this evaluator? This cannot be
            undone.
          </div>
          <button onClick={onConfirm} data-testid="confirm-delete">
            Delete
          </button>
          <button onClick={onClose}>Cancel</button>
        </div>
      ) : null,
  };
});

const mockData: LLMEvaluatorItem[] = [
  {
    id: "1",
    title: "System Eval",
    type: EvaluatorType.SYSTEM,
    description: "System evaluator description",
    evaluationTypes: [ProjectType.POINTWISE],
  },
  {
    id: "2",
    title: "User Eval",
    type: EvaluatorType.USER,
    description: "User evaluator description",
    evaluationTypes: [ProjectType.POINTWISE],
  },
] as any;

describe("EvaluatorTabs", () => {
  const setData = jest.fn();
  const setSelectedCards = jest.fn();
  const toggleCardSelection = jest.fn();
  const setActiveTab = jest.fn();
  const defaultProps = {
    data: mockData,
    setData,
    isPage: true,
    setSelectedCards,
    selectedCards: [] as ScorerCardsProps[],
    toggleCardSelection,
    isLoading: false,
    activeTab: EvaluatorTab.DEFAULT,
    setActiveTab,
    initialData: mockData,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders tabs correctly", () => {
    testRender(<EvaluatorTabs {...defaultProps} />);
    expect(screen.getByText("Default")).toBeInTheDocument();
    expect(screen.getByText("My Evaluators")).toBeInTheDocument();
  });

  it("renders system evaluators in default tab", () => {
    testRender(<EvaluatorTabs {...defaultProps} />);
    expect(screen.getByText("System Eval")).toBeInTheDocument();
  });

  it("shows empty state when no user evaluators", () => {
    testRender(
      <EvaluatorTabs
        {...defaultProps}
        data={mockData.filter((d) => d.type === EvaluatorType.SYSTEM)}
        activeTab={EvaluatorTab.MY_EVALUATORS}
      />,
    );
    expect(
      screen.getByText("Create your first custom LLM evaluator"),
    ).toBeInTheDocument();
  });

  it("calls setActiveTab on tab change", () => {
    testRender(<EvaluatorTabs {...defaultProps} />);
    fireEvent.click(screen.getByText("My Evaluators"));
    expect(setActiveTab).toHaveBeenCalledWith(EvaluatorTab.MY_EVALUATORS);
  });

  it("renders non-page mode with empty user evaluators", () => {
    testRender(
      <EvaluatorTabs
        {...defaultProps}
        isPage={false}
        data={mockData.filter((d) => d.type === EvaluatorType.SYSTEM)}
        activeTab={EvaluatorTab.MY_EVALUATORS}
      />,
    );

    expect(
      screen.getByText("You haven't created any evaluators yet."),
    ).toBeInTheDocument();
    expect(screen.getByText("Evaluator Gallery")).toBeInTheDocument();
  });

  it("renders user evaluators in my evaluators tab", () => {
    testRender(
      <EvaluatorTabs
        {...defaultProps}
        activeTab={EvaluatorTab.MY_EVALUATORS}
      />,
    );

    expect(screen.getByText("User Eval")).toBeInTheDocument();
  });

  it("handles delete modal opening and closing", async () => {
    testRender(
      <EvaluatorTabs
        {...defaultProps}
        activeTab={EvaluatorTab.MY_EVALUATORS}
      />,
    );

    const deleteButton = screen.getByTestId("delete-btn-2");
    await act(async () => {
      fireEvent.click(deleteButton);
    });

    const deleteModal = screen.getByTestId("delete-modal");
    expect(deleteModal).toBeInTheDocument();

    const confirmButton = screen.getByTestId("confirm-delete");

    await act(async () => {
      fireEvent.click(confirmButton);
    });

    await waitFor(() => {
      expect(deleteLLMEvaluatorQuery).toHaveBeenCalled();
    });

    expect(setData).toHaveBeenCalled();
  });

  it("renders loading state", () => {
    testRender(<EvaluatorTabs {...defaultProps} isLoading={true} />);

    const loader = document.querySelector(".mantine-Loader-root");
    expect(loader).toBeTruthy();
  });

  it("renders search box in non-page mode", () => {
    testRender(<EvaluatorTabs {...defaultProps} isPage={false} />);

    // Look for our mocked search box
    expect(screen.getByTestId("mocked-search-box")).toBeInTheDocument();
  });

  it("renders export button in page mode", () => {
    testRender(<EvaluatorTabs {...defaultProps} />);

    expect(screen.getByTestId("export-button")).toBeInTheDocument();
  });

  it("handles card selection in non-page mode", () => {
    testRender(<EvaluatorTabs {...defaultProps} isPage={false} />);

    fireEvent.click(screen.getByTestId("card-1"));

    expect(toggleCardSelection).toHaveBeenCalled();
  });

  it("shows checked cards correctly", () => {
    testRender(
      <EvaluatorTabs
        {...defaultProps}
        selectedCards={[{ id: "1", title: "System Eval" }]}
      />,
    );

    const checkbox = screen.getByRole("checkbox");
    expect(checkbox).toBeInTheDocument();
    expect(checkbox).toBeChecked();
  });
});
