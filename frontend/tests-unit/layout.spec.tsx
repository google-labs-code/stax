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

import RootLayout, { metadata } from "@/app/layout";
import { siteConfig } from "@/config/site";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import React from "react";

// Adjust path as needed

// Mock dependencies
jest.mock("@/config/site", () => ({
  siteConfig: {
    name: "Test App",
    description: "Test App Description",
  },
}));

jest.mock("@/hooks/useGlobalContext", () => ({
  GlobalContextProvider: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="global-context-provider">{children}</div>
  ),
}));

jest.mock("@/styles/main.scss", () => ({}));

jest.mock("@mantine/core", () => ({
  ColorSchemeScript: () => <script data-testid="color-scheme-script" />,
}));

jest.mock("next/font/local", () => ({
  __esModule: true,
  default: jest.fn(({ variable }) => ({
    variable: variable,
    className: `font-${variable}`,
  })),
}));

jest.mock("next/script", () => {
  return function MockScript({
    src,
    dangerouslySetInnerHTML,
    async: isAsync,
    type,
  }: any) {
    return (
      <script
        data-testid={`script-${src || "inline"}`}
        src={src}
        async={isAsync}
        type={type}
        dangerouslySetInnerHTML={dangerouslySetInnerHTML}
      />
    );
  };
});

