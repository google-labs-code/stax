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

import ManageTagsModal from "@/components/ManageTagsModal";
import { testRender } from "@/tests-unit/render";
import { ManageTagsModalType, TagLinkEntityType } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

const addTagsProps = { current: null };
const removeTagsProps = { current: null };

jest.mock("@/components/AddTagsContainer", () => ({
  __esModule: true,
  default: (props: any) => {
    addTagsProps.current = props;

    return (
      <div data-testid="add-tags-container">
        <button
          onClick={() => props.setAddedTags([{ id: "tag1", name: "Tag 1" }])}
          data-testid="add-tag-button"
        >
          Add Tag
        </button>
      </div>
    );
  },
}));

jest.mock("@/components/RemoveTagsContainer", () => ({
  __esModule: true,
  default: (props: any) => {
    removeTagsProps.current = props;

    return (
      <div data-testid="remove-tags-container">
        <button
          onClick={() => props.setRemovedTagIds(["tag1"])}
          data-testid="remove-tag-button"
        >
          Remove Tag
        </button>
      </div>
    );
  },
}));

describe("ManageTagsModal", () => {
  const defaultProps = {
    isOpened: true,
    onClose: jest.fn(),
    entityType: TagLinkEntityType.CHAT,
    entityIds: ["1"],
    type: ManageTagsModalType.ADD,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders modal when isOpened is true", () => {
    testRender(<ManageTagsModal {...defaultProps} />);
    expect(screen.getByText("Add tags")).toBeInTheDocument();
  });

  it("doesn't render modal when isOpened is false", () => {
    testRender(<ManageTagsModal {...defaultProps} isOpened={false} />);
    expect(screen.queryByText("Add tags")).not.toBeInTheDocument();
  });

  it("displays correct title for ADD type", () => {
    testRender(<ManageTagsModal {...defaultProps} />);
    expect(screen.getByText("Add tags")).toBeInTheDocument();
  });

  it("displays correct title for REMOVE type", () => {
    testRender(
      <ManageTagsModal {...defaultProps} type={ManageTagsModalType.REMOVE} />,
    );
    expect(screen.getByText("Remove tags")).toBeInTheDocument();
  });

  it("renders AddTagsContainer for ADD type", () => {
    testRender(<ManageTagsModal {...defaultProps} />);
    expect(screen.getByTestId("add-tags-container")).toBeInTheDocument();
    expect(
      screen.queryByTestId("remove-tags-container"),
    ).not.toBeInTheDocument();
  });

  it("renders RemoveTagsContainer for REMOVE type", () => {
    testRender(
      <ManageTagsModal {...defaultProps} type={ManageTagsModalType.REMOVE} />,
    );
    expect(screen.getByTestId("remove-tags-container")).toBeInTheDocument();
    expect(screen.queryByTestId("add-tags-container")).not.toBeInTheDocument();
  });

  it("calls onClose with added tags when closed", () => {
    const onClose = jest.fn();
    testRender(<ManageTagsModal {...defaultProps} onClose={onClose} />);

    fireEvent.click(screen.getByTestId("add-tag-button"));
    fireEvent.click(screen.getByLabelText("close button"));

    expect(onClose).toHaveBeenCalledWith([{ id: "tag1", name: "Tag 1" }], []);
  });

  it("calls onClose with removed tag IDs when closed", () => {
    const onClose = jest.fn();
    testRender(
      <ManageTagsModal
        {...defaultProps}
        type={ManageTagsModalType.REMOVE}
        onClose={onClose}
      />,
    );

    fireEvent.click(screen.getByTestId("remove-tag-button"));
    fireEvent.click(screen.getByLabelText("close button"));

    expect(onClose).toHaveBeenCalledWith([], ["tag1"]);
  });

  it("resets state when opened", () => {
    // Use the existing mocks to test state reset
    const onClose = jest.fn();
    const { rerender } = testRender(
      <ManageTagsModal {...defaultProps} isOpened={false} onClose={onClose} />,
    );

    rerender(
      <ManageTagsModal {...defaultProps} isOpened={true} onClose={onClose} />,
    );

    fireEvent.click(screen.getByLabelText("close button"));
    expect(onClose).toHaveBeenCalledWith([], []);
  });

  it("passes optional props to AddTagsContainer", () => {
    const onClearBannerState = jest.fn();
    addTagsProps.current = null;

    testRender(
      <ManageTagsModal
        {...defaultProps}
        selectAllRowsInProject={true}
        projectId="project1"
        onClearBannerState={onClearBannerState}
      />,
    );

    expect(addTagsProps.current).toMatchObject({
      selectAllRowsInProject: true,
      projectId: "project1",
      onClearBannerState,
    });
  });

  it("passes optional props to RemoveTagsContainer", () => {
    const onClearBannerState = jest.fn();
    removeTagsProps.current = null;

    testRender(
      <ManageTagsModal
        {...defaultProps}
        type={ManageTagsModalType.REMOVE}
        selectAllRowsInProject={true}
        projectId="project1"
        onClearBannerState={onClearBannerState}
      />,
    );

    expect(removeTagsProps.current).toMatchObject({
      selectAllRowsInProject: true,
      projectId: "project1",
      onClearBannerState,
    });
  });
});
