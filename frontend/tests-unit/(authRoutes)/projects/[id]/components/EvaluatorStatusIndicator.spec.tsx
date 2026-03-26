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

import EvaluatorStatusIndicator from "@/app/(authRoutes)/projects/[id]/components/EvaluatorStatusIndicator";
import { testRender } from "@/tests-unit/render";
import { EvaluationScoreStatus } from "@/types";
import { render, screen } from "@testing-library/react";

jest.mock("@/components/Chip", () => {
  const MockChip = ({ label, icon }: any) => (
    <div data-testid="chip">
      {icon && <span data-testid="chip-icon">{icon}</span>}
      <span>{label}</span>
    </div>
  );
  MockChip.displayName = "MockChip";

  return MockChip;
});

jest.mock("@/components/MaterialIcon", () => {
  const MockMaterialIcon = ({ name }: any) => (
    <span data-testid="material-icon">{name}</span>
  );
  MockMaterialIcon.displayName = "MockMaterialIcon";

  return MockMaterialIcon;
});

describe("EvaluatorStatusIndicator", () => {
  it("returns null when status is null", () => {
    const { container } = render(<EvaluatorStatusIndicator status={null} />);
    expect(container.firstChild).toBeNull();
  });

  it("renders a SUCCESSFUL chip with score and category", () => {
    testRender(
      <EvaluatorStatusIndicator
        status={EvaluationScoreStatus.SUCCESSFUL}
        score={0.95}
        category="Accuracy"
      />,
    );
    expect(screen.getByText("0.95 - Accuracy")).toBeInTheDocument();
  });

  it("renders tooltip when provided (SUCCESSFUL)", () => {
    testRender(
      <EvaluatorStatusIndicator
        status={EvaluationScoreStatus.SUCCESSFUL}
        score={0.8}
        category="F1"
        toolTipText="F1 Score from model"
      />,
    );
    expect(screen.getByText("0.8 - F1")).toBeInTheDocument();
  });

  it("renders FAILED status with icon", () => {
    testRender(
      <EvaluatorStatusIndicator status={EvaluationScoreStatus.FAILED} />,
    );
    expect(screen.getByText("Failed")).toBeInTheDocument();
    expect(screen.getByTestId("material-icon")).toHaveTextContent("error");
  });

  it("renders PENDING status with icon", () => {
    testRender(
      <EvaluatorStatusIndicator status={EvaluationScoreStatus.PENDING} />,
    );
    expect(screen.getByText("Pending")).toBeInTheDocument();
    expect(screen.getByTestId("material-icon")).toHaveTextContent("schedule");
  });

  it("adds count/total if provided", () => {
    testRender(
      <EvaluatorStatusIndicator
        status={EvaluationScoreStatus.FAILED}
        count={3}
        total={10}
      />,
    );
    expect(screen.getByText("Failed: 3/10")).toBeInTheDocument();
  });

  it("returns null for SUCCESSFUL without score", () => {
    const { container } = render(
      <EvaluatorStatusIndicator status={EvaluationScoreStatus.SUCCESSFUL} />,
    );
    expect(container.firstChild).toBeNull();
  });
});
