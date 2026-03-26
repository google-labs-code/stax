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

import { UploadDatasetModal } from "@/components/UploadDatasetModal";
import { createDatasetQuery } from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { ProjectType, UploadDatasetModalSource } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import Papa from "papaparse";

import { enableUploadDatasetModalNextButton } from "../helpers";

jest.mock("@/queries/clientQueries", () => ({
  createDatasetQuery: jest.fn(),
}));
jest.mock("papaparse", () => ({
  parse: jest.fn(),
}));
jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

describe("UploadDatasetModal", () => {
  const defaultProps = {
    isOpened: true,
    onClose: jest.fn(),
    onSuccess: jest.fn(),
    tooltipLabel: "upload tooltip",
    source: UploadDatasetModalSource.PROJECT,
    projectType: ProjectType.POINTWISE,
    onDatasetCreated: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    window.open = jest.fn();
  });

  it("renders modal title correctly when not side-by-side", () => {
    testRender(<UploadDatasetModal {...defaultProps} />);
    expect(screen.getByText("Upload dataset")).toBeInTheDocument();
  });

  it("renders modal title differently for SIDE_BY_SIDE project type", () => {
    testRender(
      <UploadDatasetModal
        {...defaultProps}
        projectType={ProjectType.SIDE_BY_SIDE}
      />,
    );
    expect(screen.getByText("Upload data")).toBeInTheDocument();
  });

  it("accepts a CSV file via dropzone, parses it, and enables Next", async () => {
    await enableUploadDatasetModalNextButton(() => {
      testRender(<UploadDatasetModal {...defaultProps} />);
    });

    const nextBtn = screen.getByText("Next");
    expect(nextBtn).not.toBeDisabled();
  });

  it("calls createDatasetMutation on Next when valid", async () => {
    const fakeFile = new File(["a,b\nc,d"], "test.csv", {
      type: "text/csv",
    });
    const fakeParseResult = {
      data: [{ a: "c", b: "d" }],
      errors: [],
      meta: {},
    };
    (Papa.parse as jest.Mock).mockImplementation((file: any, opts: any) => {
      opts.complete(fakeParseResult);
    });

    (createDatasetQuery as jest.Mock).mockResolvedValue({
      id: "new-ds-id",
      name: "My Dataset",
      description: "",
    });

    testRender(<UploadDatasetModal {...defaultProps} />);

    const dropzones = screen.getAllByText(
      /Drag and drop your file or click to upload/i,
    );
    fireEvent.drop(dropzones[0], {
      dataTransfer: {
        files: [fakeFile],
        items: [
          {
            kind: "file",
            type: fakeFile.type,
            getAsFile: () => fakeFile,
          },
        ],
        types: ["Files"],
      },
    });

    await waitFor(() => {
      expect(
        screen.getByText(/File selected. Click to change/i),
      ).toBeInTheDocument();
    });

    const nameInput = screen.getByPlaceholderText("Name");
    fireEvent.change(nameInput, { target: { value: "My Dataset" } });

    const nextBtn = screen.getByText("Next");
    fireEvent.click(nextBtn);

    await waitFor(() => {
      expect(createDatasetQuery).toHaveBeenCalledWith({
        name: "My Dataset",
        description: "",
      });
    });
  });

  it("opens CSV guidelines link when clicked", async () => {
    testRender(<UploadDatasetModal {...defaultProps} />);

    const guidelinesBtn = screen.getByText("CSV Guidelines");
    fireEvent.click(guidelinesBtn);

    expect(window.open).toHaveBeenCalledWith(
      "https://developers.google.com/stax/datasets#csv_upload",
      "_blank",
    );
  });
});
