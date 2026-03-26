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

import { convertMsToS, handleCopy } from "@/utils/helpers";

describe("helpers utilities", () => {
  describe("handleCopy", () => {
    const originalClipboard = navigator.clipboard;

    beforeEach(() => {
      Object.defineProperty(navigator, "clipboard", {
        value: {
          writeText: jest.fn().mockImplementation(() => Promise.resolve()),
        },
        configurable: true,
      });
    });

    afterEach(() => {
      Object.defineProperty(navigator, "clipboard", {
        value: originalClipboard,
        configurable: true,
      });
    });

    it("copies the provided text to clipboard", () => {
      const textToCopy = "This is a test string";

      handleCopy(textToCopy);

      expect(navigator.clipboard.writeText).toHaveBeenCalledWith(textToCopy);
    });

    it("handles empty strings", () => {
      handleCopy("");

      expect(navigator.clipboard.writeText).toHaveBeenCalledWith("");
    });

    it("handles strings with special characters", () => {
      const specialText = "Special chars: \n\t\"'\\";

      handleCopy(specialText);
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith(specialText);
    });
  });

  describe("convertMsToS", () => {
    it("converts milliseconds to seconds with 2 decimal places and 's' suffix", () => {
      expect(convertMsToS(1000)).toBe("1.00s");
      expect(convertMsToS(1500)).toBe("1.50s");
      expect(convertMsToS(1234)).toBe("1.23s");
      expect(convertMsToS(5)).toBe("0.01s");
    });

    it("handles zero correctly", () => {
      expect(convertMsToS(0)).toBe("0.00s");
    });

    it("handles undefined by returning '0.00s'", () => {
      expect(convertMsToS(undefined)).toBe("0.00s");
    });

    it("handles NaN by returning '0.00s'", () => {
      expect(convertMsToS(NaN)).toBe("0.00s");
    });

    it("handles negative values", () => {
      expect(convertMsToS(-1000)).toBe("-1.00s");
      expect(convertMsToS(-1234)).toBe("-1.23s");
    });

    it("handles large values", () => {
      expect(convertMsToS(60000)).toBe("60.00s");
      expect(convertMsToS(3600000)).toBe("3600.00s");
    });

    it("properly rounds values", () => {
      expect(convertMsToS(1)).toBe("0.00s");
      expect(convertMsToS(4)).toBe("0.00s");
      expect(convertMsToS(5)).toBe("0.01s");
      expect(convertMsToS(994)).toBe("0.99s");
      expect(convertMsToS(996)).toBe("1.00s");
    });
  });
});
