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

import focusEditableInput from "@/utils/focusEditableInput";

describe("focusEditableInput", () => {
  let mockElement: { focus: jest.Mock };
  let mockRange: { selectNodeContents: jest.Mock; collapse: jest.Mock };
  let mockSelection: { removeAllRanges: jest.Mock; addRange: jest.Mock };

  beforeEach(() => {
    mockElement = { focus: jest.fn() };
    mockRange = { selectNodeContents: jest.fn(), collapse: jest.fn() };
    mockSelection = { removeAllRanges: jest.fn(), addRange: jest.fn() };

    jest.spyOn(document, "getElementById").mockImplementation((id) => {
      return id === "test-id" ? (mockElement as any) : null;
    });

    jest.spyOn(document, "createRange").mockReturnValue(mockRange as any);
    jest.spyOn(window, "getSelection").mockReturnValue(mockSelection as any);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it("focuses the element and sets cursor to end", () => {
    focusEditableInput("test-id");

    expect(document.getElementById).toHaveBeenCalledWith("test-id");
    expect(mockElement.focus).toHaveBeenCalled();
    expect(document.createRange).toHaveBeenCalled();
    expect(mockRange.selectNodeContents).toHaveBeenCalledWith(mockElement);
    expect(mockRange.collapse).toHaveBeenCalledWith(false); 
    expect(mockSelection.removeAllRanges).toHaveBeenCalled();
    expect(mockSelection.addRange).toHaveBeenCalledWith(mockRange);
  });

  it("does nothing when element doesn't exist", () => {
    focusEditableInput("non-existent-id");

    expect(document.getElementById).toHaveBeenCalledWith("non-existent-id");
    expect(mockElement.focus).not.toHaveBeenCalled();
    expect(mockRange.selectNodeContents).not.toHaveBeenCalled();
    expect(mockSelection.removeAllRanges).not.toHaveBeenCalled();
  });

  it("only focuses element when selection API is not available", () => {
    jest.spyOn(window, "getSelection").mockReturnValueOnce(null);

    focusEditableInput("test-id");

    expect(document.getElementById).toHaveBeenCalledWith("test-id");
    expect(mockElement.focus).toHaveBeenCalled();
    expect(document.createRange).toHaveBeenCalled();
    expect(mockRange.selectNodeContents).not.toHaveBeenCalled();
    expect(mockSelection.removeAllRanges).not.toHaveBeenCalled();
  });

  it("follows correct method call order", () => {
    const callOrder: string[] = [];
    mockElement.focus.mockImplementation(() => callOrder.push("focus"));
    mockRange.selectNodeContents.mockImplementation(() =>
      callOrder.push("selectNodeContents"),
    );
    mockRange.collapse.mockImplementation(() => callOrder.push("collapse"));
    mockSelection.removeAllRanges.mockImplementation(() =>
      callOrder.push("removeAllRanges"),
    );
    mockSelection.addRange.mockImplementation(() => callOrder.push("addRange"));

    focusEditableInput("test-id");

    expect(callOrder).toEqual([
      "focus",
      "selectNodeContents",
      "collapse",
      "removeAllRanges",
      "addRange",
    ]);
  });
});
