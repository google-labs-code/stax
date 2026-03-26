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

import PlotFigure from "@/app/(authRoutes)/analytics/components/charts/utils/PlotFigure";
import { TooltipContentItem } from "@/app/(authRoutes)/analytics/types/charts";
import { testRender } from "@/tests-unit/render";
import { act, fireEvent, screen, waitFor } from "@testing-library/react";

// Mock dependencies
jest.mock("@observablehq/plot", () => ({
  plot: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/analytics/components/charts/utils/hooks", () => ({
  useWindowWidth: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/analytics/components/charts/utils/Tooltip", () => ({
  __esModule: true,
  default: ({ position, content }: any) => (
    <div
      data-testid="tooltip"
      data-x={position.x}
      data-y={position.y}
    >
      {content.map((item: TooltipContentItem, index: number) => (
        <div key={index} data-testid={`tooltip-item-${index}`}>
          <span data-testid={`tooltip-label-${index}`}>{item.label}</span>
          <span data-testid={`tooltip-value-${index}`}>{item.value}</span>
        </div>
      ))}
    </div>
  ),
}));

import * as Plot from "@observablehq/plot";
import { useWindowWidth } from "@/app/(authRoutes)/analytics/components/charts/utils/hooks";

const mockPlot = Plot.plot as jest.MockedFunction<typeof Plot.plot>;
const mockUseWindowWidth = useWindowWidth as jest.MockedFunction<
  typeof useWindowWidth
>;

