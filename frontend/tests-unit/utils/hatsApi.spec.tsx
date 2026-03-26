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

import HatsApi from "@/utils/hatsApi";

describe("HatsApi", () => {
  const mockRequestSurvey = jest.fn();
  const mockPresentSurvey = jest.fn();
  const mockLazyCreate = jest.fn(() => ({
    requestSurvey: mockRequestSurvey,
    presentSurvey: mockPresentSurvey,
  }));

  let mockWindow: any;

  beforeEach(() => {
    mockRequestSurvey.mockReset();
    mockPresentSurvey.mockReset();
    mockLazyCreate.mockReset();

    mockWindow = {
      help: {
        service: {
          Lazy: {
            create: mockLazyCreate,
          },
        },
      },
    };
  });

  it("creates a singleton instance", () => {
    HatsApi.instance = undefined;
    const instance1 = HatsApi.getInstance(mockWindow);
    const instance2 = HatsApi.getInstance(mockWindow);
    expect(instance1).toBe(instance2);
  });

  it("initializes hatsApi only once", () => {
    HatsApi.instance = undefined;
    const first = HatsApi.getInstance(mockWindow);
    const second = HatsApi.getInstance(mockWindow);
    expect(first).toBe(second);
    expect(mockLazyCreate).toHaveBeenCalledTimes(2);
  });

  it("does not initialize hatsApi if window is undefined", () => {
    HatsApi.instance = undefined;
    HatsApi.getInstance(undefined);
    expect(mockLazyCreate).not.toHaveBeenCalled();
  });
});
