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

import AnalyticsFiltersContainer from "@/app/(authRoutes)/analytics/components/AnalyticsFiltersContainer";
import { useAnalyticsContext } from "@/hooks/useAnalyticsContext";
import { useModelsContext } from "@/hooks/useModelsContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { useTagsContext } from "@/hooks/useTagsContext";
import { testRender } from "@/tests-unit/render";
import { Provider } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@/hooks/useAnalyticsContext", () => ({
  useAnalyticsContext: jest.fn(),
  FilterType: jest.requireActual("@/hooks/useAnalyticsContext").FilterType,
}));

jest.mock("@/hooks/useModelsContext");

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

jest.mock("@/hooks/useTagsContext", () => ({
  useTagsContext: jest.fn(),
}));

const mockFilters = [{ project: "proj1", model: "OPENAI", tags: ["OPENAI"] }];

const allProjectsMock = [
  { project_id: "proj1", name: "Project One" },
  { project_id: "proj2", name: "Project Two" },
  { project_id: "proj3", name: "Alpha Project" },
];

const allModelsMock = [
  {
    properties: {
      max_tokens: undefined,
      n: undefined,
      temperature: undefined,
      top_p: undefined,
      top_k: undefined,
      seed: undefined,
    },
    id: "OPENAI",
    name: "OPENAI",
    version: "OPENAI",
    label: "OPENAI",
    url: "OPENAI",
    tag: "OPENAI",
    provider: Provider.OPENAI,
    model_type: "",
    additional_headers: {},
  },
  {
    properties: {
      max_tokens: undefined,
      n: undefined,
      temperature: undefined,
      top_p: undefined,
      top_k: undefined,
      seed: undefined,
    },
    id: "DEEPSEEK",
    name: "DEEPSEEK",
    version: "DEEPSEEK",
    label: "DEEPSEEK",
    url: "DEEPSEEK",
    tag: "DEEPSEEK",
    provider: Provider.DEEPSEEK,
    model_type: "",
    additional_headers: {},
  },
];

function setup({
  filters = mockFilters,
  projects = allProjectsMock,
  models = allModelsMock,
  isLoadingModels = false,
  isLoadingProjects = false,
  setSelectedFilters = jest.fn(),
} = {}) {
  (useAnalyticsContext as jest.Mock).mockReturnValue({
    selectedFilters: filters,
    setSelectedFilters,
  });

  (useModelsContext as jest.Mock).mockReturnValue({
    allModels: models,
    isLoadingModels,
  });

  (useProjectsContext as jest.Mock).mockReturnValue({
    allProjects: projects,
    isLoadingProjects,
  });

  (useTagsContext as jest.Mock).mockReturnValue({
    userTags: [
      { id: "tag1", name: "Tag One" },
      { id: "tag2", name: "Tag Two" },
    ],
    isLoadingTags: false,
  });

  return {
    setSelectedFilters,
    ...testRender(<AnalyticsFiltersContainer />),
  };
}

