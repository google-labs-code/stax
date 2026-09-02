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

import CreateNewEntityModal from "@/components/CreateNewProjectModal";
import { useMutation } from "@tanstack/react-query";
import { fireEvent, screen, waitFor } from "@testing-library/react";

import { testRender } from "../render";

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: () => ({
    refreshProjects: jest.fn(),
  }),
}));

jest.mock("@/hooks/useDatasetsContext", () => ({
  useDatasetsContext: () => ({
    refreshDatasets: jest.fn(),
  }),
}));

jest.mock("@tanstack/react-query", () => {
  const actual = jest.requireActual("@tanstack/react-query");

  return {
    ...actual,
    useMutation: jest.fn(),
  };
});

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

describe("CreateNewEntityModal", () => {
  const onClose = jest.fn();
  const onBackClick = jest.fn();
  const setSelectedProject = jest.fn();

  const defaultProps = {
    isOpened: true,
    onClose,
    onBackClick,
    setSelectedProject,
  };

  const fillFormAndSubmit = () => {
    fireEvent.change(screen.getByPlaceholderText("Name"), {
      target: { value: "Test Project" },
    });
    fireEvent.click(screen.getByText("Create project"));
  };

  beforeEach(() => {
    jest.clearAllMocks();

    (useMutation as jest.Mock).mockImplementation(({ onSuccess }) => ({
      mutate: (data: any) => {
        onSuccess({
          project_id: "123",
          name: data.name,
        });
      },
      isPending: false,
    }));
  });

  it("renders modal with project title", () => {
    testRender(<CreateNewEntityModal {...defaultProps} />);
    expect(screen.getByText("Create new project")).toBeInTheDocument();
  });

  it("renders modal with dataset title when isDataset=true", () => {
    testRender(<CreateNewEntityModal {...defaultProps} isDataset />);
    expect(screen.getByText("Create new dataset")).toBeInTheDocument();
  });

  it("calls mutation and callbacks on valid submission", async () => {
    testRender(<CreateNewEntityModal {...defaultProps} />);
    fillFormAndSubmit();

    await waitFor(() => {
      expect(setSelectedProject).toHaveBeenCalledWith({
        value: "123",
        label: "Test Project",
        isNew: true,
      });

      expect(onBackClick).toHaveBeenCalled();
    });
  });

  it("toggles between project types", () => {
    testRender(<CreateNewEntityModal {...defaultProps} />);
    const pointwiseButton = screen.getByText("Pointwise");
    const sideBySideButton = screen.getByText("Side by Side");

    fireEvent.click(sideBySideButton);
    expect(sideBySideButton).toHaveClass(
      "text-title-12 text-body-12 m_811560b9 mantine-Button-label",
    );

    fireEvent.click(pointwiseButton);
    expect(pointwiseButton).toHaveClass(
      "text-title-12 text-body-12 m_811560b9 mantine-Button-label",
    );
  });

  it("calls dataset mutation when isDataset=true", async () => {
    (useMutation as jest.Mock).mockImplementation(({ onSuccess }) => ({
      mutate: (data: any) => {
        onSuccess({
          id: "456",
          name: data.name,
        });
      },
      isPending: false,
    }));

    testRender(<CreateNewEntityModal {...defaultProps} isDataset />);
    fireEvent.change(screen.getByPlaceholderText("Name"), {
      target: { value: "Dataset One" },
    });

    fireEvent.click(screen.getByText("Create dataset"));

    await waitFor(() => {
      expect(setSelectedProject).toHaveBeenCalledWith({
        value: "456",
        label: "Dataset One",
        isNew: true,
      });
    });
  });

  it("shows error notification and calls onBackClick on mutation error", async () => {
    const errorMessage = "Creation failed";

    (useMutation as jest.Mock).mockImplementation(({ onError }) => ({
      mutate: () => {
        onError({ message: errorMessage });
      },
      isPending: false,
    }));

    const { notifications } = require("@mantine/notifications");

    testRender(<CreateNewEntityModal {...defaultProps} />);
    fillFormAndSubmit();

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalledWith(
        expect.objectContaining({
          message: expect.objectContaining({
            props: expect.objectContaining({
              children: "Creation failed",
            }),
          }),
        }),
      );

      expect(onBackClick).toHaveBeenCalled();
    });
  });
});
