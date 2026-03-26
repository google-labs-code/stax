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

import { InferenceStatus } from "@/app/(authRoutes)/projects/[id]/types";
import * as inferenceLatencyColumnModule from "@/components/table/columns/inferenceLatencyColumn";
import { ProjectType } from "@/types";
import { screen } from "@testing-library/react";

import { testRender } from "../../../render";

describe("inferenceLatencyColumn", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Column configuration", () => {
    it("has correct accessorKey, header, and settings", () => {
      expect(
        inferenceLatencyColumnModule.inferenceLatencyColumn.accessorKey,
      ).toBe("inference_latency");
      expect(inferenceLatencyColumnModule.inferenceLatencyColumn.header).toBe(
        "Latency",
      );
      expect(
        inferenceLatencyColumnModule.inferenceLatencyColumn.enableEditing,
      ).toBe(false);
      expect(
        inferenceLatencyColumnModule.inferenceLatencyColumn.enableSorting,
      ).toBe(true);
      expect(
        inferenceLatencyColumnModule.inferenceLatencyColumn.enableColumnFilter,
      ).toBe(true);
      expect(inferenceLatencyColumnModule.inferenceLatencyColumn.size).toBe(
        100,
      );
    });
  });

  describe("Header component", () => {
    const mockColumn = {
      id: "inference_latency",
      columnDef: {
        header: "Latency",
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

    it("renders ColumnHeader with correct props", () => {
      const HeaderComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Header;
      testRender(<HeaderComponent {...defaultProps} />);

      expect(screen.getByText("Latency")).toBeInTheDocument();
    });
  });

  describe("Cell component - Side by Side project type", () => {
    const mockTable = {
      options: {
        meta: {
          projectType: ProjectType.SIDE_BY_SIDE,
        },
      },
    };

    const createCellProps = (originalRow: any) => ({
      row: { original: originalRow },
      table: mockTable,
    });

    it("renders SideBySideDefaultColumn with correct values when both values exist", () => {
      const originalRow = {
        chat_turn_a: { inference_latency: 1500 },
        chat_turn_b: { inference_latency: 2000 },
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      // Check that SideBySideDefaultColumn is rendered with correct values
      expect(screen.getByText("1500")).toBeInTheDocument(); // valueA raw ms
      expect(screen.getByText("2000")).toBeInTheDocument(); // valueB raw ms
      expect(screen.getByText("-0.50s")).toBeInTheDocument(); // difference (A - B) converted
    });

    it("renders SideBySideDefaultColumn with only valueA", () => {
      const originalRow = {
        chat_turn_a: { inference_latency: 1500 },
        chat_turn_b: { inference_latency: undefined },
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getByText("1500")).toBeInTheDocument(); // valueA raw ms
      expect(screen.getAllByText("-")).toHaveLength(2); // valueB placeholder and difference placeholder
    });

    it("renders SideBySideDefaultColumn with only valueB", () => {
      const originalRow = {
        chat_turn_a: { inference_latency: undefined },
        chat_turn_b: { inference_latency: 2000 },
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getAllByText("-")).toHaveLength(2); // valueA placeholder and difference placeholder
      expect(screen.getByText("2000")).toBeInTheDocument(); // valueB raw ms
    });

    it("renders SideBySideDefaultColumn with no values", () => {
      const originalRow = {
        chat_turn_a: { inference_latency: undefined },
        chat_turn_b: { inference_latency: undefined },
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      // SideBySideDefaultColumn returns null when both values are undefined
      expect(screen.queryByText("-")).not.toBeInTheDocument();
      // The container should only contain the Mantine styles, no actual content
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("calculates positive difference correctly", () => {
      const originalRow = {
        chat_turn_a: { inference_latency: 3000 },
        chat_turn_b: { inference_latency: 2000 },
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getByText("3000")).toBeInTheDocument(); // valueA raw ms
      expect(screen.getByText("2000")).toBeInTheDocument(); // valueB raw ms
      expect(screen.getByText("1.00s")).toBeInTheDocument(); // difference (A - B = 1000ms = 1.00s)
    });

    it("calculates negative difference correctly", () => {
      const originalRow = {
        chat_turn_a: { inference_latency: 1000 },
        chat_turn_b: { inference_latency: 2500 },
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getByText("1000")).toBeInTheDocument(); // valueA raw ms
      expect(screen.getByText("2500")).toBeInTheDocument(); // valueB raw ms
      expect(screen.getByText("-1.50s")).toBeInTheDocument(); // difference (A - B = -1500ms = -1.50s)
    });
  });

  describe("Cell component - Regular project type", () => {
    const mockTable = {
      options: {
        meta: {
          projectType: ProjectType.POINTWISE,
        },
      },
    };

    const createCellProps = (originalRow: any) => ({
      row: { original: originalRow },
      table: mockTable,
    });

    it("renders latency value when all conditions are met", () => {
      const originalRow = {
        inference_latency: 1500,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getByText("1.50s")).toBeInTheDocument();
    });

    it("renders latency value with different latency values", () => {
      const originalRow = {
        inference_latency: 2500,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getByText("2.50s")).toBeInTheDocument();
    });

    it("returns null when inference status is not successful", () => {
      const originalRow = {
        inference_latency: 1500,
        inference_status: InferenceStatus.FAILED,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      // Check that no latency text is rendered
      expect(screen.queryByText("1.50s")).not.toBeInTheDocument();
      // The container should only contain the Mantine styles, no actual content
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("returns null when output is missing", () => {
      const originalRow = {
        inference_latency: 1500,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: null,
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      // Check that no latency text is rendered
      expect(screen.queryByText("1.50s")).not.toBeInTheDocument();
      // The container should only contain the Mantine styles, no actual content
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("returns null when output is empty string", () => {
      const originalRow = {
        inference_latency: 1500,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      // Check that no latency text is rendered
      expect(screen.queryByText("1.50s")).not.toBeInTheDocument();
      // The container should only contain the Mantine styles, no actual content
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("returns null when inference_latency is zero", () => {
      const originalRow = {
        inference_latency: 0,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      // Check that no latency text is rendered
      expect(screen.queryByText("0.00s")).not.toBeInTheDocument();
      // The container should only contain the Mantine styles, no actual content
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("returns null when inference_latency is negative", () => {
      const originalRow = {
        inference_latency: -100,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      // Check that no latency text is rendered
      expect(screen.queryByText("-0.10s")).not.toBeInTheDocument();
      // The container should only contain the Mantine styles, no actual content
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("returns null when inference_latency is undefined", () => {
      const originalRow = {
        inference_latency: undefined,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      // Check that no latency text is rendered
      expect(screen.queryByText("0.00s")).not.toBeInTheDocument();
      // The container should only contain the Mantine styles, no actual content
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("returns null when inference status is PENDING", () => {
      const originalRow = {
        inference_latency: 1500,
        inference_status: InferenceStatus.PENDING,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      expect(screen.queryByText("1.50s")).not.toBeInTheDocument();
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("returns null when inference status is IN_PROGRESS", () => {
      const originalRow = {
        inference_latency: 1500,
        inference_status: InferenceStatus.IN_PROGRESS,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      expect(screen.queryByText("1.50s")).not.toBeInTheDocument();
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("returns null when inference status is STOPPED", () => {
      const originalRow = {
        inference_latency: 1500,
        inference_status: InferenceStatus.STOPPED,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      const { container } = testRender(
        <CellComponent {...createCellProps(originalRow)} />,
      );

      expect(screen.queryByText("1.50s")).not.toBeInTheDocument();
      expect(container.children).toHaveLength(2); // Two style elements
      expect((container.firstChild as Element)?.tagName).toBe("STYLE");
    });

    it("renders with correct CSS classes", () => {
      const originalRow = {
        inference_latency: 1500,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      const textElement = screen.getByText("1.50s");
      expect(textElement).toHaveClass("text-secondary", "text-body-12");
    });
  });

  describe("Cell component - Edge cases", () => {
    const mockTable = {
      options: {
        meta: {
          projectType: ProjectType.POINTWISE,
        },
      },
    };

    const createCellProps = (originalRow: any) => ({
      row: { original: originalRow },
      table: mockTable,
    });

    it("handles very small latency values", () => {
      const originalRow = {
        inference_latency: 1,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getByText("0.00s")).toBeInTheDocument();
    });

    it("handles very large latency values", () => {
      const originalRow = {
        inference_latency: 300000, // 5 minutes
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getByText("300.00s")).toBeInTheDocument();
    });

    it("handles decimal latency values", () => {
      const originalRow = {
        inference_latency: 1234.567,
        inference_status: InferenceStatus.SUCCESSFUL,
        output: "Some output text",
      };

      const CellComponent =
        inferenceLatencyColumnModule.inferenceLatencyColumn.Cell;
      testRender(<CellComponent {...createCellProps(originalRow)} />);

      expect(screen.getByText("1.23s")).toBeInTheDocument();
    });
  });
});
