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

import {
  formatJsonString,
  parseJsonSafely,
  validateJson,
} from "@/utils/jsonUtils";

describe("formatJsonString", () => {
  it("should format valid JSON string with proper indentation", () => {
    const input = '{"name":"John","age":30}';
    const expected = `{
  "name": "John",
  "age": 30
}`;
    expect(formatJsonString(input)).toBe(expected);
  });

  it("should return empty string for empty input", () => {
    expect(formatJsonString("")).toBe("");
    expect(formatJsonString("   ")).toBe("");
    expect(formatJsonString(null as any)).toBe("");
  });

  it("should return original string if parsing fails", () => {
    const invalidJson = '{"name": "John", age: 30}';
    expect(formatJsonString(invalidJson)).toBe(invalidJson);
  });

  it("should preserve nested objects and arrays", () => {
    const input = '{"user":{"name":"John","hobbies":["reading","coding"]}}';
    const formatted = formatJsonString(input);
    expect(formatted).toContain('"user": {');
    expect(formatted).toContain('"hobbies": [');
    expect(formatted).toContain('"reading",');
  });
});

describe("validateJson", () => {
  it("should validate correct JSON object", () => {
    const result = validateJson('{"name":"John","age":30}');
    expect(result.valid).toBe(true);
    expect(result.error).toBeNull();
  });

  it("should accept empty string", () => {
    const result = validateJson("");
    expect(result.valid).toBe(true);
    expect(result.error).toBeNull();
  });

  it("should reject JSON not starting with { and ending with }", () => {
    const result = validateJson('"name":"John"');
    expect(result.valid).toBe(false);
    expect(result.error).toBe("Invalid JSON: must start with { and end with }");
  });

  it("should reject empty objects", () => {
    const result = validateJson("{}");
    expect(result.valid).toBe(false);
    expect(result.error).toBe("Invalid JSON: must not be empty");
  });

  it("should reject invalid JSON syntax", () => {
    const result = validateJson('{"name": "John", age: 30}');
    expect(result.valid).toBe(false);
    expect(result.error).toContain("Invalid JSON:");
  });
});

describe("parseJsonSafely", () => {
  it("should parse JSON and convert all values to strings", () => {
    const input = '{"name":"John","age":30,"isAdmin":true,"score":null}';
    const result = parseJsonSafely(input);
    expect(result).toEqual({
      name: "John",
      age: "30",
      isAdmin: "true",
      score: "null",
    });
  });

  it("should return null for invalid JSON", () => {
    const invalidJson = '{"name": "John", age: 30}';
    expect(parseJsonSafely(invalidJson)).toBeNull();
  });

  it("should handle empty object correctly", () => {
    const emptyObj = "{}";
    expect(parseJsonSafely(emptyObj)).toEqual({});
  });
});
