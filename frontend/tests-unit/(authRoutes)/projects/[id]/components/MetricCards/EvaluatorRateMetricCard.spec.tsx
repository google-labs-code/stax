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

import EvaluatorRateMetricCard from "@/app/(authRoutes)/projects/[id]/components/MetricCards/EvaluatorRateMetricCard";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { testRender } from "@/tests-unit/render";
import { ProjectType } from "@/types";
import { screen, waitFor } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");

const mockUseProjectContext = useProjectContext as jest.MockedFunction<
  typeof useProjectContext
>;

describe("EvaluatorRateMetricCard", () => {
  const providers = ["ProviderA", "ProviderB"];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  function setup(
    projectType: ProjectType | undefined,
    loading: boolean,
    passRate?: number,
    ratingCounts?: any,
    likes?: number,
    dislikes?: number,
    total?: number,
    isSideBySide = false,
    evaluatorName?: string,
  ) {
    mockUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: projectType },
        humanEvalPassRateIsLoading: loading,
      },
      isSideBySide,
    } as any);

    testRender(
      <EvaluatorRateMetricCard
        evaluatorName={evaluatorName}
        providers={providers}
        passRate={passRate || 0}
        likes={likes || 0}
        dislikes={dislikes || 0}
        ratingCounts={ratingCounts}
        total={total}
      />,
    );
  }

  it("displays pass rate when not side-by-side and after load", async () => {
    setup(ProjectType.POINTWISE, false, 0.75, undefined, 30, 10);

    await waitFor(() => {
      expect(screen.queryByRole("status")).not.toBeInTheDocument();
    });

    expect(screen.getByText("30")).toBeInTheDocument();
    expect(screen.getByText("10")).toBeInTheDocument();
  });

  it("shows '-- %' when no likes in side-by-side", async () => {
    const ratingCounts = {
      A_IS_BETTER: 0,
      B_IS_BETTER: 0,
      BOTH_ARE_GOOD: 0,
      BOTH_ARE_BAD: 0,
    };
    setup(
      ProjectType.SIDE_BY_SIDE,
      false,
      undefined,
      ratingCounts,
      undefined,
      undefined,
      0,
      true,
    );

    await waitFor(() => {
      expect(screen.queryByRole("status")).not.toBeInTheDocument();
    });

    expect(screen.queryAllByText("0")).toHaveLength(3);
  });

  it("renders side-by-side win rate and bars correctly", async () => {
    const ratingCounts = {
      A_IS_BETTER: 30,
      B_IS_BETTER: 10,
      BOTH_ARE_GOOD: 5,
      BOTH_ARE_BAD: 5,
    };

    mockUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: ProjectType.SIDE_BY_SIDE },
        humanEvalPassRateIsLoading: false,
      },
      isSideBySide: true,
    } as any);

    testRender(
      <EvaluatorRateMetricCard
        evaluatorName="Evaluator 1"
        providers={["ProviderA"]}
        ratingCounts={ratingCounts}
        total={50}
        passRate={null}
        likes={0}
        dislikes={0}
      />,
    );

    await waitFor(() => {
      expect(screen.queryByRole("status")).not.toBeInTheDocument();
    });

    expect(screen.getByText("Human Evaluation Win Rate")).toBeInTheDocument();
    expect(screen.getByText("30")).toBeInTheDocument();
    expect(screen.getByText("10")).toBeInTheDocument();
  });

  it("renders correct title based on evaluatorName and isSideBySide", () => {
    mockUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: ProjectType.POINTWISE },
        humanEvalPassRateIsLoading: false,
      },
      isSideBySide: false,
    } as any);

    testRender(
      <EvaluatorRateMetricCard
        providers={["ProviderA"]}
        passRate={0.5}
        likes={0}
        dislikes={0}
      />,
    );

    expect(screen.getByText("Human Evaluation Pass Rate")).toBeInTheDocument();

    testRender(
      <EvaluatorRateMetricCard
        providers={["ProviderA"]}
        evaluatorName="Eval"
        passRate={null}
        likes={0}
        dislikes={0}
      />,
    );

    expect(screen.getByText("Human Evaluation Win Rate")).toBeInTheDocument();
  });
});
