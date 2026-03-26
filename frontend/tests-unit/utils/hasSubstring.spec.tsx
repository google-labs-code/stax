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

import hasSubstring from "@/utils/hasSubstring";

describe("hasSubstring", () => {
  it("finds exact matches with same case", () => {
    expect(hasSubstring("hello world", "hello")).toBe(true);
    expect(hasSubstring("test string", "test")).toBe(true);
    expect(hasSubstring("JavaScript", "Script")).toBe(true);
  });

  it("finds case-insensitive matches", () => {
    expect(hasSubstring("Hello World", "hello")).toBe(true);
    expect(hasSubstring("TEST STRING", "test")).toBe(true);
    expect(hasSubstring("javascript", "JavaScript")).toBe(true);
  });

  it("finds substrings at different positions", () => {
    expect(hasSubstring("Hello World", "Hello")).toBe(true);
    
    expect(hasSubstring("Hello World", "o Wo")).toBe(true);
    
    expect(hasSubstring("Hello World", "World")).toBe(true);
  });

  it("handles empty search terms", () => {
    expect(hasSubstring("Hello World", "")).toBe(true);
    expect(hasSubstring("", "")).toBe(true);
  });

  it("handles empty values", () => {
    expect(hasSubstring("", "test")).toBe(false);
  });

  it("handles whitespace correctly", () => {
    expect(hasSubstring("Hello World", " ")).toBe(true);
    expect(hasSubstring("Hello World", "  ")).toBe(false);
    expect(hasSubstring("  spaces  ", " spaces ")).toBe(true);
  });

  it("works with special characters", () => {
    expect(hasSubstring("email@example.com", "@example")).toBe(true);
    expect(hasSubstring("test-string", "-str")).toBe(true);
    expect(hasSubstring("price: $100", "$100")).toBe(true);
  });

  it("works with numbers in strings", () => {
    expect(hasSubstring("Item 123", "123")).toBe(true);
    expect(hasSubstring("Version 2.0.1", "2.0")).toBe(true);
  });

  it("returns false when substring is not found", () => {
    expect(hasSubstring("Hello World", "goodbye")).toBe(false);
    expect(hasSubstring("test string", "testing")).toBe(false);
    expect(hasSubstring("JavaScript", "Java code")).toBe(false);
  });

  it("correctly handles similar but non-matching substrings", () => {
    expect(hasSubstring("hello", "helo")).toBe(false);
    expect(hasSubstring("test", "tes ")).toBe(false);
  });
});