describe("Root Layout Component", () => {
  const originalEnv = process.env;

  beforeEach(() => {
    jest.clearAllMocks();
    process.env = { ...originalEnv };
  });

  afterEach(() => {
    process.env = originalEnv;
  });

  describe("Metadata Export", () => {
    it("should export metadata with correct default title", () => {
      expect(metadata.title).toBeDefined();
    });
    it("should export metadata with correct description", () => {
      expect(metadata.description).toBe(siteConfig.description);
    });

    it("should have consistent metadata structure", () => {
      expect(metadata).toEqual({
        title: {
          default: "Test App",
          template: "%s - Test App",
        },
        description: "Test App Description",
      });
    });
  });

  describe("Component Rendering", () => {
    it("should render without crashing", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      expect(container).toBeInTheDocument();
    });

    it("should render html element with lang attribute", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const htmlElement = container.querySelector("html");
      expect(htmlElement).toHaveAttribute("lang", "en");
    });

    it("should render children inside GlobalContextProvider", async () => {
      render(
        await RootLayout({
          children: <div data-testid="test-content">Test Content</div>,
        }),
      );
      expect(screen.getByTestId("global-context-provider")).toBeInTheDocument();
      expect(screen.getByTestId("test-content")).toBeInTheDocument();
    });

    it("should render body element with correct classes", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const bodyElement = container.querySelector("body");
      expect(bodyElement).toHaveClass("min-h-screen", "bg-neutrals-950");
    });
  });

  describe("Head Section", () => {
    it("should render favicon links", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const faviconIco = container.querySelector(
        'link[rel="icon"][href="/favicon.ico"]',
      );
      expect(faviconIco).toBeInTheDocument();
    });

    it("should render PNG favicon", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const faviconPng = container.querySelector(
        'link[type="image/png"][sizes="96x96"]',
      );
      expect(faviconPng).toHaveAttribute("href", "/favicon-96x96.png");
    });

    it("should render SVG favicon", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const faviconSvg = container.querySelector('link[type="image/svg+xml"]');
      expect(faviconSvg).toHaveAttribute("href", "/favicon.svg");
    });

    it("should render apple touch icon", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const appleTouchIcon = container.querySelector(
        'link[rel="apple-touch-icon"]',
      );
      expect(appleTouchIcon).toHaveAttribute("href", "/apple-touch-icon.png");
    });

    it("should render charset meta tag", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const charsetMeta = container.querySelector('meta[charSet="UTF-8"]');
      expect(charsetMeta).toBeInTheDocument();
    });

    it("should render viewport meta tag", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const viewportMeta = container.querySelector('meta[name="viewport"]');
      expect(viewportMeta).toBeInTheDocument();
      expect(viewportMeta).toHaveAttribute(
        "content",
        "minimum-scale=1, initial-scale=1, width=device-width, user-scalable=no",
      );
    });

    it("should render ColorSchemeScript", async () => {
      render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      expect(screen.getByTestId("color-scheme-script")).toBeInTheDocument();
    });
  });

  describe("Font Variable Classes", () => {
    it("should include all font variables in html className", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const htmlElement = container.querySelector("html");
      const className = htmlElement?.getAttribute("class") || "";

      // Check that font variables are included
      expect(className).toMatch(/--font-google-sans/);
      expect(className).toMatch(/--font-material-symbols/);
      expect(className).toMatch(/--font-google-sans-text/);
      expect(className).toMatch(/--font-google-sans-mono/);
    });
  });

  describe("Google Analytics Scripts", () => {
    it("should not render Google Analytics when NEXT_PUBLIC_GA_TAG_ID is not set", async () => {
      delete process.env.NEXT_PUBLIC_GA_TAG_ID;
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const gaScript = container.querySelector(
        'script[src*="googletagmanager.com"]',
      );
      expect(gaScript).not.toBeInTheDocument();
    });

    it("should render Google Analytics when NEXT_PUBLIC_GA_TAG_ID is set", async () => {
      process.env.NEXT_PUBLIC_GA_TAG_ID = "GA-123456789";
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const gaScript = container.querySelector(
        'script[src*="googletagmanager.com"]',
      );
      expect(gaScript).toBeInTheDocument();
      expect(gaScript).toHaveAttribute("async");
    });

    it("should render Google Analytics config script with correct GAID", async () => {
      process.env.NEXT_PUBLIC_GA_TAG_ID = "GA-123456789";
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const scripts = container.querySelectorAll("script");
      const configScript = Array.from(scripts).find(
        (s) =>
          s.innerHTML &&
          s.innerHTML.includes("gtag") &&
          s.innerHTML.includes("GA-123456789"),
      );
      expect(configScript).toBeInTheDocument();
    });
  });

  describe("Feedback Scripts", () => {
    it("should render Google Feedback script when NEXT_PUBLIC_FEEDBACK_PRODUCT_ID is set", async () => {
      process.env.NEXT_PUBLIC_FEEDBACK_PRODUCT_ID = "test-product-id";
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const feedbackScript = container.querySelector(
        'script[src*="support.google.com/inapp/api.js"]',
      );
      expect(feedbackScript).toBeInTheDocument();
    });

    it("should not render Google Feedback script when NEXT_PUBLIC_FEEDBACK_PRODUCT_ID is not set", async () => {
      delete process.env.NEXT_PUBLIC_FEEDBACK_PRODUCT_ID;
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const feedbackScript = container.querySelector(
        'script[src*="support.google.com/inapp/api.js"]',
      );
      expect(feedbackScript).not.toBeInTheDocument();
    });
  });

  describe("HATS Scripts", () => {
    it("should render HATS script when NEXT_PUBLIC_HATS_API_KEY is set", async () => {
      process.env.NEXT_PUBLIC_HATS_API_KEY = "test-hats-key";
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const hatsScript = container.querySelector(
        'script[src*="gstatic.com/feedback/js"]',
      );
      expect(hatsScript).toBeInTheDocument();
    });

    it("should not render HATS script when NEXT_PUBLIC_HATS_API_KEY is not set", async () => {
      delete process.env.NEXT_PUBLIC_HATS_API_KEY;
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const hatsScript = container.querySelector(
        'script[src*="gstatic.com/feedback/js"]',
      );
      expect(hatsScript).not.toBeInTheDocument();
    });
  });

  describe("Trusted Types Policy Script", () => {
    it("should include trusted types policy script", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const scripts = container.querySelectorAll("script");
      const trustedTypesScript = Array.from(scripts).find(
        (s) => s.innerHTML && s.innerHTML.includes("trustedTypes"),
      );
      expect(trustedTypesScript).toBeInTheDocument();
    });
  });

  describe("Layout Structure", () => {
    it("should have correct main container structure", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const mainDiv = container.querySelector(
        ".flex.h-screen.w-screen.flex-row",
      );
      expect(mainDiv).toBeInTheDocument();
      expect(mainDiv).toHaveClass("items-center", "justify-center");
    });

    it("should wrap children with GlobalContextProvider", async () => {
      render(
        await RootLayout({
          children: <span data-testid="child-element">Child</span>,
        }),
      );
      const provider = screen.getByTestId("global-context-provider");
      const child = screen.getByTestId("child-element");
      expect(provider).toContainElement(child);
    });
  });

  describe("Environment Variable Combinations", () => {
    it("should handle all environment variables set", async () => {
      process.env.NEXT_PUBLIC_GA_TAG_ID = "GA-123";
      process.env.NEXT_PUBLIC_FEEDBACK_PRODUCT_ID = "feedback-123";
      process.env.NEXT_PUBLIC_HATS_API_KEY = "hats-key";

      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );

      expect(
        container.querySelector('script[src*="googletagmanager"]'),
      ).toBeInTheDocument();
      expect(
        container.querySelector('script[src*="support.google.com"]'),
      ).toBeInTheDocument();
      expect(
        container.querySelector('script[src*="gstatic.com"]'),
      ).toBeInTheDocument();
    });

    it("should handle no optional environment variables set", async () => {
      delete process.env.NEXT_PUBLIC_GA_TAG_ID;
      delete process.env.NEXT_PUBLIC_FEEDBACK_PRODUCT_ID;
      delete process.env.NEXT_PUBLIC_HATS_API_KEY;

      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );

      // Should still render without errors
      expect(container.querySelector("html")).toBeInTheDocument();
      expect(screen.getByTestId("global-context-provider")).toBeInTheDocument();
    });
  });

  describe("Accessibility", () => {
    it("should have proper HTML lang attribute for accessibility", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const htmlElement = container.querySelector("html");
      expect(htmlElement?.getAttribute("lang")).toBe("en");
    });

    it("should have viewport meta for mobile accessibility", async () => {
      const { container } = render(
        await RootLayout({
          children: <div>Test Content</div>,
        }),
      );
      const viewportMeta = container.querySelector('meta[name="viewport"]');
      expect(viewportMeta?.getAttribute("content")).toContain(
        "initial-scale=1",
      );
    });
  });

  describe("Type Safety", () => {
    it("should accept ReactNode children", async () => {
      const children: React.ReactNode = (
        <>
          <div>Child 1</div>
          <div>Child 2</div>
        </>
      );

      render(await RootLayout({ children }));
      expect(screen.getByText("Child 1")).toBeInTheDocument();
      expect(screen.getByText("Child 2")).toBeInTheDocument();
    });

    it("should handle complex children structure", async () => {
      const children = (
        <div>
          <header>Header</header>
          <main>Main Content</main>
          <footer>Footer</footer>
        </div>
      );

      render(await RootLayout({ children }));
      expect(screen.getByText("Header")).toBeInTheDocument();
      expect(screen.getByText("Main Content")).toBeInTheDocument();
      expect(screen.getByText("Footer")).toBeInTheDocument();
    });
  });
});
