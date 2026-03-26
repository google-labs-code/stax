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

import ActionMenu from "@/components/ActionMenu";
import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";

jest.mock("@/components/MaterialIcon", () => ({
  __esModule: true,
  default: ({ name }: { name: string }) => (
    <span data-testid={`icon-${name}`}>{name}</span>
  ),
}));

jest.mock("@mantine/core", () => {
  return {
    Menu: Object.assign(
      function Menu(props: any) {
        return (
          <div data-testid="menu">
            {props.children}
            <button
              data-testid="open-trigger"
              onClick={() => {
                if (props.onOpen) props.onOpen();
              }}
            />
            <button
              data-testid="close-trigger"
              onClick={() => {
                if (props.onClose) props.onClose();
              }}
            />
          </div>
        );
      },
      {
        Target: ({ children }: { children: React.ReactNode }) => (
          <div data-testid="menu-target">{children}</div>
        ),
        Dropdown: ({
          children,
          className,
        }: {
          children: React.ReactNode;
          className?: string;
        }) => (
          <div data-testid="menu-dropdown" className={className}>
            {children}
          </div>
        ),
        Item: ({ leftSection, children, onClick, ...props }: any) => (
          <button data-testid="menu-item" onClick={onClick} {...props}>
            {leftSection && <div data-testid="left-section">{leftSection}</div>}
            <span>{children}</span>
          </button>
        ),
      },
    ),
    ActionIcon: ({ children, ...props }: any) => (
      <button data-testid="action-icon" {...props}>
        {children}
      </button>
    ),
  };
});

describe("ActionMenu", () => {
  const mockOnClick1 = jest.fn();
  const mockOnClick2 = jest.fn();

  const mockMenuItems = [
    {
      label: "Item 1",
      leftSection: <span>Icon 1</span>,
      onClick: mockOnClick1,
    },
    {
      label: "Item 2",
      leftSection: <span>Icon 2</span>,
      onClick: mockOnClick2,
      component: "a",
      href: "https://example.com",
    },
    {
      label: "Item 3",
      leftSection: <span>Icon 3</span>,
      disabled: true,
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders without errors", () => {
    render(<ActionMenu menuItems={mockMenuItems} />);
    expect(screen.getByTestId("menu")).toBeInTheDocument();
  });

  it("renders the more_vert icon", () => {
    render(<ActionMenu menuItems={mockMenuItems} />);
    expect(screen.getByTestId("icon-more_vert")).toBeInTheDocument();
  });

  it("renders all menu items", () => {
    render(<ActionMenu menuItems={mockMenuItems} />);
    const menuItems = screen.getAllByTestId("menu-item");
    expect(menuItems).toHaveLength(3);
    expect(menuItems[0].textContent).toContain("Item 1");
    expect(menuItems[1].textContent).toContain("Item 2");
    expect(menuItems[2].textContent).toContain("Item 3");
  });

  it("applies correct props to menu items", () => {
    render(<ActionMenu menuItems={mockMenuItems} />);
    const menuItems = screen.getAllByTestId("menu-item");

    expect(menuItems[1].getAttribute("href")).toBe("https://example.com");
    expect(menuItems[1].getAttribute("component")).toBe("a");
    expect(menuItems[2].getAttribute("disabled")).not.toBeNull();
  });

  it("calls onClick handler when menu item is clicked", () => {
    render(<ActionMenu menuItems={mockMenuItems} />);
    const menuItems = screen.getAllByTestId("menu-item");

    fireEvent.click(menuItems[0]);
    expect(mockOnClick1).toHaveBeenCalledTimes(1);

    fireEvent.click(menuItems[1]);
    expect(mockOnClick2).toHaveBeenCalledTimes(1);
  });

  it("updates state when menu opens", () => {
    const setStateMock = jest.fn();
    jest
      .spyOn(React, "useState")
      .mockImplementationOnce(() => [false, setStateMock]);

    render(<ActionMenu menuItems={mockMenuItems} />);

    fireEvent.click(screen.getByTestId("open-trigger"));

    expect(setStateMock).toHaveBeenCalledWith(true);
  });

  it("updates state when menu closes", () => {
    const setStateMock = jest.fn();
    jest
      .spyOn(React, "useState")
      .mockImplementationOnce(() => [true, setStateMock]);

    render(<ActionMenu menuItems={mockMenuItems} />);

    fireEvent.click(screen.getByTestId("close-trigger"));

    expect(setStateMock).toHaveBeenCalledWith(false);
  });

  it("applies correct styling to menu dropdown", () => {
    render(<ActionMenu menuItems={mockMenuItems} />);
    const dropdown = screen.getByTestId("menu-dropdown");
    expect(dropdown.className).toContain("top-2");
  });
});
