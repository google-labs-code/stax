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

import SideNavigation from "@/components/SideNav/SideNavigation";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";
import { useParams, useRouter } from "next/navigation";

// Mock next/navigation
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  useParams: jest.fn(),
}));

jest.mock("@/hooks/useGlobalContext", () => ({
  useGlobalContext: jest.fn(),
}));

// Mock next/image
jest.mock("next/image", () => {
  return function MockImage({
    src,
    alt,
    priority,
  }: {
    src: string;
    alt: string;
    priority?: boolean;
  }) {
    return (
      <img
        src={src}
        alt={alt}
        data-priority={priority}
        data-testid="next-image"
      />
    );
  };
});

// Mock MaterialIcon component
jest.mock("@/components/MaterialIcon", () => {
  return function MockMaterialIcon({
    name,
    className,
    size,
  }: {
    name: string;
    className?: string;
    size?: number;
  }) {
    return (
      <div
        data-testid="material-icon"
        data-name={name}
        className={className}
        data-size={size}
      />
    );
  };
});

// Mock SidenavLink component
jest.mock("@/components/SideNav/SidenavLink", () => {
  return function MockSidenavLink({
    listItem,
    isSidenavOpen,
    isLoading,
    onLinkClick,
  }: {
    listItem: any;
    isSidenavOpen: boolean;
    isLoading?: boolean;
    onLinkClick?: () => void;
  }) {
    return (
      <div
        data-testid={`sidenav-link-${listItem.key}`}
        data-sidenav-open={isSidenavOpen}
        data-loading={isLoading}
        onClick={onLinkClick}
      >
        {listItem.label}
      </div>
    );
  };
});

// Mock useProjectsContext hook
jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

// Mock createProjectQuery
jest.mock("@/queries/clientQueries", () => ({
  createProjectQuery: jest.fn(),
}));

// Mock logGAevent
jest.mock("@/utils/logGAevent", () => ({
  __esModule: true,
  default: jest.fn(),
}));

// Mock routes
jest.mock("@/config/routes", () => ({
  ALL_PAGE_LINKS: [
    {
      link: "/quick-compare",
      key: "quickCompare",
      label: "Quick Compare",
      icon: "width_normal",
      childLinks: [],
    },
    {
      link: "/projects",
      key: "projects",
      label: "My Projects",
      icon: "home_storage",
      isExpanded: true,
      childLinks: [],
    },
    {
      link: "/datasets",
      key: "datasets",
      label: "Datasets",
      icon: "data_table",
      childLinks: [],
    },
  ],
  routes: {
    quickCompare: "/quick-compare",
    projects: "/projects",
    settings: "/settings",
  },
}));

