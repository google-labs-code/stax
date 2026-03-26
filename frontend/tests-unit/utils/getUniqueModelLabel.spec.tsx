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

import { getUniqueModelLabel } from "@/utils/getUniqueModelLabel";

describe("getUniqueModelLabel", () => {
  const originalRandom = Math.random;

  beforeEach(() => {
    Math.random = jest.fn().mockReturnValue(0.5);
  });

  afterEach(() => {
    Math.random = originalRandom;
  });

  it("appends a unique identifier to labels without custom version", () => {
    const result = getUniqueModelLabel("GPT-4");
    expect(result).toBe("GPT-4 (custom v55000)");
  });

  it("replaces existing custom version with a new one", () => {
    const result = getUniqueModelLabel("GPT-4 (custom v123)");
    expect(result).toBe("GPT-4 (custom v55000)");
  });

  it("replaces first occurrence of 'custom v' digits if label contains '(custom v'", () => {
    const result = getUniqueModelLabel("custom v100 model (custom v200)");
    expect(result).toBe("custom v55000 model (custom v200)");
  });

  it("works with empty strings", () => {
    const result = getUniqueModelLabel("");
    expect(result).toBe(" (custom v55000)");
  });

  it("handles case where 'custom v' appears without parentheses", () => {
    const result = getUniqueModelLabel("Model custom v100");
    expect(result).toBe("Model custom v100 (custom v55000)");
  });

  it("generates different identifiers when Math.random returns different values", () => {
    Math.random = jest.fn().mockReturnValueOnce(0.3).mockReturnValueOnce(0.7);

    const label = "Model";
    const result1 = getUniqueModelLabel(label);
    const result2 = getUniqueModelLabel(label);

    expect(result1).toBe("Model (custom v37000)");
    expect(result2).toBe("Model (custom v73000)");
  });
});
