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

import TabsActionMenu from "@/components/Evaluation/TabsActionMenu";
import { EvaluatorTab } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../../render";

// Mock next/navigation useRouter
const pushMock = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: pushMock }),
}));

describe("TabsActionMenu", () => {
  const dataId = "test-id";
  const openDeleteModal = jest.fn();

  beforeEach(() => {
    pushMock.mockClear();
    openDeleteModal.mockClear();
  });

  it("renders only Duplicate for DEFAULT tab and triggers router.push on click", () => {
    testRender(
      <TabsActionMenu
        tab={EvaluatorTab.DEFAULT}
        dataId={dataId}
        openDeleteModal={openDeleteModal}
      />,
    );
    // Open the menu
    fireEvent.click(screen.getByTestId("material-icon"));
    // Click Duplicate
    fireEvent.click(
      screen.getAllByText(
        (content, element) => element?.textContent === "Duplicate",
      )[0],
    );
    expect(pushMock).toHaveBeenCalledWith(
      "/evaluatorGallery/" + dataId + "/duplicate",
    );
    expect(
      screen.queryByText((content, element) => element?.textContent === "Edit"),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText(
        (content, element) => element?.textContent === "Delete",
      ),
    ).not.toBeInTheDocument();
  });

  it("does not throw if openDeleteModal is not provided", () => {
    testRender(
      <TabsActionMenu tab={EvaluatorTab.MY_EVALUATORS} dataId={dataId} />,
    );
    fireEvent.click(screen.getByTestId("material-icon"));
    fireEvent.click(
      screen.getAllByText(
        (content, element) => element?.textContent === "Delete",
      )[0],
    );
    // Should not throw, nothing to assert
  });
});
