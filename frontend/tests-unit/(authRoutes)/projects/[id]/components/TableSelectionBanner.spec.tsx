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

import TableSelectionBanner from "@/app/(authRoutes)/projects/[id]/components/TableSelectionBanner";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

describe("TableSelectionBanner", () => {
  const baseProps = {
    visible: true,
    allRowsSelected: false,
    selectAllRowsInProject: false,
    totalInProject: 100,
    currentPageSize: 10,
    selectedCount: 0,
    onSelectAll: jest.fn(),
    onClearSelection: jest.fn(),
  };

  const baseText = "All 10 rows on this page are selected.";

  it("shows 'All rows in project selected' banner and calls onClearSelection", () => {
    testRender(
      <TableSelectionBanner
        {...baseProps}
        selectAllRowsInProject={true}
        allRowsSelected={false}
      />,
    );

    screen.getByText("rows in the project are selected", { exact: false });

    fireEvent.click(screen.getByText("Clear selection"));
    expect(baseProps.onClearSelection).toHaveBeenCalled();
  });

  it("shows 'All rows on this page are selected' and calls onSelectAll", () => {
    testRender(
      <TableSelectionBanner
        {...baseProps}
        selectAllRowsInProject={false}
        allRowsSelected={true}
      />,
    );

    expect(screen.getByText(baseText)).toBeInTheDocument();

    fireEvent.click(screen.getByText("Select all 100 rows in the project"));
    expect(baseProps.onSelectAll).toHaveBeenCalled();
  });

  it("renders nothing if visible is false", () => {
    testRender(
      <TableSelectionBanner
        {...baseProps}
        visible={false}
        selectAllRowsInProject={false}
        allRowsSelected={true}
      />,
    );

    expect(screen.queryByText(baseText)).not.toBeInTheDocument();
  });

  it("renders nothing if both selectAllRowsInProject and allRowsSelected are false", () => {
    testRender(
      <TableSelectionBanner
        {...baseProps}
        selectAllRowsInProject={false}
        allRowsSelected={false}
      />,
    );

    expect(screen.queryByText(baseText)).not.toBeInTheDocument();
  });
});