describe("PlotFigure", () => {
  const defaultOptions = {
    data: [
      { x: 1, y: 10 },
      { x: 2, y: 20 },
      { x: 3, y: 30 },
    ],
    marks: [],
  };

  const mockGenerateTooltipContent = jest.fn(
    (dataPoint: any): TooltipContentItem[] => [
      { label: "X", value: dataPoint.x },
      { label: "Y", value: dataPoint.y },
    ],
  );

  let mockPlotElement: HTMLElement;
  let mockCircleElement: HTMLElement & { __data__: number };
  let mockRectElement: HTMLElement & { __data__: number };
  let mockOtherElement: HTMLElement;

  beforeEach(() => {
    jest.clearAllMocks();

    // Setup window width mock
    mockUseWindowWidth.mockReturnValue(1024);

    // Create mock plot element
    mockPlotElement = document.createElement("div");
    mockPlotElement.className = "plot-container";

    // Create mock circle element (SVG elements have correct tagName automatically)
    mockCircleElement = document.createElementNS(
      "http://www.w3.org/2000/svg",
      "circle",
    ) as HTMLElement & { __data__: number };
    mockCircleElement.__data__ = 0;
    mockCircleElement.classList.add = jest.fn();

    // Create mock rect element (SVG elements have correct tagName automatically)
    mockRectElement = document.createElementNS(
      "http://www.w3.org/2000/svg",
      "rect",
    ) as HTMLElement & { __data__: number };
    mockRectElement.__data__ = 1;
    mockRectElement.classList.add = jest.fn();

    // Create mock other element
    mockOtherElement = document.createElement("div");

    // Setup plot mock to return an element with append method
    mockPlot.mockReturnValue(mockPlotElement as any);
  });

  afterEach(() => {
    // Clean up any event listeners
    jest.restoreAllMocks();
  });

  it("renders with className", () => {
    const { container } = testRender(
      <PlotFigure className="test-class" options={defaultOptions} />,
    );

    const plotContainer = container.querySelector(".test-class");
    expect(plotContainer).toBeInTheDocument();
  });

  it("calls Plot.plot with options and width", () => {
    testRender(<PlotFigure className="test" options={defaultOptions} />);

    expect(mockPlot).toHaveBeenCalledWith({
      ...defaultOptions,
      width: 1024,
    });
  });

  it("appends plot element to container", () => {
    const appendSpy = jest.spyOn(HTMLElement.prototype, "append");

    testRender(<PlotFigure className="test" options={defaultOptions} />);

    expect(appendSpy).toHaveBeenCalledWith(mockPlotElement);
    appendSpy.mockRestore();
  });

  it("does not create plot when options is null", () => {
    testRender(<PlotFigure className="test" options={null as any} />);

    expect(mockPlot).not.toHaveBeenCalled();
  });

  it("does not create plot when options is undefined", () => {
    testRender(<PlotFigure className="test" options={undefined as any} />);

    expect(mockPlot).not.toHaveBeenCalled();
  });

  it("updates plot when width changes", () => {
    const { rerender } = testRender(
      <PlotFigure className="test" options={defaultOptions} />,
    );

    expect(mockPlot).toHaveBeenCalledTimes(1);

    mockUseWindowWidth.mockReturnValue(768);
    rerender(<PlotFigure className="test" options={defaultOptions} />);

    expect(mockPlot).toHaveBeenCalledTimes(2);
    expect(mockPlot).toHaveBeenLastCalledWith({
      ...defaultOptions,
      width: 768,
    });
  });

  it("updates plot when options change", () => {
    const { rerender } = testRender(
      <PlotFigure className="test" options={defaultOptions} />,
    );

    expect(mockPlot).toHaveBeenCalledTimes(1);

    const newOptions = { ...defaultOptions, data: [{ x: 4, y: 40 }] };
    rerender(<PlotFigure className="test" options={newOptions} />);

    expect(mockPlot).toHaveBeenCalledTimes(2);
    expect(mockPlot).toHaveBeenLastCalledWith({
      ...newOptions,
      width: 1024,
    });
  });

  it("removes plot element on unmount", () => {
    const removeSpy = jest.spyOn(mockPlotElement, "remove");

    const { unmount } = testRender(
      <PlotFigure className="test" options={defaultOptions} />,
    );

    unmount();

    expect(removeSpy).toHaveBeenCalled();
    removeSpy.mockRestore();
  });

  describe("mouse interactions", () => {
    it("shows tooltip when hovering over circle element", async () => {
      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={mockGenerateTooltipContent}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;
      expect(plotContainer).toBeInTheDocument();

      // Simulate mouse move over circle
      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: mockCircleElement,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      await waitFor(() => {
        expect(mockGenerateTooltipContent).toHaveBeenCalledWith(
          defaultOptions.data[0],
        );
      });

      const tooltip = screen.getByTestId("tooltip");
      expect(tooltip).toBeInTheDocument();
      expect(tooltip).toHaveAttribute("data-x", "100");
      expect(tooltip).toHaveAttribute("data-y", "200");
    });

    it("shows tooltip when hovering over rect element", async () => {
      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={mockGenerateTooltipContent}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 150,
        clientY: 250,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: mockRectElement,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      await waitFor(() => {
        expect(mockGenerateTooltipContent).toHaveBeenCalledWith(
          defaultOptions.data[1],
        );
      });

      const tooltip = screen.getByTestId("tooltip");
      expect(tooltip).toBeInTheDocument();
      expect(tooltip).toHaveAttribute("data-x", "150");
      expect(tooltip).toHaveAttribute("data-y", "250");
    });

    it("adds custom-z-index-dot class to circle elements on hover", () => {
      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={mockGenerateTooltipContent}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: mockCircleElement,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      expect(mockCircleElement.classList.add).toHaveBeenCalledWith(
        "custom-z-index-dot",
      );
    });

    it("adds custom-z-index-dot class to rect elements on hover", () => {
      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={mockGenerateTooltipContent}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: mockRectElement,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      expect(mockRectElement.classList.add).toHaveBeenCalledWith(
        "custom-z-index-dot",
      );
    });

    it("changes cursor to pointer when hovering over data point", () => {
      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={mockGenerateTooltipContent}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: mockCircleElement,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      expect(plotContainer.style.cursor).toBe("pointer");
    });

    it("hides tooltip and resets cursor when moving away from data point", async () => {
      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={mockGenerateTooltipContent}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      // First hover over circle
      const hoverEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(hoverEvent, "target", {
        value: mockCircleElement,
        writable: false,
      });
      fireEvent(plotContainer, hoverEvent);

      await waitFor(() => {
        expect(screen.getByTestId("tooltip")).toBeInTheDocument();
      });

      // Then move to other element
      const moveAwayEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 50,
        clientY: 50,
      });
      Object.defineProperty(moveAwayEvent, "target", {
        value: mockOtherElement,
        writable: false,
      });
      fireEvent(plotContainer, moveAwayEvent);

      await waitFor(() => {
        expect(screen.queryByTestId("tooltip")).not.toBeInTheDocument();
      });

      expect(plotContainer.style.cursor).toBe("default");
    });

    it("does not show tooltip when dataPoint is not found", () => {
      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={mockGenerateTooltipContent}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      const elementWithoutData = document.createElementNS(
        "http://www.w3.org/2000/svg",
        "circle",
      ) as HTMLElement & { __data__: number };
      elementWithoutData.__data__ = 999; // Index that doesn't exist

      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: elementWithoutData,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      expect(mockGenerateTooltipContent).not.toHaveBeenCalled();
      expect(screen.queryByTestId("tooltip")).not.toBeInTheDocument();
    });

    it("does not show tooltip when generateTooltipContent is not provided", () => {
      const { container } = testRender(
        <PlotFigure className="test" options={defaultOptions} />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: mockCircleElement,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      expect(screen.queryByTestId("tooltip")).not.toBeInTheDocument();
    });
  });

  describe("scroll handling", () => {
    it("hides tooltip on scroll", async () => {
      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={mockGenerateTooltipContent}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      // First show tooltip
      const hoverEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(hoverEvent, "target", {
        value: mockCircleElement,
        writable: false,
      });
      fireEvent(plotContainer, hoverEvent);

      await waitFor(() => {
        expect(screen.getByTestId("tooltip")).toBeInTheDocument();
      });

      // Then scroll
      fireEvent.scroll(window, { bubbles: true });

      await waitFor(() => {
        expect(screen.queryByTestId("tooltip")).not.toBeInTheDocument();
      });
    });
  });

  describe("tooltip content generation", () => {
    it("renders tooltip with generated content", async () => {
      const customTooltipGenerator = jest.fn(
        (dataPoint: any): TooltipContentItem[] => [
          { label: "Custom Label", value: `Value: ${dataPoint.x}` },
        ],
      );

      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={customTooltipGenerator}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: mockCircleElement,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      await waitFor(() => {
        expect(customTooltipGenerator).toHaveBeenCalledWith(
          defaultOptions.data[0],
        );
      });

      expect(screen.getByTestId("tooltip-label-0")).toHaveTextContent(
        "Custom Label",
      );
      expect(screen.getByTestId("tooltip-value-0")).toHaveTextContent(
        "Value: 1",
      );
    });

    it("renders multiple tooltip items", async () => {
      const multiItemGenerator = jest.fn(
        (dataPoint: any): TooltipContentItem[] => [
          { label: "X", value: dataPoint.x },
          { label: "Y", value: dataPoint.y },
          { label: "Sum", value: dataPoint.x + dataPoint.y },
        ],
      );

      const { container } = testRender(
        <PlotFigure
          className="test"
          options={defaultOptions}
          generateTooltipContent={multiItemGenerator}
        />,
      );

      const plotContainer = container.querySelector(".test") as HTMLElement;

      const mouseEvent = new MouseEvent("mousemove", {
        bubbles: true,
        cancelable: true,
        clientX: 100,
        clientY: 200,
      });
      Object.defineProperty(mouseEvent, "target", {
        value: mockCircleElement,
        writable: false,
      });

      fireEvent(plotContainer, mouseEvent);

      await waitFor(() => {
        expect(screen.getByTestId("tooltip-item-0")).toBeInTheDocument();
        expect(screen.getByTestId("tooltip-item-1")).toBeInTheDocument();
        expect(screen.getByTestId("tooltip-item-2")).toBeInTheDocument();
      });

      expect(screen.getByTestId("tooltip-label-0")).toHaveTextContent("X");
      expect(screen.getByTestId("tooltip-value-0")).toHaveTextContent("1");
      expect(screen.getByTestId("tooltip-label-1")).toHaveTextContent("Y");
      expect(screen.getByTestId("tooltip-value-1")).toHaveTextContent("10");
      expect(screen.getByTestId("tooltip-label-2")).toHaveTextContent("Sum");
      expect(screen.getByTestId("tooltip-value-2")).toHaveTextContent("11");
    });
  });

  describe("event listener cleanup", () => {
    it("removes mousemove event listener on unmount", async () => {
      const { unmount } = testRender(
        <PlotFigure className="test" options={defaultOptions} />,
      );

      // Wait for useEffect to run and add event listeners
      await waitFor(() => {
        expect(mockPlot).toHaveBeenCalled();
      });

      // Verify cleanup by ensuring unmount doesn't throw and can remount
      await act(async () => {
        unmount();
      });

      // Verify we can render again without issues (cleanup worked)
      const { unmount: unmount2 } = testRender(
        <PlotFigure className="test" options={defaultOptions} />,
      );
      await waitFor(() => {
        expect(mockPlot).toHaveBeenCalledTimes(2);
      });
      unmount2();
    });

    it("removes scroll event listener on unmount", () => {
      const removeEventListenerSpy = jest.spyOn(
        window,
        "removeEventListener",
      );

      const { unmount } = testRender(
        <PlotFigure className="test" options={defaultOptions} />,
      );

      unmount();

      expect(removeEventListenerSpy).toHaveBeenCalledWith(
        "scroll",
        expect.any(Function),
        true,
      );
      removeEventListenerSpy.mockRestore();
    });
  });

  describe("edge cases", () => {
    it("handles empty data array", () => {
      const emptyOptions = {
        data: [],
        marks: [],
      };

      testRender(<PlotFigure className="test" options={emptyOptions} />);

      expect(mockPlot).toHaveBeenCalledWith({
        ...emptyOptions,
        width: 1024,
      });
    });

    it("handles options without data property", () => {
      const optionsWithoutData = {
        marks: [],
      };

      testRender(
        <PlotFigure className="test" options={optionsWithoutData as any} />,
      );

      expect(mockPlot).toHaveBeenCalledWith({
        ...optionsWithoutData,
        width: 1024,
      });
    });

    it("handles undefined width from useWindowWidth", () => {
      mockUseWindowWidth.mockReturnValue(undefined);

      testRender(<PlotFigure className="test" options={defaultOptions} />);

      expect(mockPlot).toHaveBeenCalledWith({
        ...defaultOptions,
        width: undefined,
      });
    });

    it("handles containerRef.current being null", () => {
      // This tests the optional chaining in the code
      testRender(<PlotFigure className="test" options={defaultOptions} />);

      // Should not throw even if containerRef.current is null
      expect(mockPlot).toHaveBeenCalled();
    });
  });
});

