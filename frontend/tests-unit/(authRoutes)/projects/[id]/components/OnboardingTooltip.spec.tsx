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

import OnboardingTooltip from "@/app/(authRoutes)/projects/[id]/components/OnboardingTooltip";
import { testRender } from "@/tests-unit/render";
import LocalStorage from "@/utils/LocalStorage";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/utils/LocalStorage", () => ({
  get: jest.fn(),
  set: jest.fn(),
}));

const defaultProps = {
  opened: true,
  onNext: jest.fn(),
  onPrevious: jest.fn(),
  onClose: jest.fn(),
  index: 0,
  content: [
    { title: "Step 1", body: "This is the first step." },
    { title: "Step 2", body: "This is the second step." },
  ],
  localStorageItemName: "ONBOARDING_SEEN",
  children: <div>Trigger Element</div>,
};

describe("OnboardingTooltip", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders tooltip when not seen before", () => {
    (LocalStorage.get as jest.Mock).mockReturnValue(null);

    testRender(<OnboardingTooltip {...defaultProps} />);

    expect(screen.getByText("Step 1")).toBeInTheDocument();
    expect(screen.getByText("This is the first step.")).toBeInTheDocument();
    expect(screen.getByText("1 of 2")).toBeInTheDocument();
    expect(LocalStorage.set).toHaveBeenCalledWith(
      defaultProps.localStorageItemName,
      "true",
    );
  });

  it("does not render tooltip if forceHide is true", () => {
    (LocalStorage.get as jest.Mock).mockReturnValue(null);

    testRender(<OnboardingTooltip {...defaultProps} forceHide={true} />);

    expect(screen.queryByText("Step 1")).not.toBeInTheDocument();
    expect(screen.getByText("Trigger Element")).toBeInTheDocument();
  });

  it("hides tooltip if user has already seen it", () => {
    (LocalStorage.get as jest.Mock).mockReturnValue("true");

    testRender(<OnboardingTooltip {...defaultProps} />);

    expect(screen.queryByText("Step 1")).not.toBeInTheDocument();
  });

  it("calls onNext when Next button is clicked", () => {
    (LocalStorage.get as jest.Mock).mockReturnValue(null);

    testRender(<OnboardingTooltip {...defaultProps} />);

    const nextButton = screen.getByRole("button", { name: /next/i });
    fireEvent.click(nextButton);
    expect(defaultProps.onNext).toHaveBeenCalled();
  });

  it("calls onPrevious when Back button is clicked", () => {
    (LocalStorage.get as jest.Mock).mockReturnValue(null);

    testRender(
      <OnboardingTooltip
        {...defaultProps}
        index={1}
        onPrevious={defaultProps.onPrevious}
      />,
    );

    const backButton = screen.getByRole("button", { name: /back/i });
    fireEvent.click(backButton);
    expect(defaultProps.onPrevious).toHaveBeenCalled();
  });

  it('shows "Get Started" on last step', () => {
    (LocalStorage.get as jest.Mock).mockReturnValue(null);

    testRender(<OnboardingTooltip {...defaultProps} index={1} />);

    expect(screen.getByText("Get Started")).toBeInTheDocument();
  });
});
