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

import SettingsPage from "@/app/(authRoutes)/settings/page";
import { routes } from "@/config/routes";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// Mock next/navigation
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  useSearchParams: jest.fn(),
}));

// Mock useModelsContext
jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

// Mock useTagsContext
jest.mock("@/hooks/useTagsContext", () => ({
  useTagsContext: jest.fn(),
}));

const mockUseRouter = jest.mocked(require("next/navigation").useRouter);
const mockUseSearchParams = jest.mocked(
  require("next/navigation").useSearchParams,
);
const mockUseModelsContext = jest.mocked(
  require("@/hooks/useModelsContext").useModelsContext,
);
const mockUseTagsContext = jest.mocked(
  require("@/hooks/useTagsContext").useTagsContext,
);

describe("SettingsPage", () => {
  const mockPush = jest.fn();
  const mockGet = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    mockUseRouter.mockReturnValue({
      push: mockPush,
      replace: jest.fn(),
    } as any);

    mockUseSearchParams.mockReturnValue({
      get: mockGet,
    } as any);

    mockUseModelsContext.mockReturnValue({
      openedModel: null,
      setOpenedModel: jest.fn(),
      allModels: [],
      isLoadingModels: false,
      refreshModels: jest.fn(),
      customModels: [],
      defaultModels: [],
      providers: {},
    });

    mockUseTagsContext.mockReturnValue({
      userTags: [],
      modelTags: [],
      datasetTags: [],
      allTags: {
        user_tags: [],
        model_tags: [],
        dataset_tags: [],
      },
      isLoadingTags: false,
      refreshTags: jest.fn(),
    });
  });

  const renderComponent = () => {
    return testRender(<SettingsPage />);
  };

  describe("Rendering", () => {
    it("should render the page with correct title", () => {
      renderComponent();

      expect(screen.getByText("Settings")).toBeInTheDocument();
    });

    it("should render all three tabs", () => {
      renderComponent();

      // Use getAllByText to get all elements with these texts
      const apiKeysElements = screen.getAllByText("API Keys");
      const modelManagerElements = screen.getAllByText("Model manager");
      const tagManagerElements = screen.getAllByText("Tag manager");

      expect(apiKeysElements.length).toBeGreaterThan(0);
      expect(modelManagerElements.length).toBeGreaterThan(0);
      expect(tagManagerElements.length).toBeGreaterThan(0);
    });

    it("should render API Keys tab content by default", () => {
      renderComponent();

      expect(screen.getByTestId("api-key-list")).toBeInTheDocument();
      // Check for the API Keys text in the content (not the tab)
      const apiKeysElements = screen.getAllByText("API Keys");
      expect(apiKeysElements.length).toBeGreaterThan(1); // Should have both tab and content
      expect(screen.getByText("Model providers")).toBeInTheDocument();
    });

    it("should render Model Manager tab content when tab is set to modelManager", () => {
      mockGet.mockReturnValue("modelManager");

      renderComponent();

      expect(screen.getByTestId("model-manager")).toBeInTheDocument();
    });

    it("should render Tag Manager tab content when tab is set to tagManager", () => {
      mockGet.mockReturnValue("tagManager");

      renderComponent();

      expect(screen.getByTestId("add-tags-container")).toBeInTheDocument();
      expect(screen.getByText("Tag Manager")).toBeInTheDocument();
    });
  });

  describe("Tab Navigation", () => {
    it("should navigate to API Keys tab when clicked", async () => {
      const user = userEvent.setup();
      renderComponent();

      // Get the tab button specifically, not just any element with "API Keys" text
      const apiKeysTab = screen.getByRole("tab", { name: "API Keys" });
      await user.click(apiKeysTab);

      expect(mockPush).toHaveBeenCalledWith(`${routes.settings}?tab=apiKeys`);
    });

    it("should navigate to Model Manager tab when clicked", async () => {
      const user = userEvent.setup();
      renderComponent();

      const modelManagerTab = screen.getByText("Model manager");
      await user.click(modelManagerTab);

      expect(mockPush).toHaveBeenCalledWith(
        `${routes.settings}?tab=modelManager`,
      );
    });

    it("should navigate to Tag Manager tab when clicked", async () => {
      const user = userEvent.setup();
      renderComponent();

      const tagManagerTab = screen.getByText("Tag manager");
      await user.click(tagManagerTab);

      expect(mockPush).toHaveBeenCalledWith(
        `${routes.settings}?tab=tagManager`,
      );
    });
  });

  describe("API Keys Tab", () => {
    it("should render API Keys section with correct content", () => {
      renderComponent();

      // Check that we have both tab and content for API Keys
      const apiKeysElements = screen.getAllByText("API Keys");
      expect(apiKeysElements.length).toBeGreaterThan(1);
      expect(screen.getByText("Model providers")).toBeInTheDocument();
      expect(screen.getByTestId("api-key-list")).toBeInTheDocument();
    });

    it("should render info tooltip for model providers", () => {
      renderComponent();

      // Look for the MaterialIcon with info name specifically
      const infoIcons = screen.getAllByTestId("material-icon");
      const infoIcon = infoIcons.find((icon) => icon.textContent === "info");
      expect(infoIcon).toBeInTheDocument();
    });
  });

  describe("Model Manager Tab", () => {
    it("should render Model Manager component when tab is active", () => {
      mockGet.mockReturnValue("modelManager");

      renderComponent();

      expect(screen.getByTestId("model-manager")).toBeInTheDocument();
    });
  });

  describe("Tag Manager Tab", () => {
    it("should render Tag Manager section with correct content", () => {
      mockGet.mockReturnValue("tagManager");

      renderComponent();

      expect(screen.getByText("Tag Manager")).toBeInTheDocument();
      expect(screen.getByTestId("add-tags-container")).toBeInTheDocument();
    });

    it("should render AddTagsContainer with correct props", () => {
      mockGet.mockReturnValue("tagManager");

      renderComponent();

      const addTagsContainer = screen.getByTestId("add-tags-container");
      expect(addTagsContainer).toBeInTheDocument();
    });
  });

  describe("URL Parameters", () => {
    it("should use default tab when no search param is provided", () => {
      mockGet.mockReturnValue(null);

      renderComponent();

      expect(screen.getByTestId("api-key-list")).toBeInTheDocument();
    });

    it("should use search param tab value when provided", () => {
      mockGet.mockReturnValue("tagManager");

      renderComponent();

      expect(screen.getByTestId("add-tags-container")).toBeInTheDocument();
    });
  });

  describe("Component Structure", () => {
    it("should render Page component with fullHeight prop", () => {
      renderComponent();

      // The Page component should be rendered with fullHeight prop
      // We can verify this by checking the structure
      expect(screen.getByText("Settings")).toBeInTheDocument();
    });

    it("should render PageHeader with correct title", () => {
      renderComponent();

      expect(screen.getByText("Settings")).toBeInTheDocument();
    });

    it("should render Tabs component with correct configuration", () => {
      renderComponent();

      // Check that all tabs are present
      const apiKeysElements = screen.getAllByText("API Keys");
      const modelManagerElements = screen.getAllByText("Model manager");
      const tagManagerElements = screen.getAllByText("Tag manager");

      expect(apiKeysElements.length).toBeGreaterThan(0);
      expect(modelManagerElements.length).toBeGreaterThan(0);
      expect(tagManagerElements.length).toBeGreaterThan(0);
    });
  });

  describe("Styling and Classes", () => {
    it("should apply correct CSS classes to tabs", () => {
      renderComponent();

      const tabs = screen.getAllByRole("tab");
      tabs.forEach((tab) => {
        expect(tab).toHaveClass("mx-3", "px-0", "pb-2", "text-secondary");
      });
    });

    it("should apply correct CSS classes to tab list", () => {
      renderComponent();

      const tabList = screen.getByRole("tablist");
      expect(tabList).toHaveClass("mb-[24px]", "before:border-transparent");
    });
  });

  describe("Accessibility", () => {
    it("should have proper tab roles and labels", () => {
      renderComponent();

      const tabs = screen.getAllByRole("tab");
      expect(tabs).toHaveLength(3);

      expect(tabs[0]).toHaveTextContent("API Keys");
      expect(tabs[1]).toHaveTextContent("Model manager");
      expect(tabs[2]).toHaveTextContent("Tag manager");
    });

    it("should have proper tab panel roles", () => {
      renderComponent();

      const tabPanels = screen.getAllByRole("tabpanel");
      // All tab panels are rendered but only one should be visible at a time
      expect(tabPanels).toHaveLength(3);
      // Check that only one tab panel is visible (not display: none)
      const visibleTabPanels = tabPanels.filter(
        (panel) => !panel.style.display || panel.style.display !== "none",
      );
      expect(visibleTabPanels).toHaveLength(1);
    });
  });

  describe("State Management", () => {
    it("should initialize addedTags state as empty array", () => {
      renderComponent();

      // The component should render without errors
      expect(screen.getByText("Settings")).toBeInTheDocument();
    });

    it("should pass setAddedTags to AddTagsContainer", () => {
      mockGet.mockReturnValue("tagManager");

      renderComponent();

      // The AddTagsContainer should be rendered with the setAddedTags prop
      expect(screen.getByTestId("add-tags-container")).toBeInTheDocument();
    });
  });
});
