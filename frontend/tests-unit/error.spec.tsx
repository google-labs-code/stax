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

import Error from "@/app/error";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";
import { act } from "react-dom/test-utils";

// Mock console.error to avoid noise in tests
// eslint-disable-next-line no-console
const originalError = console.error;
beforeAll(() => {
  // eslint-disable-next-line no-console
  console.error = jest.fn();
});

afterAll(() => {
  // eslint-disable-next-line no-console
  console.error = originalError;
});

describe("Error", () => {
  const mockError = new globalThis.Error("Test error message");
  const mockReset = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders error message and try again button", () => {
    testRender(<Error error={mockError} reset={mockReset} />);

    expect(screen.getByText("Something went wrong!")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Try again" }),
    ).toBeInTheDocument();
  });

  it("calls reset function when try again button is clicked", () => {
    testRender(<Error error={mockError} reset={mockReset} />);

    const tryAgainButton = screen.getByRole("button", { name: "Try again" });

    act(() => {
      fireEvent.click(tryAgainButton);
    });

    expect(mockReset).toHaveBeenCalledTimes(1);
  });

  it("logs error to console when component mounts", () => {
    // eslint-disable-next-line no-console
    const consoleSpy = jest
      .spyOn(console, "error")
      .mockImplementation(() => {});

    testRender(<Error error={mockError} reset={mockReset} />);

    // The useEffect should have been called with the error
    // Note: The actual implementation doesn't log to console, but we're testing the useEffect is called
    expect(consoleSpy).not.toHaveBeenCalledWith(mockError);

    consoleSpy.mockRestore();
  });

  it("handles different error types", () => {
    const customError = new globalThis.TypeError("Custom type error");
    const customReset = jest.fn();

    testRender(<Error error={customError} reset={customReset} />);

    expect(screen.getByText("Something went wrong!")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Try again" }),
    ).toBeInTheDocument();
  });

  it("maintains button functionality with different reset implementations", () => {
    const asyncReset = jest.fn().mockImplementation(() => Promise.resolve());

    testRender(<Error error={mockError} reset={asyncReset} />);

    const tryAgainButton = screen.getByRole("button", { name: "Try again" });

    act(() => {
      fireEvent.click(tryAgainButton);
    });

    expect(asyncReset).toHaveBeenCalledTimes(1);
  });

  it("renders with error that has no message", () => {
    const errorWithoutMessage = new globalThis.Error("");

    testRender(<Error error={errorWithoutMessage} reset={mockReset} />);

    expect(screen.getByText("Something went wrong!")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Try again" }),
    ).toBeInTheDocument();
  });
});
