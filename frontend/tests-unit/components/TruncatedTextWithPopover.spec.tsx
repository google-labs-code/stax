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

import TruncatedTextWithPopover from "@/components/TruncatedTextWithPopover";
import { testRender } from "@/tests-unit/render";
import { ReplaceVariablesValueMarkupWithValue } from "@/utils/textMarkup";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/utils/GetChatMessageWithMarkup", () => ({
  __esModule: true,
  default: ({ message }: { message: string; className?: string }) => (
    <div data-testid="mocked-markup">{message}</div>
  ),
}));

jest.mock("@/utils/textMarkup", () => ({
  ReplaceVariablesValueMarkupWithValue: jest.fn((text) => text),
}));

describe("TruncatedTextWithPopover", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders correctly with text", () => {
    testRender(<TruncatedTextWithPopover text="Sample text" />);
    expect(screen.getByText("Sample text")).toBeInTheDocument();
  });

  it("processes text through ReplaceVariablesValueMarkupWithValue", () => {
    const text = "Test {{variable}}";
    testRender(<TruncatedTextWithPopover text={text} />);

    expect(ReplaceVariablesValueMarkupWithValue).toHaveBeenCalledWith(text);
  });

  it("applies custom classes correctly", () => {
    const customClass = "custom-test-class";
    const customHeight = "max-h-[50px]";

    testRender(
      <TruncatedTextWithPopover
        text="Sample text"
        additionalClassName={customClass}
        maxHeight={customHeight}
      />,
    );

    const textElement = screen.getByText("Sample text");
    expect(textElement).toHaveClass(customClass);

    const container = screen.getByText("Sample text").closest("div");
    expect(container).toHaveClass(customHeight);
  });

  it("sets up and tears down scroll listener", () => {
    const addEventListenerSpy = jest.spyOn(window, "addEventListener");
    const removeEventListenerSpy = jest.spyOn(window, "removeEventListener");

    const { unmount } = testRender(
      <TruncatedTextWithPopover text="Sample text" />,
    );

    expect(addEventListenerSpy).toHaveBeenCalledWith(
      "scroll",
      expect.any(Function),
      true,
    );

    unmount();

    expect(removeEventListenerSpy).toHaveBeenCalledWith(
      "scroll",
      expect.any(Function),
      true,
    );

    addEventListenerSpy.mockRestore();
    removeEventListenerSpy.mockRestore();
  });

  it("uses getBoundingClientRect for positioning calculations", () => {
    const originalGetBoundingClientRect =
      Element.prototype.getBoundingClientRect;
    const mockGetBoundingClientRect = jest.fn().mockImplementation(() => ({
      top: 200,
      bottom: 230,
      left: 100,
      right: 300,
      width: 200,
      height: 30,
      x: 100,
      y: 200,
      toJSON: () => ({}),
    }));
    Element.prototype.getBoundingClientRect = mockGetBoundingClientRect;

    testRender(<TruncatedTextWithPopover text="Sample text" />);
    fireEvent.click(screen.getByText("Sample text"));

    expect(mockGetBoundingClientRect).toHaveBeenCalled();

    Element.prototype.getBoundingClientRect = originalGetBoundingClientRect;
  });

  it("calculates max height based on available space", () => {
    Object.defineProperty(window, "innerHeight", {
      value: 1000,
      configurable: true,
    });

    const originalGetBoundingClientRect =
      Element.prototype.getBoundingClientRect;

    Element.prototype.getBoundingClientRect = jest
      .fn()
      .mockImplementation(() => ({
        top: 900,
        bottom: 930,
        left: 100,
        right: 300,
        width: 200,
        height: 30,
        x: 100,
        y: 900,
        toJSON: () => ({}),
      }));

    testRender(
      <TruncatedTextWithPopover text="Sample text" popoverMaxHeight={200} />,
    );

    fireEvent.click(screen.getByText("Sample text"));

    expect(Element.prototype.getBoundingClientRect).toHaveBeenCalled();
    Element.prototype.getBoundingClientRect = originalGetBoundingClientRect;
  });
});
