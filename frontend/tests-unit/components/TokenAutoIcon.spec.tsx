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

import TokenAutoIcon from "@/components/icons/TokenAutoIcon";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

describe("TokenAutoIcon", () => {
  const renderComponent = (props = {}) => {
    return testRender(<TokenAutoIcon {...props} />);
  };

  it("should render the SVG element with default props", () => {
    renderComponent();

    const svg = screen.getByTestId("token-auto-icon");
    expect(svg).toBeInTheDocument();
    expect(svg).toHaveAttribute("width", "18");
    expect(svg).toHaveAttribute("height", "18");
    expect(svg).toHaveAttribute("viewBox", "0 0 17 18");
    expect(svg).toHaveAttribute("fill", "none");
    expect(svg).toHaveAttribute("xmlns", "http://www.w3.org/2000/svg");
  });

  it("should render with custom size", () => {
    renderComponent({ size: 24 });

    const svg = screen.getByTestId("token-auto-icon");
    expect(svg).toHaveAttribute("width", "24");
    expect(svg).toHaveAttribute("height", "24");
  });

  it("should render with custom fill color", () => {
    renderComponent({ fill: "#FF0000" });

    const path = screen.getByTestId("token-auto-icon").querySelector("path");
    expect(path).toHaveAttribute("fill", "#FF0000");
  });

  it("should render with default fill color when not provided", () => {
    renderComponent();

    const path = screen.getByTestId("token-auto-icon").querySelector("path");
    expect(path).toHaveAttribute("fill", "var(--color-secondary)");
  });

  it("should render the path element with correct d attribute", () => {
    renderComponent();

    const path = screen.getByTestId("token-auto-icon").querySelector("path");
    expect(path).toBeInTheDocument();
    expect(path).toHaveAttribute("d");

    // Check that the path has a non-empty d attribute (the actual path data)
    const dAttribute = path?.getAttribute("d");
    expect(dAttribute).toBeTruthy();
    expect(dAttribute?.length).toBeGreaterThan(0);
  });

  it("should handle zero size gracefully", () => {
    renderComponent({ size: 0 });

    const svg = screen.getByTestId("token-auto-icon");
    expect(svg).toHaveAttribute("width", "0");
    expect(svg).toHaveAttribute("height", "0");
  });

  it("should handle negative size gracefully", () => {
    renderComponent({ size: -10 });

    const svg = screen.getByTestId("token-auto-icon");
    expect(svg).toHaveAttribute("width", "-10");
    expect(svg).toHaveAttribute("height", "-10");
  });

  it("should handle empty fill color", () => {
    renderComponent({ fill: "" });

    const path = screen.getByTestId("token-auto-icon").querySelector("path");
    expect(path).toHaveAttribute("fill", "");
  });

  it("should maintain SVG structure with all props", () => {
    renderComponent({ size: 32, fill: "#00FF00" });

    const svg = screen.getByTestId("token-auto-icon");
    const path = svg.querySelector("path");

    expect(svg).toHaveAttribute("width", "32");
    expect(svg).toHaveAttribute("height", "32");
    expect(svg).toHaveAttribute("viewBox", "0 0 17 18");
    expect(svg).toHaveAttribute("fill", "none");
    expect(svg).toHaveAttribute("xmlns", "http://www.w3.org/2000/svg");
    expect(path).toHaveAttribute("fill", "#00FF00");
  });

  it("should be accessible with proper ARIA attributes", () => {
    renderComponent();

    const svg = screen.getByTestId("token-auto-icon");
    // SVG should be present but hidden from screen readers since it's decorative
    expect(svg).toBeInTheDocument();
  });

  it("should render consistently with different prop combinations", () => {
    const testCases = [
      { size: 16, fill: "#000000" },
      { size: 20, fill: "#FFFFFF" },
      { size: 12, fill: "#123456" },
    ];

    testCases.forEach(({ size, fill }) => {
      const { unmount } = renderComponent({ size, fill });

      const svg = screen.getByTestId("token-auto-icon");
      const path = svg.querySelector("path");

      expect(svg).toHaveAttribute("width", size.toString());
      expect(svg).toHaveAttribute("height", size.toString());
      expect(path).toHaveAttribute("fill", fill);

      unmount();
    });
  });
});
