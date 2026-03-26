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

import PopoverEvaluatorMenu from "@/app/(authRoutes)/analytics/components/evaluation/EvaluatorsOptions";
import { testRender } from "@/tests-unit/render";
import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("PopoverEvaluatorMenu", () => {
  const mockSetSelectedEvals = jest.fn();
  const mockEvals = [
    { score: "Eval 1", checked: true, visible: true },
    { score: "Eval 2", checked: false, visible: true },
    { score: "Eval 3", checked: true, visible: true },
  ];

  const renderComponent = (customEvals = mockEvals) =>
    testRender(
      <PopoverEvaluatorMenu
        selectedEvals={customEvals}
        setSelectedEvals={mockSetSelectedEvals}
      />,
    );

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("disables button if no evaluators", () => {
    renderComponent([]);
    expect(
      screen.getByRole("button", { name: /view evaluator/i }),
    ).toBeDisabled();
  });

  it("opens popover and displays evaluators", async () => {
    renderComponent();
    const button = screen.getByRole("button", { name: /view evaluator/i });
    await userEvent.click(button);

    const portal = await screen.findByRole("dialog");

    expect(within(portal).getByText("Hide All")).toBeInTheDocument();
    expect(within(portal).getByText("Eval 1")).toBeInTheDocument();
    expect(within(portal).getByText("Eval 2")).toBeInTheDocument();
    expect(within(portal).getByText("Eval 3")).toBeInTheDocument();
  });

  it("filters evaluators based on search input", async () => {
    renderComponent();
    const button = screen.getByRole("button", { name: /view evaluator/i });
    await userEvent.click(button);

    const portal = await screen.findByRole("dialog");

    const input = within(portal).getByRole("textbox");
    await userEvent.clear(input);
    await userEvent.type(input, "Eval 2");

    await waitFor(() => {
      expect(mockSetSelectedEvals).toHaveBeenCalledWith(
        expect.arrayContaining([
          expect.objectContaining({
            score: "Eval 1",
            visible: false,
          }),
          expect.objectContaining({
            score: "Eval 2",
            visible: true,
          }),
        ]),
      );
    });
  });

  it("calls setSelectedEvals to uncheck all when Hide All is clicked", async () => {
    renderComponent();
    const button = screen.getByText("View evaluator");
    await userEvent.click(button);

    const portal = await screen.findByRole("dialog");

    const hideAllButton = within(portal).getByText("Hide All");
    await userEvent.click(hideAllButton);

    expect(mockSetSelectedEvals).toHaveBeenCalledWith(
      expect.arrayContaining([
        expect.objectContaining({
          score: "Eval 1",
          checked: false,
          visible: true,
        }),
      ]),
    );
  });
});
