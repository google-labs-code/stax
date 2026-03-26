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

import {
  ProjectProvider,
  useProjectContext,
} from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { useChatContext } from "@/hooks/useChatContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import * as clientQueries from "@/queries/clientQueries";
import { ProjectType } from "@/types";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, waitFor } from "@testing-library/react";
import { useParams } from "next/navigation";

jest.mock("next/navigation", () => ({
  useParams: jest.fn(),
}));

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: jest.fn(),
}));

jest.mock("@/queries/clientQueries", () => ({
  getProjectQuery: jest.fn(),
  getProjectSxSQuery: jest.fn(),
  metricsSummaryQuery: jest.fn(),
  getPointwiseEvalAnalyticsQuery: jest.fn(),
  getSxSEvalAnalyticsQuery: jest.fn(),
  inferenceSxsMetricsQuery: jest.fn(),
  humanEvalPassRateQuery: jest.fn(),
  SxsHumanEvalPassRateQuery: jest.fn(),
  projectHasRemainingJobsQuery: jest.fn(),
}));

const mockProject = {
  project_id: "test-project-id",
  type: ProjectType.POINTWISE,
};

const queryClient = new QueryClient();

const TestComponent = () => {
  const { projectState } = useProjectContext();

  return <div>Project ID: {projectState.project?.project_id}</div>;
};

describe("ProjectProvider", () => {
  beforeEach(() => {
    jest.clearAllMocks();

    (useParams as jest.Mock).mockReturnValue({ id: "test-project-id" });
    (useProjectsContext as jest.Mock).mockReturnValue({
      allProjects: [mockProject],
      isLoadingProjects: false,
    });
    (useChatContext as jest.Mock).mockReturnValue({
      fetchChats: jest.fn(),
    });
  });

  it("should provide project context with correct project ID", async () => {
    (clientQueries.getProjectQuery as jest.Mock).mockResolvedValue({
      workbook_rows: [],
      total_size: 0,
    });
    (clientQueries.metricsSummaryQuery as jest.Mock).mockResolvedValue({});
    (
      clientQueries.getPointwiseEvalAnalyticsQuery as jest.Mock
    ).mockResolvedValue([]);
    (clientQueries.humanEvalPassRateQuery as jest.Mock).mockResolvedValue({
      passRate: 0.75,
    });
    (clientQueries.projectHasRemainingJobsQuery as jest.Mock).mockResolvedValue(
      false,
    );

    const { getByText } = render(
      <QueryClientProvider client={queryClient}>
        <ProjectProvider>
          <TestComponent />
        </ProjectProvider>
      </QueryClientProvider>,
    );

    await waitFor(() => {
      expect(getByText("Project ID: test-project-id")).toBeInTheDocument();
    });
  });

  it("should handle SIDE_BY_SIDE project type", async () => {
    const sxsProject = { ...mockProject, type: ProjectType.SIDE_BY_SIDE };

    (useProjectsContext as jest.Mock).mockReturnValue({
      allProjects: [sxsProject],
      isLoadingProjects: false,
    });

    (clientQueries.getProjectSxSQuery as jest.Mock).mockResolvedValue({
      sxs_rows: [],
      total_size: 0,
    });
    (clientQueries.inferenceSxsMetricsQuery as jest.Mock).mockResolvedValue({});
    (clientQueries.getSxSEvalAnalyticsQuery as jest.Mock).mockResolvedValue([]);
    (clientQueries.SxsHumanEvalPassRateQuery as jest.Mock).mockResolvedValue({
      ratingCounts: {
        A_IS_BETTER: 2,
        B_IS_BETTER: 1,
      },
      total: 3,
    });
    (clientQueries.projectHasRemainingJobsQuery as jest.Mock).mockResolvedValue(
      false,
    );

    const { getByText } = render(
      <QueryClientProvider client={queryClient}>
        <ProjectProvider>
          <TestComponent />
        </ProjectProvider>
      </QueryClientProvider>,
    );

    await waitFor(() => {
      expect(getByText("Project ID: test-project-id")).toBeInTheDocument();
    });
  });

  it("should not crash if no matching project is found", async () => {
    (useProjectsContext as jest.Mock).mockReturnValue({
      allProjects: [],
      isLoadingProjects: false,
    });

    const { getByText } = render(
      <QueryClientProvider client={queryClient}>
        <ProjectProvider>
          <TestComponent />
        </ProjectProvider>
      </QueryClientProvider>,
    );

    await waitFor(() => {
      expect(getByText("Project ID:")).toBeInTheDocument();
    });
  });
});
