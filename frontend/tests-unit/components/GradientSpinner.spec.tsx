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

import { GradientSpinner } from "@/components/GradientSpinner";
import { render } from "@testing-library/react";

describe("GradientSpinner", () => {
  it("renders with default size", () => {
    const { container } = render(<GradientSpinner />);

    const svg = container.querySelector("svg.animate-spin");
    expect(svg).toHaveAttribute("width", "20");
    expect(svg).toHaveAttribute("height", "20");
    expect(svg).toHaveClass("animate-spin");
  });

  it("renders with custom size", () => {
    const { container } = render(<GradientSpinner size={40} />);
    const svg = container.querySelector("svg.animate-spin");

    expect(svg).toHaveAttribute("width", "40");
    expect(svg).toHaveAttribute("height", "40");
  });

  it("uses a unique gradient id", () => {
    const { container } = render(<GradientSpinner />);
    const gradient = container.querySelector("linearGradient");
    const id = gradient?.getAttribute("id");

    expect(gradient).toBeInTheDocument();

    const circle = container.querySelector("circle");
    expect(circle?.getAttribute("stroke")).toBe(`url(#${id})`);
  });
});
