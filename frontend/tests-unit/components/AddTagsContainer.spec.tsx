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

import AddTagsContainer from "@/components/AddTagsContainer";
import { useTagsContext } from "@/hooks/useTagsContext";
import {
  addAllTagsLinksQuery,
  createNewTagQuery,
  createTagLinksQuery,
  deleteTagQuery,
  getTagLinksQuery,
} from "@/queries/clientQueries";
import { TagLinkEntityType, TagRaw } from "@/types";
import { notifications } from "@mantine/notifications";
import { fireEvent, screen, waitFor } from "@testing-library/react";

import { testRender } from "../render";

jest.mock("@/hooks/useTagsContext", () => ({
  useTagsContext: jest.fn(),
}));

jest.mock("@/queries/clientQueries", () => ({
  createNewTagQuery: jest.fn(),
  createTagLinksQuery: jest.fn(),
  deleteTagQuery: jest.fn(),
  getTagLinksQuery: jest.fn(),
  addAllTagsLinksQuery: jest.fn(),
}));

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

const mockTag: TagRaw = {
  id: "1",
  name: "TagOne",
  color: "",
  created_at: "",
  updated_at: "",
};

const mockTag2: TagRaw = {
  id: "2",
  name: "TagTwo",
  color: "",
  created_at: "",
  updated_at: "",
};

