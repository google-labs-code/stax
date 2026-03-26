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

import { ImportFileModal } from "@/components/ImportFileModal";
import { getErrorNotificationConfig } from "@/config/notifications";
import {
  uploadDatasetFileQuery,
  uploadProjectFileQuery,
} from "@/queries/clientQueries";
import { notifications } from "@mantine/notifications";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { testRender } from "../render";

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

jest.mock("@/queries/clientQueries", () => ({
  uploadDatasetFileQuery: jest.fn(() => Promise.resolve({})),
  uploadProjectFileQuery: jest.fn(() => Promise.resolve({})),
}));

jest.mock("@/config/notifications", () => ({
  getErrorNotificationConfig: jest.fn((message) => ({
    message,
    id: "mock-error",
  })),
}));

const simulateFileDrop = (element: HTMLElement, files: File[]) => {
  fireEvent.drop(element, {
    dataTransfer: {
      files,
      types: ["Files"],
    },
  });
};

describe("ImportFileModal", () => {
  const baseProps = {
    isOpened: true,
    onClose: jest.fn(),
    onSuccess: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders and handles valid file upload (dataset)", async () => {
    testRender(<ImportFileModal {...baseProps} datasetId="123-dataset" />);

    const dropzoneArea = screen.getByText(/drag and drop/i).closest("div")!;
    const file = new File(["id,name\n1,Test"], "test.csv", {
      type: "text/csv",
    });

    simulateFileDrop(dropzoneArea, [file]);

    expect(await screen.findByText("test.csv")).toBeInTheDocument();

    const importButton = screen.getByRole("button", { name: /import/i });
    fireEvent.click(importButton);

    await waitFor(() => {
      expect(uploadDatasetFileQuery).toHaveBeenCalledWith("123-dataset", file);
      expect(baseProps.onSuccess).toHaveBeenCalledWith(file);
      expect(baseProps.onClose).toHaveBeenCalled();
    });
  });

  it("renders and handles valid file upload (project)", async () => {
    testRender(<ImportFileModal {...baseProps} projectId="456-project" />);

    const dropzoneArea = screen.getByText(/drag and drop/i).closest("div")!;
    const file = new File(["id,name\n1,Test"], "project.csv", {
      type: "text/csv",
    });

    simulateFileDrop(dropzoneArea, [file]);

    expect(await screen.findByText("project.csv")).toBeInTheDocument();

    const importButton = screen.getByRole("button", { name: /import/i });
    fireEvent.click(importButton);

    await waitFor(() => {
      expect(uploadProjectFileQuery).toHaveBeenCalledWith("456-project", file);
      expect(baseProps.onSuccess).toHaveBeenCalledWith(file);
      expect(baseProps.onClose).toHaveBeenCalled();
    });
  });

  it("disables import button when no file is selected", () => {
    testRender(<ImportFileModal {...baseProps} datasetId="123-dataset" />);

    const importButton = screen.getByRole("button", { name: /import/i });
    expect(importButton).toBeDisabled();

    fireEvent.click(importButton);
    expect(uploadDatasetFileQuery).not.toHaveBeenCalled();
    expect(uploadProjectFileQuery).not.toHaveBeenCalled();
  });

  it("resets state when modal is reopened", async () => {
    const { rerender } = testRender(
      <ImportFileModal
        {...baseProps}
        datasetId="123-dataset"
        isOpened={false}
      />,
    );

    rerender(
      <ImportFileModal
        {...baseProps}
        datasetId="123-dataset"
        isOpened={true}
      />,
    );

    const dropzoneArea = screen.getByText(/drag and drop/i).closest("div")!;
    const file = new File(["id,name\n1,Test"], "test.csv", {
      type: "text/csv",
    });

    simulateFileDrop(dropzoneArea, [file]);

    expect(await screen.findByText("test.csv")).toBeInTheDocument();

    rerender(
      <ImportFileModal
        {...baseProps}
        datasetId="123-dataset"
        isOpened={false}
      />,
    );
    rerender(
      <ImportFileModal
        {...baseProps}
        datasetId="123-dataset"
        isOpened={true}
      />,
    );

    expect(screen.queryByText("test.csv")).not.toBeInTheDocument();
    expect(screen.getByText(/drag and drop your file/i)).toBeInTheDocument();
  });

  it("simulates file upload button click and displays validation state", async () => {
    const user = userEvent.setup();
    testRender(<ImportFileModal {...baseProps} datasetId="123-dataset" />);

    const uploadButton = screen.getByText("Upload CSV");

    const dropzoneArea =
      uploadButton.closest("div.mantine-Dropzone-root") ||
      uploadButton.parentElement;

    await user.click(uploadButton);

    fireEvent.click(document.body);

    const cardElement = dropzoneArea?.closest('[class*="border"]');
    expect(cardElement).toHaveClass("border-red-500");
  });

  it("shows error notification for invalid file type", async () => {
    testRender(<ImportFileModal {...baseProps} datasetId="123-dataset" />);

    const mockErrorConfig = {
      message: "Invalid file type. Upload a .csv file.",
      id: "mock-error",
    };
    (getErrorNotificationConfig as jest.Mock).mockReturnValueOnce(
      mockErrorConfig,
    );

    notifications.show(
      getErrorNotificationConfig("Invalid file type. Upload a .csv file."),
    );

    expect(notifications.show).toHaveBeenCalledWith(mockErrorConfig);
  });

  it("shows error notification for file size exceeding limit", async () => {
    testRender(<ImportFileModal {...baseProps} datasetId="123-dataset" />);

    const mockErrorConfig = {
      message: "File size exceeds the maximum allowed size.",
      id: "mock-error",
    };
    (getErrorNotificationConfig as jest.Mock).mockReturnValueOnce(
      mockErrorConfig,
    );

    notifications.show(
      getErrorNotificationConfig("File size exceeds the maximum allowed size."),
    );

    expect(notifications.show).toHaveBeenCalledWith(mockErrorConfig);
  });
});
