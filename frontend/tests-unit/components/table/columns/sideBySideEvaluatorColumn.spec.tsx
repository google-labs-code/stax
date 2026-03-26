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

import SideBySideEvaluatorColumn from "@/components/table/columns/SideBySideEvaluatorColumn";
import { screen } from "@testing-library/react";

import { testRender } from "../../../render";

describe("SideBySideEvaluatorColumn", () => {
  describe("rendering with both statusA and statusB", () => {
    it("renders both status components when both are provided", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;
      const statusB = <div data-testid="status-b">Status B Content</div>;

      testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={statusB} />,
      );

      expect(screen.getByTestId("status-a")).toBeInTheDocument();
      expect(screen.getByTestId("status-b")).toBeInTheDocument();
    });

    it("renders correct model letters for both statuses", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;
      const statusB = <div data-testid="status-b">Status B Content</div>;

      testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={statusB} />,
      );

      expect(screen.getByText("A")).toBeInTheDocument();
      expect(screen.getByText("B")).toBeInTheDocument();
    });

    it("applies correct CSS classes to the main container", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;
      const statusB = <div data-testid="status-b">Status B Content</div>;

      const { container } = testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={statusB} />,
      );

      // Find the main Group component (skip the style elements)
      const mainGroup = container.querySelector(
        ".mantine-Group-root",
      ) as HTMLElement;
      expect(mainGroup).toHaveClass("flex", "flex-col", "w-[100%]");
    });

    it("applies correct CSS classes to scroll areas and inner groups", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;
      const statusB = <div data-testid="status-b">Status B Content</div>;

      const { container } = testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={statusB} />,
      );

      // Check for ScrollArea components
      const scrollAreas = container.querySelectorAll(
        ".mantine-ScrollArea-viewport",
      );
      expect(scrollAreas).toHaveLength(2);

      scrollAreas.forEach((scrollArea) => {
        expect(scrollArea).toHaveClass("m_c0783ff9");
      });

      // Check for Mantine Group components
      const groups = container.querySelectorAll(".mantine-Group-root");
      expect(groups.length).toBeGreaterThan(0);
    });
  });

  describe("conditional rendering", () => {
    it("renders only statusA when statusB is null", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;

      testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={null} />,
      );

      expect(screen.getByTestId("status-a")).toBeInTheDocument();
      expect(screen.queryByTestId("status-b")).not.toBeInTheDocument();
      expect(screen.getByText("A")).toBeInTheDocument();
      expect(screen.queryByText("B")).not.toBeInTheDocument();
    });

    it("renders only statusB when statusA is null", () => {
      const statusB = <div data-testid="status-b">Status B Content</div>;

      testRender(
        <SideBySideEvaluatorColumn statusA={null} statusB={statusB} />,
      );

      expect(screen.queryByTestId("status-a")).not.toBeInTheDocument();
      expect(screen.getByTestId("status-b")).toBeInTheDocument();
      expect(screen.queryByText("A")).not.toBeInTheDocument();
      expect(screen.getByText("B")).toBeInTheDocument();
    });

    it("renders only statusA when statusB is undefined", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;

      testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={undefined} />,
      );

      expect(screen.getByTestId("status-a")).toBeInTheDocument();
      expect(screen.queryByTestId("status-b")).not.toBeInTheDocument();
    });

    it("renders only statusB when statusA is undefined", () => {
      const statusB = <div data-testid="status-b">Status B Content</div>;

      testRender(
        <SideBySideEvaluatorColumn statusA={undefined} statusB={statusB} />,
      );

      expect(screen.queryByTestId("status-a")).not.toBeInTheDocument();
      expect(screen.getByTestId("status-b")).toBeInTheDocument();
    });
  });

  describe("empty states", () => {
    it("renders empty container when both statusA and statusB are null", () => {
      const { container } = testRender(
        <SideBySideEvaluatorColumn statusA={null} statusB={null} />,
      );

      const mainGroup = container.querySelector(
        ".mantine-Group-root",
      ) as HTMLElement;
      expect(mainGroup).toBeInTheDocument();
      expect(mainGroup).toHaveClass("flex", "flex-col", "w-[100%]");
      expect(screen.queryByTestId("status-a")).not.toBeInTheDocument();
      expect(screen.queryByTestId("status-b")).not.toBeInTheDocument();
      expect(
        screen.queryByTestId("side-by-side-model-letter-A"),
      ).not.toBeInTheDocument();
      expect(
        screen.queryByTestId("side-by-side-model-letter-B"),
      ).not.toBeInTheDocument();
    });

    it("renders empty container when both statusA and statusB are undefined", () => {
      const { container } = testRender(
        <SideBySideEvaluatorColumn statusA={undefined} statusB={undefined} />,
      );

      const mainGroup = container.querySelector(
        ".mantine-Group-root",
      ) as HTMLElement;
      expect(mainGroup).toBeInTheDocument();
      expect(mainGroup).toHaveClass("flex", "flex-col", "w-[100%]");
      expect(screen.queryByTestId("status-a")).not.toBeInTheDocument();
      expect(screen.queryByTestId("status-b")).not.toBeInTheDocument();
    });
  });

  describe("component structure", () => {
    it("renders with proper Mantine Group components", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;
      const statusB = <div data-testid="status-b">Status B Content</div>;

      const { container } = testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={statusB} />,
      );

      // Check for Mantine Group components
      const groups = container.querySelectorAll(".mantine-Group-root");
      expect(groups.length).toBeGreaterThan(0);
    });

    it("renders with proper ScrollArea components", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;
      const statusB = <div data-testid="status-b">Status B Content</div>;

      const { container } = testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={statusB} />,
      );

      // Check for ScrollArea components
      const scrollAreas = container.querySelectorAll(
        ".mantine-ScrollArea-viewport",
      );
      expect(scrollAreas).toHaveLength(2);
    });

    it("applies correct styling classes to status containers", () => {
      const statusA = <div data-testid="status-a">Status A Content</div>;
      const statusB = <div data-testid="status-b">Status B Content</div>;

      testRender(
        <SideBySideEvaluatorColumn statusA={statusA} statusB={statusB} />,
      );

      // Find the status containers by looking for the parent div that contains the model letter and status
      const statusAContainer = screen.getByText("A")
        .parentElement as HTMLElement;
      const statusBContainer = screen.getByText("B")
        .parentElement as HTMLElement;

      expect(statusAContainer).toHaveClass(
        "flex",
        "flex-row",
        "flex-nowrap",
        "w-[100%]",
        "gap-xl",
        "border-b-default",
        "min-h-[40px]",
        "p-[12px]",
      );

      expect(statusBContainer).toHaveClass(
        "flex",
        "flex-row",
        "flex-nowrap",
        "w-[100%]",
        "gap-xl",
        "border-b-default",
        "min-h-[40px]",
        "p-[12px]",
      );
    });
  });

  describe("props validation", () => {
    it("accepts ReactNode for statusA", () => {
      const complexStatusA = (
        <div>
          <span>Complex</span>
          <button>Button</button>
          <div>Nested content</div>
        </div>
      );

      testRender(
        <SideBySideEvaluatorColumn statusA={complexStatusA} statusB={null} />,
      );

      expect(screen.getByText("Complex")).toBeInTheDocument();
      expect(screen.getByRole("button")).toBeInTheDocument();
      expect(screen.getByText("Nested content")).toBeInTheDocument();
    });

    it("accepts ReactNode for statusB", () => {
      const complexStatusB = (
        <div>
          <span>Complex B</span>
          <input type="text" placeholder="Input field" />
        </div>
      );

      testRender(
        <SideBySideEvaluatorColumn statusA={null} statusB={complexStatusB} />,
      );

      expect(screen.getByText("Complex B")).toBeInTheDocument();
      expect(screen.getByPlaceholderText("Input field")).toBeInTheDocument();
    });

    it("handles string content in status props", () => {
      const stringStatusA = "String status A";
      const stringStatusB = "String status B";

      testRender(
        <SideBySideEvaluatorColumn
          statusA={stringStatusA}
          statusB={stringStatusB}
        />,
      );

      expect(screen.getByText("String status A")).toBeInTheDocument();
      expect(screen.getByText("String status B")).toBeInTheDocument();
    });

    it("handles number content in status props", () => {
      const numberStatusA = 42;
      const numberStatusB = 100;

      testRender(
        <SideBySideEvaluatorColumn
          statusA={numberStatusA}
          statusB={numberStatusB}
        />,
      );

      expect(screen.getByText("42")).toBeInTheDocument();
      expect(screen.getByText("100")).toBeInTheDocument();
    });
  });
});
