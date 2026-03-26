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

import { EvaluatorPageAction } from "@/app/(authRoutes)/evaluatorGallery/types";
import EvaluatorDuplicateButton from "@/components/Evaluation/EvaluatorDuplicateButton";
import { routes } from "@/config/routes";
import { testRenderLite } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";
import { useRouter } from "next/navigation";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

describe("EvaluatorDuplicateButton", () => {
  const mockPush = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });
  });

  it("renders with default props", () => {
    testRenderLite(<EvaluatorDuplicateButton id="123" />);

    const button = screen.getByRole("button", { name: /duplicate/i });
    expect(button).toBeInTheDocument();
    expect(button).toHaveTextContent("Duplicate");
  });

  it("calls router.push with correct URL when clicked and no custom onClick is provided", () => {
    testRenderLite(<EvaluatorDuplicateButton id="123" />);

    const button = screen.getByRole("button", { name: /duplicate/i });

    fireEvent.click(button);

    expect(mockPush).toHaveBeenCalledWith(
      `${routes.evaluatorGallery.root}/123/${EvaluatorPageAction.DUPLICATE}`,
    );
  });

  it("calls custom onClick handler if provided", () => {
    const onClickMock = jest.fn();
    testRenderLite(<EvaluatorDuplicateButton id="123" onClick={onClickMock} />);

    const button = screen.getByRole("button", { name: /duplicate/i });

    fireEvent.click(button);

    expect(onClickMock).toHaveBeenCalledTimes(1);
    expect(mockPush).not.toHaveBeenCalled();
  });

  it("applies the passed className", () => {
    testRenderLite(
      <EvaluatorDuplicateButton id="123" className="custom-class-name" />,
    );

    const button = screen.getByRole("button", { name: /duplicate/i });

    expect(button.className).toMatch(/custom-class-name/);
  });

  it("renders the correct icon size based on button size", () => {
    testRenderLite(<EvaluatorDuplicateButton id="123" size="sm" />);

    const icon = screen.getByTestId("material-icon");
    expect(icon).toBeInTheDocument();
  });
});
