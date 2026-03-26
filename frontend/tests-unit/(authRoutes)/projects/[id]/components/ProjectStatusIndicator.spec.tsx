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

import ProjectStatusIndicator from "@/app/(authRoutes)/projects/[id]/components/ProjectStatusIndicator";
import { testRender, testRenderLite } from "@/tests-unit/render";
import { JobDetailStatus, Project } from "@/types";
import "@testing-library/jest-dom";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@/components/Chip", () => ({
  __esModule: true,
  default: ({ label, icon }: any) => (
    <div data-testid="chip">
      {icon}
      <span>{label}</span>
    </div>
  ),
}));

jest.mock("@/app/(authRoutes)/projects/components/JobDetailsContent", () => ({
  __esModule: true,
  default: () => <div data-testid="job-details">Job Details Content</div>,
}));

const baseProject: Project = {
  id: "1",
  name: "Test Project",
  total_job_tasks: 10,
  finished_job_tasks: 5,
} as unknown as Project;

const createProps = (
  status?: JobDetailStatus,
  overrides: Partial<Project> = {},
) => ({
  project: { ...baseProject, ...overrides },
  firstNonCompletedJob: status
    ? {
        id: "job-1",
        status,
        job_id: "job-1",
        type: "some_type",
        start_time: "2023-01-01T00:00:00Z",
        input_ids: [],
        pending: 1,
        in_progress: 0,
        total: 1,
      }
    : undefined,
});

describe("ProjectStatusIndicator", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it("renders COMPLETED icon and correct label", () => {
    testRender(
      <ProjectStatusIndicator {...createProps(JobDetailStatus.COMPLETED)} />,
    );
    expect(screen.getByTestId("chip")).toBeInTheDocument();
    expect(screen.getByText("5/10")).toBeInTheDocument();
    expect(screen.getByText("check_circle")).toBeInTheDocument();
  });

  it("renders IN_PROGRESS loader icon", () => {
    testRender(
      <ProjectStatusIndicator {...createProps(JobDetailStatus.IN_PROGRESS)} />,
    );
    expect(screen.getByTestId("chip")).toBeInTheDocument();
    expect(screen.getByText("5/10")).toBeInTheDocument();
  });

  it("renders FAILED icon", () => {
    testRender(
      <ProjectStatusIndicator {...createProps(JobDetailStatus.FAILED)} />,
    );
    expect(screen.getByTestId("chip")).toBeInTheDocument();
    expect(screen.getByText("5/10")).toBeInTheDocument();
    expect(screen.getByText("error")).toBeInTheDocument();
  });

  it("renders PENDING icon", () => {
    testRender(
      <ProjectStatusIndicator {...createProps(JobDetailStatus.PENDING)} />,
    );
    expect(screen.getByTestId("chip")).toBeInTheDocument();
    expect(screen.getByText("5/10")).toBeInTheDocument();
    expect(screen.getByText("schedule")).toBeInTheDocument();
  });

  it("renders nothing if no status is provided", () => {
    testRender(<ProjectStatusIndicator {...createProps(undefined)} />);
    expect(screen.queryByTestId("chip")).not.toBeInTheDocument();
  });

  it("renders nothing if finished or total are missing", () => {
    testRender(
      <ProjectStatusIndicator
        {...createProps(JobDetailStatus.COMPLETED, {
          total_job_tasks: undefined,
          finished_job_tasks: undefined,
        })}
      />,
    );
    expect(screen.queryByTestId("chip")).not.toBeInTheDocument();
  });

  it("shows hover card on mouse over", async () => {
    testRenderLite(
      <ProjectStatusIndicator {...createProps(JobDetailStatus.PENDING)} />,
    );
    fireEvent.mouseOver(screen.getByText("5/10"));
    await waitFor(() => {
      expect(screen.getByTestId("job-details")).toBeInTheDocument();
    });
  });

  it("supports 0 finished out of 0 total edge case", () => {
    testRender(
      <ProjectStatusIndicator
        {...createProps(JobDetailStatus.IN_PROGRESS, {
          total_job_tasks: 0,
          finished_job_tasks: 0,
        })}
      />,
    );
    expect(screen.getByTestId("chip")).toBeInTheDocument();
    expect(screen.getByText("0/0")).toBeInTheDocument();
  });

  it("renders correctly when finished is 0 and total is defined", () => {
    testRender(
      <ProjectStatusIndicator
        {...createProps(JobDetailStatus.PENDING, {
          total_job_tasks: 10,
          finished_job_tasks: 0,
        })}
      />,
    );
    expect(screen.getByTestId("chip")).toBeInTheDocument();
    expect(screen.getByText("0/10")).toBeInTheDocument();
    expect(screen.getByText("schedule")).toBeInTheDocument();
  });
});
