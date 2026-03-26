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

import LocalStorage from "@/utils/LocalStorage";

jest.mock("@/config/constants", () => ({
  LOCAL_STORAGE_KEYS_TO_PRESERVE_ON_CLEAR: ["preserveKey1", "preserveKey2"],
}));

describe("LocalStorage", () => {
  let mockStorage: Record<string, string> = {};

  const mockLocalStorage = {
    getItem: jest.fn((key: string) => mockStorage[key] || null),
    setItem: jest.fn((key: string, value: string) => {
      mockStorage[key] = value;
    }),
    removeItem: jest.fn((key: string) => {
      delete mockStorage[key];
    }),
    clear: jest.fn(() => {
      mockStorage = {};
    }),
  };

  beforeEach(() => {
    mockStorage = {};

    Object.defineProperty(window, "localStorage", {
      value: mockLocalStorage,
      writable: true,
    });

    jest.clearAllMocks();
  });

  describe("get", () => {
    it("should get a value from localStorage", () => {
      mockStorage.testKey = "testValue";

      const result = LocalStorage.get("testKey");

      expect(result).toBe("testValue");
      expect(mockLocalStorage.getItem).toHaveBeenCalledWith("testKey");
    });

    it("should return null for non-existent keys", () => {
      const result = LocalStorage.get("nonExistentKey");

      expect(result).toBeNull();
      expect(mockLocalStorage.getItem).toHaveBeenCalledWith("nonExistentKey");
    });
  });

  describe("set", () => {
    it("should set a string value in localStorage", () => {
      LocalStorage.set("testKey", "testValue");

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        "testKey",
        "testValue",
      );
      expect(mockStorage.testKey).toBe("testValue");
    });

    it("should stringify non-string values before setting", () => {
      const testObj = { foo: "bar" };
      LocalStorage.set("testKey", testObj);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        "testKey",
        JSON.stringify(testObj),
      );
      expect(mockStorage.testKey).toBe('{"foo":"bar"}');
    });

    it("should stringify array values", () => {
      const testArray = [1, 2, 3];
      LocalStorage.set("testKey", testArray);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        "testKey",
        JSON.stringify(testArray),
      );
      expect(mockStorage.testKey).toBe("[1,2,3]");
    });

    it("should stringify number values", () => {
      LocalStorage.set("testKey", 123);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith("testKey", "123");
    });

    it("should stringify boolean values", () => {
      LocalStorage.set("testKey", true);

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith("testKey", "true");
    });
  });

  describe("remove", () => {
    it("should remove an item from localStorage", () => {
      mockStorage.testKey = "testValue";

      LocalStorage.remove("testKey");

      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith("testKey");
      expect(mockStorage.testKey).toBeUndefined();
    });
  });

  describe("clear", () => {
    it("should clear localStorage while preserving specified keys", () => {
      mockStorage = {
        preserveKey1: "value1",
        preserveKey2: "value2",
        regularKey: "value3",
      };

      LocalStorage.clear();

      expect(mockLocalStorage.clear).toHaveBeenCalled();
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        "preserveKey1",
        "value1",
      );
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        "preserveKey2",
        "value2",
      );
      expect(mockStorage).toEqual({
        preserveKey1: "value1",
        preserveKey2: "value2",
      });
      expect(mockStorage.regularKey).toBeUndefined();
    });

    it("should only preserve keys that exist", () => {
      mockStorage = {
        preserveKey1: "value1",
        regularKey: "value3",
      };

      LocalStorage.clear();

      expect(mockLocalStorage.clear).toHaveBeenCalled();
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        "preserveKey1",
        "value1",
      );
      expect(mockLocalStorage.setItem).not.toHaveBeenCalledWith(
        "preserveKey2",
        expect.anything(),
      );
      expect(mockStorage).toEqual({
        preserveKey1: "value1",
      });
    });

    it("should handle empty preserved keys", () => {
      mockStorage = {
        regularKey: "value3",
      };

      LocalStorage.clear();

      expect(mockLocalStorage.clear).toHaveBeenCalled();
      expect(mockLocalStorage.setItem).not.toHaveBeenCalled();
      expect(mockStorage).toEqual({});
    });
  });

  describe("server-side behavior", () => {
    const originalWindow = global.window;

    beforeEach(() => {
      // @ts-expect-error - simulate server environment
      delete global.window;
    });

    afterEach(() => {
      global.window = originalWindow;
    });

    it("should not throw errors when on server side", () => {
      jest.resetModules();
      const ServerLocalStorage = require("@/utils/LocalStorage").default;

      expect(() => ServerLocalStorage.get("testKey")).not.toThrow();
      expect(() => ServerLocalStorage.set("testKey", "value")).not.toThrow();
      expect(() => ServerLocalStorage.remove("testKey")).not.toThrow();
      expect(() => ServerLocalStorage.clear()).not.toThrow();
    });
  });
});
