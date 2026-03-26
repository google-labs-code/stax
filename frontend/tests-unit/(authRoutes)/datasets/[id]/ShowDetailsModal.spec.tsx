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

import { ShowDetailsModal } from "@/app/(authRoutes)/datasets/components/ShowDetailsModal";
import { testRender } from "@/tests-unit/render";
import dayjs from "@/utils/dayjsSetup";
import { fireEvent, screen } from "@testing-library/react";

const mockDataset = {
  id: "ds1",
  name: "Sample Dataset",
  created_at: "2025-08-21T10:15:00.000Z",
  updated_at: "2025-08-21T10:15:00.000Z",
};

describe("ShowDetailsModal", () => {
  it("renders dataset details correctly when opened", () => {
    testRender(
      <ShowDetailsModal
        isOpened={true}
        onClose={jest.fn()}
        dataset={mockDataset}
      />,
    );

    expect(screen.getByText("Sample Dataset details")).toBeInTheDocument();
    expect(screen.getByText("Date added")).toBeInTheDocument();

    const expectedDate = dayjs(mockDataset.created_at).format("MM/DD/YYYY H:m");
    expect(screen.getByText(expectedDate)).toBeInTheDocument();
  });

  it("does not render modal when isOpened is false", () => {
    testRender(
      <ShowDetailsModal
        isOpened={false}
        onClose={jest.fn()}
        dataset={mockDataset}
      />,
    );

    expect(
      screen.queryByText("Sample Dataset details"),
    ).not.toBeInTheDocument();
  });

  it("calls onClose when modal close button is clicked", () => {
    const onCloseMock = jest.fn();

    testRender(
      <ShowDetailsModal
        isOpened={true}
        onClose={onCloseMock}
        dataset={mockDataset}
      />,
    );

    const closeButton = screen.getByRole("button");
    fireEvent.click(closeButton);

    expect(onCloseMock).toHaveBeenCalled();
  });
});
