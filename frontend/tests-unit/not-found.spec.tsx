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

import NotFound from "@/app/not-found";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

describe("NotFound", () => {
  it("renders 404 error message", () => {
    testRender(<NotFound />);

    expect(screen.getByText("404")).toBeInTheDocument();
    expect(
      screen.getByText("This page could not be found."),
    ).toBeInTheDocument();
  });

  it("renders go back link", () => {
    testRender(<NotFound />);

    const goBackLink = screen.getByRole("link", { name: "Go back" });
    expect(goBackLink).toBeInTheDocument();
    expect(goBackLink).toHaveAttribute("href", "/");
  });

  it("renders with correct styling classes", () => {
    testRender(<NotFound />);

    // Check that the main container has the text-center class
    const container = screen.getByText("404").closest(".text-center");
    expect(container).toBeInTheDocument();

    // Check that the 404 span has the expected styling classes
    const errorCode = screen.getByText("404");
    expect(errorCode).toHaveClass(
      "text-2xl",
      "font-medium",
      "align-top",
      "border-r",
      "pr-[23px]",
      "border-gray-300",
    );
  });

  it("renders error message in correct text size", () => {
    testRender(<NotFound />);

    const errorMessage = screen.getByText("This page could not be found.");
    expect(errorMessage).toBeInTheDocument();
  });

  it("maintains proper layout structure", () => {
    testRender(<NotFound />);

    // Verify the component renders without throwing errors
    expect(screen.getByText("404")).toBeInTheDocument();
    expect(
      screen.getByText("This page could not be found."),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Go back" })).toBeInTheDocument();
  });
});
