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

import { scrollToCardsContainer } from "@/app/(authRoutes)/projects/[id]/playground/utils";

describe("playground utils", () => {
  describe("scrollToCardsContainer", () => {
    let mockContainer: HTMLElement;
    let mockScrollTo: jest.Mock;
    let originalGetElementsByClassName: typeof document.getElementsByClassName;
    let mockSetTimeout: jest.SpyInstance;
    let mockClearTimeout: jest.SpyInstance;

    beforeEach(() => {
      // Mock scrollTo method
      mockScrollTo = jest.fn();
      mockContainer = {
        scrollTo: mockScrollTo,
      } as any;

      // Mock getElementsByClassName to return our mock container
      originalGetElementsByClassName = document.getElementsByClassName;
      document.getElementsByClassName = jest
        .fn()
        .mockReturnValue([mockContainer] as any);

      // Mock setTimeout and clearTimeout
      mockSetTimeout = jest.spyOn(global, "setTimeout");
      mockClearTimeout = jest.spyOn(global, "clearTimeout");
    });

    afterEach(() => {
      // Restore original methods
      document.getElementsByClassName = originalGetElementsByClassName;
      mockSetTimeout.mockRestore();
      mockClearTimeout.mockRestore();
      jest.clearAllMocks();
    });

    it("should find the playground scroll area viewport container", () => {
      scrollToCardsContainer(0, 0);

      // Get the callback function passed to setTimeout
      const callback = mockSetTimeout.mock.calls[0][0];

      // Execute the callback
      callback();

      expect(document.getElementsByClassName).toHaveBeenCalledWith(
        "playground-scroll-area-viewport",
      );
    });

    it("should call scrollTo with correct parameters when container exists", () => {
      scrollToCardsContainer(0, 0);

      // Verify setTimeout was called with 0 delay
      expect(mockSetTimeout).toHaveBeenCalledWith(expect.any(Function), 0);

      // Get the callback function passed to setTimeout
      const callback = mockSetTimeout.mock.calls[0][0];

      // Execute the callback
      callback();

      expect(mockScrollTo).toHaveBeenCalledWith({
        top: 0,
        behavior: "smooth",
      });
    });

    it("should clear timeout after scrolling", () => {
      scrollToCardsContainer(0, 0);

      // Get the callback function passed to setTimeout
      const callback = mockSetTimeout.mock.calls[0][0];

      // Execute the callback
      callback();

      expect(mockClearTimeout).toHaveBeenCalled();
    });

    it("should handle case when container does not exist", () => {
      // Mock getElementsByClassName to return empty array
      document.getElementsByClassName = jest.fn().mockReturnValue([] as any);

      scrollToCardsContainer(0, 0);

      // Verify setTimeout was still called
      expect(mockSetTimeout).toHaveBeenCalledWith(expect.any(Function), 0);

      // Get the callback function passed to setTimeout
      const callback = mockSetTimeout.mock.calls[0][0];

      // Execute the callback - should not throw error
      expect(() => callback()).not.toThrow();

      // scrollTo should not be called
      expect(mockScrollTo).not.toHaveBeenCalled();
    });

    it("should handle case when container is null", () => {
      // Mock getElementsByClassName to return array with null
      document.getElementsByClassName = jest
        .fn()
        .mockReturnValue([null] as any);

      scrollToCardsContainer(0, 0);

      // Verify setTimeout was still called
      expect(mockSetTimeout).toHaveBeenCalledWith(expect.any(Function), 0);

      // Get the callback function passed to setTimeout
      const callback = mockSetTimeout.mock.calls[0][0];

      // Execute the callback - should not throw error
      expect(() => callback()).not.toThrow();

      // scrollTo should not be called
      expect(mockScrollTo).not.toHaveBeenCalled();
    });

    it("should use immediate timeout (0ms delay)", () => {
      scrollToCardsContainer(0, 0);

      expect(mockSetTimeout).toHaveBeenCalledWith(expect.any(Function), 0);
    });

    it("should not call scrollTo if container is undefined", () => {
      // Mock getElementsByClassName to return array with undefined
      document.getElementsByClassName = jest
        .fn()
        .mockReturnValue([undefined] as any);

      scrollToCardsContainer(0, 0);

      const callback = mockSetTimeout.mock.calls[0][0];
      callback();

      expect(mockScrollTo).not.toHaveBeenCalled();
    });

    it("should handle multiple containers and use the first one", () => {
      const secondContainer = { scrollTo: jest.fn() } as any;
      document.getElementsByClassName = jest
        .fn()
        .mockReturnValue([mockContainer, secondContainer] as any);

      scrollToCardsContainer(0, 0);

      const callback = mockSetTimeout.mock.calls[0][0];
      callback();

      expect(mockScrollTo).toHaveBeenCalledWith({
        top: 0,
        behavior: "smooth",
      });
      expect(secondContainer.scrollTo).not.toHaveBeenCalled();
    });
  });
});
