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

import RemoveTagsContainer from "@/components/RemoveTagsContainer";
import { useTagsContext } from "@/hooks/useTagsContext";
import * as clientQueries from "@/queries/clientQueries";
import { TagLinkEntityType } from "@/types";
import { notifications } from "@mantine/notifications";
import { fireEvent, screen, waitFor } from "@testing-library/react";

import { testRender } from "../render";

jest.mock("@/hooks/useTagsContext");

jest.mock("@/queries/clientQueries", () => ({
  getTagLinksQuery: jest.fn(),
  deleteTagLinksQuery: jest.fn(),
  deleteAllTagsLinksQuery: jest.fn(),
}));

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

const mockUseTagsContext = useTagsContext as jest.Mock;
const mockShow = notifications.show as jest.Mock;

const renderComponent = (props = {}) => {
  const defaultProps = {
    entityType: TagLinkEntityType.CHAT,
    entityIds: ["123", "456"],
    setRemovedTagIds: jest.fn(),
    projectId: "project-1",
    selectAllRowsInProject: false,
    ...props,
  };

  return testRender(<RemoveTagsContainer {...defaultProps} />);
};

describe("RemoveTagsContainer", () => {
  beforeEach(() => {
    jest.clearAllMocks();

    mockUseTagsContext.mockReturnValue({
      userTags: [{ id: "tag-2", name: "Important" }],
    });

    (clientQueries.getTagLinksQuery as jest.Mock).mockResolvedValue([
      {
        id: "link-2",
        tag_id: "tag-2",
        target_entity: "CHAT",
        target_entity_id: "456",
      },
    ]);
  });

  it("renders tags retrieved from API", async () => {
    renderComponent();

    await waitFor(() => {
      expect(clientQueries.getTagLinksQuery).toHaveBeenCalled();
      expect(screen.getByText("Important")).toBeInTheDocument();
    });
  });

  it("calls deleteTagLinksQuery when removing a tag", async () => {
    const setRemovedTagIds = jest.fn();
    (clientQueries.deleteTagLinksQuery as jest.Mock).mockResolvedValue({});

    renderComponent({ setRemovedTagIds });

    await waitFor(() => {
      expect(screen.getByText("Important")).toBeInTheDocument();
    });

    const closeIcons = screen.getAllByTestId("material-icon");
    const tagCloseIcon = Array.from(closeIcons).find(
      (icon) =>
        icon.textContent === "close" &&
        icon.closest("div")?.textContent?.includes("Important"),
    );

    expect(tagCloseIcon).toBeTruthy();
    fireEvent.click(tagCloseIcon!);

    await waitFor(() => {
      expect(clientQueries.deleteTagLinksQuery).toHaveBeenCalledWith(
        {
          tag_ids: ["tag-2"],
          entity_type: TagLinkEntityType.CHAT,
          entity_ids: ["123", "456"],
        },
        expect.anything(),
      );

      expect(setRemovedTagIds).toHaveBeenCalled();

      const firstCallArg = setRemovedTagIds.mock.calls[0][0];

      if (typeof firstCallArg === "function") {
        const result = firstCallArg([]);
        expect(result).toContain("tag-2");
      }
    });
  });

  it("calls deleteAllTagsLinksQuery when removing a tag with selectAllRowsInProject", async () => {
    const setRemovedTagIds = jest.fn();
    const onClearBannerState = jest.fn();
    (clientQueries.deleteAllTagsLinksQuery as jest.Mock).mockResolvedValue({});

    renderComponent({
      setRemovedTagIds,
      selectAllRowsInProject: true,
      onClearBannerState,
    });

    await waitFor(() => {
      expect(screen.getByText("Important")).toBeInTheDocument();
    });

    const closeIcons = screen.getAllByTestId("material-icon");
    const tagCloseIcon = Array.from(closeIcons).find(
      (icon) =>
        icon.textContent === "close" &&
        icon.closest("div")?.textContent?.includes("Important"),
    );

    expect(tagCloseIcon).toBeTruthy();
    fireEvent.click(tagCloseIcon!);

    await waitFor(() => {
      expect(clientQueries.deleteAllTagsLinksQuery).toHaveBeenCalledWith(
        "project-1",
        ["tag-2"],
      );

      expect(setRemovedTagIds).toHaveBeenCalled();

      const firstCallArg = setRemovedTagIds.mock.calls[0][0];
      if (typeof firstCallArg === "function") {
        const result = firstCallArg([]);
        expect(result).toContain("tag-2");
      }

      expect(onClearBannerState).toHaveBeenCalled();
    });
  });

  it("shows error notification when deleteTagLinksQuery fails", async () => {
    (clientQueries.deleteTagLinksQuery as jest.Mock).mockRejectedValue(
      new Error("API error"),
    );

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText("Important")).toBeInTheDocument();
    });

    // Find the close icon near the Important tag
    const closeIcons = screen.getAllByTestId("material-icon");
    const tagCloseIcon = Array.from(closeIcons).find(
      (icon) =>
        icon.textContent === "close" &&
        icon.closest("div")?.textContent?.includes("Important"),
    );

    expect(tagCloseIcon).toBeTruthy();
    fireEvent.click(tagCloseIcon!);

    await waitFor(() => {
      expect(mockShow).toHaveBeenCalledWith(
        expect.objectContaining({
          id: "form-error",
        }),
      );

      // Get the message prop from the notification call
      const notificationCall = mockShow.mock.calls[0][0];

      // Check if the message contains our expected text in the dangerouslySetInnerHTML
      if (
        notificationCall.message &&
        notificationCall.message.props &&
        notificationCall.message.props.dangerouslySetInnerHTML
      ) {
        const htmlContent =
          notificationCall.message.props.dangerouslySetInnerHTML.__html;
        expect(htmlContent).toContain("Failed to remove tag");
      }
    });
  });

  it("shows error notification when deleteAllTagsLinksQuery fails", async () => {
    (clientQueries.deleteAllTagsLinksQuery as jest.Mock).mockRejectedValue(
      new Error("API error"),
    );

    renderComponent({ selectAllRowsInProject: true });

    await waitFor(() => {
      expect(screen.getByText("Important")).toBeInTheDocument();
    });

    const closeIcons = screen.getAllByTestId("material-icon");
    const tagCloseIcon = Array.from(closeIcons).find(
      (icon) =>
        icon.textContent === "close" &&
        icon.closest("div")?.textContent?.includes("Important"),
    );

    expect(tagCloseIcon).toBeTruthy();
    fireEvent.click(tagCloseIcon!);

    await waitFor(() => {
      expect(mockShow).toHaveBeenCalledTimes(2);

      const notificationCall = mockShow.mock.calls[1][0];

      expect(notificationCall.id).toBe("form-error");

      if (
        notificationCall.message &&
        notificationCall.message.props &&
        notificationCall.message.props.dangerouslySetInnerHTML
      ) {
        const htmlContent =
          notificationCall.message.props.dangerouslySetInnerHTML.__html;
        expect(htmlContent).toContain("Failed to remove tag from all rows");
      }
    });
  });

  it("displays a loader while fetching tags", () => {
    (clientQueries.getTagLinksQuery as jest.Mock).mockReturnValue(
      new Promise(() => {}),
    );

    renderComponent();

    const loader = screen.getByText("", {
      selector: ".mantine-Loader-root",
    });
    expect(loader).toBeInTheDocument();
  });

  it("displays a loader while removing tags", async () => {
    (clientQueries.getTagLinksQuery as jest.Mock).mockResolvedValue([
      {
        id: "link-2",
        tag_id: "tag-2",
        target_entity: "CHAT",
        target_entity_id: "456",
      },
    ]);

    (clientQueries.deleteTagLinksQuery as jest.Mock).mockImplementation(() => {
      return new Promise(() => {});
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText("Important")).toBeInTheDocument();
    });

    const closeIcons = screen.getAllByTestId("material-icon");
    const tagCloseIcon = Array.from(closeIcons).find(
      (icon) =>
        icon.textContent === "close" &&
        icon.closest("div")?.textContent?.includes("Important"),
    );

    expect(tagCloseIcon).toBeTruthy();
    fireEvent.click(tagCloseIcon!);

    await waitFor(() => {
      const loader = screen.getByText("", {
        selector: ".mantine-Loader-root",
      });
      expect(loader).toBeInTheDocument();
    });
  });
});