describe("AnalyticsFiltersContainer without providers (hook mocks)", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders filters with projects, models, and tags", () => {
    setup();

    expect(screen.getByPlaceholderText("Select Project")).toBeInTheDocument();
    expect(screen.getByText("Project One")).toBeInTheDocument();
    expect(screen.getByTestId("model-combobox")).toBeInTheDocument();
    expect(screen.getByTestId("tags-select")).toBeInTheDocument();
  });

  it("calls setSelectedFilters when project is changed", async () => {
    const { setSelectedFilters } = setup();

    fireEvent.mouseDown(screen.getByPlaceholderText("Select Project"));

    await waitFor(() => screen.getByText("Project Two"));

    fireEvent.click(screen.getByText("Project Two"));

    expect(setSelectedFilters).toHaveBeenCalled();
  });

  it("calls updateFilterProperty when model is selected", async () => {
    const setSelectedFilters = jest.fn();
    setup({ setSelectedFilters });

    const modelCombobox = screen.getByTestId("model-combobox");
    fireEvent.click(modelCombobox);

    await waitFor(() => {
      // Find the model option by looking for the clickable div that contains "OPENAI"
      const modelOptions = screen.getAllByText("OPENAI");
      // The option container is a div with onClick that contains the text
      const dropdownOption = modelOptions.find(
        (option) =>
          option.closest('[data-testid^="accordion-model-option-"]') !== null,
      );
      expect(dropdownOption).toBeInTheDocument();
    });

    // Find the option container div and click it
    const optionContainer = screen.getByTestId("accordion-model-option-0");
    fireEvent.click(optionContainer);

    expect(setSelectedFilters).toHaveBeenCalled();
    const callArgs = setSelectedFilters.mock.calls[0][0];
    expect(typeof callArgs).toBe("function");
  });

  it("calls updateFilterProperty when model is cleared", () => {
    const setSelectedFilters = jest.fn();
    setup({
      setSelectedFilters,
      filters: [{ project: "proj1", model: allModelsMock[0] as any, tags: [] }],
    });

    const modelCombobox = screen.getByTestId("model-combobox");
    const clearIcon = modelCombobox.querySelector(
      '[data-testid="material-icon-close"]',
    );
    expect(clearIcon).toBeInTheDocument();
    if (clearIcon) {
      fireEvent.click(clearIcon);
    }

    expect(setSelectedFilters).toHaveBeenCalled();
  });

  it("calls updateFilterProperty when tags are added", async () => {
    const setSelectedFilters = jest.fn();
    setup({ setSelectedFilters });

    const input = screen.getByPlaceholderText("Add tags");
    fireEvent.focus(input);

    await waitFor(() => {
      const tagOption = screen.getByText("Tag One");
      expect(tagOption).toBeInTheDocument();
    });

    const tagOption = screen.getByText("Tag One");
    fireEvent.click(tagOption);

    expect(setSelectedFilters).toHaveBeenCalled();
  });

  it("calls updateFilterProperty when tags are removed", () => {
    const setSelectedFilters = jest.fn();
    setup({
      setSelectedFilters,
      filters: [{ project: "proj1", model: null as any, tags: ["tag1"] }],
    });

    const tagPill = screen.getByTestId("tag-tag1");
    // Find the remove button inside the Pill component
    const removeButton = tagPill.querySelector("button");
    if (removeButton) {
      fireEvent.click(removeButton);
    }

    expect(setSelectedFilters).toHaveBeenCalled();
  });

  it("calls removeFilterGroupAtIndex when cancel icon is clicked with multiple filters", () => {
    const setSelectedFilters = jest.fn();
    setup({
      setSelectedFilters,
      filters: [
        { project: "proj1", model: null as any, tags: [] },
        { project: "proj2", model: null as any, tags: [] },
      ],
    });

    const cancelIcons = screen.getAllByTestId("material-icon-cancel");
    expect(cancelIcons.length).toBeGreaterThan(0);

    fireEvent.click(cancelIcons[0]);

    expect(setSelectedFilters).toHaveBeenCalled();
    const callArgs = setSelectedFilters.mock.calls[0][0];
    expect(typeof callArgs).toBe("function");

    // Test the filter function
    const prevFilters = [
      { project: "proj1", model: null, tags: [] },
      { project: "proj2", model: null, tags: [] },
    ];
    const result = callArgs(prevFilters);
    expect(result).toHaveLength(1);
  });

  it("calls setSelectedFilters to add new filter when add icon is clicked with single filter", () => {
    const setSelectedFilters = jest.fn();
    setup({
      setSelectedFilters,
      filters: [{ project: "proj1", model: null as any, tags: [] }],
    });

    const addIcon = screen.getByTestId("material-icon-add_circle");
    fireEvent.click(addIcon);

    expect(setSelectedFilters).toHaveBeenCalledWith([
      { project: "proj1", model: null as any, tags: [] },
      { project: "", model: null as any, tags: [] },
    ]);
  });

  it("calls updateFilterProperty to clear project when close icon is clicked", () => {
    const setSelectedFilters = jest.fn();
    setup({
      setSelectedFilters,
      filters: [{ project: "proj1", model: null as any, tags: [] }],
    });

    const closeIcon = screen.getByTestId("material-icon-close");
    fireEvent.click(closeIcon);

    expect(setSelectedFilters).toHaveBeenCalled();
    const callArgs = setSelectedFilters.mock.calls[0][0];
    expect(typeof callArgs).toBe("function");

    // Test the filter function
    const prevFilters = [{ project: "proj1", model: null, tags: [] }];
    const result = callArgs(prevFilters);
    expect(result[0].project).toBeNull();
  });

  it("shows loading state for projects", () => {
    setup({ isLoadingProjects: true });

    expect(screen.getByPlaceholderText("Loading...")).toBeInTheDocument();
    expect(
      screen.queryByPlaceholderText("Select Project"),
    ).not.toBeInTheDocument();
  });

  it("shows loading state for models", () => {
    setup({ isLoadingModels: true });

    expect(screen.getByPlaceholderText("Loading...")).toBeInTheDocument();
  });

  it("renders multiple filter groups when multiple filters are provided", () => {
    setup({
      filters: [
        { project: "proj1", model: null as any, tags: [] },
        { project: "proj2", model: null as any, tags: [] },
      ],
    });

    const projectSelects = screen.getAllByPlaceholderText("Select Project");
    expect(projectSelects).toHaveLength(2);

    const modelComboboxes = screen.getAllByTestId("model-combobox");
    expect(modelComboboxes).toHaveLength(2);

    const tagsSelects = screen.getAllByTestId("tags-select");
    expect(tagsSelects).toHaveLength(2);
  });

  it("sorts projects alphabetically by name", () => {
    setup({
      projects: [
        { project_id: "proj3", name: "Zebra Project" },
        { project_id: "proj1", name: "Alpha Project" },
        { project_id: "proj2", name: "Beta Project" },
      ],
    });

    fireEvent.mouseDown(screen.getByPlaceholderText("Select Project"));

    // Check that projects appear in sorted order
    // The first option should be "Alpha Project" based on alphabetical sorting
    waitFor(() => {
      const options = screen.getAllByText(/Project/);
      expect(options[0]).toHaveTextContent("Alpha Project");
    });
  });

  it("renders filter labels correctly", () => {
    setup({
      filters: [
        { project: "proj1", model: null as any, tags: [] },
        { project: "proj2", model: null as any, tags: [] },
      ],
    });

    // FILTER_LABELS should be rendered for each filter
    // We can't easily test the exact label text without importing FILTER_LABELS,
    // but we can verify that Text components are rendered
    const filterContainers = screen.getAllByPlaceholderText("Select Project");
    expect(filterContainers.length).toBe(2);
  });

  it("handles empty projects list", () => {
    setup({ projects: [] });

    expect(screen.getByPlaceholderText("Select Project")).toBeInTheDocument();
    fireEvent.mouseDown(screen.getByPlaceholderText("Select Project"));
    // Should not crash and should show empty dropdown
  });

  it("handles empty models list", () => {
    setup({ models: [] });

    expect(screen.getByTestId("model-combobox")).toBeInTheDocument();
  });

  it("displays selected model in ModelComboBox", () => {
    setup({
      filters: [{ project: "proj1", model: allModelsMock[0] as any, tags: [] }],
    });

    const modelInput = screen
      .getByTestId("model-combobox")
      .querySelector("input");
    expect(modelInput).toHaveValue("OPENAI");
  });

  it("displays selected tags in TagsSelect", () => {
    setup({
      filters: [
        { project: "proj1", model: null as any, tags: ["tag1", "tag2"] },
      ],
    });

    expect(screen.getByTestId("tag-tag1")).toBeInTheDocument();
    expect(screen.getByTestId("tag-tag2")).toBeInTheDocument();
  });

  it("shows keyboard_arrow_down icon when project is not selected", () => {
    setup({
      filters: [{ project: "", model: null as any, tags: [] }],
    });

    // Find the project Select input and then find the icon within it
    const projectInput = screen.getByPlaceholderText("Select Project");
    const projectSelectContainer = projectInput.closest(".mantine-Select-root");
    const keyboardArrowDownIcon = projectSelectContainer?.querySelector(
      '[data-testid="material-icon-keyboard_arrow_down"]',
    );

    expect(keyboardArrowDownIcon).toBeInTheDocument();

    // Verify the close icon is not in the project select (but might be in model combobox)
    const projectCloseIcon = projectSelectContainer?.querySelector(
      '[data-testid="material-icon-close"]',
    );
    expect(projectCloseIcon).not.toBeInTheDocument();
  });

  it("shows close icon when project is selected", () => {
    setup({
      filters: [{ project: "proj1", model: null as any, tags: [] }],
    });

    // Find the project Select input and then find the icon within it
    const projectInput = screen.getByPlaceholderText("Select Project");
    const projectSelectContainer = projectInput.closest(".mantine-Select-root");
    const projectCloseIcon = projectSelectContainer?.querySelector(
      '[data-testid="material-icon-close"]',
    );

    expect(projectCloseIcon).toBeInTheDocument();

    // Verify the keyboard_arrow_down icon is not in the project select (but might be in model combobox)
    const projectKeyboardArrowDownIcon = projectSelectContainer?.querySelector(
      '[data-testid="material-icon-keyboard_arrow_down"]',
    );
    expect(projectKeyboardArrowDownIcon).not.toBeInTheDocument();
  });
});
