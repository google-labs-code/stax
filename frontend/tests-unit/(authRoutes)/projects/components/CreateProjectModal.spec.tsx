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

import CreateProjectModal from "@/app/(authRoutes)/projects/components/CreateProjectModal";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { createProjectQuery } from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { GAevents, Project, ProjectType } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { useRouter } from "next/navigation";

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));
jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));
jest.mock("@/queries/clientQueries", () => ({
  createProjectQuery: jest.fn(),
}));
jest.mock("@/utils/logGAevent", () => jest.fn());

describe("CreateProjectModal", () => {
  const mockRefreshProjects = jest.fn();
  const mockPush = jest.fn();
  const onClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useProjectsContext as jest.Mock).mockReturnValue({
      refreshProjects: mockRefreshProjects,
    });
    (useProjectContext as jest.Mock).mockReturnValue({
      projectActions: {
        resetProjectData: jest.fn(),
      },
    });
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });
  });

  const setup = (isOpened = true) => {
    testRender(<CreateProjectModal isOpened={isOpened} onClose={onClose} />);
  };

  it("renders two action cards", () => {
    setup();
    const cards = screen.getAllByTestId("action-card");
    expect(cards).toHaveLength(2);
    expect(screen.getByText("Pointwise Evaluation")).toBeInTheDocument();
    expect(screen.getByText("Side-by-Side Comparison")).toBeInTheDocument();
  });

  it("calls logGAevent & mutate & refresh and routes on Create for a card", async () => {
    const fakeProject: Project = {
      project_id: "proj-123",
      name: "Test",
      type: ProjectType.POINTWISE,
    } as any;

    (createProjectQuery as jest.Mock).mockResolvedValue(fakeProject);

    setup();

    const btn = screen.getAllByRole("button", { name: /create project/i })[0];
    fireEvent.click(btn);

    expect(logGAevent).toHaveBeenCalledWith(GAevents.CREATE_PROJECT, {
      type: ProjectType.POINTWISE,
    });

    await waitFor(() => {
      expect(mockRefreshProjects).toHaveBeenCalled();
    });
    expect(mockPush).toHaveBeenCalledWith(
      `/projects/${fakeProject.project_id}`,
    );
  });

  it("disables button while loading", async () => {
    (createProjectQuery as jest.Mock).mockImplementation(
      () => new Promise(() => {}),
    );

    setup();

    const btn = screen.getAllByRole("button", { name: /create project/i })[0];
    fireEvent.click(btn);
    expect(btn).not.toBeDisabled();
  });

  it("does nothing when modal is closed", () => {
    setup(false);
    expect(screen.queryByTestId("action-card")).toBeNull();
  });
});
