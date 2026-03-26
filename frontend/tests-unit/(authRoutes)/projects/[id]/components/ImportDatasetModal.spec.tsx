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

import ImportDatasetModal from "@/components/ImportDatasetModal";
import * as clientQueries from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import {
  DatasetUploadFormData,
  ProjectType,
  UploadDatasetModalSource,
} from "@/types";
import { notifications } from "@mantine/notifications";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { ParseResult } from "papaparse";

jest.mock("@mantine/notifications", () => ({
  notifications: { show: jest.fn() },
}));

jest.mock("next/navigation", () => ({
  useParams: () => ({ id: "test-project-id" }),
}));

jest.mock("@/queries/clientQueries", () => ({
  importDatasetQuery: jest.fn().mockResolvedValue({ successfulRows: 1 }),
  importSxsProjectQuery: jest.fn().mockResolvedValue({ successfulRows: 1 }),
  dataTransferAllQuery: jest.fn().mockResolvedValue({}),
}));

jest.mock("@/components/TruncatedTextWithPopover", () => ({
  __esModule: true,
  default: ({ text }: { text: string }) => (
    <div data-testid="truncated-text">{text}</div>
  ),
}));

jest.mock("@/components/MaterialIcon", () => ({
  __esModule: true,
  default: ({
    name,
    tooltipLabel,
  }: {
    name: string;
    tooltipLabel?: string;
  }) => (
    <div data-testid={`icon-${name}`}>
      {name}
      {tooltipLabel && <div data-testid="tooltip">{tooltipLabel}</div>}
    </div>
  ),
}));

