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

import WorkbookActionMenu from "@/app/(authRoutes)/projects/[id]/components/WorkbookActionMenu";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { REPORT_LEGAL_ISSUE } from "@/config/constants";
import { inferenceChatCompletionQuery } from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { InferenceChatCompletionPromptRole, ProjectType } from "@/types";
import {
  QueryClient,
  QueryClientProvider,
  useMutation,
} from "@tanstack/react-query";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@mantine/notifications", () => ({
  notifications: { show: jest.fn() },
}));

const createQueryClient = () =>
  new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

jest.mock("@/queries/clientQueries", () => {
  const actual = jest.requireActual("@/queries/clientQueries");

  return {
    ...actual,
    inferenceChatCompletionQuery: jest.fn(actual.inferenceChatCompletionQuery),
  };
});

jest.mock("@tanstack/react-query", () => {
  const actual = jest.requireActual("@tanstack/react-query");

  return {
    ...actual,
    useMutation: jest.fn((...args) => actual.useMutation(...args)),
  };
});

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: () => ({
    defaultProjectId: "mock-project-id",
  }),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

(useProjectContext as jest.Mock).mockReturnValue({
  isSideBySide: false,
  projectType: "DEFAULT",
});

describe("WorkbookActionMenu", () => {
  const defaultProps = {
    onRowDelete: { mutate: jest.fn() },
    selectedRow: {
      chat_id: "chat1",
      model_id: "model1",
      model_provider: "OTHER",
      input: "test input",
      expected_output: "expected",
      variables: {
        test: { value: "Test" },
      },
      chat_turn_b: {
        model_id: "model2",
      },
    },
    projectId: "proj1",
    setRowSelection: jest.fn(),
    onAddTags: jest.fn(),
    onRemoveTags: jest.fn(),
    onClearResults: jest.fn(),
    onDuplicateSuccess: jest.fn(),
    projectType: "DEFAULT",
  };

  beforeEach(() => {
    jest.restoreAllMocks();
  });

  function renderComponent(props = {}) {
    const queryClient = createQueryClient();

    return testRender(
      <QueryClientProvider client={queryClient}>
        <WorkbookActionMenu {...defaultProps} {...props} />
      </QueryClientProvider>,
    );
  }

  it("renders action menu and items", () => {
    renderComponent();
    fireEvent.click(screen.getByRole("button"));

    expect(screen.getByText("Delete")).toBeInTheDocument();
    expect(screen.getByText("Add tags")).toBeInTheDocument();
    expect(screen.getByText("Remove tags")).toBeInTheDocument();
    expect(screen.getByText("Duplicate")).toBeInTheDocument();
    expect(screen.getByText("Clear results")).toBeInTheDocument();
  });

  it("does not render Delete if isSubRow is true", () => {
    renderComponent({
      selectedRow: { ...defaultProps.selectedRow, isSubRow: true },
    });
    fireEvent.click(screen.getByRole("button"));

    expect(screen.queryByText("Delete")).toBeNull();
  });

  it("shows Report legal issue if provider is GOOGLE", () => {
    renderComponent({
      selectedRow: { ...defaultProps.selectedRow, model_provider: "GOOGLE" },
    });
    fireEvent.click(screen.getByRole("button"));

    expect(screen.getByText("Report legal issue")).toBeInTheDocument();
  });

  it("should open the official support link for legal issues if provider is GOOGLE", () => {
    (window.open as jest.Mock) = jest.fn();

    renderComponent({
      selectedRow: { ...defaultProps.selectedRow, model_provider: "GOOGLE" },
    });

    fireEvent.click(screen.getByRole("button"));

    fireEvent.click(screen.getByText("Report legal issue"));

    expect(window.open).toHaveBeenCalledWith(
      REPORT_LEGAL_ISSUE,
      "_blank",
      "noopener,noreferrer",
    );
  });

  it("calls onAddTags with correct chat_id", () => {
    renderComponent();
    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(screen.getByText("Add tags"));

    expect(defaultProps.onAddTags).toHaveBeenCalledWith("chat1");
  });

  it("calls onRemoveTags with correct chat_id", () => {
    renderComponent();
    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(screen.getByText("Remove tags"));

    expect(defaultProps.onRemoveTags).toHaveBeenCalledWith("chat1");
  });

  it("calls onClearResults when Clear results clicked", () => {
    renderComponent();
    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(screen.getByText("Clear results"));

    expect(defaultProps.onClearResults).toHaveBeenCalled();
  });

  it("calls inferenceChatCompletionQuery where selectedRow has no system_instructions", async () => {
    defaultProps.selectedRow.expected_output = "";

    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: false,
      projectType: ProjectType.POINTWISE,
    });

    renderComponent();
    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(screen.getByText("Duplicate"));

    await waitFor(() => {
      expect(inferenceChatCompletionQuery).toHaveBeenCalled();
    });
  });

  it("calls duplicate with variables when isSideBySide is true", () => {
    const row = defaultProps.selectedRow;
    const mockMutate = jest.fn();
    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: true,
      projectType: ProjectType.SIDE_BY_SIDE,
    });

    (useMutation as jest.Mock).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    });

    renderComponent();
    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(screen.getByText("Duplicate"));

    expect(mockMutate).toHaveBeenCalledWith({
      model_id_a: row.model_id,
      model_id_b: row.chat_turn_b.model_id,
      prompts: [
        {
          role: InferenceChatCompletionPromptRole.USER,
          text: row.input,
        },
      ],
      expected_output: row.expected_output,
      variables: { test: "Test" },
    });
  });

  it("calls duplicate without variables when isSideBySide is true", () => {
    const row = defaultProps.selectedRow;
    defaultProps.selectedRow.variables = null as any;
    const mockMutate = jest.fn();
    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: true,
      projectType: ProjectType.SIDE_BY_SIDE,
    });

    (useMutation as jest.Mock).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    });

    renderComponent();
    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(screen.getByText("Duplicate"));

    expect(mockMutate).toHaveBeenCalledWith({
      model_id_a: row.model_id,
      model_id_b: row.chat_turn_b.model_id,
      prompts: [
        {
          role: InferenceChatCompletionPromptRole.USER,
          text: row.input,
        },
      ],
      expected_output: row.expected_output,
      variables: {},
    });
  });
});
