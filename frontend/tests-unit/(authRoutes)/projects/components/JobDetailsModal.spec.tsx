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

import JobDetailsModal from "@/app/(authRoutes)/projects/components/JobDetailsModal";
import { testRender } from "@/tests-unit/render";
import { JobDetailStatus } from "@/types";
import "@testing-library/jest-dom";
import { screen } from "@testing-library/react";

describe("JobDetailsModal", () => {
  const title = "Job Details";
  const mockOnClose = jest.fn();
  const mockProjectData = {
    project_id: "123",
    name: "Test Project",
    description: "A test project",
    created_at: "2021-01-01T00:00:00Z",
    updated_at: "2021-01-01T00:00:00Z",
    is_default_project: false,
    job_statuses: [
      {
        status: JobDetailStatus.PENDING,
        job_id: "job-1",
        type: "eval",
        start_time: "2021-01-01T00:00:00Z",
        end_time: "2021-01-01T01:00:00Z",
        input_ids: [],
        pending: 1,
        in_progress: 0,
        total: 1,
      },
      {
        status: JobDetailStatus.IN_PROGRESS,
        job_id: "job-2",
        type: "inference",
        start_time: "2021-01-02T00:00:00Z",
        end_time: undefined,
        input_ids: [],
        pending: 0,
        in_progress: 1,
        total: 2,
      },
    ],
  };

  it("Should render modal when isOpened is true", () => {
    testRender(
      <JobDetailsModal
        isOpened
        onClose={mockOnClose}
        projectData={mockProjectData}
      />,
    );

    const modal = screen.getByText(title);
    expect(modal).toBeInTheDocument();
  });

  it("Should not render modal when isOpened is false", () => {
    testRender(
      <JobDetailsModal
        isOpened={false}
        onClose={mockOnClose}
        projectData={mockProjectData}
      />,
    );

    const modal = screen.queryByText(title);
    expect(modal).not.toBeInTheDocument();
  });

  it("All content is present when opened", () => {
    testRender(
      <JobDetailsModal
        isOpened
        onClose={mockOnClose}
        projectData={mockProjectData}
      />,
    );

    expect(screen.getAllByText("Job started").length).toBe(2);
    expect(screen.getAllByText("Job type").length).toBe(2);
    expect(screen.getAllByText("Job status").length).toBeGreaterThanOrEqual(2);
    expect(screen.getAllByText("Job ended").length).toBe(1);

    expect(screen.getByText("Evaluation")).toBeInTheDocument();
    expect(screen.getByText("Inference")).toBeInTheDocument();
    expect(screen.getByText("pending: 1")).toBeInTheDocument();
    expect(screen.getByText("in-progress: 1")).toBeInTheDocument();
  });
});
