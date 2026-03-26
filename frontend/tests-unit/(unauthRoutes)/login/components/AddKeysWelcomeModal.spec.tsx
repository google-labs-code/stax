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

import { InitialKeys } from "@/app/(authRoutes)/settings/config/initialKeys";
import AddKeysWelcomeModal from "@/app/(unauthRoutes)/login/components/AddKeysWelcomeModal";
import { testRenderLite } from "@/tests-unit/render";
import { fireEvent, screen, waitFor } from "@testing-library/react";

const mutateMock = jest.fn();
jest.mock("@tanstack/react-query", () => ({
  useMutation: jest.fn(() => ({
    mutate: mutateMock,
    isPending: false,
  })),
}));

describe("AddKeysWelcomeModal", () => {
  it("renders modal when isOpened is true", () => {
    const onProceed = jest.fn();
    testRenderLite(
      <AddKeysWelcomeModal isOpened={true} onProceed={onProceed} />,
    );
    expect(screen.getByTestId("welcome-modal")).toBeInTheDocument();
  });

  it('calls "onProceed" when clicking "Skip"', () => {
    const onProceed = jest.fn();
    testRenderLite(
      <AddKeysWelcomeModal isOpened={true} onProceed={onProceed} />,
    );
    fireEvent.click(screen.getByText("Skip"));
    expect(onProceed).toHaveBeenCalled();
  });

  it("updates input field value", () => {
    const onProceed = jest.fn();
    testRenderLite(
      <AddKeysWelcomeModal isOpened={true} onProceed={onProceed} />,
    );
    const input = screen.getByPlaceholderText("Enter your API key here");
    fireEvent.change(input, { target: { value: "abc123" } });
    expect((input as HTMLInputElement).value).toBe("abc123");
  });

  it("calls mutate function on clicking Add Key", async () => {
    const onProceed = jest.fn();
    testRenderLite(
      <AddKeysWelcomeModal isOpened={true} onProceed={onProceed} />,
    );
    const input = screen.getByPlaceholderText("Enter your API key here");
    fireEvent.change(input, { target: { value: "abc123" } });
    const button = screen.getByRole("button", { name: "Add Key" });
    fireEvent.click(button);

    await waitFor(() => {
      expect(mutateMock).toHaveBeenCalledWith({
        provider: InitialKeys[0].provider,
        key: "abc123",
      });
    });
  });

  it('"Add Key" button is disabled if input is empty', () => {
    const onProceed = jest.fn();
    testRenderLite(
      <AddKeysWelcomeModal isOpened={true} onProceed={onProceed} />,
    );
    const button = screen.getByRole("button", { name: "Add Key" });
    expect(button).toBeDisabled();
  });
});
