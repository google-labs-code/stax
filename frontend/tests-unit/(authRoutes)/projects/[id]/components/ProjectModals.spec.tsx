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

import ProjectModals from "@/app/(authRoutes)/projects/[id]/components/ProjectModals";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { enableUploadDatasetModalNextButton } from "@/tests-unit/helpers";
import { testRenderLite } from "@/tests-unit/render";
import { ProjectType } from "@/types";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

jest.mock("@tanstack/react-query", () => ({
  useMutation: jest.fn((options) => ({
    mutate: jest.fn((variables, mutateOptions) => {
      const fakeResult = {
        id: "fake dataset id",
        user_data_sets: [],
        successfulRows: 1,
      };

      // Call the per-call onSuccess (if provided)
      mutateOptions?.onSuccess?.(fakeResult);

      // Call the main onSuccess from useMutation config
      options?.onSuccess?.(fakeResult);
    }),
    isPending: false,
  })),
}));

jest.mock("@/utils/uploadCSV", () => {
  const original = jest.requireActual("@/utils/uploadCSV");

  return {
    ...original,
    createInitialSelectedState: jest.fn((...args) => {
      const originalReturn = original.createInitialSelectedState(...args);

      return { ...originalReturn, inputColumn: true };
    }),
  };
});

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");
jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(() => ({
    allProjects: [],
    refreshProjects: () => {},
    isLoadingProjects: true,
  })),
}));

jest.mock("@/components/ImportFileModal", () => ({
  ImportFileModal: ({ isOpened }: any) =>
    isOpened ? <div data-testid="import-file-modal" /> : null,
}));

const fakeDataset = {
  id: "testId",
  name: "test name",
  created_at: "",
  updated_at: "",
};

const mockSetImportedDataset = jest.fn();
const mockRefetchProject = jest.fn();
const mockOnTooltipVisibilityChange = jest.fn();

describe("ProjectModals", () => {
  const baseProps = {
    projectId: "proj-1",
    importedDataset: null,
    projectType: ProjectType.POINTWISE,
    setImportedDataset: mockSetImportedDataset,
    refetchProject: mockRefetchProject,
    onTooltipVisibilityChange: mockOnTooltipVisibilityChange,
  };

  beforeEach(() => jest.clearAllMocks());

  it("renders only the modals that are open", () => {
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isEditProjectModalOpened: false,
        isGetStartedModalOpened: false,
        isAddDatasetModalOpened: false,
        isUploadDatasetModalOpened: false,
      },
    }));

    testRenderLite(<ProjectModals {...baseProps} />);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("shows editProjectModal when open", () => {
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isEditProjectModalOpened: true,
      },
    }));

    testRenderLite(<ProjectModals {...baseProps} />);
    expect(screen.getByTestId("edit-project-modal")).toBeInTheDocument();
  });

  it("should render getStartedModal when open", async () => {
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isGetStartedModalOpened: true,
      },
    }));

    const customScreen = testRenderLite(<ProjectModals {...baseProps} />);

    expect(customScreen.getByTestId("get-started-modal")).toBeInTheDocument();
  });

  it("should render addDatasetModal when open", async () => {
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isAddDatasetModalOpened: true,
      },
    }));

    const customScreen = testRenderLite(<ProjectModals {...baseProps} />);

    expect(customScreen.getByTestId("add-dataset-modal")).toBeInTheDocument();
  });

  it("should render uploadDatasetModal when open", () => {
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isUploadDatasetModalOpened: true,
      },
    }));

    testRenderLite(<ProjectModals {...baseProps} />);

    expect(screen.getByTestId("upload-dataset-modal")).toBeInTheDocument();
  });

  it("should call the proper callbacks when Upload CSV is clicked", async () => {
    const mockCloseAddStartedModal = jest.fn();
    const mockOpenUploadDatasetModal = jest.fn();
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isAddDatasetModalOpened: true,
        closeAddDatasetModal: mockCloseAddStartedModal,
        openUploadDatasetModal: mockOpenUploadDatasetModal,
      },
    }));

    const customScreen = testRenderLite(<ProjectModals {...baseProps} />);

    const uploadCSVBtn = customScreen.getByRole("button", {
      name: /Upload CSV/i,
    });

    await userEvent.click(uploadCSVBtn);

    expect(mockCloseAddStartedModal).toHaveBeenCalled();
    expect(mockOpenUploadDatasetModal).toHaveBeenCalled();
  });

  it("should call the proper callbacks when onImport is clicked", async () => {
    const mockCloseAddStartedModal = jest.fn();
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isAddDatasetModalOpened: true,
        closeAddDatasetModal: mockCloseAddStartedModal,
      },
    }));

    const customBaseProps = { ...baseProps, importedDataset: fakeDataset };
    const customScreen = testRenderLite(<ProjectModals {...customBaseProps} />);

    const importBtn = customScreen.getByRole("button", {
      name: /Import/i,
    });

    await userEvent.click(importBtn);

    expect(mockCloseAddStartedModal).toHaveBeenCalled();
    expect(mockOnTooltipVisibilityChange).toHaveBeenCalledWith(true);
    expect(mockRefetchProject).toHaveBeenCalled();
  });

  it("should call the proper callbacks when 'Open prompt playground' is clicked", async () => {
    const mockCloseGetStartedModal = jest.fn();
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isGetStartedModalOpened: true,
        closeGetStartedModal: mockCloseGetStartedModal,
      },
    }));

    const customScreen = testRenderLite(<ProjectModals {...baseProps} />);

    const importBtn = customScreen.getByRole("button", {
      name: /Open prompt playground/i,
    });

    await userEvent.click(importBtn);

    expect(mockCloseGetStartedModal).toHaveBeenCalled();
    expect(mockOnTooltipVisibilityChange).toHaveBeenCalledWith(false);
  });

  it("should call respective functions when 'Import dataset' is clicked", async () => {
    const mockCloseGetStartedModal = jest.fn();
    const mockOpenAddDatasetModal = jest.fn();
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isGetStartedModalOpened: true,
        closeGetStartedModal: mockCloseGetStartedModal,
        openAddDatasetModal: mockOpenAddDatasetModal,
      },
    }));

    const customScreen = testRenderLite(<ProjectModals {...baseProps} />);

    const importDatasetBtn = customScreen.getByRole("button", {
      name: /Import dataset/i,
    });

    await userEvent.click(importDatasetBtn);

    expect(mockOnTooltipVisibilityChange).toHaveBeenCalledWith(false);
    expect(mockCloseGetStartedModal).toHaveBeenCalled();
    expect(mockOpenAddDatasetModal).toHaveBeenCalled();
  });

  it("should call onClose and setImportedDataset after adding a dataset", async () => {
    const mockCloseUploadDatasetModal = jest.fn();
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectModals: {
        isUploadDatasetModalOpened: true,
        closeUploadDatasetModal: mockCloseUploadDatasetModal,
      },
    }));

    await enableUploadDatasetModalNextButton(() => {
      testRenderLite(<ProjectModals {...baseProps} />);
    });

    const nextBtn = screen.getByText("Next");
    await userEvent.click(nextBtn);

    expect(mockCloseUploadDatasetModal).toHaveBeenCalled();

    await waitFor(async () => {
      const datasetBtn = screen.getByText("Add dataset");
      await userEvent.click(datasetBtn);
    });

    expect(mockSetImportedDataset).toHaveBeenCalled();
  });
});
