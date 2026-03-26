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

import OutputCard from "@/app/(authRoutes)/projects/[id]/playground/components/Output/OutputCard";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock(
  "@/app/(authRoutes)/projects/[id]/playground/components/Output/OutputCardEvaluator",
  () =>
    jest.fn(() => (
      <div data-testid="output-card-evaluator">OutputCardEvaluator</div>
    )),
);

jest.mock(
  "@/app/(authRoutes)/projects/[id]/playground/components/Output/OutputCardItem",
  () =>
    jest.fn(({ text, tokens, latency, isLoading, promptName }) => (
      <div
        data-testid={`output-card-item-${promptName}`}
        data-text={text}
        data-tokens={tokens}
        data-latency={latency}
        data-is-loading={isLoading}
      >
        OutputCardItem-{promptName}
      </div>
    )),
);

jest.mock("@/app/(authRoutes)/projects/[id]/components/OnboardingTooltip", () =>
  jest.fn(({ children }) => (
    <div data-testid="onboarding-tooltip">{children}</div>
  )),
);

describe("OutputCard", () => {
  const mockSetOnboardingIndex = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (usePlaygroundContext as jest.Mock).mockReturnValue({
      outputs: [
        { text: "Output A", tokens: 120, latency: 200 },
        { text: "Output B", tokens: 150, latency: 300 },
      ],
      isLoading: [false, true],
      onboardingIndex: 3,
      setOnboardingIndex: mockSetOnboardingIndex,
    });

    (useProjectContext as jest.Mock).mockReturnValue({
      projectState: {
        project: { type: "SIDE_BY_SIDE", project_id: "proj-123" },
      },
    });
  });

  it("renders OutputCard properly when not in SIDE_BY_SIDE mode", () => {
    (useProjectContext as jest.Mock).mockReturnValue({
      projectState: { project: { type: "OTHER", project_id: "proj-123" } },
    });

    testRender(<OutputCard />);

    const outputCardItemA = screen.getByTestId("output-card-item-A");
    expect(outputCardItemA).toBeInTheDocument();

    expect(screen.queryByTestId("output-card-item-B")).not.toBeInTheDocument();
  });

  it("handles onboarding tooltip interactions correctly", () => {
    testRender(<OutputCard />);

    const onboardingTooltip = screen.getByTestId("onboarding-tooltip");
    expect(onboardingTooltip).toBeInTheDocument();

    mockSetOnboardingIndex(4);
    expect(mockSetOnboardingIndex).toHaveBeenCalledWith(4);

    mockSetOnboardingIndex(2);
    expect(mockSetOnboardingIndex).toHaveBeenCalledWith(2);

    mockSetOnboardingIndex(null);
    expect(mockSetOnboardingIndex).toHaveBeenCalledWith(null);
  });

  it("renders OutputCardEvaluator correctly", () => {
    testRender(<OutputCard />);

    const evaluator = screen.getByTestId("output-card-evaluator");
    expect(evaluator).toBeInTheDocument();
  });
});
