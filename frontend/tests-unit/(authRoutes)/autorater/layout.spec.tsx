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

import RootLayout from "@/app/(authRoutes)/autorater/layout";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/autorater/components/SideNavigation", () => ({
  __esModule: true,
  default: () => <div data-testid="mock-side-navigation">SideNav Mock</div>,
}));

describe("Autorater RootLayout", () => {
  it("renders the layout with SideNavigation and content", () => {
    const childContent = <div data-testid="child-content">Child Content</div>;
    const modalContent = <div data-testid="modal-content">Modal Content</div>;

    testRender(<RootLayout modal={modalContent}>{childContent}</RootLayout>);

    expect(screen.getByTestId("mock-side-navigation")).toBeInTheDocument();
    expect(screen.getByTestId("child-content")).toBeInTheDocument();
    expect(screen.getByText("Child Content")).toBeInTheDocument();
    expect(screen.getByTestId("modal-content")).toBeInTheDocument();
    expect(screen.getByText("Modal Content")).toBeInTheDocument();

    const mainContainer = screen.getByText("Child Content").closest("main");
    expect(mainContainer).toBeInTheDocument();
    expect(mainContainer).toHaveClass("flex");
    expect(mainContainer).toHaveClass("h-full");
    expect(mainContainer).toHaveClass("grow");
    expect(mainContainer).toHaveClass("basis-10/12");
  });

  it("renders without modal content", () => {
    const childContent = (
      <div data-testid="child-content">Only Child Content</div>
    );

    testRender(<RootLayout modal={null}>{childContent}</RootLayout>);

    expect(screen.getByTestId("mock-side-navigation")).toBeInTheDocument();
    expect(screen.getByTestId("child-content")).toBeInTheDocument();
    expect(screen.getByText("Only Child Content")).toBeInTheDocument();
    expect(screen.queryByTestId("modal-content")).not.toBeInTheDocument();
  });
});
