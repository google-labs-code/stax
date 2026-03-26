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

import ProjectsLayout from "@/app/(authRoutes)/projects/layout";
import { render, screen } from "@testing-library/react";
import React from "react";

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  ProjectProvider: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="project-provider">{children}</div>
  ),
  useProjectContext: () => ({
    projectState: {
      project: {
        type: "test",
      },
    },
  }),
}));

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: () => ({
    setOpenedModel: jest.fn(),
  }),
}));

describe("ProjectsLayout", () => {
  it("renders children within the ProjectProvider", () => {
    render(
      <ProjectsLayout modal={<div>Modal</div>}>
        <div>Test Child</div>
      </ProjectsLayout>,
    );

    expect(screen.getByText("Test Child")).toBeInTheDocument();

    expect(screen.getByTestId("project-provider")).toBeInTheDocument();
  });
});