describe("AddTagsContainer", () => {
  const mockRefreshTags = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (useTagsContext as jest.Mock).mockReturnValue({
      userTags: [mockTag, mockTag2],
      refreshTags: mockRefreshTags,
      isLoadingTags: false,
    });

    (createNewTagQuery as jest.Mock).mockResolvedValue({
      id: "new-tag",
      name: "NewTag",
    });

    (getTagLinksQuery as jest.Mock).mockResolvedValue([
      {
        tag_id: "1",
        target_entity: TagLinkEntityType.CHAT,
        target_entity_id: "entity1",
      },
    ]);

    (createTagLinksQuery as jest.Mock).mockImplementation(() => {
      return Promise.resolve({ success: true });
    });

    (deleteTagQuery as jest.Mock).mockImplementation(() => {
      return Promise.resolve({ success: true });
    });
  });

  it("renders tag input and 'Add' button", () => {
    testRender(
      <AddTagsContainer
        setAddedTags={jest.fn()}
        showMyTags={false}
        showSelectedTags={false}
      />,
    );

    expect(
      screen.getByPlaceholderText("Add additional or select from below"),
    ).toBeInTheDocument();
    expect(screen.getByTestId("add-tag-button-disabled")).toBeInTheDocument();
  });

  it("adds a new tag when 'Add' is clicked", async () => {
    const setAddedTagsMock = jest.fn();

    testRender(
      <AddTagsContainer
        setAddedTags={setAddedTagsMock}
        entityIds={["entity1"]}
        entityType={TagLinkEntityType.CHAT}
      />,
    );

    const input = screen.getByPlaceholderText(/Add additional/i);
    fireEvent.change(input, { target: { value: "NewTag" } });

    await waitFor(() => {
      expect(input).toHaveValue("NewTag");
    });

    const addButton = await screen.findByTestId("add-tag-button-enabled");
    expect(addButton).not.toBeDisabled();

    fireEvent.click(addButton);

    await waitFor(() => {
      expect(createNewTagQuery).toHaveBeenCalledWith(
        {
          name: "NewTag",
          color: "",
        },
        expect.anything(),
      );
    });
  });

  it("shows user's tags and allows selecting them", async () => {
    const setAddedTagsMock = jest.fn();

    testRender(
      <AddTagsContainer
        setAddedTags={setAddedTagsMock}
        showMyTags={true}
        entityIds={["entity1"]}
        entityType={TagLinkEntityType.CHAT_TURN}
      />,
    );

    expect(screen.getByText("TagOne")).toBeInTheDocument();

    (createTagLinksQuery as jest.Mock).mockImplementation(() =>
      Promise.resolve({ success: true }),
    );

    const tagElement = screen.getByText("TagOne");
    fireEvent.click(tagElement);

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalledWith(
        expect.objectContaining({
          message: expect.stringContaining("Tag successfully added"),
        }),
      );
    });
  });

  it("gets tag links on component mount when entityIds are provided", async () => {
    testRender(
      <AddTagsContainer
        setAddedTags={jest.fn()}
        entityIds={["entity1"]}
        entityType={TagLinkEntityType.CHAT}
      />,
    );

    await waitFor(() => {
      expect(getTagLinksQuery).toHaveBeenCalled();
    });
  });

  it("adds multiple tags separated by commas", async () => {
    const setAddedTagsMock = jest.fn();
    (createNewTagQuery as jest.Mock)
      .mockResolvedValueOnce({
        id: "new-tag-1",
        name: "Tag1",
      })
      .mockResolvedValueOnce({
        id: "new-tag-2",
        name: "Tag2",
      });

    testRender(
      <AddTagsContainer
        setAddedTags={setAddedTagsMock}
        entityIds={["entity1"]}
        entityType={TagLinkEntityType.CHAT}
      />,
    );

    const input = screen.getByPlaceholderText(/Add additional/i);
    fireEvent.change(input, { target: { value: "Tag1, Tag2" } });

    await waitFor(() => {
      expect(input).toHaveValue("Tag1, Tag2");
    });

    const addButton = screen.getByTestId("add-tag-button-enabled");
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(createNewTagQuery).toHaveBeenCalledTimes(2);
      expect(createNewTagQuery).toHaveBeenCalledWith(
        { name: "Tag1", color: "" },
        expect.anything(),
      );
      expect(createNewTagQuery).toHaveBeenCalledWith(
        { name: "Tag2", color: "" },
        expect.anything(),
      );
    });
  });

  it("shows notification when trying to add existing tags", async () => {
    testRender(
      <AddTagsContainer
        setAddedTags={jest.fn()}
        entityIds={["entity1"]}
        entityType={TagLinkEntityType.CHAT}
      />,
    );

    const input = screen.getByPlaceholderText(/Add additional/i);
    fireEvent.change(input, { target: { value: "TagOne" } });

    const addButton = screen.getByTestId("add-tag-button-enabled");
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalledWith(
        expect.objectContaining({
          message: expect.anything(),
        }),
      );
    });
  });

  it("adds tags to all rows in a project when selectAllRowsInProject is true", async () => {
    const setAddedTagsMock = jest.fn();
    const clearBannerStateMock = jest.fn();
    (addAllTagsLinksQuery as jest.Mock).mockResolvedValue({ success: true });
    (createNewTagQuery as jest.Mock).mockResolvedValue({
      id: "new-project-tag",
      name: "ProjectTag",
    });

    testRender(
      <AddTagsContainer
        setAddedTags={setAddedTagsMock}
        selectAllRowsInProject={true}
        projectId="project-1"
        onClearBannerState={clearBannerStateMock}
      />,
    );

    const input = screen.getByPlaceholderText(/Add additional/i);
    fireEvent.change(input, { target: { value: "ProjectTag" } });

    const addButton = screen.getByTestId("add-tag-button-enabled");
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(createNewTagQuery).toHaveBeenCalledWith(
        { name: "ProjectTag", color: "" },
        expect.anything(),
      );
    });

    await waitFor(() => {
      expect(addAllTagsLinksQuery).toHaveBeenCalledWith("project-1", [
        "new-project-tag",
      ]);
    });

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalledWith(
        expect.objectContaining({
          message: expect.stringContaining(
            "Tag has been added to all rows in the project",
          ),
        }),
      );
      expect(clearBannerStateMock).toHaveBeenCalled();
    });
  });

  it("shows disabled add button with tooltip when input is empty", () => {
    testRender(<AddTagsContainer setAddedTags={jest.fn()} />);

    const disabledButton = screen.getByTestId("add-tag-button-disabled");
    expect(disabledButton).toBeInTheDocument();
    expect(disabledButton).toBeDisabled();

    const tooltipContainer = disabledButton.closest("div");
    expect(tooltipContainer).toBeInTheDocument();
  });

  it("shows 'No tags added yet' message when no tags are available", () => {
    (useTagsContext as jest.Mock).mockReturnValue({
      userTags: [],
      refreshTags: mockRefreshTags,
      isLoadingTags: false,
    });

    testRender(<AddTagsContainer setAddedTags={jest.fn()} showMyTags={true} />);

    expect(screen.getByText("No tags added yet.")).toBeInTheDocument();
  });
});