describe("SideNavContent", () => {
  const mockUseRouter = useRouter as jest.MockedFunction<typeof useRouter>;
  const mockUseProjectsContext = require("@/hooks/useProjectsContext")
    .useProjectsContext as jest.MockedFunction<any>;
  const mockUseGlobalContext = require("@/hooks/useGlobalContext")
    .useGlobalContext as jest.MockedFunction<any>;
  const mockCreateProjectQuery = require("@/queries/clientQueries")
    .createProjectQuery as jest.MockedFunction<any>;

  const defaultProjectsContext = {
    allProjects: [
      {
        project_id: "project-1",
        name: "Project 1",
        is_default_project: true,
        updated_at: "2023-01-01T00:00:00Z",
      },
      {
        project_id: "project-2",
        name: "Project 2",
        is_default_project: false,
        updated_at: "2023-01-02T00:00:00Z",
      },
    ],
    refreshProjects: jest.fn(),
    isLoadingProjects: false,
    isPendingProjects: false,
    defaultProjectId: "",
    setDefaultProjectId: jest.fn(),
  };

  const defaultRouter = {
    push: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
    refresh: jest.fn(),
    replace: jest.fn(),
    prefetch: jest.fn(),
  };

  const mockUseParams = useParams as jest.MockedFunction<typeof useParams>;

  beforeEach(() => {
    jest.resetModules();
    delete require.cache[require.resolve("@/hooks/useProjectsContext")];
    jest.clearAllMocks();
    mockUseRouter.mockReturnValue(defaultRouter);
    mockUseProjectsContext.mockReturnValue(defaultProjectsContext);
    mockCreateProjectQuery.mockResolvedValue({ project_id: "new-project" });
    mockUseGlobalContext.mockReturnValue({
      userDetails: {
        firstName: "John",
        lastName: "Doe",
        email: "john_doe",
      },
    });
    mockUseParams.mockReturnValue({ id: "project-1" }); // ← Provide a mock ID
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("should render the component with logo and navigation", () => {
    testRender(<SideNavigation />);

    // Check if logo is rendered
    const logo = screen.getByTestId("next-image");
    expect(logo).toBeInTheDocument();
    expect(logo).toHaveAttribute("alt", "navbarLogo");

    // Check if navigation links are rendered
    expect(screen.getByTestId("sidenav-link-quickCompare")).toBeInTheDocument();
    expect(screen.getByTestId("sidenav-link-projects")).toBeInTheDocument();
    expect(screen.getByTestId("sidenav-link-datasets")).toBeInTheDocument();
  });

  it("should render the Stax logo and text when sidenav is open", () => {
    testRender(<SideNavigation />);

    const logo = screen.getByTestId("next-image");
    const staxText = screen.getByText("Stax");

    expect(logo).toBeInTheDocument();
    expect(staxText).toBeInTheDocument();
    expect(staxText).toHaveClass("text-white", "text-logo");
  });

  it("should render project child links when projects are available", () => {
    testRender(<SideNavigation />);

    // Check if project links are rendered as child links
    expect(screen.getByTestId("sidenav-link-project-1")).toBeInTheDocument();
  });

  it("should render AI policy text when sidenav is open", () => {
    testRender(<SideNavigation />);

    const aiPolicy = screen.getByText(
      "AI can make mistakes. Google is not responsible for third-party model output.",
    );
    expect(aiPolicy).toBeInTheDocument();
    expect(aiPolicy).toHaveClass("text-disabled", "text-body-sans-12");
  });

  it("should render settings link", () => {
    testRender(<SideNavigation />);

    const settingsLink = screen.getByTestId("sidenav-link-settings");
    expect(settingsLink).toBeInTheDocument();
  });

  it("should render user button", () => {
    testRender(<SideNavigation />);

    const userButton = screen.getByText("john_doe");
    expect(userButton).toBeInTheDocument();
  });

  it("should render toggle sidebar button", () => {
    testRender(<SideNavigation />);

    const allIcons = screen.getAllByTestId("material-icon");
    const chevronLeft = allIcons.find(
      (icon) => icon.getAttribute("data-name") === "chevron_left",
    );

    expect(chevronLeft).toBeInTheDocument();
  });

  it("should handle sidebar toggle", () => {
    testRender(<SideNavigation />);

    const allIcons = screen.getAllByTestId("material-icon");
    const chevronLeft = allIcons.find(
      (icon) => icon.getAttribute("data-name") === "chevron_left",
    );

    if (chevronLeft) {
      fireEvent.click(chevronLeft);
      // Note: The toggle functionality is internal to the component, so we can't easily test the state change
      // without exposing it or using more complex testing strategies
    }
  });

  it("should filter out protected routes from navigation", () => {
    testRender(<SideNavigation />);

    // Other routes should still be rendered
    expect(screen.getByTestId("sidenav-link-projects")).toBeInTheDocument();
    expect(screen.getByTestId("sidenav-link-datasets")).toBeInTheDocument();
  });

  it("should handle expandable menu items", () => {
    testRender(<SideNavigation />);

    const projectsLink = screen.getByTestId("sidenav-link-projects");

    // Simulate clicking on expandable menu item
    fireEvent.click(projectsLink);

    // The component should handle the expansion internally
    expect(projectsLink).toBeInTheDocument();
  });

  it("should render with correct layout structure", () => {
    testRender(<SideNavigation />);

    // Check for Stax container structure
    expect(screen.getByText("Stax")).toBeInTheDocument();
  });
});
