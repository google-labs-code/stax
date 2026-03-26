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

import EvaluationTypeDropdown from "@/components/Evaluation/EvaluationTypeDropdown";
import { ProjectType } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../../render";

const mockCard: any = {
  id: "pairwise-test",
  evaluationType: ProjectType.SIDE_BY_SIDE,
  evaluationTypes: [
    { id: "test", type: ProjectType.POINTWISE },
    {
      id: "pairwise-test",
      type: ProjectType.SIDE_BY_SIDE,
    },
  ],
};
describe("EvaluationTypeDropdown", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders with default selected option", () => {
    testRender(
      <EvaluationTypeDropdown isCardChecked={false} card={mockCard} />,
    );
    expect(screen.getByText("SIDE-BY-SIDE")).toBeInTheDocument();
  });

  it("opens menu on button click and shows all options", () => {
    testRender(
      <EvaluationTypeDropdown isCardChecked={false} card={mockCard} />,
    );
    const button = screen.getByRole("button");
    fireEvent.click(button);

    expect(screen.getAllByText("SIDE-BY-SIDE")).toHaveLength(2);
    expect(screen.getByText("POINTWISE")).toBeInTheDocument();
  });

  it("updates selected label", () => {
    testRender(
      <EvaluationTypeDropdown isCardChecked={false} card={mockCard} />,
    );
    const button = screen.getByRole("button");
    fireEvent.click(button);

    const pointwiseOption = screen.getByText("POINTWISE");
    fireEvent.click(pointwiseOption);

    expect(screen.getByText("POINTWISE")).toBeInTheDocument();
  });
});
