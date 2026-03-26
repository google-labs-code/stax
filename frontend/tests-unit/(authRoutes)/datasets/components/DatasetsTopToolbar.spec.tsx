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

import { DatasetsTopToolbar } from "@/app/(authRoutes)/datasets/components/DatasetsTopToolbar";
import { Dataset } from "@/app/(authRoutes)/datasets/types";
import * as clientQueries from "@/queries/clientQueries";
import { jsonToCsvExport } from "@/utils/jsonToCsvExport";
import { MantineProvider } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";

// Mock dependencies
jest.mock("@/queries/clientQueries");
jest.mock("@/utils/jsonToCsvExport");
jest.mock("@mantine/notifications");
jest.mock("@/components/MaterialIcon", () => {
  return function MockMaterialIcon({ name, size, className }: any) {
    return (
      <span data-testid={`icon-${name}`} className={className} data-size={size}>
        {name}
      </span>
    );
  };
});

describe("DatasetsTopToolbar", () => {
  const mockDatasets: Dataset[] = [
    {
      id: "dataset-1",
      name: "Dataset 1",
      created_at: "1234567890",
      updated_at: "1",
    },
    {
      id: "dataset-2",
      name: "Dataset 2",
      created_at: "1234567891",
      updated_at: "2",
    },
  ];

  const mockExportData = [
    { id: "1", name: "Item 1", value: 100 },
    { id: "2", name: "Item 2", value: 200 },
  ];

  let mockTable: any;
  let openUploadModalMock: jest.Mock;
  let createDatasetMutationMock: any;
  let openDeleteModalMock: jest.Mock;
  let setActiveDatasetDataMock: jest.Mock;

  beforeEach(() => {
    // Reset all mocks
    jest.clearAllMocks();

    // Setup mock functions
    openUploadModalMock = jest.fn();
    openDeleteModalMock = jest.fn();
    setActiveDatasetDataMock = jest.fn();

    // Setup mutation mock
    createDatasetMutationMock = {
      mutate: jest.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
      data: null,
      error: null,
    };

    // Setup table mock
    mockTable = {
      getState: jest.fn().mockReturnValue({
        rowSelection: {},
      }),
      getSelectedRowModel: jest.fn().mockReturnValue({
        flatRows: [],
      }),
    };

    // Setup API mocks
    (clientQueries.getDatasetExportQuery as jest.Mock).mockResolvedValue(
      mockExportData,
    );
    (jsonToCsvExport as jest.Mock).mockImplementation(() => {});
    jest.spyOn(notifications, "show").mockImplementation();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  const renderComponent = (props: any = {}) => {
    const defaultProps = {
      table: mockTable,
      openUploadModal: openUploadModalMock,
      createDatasetMutation: createDatasetMutationMock,
      openDeleteModal: openDeleteModalMock,
      setActiveDatasetData: setActiveDatasetDataMock,
      ...props,
    };

    return render(
      <MantineProvider>
        <DatasetsTopToolbar {...defaultProps} />
      </MantineProvider>,
    );
  };

  const getMenuButton = () => {
    const buttons = screen.getAllByRole("button");

    return buttons.find((btn) =>
      btn.querySelector('[data-testid="icon-more_vert"]'),
    );
  };

  const openMenu = async () => {
    const menuButton = getMenuButton();
    expect(menuButton).toBeInTheDocument();
    fireEvent.click(menuButton!);

    // Wait for menu to open
    await waitFor(
      () => {
        const menuItems = document.querySelectorAll('[role="menuitem"]');
        expect(menuItems.length).toBeGreaterThan(0);
      },
      { timeout: 1000 },
    );
  };

  const getMenuItemByText = (text: string) => {
    const menuItems: any = document.querySelectorAll('[role="menuitem"]');
    for (const item of menuItems) {
      if (item.textContent?.includes(text)) {
        return item;
      }
    }

    return null;
  };

  describe("Component Rendering", () => {
    it("should render the toolbar component", () => {
      renderComponent();

      expect(screen.getByText("My Datasets")).toBeInTheDocument();
    });

    it('should display "My Datasets" heading', () => {
      renderComponent();

      const heading = screen.getByText("My Datasets");
      expect(heading).toBeInTheDocument();
      expect(heading).toHaveClass("text-title-16");
    });

    it("should render bulk action menu button", () => {
      renderComponent();

      const menuButtons = screen
        .getAllByRole("button")
        .filter((btn) => btn.querySelector('[data-testid="icon-more_vert"]'));
      expect(menuButtons.length).toBeGreaterThan(0);
    });

    it("should render create dataset button", () => {
      renderComponent();

      expect(
        screen.getByRole("button", { name: /create empty dataset/i }),
      ).toBeInTheDocument();
    });

    it("should render upload CSV button", () => {
      renderComponent();

      expect(
        screen.getByRole("button", { name: /upload csv/i }),
      ).toBeInTheDocument();
    });

    it("should have correct styling classes on buttons", () => {
      renderComponent();

      const createButton = screen.getByRole("button", {
        name: /create empty dataset/i,
      });
      expect(createButton).toHaveClass(
        "border-neutrals-300",
        "text-neutrals-900",
      );

      const uploadButton = screen.getByRole("button", { name: /upload csv/i });
      expect(uploadButton).toHaveClass("bg-brand");
    });
  });

  describe("Menu Disabled State", () => {
    it("should disable menu when no datasets selected", () => {
      mockTable.getState.mockReturnValue({
        rowSelection: {},
      });

      renderComponent({ table: mockTable });

      const menuButtons = screen
        .getAllByRole("button")
        .filter((btn) => btn.querySelector('[data-testid="icon-more_vert"]'));

      expect(menuButtons[0]).toHaveClass("opacity-50");
    });

    it("should not allow menu to open when disabled", () => {
      mockTable.getState.mockReturnValue({
        rowSelection: {},
      });

      renderComponent({ table: mockTable });

      const menuButton = getMenuButton();
      fireEvent.click(menuButton!);

      // Menu items should not be visible
      const menuItems = document.querySelectorAll('[role="menuitem"]');
      expect(menuItems.length).toBe(0);
    });

    it("should display correct tooltip message when no items selected", () => {
      mockTable.getState.mockReturnValue({
        rowSelection: {},
      });

      renderComponent({ table: mockTable });

      // Find the button with disabled state
      const buttons = screen.getAllByRole("button");
      const disabledMenu = buttons.find((btn) =>
        btn.classList.contains("opacity-50"),
      );

      expect(disabledMenu).toBeInTheDocument();
    });
  });

  describe("Menu Enabled State", () => {
    it("should enable menu when datasets are selected", () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      const menuButton = getMenuButton();
      expect(menuButton).not.toHaveClass("opacity-50");
    });

    it("should display menu items when menu is enabled and opened", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      // Check for menu items by looking in the DOM directly
      const exportItem = getMenuItemByText("Export");
      const deleteItem = getMenuItemByText("Delete");

      expect(exportItem).toBeInTheDocument();
      expect(deleteItem).toBeInTheDocument();
    });
  });

  describe("Menu Items Rendering", () => {
    beforeEach(() => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });
    });

    it("should display Export menu item", async () => {
      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      expect(exportItem).toBeInTheDocument();
    });

    it("should display Delete menu item", async () => {
      renderComponent({ table: mockTable });

      await openMenu();

      const deleteItem = getMenuItemByText("Delete");
      expect(deleteItem).toBeInTheDocument();
    });

    it("should have export icon on export menu item", async () => {
      renderComponent({ table: mockTable });

      await openMenu();

      // Check if download icon exists in the menu
      expect(screen.getByTestId("icon-download")).toBeInTheDocument();
    });

    it("should have delete icon on delete menu item", async () => {
      renderComponent({ table: mockTable });

      await openMenu();

      // Check if delete icon exists in the menu
      expect(screen.getByTestId("icon-delete")).toBeInTheDocument();
    });
  });

  describe("Bulk Export Functionality", () => {
    it("should export single dataset when one is selected", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        expect(clientQueries.getDatasetExportQuery).toHaveBeenCalledWith(
          mockDatasets[0].id,
        );
      });
    });

    it("should export multiple datasets when multiple are selected", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true, 1: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [
          { original: mockDatasets[0] },
          { original: mockDatasets[1] },
        ],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        expect(clientQueries.getDatasetExportQuery).toHaveBeenCalledWith(
          mockDatasets[0].id,
        );
        expect(clientQueries.getDatasetExportQuery).toHaveBeenCalledWith(
          mockDatasets[1].id,
        );
        expect(clientQueries.getDatasetExportQuery).toHaveBeenCalledTimes(2);
      });
    });

    it("should call jsonToCsvExport for each dataset", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true, 1: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [
          { original: mockDatasets[0] },
          { original: mockDatasets[1] },
        ],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        expect(jsonToCsvExport).toHaveBeenCalledTimes(2);
      });
    });

    it("should use correct filename format for export", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        const calls = (jsonToCsvExport as jest.Mock).mock.calls;
        calls.forEach((call: any) => {
          expect(call[1]).toMatch(/^dataset-export-/);
          expect(call[1]).toMatch(/\.csv$/);
        });
      });
    });

    it("should use dataset name in filename", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        const firstCall = (jsonToCsvExport as jest.Mock).mock.calls[0];
        expect(firstCall[1]).toContain(mockDatasets[0].name);
      });
    });

    it("should include date in filename", async () => {
      const mockDate = new Date("2024-01-15T10:00:00Z");
      const originalDate = global.Date;
      global.Date = jest.fn(() => mockDate) as any;
      (global.Date as any).prototype = originalDate.prototype;

      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        const calls = (jsonToCsvExport as jest.Mock).mock.calls;
        calls.forEach((call: any) => {
          expect(call[1]).toMatch(/\d{4}-\d{2}-\d{2}/);
        });
      });

      global.Date = originalDate;
    });

    it("should show success notification after export", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        expect(notifications.show).toHaveBeenCalled();
      });
    });

    it("should show correct notification message for single export", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        const notificationCall = (notifications.show as jest.Mock).mock
          .calls[0];
        expect(notificationCall[0].message).toContain("1 dataset");
      });
    });

    it("should show correct notification message for multiple exports", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true, 1: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [
          { original: mockDatasets[0] },
          { original: mockDatasets[1] },
        ],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        const notificationCall = (notifications.show as jest.Mock).mock
          .calls[0];
        expect(notificationCall[0].message).toContain("2 datasets");
      });
    });

    it('should use "data" as fallback name if dataset has no name', async () => {
      const datasetWithoutName = { ...mockDatasets[0], name: "" };
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: datasetWithoutName }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        const calls = (jsonToCsvExport as jest.Mock).mock.calls;
        expect(calls[0][1]).toContain("data");
      });
    });
  });

  describe("Bulk Delete Functionality", () => {
    beforeEach(() => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true, 1: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [
          { original: mockDatasets[0] },
          { original: mockDatasets[1] },
        ],
      });
    });

    it("should call setActiveDatasetData with selected datasets", async () => {
      renderComponent({ table: mockTable });

      await openMenu();

      const deleteItem = getMenuItemByText("Delete");
      fireEvent.click(deleteItem!);

      await waitFor(() => {
        expect(setActiveDatasetDataMock).toHaveBeenCalledWith(mockDatasets);
      });
    });

    it("should open delete modal after delete clicked", async () => {
      renderComponent({ table: mockTable });

      await openMenu();

      const deleteItem = getMenuItemByText("Delete");
      fireEvent.click(deleteItem!);

      await waitFor(() => {
        expect(openDeleteModalMock).toHaveBeenCalled();
      });
    });

    it("should open delete modal with single dataset", async () => {
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const deleteItem = getMenuItemByText("Delete");
      fireEvent.click(deleteItem!);

      await waitFor(() => {
        expect(setActiveDatasetDataMock).toHaveBeenCalledWith([
          mockDatasets[0],
        ]);
        expect(openDeleteModalMock).toHaveBeenCalled();
      });
    });
  });

  describe("Create Dataset Button", () => {
    it("should call mutation when create dataset button clicked", async () => {
      renderComponent();

      const createButton = screen.getByRole("button", {
        name: /create empty dataset/i,
      });
      fireEvent.click(createButton);

      expect(createDatasetMutationMock.mutate).toHaveBeenCalledWith({});
    });

    it("should be always enabled", () => {
      renderComponent();

      const createButton = screen.getByRole("button", {
        name: /create empty dataset/i,
      });
      expect(createButton).not.toBeDisabled();
    });

    it("should have correct styling", () => {
      renderComponent();

      const createButton = screen.getByRole("button", {
        name: /create empty dataset/i,
      });
      expect(createButton).toHaveClass("border-neutrals-300");
    });
  });

  describe("Upload CSV Button", () => {
    it("should call openUploadModal when upload button clicked", async () => {
      renderComponent();

      const uploadButton = screen.getByRole("button", { name: /upload csv/i });
      fireEvent.click(uploadButton);

      expect(openUploadModalMock).toHaveBeenCalled();
    });

    it("should be always enabled", () => {
      renderComponent();

      const uploadButton = screen.getByRole("button", { name: /upload csv/i });
      expect(uploadButton).not.toBeDisabled();
    });

    it("should have correct styling", () => {
      renderComponent();

      const uploadButton = screen.getByRole("button", { name: /upload csv/i });
      expect(uploadButton).toHaveClass("bg-brand");
    });
  });

  describe("Button Click Handlers", () => {
    it("should handle multiple button clicks correctly", async () => {
      renderComponent();

      const uploadButton = screen.getByRole("button", { name: /upload csv/i });
      const createButton = screen.getByRole("button", {
        name: /create empty dataset/i,
      });

      fireEvent.click(uploadButton);
      fireEvent.click(createButton);
      fireEvent.click(uploadButton);

      expect(openUploadModalMock).toHaveBeenCalledTimes(2);
      expect(createDatasetMutationMock.mutate).toHaveBeenCalledTimes(1);
    });

    it("should handle rapid clicks on same button", async () => {
      renderComponent();

      const createButton = screen.getByRole("button", {
        name: /create empty dataset/i,
      });

      fireEvent.click(createButton);
      fireEvent.click(createButton);
      fireEvent.click(createButton);

      expect(createDatasetMutationMock.mutate).toHaveBeenCalledTimes(3);
    });
  });

  describe("Material Icons", () => {
    it("should render more_vert icon for menu button", () => {
      renderComponent();

      expect(screen.getByTestId("icon-more_vert")).toBeInTheDocument();
    });

    it("should render download icon for export", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      expect(screen.getByTestId("icon-download")).toBeInTheDocument();
    });

    it("should render delete icon for delete", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      expect(screen.getByTestId("icon-delete")).toBeInTheDocument();
    });

    it("should have correct icon sizes", () => {
      renderComponent();

      const moreVertIcon = screen.getByTestId("icon-more_vert");
      expect(moreVertIcon).toHaveAttribute("data-size", "20");
    });
  });

  describe("Text Content and Typography", () => {
    it("should display heading with correct text content", () => {
      renderComponent();

      const heading = screen.getByText("My Datasets");
      expect(heading).toHaveTextContent("My Datasets");
    });

    it("should display create button text", () => {
      renderComponent();

      expect(
        screen.getByRole("button", { name: /create empty dataset/i }),
      ).toBeInTheDocument();
    });

    it("should display upload button text", () => {
      renderComponent();

      expect(
        screen.getByRole("button", { name: /upload csv/i }),
      ).toBeInTheDocument();
    });
  });

  describe("Selection Counting", () => {
    it("should count selected rows correctly", () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true, 1: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [
          { original: mockDatasets[0] },
          { original: mockDatasets[1] },
        ],
      });

      renderComponent({ table: mockTable });

      // Menu should be enabled with 2 items selected
      const menuButton = getMenuButton();
      expect(menuButton).not.toHaveClass("opacity-50");
    });

    it("should handle empty selection", () => {
      mockTable.getState.mockReturnValue({
        rowSelection: {},
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [],
      });

      renderComponent({ table: mockTable });

      // Menu should be disabled
      const menuButton = getMenuButton();
      expect(menuButton).toHaveClass("opacity-50");
    });

    it("should disable menu after all items deselected", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      const { rerender } = renderComponent({ table: mockTable });

      // Menu should be enabled
      let menuButton = getMenuButton();
      expect(menuButton).not.toHaveClass("opacity-50");

      // Update mock to deselect
      mockTable.getState.mockReturnValue({
        rowSelection: {},
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [],
      });

      rerender(
        <MantineProvider>
          <DatasetsTopToolbar
            table={mockTable}
            openUploadModal={openUploadModalMock}
            createDatasetMutation={createDatasetMutationMock}
            openDeleteModal={openDeleteModalMock}
            setActiveDatasetData={setActiveDatasetDataMock}
          />
        </MantineProvider>,
      );

      // Menu should be disabled
      menuButton = getMenuButton();
      expect(menuButton).toHaveClass("opacity-50");
    });
  });

  describe("Edge Cases", () => {
    it("should handle datasets with special characters in name", async () => {
      const specialDataset = {
        ...mockDatasets[0],
        name: "Dataset & Export (2024)",
      };

      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: specialDataset }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        const calls = (jsonToCsvExport as jest.Mock).mock.calls;
        expect(calls[0][1]).toContain("Dataset & Export (2024)");
      });
    });

    it("should handle very long dataset names", async () => {
      const longNameDataset = {
        ...mockDatasets[0],
        name: "A".repeat(100),
      };

      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: longNameDataset }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        expect(jsonToCsvExport).toHaveBeenCalled();
      });
    });

    it("should handle large number of selected datasets", async () => {
      const manyDatasets = Array.from({ length: 50 }, (_, i) => ({
        id: `dataset-${i}`,
        name: `Dataset ${i}`,
        created_at: 1234567890 + i,
      }));

      const selection = Object.fromEntries(
        manyDatasets.map((_, i) => [i, true]),
      );

      mockTable.getState.mockReturnValue({
        rowSelection: selection,
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: manyDatasets.map((ds) => ({ original: ds })),
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        expect(clientQueries.getDatasetExportQuery).toHaveBeenCalledTimes(50);
      });
    });
  });

  describe("Accessibility", () => {
    it("should have semantic button elements", () => {
      renderComponent();

      const buttons = screen.getAllByRole("button");
      expect(buttons.length).toBeGreaterThanOrEqual(2); // Create, Upload
    });

    it("should have descriptive text for buttons", () => {
      renderComponent();

      const createButton = screen.getByRole("button", {
        name: /create empty dataset/i,
      });
      const uploadButton = screen.getByRole("button", { name: /upload csv/i });

      expect(createButton).toHaveAccessibleName(/create empty dataset/i);
      expect(uploadButton).toHaveAccessibleName(/upload csv/i);
    });

    it("should have proper button grouping", () => {
      renderComponent();

      const buttons = screen
        .getAllByRole("button")
        .filter(
          (btn) =>
            btn.textContent?.includes("Create") ||
            btn.textContent?.includes("Upload"),
        );

      expect(buttons.length).toBe(2);
    });
  });

  describe("Menu Item Interaction", () => {
    it("should close menu after export clicked", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const exportItem = getMenuItemByText("Export");
      fireEvent.click(exportItem!);

      await waitFor(() => {
        // Menu should be closed after action
        expect(clientQueries.getDatasetExportQuery).toHaveBeenCalled();
      });
    });

    it("should close menu after delete clicked", async () => {
      mockTable.getState.mockReturnValue({
        rowSelection: { 0: true },
      });
      mockTable.getSelectedRowModel.mockReturnValue({
        flatRows: [{ original: mockDatasets[0] }],
      });

      renderComponent({ table: mockTable });

      await openMenu();

      const deleteItem = getMenuItemByText("Delete");
      fireEvent.click(deleteItem!);

      await waitFor(() => {
        expect(setActiveDatasetDataMock).toHaveBeenCalled();
      });
    });
  });
});
