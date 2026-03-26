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
import { ProjectsOnboardingStorage } from "@/utils/projectsOnboardingStorage";

jest.mock("@/utils/LocalStorage", () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    set: jest.fn(),
  },
}));

describe("ProjectsOnboardingStorage", () => {
  const STORAGE_KEY = "projectsOnboarding";

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("get", () => {
    it("returns false when no onboarding data exists", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(null);

      const result = ProjectsOnboardingStorage.get("project1");

      expect(result).toBe(false);
      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
    });

    it("returns false when project ID is not found in onboarding data", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue('{"project2":true}');

      const result = ProjectsOnboardingStorage.get("project1");

      expect(result).toBe(false);
      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
    });

    it("returns the correct onboarding state when project ID exists", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(
        '{"project1":true,"project2":false}',
      );

      const result = ProjectsOnboardingStorage.get("project1");

      expect(result).toBe(true);
      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
    });
  });

  describe("set", () => {
    it("creates a new onboarding object when none exists", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(null);

      ProjectsOnboardingStorage.set("project1", true);

      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
      expect(LocalStorage.set).toHaveBeenCalledWith(
        STORAGE_KEY,
        '{"project1":true}',
      );
    });

    it("updates an existing onboarding object with a new project", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue('{"project2":false}');

      ProjectsOnboardingStorage.set("project1", true);

      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
      expect(LocalStorage.set).toHaveBeenCalledWith(
        STORAGE_KEY,
        '{"project2":false,"project1":true}',
      );
    });

    it("updates an existing project's onboarding state", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(
        '{"project1":false,"project2":true}',
      );

      ProjectsOnboardingStorage.set("project1", true);

      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
      expect(LocalStorage.set).toHaveBeenCalledWith(
        STORAGE_KEY,
        '{"project1":true,"project2":true}',
      );
    });
  });

  describe("remove", () => {
    it("does nothing when no onboarding data exists", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(null);

      ProjectsOnboardingStorage.remove("project1");

      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
      expect(LocalStorage.set).not.toHaveBeenCalled();
    });

    it("removes a project from the onboarding object", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(
        '{"project1":true,"project2":false}',
      );

      ProjectsOnboardingStorage.remove("project1");

      // Assert
      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
      expect(LocalStorage.set).toHaveBeenCalledWith(
        STORAGE_KEY,
        '{"project2":false}',
      );
    });

    it("does nothing when project doesn't exist in onboarding data", () => {
      (LocalStorage.get as jest.Mock).mockReturnValue('{"project2":false}');

      ProjectsOnboardingStorage.remove("project1");

      expect(LocalStorage.get).toHaveBeenCalledWith(STORAGE_KEY);
      expect(LocalStorage.set).toHaveBeenCalledWith(
        STORAGE_KEY,
        '{"project2":false}',
      );
    });
  });

  describe("STORAGE_KEY", () => {
    it("has the correct storage key value", () => {
      expect(ProjectsOnboardingStorage.STORAGE_KEY).toBe("projectsOnboarding");
    });
  });
});
