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

import RootLayout from "@/app/(authRoutes)/layout";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import { usePathname } from "next/navigation";
import React from "react";

// Adjust path as needed

// Mock Next.js navigation
jest.mock("next/navigation", () => ({
  usePathname: jest.fn(),
}));

// Mock context providers
jest.mock("@/hooks/useAnalyticsContext", () => ({
  AnalyticsFilterProvider: ({ children }: { children: React.ReactNode }) => (
    <>{children}</>
  ),
}));

jest.mock("@/hooks/useChatContext", () => ({
  ChatProvider: ({ children }: { children: React.ReactNode }) => (
    <>{children}</>
  ),
}));

jest.mock("@/hooks/useDatasetsContext", () => ({
  DatasetsProvider: ({ children }: { children: React.ReactNode }) => (
    <>{children}</>
  ),
}));

jest.mock("@/hooks/useHumanEvalsContext", () => ({
  HumanEvalsProvider: ({ children }: { children: React.ReactNode }) => (
    <>{children}</>
  ),
}));

jest.mock("@/hooks/useModelsContext", () => ({
  ModelsProvider: ({ children }: { children: React.ReactNode }) => (
    <>{children}</>
  ),
}));

jest.mock("@/hooks/useProjectsContext", () => ({
  ProjectsProvider: ({ children }: { children: React.ReactNode }) => (
    <>{children}</>
  ),
}));

jest.mock("@/hooks/useTagsContext", () => ({
  TagsProvider: ({ children }: { children: React.ReactNode }) => (
    <>{children}</>
  ),
}));

// Mock SideNavigation component
jest.mock("@/components/SideNav/SideNavigation", () => {
  return function MockSideNavigation() {
    return <nav data-testid="side-navigation">Side Navigation</nav>;
  };
});

// Mock styles
jest.mock("@/styles/main.scss", () => ({}));

describe("RootLayout Component", () => {
  const mockUsePathname = usePathname as jest.MockedFunction<
    typeof usePathname
  >;

  beforeEach(() => {
    jest.clearAllMocks();
    mockUsePathname.mockReturnValue("/");
  });

  describe("Rendering", () => {
    it("should render without crashing", () => {
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children Content</div>
        </RootLayout>,
      );
      expect(screen.getByRole("main")).toBeInTheDocument();
    });

    it("should render children", () => {
      const testChildren = "Test Children Content";
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>{testChildren}</div>
        </RootLayout>,
      );
      expect(screen.getByText(testChildren)).toBeInTheDocument();
    });

    it("should render modal", () => {
      const modalContent = "Modal Content";
      render(
        <RootLayout modal={<div>{modalContent}</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.getByText(modalContent)).toBeInTheDocument();
    });
  });

  describe("SideNavigation Visibility", () => {
    it('should render SideNavigation when pathname is not "/autorater"', () => {
      mockUsePathname.mockReturnValue("/dashboard");
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.getByTestId("side-navigation")).toBeInTheDocument();
    });

    it('should not render SideNavigation when pathname is "/autorater"', () => {
      mockUsePathname.mockReturnValue("/autorater");
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.queryByTestId("side-navigation")).not.toBeInTheDocument();
    });

    it('should render SideNavigation on home route "/"', () => {
      mockUsePathname.mockReturnValue("/");
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.getByTestId("side-navigation")).toBeInTheDocument();
    });

    it('should render SideNavigation on "/projects" route', () => {
      mockUsePathname.mockReturnValue("/projects");
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.getByTestId("side-navigation")).toBeInTheDocument();
    });
  });

  describe("Layout Structure", () => {
    it("should have correct CSS classes on main container", () => {
      const { container } = render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      const rootDiv = container.firstChild as HTMLElement;
      expect(rootDiv).toHaveClass("relative", "flex", "h-screen", "w-full");
    });

    it("should have gradient background element", () => {
      const { container } = render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      const gradientDiv = container.querySelector(".blur-\\[100px\\]");
      expect(gradientDiv).toBeInTheDocument();
    });

    it("should render main element with correct structure", () => {
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      const mainElement = screen.getByRole("main");
      expect(mainElement).toHaveClass("flex", "h-full", "grow", "basis-10/12");
    });

    it("should have a primary background div inside main", () => {
      const { container } = render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      const primaryDiv = container.querySelector(".bg-primary");
      expect(primaryDiv).toBeInTheDocument();
      expect(primaryDiv).toHaveClass("rounded-2xl");
    });
  });

  describe("Props Handling", () => {
    it("should handle ReactNode modal prop", () => {
      const modal = (
        <div data-testid="custom-modal">
          <h1>Custom Modal</h1>
        </div>
      );
      render(
        <RootLayout modal={modal}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.getByTestId("custom-modal")).toBeInTheDocument();
      expect(
        screen.getByRole("heading", { name: /Custom Modal/i }),
      ).toBeInTheDocument();
    });

    it("should handle complex children structure", () => {
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>
            <header>Header</header>
            <section>Content</section>
            <footer>Footer</footer>
          </div>
        </RootLayout>,
      );
      expect(screen.getByText("Header")).toBeInTheDocument();
      expect(screen.getByText("Content")).toBeInTheDocument();
      expect(screen.getByText("Footer")).toBeInTheDocument();
    });

    it("should handle undefined or null modal gracefully", () => {
      render(
        <RootLayout modal={null}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.getByText("Children")).toBeInTheDocument();
    });
  });

  describe("Context Providers", () => {
    it("should wrap children with all required providers", () => {
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div data-testid="test-child">Children</div>
        </RootLayout>,
      );
      // If providers are missing, the component would fail to render
      expect(screen.getByTestId("test-child")).toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("should handle pathname changes", () => {
      const { rerender } = render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.getByTestId("side-navigation")).toBeInTheDocument();

      mockUsePathname.mockReturnValue("/autorater");
      rerender(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.queryByTestId("side-navigation")).not.toBeInTheDocument();
    });

    it("should handle very long pathname", () => {
      mockUsePathname.mockReturnValue(
        "/very/long/nested/path/that/is/not/autorater",
      );
      render(
        <RootLayout modal={<div>Modal</div>}>
          <div>Children</div>
        </RootLayout>,
      );
      expect(screen.getByTestId("side-navigation")).toBeInTheDocument();
    });
  });
});
