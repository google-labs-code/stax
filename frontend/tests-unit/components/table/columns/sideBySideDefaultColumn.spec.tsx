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

import SideBySideDefaultColumn from "@/components/table/columns/SideBySideDefaultColumn";
import { screen } from "@testing-library/react";

import { testRender } from "../../../render";

describe("SideBySideDefaultColumn", () => {
  describe("rendering", () => {
    it("renders with both values", () => {
      const props = {
        valueA: "Test value A",
        valueB: "Test value B",
        difference: "0.5",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      expect(screen.getByText("Test value A")).toBeInTheDocument();
      expect(screen.getByText("Test value B")).toBeInTheDocument();
      expect(screen.getByText("0.5")).toBeInTheDocument();
      expect(screen.getByText("Difference")).toBeInTheDocument();
    });

    it("renders with only valueA", () => {
      const props = {
        valueA: "Only value A",
        valueB: "",
        difference: null,
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      expect(screen.getByText("Only value A")).toBeInTheDocument();
      expect(screen.getAllByText("-")).toHaveLength(2); // valueB fallback and difference fallback
      expect(screen.getByText("Difference")).toBeInTheDocument();
    });

    it("renders with only valueB", () => {
      const props = {
        valueA: "",
        valueB: "Only value B",
        difference: "-0.3",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      expect(screen.getByText("-")).toBeInTheDocument(); // valueA fallback
      expect(screen.getByText("Only value B")).toBeInTheDocument();
      expect(screen.getByText("-0.3")).toBeInTheDocument();
    });

    it("renders with empty strings as values", () => {
      const props = {
        valueA: "",
        valueB: "",
        difference: "1.2",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      // When both values are empty, the component returns null, so we can't test for content
      // This test case is actually covered by the conditional rendering tests
      const { container } = testRender(<SideBySideDefaultColumn {...props} />);
      expect(container.children).toHaveLength(2); // Only style tags
    });
  });

  describe("conditional rendering", () => {
    it("returns null when both valueA and valueB are empty", () => {
      const props = {
        valueA: "",
        valueB: "",
        difference: "0.5",
      };

      const { container } = testRender(<SideBySideDefaultColumn {...props} />);
      // The component returns null, so only style tags from Mantine should be present
      expect(container.children).toHaveLength(2);
      expect(container.firstChild?.nodeName).toBe("STYLE");
    });

    it("returns null when both valueA and valueB are null", () => {
      const props = {
        valueA: null as any,
        valueB: null as any,
        difference: "0.5",
      };

      const { container } = testRender(<SideBySideDefaultColumn {...props} />);
      // The component returns null, so only style tags from Mantine should be present
      expect(container.children).toHaveLength(2);
      expect(container.firstChild?.nodeName).toBe("STYLE");
    });

    it("renders when valueA is present even if valueB is empty", () => {
      const props = {
        valueA: "Value A present",
        valueB: "",
        difference: null,
      };

      testRender(<SideBySideDefaultColumn {...props} />);
      expect(screen.getByText("Value A present")).toBeInTheDocument();
    });

    it("renders when valueB is present even if valueA is empty", () => {
      const props = {
        valueA: "",
        valueB: "Value B present",
        difference: null,
      };

      testRender(<SideBySideDefaultColumn {...props} />);
      expect(screen.getByText("Value B present")).toBeInTheDocument();
    });
  });

  describe("difference display", () => {
    it("displays positive difference with lime color class", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "0.5",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      const differenceText = screen.getByText("0.5");
      expect(differenceText).toHaveClass("text-lime");
    });

    it("displays negative difference with red color class", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "-0.3",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      const differenceText = screen.getByText("-0.3");
      expect(differenceText).toHaveClass("text-red");
    });

    it("displays zero difference with lime color class", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "0",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      const differenceText = screen.getByText("0");
      expect(differenceText).toHaveClass("text-lime");
    });

    it("displays fallback when difference is null", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: null,
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      const differenceText = screen.getByText("-");
      expect(differenceText).toHaveClass("text-secondary");
    });

    it("displays fallback when difference is empty string", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      const differenceText = screen.getByText("-");
      expect(differenceText).toHaveClass("text-secondary");
    });
  });

  describe("SideBySideModelLetter integration", () => {
    it("renders SideBySideModelLetter components with correct letters", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "0.5",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      expect(screen.getByText("A")).toBeInTheDocument();
      expect(screen.getByText("B")).toBeInTheDocument();
    });
  });

  describe("CSS classes and styling", () => {
    it("applies correct CSS classes to container elements", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "0.5",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      // Check that the main container has the expected classes
      const mainGroup = screen.getByText("Value A").closest(".w-full");
      expect(mainGroup).toBeInTheDocument();

      // Check that ScrollArea components are present by looking for their specific classes
      const scrollAreaElements = document.querySelectorAll(
        ".mantine-ScrollArea-root",
      );
      expect(scrollAreaElements).toHaveLength(3); // Two for values, one for difference
    });

    it("applies correct text styling classes", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "0.5",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      const valueAText = screen.getByText("Value A");
      expect(valueAText).toHaveClass("text-body-12", "text-secondary");

      const valueBText = screen.getByText("Value B");
      expect(valueBText).toHaveClass("text-body-12", "text-secondary");

      const differenceLabel = screen.getByText("Difference");
      expect(differenceLabel).toHaveClass(
        "text-body-11",
        "text-resting",
        "!font-medium",
      );
    });
  });

  describe("edge cases", () => {
    it("handles very long values", () => {
      const longValue = "A".repeat(1000);
      const props = {
        valueA: longValue,
        valueB: "Short value",
        difference: "0.1",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      expect(screen.getByText(longValue)).toBeInTheDocument();
      expect(screen.getByText("Short value")).toBeInTheDocument();
    });

    it("handles special characters in values", () => {
      const props = {
        valueA: "Value with special chars: !@#$%^&*()",
        valueB: "Value with unicode: 你好世界 🌍",
        difference: "0.0",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      expect(
        screen.getByText("Value with special chars: !@#$%^&*()"),
      ).toBeInTheDocument();
      expect(
        screen.getByText("Value with unicode: 你好世界 🌍"),
      ).toBeInTheDocument();
    });

    it("handles numeric difference values", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "123.456",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      const differenceText = screen.getByText("123.456");
      expect(differenceText).toHaveClass("text-lime");
    });

    it("handles very small difference values", () => {
      const props = {
        valueA: "Value A",
        valueB: "Value B",
        difference: "0.0001",
      };

      testRender(<SideBySideDefaultColumn {...props} />);

      const differenceText = screen.getByText("0.0001");
      expect(differenceText).toHaveClass("text-lime");
    });
  });
});
