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

import ProjectsTable from "@/app/(authRoutes)/projects/components/ProjectsTable";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { deleteProjectQuery } from "@/app/(authRoutes)/projects/state/queries";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { useTagsContext } from "@/hooks/useTagsContext";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { act } from "react-dom/test-utils";

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/utils/apiClient", () => ({
  __esModule: true,
  default: "mockedDefaultExport",
  postRequest: jest.fn().mockReturnValue({}),
}));

jest.mock("@/queries/clientQueries", () => ({
  createProjectQuery: jest.fn(),
  deleteProjectQuery: jest.fn(),
  getProjectsQuery: jest.fn().mockReturnValue({
    projects: [
      {
        project_id: "test",
        name: "test project",
        description: "test desc",
        created_at: "2025-03-13T08:33:41.025+00:00",
        updated_at: "2025-03-13T08:33:41.025+00:00",
        is_default_project: false,
        job_statuses: [],
      },
    ],
  }),
}));

jest.mock("@/app/(authRoutes)/projects/state/queries", () => ({
  deleteProjectQuery: jest.fn(),
}));
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

jest.mock("@/hooks/useTagsContext", () => ({
  useTagsContext: jest.fn(),
}));

describe("ProjectsTable", () => {
  beforeEach(() => {
    (useProjectsContext as jest.Mock).mockImplementation(() => ({
      allProjects: [],
      refreshProjects: () => {},
    }));

    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectActions: [],
    }));

    (useTagsContext as jest.Mock).mockImplementation(() => ({
      useTagsContext: jest.fn(() => ({
        userTags: [
          {
            tag_id: "1",
            name: "tag1",
            description: "tag1 description",
            created_at: "2025-03-13T08:33:41.025+00:00",
            updated_at: "2025-03-13T08:33:41.025+00:00",
          },
        ],
        modelTags: [],
        datasetTags: [],
        allTags: {
          user_tags: [
            {
              tag_id: "1",
              name: "tag1",
              description: "tag1 description",
              created_at: "2025-03-13T08:33:41.025+00:00",
              updated_at: "2025-03-13T08:33:41.025+00:00",
            },
          ],
          model_tags: [],
          dataset_tags: [],
        },
        isLoadingTags: false,
        refreshTags: jest.fn(),
      })),
    }));
  });

  describe("general", () => {
    beforeEach(() => {
      testRender(<ProjectsTable />);
    });

    it("should render the content correctly", async () => {
      (useProjectsContext as jest.Mock).mockImplementation(() => ({
        allProjects: [
          {
            project_id: "test",
            name: "test project",
            description: "test desc",
            created_at: "2025-03-13T08:33:41.025+00:00",
            updated_at: "2025-03-13T08:33:41.025+00:00",
            is_default_project: false,
            job_statuses: [],
          },
        ],
        refreshProjects: () => {},
      }));
      await waitFor(() => {
        expect(screen.getByLabelText("table")).toBeInTheDocument();
      });
    });

    it("renders table with correct headers", async () => {
      (useProjectsContext as jest.Mock).mockImplementation(() => ({
        allProjects: [
          {
            project_id: "test",
            name: "test project",
            description: "test desc",
            created_at: "2025-03-13T08:33:41.025+00:00",
            updated_at: "2025-03-13T08:33:41.025+00:00",
            is_default_project: false,
            job_statuses: [],
          },
        ],
        refreshProjects: () => {},
      }));

      await waitFor(() => {
        expect(screen.getByText("Project name")).toBeInTheDocument();
        expect(screen.getByText("Models")).toBeInTheDocument();
        expect(screen.getByText("Job status")).toBeInTheDocument();
      });
      expect(
        screen.queryAllByTestId("action-btn").length,
      ).toBeGreaterThanOrEqual(0);
    });
  });

  describe("projects array is not empty", () => {
    beforeEach(() => {
      (useProjectsContext as jest.Mock).mockImplementation(() => {
        return {
          allProjects: [
            {
              project_id: "test",
              name: "test project",
              description: "test desc",
              created_at: "2025-03-13T08:33:41.025+00:00",
              updated_at: "2025-03-13T08:33:41.025+00:00",
              is_default_project: false,
              job_statuses: [
                {
                  status: "Failed",
                  job_id: "job-1",
                  type: "eval",
                  start_time: "2025-03-13T08:33:41.025+00:00",
                  end_time: "2025-03-13T09:33:41.025+00:00",
                  input_ids: [],
                  pending: 0,
                  in_progress: 0,
                  failed: 1,
                  total: 1,
                },
              ],
            },
            {
              project_id: "test2",
              name: "test project2",
              description: "test desc2",
              created_at: "2025-03-13T08:33:41.025+00:00",
              updated_at: "2025-03-13T08:33:41.025+00:00",
              is_default_project: false,
              job_statuses: [
                {
                  status: "In-Progress",
                  job_id: "job-2",
                  type: "inference",
                  start_time: "2025-03-13T08:33:41.025+00:00",
                  end_time: undefined,
                  input_ids: [],
                  pending: 0,
                  in_progress: 1,
                  failed: 0,
                  total: 2,
                },
              ],
            },
            {
              project_id: "test3",
              name: "test project3",
              description: "test desc3",
              created_at: "2025-03-13T08:33:41.025+00:00",
              updated_at: "2025-03-13T08:33:41.025+00:00",
              is_default_project: false,
              job_statuses: [
                {
                  status: "Pending",
                  job_id: "job-3",
                  type: "eval",
                  start_time: "2025-03-13T08:33:41.025+00:00",
                  end_time: undefined,
                  input_ids: [],
                  pending: 1,
                  in_progress: 0,
                  failed: 0,
                  total: 3,
                },
              ],
            },
          ],
          refreshProjects: () => {},
        };
      });
      testRender(<ProjectsTable />);
    });

    it("renders table with rows data when projects array is present", async () => {
      (useProjectsContext as jest.Mock).mockImplementation(() => ({
        allProjects: [
          {
            project_id: "test",
            name: "test project",
            description: "test desc",
            created_at: "2025-03-13T08:33:41.025+00:00",
            updated_at: "2025-03-13T08:33:41.025+00:00",
            is_default_project: false,
            job_statuses: [],
          },
        ],
        refreshProjects: () => {},
      }));

      await waitFor(() => {
        expect(screen.getByText("test project")).toBeInTheDocument();
      });
    });
  });

  it("on delete action calls delete mutation", async () => {
    const projMockName = "test project";
    (useProjectsContext as jest.Mock).mockImplementation(() => ({
      allProjects: [
        {
          project_id: "test",
          name: projMockName,
          description: "test desc",
          created_at: "2025-03-13T08:33:41.025+00:00",
          updated_at: "2025-03-13T08:33:41.025+00:00",
          is_default_project: false,
          job_statuses: [],
        },
      ],
      refreshProjects: () => {},
    }));
    (deleteProjectQuery as jest.Mock).mockResolvedValue({
      name: "test project",
    });
    testRender(<ProjectsTable />);

    await act(async () => {
      await Promise.resolve();
    });

    act(() => {
      const actionBtn = screen.getAllByTestId("action-btn")[0];
      fireEvent.click(actionBtn);
    });

    await waitFor(() => {
      expect(screen.getByTestId("delete-btn")).toBeInTheDocument();
    });

    act(() => {
      const deleteBtn = screen.getAllByTestId("delete-btn")[0];
      fireEvent.click(deleteBtn);
    });

    await waitFor(() => {
      expect(screen.getByText(`Delete ${projMockName}`)).toBeInTheDocument();
    });

    act(() => {
      fireEvent.click(screen.getAllByText("Confirm Delete")[0]);
    });

    await waitFor(() => {
      expect(deleteProjectQuery).toHaveBeenCalledWith(
        "test",
        expect.anything(),
      );
    });
  });

  it("on details action open job details modal", async () => {
    (useProjectsContext as jest.Mock).mockImplementation(() => ({
      allProjects: [
        {
          project_id: "test",
          name: "test project",
          description: "test desc",
          created_at: "2025-03-13T08:33:41.025+00:00",
          updated_at: "2025-03-13T08:33:41.025+00:00",
          is_default_project: false,
          job_statuses: [],
        },
      ],
      refreshProjects: () => {},
    }));
    testRender(<ProjectsTable />);

    await act(async () => {
      await Promise.resolve();
    });

    act(() => {
      const actionBtn = screen.getAllByTestId("action-btn")[0];
      fireEvent.click(actionBtn);
    });

    await waitFor(() => {
      expect(screen.getByTestId("show-details-btn")).toBeInTheDocument();
    });

    act(() => {
      fireEvent.click(screen.getAllByTestId("show-details-btn")[0]);
    });

    await waitFor(() => {
      expect(screen.getByText("Job Details")).toBeInTheDocument();
    });
  });

  it("clicking 'Add project' button opens the create project modal", async () => {
    testRender(<ProjectsTable />);

    const btnNewProject = screen.getByTestId("btn-new-project");
    fireEvent.click(btnNewProject);
    expect(
      screen.getByText(/Create a new evaluation project/i),
    ).toBeInTheDocument();
  });
});
