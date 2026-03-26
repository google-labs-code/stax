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

import isValidJSON from "@/utils/isValidJSON";

describe("isValidJSON", () => {
  it("returns true for valid JSON objects", () => {
    expect(isValidJSON('{"name":"John","age":30}')).toBe(true);
    expect(isValidJSON('{"nested":{"key":"value"}}')).toBe(true);
    expect(isValidJSON('{}')).toBe(true);
  });

  it("returns true for valid JSON arrays", () => {
    expect(isValidJSON('[1,2,3]')).toBe(true);
    expect(isValidJSON('["apple","banana"]')).toBe(true);
    expect(isValidJSON('[]')).toBe(true);
    expect(isValidJSON('[{"id":1},{"id":2}]')).toBe(true);
  });

  it("returns true for valid JSON primitives", () => {
    expect(isValidJSON('"string"')).toBe(true);
    expect(isValidJSON('123')).toBe(true);
    expect(isValidJSON('true')).toBe(true);
    expect(isValidJSON('false')).toBe(true);
    expect(isValidJSON('null')).toBe(true);
  });

  it("returns false for invalid JSON syntax", () => {
    expect(isValidJSON('{')).toBe(false);
    expect(isValidJSON('{"name":"John"')).toBe(false);
    expect(isValidJSON('["value",')).toBe(false);
    expect(isValidJSON('{"key": value}')).toBe(false);
    expect(isValidJSON('NaN')).toBe(false);
    expect(isValidJSON('Infinity')).toBe(false);
    expect(isValidJSON('undefined')).toBe(false);
  });

  it("returns false for non-JSON content", () => {
    expect(isValidJSON('')).toBe(false);
    expect(isValidJSON('Hello world')).toBe(false);
    expect(isValidJSON('function() {}')).toBe(false);
  });

  it("handles numeric edge cases", () => {
    expect(isValidJSON('-0')).toBe(true);
    expect(isValidJSON('-123.456')).toBe(true);
    expect(isValidJSON('1e10')).toBe(true);
    expect(isValidJSON('1.23e-10')).toBe(true);
    expect(isValidJSON('+123')).toBe(false); 
    expect(isValidJSON('.123')).toBe(false);
  });
  
  it("handles whitespace correctly", () => {
    expect(isValidJSON(' {"name":"John"} ')).toBe(true);
    expect(isValidJSON('\n123\t')).toBe(true);
    expect(isValidJSON('\r\n{"key": "value"}\n')).toBe(true);
  });

  it("handles special characters in strings", () => {
    expect(isValidJSON('"special chars: \\n\\t\\r\\""')).toBe(true);
    expect(isValidJSON('"unicode: \\u00A9"')).toBe(true);
    
    expect(isValidJSON('"contains "unescaped" quote"')).toBe(false);
    expect(isValidJSON('"invalid escape: \\"')).toBe(false);
  });
});