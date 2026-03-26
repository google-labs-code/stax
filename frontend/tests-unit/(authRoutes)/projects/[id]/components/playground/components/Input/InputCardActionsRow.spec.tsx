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

import InputCardActionsRow from "@/app/(authRoutes)/projects/[id]/playground/components/Input/InputCardActionsRow";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/utils/logGAevent", () => jest.fn());

describe("InputCardActionsRow", () => {
  const mockSetInputs = jest.fn();
  const mockSetIsLoading = jest.fn();
  const mockOpenManageVariablesModal = jest.fn();
  const mockScrollToCardsContainer = jest.fn();
  const mockContext = {
    inputs: [{ id: "input-1", text: "Hello, world!", role: "USER" }],
    models: [{ id: "model-1" }],
    isLoading: [false],
    setInputs: mockSetInputs,
    setOutputs: jest.fn(),
    openManageVariablesModal: mockOpenManageVariablesModal,
    isModelChanged: false,
    setIsLoading: mockSetIsLoading,
    scrollToCardsContainer: mockScrollToCardsContainer,
  };

  beforeEach(() => {
    jest.clearAllMocks();

    jest
      .spyOn(require("@/hooks/usePlaygroundContext"), "usePlaygroundContext")
      .mockReturnValue(mockContext);

    jest
      .spyOn(
        require("@/app/(authRoutes)/projects/hooks/useProjectContext"),
        "useProjectContext",
      )
      .mockReturnValue({
        projectState: { project: { type: "SIDE_BY_SIDE" } },
      });
  });

  const setup = () => testRender(<InputCardActionsRow />);

  it("renders all buttons correctly", () => {
    setup();
    expect(
      screen.getByRole("button", { name: /add message/i }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /manage variables/i }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /generate output/i }),
    ).toBeInTheDocument();
  });

  it("calls openManageVariablesModal when Manage Variables button is clicked", () => {
    setup();
    fireEvent.click(screen.getByRole("button", { name: /manage variables/i }));
    expect(mockOpenManageVariablesModal).toHaveBeenCalled();
  });

  it("disables Generate Output button when conditions are unmet", () => {
    jest
      .spyOn(require("@/hooks/usePlaygroundContext"), "usePlaygroundContext")
      .mockReturnValueOnce({
        ...mockContext,
        inputs: [],
      });
    setup();
    expect(
      screen.getByRole("button", { name: /generate output/i }),
    ).toBeDisabled();
  });
});
