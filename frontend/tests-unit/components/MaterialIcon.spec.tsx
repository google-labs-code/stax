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

import MaterialIcon from "@/components/MaterialIcon";
import { fireEvent, screen } from "@testing-library/react";

import { testRenderLite } from "../render";

describe("MaterialIcon", () => {
  it("renders the icon with the given name", () => {
    testRenderLite(<MaterialIcon name="home" />);
    const icon = screen.getByTestId("material-icon");
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveTextContent("home");
  });

  it("adds 'text-disabled' class when disabled", () => {
    testRenderLite(<MaterialIcon name="star" disabled />);
    const icon = screen.getByTestId("material-icon");
    expect(icon).toHaveClass("text-disabled");
  });

  it("adds 'cursor-pointer' class when onClick handler is provided", () => {
    const onClick = jest.fn();
    testRenderLite(<MaterialIcon name="star" onClick={onClick} />);
    const icon = screen.getByTestId("material-icon");
    expect(icon).toHaveClass("cursor-pointer");

    fireEvent.click(icon);
    expect(onClick).toHaveBeenCalled();
  });

  it("uses the provided data-testid", () => {
    testRenderLite(<MaterialIcon name="custom" dataTestId="custom-icon" />);
    const icon = screen.getByTestId("custom-icon");
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveTextContent("custom");
  });
});
