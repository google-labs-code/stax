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

import PromptCleaningInfoBar from "@/app/(authRoutes)/projects/[id]/playground/components/PromptCleaningInfoBar";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/components/MaterialIcon", () => {
  const MaterialIcon = ({ name, className, onClick }: any) => (
    <span
      data-testid={`material-icon-${name}`}
      className={className}
      onClick={onClick}
    >
      {name}
    </span>
  );
  
  return MaterialIcon;
});

describe("PromptCleaningInfoBar", () => {
  const mockResetPlayground = jest.fn();
  const mockSetHumanEvalReady = jest.fn();
  const mockSetHasShownCleaningBar = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (usePlaygroundContext as jest.Mock).mockReturnValue({
      resetPlayground: mockResetPlayground,
      setHumanEvalReady: mockSetHumanEvalReady,
      setHasShownCleaningBar: mockSetHasShownCleaningBar,
    });
  });

  it("renders the info text and icons correctly", () => {
    testRender(<PromptCleaningInfoBar />);

    expect(
      screen.getByText("Your prompt has been saved to your project."),
    ).toBeInTheDocument();

    const infoIcon = screen.getByTestId("material-icon-info");
    expect(infoIcon).toBeInTheDocument();
    expect(infoIcon).toHaveClass("cursor-default text-brand");

    const closeIcon = screen.getByTestId("material-icon-close");
    expect(closeIcon).toBeInTheDocument();
    expect(closeIcon).toHaveClass("text-secondaryDark cursor-pointer");

    expect(screen.getByText("Add another prompt")).toBeInTheDocument();
  });

  it("calls resetPlayground when 'Add another prompt' is clicked", () => {
    testRender(<PromptCleaningInfoBar />);

    fireEvent.click(screen.getByText("Add another prompt"));
    expect(mockResetPlayground).toHaveBeenCalledTimes(1);
  });

  it("calls setHumanEvalReady(false) and setHasShownCleaningBar(true) when close icon is clicked", () => {
    testRender(<PromptCleaningInfoBar />);

    fireEvent.click(screen.getByTestId("material-icon-close"));

    expect(mockSetHumanEvalReady).toHaveBeenCalledTimes(1);
    expect(mockSetHumanEvalReady).toHaveBeenCalledWith(false);

    expect(mockSetHasShownCleaningBar).toHaveBeenCalledTimes(1);
    expect(mockSetHasShownCleaningBar).toHaveBeenCalledWith(true);
  });
});
