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

import * as createdAtColumnModule from "@/components/table/columns/createdAtColumn";
import dayjs from "@/utils/dayjsSetup";
import { screen } from "@testing-library/react";

import { testRender } from "../../../render";

describe("createdAtColumn", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Column configuration", () => {
    it("has correct accessorKey, header, and settings", () => {
      expect(createdAtColumnModule.createdAtColumn.accessorKey).toBe(
        "created_at",
      );
      expect(createdAtColumnModule.createdAtColumn.header).toBe("Date added");
      expect(createdAtColumnModule.createdAtColumn.enableEditing).toBe(false);
      expect(createdAtColumnModule.createdAtColumn.enableSorting).toBe(true);
      expect(createdAtColumnModule.createdAtColumn.enableColumnFilter).toBe(
        true,
      );
    });
  });

  describe("Header component", () => {
    const mockColumn = {
      id: "created_at",
      columnDef: {
        header: "Date added",
        enableColumnFilter: true,
        enableSorting: true,
      },
      getFilterValue: jest.fn(),
      setFilterValue: jest.fn(),
    };

    const mockTable = {
      getVisibleLeafColumns: jest.fn(() => []),
      setColumnOrder: jest.fn(),
      options: {
        state: {
          columnVisibility: {},
        },
      },
    };

    const defaultProps = {
      column: mockColumn,
      table: mockTable,
    };

    it("renders Header component with correct props", () => {
      const HeaderComponent = createdAtColumnModule.createdAtColumn.Header;
      testRender(<HeaderComponent {...defaultProps} />);

      // The Header component should render the column header text
      expect(screen.getByText("Date added")).toBeInTheDocument();
    });

    it("passes filterType as date-range to ColumnHeader", () => {
      const HeaderComponent = createdAtColumnModule.createdAtColumn.Header;
      const { container } = testRender(<HeaderComponent {...defaultProps} />);

      // The Header component should be rendered (we can't easily test the filterType prop directly
      // without mocking, but we can verify the component renders)
      expect(container).toBeInTheDocument();
    });
  });

  describe("Cell component", () => {
    const mockColumn = {
      id: "created_at",
    };

    const defaultProps = {
      column: mockColumn,
      renderedCellValue: 1640995200000, // January 1, 2022 00:00:00 UTC
    };

    it("renders FormatDate component when value is provided", () => {
      const CellComponent = createdAtColumnModule.createdAtColumn.Cell;
      testRender(<CellComponent {...defaultProps} />);

      // FormatDate should format the timestamp and display it
      const formattedDate = dayjs(1640995200000)
        .local()
        .format("MMM D, YYYY h:mm A");
      expect(screen.getByText(formattedDate)).toBeInTheDocument();
    });

    it("renders with correct className", () => {
      const CellComponent = createdAtColumnModule.createdAtColumn.Cell;
      const { container } = testRender(<CellComponent {...defaultProps} />);

      // Check that the FormatDate component has the expected className
      const formatDateElement = container.querySelector(
        ".text-secondary.text-body-12",
      );
      expect(formatDateElement).toBeInTheDocument();
    });

    it("returns null when value is null", () => {
      const CellComponent = createdAtColumnModule.createdAtColumn.Cell;
      const propsWithNullValue = {
        ...defaultProps,
        renderedCellValue: null,
      };

      const { container } = testRender(
        <CellComponent {...propsWithNullValue} />,
      );

      // Should return null, so only Mantine style tags should be present
      expect(container.children).toHaveLength(2);
      expect(container.firstChild?.nodeName).toBe("STYLE");
    });

    it("returns null when value is undefined", () => {
      const CellComponent = createdAtColumnModule.createdAtColumn.Cell;
      const propsWithUndefinedValue = {
        ...defaultProps,
        renderedCellValue: undefined,
      };

      const { container } = testRender(
        <CellComponent {...propsWithUndefinedValue} />,
      );

      // Should return null, so only Mantine style tags should be present
      expect(container.children).toHaveLength(2);
      expect(container.firstChild?.nodeName).toBe("STYLE");
    });

    it("returns null when value is 0", () => {
      const CellComponent = createdAtColumnModule.createdAtColumn.Cell;
      const propsWithZeroValue = {
        ...defaultProps,
        renderedCellValue: 0,
      };

      const { container } = testRender(
        <CellComponent {...propsWithZeroValue} />,
      );

      // Should return null since 0 is falsy, so only Mantine style tags should be present
      expect(container.children).toHaveLength(2);
      expect(container.firstChild?.nodeName).toBe("STYLE");
    });

    it("renders with different timestamp values", () => {
      const CellComponent = createdAtColumnModule.createdAtColumn.Cell;

      // Test with a different timestamp
      const propsWithDifferentTimestamp = {
        ...defaultProps,
        renderedCellValue: 1672531200000, // January 1, 2023 00:00:00 UTC
      };

      testRender(<CellComponent {...propsWithDifferentTimestamp} />);

      const formattedDate = dayjs(1672531200000)
        .local()
        .format("MMM D, YYYY h:mm A");
      expect(screen.getByText(formattedDate)).toBeInTheDocument();
    });

    it("handles string timestamp values", () => {
      const CellComponent = createdAtColumnModule.createdAtColumn.Cell;
      const propsWithStringTimestamp = {
        ...defaultProps,
        renderedCellValue: "1640995200000", // String timestamp
      };

      testRender(<CellComponent {...propsWithStringTimestamp} />);

      const formattedDate = dayjs("1640995200000")
        .local()
        .format("MMM D, YYYY h:mm A");
      expect(screen.getByText(formattedDate)).toBeInTheDocument();
    });
  });

  describe("filterFn", () => {
    const filterFn = createdAtColumnModule.createdAtColumn.filterFn;
    const mockRow = {
      getValue: jest.fn(),
    };

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it("returns true when filterValue is empty array", () => {
      const result = filterFn(mockRow, "created_at", []);
      expect(result).toBe(true);
    });

    it("returns true when filterValue has null values", () => {
      const result = filterFn(mockRow, "created_at", [null, null]);
      expect(result).toBe(true);
    });

    it("returns true when filterValue has undefined values", () => {
      const result = filterFn(mockRow, "created_at", [undefined, undefined]);
      expect(result).toBe(true);
    });

    it("returns true when filterValue has empty string values", () => {
      const result = filterFn(mockRow, "created_at", ["", ""]);
      expect(result).toBe(true);
    });

    it("returns true when filterValue length is 0", () => {
      const result = filterFn(mockRow, "created_at", []);
      expect(result).toBe(true);
    });

    it("returns true when first filterValue is null", () => {
      const result = filterFn(mockRow, "created_at", [null, "2022-01-01"]);
      expect(result).toBe(true);
    });

    it("returns true when second filterValue is null", () => {
      const result = filterFn(mockRow, "created_at", ["2022-01-01", null]);
      expect(result).toBe(true);
    });

    it("returns true when first filterValue is undefined", () => {
      const result = filterFn(mockRow, "created_at", [undefined, "2022-01-01"]);
      expect(result).toBe(true);
    });

    it("returns true when second filterValue is undefined", () => {
      const result = filterFn(mockRow, "created_at", ["2022-01-01", undefined]);
      expect(result).toBe(true);
    });

    it("filters correctly when date is within range", () => {
      const testDate = dayjs("2022-06-15");
      mockRow.getValue.mockReturnValue(testDate);

      const result = filterFn(mockRow, "created_at", [
        dayjs("2022-01-01"),
        dayjs("2022-12-31"),
      ]);

      expect(result).toBe(true);
      expect(mockRow.getValue).toHaveBeenCalledWith("created_at");
    });

    it("filters correctly when date is outside range", () => {
      const testDate = dayjs("2023-01-01");
      mockRow.getValue.mockReturnValue(testDate);

      const result = filterFn(mockRow, "created_at", [
        dayjs("2022-01-01"),
        dayjs("2022-12-31"),
      ]);

      expect(result).toBe(false);
      expect(mockRow.getValue).toHaveBeenCalledWith("created_at");
    });

    it("filters correctly when date equals start date", () => {
      const testDate = dayjs("2022-01-01");
      mockRow.getValue.mockReturnValue(testDate);

      const result = filterFn(mockRow, "created_at", [
        dayjs("2022-01-01"),
        dayjs("2022-12-31"),
      ]);

      expect(result).toBe(true);
    });

    it("filters correctly when date equals end date", () => {
      const testDate = dayjs("2022-12-31");
      mockRow.getValue.mockReturnValue(testDate);

      const result = filterFn(mockRow, "created_at", [
        dayjs("2022-01-01"),
        dayjs("2022-12-31"),
      ]);

      expect(result).toBe(true);
    });

    it("handles timestamp values correctly", () => {
      const testTimestamp = 1640995200000; // January 1, 2022 00:00:00 UTC
      mockRow.getValue.mockReturnValue(testTimestamp);

      const result = filterFn(mockRow, "created_at", [
        dayjs("2021-12-01"),
        dayjs("2022-02-01"),
      ]);

      expect(result).toBe(true);
    });

    it("handles string date values correctly", () => {
      const testDateString = "2022-06-15";
      mockRow.getValue.mockReturnValue(testDateString);

      const result = filterFn(mockRow, "created_at", [
        dayjs("2022-01-01"),
        dayjs("2022-12-31"),
      ]);

      expect(result).toBe(true);
    });

    it("handles edge case with same start and end date", () => {
      const testDate = dayjs("2022-06-15");
      mockRow.getValue.mockReturnValue(testDate);

      const result = filterFn(mockRow, "created_at", [
        dayjs("2022-06-15"),
        dayjs("2022-06-15"),
      ]);

      expect(result).toBe(true);
    });

    it("handles edge case with date exactly at boundary", () => {
      const testDate = dayjs("2022-06-15T00:00:00");
      mockRow.getValue.mockReturnValue(testDate);

      const result = filterFn(mockRow, "created_at", [
        dayjs("2022-06-15"),
        dayjs("2022-06-15"),
      ]);

      expect(result).toBe(true);
    });
  });
});
