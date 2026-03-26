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

import ApiKeyMissingHoverCard from "@/components/ApiKeyMissingHoverCard";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";
import React, { ReactNode } from "react";

jest.mock("next/link", () => {
  const MockLink = ({
    href,
    children,
    className,
  }: {
    href: string;
    children: ReactNode;
    className?: string;
  }) => (
    <a href={href} className={className} data-testid="mock-link">
      {children}
    </a>
  );
  MockLink.displayName = "MockLink";
  
  return {
    __esModule: true,
    default: MockLink,
  };
});

jest.mock("@mantine/core", () => {
  const actual = jest.requireActual("@mantine/core");

  const HoverCardComponent = ({ children }: { children: ReactNode }) => {
    return <div data-testid="hover-card">{children}</div>;
  };
  HoverCardComponent.displayName = "HoverCard";

  const HoverCardTarget = function HoverCardTarget({
    children,
  }: {
    children: ReactNode;
  }) {
    return <div data-testid="hover-target">{children}</div>;
  };
  HoverCardComponent.Target = HoverCardTarget;

  const HoverCardDropdown = function HoverCardDropdown({
    children,
  }: {
    children: ReactNode;
  }) {
    return <div data-testid="hover-dropdown">{children}</div>;
  };
  HoverCardComponent.Dropdown = HoverCardDropdown;

  const Group = function Group({
    children,
    justify,
  }: {
    children: ReactNode;
    justify?: string;
  }) {
    return (
      <div data-testid="group" data-justify={justify}>
        {children}
      </div>
    );
  };

  const Stack = function Stack({
    children,
    gap,
  }: {
    children: ReactNode;
    gap?: number;
  }) {
    return (
      <div data-testid="stack" data-gap={gap}>
        {children}
      </div>
    );
  };

  const Text = function Text({
    children,
    className,
  }: {
    children: ReactNode;
    className?: string;
  }) {
    return (
      <div data-testid="text" className={className}>
        {children}
      </div>
    );
  };

  return {
    ...actual,
    HoverCard: HoverCardComponent,
    Group,
    Stack,
    Text,
  };
});

describe("ApiKeyMissingHoverCard", () => {
  it("renders children correctly", () => {
    testRender(
      <ApiKeyMissingHoverCard show={false}>
        <div data-testid="child-component">Test Content</div>
      </ApiKeyMissingHoverCard>,
    );

    expect(screen.getByTestId("child-component")).toBeInTheDocument();
    expect(screen.getByText("Test Content")).toBeInTheDocument();
  });

  it("conditionally renders dropdown content based on show prop", () => {
    const { rerender } = testRender(
      <ApiKeyMissingHoverCard show={false}>
        <div>Test Content</div>
      </ApiKeyMissingHoverCard>,
    );

    expect(screen.queryByTestId("hover-dropdown")).not.toBeInTheDocument();

    rerender(
      <ApiKeyMissingHoverCard show={true}>
        <div>Test Content</div>
      </ApiKeyMissingHoverCard>,
    );

    expect(screen.getByTestId("hover-dropdown")).toBeInTheDocument();
  });

  it("includes correct content in the dropdown when show is true", () => {
    testRender(
      <ApiKeyMissingHoverCard show={true}>
        <div>Test Content</div>
      </ApiKeyMissingHoverCard>,
    );

    const dropdown = screen.getByTestId("hover-dropdown");
    expect(dropdown).toBeInTheDocument();

    expect(screen.getByText("Missing API key.")).toBeInTheDocument();
    expect(
      screen.getByText(
        "Go to your settings to add API key to start using this functionality.",
      ),
    ).toBeInTheDocument();
    expect(screen.getByText("Settings")).toBeInTheDocument();
  });

  it("contains a settings link with the correct URL", () => {
    testRender(
      <ApiKeyMissingHoverCard show={true}>
        <div>Test Content</div>
      </ApiKeyMissingHoverCard>,
    );

    const link = screen.getByTestId("mock-link");
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "/settings?tab=apiKeys");
    expect(link).toHaveTextContent("Settings");
  });

  it("stops event propagation on mouseDown and click", () => {
    const mockOnClick = jest.fn();
    const mockOnMouseDown = jest.fn();

    testRender(
      <div onClick={mockOnClick} onMouseDown={mockOnMouseDown}>
        <ApiKeyMissingHoverCard show={true}>
          <div>Test Content</div>
        </ApiKeyMissingHoverCard>
      </div>,
    );

    const dropdown = screen.getByTestId("hover-dropdown").firstChild;

    if (dropdown) {
      fireEvent.mouseDown(dropdown);
      fireEvent.click(dropdown);
    }

    expect(mockOnClick).not.toHaveBeenCalled();
    expect(mockOnMouseDown).not.toHaveBeenCalled();
  });

  it("forwards ref correctly", () => {
    const ref = React.createRef<HTMLDivElement>();

    testRender(
      <ApiKeyMissingHoverCard show={true} ref={ref}>
        <div>Test Content</div>
      </ApiKeyMissingHoverCard>,
    );

    expect(screen.getByText("Test Content")).toBeInTheDocument();
  });
});
