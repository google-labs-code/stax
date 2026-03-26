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

import DeleteDataModal from "@/components/DeleteDataModal";
import { deleteUserDataQuery } from "@/queries/clientQueries";
import { notifications } from "@mantine/notifications";
import { fireEvent, screen, waitFor } from "@testing-library/react";

import { testRender } from "../render";

jest.mock("@/queries/clientQueries", () => ({
  deleteUserDataQuery: jest.fn(),
}));

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

describe("DeleteDataModal", () => {
  const onCloseMock = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders modal with correct title and content when opened", () => {
    testRender(<DeleteDataModal isOpened={true} onClose={onCloseMock} />);

    expect(screen.getByText("Delete Stax data")).toBeInTheDocument();
    expect(
      screen.getByText(/You are about to permanently delete your data/),
    ).toBeInTheDocument();
    expect(
      screen.getByLabelText("Type 'delete' to confirm"),
    ).toBeInTheDocument();
    expect(screen.getByText("Cancel")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Delete" })).toBeInTheDocument();
  });

  it("doesn't render the modal when isOpened is false", () => {
    testRender(<DeleteDataModal isOpened={false} onClose={onCloseMock} />);

    expect(screen.queryByText("Delete Stax data")).not.toBeInTheDocument();
  });

  it("disables the delete button initially", () => {
    testRender(<DeleteDataModal isOpened={true} onClose={onCloseMock} />);

    const deleteButton = screen.getByRole("button", { name: "Delete" });
    expect(deleteButton).toBeDisabled();
  });

  it("enables the delete button when confirmation text is 'delete'", async () => {
    testRender(<DeleteDataModal isOpened={true} onClose={onCloseMock} />);

    const confirmInput = screen.getByLabelText("Type 'delete' to confirm");
    fireEvent.change(confirmInput, { target: { value: "delete" } });

    await waitFor(() => {
      const deleteButton = screen.getByRole("button", { name: "Delete" });
      expect(deleteButton).not.toBeDisabled();
    });
  });

  it("toggles delete button state based on confirmation text", () => {
    testRender(<DeleteDataModal isOpened={true} onClose={onCloseMock} />);

    const deleteButton = screen.getByRole("button", { name: "Delete" });
    const confirmInput = screen.getByLabelText("Type 'delete' to confirm");

    expect(deleteButton).toBeDisabled();

    fireEvent.change(confirmInput, { target: { value: "delete" } });

    expect(screen.getByRole("button", { name: "Delete" })).not.toBeDisabled();

    fireEvent.change(confirmInput, { target: { value: "something else" } });

    expect(screen.getByRole("button", { name: "Delete" })).toBeDisabled();
  });

  it("closes the modal and resets confirmation text when Cancel is clicked", () => {
    testRender(<DeleteDataModal isOpened={true} onClose={onCloseMock} />);

    const cancelButton = screen.getByText("Cancel");
    fireEvent.click(cancelButton);

    expect(onCloseMock).toHaveBeenCalled();
  });

  it("triggers the delete mutation when Delete button is clicked", async () => {
    (deleteUserDataQuery as jest.Mock).mockResolvedValue({ success: true });

    testRender(<DeleteDataModal isOpened={true} onClose={onCloseMock} />);

    const confirmInput = screen.getByLabelText("Type 'delete' to confirm");
    fireEvent.change(confirmInput, { target: { value: "delete" } });

    await waitFor(() => {
      const deleteButton = screen.getByRole("button", { name: "Delete" });
      expect(deleteButton).not.toBeDisabled();
      fireEvent.click(deleteButton);
    });

    expect(deleteUserDataQuery).toHaveBeenCalled();
  });

  it("shows success notification and closes modal on successful deletion", async () => {
    (deleteUserDataQuery as jest.Mock).mockResolvedValue({ success: true });

    testRender(<DeleteDataModal isOpened={true} onClose={onCloseMock} />);

    const confirmInput = screen.getByLabelText("Type 'delete' to confirm");
    fireEvent.change(confirmInput, { target: { value: "delete" } });

    await waitFor(() => {
      const deleteButton = screen.getByRole("button", { name: "Delete" });
      expect(deleteButton).not.toBeDisabled();
      fireEvent.click(deleteButton);
    });

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalledWith(
        expect.objectContaining({
          message: expect.stringContaining("All data deleted successfully"),
        }),
      );
      expect(onCloseMock).toHaveBeenCalled();
    });
  });

  it("closes the modal on deletion error", async () => {
    (deleteUserDataQuery as jest.Mock).mockRejectedValue(
      new Error("Delete failed"),
    );

    testRender(<DeleteDataModal isOpened={true} onClose={onCloseMock} />);

    const confirmInput = screen.getByLabelText("Type 'delete' to confirm");
    fireEvent.change(confirmInput, { target: { value: "delete" } });

    await waitFor(() => {
      const deleteButton = screen.getByRole("button", { name: "Delete" });
      expect(deleteButton).not.toBeDisabled();
      fireEvent.click(deleteButton);
    });

    await waitFor(() => {
      expect(onCloseMock).toHaveBeenCalled();
    });
  });
});
