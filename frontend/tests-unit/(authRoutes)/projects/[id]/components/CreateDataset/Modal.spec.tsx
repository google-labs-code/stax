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

import CreateDatasetModal from "@/app/(authRoutes)/projects/[id]/components/CreateDataset/Modal";
import { ModelsProvider } from "@/hooks/useModelsContext";
import { testRender } from "@/tests-unit/render";
import { MantineProvider } from "@mantine/core";
import { fireEvent, screen } from "@testing-library/react";
import { ReactElement } from "react";

describe("CreateDatasetModal", () => {
  const mockOnClose = jest.fn();

  const renderWithProviders = (children: ReactElement) => {
    return testRender(
      <ModelsProvider>
        <MantineProvider>{children}</MantineProvider>
      </ModelsProvider>,
    );
  };

  it("should render modal when isOpened is true", () => {
    renderWithProviders(
      <CreateDatasetModal isOpened={true} onClose={mockOnClose} />,
    );

    const modal = screen.getByText("Create a dataset");
    expect(modal).toBeInTheDocument();
  });

  it("should not render modal when isOpened is false", () => {
    renderWithProviders(
      <CreateDatasetModal isOpened={false} onClose={mockOnClose} />,
    );

    const modal = screen.queryByText("Create a dataset");
    expect(modal).not.toBeInTheDocument();
  });

  it("should call onClose when modal is closed", () => {
    renderWithProviders(
      <CreateDatasetModal isOpened={true} onClose={mockOnClose} />,
    );

    const closeButton = screen.getByLabelText("close button");
    fireEvent.click(closeButton);

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });
});
