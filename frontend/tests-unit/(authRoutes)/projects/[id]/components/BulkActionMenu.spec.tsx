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

import BulkActionMenu from "@/components/BulkActionMenu";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("BulkActionMenu", () => {
  const onAddTags = jest.fn();
  const onRemoveTags = jest.fn();
  const baseProps = {
    isDisabled: false,
    menuItems: [
      {
        label: "Add tags",
        icon: "add_tag",
        onClick: onAddTags,
      },
      {
        label: "Remove tags",
        icon: "remove_tag",
        onClick: onRemoveTags,
      },
    ],
  };

  it("renders as disabled when isDisabled is true", () => {
    // Disabled state: should render with opacity and not open menu
    testRender(<BulkActionMenu {...baseProps} isDisabled={true} />);
    // Should render the icon, but not as a button
    const icon = screen.getByTestId("material-icon");
    expect(icon).toBeInTheDocument();
    // Should not render menu items
    expect(screen.queryByTestId("menu-item")).not.toBeInTheDocument();
  });

  it("renders and opens menu when enabled", async () => {
    testRender(<BulkActionMenu {...baseProps} isDisabled={false} />);
    // Find the menu trigger by icon (role img) or by label
    const trigger = screen.getByTestId("material-icon");
    expect(trigger).toBeInTheDocument();
    // Open the menu (click the parent div)
    await userEvent.click(trigger.parentElement!);
    // Menu items should be rendered
    const menuItems = screen.getAllByTestId("menu-item");
    const menuTexts = menuItems.map((item) => item.textContent);
    expect(menuTexts).toEqual(
      expect.arrayContaining([
        expect.stringContaining("Add tags"),
        expect.stringContaining("Remove tags"),
      ]),
    );
  });

  it("calls the correct callback when a menu item is clicked", async () => {
    testRender(<BulkActionMenu {...baseProps} isDisabled={false} />);
    // Open the menu
    const trigger = screen.getByTestId("material-icon");
    await userEvent.click(trigger.parentElement!);

    await userEvent.click(screen.getByText("Add tags"));
    expect(onAddTags).toHaveBeenCalled();

    await userEvent.click(trigger.parentElement!);
    await userEvent.click(screen.getByText("Remove tags"));
    expect(onRemoveTags).toHaveBeenCalled();
  });
});
