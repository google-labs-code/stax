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

import SectionHeader from "@/app/(authRoutes)/evaluatorGallery/[id]/components/SectionHeader";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

describe("SectionHeader", () => {
  const defaultProps = {
    title: "Test Title",
    description: "Test Description",
  };

  it("renders with title and description", () => {
    testRender(<SectionHeader {...defaultProps} />);

    expect(screen.getByText("Test Title")).toBeInTheDocument();
    expect(screen.getByText("Test Description")).toBeInTheDocument();
  });

  it("renders with different title and description", () => {
    const props = {
      title: "Another Title",
      description: "Another Description",
    };

    testRender(<SectionHeader {...props} />);

    expect(screen.getByText("Another Title")).toBeInTheDocument();
    expect(screen.getByText("Another Description")).toBeInTheDocument();
  });

  it("renders with long text content", () => {
    const props = {
      title: "This is a very long title that might wrap to multiple lines",
      description:
        "This is a very long description that contains a lot of text and might also wrap to multiple lines to test how the component handles longer content",
    };

    testRender(<SectionHeader {...props} />);

    expect(screen.getByText(props.title)).toBeInTheDocument();
    expect(screen.getByText(props.description)).toBeInTheDocument();
  });

  it("has correct CSS classes for styling", () => {
    const { container } = testRender(<SectionHeader {...defaultProps} />);

    // Check that the Group component has the expected classes
    const groupElement = container.querySelector(
      '[class*="flex flex-col items-start"]',
    );
    expect(groupElement).toBeInTheDocument();

    // Check that the title has the expected classes
    const titleElement = screen.getByText("Test Title");
    expect(titleElement).toHaveClass(
      "text-body-16",
      "!font-medium",
      "flex",
      "flex-row",
      "items-center",
    );

    // Check that the description has the expected classes
    const descriptionElement = screen.getByText("Test Description");
    expect(descriptionElement).toHaveClass("text-body-14");
  });
});
