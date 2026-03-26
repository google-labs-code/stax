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

import BreadcrumbSegment from "@/components/BreadcrumbSegment";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

// Mock next/navigation
const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

describe("BreadcrumbSegment", () => {
  const defaultProps = {
    label: "Test Label",
    routes: "/test-route",
  };

  const renderComponent = (props = {}) => {
    const componentProps = { ...defaultProps, ...props };

    return testRender(<BreadcrumbSegment {...componentProps} />);
  };

  beforeEach(() => {
    mockPush.mockClear();
  });

  it("should render the label text", () => {
    renderComponent();

    const labelElement = screen.getByText("Test Label");
    expect(labelElement).toBeInTheDocument();
  });

  it("should render with correct styling classes", () => {
    renderComponent();

    const labelElement = screen.getByText("Test Label");
    expect(labelElement).toHaveClass(
      "mr-1",
      "cursor-pointer",
      "text-secondary",
      "text-title-21",
    );
  });

  it("should show arrow by default", () => {
    renderComponent();

    const arrowElement = screen.getByText("chevron_right");
    expect(arrowElement).toBeInTheDocument();
  });

  it("should not show arrow when showArrow is false", () => {
    renderComponent({ showArrow: false });

    const arrowElement = screen.queryByText("chevron_right");
    expect(arrowElement).not.toBeInTheDocument();
  });

  it("should call router.push when clicked", () => {
    renderComponent();

    const labelElement = screen.getByText("Test Label");
    fireEvent.click(labelElement);

    expect(mockPush).toHaveBeenCalledWith("/test-route");
  });

  it("should call router.push with custom route when provided", () => {
    renderComponent({ routes: "/custom-route" });

    const labelElement = screen.getByText("Test Label");
    fireEvent.click(labelElement);

    expect(mockPush).toHaveBeenCalledWith("/custom-route");
  });

  it("should render with custom label", () => {
    renderComponent({ label: "Custom Label" });

    const labelElement = screen.getByText("Custom Label");
    expect(labelElement).toBeInTheDocument();
  });
});