describe("ImportDatasetModal", () => {
  const mockOnClose = jest.fn();
  const mockOnSuccess = jest.fn();

  const dummyFile = new File(["name,age\nJohn,30"], "test.csv", {
    type: "text/csv",
  });

  const parsedFileResults: ParseResult<any> = {
    data: [
      { name: "John", age: "30" },
      { name: "Jane", age: "25" },
    ],
    errors: [],
    meta: {
      fields: ["name", "age"],
      delimiter: ",",
      linebreak: "\n",
      aborted: false,
      truncated: false,
      cursor: 0,
    },
  };

  const formData: DatasetUploadFormData = {
    inputColumn: "",
    outputColumn: "",
    expectedOutput: "",
    tagsColumn: "",
    systemInstructionColumn: "",
    modelLabelColumn: "",
    humanEvalScoreColumn: "",
    humanEvalScoreNotesColumn: "",
    chatColumn: "",
    llmEvaluationsColumn: "",
    inferenceAnalyticsColumn: "",
    file: null,
    name: "",
    description: "",
    chatAColumn: "",
    chatBColumn: "",
    outputColumnA: "",
    outputColumnB: "",
    systemInstructionColumnA: "",
    systemInstructionColumnB: "",
    modelLabelColumnA: "",
    modelLabelColumnB: "",
    llmEvaluationsColumnA: "",
    llmEvaluationsColumnB: "",
    inferenceAnalyticsColumnA: "",
    inferenceAnalyticsColumnB: "",
    variablesColumn: "",
  };

  const defaultProps = {
    isOpened: true,
    onClose: mockOnClose,
    file: dummyFile,
    parsedFileResults,
    formData,
    onSuccess: mockOnSuccess,
    tooltipLabel: "Map your dataset columns",
    source: UploadDatasetModalSource.PROJECT,
    projectType: ProjectType.POINTWISE,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders modal with file name and columns", () => {
    testRender(<ImportDatasetModal {...defaultProps} />);

    expect(screen.getByText("Import a dataset")).toBeInTheDocument();
    expect(screen.getByTestId("icon-draft")).toBeInTheDocument();
    expect(screen.getByText("name")).toBeInTheDocument();
    expect(screen.getByText("age")).toBeInTheDocument();
    expect(screen.getAllByRole("button", { name: /map column/i })).toHaveLength(
      2,
    );
  });

  it("opens and selects a mapping for a column", async () => {
    testRender(<ImportDatasetModal {...defaultProps} />);

    const mapButtons = screen.getAllByRole("button", { name: /map column/i });
    fireEvent.click(mapButtons[0]);

    const option = await screen.findByText("Input");
    expect(option).toBeInTheDocument();

    fireEvent.click(option);

    expect(screen.getAllByText("Input")).toHaveLength(2);
  });

  it("disables add dataset button if required column is not mapped", () => {
    testRender(<ImportDatasetModal {...defaultProps} />);

    const addButton = screen.getByRole("button", { name: /add dataset/i });
    expect(addButton).toBeDisabled();
  });

  it("enables add dataset button when required column is mapped", async () => {
    testRender(<ImportDatasetModal {...defaultProps} />);

    const mapButtons = screen.getAllByRole("button", { name: /map column/i });
    fireEvent.click(mapButtons[0]);

    const inputOption = await screen.findByText("Input");
    fireEvent.click(inputOption);

    const addButton = screen.getByRole("button", { name: /add dataset/i });
    await waitFor(() => expect(addButton).toBeEnabled());
  });

  it("removes mapping and disables selected option", async () => {
    testRender(<ImportDatasetModal {...defaultProps} />);

    const mapButtons = screen.getAllByRole("button", { name: /map column/i });
    fireEvent.click(mapButtons[0]);

    const inputOption = await screen.findByText("Input");
    fireEvent.click(inputOption);

    expect(screen.getAllByText("Input")).toHaveLength(2);
  });

  it("toggles chat format when checkbox is clicked", () => {
    testRender(<ImportDatasetModal {...defaultProps} />);

    const chatFormatCheckbox = screen.getByRole("checkbox", {
      name: /chat format/i,
    });
    expect(chatFormatCheckbox).not.toBeChecked();

    fireEvent.click(chatFormatCheckbox);

    expect(chatFormatCheckbox).toBeChecked();
  });

  it("processes pointwise dataset import", async () => {
    const datasetId = "dataset-123";
    const mockImport = clientQueries.importDatasetQuery as jest.Mock;

    testRender(
      <ImportDatasetModal
        {...defaultProps}
        dataset={{ id: datasetId } as any}
      />,
    );

    const mapButtons = screen.getAllByRole("button", { name: /map column/i });
    fireEvent.click(mapButtons[0]);

    const inputOption = await screen.findByText("Input");
    fireEvent.click(inputOption);

    const addButton = screen.getByRole("button", { name: /add dataset/i });
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(mockImport).toHaveBeenCalledWith(
        datasetId,
        expect.objectContaining({
          input_column_name: "name",
        }),
      );
    });
  });

  it("processes side-by-side dataset import", async () => {
    const mockImport = clientQueries.importSxsProjectQuery as jest.Mock;

    testRender(
      <ImportDatasetModal
        {...defaultProps}
        projectType={ProjectType.SIDE_BY_SIDE}
      />,
    );

    const mapButtons = screen.getAllByRole("button", { name: /map column/i });
    fireEvent.click(mapButtons[0]);

    const inputOption = await screen.findByText("Input");
    fireEvent.click(inputOption);

    const addButton = screen.getByRole("button", { name: /add dataset/i });
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(mockImport).toHaveBeenCalledWith(
        "test-project-id",
        expect.objectContaining({
          input_column_name: "name",
        }),
      );
    });
  });

  it("transfers dataset to project after successful import", async () => {
    const datasetId = "dataset-123";
    const mockTransfer = clientQueries.dataTransferAllQuery as jest.Mock;

    testRender(
      <ImportDatasetModal
        {...defaultProps}
        dataset={{ id: datasetId } as any}
        source={UploadDatasetModalSource.PROJECT}
      />,
    );

    const mapButtons = screen.getAllByRole("button", { name: /map column/i });
    fireEvent.click(mapButtons[0]);

    const inputOption = await screen.findByText("Input");
    fireEvent.click(inputOption);

    const addButton = screen.getByRole("button", { name: /add dataset/i });
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(mockTransfer).toHaveBeenCalled();
      const args = mockTransfer.mock.calls[0][0];
      expect(args.source_id).toBe(datasetId);
      expect(args.target_id).toBe("test-project-id");
    });
  });

  it("shows error notification when import has errors", async () => {
    (clientQueries.importDatasetQuery as jest.Mock).mockResolvedValueOnce({
      successfulRows: 0,
      errorMessages: ["Error 1", "Error 2", "Error 3", "Error 4"],
    });

    testRender(
      <ImportDatasetModal
        {...defaultProps}
        dataset={{ id: "dataset-123" } as any}
      />,
    );

    const mapButtons = screen.getAllByRole("button", { name: /map column/i });
    fireEvent.click(mapButtons[0]);

    const inputOption = await screen.findByText("Input");
    fireEvent.click(inputOption);

    const addButton = screen.getByRole("button", { name: /add dataset/i });
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalled();
    });
  });

  it("handles large datasets with many rows", () => {
    const manyRowsData: ParseResult<any> = {
      data: Array(20)
        .fill(0)
        .map((_, i) => ({ name: `Person ${i}`, age: i })),
      errors: [],
      meta: {
        fields: ["name", "age"],
        delimiter: ",",
        linebreak: "\n",
        aborted: false,
        truncated: false,
        cursor: 0,
      },
    };

    testRender(
      <ImportDatasetModal {...defaultProps} parsedFileResults={manyRowsData} />,
    );

    expect(screen.getByText(/20 rows/)).toBeInTheDocument();

    const truncatedTexts = screen.getAllByTestId("truncated-text");
    expect(truncatedTexts.length).toBeLessThan(20 * 2);
  });

  it("handles dataset with many columns", () => {
    const manyColumnsData: ParseResult<any> = {
      data: [
        {
          col1: "val1",
          col2: "val2",
          col3: "val3",
          col4: "val4",
          col5: "val5",
          col6: "val6",
          col7: "val7",
        },
      ],
      errors: [],
      meta: {
        fields: ["col1", "col2", "col3", "col4", "col5", "col6", "col7"],
        delimiter: ",",
        linebreak: "\n",
        aborted: false,
        truncated: false,
        cursor: 0,
      },
    };

    testRender(
      <ImportDatasetModal
        {...defaultProps}
        parsedFileResults={manyColumnsData}
      />,
    );

    expect(screen.getByText("col1")).toBeInTheDocument();
    expect(screen.getByText("col7")).toBeInTheDocument();
    expect(screen.getAllByRole("button", { name: /map column/i })).toHaveLength(
      7,
    );
  });
});
