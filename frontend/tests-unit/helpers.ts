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

import { fireEvent, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Papa from "papaparse";

jest.mock("papaparse", () => ({
  parse: jest.fn(),
}));

interface triggerDateRangeInputChangeProps {
  customScreen: any;
  startTime?: string;
  endTime?: string;
}

export const triggerDateRangeInputChange = async ({
  customScreen,
  startTime = "08:15",
  endTime = "09:15",
}: triggerDateRangeInputChangeProps) => {
  // Find the InputBase target by placeholder or text
  const input = customScreen.getByText(/pick dates/i);

  // 2 Click to open dropdown
  await userEvent.click(input);

  // 3 Wait for the option to appear (Mantine renders dropdown in a portal)
  const option = await customScreen.findByRole("option", { name: /custom/i });

  // 4 Click the option
  await userEvent.click(option);

  // 5 Set start and end times
  const inputStartTime = await customScreen.findByLabelText(/Start time/i);
  const inputEndTime = await customScreen.findByLabelText(/End time/i);
  fireEvent.change(inputStartTime, { target: { value: startTime } });
  fireEvent.change(inputEndTime, { target: { value: endTime } });

  // 6 Wait for apply button to be enabled
  const applyButton = customScreen.getByRole("button", { name: /apply/i });
  await waitFor(() => expect(applyButton).toBeEnabled());

  // 7 Trigger the time change
  await userEvent.click(applyButton);
};

export const enableUploadDatasetModalNextButton = async (
  renderUploadDatasetModal: any,
) => {
  const fakeFile = new File(["col1,col2\nval1,val2"], "test.csv", {
    type: "text/csv",
  });
  const fakeParseResult = {
    data: [{ col1: "val1", col2: "val2" }],
    errors: [],
    meta: {},
  };
  (Papa.parse as jest.Mock).mockImplementation((file: any, opts: any) => {
    opts.complete(fakeParseResult);
  });

  renderUploadDatasetModal();

  const dropzones = screen.getAllByText(
    /Drag and drop your file or click to upload/i,
  );
  expect(dropzones.length).toBeGreaterThan(0);

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
};
