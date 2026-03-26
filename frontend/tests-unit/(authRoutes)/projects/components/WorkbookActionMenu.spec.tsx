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
import { useProjectsContext } from "@/hooks/useProjectsContext";
import * as clientQueries from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { ProjectType, Provider } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

jest.mock("@/queries/clientQueries", () => ({
  inferenceChatCompletionQuery: jest.fn(),
  generateSxSInferenceQuery: jest.fn(),
  updateExpectedOutput: jest.fn(),
}));

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

(useProjectsContext as jest.Mock).mockReturnValue({
  defaultProjectId: "proj-123",
});

(useProjectContext as jest.Mock).mockReturnValue({
  isSideBySide: false,
  projectType: ProjectType.POINTWISE,
});

const renderComponent = (overrides = {}) => {
  const defaultProps = {
    selectedRow: {
      model_id: "model-123",
      model_provider: Provider.OPENAI,
      input: "Test input",
      system_instructions: "System message",
      expected_output: "Expected output",
      chat_id: "chat-123",
      isSubRow: false,
    },
    onRowDelete: {
      mutate: jest.fn(),
    },
    setRowSelection: jest.fn(),
    onAddTags: jest.fn(),
    onRemoveTags: jest.fn(),
    onClearResults: jest.fn(),
    onDuplicateSuccess: jest.fn(),
  };

  return testRender(
    <WorkbookActionMenu projectId={""} {...defaultProps} {...overrides} />,
  );
};

describe("WorkbookActionMenu", () => {
  it("renders menu icon", () => {
    renderComponent();
    expect(screen.getByRole("button")).toBeInTheDocument();
  });

  it("opens the menu and displays actions", async () => {
    renderComponent();
    fireEvent.click(screen.getByRole("button"));
    expect(await screen.findByText("Duplicate")).toBeInTheDocument();
    expect(screen.getByText("Add tags")).toBeInTheDocument();
    expect(screen.getByText("Remove tags")).toBeInTheDocument();
    expect(screen.getByText("Clear results")).toBeInTheDocument();
  });

  it("calls onAddTags when 'Add tags' is clicked", async () => {
    const onAddTags = jest.fn();
    renderComponent({ onAddTags });

    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(await screen.findByText("Add tags"));
    expect(onAddTags).toHaveBeenCalledWith("chat-123");
  });

  it("calls onRemoveTags when 'Remove tags' is clicked", async () => {
    const onRemoveTags = jest.fn();
    renderComponent({ onRemoveTags });

    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(await screen.findByText("Remove tags"));
    expect(onRemoveTags).toHaveBeenCalledWith("chat-123");
  });

  it("calls inference mutation and updateExpectedOutput on 'Duplicate' for POINTWISE", async () => {
    const inferenceResponse = {
      chat_turn_id: "turn-1",
      model_output: { text: "AI Output" },
    };

    (clientQueries.inferenceChatCompletionQuery as jest.Mock).mockResolvedValue(
      inferenceResponse,
    );
    const updateExpectedOutput =
      clientQueries.updateExpectedOutput as jest.Mock;
    updateExpectedOutput.mockResolvedValue({});

    const onDuplicateSuccess = jest.fn();

    renderComponent({ onDuplicateSuccess });

    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(await screen.findByText("Duplicate"));

    await waitFor(() => {
      expect(clientQueries.inferenceChatCompletionQuery).toHaveBeenCalled();
      expect(updateExpectedOutput).toHaveBeenCalledWith(
        "turn-1",
        "Expected output",
      );
      expect(onDuplicateSuccess).toHaveBeenCalled();
    });
  });

  it("calls SxS mutation on 'Duplicate' for SIDE_BY_SIDE", async () => {
    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: true,
      projectType: ProjectType.SIDE_BY_SIDE,
    });

    const generateSxSInferenceQuery =
      clientQueries.generateSxSInferenceQuery as jest.Mock;
    generateSxSInferenceQuery.mockResolvedValue({
      chat_id: "chat-new",
      input: "Test input",
      model_id: "model-123",
    });

    const onDuplicateSuccess = jest.fn();
    renderComponent({
      onDuplicateSuccess,
      selectedRow: {
        model_id: "model-123",
        input: "Test input",
        expected_output: "Expected output",
        metadata: {},
        chat_id: "chat-123",
      },
    });

    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(await screen.findByText("Duplicate"));

    await waitFor(() => {
      expect(generateSxSInferenceQuery).toHaveBeenCalled();
      expect(onDuplicateSuccess).toHaveBeenCalled();
    });
  });

  it("disables 'Duplicate' if model_id is missing", async () => {
    renderComponent({
      selectedRow: {
        model_id: null,
        input: "Test input",
        expected_output: "Expected output",
        chat_id: "chat-123",
      },
    });

    fireEvent.click(screen.getByRole("button"));
    const duplicateItem = await screen.findByText("Duplicate");

    expect(duplicateItem.closest("button")).toBeDisabled();
  });

  it("renders 'Report legal issue' if provider is GOOGLE", async () => {
    renderComponent({
      selectedRow: {
        model_provider: Provider.GOOGLE,
        model_id: "google-1",
        input: "text",
        chat_id: "chat-123",
      },
    });

    fireEvent.click(screen.getByRole("button"));
    expect(await screen.findByText("Report legal issue")).toBeInTheDocument();
  });
});
