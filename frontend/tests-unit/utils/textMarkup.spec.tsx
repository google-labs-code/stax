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

import {
  GetFormattedChildrenMarkup,
  ReplaceVariablesValueMarkupWithValue,
} from "@/utils/textMarkup";
import { render, screen } from "@testing-library/react";
import React from "react";

jest.mock("@mantine/core", () => ({
  Tooltip: ({
    label,
    children,
  }: {
    label: string;
    children: React.ReactNode;
  }) => (
    <div data-testid="tooltip" data-tooltip={label}>
      {children}
    </div>
  ),
}));

describe("ReplaceVariablesValueMarkupWithValue", () => {
  it("replaces a simple variablesValue tag with its value", () => {
    const input = 'This is <variablesValue value="important" /> text.';
    expect(ReplaceVariablesValueMarkupWithValue(input)).toBe(
      "This is important text.",
    );
  });

  it("replaces multiple variablesValue tags", () => {
    const input =
      '<variablesValue value="First" /> and <variablesValue value="Second" />';
    expect(ReplaceVariablesValueMarkupWithValue(input)).toBe(
      "First and Second",
    );
  });

  it("handles variablesValue tags with multiple attributes", () => {
    const input =
      'This is <variablesValue color="red" value="critical" tooltip="Important!" /> text.';
    expect(ReplaceVariablesValueMarkupWithValue(input)).toBe(
      "This is critical text.",
    );
  });

  it("handles strings with no variablesValue tags", () => {
    const input = "Plain text with no tags";
    expect(ReplaceVariablesValueMarkupWithValue(input)).toBe(
      "Plain text with no tags",
    );
  });
});

describe("GetFormattedChildrenMarkup", () => {
  it("returns the original children when no variablesValue tags are present", () => {
    const children = "Plain text";
    const result = GetFormattedChildrenMarkup(children);
    expect(result).toBe(children);
  });

  it("returns plain text when plainText option is true", () => {
    const children = [
      '<variablesValue value="Important" tooltip="Critical info" color="red" />',
    ];
    const result = GetFormattedChildrenMarkup(children, true);

    expect(result).toEqual(["Important"]);
  });

  it("processes multiple variablesValue tags in an array of children", () => {
    const children = [
      "Text before",
      '<variablesValue value="First" tooltip="First tooltip" color="blue" />',
      "Text between",
      '<variablesValue value="Second" tooltip="Second tooltip" color="green" />',
      "Text after",
    ];

    const { container } = render(
      <div>{GetFormattedChildrenMarkup(children)}</div>,
    );

    const tooltips = screen.getAllByTestId("tooltip");
    expect(tooltips).toHaveLength(2);

    expect(tooltips[0]).toHaveAttribute("data-tooltip", "First tooltip");
    const span1 = tooltips[0].querySelector("span");
    expect(span1).toBeInTheDocument();
    expect(span1).toHaveTextContent("First");
    expect(span1?.getAttribute("style")).toContain("color");
    expect(span1?.getAttribute("style")).toContain("blue");

    expect(tooltips[1]).toHaveAttribute("data-tooltip", "Second tooltip");
    const span2 = tooltips[1].querySelector("span");
    expect(span2).toBeInTheDocument();
    expect(span2).toHaveTextContent("Second");
    expect(span2?.getAttribute("style")).toContain("color");
    expect(span2?.getAttribute("style")).toContain("green");

    expect(container.textContent).toContain("Text before");
    expect(container.textContent).toContain("Text between");
    expect(container.textContent).toContain("Text after");
  });

  it("handles tags with missing attributes", () => {
    const children = ['<variablesValue value="Just Value" />'];

    render(<div>{GetFormattedChildrenMarkup(children)}</div>);

    const tooltip = screen.getByTestId("tooltip");
    expect(tooltip).toHaveAttribute("data-tooltip", "");

    const span = tooltip.querySelector("span");
    expect(span).toBeInTheDocument();
    expect(span).toHaveTextContent("Just Value");
  });

  it("passes through non-string children unchanged", () => {
    const reactElement = <div data-testid="original">Original Element</div>;
    const children = ["Text", reactElement, "More text"];

    render(<div>{GetFormattedChildrenMarkup(children)}</div>);

    expect(screen.getByTestId("original")).toBeInTheDocument();
    expect(screen.getByTestId("original")).toHaveTextContent(
      "Original Element",
    );
  });

  it("handles empty arrays", () => {
    const result = GetFormattedChildrenMarkup([]);
    expect(result).toEqual([]);
  });

  it("handles null or undefined children", () => {
    const result1 = GetFormattedChildrenMarkup(null);
    const result2 = GetFormattedChildrenMarkup(undefined);

    expect(result1).toBeNull();
    expect(result2).toBeUndefined();
  });
});
