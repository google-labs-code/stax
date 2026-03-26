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

import ProjectMetrics from "@/app/(authRoutes)/projects/[id]/components/ProjectMetrics";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { testRender } from "@/tests-unit/render";
import { ProjectType } from "@/types";
import { formatLatency } from "@/utils/helpers";
import { screen } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");

const mockProjectState = {
  projectType: ProjectType.POINTWISE,
  project: {
    project_id: "project-1365d713-6422-47f0-b995-480ada786d4e",
    name: "Quick Compare History",
    type: ProjectType.POINTWISE,
    description:
      "This project will by default contain all quick compare history.",
    created_at: "2025-07-22T08:19:43.128+00:00",
    updated_at: "2025-07-22T08:19:43.128+00:00",
    is_default_project: true,
    providers: ["GOOGLE"],
  },
  metricsData: {
    total_inferences: 338,
    average_turn_time_taken: 5736.458579881657,
    total_prompt_tokens: 126754,
    total_completion_tokens: 86860,
    total_tokens: 382721,
  },
  humanEvalPassRate: {
    passRate: 0.6666666666666666,
    likes: 6,
    dislikes: 3,
  },
  evalAnalyticsScores: [
    {
      score_counts: [
        {
          category: "Very good",
          score: "1.0",
          count: 2,
          color: "var(--color-green)",
        },
        {
          category: "Good",
          score: "0.75",
          count: 0,
          color: "var(--color-purple)",
        },
      ],
      average_score: 1,
      scorer_id: "llm-eval-2d5e92cd-13d0-4705-aa57-1f76e3abf41e",
      scorer_name: "Chat Quality",
    },
    {
      score_counts: [
        {
          category: "Safe",
          score: "1.0",
          count: 2,
          color: "var(--color-green)",
        },
        {
          category: "Unsafe",
          score: "0.0",
          count: 0,
          color: "var(--color-red)",
        },
      ],
      average_score: 1,
      scorer_id: "llm-eval-5444386f-1308-46bc-bf9e-a84f32d2fd81",
      scorer_name: "Chat Safety",
    },
  ],
  evalSxSAnalyticsScores: {
    sideBySide: [],
    pointwise: [],
  },
  isLoadingProject: false,
  humanEvalPassRateIsLoading: false,
  inferenceMetricsIsLoading: false,
  evaluatorMetricsIsLoading: false,
  importedDataset: null,
  pageSize: 15,
  page: 1,
  activeStep: 1,
  projectData: null,
};

describe("ProjectMetrics", () => {
  it("renders Project Metrics section and metric cards", () => {
    (useProjectContext as jest.Mock).mockReturnValue({
      projectState: mockProjectState,
    });

    testRender(<ProjectMetrics providers={["GOOGLE"]} />);

    expect(screen.getByText("Project metrics")).toBeInTheDocument();

    expect(screen.getByText("6")).toBeInTheDocument();
    expect(screen.getByText("3")).toBeInTheDocument();

    expect(screen.getByText(/latency/i)).toBeInTheDocument();

    expect(screen.getByText(/tokens/i)).toBeInTheDocument();

    expect(screen.getByText("Chat Quality")).toBeInTheDocument();
    expect(screen.getByText("Chat Safety")).toBeInTheDocument();
  });

  it("renders Project Metrics with EvaluatorScoreSxSMetricCard", () => {
    const evalSxSAnalyticsScores = {
      sideBySide: [{ scorer_name: "test sideBySide" }],
      pointwise: [{ scorer_name: "test pointwise" }],
    };
    (useProjectContext as jest.Mock).mockReturnValue({
      projectState: { ...mockProjectState, evalSxSAnalyticsScores },
    });

    testRender(<ProjectMetrics providers={["GOOGLE"]} />);

    expect(screen.getByText("test sideBySide")).toBeInTheDocument();
    expect(screen.getByText("test pointwise")).toBeInTheDocument();
  });

  it("renders SxS metrics", () => {
    const metricsData = {
      sideA: {
        average_turn_time_taken: 5736.458579881657,
        total_prompt_tokens: 126754,
        total_completion_tokens: 86860,
        total_tokens: 382721,
      },
      sideB: {
        average_turn_time_taken: 6336.458579881657,
        total_prompt_tokens: 231754,
        total_completion_tokens: 85860,
        total_tokens: 456721,
      },
    };
    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: true,
      projectState: { ...mockProjectState, metricsData },
    });

    testRender(<ProjectMetrics providers={["GOOGLE"]} />);

    expect(
      screen.getByText(
        formatLatency(metricsData.sideA.average_turn_time_taken),
      ),
    ).toBeInTheDocument();
    expect(
      screen.getByText(
        formatLatency(metricsData.sideB.average_turn_time_taken),
      ),
    ).toBeInTheDocument();
  });

  it("renders Project Metrics with EvaluatorScoreSxSMetricCard", () => {
    const humanEvalPassRate = {
      ratingCounts: { A_IS_BETTER: 1, B_IS_BETTER: 0 },
      total: 13355,
    };
    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: true,
      projectState: { ...mockProjectState, humanEvalPassRate },
    });

    testRender(<ProjectMetrics providers={["GOOGLE"]} />);

    expect(screen.getByText(13355)).toBeInTheDocument();
  });
});
