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

import sortByName from "@/utils/sortByName";

describe("sortByName", () => {
  it("sorts objects alphabetically by name", () => {
    const unsorted = [
      { name: "Zebra", id: 1 },
      { name: "Apple", id: 2 },
      { name: "Banana", id: 3 }
    ];
    
    const sorted = sortByName(unsorted);
    
    expect(sorted).toEqual([
      { name: "Apple", id: 2 },
      { name: "Banana", id: 3 },
      { name: "Zebra", id: 1 }
    ]);
  });

  it("handles empty arrays", () => {
    const emptyArray: any[] = [];
    expect(sortByName(emptyArray)).toEqual([]);
  });

  it("handles arrays with a single item", () => {
    const singleItem = [{ name: "Only Item" }];
    expect(sortByName(singleItem)).toEqual([{ name: "Only Item" }]);
  });

  it("maintains stable sort for duplicate names", () => {
    const withDuplicates = [
      { name: "Same", id: 1 },
      { name: "Different", id: 2 },
      { name: "Same", id: 3 }
    ];
    
    const sorted = sortByName(withDuplicates);
    
    // Check that "Different" comes first alphabetically
    expect(sorted[0].name).toBe("Different");
    
    // Check that both "Same" entries are present
    const sameEntries = sorted.filter(item => item.name === "Same");
    expect(sameEntries.length).toBe(2);
    
    // Since JS sort is stable, original order of duplicates should be preserved
    expect(sorted.filter(item => item.name === "Same")[0].id).toBe(1);
    expect(sorted.filter(item => item.name === "Same")[1].id).toBe(3);
  });

  it("sorts case-insensitively", () => {
    const mixedCase = [
      { name: "zebra" },
      { name: "Apple" },
      { name: "banana" }
    ];
    
    const sorted = sortByName(mixedCase);
    
    expect(sorted.map(item => item.name)).toEqual(["Apple", "banana", "zebra"]);
  });

  it("sorts names with special characters correctly", () => {
    const specialChars = [
      { name: "Zürich" },
      { name: "Aachen" },
      { name: "Østersund" }
    ];
    
    const sorted = sortByName(specialChars);
    
    // The exact order can depend on locale, but Aachen should always come first
    expect(sorted[0].name).toBe("Aachen");
  });

  it("throws error when items don't have name property", () => {
    const invalidItems = [
      { id: 1 },
      { label: "No Name" }
    ];
    
    expect(() => sortByName(invalidItems)).toThrow();
  });

  it("preserves other properties of objects", () => {
    const items = [
      { name: "B", data: { value: 100 }, active: true },
      { name: "A", data: { value: 200 }, active: false }
    ];
    
    const sorted = sortByName(items);
    
    expect(sorted[0].name).toBe("A");
    expect(sorted[0].data.value).toBe(200);
    expect(sorted[0].active).toBe(false);
    expect(sorted[1].name).toBe("B");
    expect(sorted[1].data.value).toBe(100);
    expect(sorted[1].active).toBe(true);
  });
});