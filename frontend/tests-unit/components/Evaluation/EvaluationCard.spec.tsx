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

import EvaluationCard from "@/components/Evaluation/EvaluationCard";
import { testRender } from "@/tests-unit/render";
import { EvaluatorCardItem, ProjectType } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("dayjs", () => {
  const actualDayjs = jest.requireActual("dayjs");

  return (...args: any[]) => actualDayjs(...args);
});

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
    prefetch: jest.fn(),
  }),
}));

describe("EvaluationCard", () => {
  const card: EvaluatorCardItem = {
    id: "1",
    name: "Test Evaluator",
    description: "This is a test description.",
    evaluationTypes: [
      {
        id: "test",
        type: ProjectType.POINTWISE,
      },
    ],
    // add other required fields if needed
  } as any;

  const setSelectedCards = jest.fn();
  const onClick = jest.fn();
  const openDeleteModal = jest.fn();

  it("testRenders the card with name and description", () => {
    testRender(
      <EvaluationCard
        card={card}
        isPage={true}
        tab={null}
        setSelectedCards={setSelectedCards}
        isCardChecked={false}
        onClick={onClick}
        openDeleteModal={openDeleteModal}
      />,
    );
    expect(screen.getByText("Test Evaluator")).toBeInTheDocument();
    expect(screen.getByText("This is a test description.")).toBeInTheDocument();
    expect(screen.getByText("LLM")).toBeInTheDocument();
  });

  it("calls onClick when card is clicked", () => {
    testRender(
      <EvaluationCard
        card={card}
        isPage={false}
        tab={null}
        setSelectedCards={setSelectedCards}
        isCardChecked={false}
        onClick={onClick}
        openDeleteModal={openDeleteModal}
      />,
    );
    fireEvent.click(screen.getByText("Test Evaluator"));
    expect(onClick).toHaveBeenCalledWith(card.id, card.name, setSelectedCards);
  });

  it("shows evaluator type chips when isPage is true", () => {
    testRender(
      <EvaluationCard
        card={card}
        isPage={true}
        tab={"tab1"}
        setSelectedCards={setSelectedCards}
        isCardChecked={false}
        onClick={onClick}
        openDeleteModal={openDeleteModal}
      />,
    );
    expect(screen.getByText("LLM")).toBeInTheDocument();
    expect(screen.getByText("POINTWISE")).toBeInTheDocument();
  });
});
