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

import ProjectWorkbook from "@/app/(authRoutes)/projects/[id]/components/ProjectWorkbook";
import { testRender } from "@/tests-unit/render";
import { ProjectType } from "@/types";
import { screen } from "@testing-library/react";

jest.mock("@/components/MaterialIcon", () => ({
  __esModule: true,
  default: ({ name }: any) => <div data-testid="material-icon">{name}</div>,
}));

jest.mock("@/app/(authRoutes)/projects/[id]/components/Workbook", () => ({
  __esModule: true,
  default: (props: any) => (
    <div data-testid="workbook">
      Workbook
      <button onClick={props.onAddData}>Trigger Add Data</button>
    </div>
  ),
}));

jest.mock("@/utils/logGAevent", () => ({
  __esModule: true,
  default: jest.fn(),
}));

const mockRefetch = jest.fn();
const mockSetPageSize = jest.fn();
const mockSetPage = jest.fn();
const mockSetRefresh = jest.fn();
const mockHandleCompare = jest.fn();

const baseProps = {
  projectData: null,
  totalSize: 20,
  page: 1,
  pageSize: 10,
  handleCompare: mockHandleCompare,
  setPage: mockSetPage,
  setPageSize: mockSetPageSize,
  setRefreshHumanPassRate: mockSetRefresh,
  deleteWorkbookRowMutation: {} as any,
  refetchProject: mockRefetch,
  projectError: null,
  projectId: "project-1",
  projectType: ProjectType.POINTWISE,
  hideTooltips: false,
};

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: () => ({
    projectType: ProjectType.POINTWISE,
    isSideBySide: false,
    projectActions: {
      resetPagination: jest.fn(),
    },
    projectState: {
      project: {
        project_id: "project-123",
        name: "Test Project",
        type: "POINTWISE",
        description: "Test description",
        created_at: "2025-07-22T08:19:43.128+00:00",
        updated_at: "2025-07-22T08:19:43.128+00:00",
        is_default_project: true,
        providers: ["GOOGLE"],
      },
      activeStep: 1,
      metricsData: {
        total_inferences: 100,
        average_turn_time_taken: 5000,
        total_prompt_tokens: 1000,
        total_completion_tokens: 1000,
        total_tokens: 2000,
      },
      humanEvalPassRate: {
        passRate: 0.5,
        likes: 2,
        dislikes: 2,
      },
      importedDataset: null,
      pageSize: 15,
      page: 1,
      isLoadingProjects: false,
      isLoadingProject: true,
      evalAnalyticsScores: [],
      projectData: null,
      humanEvalPassRateIsLoading: true,
      inferenceMetricsIsLoading: false,
      evaluatorMetricsIsLoading: false,
    },
  }),
}));

describe("ProjectWorkbook", () => {
  it("renders header and MaterialIcon", async () => {
    testRender(<ProjectWorkbook {...baseProps} />);
    expect(screen.getByText("Project benchmark")).toBeInTheDocument();
    expect(screen.getByTestId("material-icon")).toHaveTextContent("info");
  });

  it("renders Workbook component", async () => {
    testRender(<ProjectWorkbook {...baseProps} />);
    expect(screen.getByTestId("workbook")).toBeInTheDocument();
    expect(screen.getByText("Trigger Add Data")).toBeInTheDocument();
  });
});
