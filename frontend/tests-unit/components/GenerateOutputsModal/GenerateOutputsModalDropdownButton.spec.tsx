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

import GenerateOutputsModalDropdownButton from "@/components/GenerateOutputsModal/GenerateOutputsModalDropdownButton";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

const renderComponent = (onModelRemove = jest.fn()) =>
  testRender(
    <GenerateOutputsModalDropdownButton onModelRemove={onModelRemove} />,
  );

describe("GenerateOutputsModalDropdownButton", () => {
  it("renders the dropdown button", () => {
    renderComponent();

    expect(screen.getByRole("button")).toBeInTheDocument();
    expect(screen.getByText("more_vert")).toBeInTheDocument();
  });

  it("opens the dropdown when clicked", () => {
    renderComponent();

    fireEvent.click(screen.getByRole("button"));

    expect(screen.getByText("Remove model")).toBeInTheDocument();
  });

  it("calls onModelRemove when clicking 'Remove model'", () => {
    const mockRemove = jest.fn();
    renderComponent(mockRemove);

    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(screen.getByText("Remove model"));

    expect(mockRemove).toHaveBeenCalled();
  });
});
