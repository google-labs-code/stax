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

import GetStartedModal from "@/app/(authRoutes)/projects/[id]/components/GetStartedModal";
import { testRender } from "@/tests-unit/render";
import { ProjectType } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

// Mock useProjectContext to return different values based on test needs
const mockUseProjectContext = jest.fn();
jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: () => mockUseProjectContext(),
}));

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
  }),
}));

jest.mock("@/components/MaterialIcon", () => {
  const MaterialIcon = ({ name }: any) => (
    <span data-testid="material-icon">{name}</span>
  );
  MaterialIcon.displayName = "MockMaterialIcon";

  return MaterialIcon;
});

describe("GetStartedModal", () => {
  const mockClose = jest.fn();
  const mockOpenAddDatasetModal = jest.fn();
  const mockRedirectToPlayground = jest.fn();

  const defaultProps = {
    isOpened: true,
    onClose: mockClose,
    projectId: "abc123",
    projectType: ProjectType.POINTWISE,
    openAddDatasetModal: mockOpenAddDatasetModal,
    redirectToPlayground: mockRedirectToPlayground,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    // Default mock for most tests
    mockUseProjectContext.mockReturnValue({
      projectType: ProjectType.POINTWISE,
      isSideBySide: false,
    });
  });

  it("renders the modal with all key content", () => {
    testRender(<GetStartedModal {...defaultProps} />);

    expect(
      screen.getByText("Get started evaluating your AI"),
    ).toBeInTheDocument();
    expect(
      screen.getByText(
        "Great AI products aren't built on luck. They're built on proof.",
      ),
    ).toBeInTheDocument();
    expect(screen.getByText("Test on your data:")).toBeInTheDocument();
    expect(screen.getByText("Import dataset")).toBeInTheDocument();
    expect(screen.getByText("Open prompt playground")).toBeInTheDocument();
  });

  it("renders Pointwise image if projectType is POINTWISE", () => {
    testRender(<GetStartedModal {...defaultProps} />);
    const img = screen.getByAltText("workbook table");
    expect(img).toHaveAttribute("src", "/Pointwise.png");
  });

  it("renders SxS image if projectType is SIDE_BY_SIDE", () => {
    mockUseProjectContext.mockReturnValue({
      projectType: ProjectType.SIDE_BY_SIDE,
      isSideBySide: true,
    });

    testRender(<GetStartedModal {...defaultProps} />);
    const img = screen.getByAltText("workbook table");
    expect(img).toHaveAttribute("src", "/SxS.png");
  });

  it("calls redirectToPlayground and router.push on playground button click", () => {
    testRender(<GetStartedModal {...defaultProps} />);

    fireEvent.click(screen.getByText("Open prompt playground"));

    expect(mockRedirectToPlayground).toHaveBeenCalled();
    expect(mockClose).not.toHaveBeenCalled();
  });

  it("calls openAddDatasetModal and onClose on import dataset click", () => {
    testRender(<GetStartedModal {...defaultProps} />);

    fireEvent.click(screen.getByText("Import dataset"));

    expect(mockOpenAddDatasetModal).toHaveBeenCalled();
    expect(mockClose).toHaveBeenCalled();
  });

  it("calls onClose when modal is closed", () => {
    testRender(<GetStartedModal {...defaultProps} />);

    fireEvent.click(screen.getByLabelText("close button"));

    expect(mockClose).toHaveBeenCalled();
  });
});
