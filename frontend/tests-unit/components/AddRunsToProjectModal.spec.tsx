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

import AddRunsToProjectModal from "@/components/AddRunsToProjectModal";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { useChatContext } from "@/hooks/useChatContext";
import { useDatasetsContext } from "@/hooks/useDatasetsContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import * as clientQueries from "@/queries/clientQueries";
import { notifications } from "@mantine/notifications";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, screen, waitFor } from "@testing-library/react";

import { testRender } from "../render";

jest.mock("@/hooks/useProjectsContext");
jest.mock("@/hooks/useDatasetsContext");
jest.mock("@/hooks/useChatContext");
jest.mock("@/queries/clientQueries");
jest.mock("@mantine/notifications");

const mockedUseProjectsContext = useProjectsContext as jest.Mock;
const mockedUseDatasetsContext = useDatasetsContext as jest.Mock;
const mockedUseChatContext = useChatContext as jest.Mock;

const queryClient = new QueryClient();

describe("AddRunsToProjectModal", () => {
  const allProjectsMock = [
    { project_id: "1", name: "Project A", is_default_project: false },
    { project_id: "2", name: "Project B", is_default_project: true },
    { project_id: "3", name: "Project C", is_default_project: false },
  ];
  const allDatasetsMock = [
    { id: "10", name: "Dataset A" },
    { id: "11", name: "Dataset B" },
  ];
  const chatsMock = [{ id: "chat1" }, { id: "chat2" }];
  const onClose = jest.fn();
  const onCreateNewProjectClick = jest.fn();
  const setSelectedProject = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockedUseProjectsContext.mockReturnValue({ allProjects: allProjectsMock });
    mockedUseDatasetsContext.mockReturnValue({
      allDatasets: allDatasetsMock,
      isLoadingDatasets: false,
    });
    mockedUseChatContext.mockReturnValue({ chats: chatsMock });
  });

  const renderComponent = (props = {}) =>
    testRender(
      <QueryClientProvider client={queryClient}>
        <AddRunsToProjectModal
          isOpened={true}
          onClose={onClose}
          onCreateNewProjectClick={onCreateNewProjectClick}
          selectedProject={null}
          setSelectedProject={setSelectedProject}
          sourceProjectId="1"
          {...props}
        />
      </QueryClientProvider>,
    );

  it("renders modal and project options, excluding default and source project", async () => {
    renderComponent();
    expect(screen.getByText("Add data to a project")).toBeInTheDocument();
    expect(screen.getByText("Select from projects")).toBeInTheDocument();

    fireEvent.click(
      screen.getByRole("button", { name: /select from projects/i }),
    );

    await waitFor(() => {
      expect(screen.getByText("Project C")).toBeInTheDocument();
    });

    expect(screen.queryByText("Project B")).not.toBeInTheDocument();
    expect(screen.queryByText("Project A")).not.toBeInTheDocument();

    fireEvent.click(screen.getByText("Project C"));
    expect(setSelectedProject).toHaveBeenCalledWith({
      value: "3",
      label: "Project C",
    });
  });

  it("renders dataset mode when transferToDataset true", async () => {
    renderComponent({ transferToDataset: true });
    expect(screen.getByText("Add data to a dataset")).toBeInTheDocument();
    expect(screen.getByText("Select from datasets")).toBeInTheDocument();

    fireEvent.click(
      screen.getByRole("button", { name: /select from datasets/i }),
    );

    await waitFor(() => {
      expect(screen.getByText("Dataset A")).toBeInTheDocument();
      expect(screen.getByText("Dataset B")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("Dataset B"));
    expect(setSelectedProject).toHaveBeenCalledWith({
      value: "11",
      label: "Dataset B",
    });
  });

  it("fires onCreateNewProjectClick for project and dataset modes", () => {
    renderComponent();
    fireEvent.click(screen.getByText("Create new project"));
    expect(onCreateNewProjectClick).toHaveBeenCalled();

    renderComponent({ transferToDataset: true });
    fireEvent.click(screen.getByText("Create new dataset"));
    expect(onCreateNewProjectClick).toHaveBeenCalled();
  });

  it("shows loading text when datasets loading", () => {
    mockedUseDatasetsContext.mockReturnValue({
      allDatasets: [],
      isLoadingDatasets: true,
    });
    renderComponent({ transferToDataset: true });

    fireEvent.click(
      screen.getByRole("button", { name: /select from datasets/i }),
    );
    expect(screen.getByText("Loading datasets...")).toBeInTheDocument();
  });

  it("shows no projects or datasets when empty", () => {
    mockedUseProjectsContext.mockReturnValue({ allProjects: [] });
    renderComponent();
    fireEvent.click(
      screen.getByRole("button", { name: /select from projects/i }),
    );
    expect(screen.getByText("No projects found")).toBeInTheDocument();

    mockedUseDatasetsContext.mockReturnValue({
      allDatasets: [],
      isLoadingDatasets: false,
    });
    renderComponent({ transferToDataset: true });
    fireEvent.click(
      screen.getByRole("button", { name: /select from datasets/i }),
    );
    expect(screen.getByText("No datasets found")).toBeInTheDocument();
  });

  it("uses dataTransferQuery when specific chatIds provided", async () => {
    (clientQueries.dataTransferQuery as jest.Mock).mockResolvedValue({});
    const selProject = { value: "3", label: "Project C" };
    renderComponent({
      selectedProject: selProject,
      selectedWorkbookChatIds: ["c1", "c2"],
    });

    fireEvent.click(screen.getByText("Add to project"));
    await waitFor(() => {
      expect(clientQueries.dataTransferQuery).toHaveBeenCalledWith({
        chat_ids: ["c1", "c2"],
        source_id: "1",
        source_type: "PROJECT",
        target_id: "3",
        target_type: "PROJECT",
      });
      const notifConfig = getSuccessNotificationConfig(
        `Data is added to the project ${selProject.label} successfully.`,
        "data-added-success",
      );
      expect(notifications.show).toHaveBeenCalledWith(notifConfig);
      expect(onClose).toHaveBeenCalled();
    });
  });

  it("uses dataTransferAllQuery when useDataTransferAll true", async () => {
    (clientQueries.dataTransferAllQuery as jest.Mock).mockResolvedValue({});
    const selProject = { value: "3", label: "Project C" };
    renderComponent({ selectedProject: selProject, useDataTransferAll: true });

    fireEvent.click(screen.getByText("Add to project"));
    await waitFor(() => {
      expect(clientQueries.dataTransferAllQuery).toHaveBeenCalledWith({
        source_id: "1",
        source_type: "PROJECT",
        target_id: "3",
        target_type: "PROJECT",
      });
      const notifConfig = getSuccessNotificationConfig(
        "Data transferred successfully.",
        "data-transferred-success",
      );
      expect(notifications.show).toHaveBeenCalledWith(notifConfig);
      expect(onClose).toHaveBeenCalled();
    });
  });
});
