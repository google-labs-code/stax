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

import ProjectHeader from "@/app/(authRoutes)/projects/components/ProjectHeader";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { testRender } from "@/tests-unit/render";
import { ProjectType, Provider } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");

describe("ProjectHeader", () => {
  const mockOpenEditModal = jest.fn();

  const baseProps: any = {
    project: {
      project_id: "project-123",
      name: "Test Project",
      type: ProjectType.POINTWISE,
      description: "Test description",
      created_at: "2025-07-22T08:19:43.128+00:00",
      updated_at: "2025-07-22T08:19:43.128+00:00",
      is_default_project: true,
      providers: [Provider.GOOGLE],
      job_statuses: [],
      inference_monitoring_summary: undefined,
      finished_job_tasks: 3,
      total_job_tasks: 4,
    },
    isLoadingProjects: false,
  };

  afterEach(() => {
    jest.clearAllMocks();
  });

  beforeEach(() => {
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isEditProjectModalOpened: false,
        openEditProjectModal: mockOpenEditModal,
        closeEditProjectModal: jest.fn(),
      },
    }));
  });

  it("renders loader when projects are loading", () => {
    testRender(
      <ProjectHeader {...baseProps} isLoadingProjects={true} project={null} />,
    );

    const loader = document.querySelector(".mantine-Loader-root");
    expect(loader).toBeInTheDocument();
  });

  it("renders project title when loaded", () => {
    testRender(<ProjectHeader {...baseProps} />);

    expect(screen.getByText("Test Project")).toBeInTheDocument();
  });

  it("falls back to default title if project name is missing", () => {
    testRender(
      <ProjectHeader {...baseProps} project={null} isLoadingProjects={false} />,
    );

    expect(screen.getAllByText("Evaluation Projects")).toHaveLength(2);
  });

  it("calls openEditProjectModal on edit icon click", () => {
    testRender(<ProjectHeader {...baseProps} />);

    const editButton = screen.getByText("edit");
    fireEvent.click(editButton);

    expect(mockOpenEditModal).toHaveBeenCalledTimes(1);
  });
});
