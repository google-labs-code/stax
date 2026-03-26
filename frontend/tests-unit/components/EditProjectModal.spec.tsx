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

import EditProjectModal from "@/components/EditProjectModal";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { testRender } from "@/tests-unit/render";
import { Project, ProjectType } from "@/types";
import { useMutation } from "@tanstack/react-query";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

jest.mock("@tanstack/react-query", () => ({
  ...jest.requireActual("@tanstack/react-query"),
  useMutation: jest.fn(),
}));

describe("EditProjectModal", () => {
  const mockProjects: Project[] = [
    {
      project_id: "project-1",
      name: "Test Project",
      type: ProjectType.POINTWISE,
      description: "Test Description",
      created_at: "2023-01-01",
      updated_at: "2023-01-01",
      is_default_project: false,
      job_statuses: [],
      providers: [],
      inference_monitoring_summary: {
        average_turn_time_taken: 0,
        total_completion_tokens: 0,
        total_inferences: 0,
        total_prompt_tokens: 0,
        total_tokens: 0,
      },
      finished_job_tasks: 0,
      total_job_tasks: 0,
    },
    {
      project_id: "project-2",
      name: "Another Project",
      type: ProjectType.SIDE_BY_SIDE,
      description: "Another Description",
      created_at: "2023-01-02",
      updated_at: "2023-01-02",
      is_default_project: false,
      job_statuses: [],
      providers: [],
      inference_monitoring_summary: {
        average_turn_time_taken: 0,
        total_completion_tokens: 0,
        total_inferences: 0,
        total_prompt_tokens: 0,
        total_tokens: 0,
      },
      finished_job_tasks: 0,
      total_job_tasks: 0,
    },
  ];

  const mockRefreshProjects = jest.fn();
  const mockMutate = jest.fn();
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (useProjectsContext as jest.Mock).mockReturnValue({
      allProjects: mockProjects,
      refreshProjects: mockRefreshProjects,
      isLoadingProjects: false,
    });

    (useMutation as jest.Mock).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    });
  });

  it("renders the modal when opened", () => {
    testRender(
      <EditProjectModal
        isOpened={true}
        onClose={mockOnClose}
        projectId={null}
      />,
    );

    expect(
      screen.getByText("Edit project name and description"),
    ).toBeInTheDocument();
  });

  it("loads project data when projectId is provided", async () => {
    testRender(
      <EditProjectModal
        isOpened={true}
        onClose={mockOnClose}
        projectId="project-1"
      />,
    );

    await waitFor(() => {
      const nameInput = screen.getByPlaceholderText("Name of the project");
      expect(nameInput).toHaveValue("Test Project");

      const descriptionInput = screen.getByPlaceholderText(
        "Add a description for your project here",
      );
      expect(descriptionInput).toHaveValue("Test Description");
    });
  });

  it("updates project description when changed", async () => {
    testRender(
      <EditProjectModal
        isOpened={true}
        onClose={mockOnClose}
        projectId="project-1"
      />,
    );

    await waitFor(() => {
      expect(screen.getByPlaceholderText("Name of the project")).toHaveValue(
        "Test Project",
      );
    });

    const descriptionInput = screen.getByPlaceholderText(
      "Add a description for your project here",
    );
    fireEvent.change(descriptionInput, {
      target: { value: "Updated Description" },
    });

    expect(descriptionInput).toHaveValue("Updated Description");
  });

  it("submits form when update button is clicked", async () => {
    testRender(
      <EditProjectModal
        isOpened={true}
        onClose={mockOnClose}
        projectId="project-1"
      />,
    );

    await waitFor(() => {
      expect(screen.getByPlaceholderText("Name of the project")).toHaveValue(
        "Test Project",
      );
    });

    const updateButton = screen.getByText("Update");
    fireEvent.click(updateButton);

    expect(mockMutate).toHaveBeenCalledWith(
      expect.objectContaining({
        project_id: "project-1",
        name: "Test Project",
        description: "Test Description",
      }),
    );
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("disables update button when name is empty", async () => {
    testRender(
      <EditProjectModal
        isOpened={true}
        onClose={mockOnClose}
        projectId="project-1"
      />,
    );

    await waitFor(() => {
      expect(screen.getByPlaceholderText("Name of the project")).toHaveValue(
        "Test Project",
      );
    });

    const nameInput = screen.getByPlaceholderText("Name of the project");
    fireEvent.change(nameInput, { target: { value: "" } });

    const updateButton = screen.getByText("Update").closest("button");
    expect(updateButton).toBeDisabled();
  });

  it("shows tooltip when name is empty and hovering over button", async () => {

    testRender(
      <EditProjectModal
        isOpened={true}
        onClose={mockOnClose}
        projectId="project-1"
      />,
    );

    await waitFor(() => {
      expect(screen.getByPlaceholderText("Name of the project")).toHaveValue(
        "Test Project",
      );
    });

    const nameInput = screen.getByPlaceholderText("Name of the project");
    fireEvent.change(nameInput, { target: { value: "" } });

    const updateButton = screen.getByText("Update");
    expect(updateButton).toBeInTheDocument();

    expect(mockMutate).not.toHaveBeenCalled();
  });

  it("shows loader when projects are loading", () => {
    (useProjectsContext as jest.Mock).mockReturnValue({
      allProjects: [],
      refreshProjects: mockRefreshProjects,
      isLoadingProjects: true,
    });

    testRender(
      <EditProjectModal
        isOpened={true}
        onClose={mockOnClose}
        projectId={null}
      />,
    );

    expect(
      screen.getByText("Edit project name and description"),
    ).toBeInTheDocument();

    const loader = document.querySelector(".mantine-Loader-root");
    expect(loader).toBeInTheDocument();
  });
});
