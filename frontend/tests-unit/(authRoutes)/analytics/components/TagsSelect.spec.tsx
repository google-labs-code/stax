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

import TagsSelect from "@/app/(authRoutes)/analytics/components/TagsSelect";
import * as TagsContextModule from "@/hooks/useTagsContext";
import { MantineProvider } from "@mantine/core";
import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";

jest.mock("@/hooks/useTagsContext");

const mockedUseTagsContext = TagsContextModule.useTagsContext as jest.Mock;

function renderWithMantine(children: React.ReactNode) {
  return render(<MantineProvider>{children}</MantineProvider>);
}

describe("TagsSelect", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders loader when tags are loading", () => {
    mockedUseTagsContext.mockReturnValue({
      userTags: [],
      isLoadingTags: true,
    });

    const setSelectedTags = jest.fn();

    renderWithMantine(
      <TagsSelect selectedTags={[]} setSelectedTags={setSelectedTags} />,
    );

    const loader = document.querySelector(".mantine-Loader-root");
    expect(loader).toBeInTheDocument();
  });

  it("renders no tags when none are available", () => {
    mockedUseTagsContext.mockReturnValue({
      userTags: [],
      isLoadingTags: false,
    });

    const setSelectedTags = jest.fn();

    renderWithMantine(
      <TagsSelect selectedTags={[]} setSelectedTags={setSelectedTags} />,
    );

    expect(screen.getByText("No tags available.")).toBeInTheDocument();
  });

  it("renders pills for selected tags", () => {
    mockedUseTagsContext.mockReturnValue({
      userTags: [
        { id: "tag1", name: "First" },
        { id: "tag2", name: "Second" },
      ],
      isLoadingTags: false,
    });

    const setSelectedTags = jest.fn();

    renderWithMantine(
      <TagsSelect selectedTags={["tag2"]} setSelectedTags={setSelectedTags} />,
    );

    expect(screen.getAllByText("Second")).toHaveLength(2);
  });

  it("adds tag when selected from dropdown", () => {
    mockedUseTagsContext.mockReturnValue({
      userTags: [{ id: "tag1", name: "First" }],
      isLoadingTags: false,
    });

    const setSelectedTags = jest.fn();

    renderWithMantine(
      <TagsSelect selectedTags={[]} setSelectedTags={setSelectedTags} />,
    );

    const input = screen.getByPlaceholderText("Add tags");
    fireEvent.focus(input);
    fireEvent.change(input, { target: { value: "F" } });

    const option = screen.getByText("First");
    fireEvent.click(option);

    expect(setSelectedTags).toHaveBeenCalledWith(["tag1"]);
  });

  it("removes tag pill when remove button is clicked", () => {
    mockedUseTagsContext.mockReturnValue({
      userTags: [{ id: "tag1", name: "First" }],
      isLoadingTags: false,
    });

    const removeTag = jest.fn();

    renderWithMantine(
      <TagsSelect selectedTags={["tag1"]} setSelectedTags={removeTag} />,
    );

    const pill = screen.getAllByText("First");
    const removeButton = pill[0].nextElementSibling;
    if (removeButton) {
      fireEvent.click(removeButton);
    }

    expect(removeTag).toHaveBeenCalledWith([]);
  });

  it("removes last tag when pressing Backspace on empty search", () => {
    mockedUseTagsContext.mockReturnValue({
      userTags: [{ id: "tag1", name: "First" }],
      isLoadingTags: false,
    });

    const setSelectedTags = jest.fn();

    renderWithMantine(
      <TagsSelect selectedTags={["tag1"]} setSelectedTags={setSelectedTags} />,
    );

    const input = screen.getByPlaceholderText("Add tags");
    fireEvent.focus(input);
    fireEvent.keyDown(input, { key: "Backspace", code: "Backspace" });

    expect(setSelectedTags).toHaveBeenCalledWith([]);
  });

  it("adds a tag to selectedTags when it is selected from the dropdown", () => {
    const setSelectedTags = jest.fn();
    const mockedUseTagsContext = TagsContextModule.useTagsContext as jest.Mock;

    mockedUseTagsContext.mockReturnValue({
      userTags: [
        { id: "tag1", name: "First" },
        { id: "tag2", name: "Second" },
      ],
      isLoadingTags: false,
    });

    renderWithMantine(
      <TagsSelect selectedTags={["tag1"]} setSelectedTags={setSelectedTags} />,
    );
    const input = screen.getByPlaceholderText("Add tags");
    fireEvent.focus(input);

    const option = screen.getByText("Second");
    fireEvent.click(option);

    expect(setSelectedTags).toHaveBeenCalledWith(["tag1", "tag2"]);
  });
});
