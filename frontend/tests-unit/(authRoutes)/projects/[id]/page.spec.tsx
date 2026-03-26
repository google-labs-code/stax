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

import Projects from "@/app/(authRoutes)/projects/[id]/page";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: () => ({ isLoadingProjects: false }),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: () => ({
    projectState: {
      isLoadingProject: false,
      project: { project_id: "1", type: "POINTWISE", providers: [] },
      projectData: { workbook_rows: [] },
      totalSize: 0,
      page: 1,
      pageSize: 10,
    },
    projectActions: {
      handleCompare: jest.fn(),
      setPage: jest.fn(),
      setPageSize: jest.fn(),
      setRefreshHumanPassRate: jest.fn(),
      setImportedDataset: jest.fn(),
      resetProjectData: jest.fn(),
    },
    projectQueries: {
      refetchProject: jest.fn(),
      startPolling: jest.fn(),
      stopPolling: jest.fn(),
      resetMetrics: jest.fn(),
      deleteWorkbookRowMutation: { mutate: jest.fn() },
      projectError: null,
    },
    projectModals: {
      openEditProjectModal: jest.fn(),
      closeGetStartedModal: jest.fn(),
      openGetStartedModal: jest.fn(),
    },
  }),
}));

jest.mock("next/navigation", () => ({
  useParams: () => ({ id: "1" }),
}));

jest.mock("@/components/Page", () => {
  const MockPage = ({ children }: any) => (
    <div data-testid="page">{children}</div>
  );
  MockPage.displayName = "MockPage";

  return MockPage;
});

jest.mock("@/app/(authRoutes)/projects/components/ProjectHeader", () => {
  const ProjectHeader = () => (
    <div data-testid="project-header">ProjectHeader</div>
  );
  ProjectHeader.displayName = "MockProjectHeader";

  return ProjectHeader;
});

jest.mock("@/app/(authRoutes)/projects/[id]/components/ProjectMetrics", () => {
  const ProjectMetrics = () => (
    <div data-testid="project-metrics">ProjectMetrics</div>
  );
  ProjectMetrics.displayName = "MockProjectMetrics";

  return ProjectMetrics;
});

jest.mock("@/app/(authRoutes)/projects/[id]/components/ProjectWorkbook", () => {
  const ProjectWorkbook = () => (
    <div data-testid="project-workbook">ProjectWorkbook</div>
  );
  ProjectWorkbook.displayName = "MockProjectWorkbook";

  return ProjectWorkbook;
});

jest.mock("@/app/(authRoutes)/projects/[id]/components/ProjectModals", () => {
  const ProjectModals = () => (
    <div data-testid="project-modals">ProjectModals</div>
  );
  ProjectModals.displayName = "MockProjectModals";

  return ProjectModals;
});

jest.mock("@/utils/projectsOnboardingStorage", () => ({
  ProjectsOnboardingStorage: {
    get: jest.fn().mockReturnValue(false),
    set: jest.fn(),
  },
}));

describe("Projects Page", () => {
  it("renders correctly with mocked data", () => {
    testRender(<Projects />);

    expect(screen.getByTestId("project-header")).toBeInTheDocument();
    expect(screen.getByTestId("project-metrics")).toBeInTheDocument();
    expect(screen.getByTestId("project-workbook")).toBeInTheDocument();
    expect(screen.getByTestId("project-modals")).toBeInTheDocument();
  });
});
