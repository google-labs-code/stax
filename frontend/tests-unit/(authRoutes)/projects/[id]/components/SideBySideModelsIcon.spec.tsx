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

import SideBySideModelsIcon from "@/app/(authRoutes)/projects/[id]/components/SideBySideModelsIcon";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

describe("SideBySideModelsIcon", () => {
  it("renders letter B and expected classes", () => {
    testRender(<SideBySideModelsIcon variant="second" />);

    const wrapper = screen.getByText("B").parentElement;
    const letter = screen.getByText("B");
    expect(wrapper).toHaveClass("flex");
    expect(wrapper).toHaveClass(
      "h-5",
      "w-5",
      "items-center",
      "justify-center",
      "rounded",
      "bg-lightBlue",
    );
    expect(letter.tagName).toBe("P");
    expect(letter).toHaveTextContent("B");
    expect(letter).toHaveClass("text-brand");
    expect(letter).toHaveClass("mantine-Text-root");
  });

  it("renders letter A and transparent background when greyedOut", () => {
    testRender(<SideBySideModelsIcon variant="first" greyedOut />);

    const wrapper = screen.getByText("A").parentElement;
    const letter = screen.getByText("A");
    expect(wrapper).toHaveClass("bg-transparent");

    expect(letter).toHaveTextContent("A");
    expect(letter).toHaveClass("text-white");
  });
});
