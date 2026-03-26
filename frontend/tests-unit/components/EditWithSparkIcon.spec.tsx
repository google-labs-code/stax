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

import { EditWithSparkIcon } from "@/components/EditWithSparkIcon";
import { render } from "@testing-library/react";

describe("EditWithSparkIcon", () => {
  it("renders with default size and gradient", () => {
    const { container } = render(<EditWithSparkIcon />);
    const svg = container.querySelector("svg");
    const path = container.querySelector("path");

    expect(svg).toBeInTheDocument();
    expect(svg).toHaveAttribute("width", "20");
    expect(svg).toHaveAttribute("height", "20");

    expect(path?.getAttribute("fill")).toMatch(/^url\(#/);

    const defs = container.querySelector("defs");
    expect(defs).toBeInTheDocument();
  });

  it("renders with custom size", () => {
    const { container } = render(<EditWithSparkIcon size={40} />);
    const svg = container.querySelector("svg");

    expect(svg).toHaveAttribute("width", "40");
    expect(svg).toHaveAttribute("height", "40");
  });

  it("renders with a solid fill color and no gradient", () => {
    const { container } = render(<EditWithSparkIcon color="#ff0000" />);
    const path = container.querySelector("path");
    const defs = container.querySelector("defs");

    expect(path?.getAttribute("fill")).toBe("#ff0000");
    expect(defs?.querySelector("linearGradient")).not.toBeInTheDocument();
  });

  it("renders full SVG structure correctly", () => {
    const { container } = render(<EditWithSparkIcon />);
    const clipPath = container.querySelector("clipPath");
    const rect = container.querySelector("clipPath > rect");

    expect(clipPath).toBeInTheDocument();
    expect(rect).toHaveAttribute("width", "24");
    expect(rect).toHaveAttribute("height", "24");
  });
});
