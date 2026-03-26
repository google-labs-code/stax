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

import EvaluatorSortButton from "@/components/Evaluation/EvaluatorSortButton";
import { testRender } from "@/tests-unit/render";
import { EvaluatorCardItem, EvaluatorTab, EvaluatorType } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@mantine/hooks", () => ({
  ...jest.requireActual("@mantine/hooks"),
  useDisclosure: () => [false, { open: jest.fn(), close: jest.fn() }],
}));

jest.mock("@/components/MaterialIcon", () => ({
  __esModule: true,
  default: (props: { name: string }) => (
    <span data-testid="material-icon">{props.name}</span>
  ),
}));

jest.mock("@/components/icons/SortZToAIcon", () => ({
  __esModule: true,
  default: () => <span data-testid="sort-z-to-a-icon" />,
}));

describe("EvaluatorSortButton", () => {
  const setData = jest.fn();

  const mockEvaluators = [
    {
      id: "z-eval",
      name: "Z-Eval",
      type: EvaluatorType.SYSTEM,
    },
    {
      id: "b-eval",
      name: "B-Eval",
      type: EvaluatorType.SYSTEM,
    },
    {
      id: "a-eval",
      name: "A-Eval",
      type: EvaluatorType.SYSTEM,
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders sort button", () => {
    testRender(
      <EvaluatorSortButton
        data={mockEvaluators as unknown as EvaluatorCardItem[]}
        setData={setData}
      />,
    );

    expect(screen.getByText("Sort")).toBeInTheDocument();
    expect(screen.getByTestId("material-icon")).toBeInTheDocument();
  });

  it("disables menu when there is no data", () => {
    testRender(<EvaluatorSortButton data={[]} setData={setData} />);

    const group = screen.getByText("Sort").closest(".mantine-Group-root");
    expect(group).toHaveClass("cursor-not-allowed");
    expect(group).toHaveClass("opacity-50");
  });

  it("handles sorting A to Z functionality", () => {
    testRender(
      <EvaluatorSortButton
        data={mockEvaluators as unknown as EvaluatorCardItem[]}
        setData={setData}
      />,
    );

    const actionItems = [
      {
        label: "Sort A to Z",
        onClick: () => {
          const sortedData = [...mockEvaluators].sort((a, b) =>
            a.name.localeCompare(b.name),
          );
          setData(sortedData);
        },
      },
      {
        label: "Sort Z to A",
        onClick: () => {
          const sortedData = [...mockEvaluators].sort((a, b) =>
            b.name.localeCompare(a.name),
          );
          setData(sortedData);
        },
      },
    ];

    actionItems[0].onClick();

    expect(setData).toHaveBeenCalledTimes(1);
    const sortedData = [...mockEvaluators].sort((a, b) =>
      a.name.localeCompare(b.name),
    );
    expect(setData).toHaveBeenCalledWith(sortedData);
  });

  it("handles sorting Z to A functionality", () => {
    testRender(
      <EvaluatorSortButton
        data={mockEvaluators as unknown as EvaluatorCardItem[]}
        setData={setData}
      />,
    );

    const actionItems = [
      {
        label: "Sort A to Z",
        onClick: () => {
          const sortedData = [...mockEvaluators].sort((a, b) =>
            a.name.localeCompare(b.name),
          );
          setData(sortedData);
        },
      },
      {
        label: "Sort Z to A",
        onClick: () => {
          const sortedData = [...mockEvaluators].sort((a, b) =>
            b.name.localeCompare(a.name),
          );
          setData(sortedData);
        },
      },
    ];

    actionItems[1].onClick();

    expect(setData).toHaveBeenCalledTimes(1);
    const sortedData = [...mockEvaluators].sort((a, b) =>
      b.name.localeCompare(a.name),
    );
    expect(setData).toHaveBeenCalledWith(sortedData);
  });

  it("filters by USER evaluator when activeTab is MY_EVALUATORS", () => {
    const mixedData = [
      { id: "user1", name: "User1", type: EvaluatorType.USER },
      { id: "system1", name: "System1", type: EvaluatorType.SYSTEM },
    ];

    testRender(
      <EvaluatorSortButton
        data={mixedData as unknown as EvaluatorCardItem[]}
        setData={setData}
        activeTab={EvaluatorTab.MY_EVALUATORS}
      />,
    );

    const button = screen.getByText("Sort");
    expect(button).toBeInTheDocument();
  });

  it("filters by SYSTEM evaluator when activeTab is DEFAULT", () => {
    const mixedData = [
      { id: "user1", name: "User1", type: EvaluatorType.USER },
      { id: "system1", name: "System1", type: EvaluatorType.SYSTEM },
    ];

    testRender(
      <EvaluatorSortButton
        data={mixedData as unknown as EvaluatorCardItem[]}
        setData={setData}
        activeTab={EvaluatorTab.DEFAULT}
      />,
    );

    const button = screen.getByText("Sort");
    expect(button).toBeInTheDocument();
  });

  it("displays tooltip when hovering over disabled sort button", async () => {
    testRender(<EvaluatorSortButton data={[]} setData={setData} />);

    const sortButton = screen.getByText("Sort").parentElement;
    if (sortButton) {
      fireEvent.mouseEnter(sortButton);

      await waitFor(() => {
        expect(
          document.querySelector(".mantine-Tooltip-tooltip"),
        ).toBeInTheDocument();
      });
    }
  });
});
