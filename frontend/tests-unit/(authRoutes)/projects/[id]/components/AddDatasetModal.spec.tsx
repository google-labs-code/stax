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

import AddDatasetModal from "@/app/(authRoutes)/projects/[id]/components/AddDatasetModal";
import * as ProjectContextModule from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { getDatasetsQuery } from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen, waitFor } from "@testing-library/react";

const projectState = {
  project: {
    project_id: "project-1365d713-6422-47f0-b995-480ada786d4e",
    name: "Quick Compare History",
    type: "POINTWISE",
    description:
      "This project will by default contain all quick compare history.",
    created_at: "2025-07-22T08:19:43.128+00:00",
    updated_at: "2025-07-22T08:19:43.128+00:00",
    is_default_project: true,
    providers: ["GOOGLE"],
  },
  activeStep: 1,
  metricsData: {
    total_inferences: 283,
    average_turn_time_taken: 6421.614840989399,
    total_prompt_tokens: 124073,
    total_completion_tokens: 84074,
    total_tokens: 364625,
  },
  humanEvalPassRate: {
    passRate: 0.6666666666666666,
    likes: 6,
    dislikes: 3,
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
};

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

jest.mock("@/queries/clientQueries", () => ({
  getDatasetsQuery: jest.fn(),
  dataTransferAllQuery: jest.fn(),
}));

jest.mock("next/navigation", () => ({
  useParams: () => ({ id: "project-id" }),
}));

const mockedUseProjectContext =
  ProjectContextModule.useProjectContext as jest.Mock;

describe("AddDatasetModal", () => {
  beforeEach(() => {
    mockedUseProjectContext.mockReturnValue({
      projectState,
    });
  });

  const mockOnClose = jest.fn();
  const mockOnImport = jest.fn();

  it("should render modal when isOpened is true", () => {
    testRender(
      <AddDatasetModal
        isOpened={true}
        onClose={mockOnClose}
        onImport={mockOnImport}
        onUploadCSV={jest.fn()}
        importedDataset={null}
      />,
    );

    const modal = screen.getByText(/import a dataset to your project/i);
    expect(modal).toBeInTheDocument();
  });

  it("should not render modal when isOpened is false", () => {
    testRender(
      <AddDatasetModal
        isOpened={false}
        onClose={mockOnClose}
        onImport={mockOnImport}
        onUploadCSV={jest.fn()}
        importedDataset={null}
      />,
    );

    const modal = screen.queryByText(/import a dataset to your project/i);
    expect(modal).not.toBeInTheDocument();
  });

  it("should call onClose when modal is closed", () => {
    testRender(
      <AddDatasetModal
        isOpened={true}
        onClose={mockOnClose}
        onImport={mockOnImport}
        onUploadCSV={jest.fn()}
        importedDataset={null}
      />,
    );

    const closeButton = screen.getByLabelText("close button");
    fireEvent.click(closeButton);

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it("should call onImport when Import button is clicked", async () => {
    const mockDataset = {
      id: "ds-1",
      name: "Dataset One",
      created_at: "2025-11-11",
      updated_at: "2025-11-11",
    };

    (getDatasetsQuery as jest.Mock).mockResolvedValue({
      user_data_sets: [mockDataset],
    });

    testRender(
      <AddDatasetModal
        isOpened={true}
        onClose={mockOnClose}
        onImport={mockOnImport}
        onUploadCSV={jest.fn()}
        importedDataset={mockDataset}
      />,
    );

    await waitFor(() => {
      expect(screen.getByTestId("active-import-btn")).toBeInTheDocument();
    });

    const importButton = screen.getByText("Import");
    fireEvent.click(importButton);

    await waitFor(() => {
      expect(mockOnImport).toHaveBeenCalledTimes(1);
    });
  });
});

const mockDatasets = [
  { id: "ds-1", name: "Dataset One" },
  { id: "ds-2", name: "Dataset Two" },
];

describe("AddDatasetModal", () => {
  const onClose = jest.fn();
  const onImport = jest.fn();
  const onUploadCSV = jest.fn();
  const isCsvUploadRef = { current: false };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("fetches datasets and displays them when modal is opened", async () => {
    (getDatasetsQuery as jest.Mock).mockResolvedValue({
      user_data_sets: mockDatasets,
    });

    testRender(
      <AddDatasetModal
        isOpened={true}
        onClose={onClose}
        onUploadCSV={onUploadCSV}
        onImport={onImport}
        importedDataset={null}
        isCsvUploadRef={isCsvUploadRef}
      />,
    );

    await waitFor(() => {
      expect(screen.getByText("Dataset One")).toBeInTheDocument();
      expect(screen.getByText("Dataset Two")).toBeInTheDocument();
    });
  });

  it("disables import button if no dataset is selected", async () => {
    (getDatasetsQuery as jest.Mock).mockResolvedValue({
      user_data_sets: mockDatasets,
    });

    testRender(
      <AddDatasetModal
        isOpened={true}
        onClose={onClose}
        onUploadCSV={onUploadCSV}
        onImport={onImport}
        importedDataset={null}
        isCsvUploadRef={isCsvUploadRef}
      />,
    );

    await waitFor(() => {
      expect(screen.getByRole("button", { name: /import/i })).toBeDisabled();
    });
  });

  it("calls onUploadCSV when Upload CSV is clicked", () => {
    testRender(
      <AddDatasetModal
        isOpened={true}
        onClose={onClose}
        onUploadCSV={onUploadCSV}
        onImport={onImport}
        importedDataset={null}
        isCsvUploadRef={isCsvUploadRef}
      />,
    );

    fireEvent.click(screen.getByText(/upload csv/i));
    expect(onUploadCSV).toHaveBeenCalled();
  });
});
