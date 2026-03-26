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

import TermsOfService from "@/app/(unauthRoutes)/login/components/TermsOfService";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

describe("TermsOfService", () => {
  const mockOnContinue = jest.fn();
  const mockOnCancel = jest.fn();
  const content = "Please read and accept the terms of service.";

  beforeEach(() => {
    testRender(
      <TermsOfService
        onContinueAction={mockOnContinue}
        onCancelAction={mockOnCancel}
        content={content}
      />,
    );
  });

  it("should render the content correctly", () => {
    expect(screen.getByText(content)).toBeInTheDocument();
  });

  it("should enable the continue button only if the consent checkbox is checked", () => {
    const continueButton = screen.getByRole("button", { name: /continue/i });
    const consentCheckbox = screen.getByTestId("accept-tos-checkbox");

    expect(continueButton).toBeDisabled();

    // Click the checkbox to check it
    fireEvent.click(consentCheckbox);
    expect(continueButton).not.toBeDisabled();

    // Click the checkbox again to uncheck it
    fireEvent.click(consentCheckbox);
    expect(continueButton).toBeDisabled();
  });

  it("should call onContinueAction when continue button is clicked and consent is given", () => {
    const continueButton = screen.getByRole("button", { name: /continue/i });
    const consentCheckbox = screen.getByTestId("accept-tos-checkbox");

    // Check the consent checkbox
    fireEvent.click(consentCheckbox);

    // Click the continue button
    fireEvent.click(continueButton);
    expect(mockOnContinue).toHaveBeenCalled();
  });

  it("should call onCancelAction when cancel button is clicked", () => {
    const cancelButton = screen.getByRole("button", { name: /cancel/i });

    // Click the cancel button
    fireEvent.click(cancelButton);
    expect(mockOnCancel).toHaveBeenCalled();
  });
});
