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

import EvaluatorScoreMetricCard from "@/app/(authRoutes)/projects/[id]/components/MetricCards/EvaluatorScoreMetricCard";
import EvaluatorScoreSxSMetricCard from "@/app/(authRoutes)/projects/[id]/components/MetricCards/EvaluatorScoreSxSMetricCard";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { testRender } from "@/tests-unit/render";
import { Project, ProjectType } from "@/types";
import { screen, waitFor } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");

function renderMockedData({
  projectType,
  isLoading,
  evaluator,
}: {
  projectType: ProjectType;
  isLoading: boolean;
  evaluator: any;
}) {
  (
    useProjectContext as jest.MockedFunction<typeof useProjectContext>
  ).mockReturnValue({
    projectType,
    isSideBySide: projectType === ProjectType.SIDE_BY_SIDE,
    projectState: {
      project: { type: projectType } as Project,
      evaluatorMetricsIsLoading: isLoading,
      evaluatorSxSMetricsIsLoading: isLoading,
      evalAnalyticsScores:
        projectType === ProjectType.POINTWISE ? [{ ...evaluator }] : [],
      evalSxSAnalyticsScores: {
        sideBySide: [],
        pointwise:
          projectType === ProjectType.SIDE_BY_SIDE ? [{ ...evaluator }] : [],
      },
    },
  } as any);
}

describe("EvaluatorScoreMetricCard", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders pointwise evaluator average_score correctly", async () => {
    const evaluator: any = { average_score: 0.92, score_counts: [] };
    renderMockedData({
      projectType: ProjectType.POINTWISE,
      isLoading: false,
      evaluator,
    });

    testRender(
      <EvaluatorScoreMetricCard
        evaluator={evaluator}
        providers={["ProviderA"]}
      />,
    );

    await waitFor(() => expect(screen.getByText("0.92")).toBeInTheDocument());
    expect(screen.queryByText("--")).not.toBeInTheDocument();
  });

  it("renders -- when side-by-side evaluator scores and delta are undefined", async () => {
    const evaluator: any = {
      side_a: {
        average_score: undefined,
        score_counts: [],
      },
      side_b: {
        average_score: undefined,
        score_counts: [],
      },
      delta: undefined,
    };
    renderMockedData({
      projectType: ProjectType.SIDE_BY_SIDE,
      isLoading: false,
      evaluator,
    });

    testRender(
      <EvaluatorScoreSxSMetricCard
        evaluator={evaluator}
        providers={["ProviderA"]}
      />,
    );

    await waitFor(() => {
      expect(screen.queryAllByText("--")).toHaveLength(2);
    });
  });

  it("show both scores in side by side", async () => {
    const evaluator: any = {
      side_a: {
        average_score: 0.71,
        score_counts: [],
      },
      side_b: {
        average_score: 0.74,
        score_counts: [],
      },
      delta: 1,
      scorer_name: "Chat Quality",
      scorer_id: "some-id",
    };

    renderMockedData({
      projectType: ProjectType.SIDE_BY_SIDE,
      isLoading: false,
      evaluator,
    });

    testRender(
      <EvaluatorScoreSxSMetricCard
        evaluator={evaluator}
        providers={["ProviderA"]}
      />,
    );
    expect(screen.getByText("0.71")).toBeInTheDocument();
    expect(screen.getByText("0.74")).toBeInTheDocument();
  });

  it("shows fallback '--' if score is undefined", async () => {
    const evaluator: any = { average_score: undefined, score_counts: [] };
    renderMockedData({
      projectType: ProjectType.POINTWISE,
      isLoading: false,
      evaluator,
    });

    testRender(
      <EvaluatorScoreMetricCard
        evaluator={evaluator}
        providers={["ProviderA"]}
      />,
    );

    await waitFor(() => {
      expect(screen.queryAllByText("--")).toHaveLength(1);
    });
  });
});
