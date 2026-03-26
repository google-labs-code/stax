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

import SideBySideModelLetter from "@/components/SideBySideModelLetter";
import { render, screen } from "@testing-library/react";

describe("SideBySideModelLetter", () => {
  it("renders without crashing", () => {
    render(<SideBySideModelLetter letter="A" />);
    expect(screen.getByText("A")).toBeInTheDocument();
  });

  it("renders the correct letter", () => {
    render(<SideBySideModelLetter letter="Z" />);
    expect(screen.getByText("Z")).toBeInTheDocument();
  });

  it("has correct class names and styles", () => {
    render(<SideBySideModelLetter letter="M" />);
    const element = screen.getByText("M");

    expect(element).toHaveClass("bg-violetBg");
    expect(element).toHaveClass("rounded-xs");
    expect(element).toHaveClass("min-w-[16px]");
    expect(element).toHaveClass("w-[16px]");
    expect(element).toHaveClass("h-[16px]");
    expect(element).toHaveClass("flex");
    expect(element).toHaveClass("justify-center");
    expect(element).toHaveClass("items-center");
    expect(element).toHaveClass("text-body-10");
    expect(element).toHaveClass("text-brand");
  });
});
