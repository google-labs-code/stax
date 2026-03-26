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

import SideNavigation from "@/app/(authRoutes)/autorater/components/SideNavigation";
import { useGlobalContext } from "@/hooks/useGlobalContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";
import React from "react";

interface NextImageProps {
  src: string;
  alt?: string;
  className?: string;
  style?: React.CSSProperties;
  width?: number | string;
  height?: number | string;
  priority?: boolean;
  loading?: "eager" | "lazy";
  quality?: number | string;
  [key: string]: any;
}

jest.mock("next/image", () => ({
  __esModule: true,
  default: ({ src, alt, className, style, width, height }: NextImageProps) => {
    return (
      <img
        src={src}
        alt={alt || ""}
        className={className}
        style={style}
        width={width}
        height={height}
        data-testid="next-image"
      />
    );
  },
}));

jest.mock("@/assets/StaxLogo.svg", () => ({
  __esModule: true,
  default: {
    src: "mocked-stax-logo",
    width: 100,
    height: 40,
  },
}));

jest.mock("@/config/routes", () => ({
  routes: {
    projects: "/projects",
    settings: "/settings",
  },
}));

jest.mock("@/components/SideNav/consts", () => ({
  AI_POLICY_TEXT: "AI Policy Text",
  DOCUMENTATION_LINK: "https://docs.example.com",
}));

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

jest.mock("@/hooks/useGlobalContext", () => ({
  useGlobalContext: jest.fn(),
}));

jest.mock("@/components/SideNav/SidenavLink", () => {
  return {
    __esModule: true,
    default: ({
      listItem,
      isSidenavOpen,
    }: {
      listItem: { key: string; icon?: string; label?: string };
      isSidenavOpen: boolean;
    }) => (
      <div data-testid={`sidenav-link-${listItem.key}`}>
        {listItem.icon && <span>{listItem.icon}</span>}
        {isSidenavOpen && listItem.label && <span>{listItem.label}</span>}
      </div>
    ),
  };
});

jest.mock("@/components/MaterialIcon", () => {
  return {
    __esModule: true,
    default: ({
      name,
      tooltipLabel,
    }: {
      name: string;
      tooltipLabel?: string;
      size?: number;
      className?: string;
      tooltipPosition?: string;
      tooltipOffset?: number;
      tooltipClassName?: string;
    }) => (
      <div data-testid={`icon-${name}`} data-tooltip={tooltipLabel} role="img">
        {name}
      </div>
    ),
  };
});

const mockUser = {
  firstName: "Test",
  lastName: "User",
  email: "test@example.com",
};

describe("SideNavigation", () => {
  beforeEach(() => {
    jest.clearAllMocks();

    (useGlobalContext as jest.Mock).mockReturnValue({
      userDetails: mockUser,
      isAuthenticated: true,
      isMobile: false,
    });

    (useProjectsContext as jest.Mock).mockReturnValue({
      allProjects: [],
      isPendingProjects: false,
    });
  });

  it("renders collapsed sidebar by default", () => {
    testRender(<SideNavigation />);
    expect(screen.getByTestId("icon-left_panel_open")).toBeInTheDocument();
    expect(screen.queryByText("Stax")).not.toBeInTheDocument();
    expect(screen.queryByText("New Autorater")).not.toBeInTheDocument();
  });

  it("expands sidebar when open button is clicked", () => {
    testRender(<SideNavigation />);
    const openButton = screen.getByTestId("icon-left_panel_open");
    expect(openButton).toBeInTheDocument();
    fireEvent.click(openButton);
    expect(screen.getByText("Stax")).toBeInTheDocument();
    expect(screen.getByText("New Autorater")).toBeInTheDocument();
    expect(screen.getByText("Recent Autoraters")).toBeInTheDocument();
    expect(screen.getByTestId("icon-left_panel_close")).toBeInTheDocument();
  });

  it("collapses sidebar when close button is clicked", () => {
    testRender(<SideNavigation />);
    const openButton = screen.getByTestId("icon-left_panel_open");
    fireEvent.click(openButton);
    const closeButton = screen.getByTestId("icon-left_panel_close");
    fireEvent.click(closeButton);
    expect(screen.getByTestId("icon-left_panel_open")).toBeInTheDocument();
    expect(screen.queryByText("Stax")).not.toBeInTheDocument();
  });

  it("displays autorater items when expanded", () => {
    testRender(<SideNavigation />);
    const openButton = screen.getByTestId("icon-left_panel_open");
    fireEvent.click(openButton);
    expect(screen.getByText("Career Compass AI")).toBeInTheDocument();
    expect(screen.getByText("Illuminate The Truth")).toBeInTheDocument();
    expect(screen.getByText("Perfect Syntax Proto")).toBeInTheDocument();
  });

  it("shows AI policy text when expanded", () => {
    testRender(<SideNavigation />);
    const openButton = screen.getByTestId("icon-left_panel_open");
    fireEvent.click(openButton);
    expect(screen.getByText("AI Policy Text")).toBeInTheDocument();
  });

  it("shows info icon with tooltip when collapsed", () => {
    testRender(<SideNavigation />);
    const infoIcon = screen.getByTestId("icon-info");
    expect(infoIcon).toBeInTheDocument();
    expect(infoIcon).toHaveAttribute("data-tooltip", "AI Policy Text");
  });

  it("renders navigation links in both states", () => {
    testRender(<SideNavigation />);
    expect(
      screen.getByTestId("sidenav-link-documentation"),
    ).toBeInTheDocument();
    expect(screen.getByTestId("sidenav-link-settings")).toBeInTheDocument();
    const openButton = screen.getByTestId("icon-left_panel_open");
    fireEvent.click(openButton);
    expect(screen.getByText("Documentation")).toBeInTheDocument();
    expect(screen.getByText("Settings")).toBeInTheDocument();
  });
});
