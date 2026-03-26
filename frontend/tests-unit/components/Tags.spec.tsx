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

import Tag from "@/components/Tag";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../render";

describe("Tag component", () => {
  it("renders tag text correctly (default chip)", () => {
    testRender(<Tag tag="Test Tag" />);
    expect(screen.getByText("Test Tag")).toBeInTheDocument();
  });

  it("renders chip with close icon", () => {
    testRender(<Tag tag="Close Tag" withCloseIcon />);
    expect(screen.getByText("Close Tag")).toBeInTheDocument();
    expect(screen.getByTestId("material-icon")).toHaveTextContent("close");
  });

  it("renders chip with edit icon", () => {
    testRender(<Tag tag="Edit Tag" withEditIcon />);
    expect(screen.getByText("Edit Tag")).toBeInTheDocument();
    expect(screen.getByTestId("material-icon")).toHaveTextContent("edit");
  });

  it("calls onClick when close icon is clicked", () => {
    const onClick = jest.fn();
    testRender(<Tag tag="Close Click" withCloseIcon onClick={onClick} />);
    fireEvent.click(screen.getByTestId("material-icon"));
    expect(onClick).toHaveBeenCalled();
  });

  it("calls onClick when edit icon is clicked", () => {
    const onClick = jest.fn();
    testRender(<Tag tag="Edit Click" withEditIcon onClick={onClick} />);
    fireEvent.click(screen.getByTestId("material-icon"));
    expect(onClick).toHaveBeenCalled();
  });

  it("calls onClick when default chip is clicked", () => {
    const onClick = jest.fn();
    testRender(<Tag tag="Clickable" onClick={onClick} />);
    fireEvent.click(screen.getByText("Clickable"));
    expect(onClick).toHaveBeenCalled();
  });

  it("applies styles when checked", () => {
    testRender(<Tag tag="Checked" checked />);
    const label = screen.getByText("Checked");
    expect(label.className).toMatch(/text-brand/);
  });
});
