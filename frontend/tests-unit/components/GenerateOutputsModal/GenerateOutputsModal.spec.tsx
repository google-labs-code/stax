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

import GenerateOutputsModal from "@/components/GenerateOutputsModal/GenerateOutputsModal";
import { ProjectType } from "@/types";
import { render, screen } from "@testing-library/react";

// Mock useProjectContext to return different values based on projectType prop
const mockUseProjectContext = jest.fn();
jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: () => mockUseProjectContext(),
}));

jest.mock(
  "@/components/GenerateOutputsModal/GenerateOutputsModalPointwise",
  () => {
    const PointwiseModalMock = () => <div data-testid="pointwise-modal" />;
    PointwiseModalMock.displayName = "GenerateOutputsModalPointwise";

    return PointwiseModalMock;
  },
);

jest.mock(
  "@/components/GenerateOutputsModal/GenerateOutputsModalSideBySide",
  () => {
    const SideBySideModalMock = () => <div data-testid="side-by-side-modal" />;
    SideBySideModalMock.displayName = "GenerateOutputsModalSideBySide";

    return SideBySideModalMock;
  },
);

describe("GenerateOutputsModal", () => {
  const baseProps = {
    isOpen: true,
    onClose: jest.fn(),
    projectId: "123",
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders Pointwise modal when projectType is POINTWISE", () => {
    mockUseProjectContext.mockReturnValue({
      projectType: ProjectType.POINTWISE,
      isSideBySide: false,
    });

    render(<GenerateOutputsModal {...baseProps} />);
    expect(screen.getByTestId("pointwise-modal")).toBeInTheDocument();
  });

  it("renders SideBySide modal when projectType is SIDE_BY_SIDE", () => {
    mockUseProjectContext.mockReturnValue({
      projectType: ProjectType.SIDE_BY_SIDE,
      isSideBySide: true,
    });

    render(<GenerateOutputsModal {...baseProps} />);
    expect(screen.getByTestId("side-by-side-modal")).toBeInTheDocument();
  });
});
