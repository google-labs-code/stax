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

import { Nullable } from "@/types";

import LocalStorage from "./LocalStorage";

interface ProjectsOnboarding {
  [key: string]: boolean;
}

export class ProjectsOnboardingStorage {
  public static STORAGE_KEY = "projectsOnboarding";

  static get(projectId: string): Nullable<boolean> {
    const projectsOnboarding = LocalStorage.get(this.STORAGE_KEY);
    if (!projectsOnboarding) return false;

    const projectsOnboardingData = JSON.parse(
      projectsOnboarding,
    ) as ProjectsOnboarding;

    return projectsOnboardingData[projectId] || false;
  }

  static set(projectId: string, onboarding: boolean): void {
    let projectsOnboarding = LocalStorage.get(this.STORAGE_KEY);
    if (!projectsOnboarding) {
      projectsOnboarding = "{}";
    }

    const projectsOnboardingData = JSON.parse(
      projectsOnboarding,
    ) as ProjectsOnboarding;

    projectsOnboardingData[projectId] = onboarding;

    return LocalStorage.set(
      this.STORAGE_KEY,
      JSON.stringify(projectsOnboardingData),
    );
  }

  static remove(projectId: string): void {
    const projectsOnboarding = LocalStorage.get(this.STORAGE_KEY);
    if (!projectsOnboarding) return;

    const projectsOnboardingData = JSON.parse(
      projectsOnboarding,
    ) as ProjectsOnboarding;

    delete projectsOnboardingData[projectId];

    return LocalStorage.set(
      this.STORAGE_KEY,
      JSON.stringify(projectsOnboardingData),
    );
  }
}
