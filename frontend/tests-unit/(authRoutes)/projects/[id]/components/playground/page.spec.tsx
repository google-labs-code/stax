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

import ProjectPlayground from "@/app/(authRoutes)/projects/[id]/playground/page";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { routes } from "@/config/routes";
import { useChatContext } from "@/hooks/useChatContext";
import { useHumanEvalsContext } from "@/hooks/useHumanEvalsContext";
import { useModelsContext } from "@/hooks/useModelsContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { testRender } from "@/tests-unit/render";
import { ProjectType } from "@/types";
import { fireEvent, screen } from "@testing-library/react";
import { useRouter } from "next/navigation";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: jest.fn(),
}));

jest.mock("@/hooks/useHumanEvalsContext", () => ({
  useHumanEvalsContext: jest.fn(),
}));

const mockPush = jest.fn();
const mockResetPlayground = jest.fn();
const mockSetInputs = jest.fn();
const mockSetSystemInstructions = jest.fn();
const mockSetModels = jest.fn();
const mockSetExpandedInputCard = jest.fn();
const mockSetExpandedOutputCard = jest.fn();
const mockSetOnboardingIndex = jest.fn();
const mockSetIsOriginalEntryDeleted = jest.fn();
const mockClosePromptCleaningModalOpen = jest.fn();

beforeEach(() => {
  (useRouter as jest.Mock).mockReturnValue({ push: mockPush });

  (useProjectContext as jest.Mock).mockReturnValue({
    projectState: {
      project: {
        name: "Test Project",
        project_id: "123",
        type: ProjectType.POINTWISE,
      },
    },
  });

  (useChatContext as jest.Mock).mockImplementation(() => ({
    updateChat: () => {},
  }));

  (useHumanEvalsContext as jest.Mock).mockImplementation(() => ({
    feedbackEvalId: () => {},
  }));

  (useModelsContext as jest.Mock).mockImplementation(() => ({
    allModels: [],
    isLoadingModels: false,
  }));

  (usePlaygroundContext as jest.Mock).mockReturnValue({
    isPromptCleaningModalOpen: false,
    closePromptCleaningModalOpen: mockClosePromptCleaningModalOpen,
    inputs: [],
    setInputs: mockSetInputs,
    models: [],
    setModels: mockSetModels,
    systemInstructions: [],
    setSystemInstructions: mockSetSystemInstructions,
    expandedInputCard: null,
    setExpandedInputCard: mockSetExpandedInputCard,
    expandedOutputCard: null,
    setExpandedOutputCard: mockSetExpandedOutputCard,
    resetPlayground: mockResetPlayground,
    onboardingIndex: null,
    setOnboardingIndex: mockSetOnboardingIndex,
    setIsOriginalEntryDeleted: mockSetIsOriginalEntryDeleted,
  });
});

afterEach(() => {
  jest.clearAllMocks();
});

describe("ProjectPlayground", () => {
  it("renders InputCard and OutputCard for Pointwise project", () => {
    testRender(<ProjectPlayground />);
    expect(screen.getByText("PLAYGROUND")).toBeInTheDocument();
  });

  it("navigates to project page when close icon is clicked", () => {
    testRender(<ProjectPlayground />);
    const closeIcon = screen
      .getAllByTestId("material-icon")
      .find((el) => el.textContent === "close");
    fireEvent.click(closeIcon!);
    expect(mockPush).toHaveBeenCalledWith(`${routes.projects}/123`);
  });

  it("resets playground on unmount", () => {
    const { unmount } = testRender(<ProjectPlayground />);
    unmount();
    expect(mockResetPlayground).toHaveBeenCalled();
  });

  it("shows onboarding tooltip when onboardingIndex is 4", () => {
    (usePlaygroundContext as jest.Mock).mockReturnValueOnce({
      isPromptCleaningModalOpen: false,
      closePromptCleaningModalOpen: mockClosePromptCleaningModalOpen,
      inputs: [],
      setInputs: mockSetInputs,
      models: [],
      setModels: mockSetModels,
      systemInstructions: [],
      setSystemInstructions: mockSetSystemInstructions,
      expandedInputCard: null,
      setExpandedInputCard: mockSetExpandedInputCard,
      expandedOutputCard: null,
      setExpandedOutputCard: mockSetExpandedOutputCard,
      resetPlayground: mockResetPlayground,
      onboardingIndex: 4,
      setOnboardingIndex: mockSetOnboardingIndex,
      setIsOriginalEntryDeleted: mockSetIsOriginalEntryDeleted,
    });

    testRender(<ProjectPlayground />);
    expect(screen.getByText("PLAYGROUND")).toBeInTheDocument();
  });

  it("does not render second InputCard when projectType is not SIDE_BY_SIDE", () => {
    (useProjectContext as jest.Mock).mockReturnValueOnce({
      projectState: {
        project: {
          name: "Test Project",
          project_id: "123",
          type: ProjectType.POINTWISE,
        },
      },
    });
    (usePlaygroundContext as jest.Mock).mockReturnValueOnce({
      isPromptCleaningModalOpen: false,
      closePromptCleaningModalOpen: mockClosePromptCleaningModalOpen,
      inputs: [],
      setInputs: mockSetInputs,
      models: [],
      setModels: mockSetModels,
      systemInstructions: [],
      setSystemInstructions: mockSetSystemInstructions,
      expandedInputCard: null,
      setExpandedInputCard: mockSetExpandedInputCard,
      expandedOutputCard: null,
      setExpandedOutputCard: mockSetExpandedOutputCard,
      resetPlayground: mockResetPlayground,
      onboardingIndex: null,
      setOnboardingIndex: mockSetOnboardingIndex,
      setIsOriginalEntryDeleted: mockSetIsOriginalEntryDeleted,
    });

    testRender(<ProjectPlayground />);
    const inputCards = screen.queryAllByTestId("input-card");
    expect(inputCards.length).toBe(0);
  });

  it("navigates to project page when clicking PLAYGROUND breadcrumb", () => {
    testRender(<ProjectPlayground />);
    const playgroundButton = screen.getByText("PLAYGROUND");
    fireEvent.click(playgroundButton);
    expect(mockPush).toHaveBeenCalledWith(`${routes.projects}/123`);
  });
});
