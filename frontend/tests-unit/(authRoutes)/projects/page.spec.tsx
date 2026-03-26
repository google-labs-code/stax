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

import Projects from "@/app/(authRoutes)/projects/page";
import { MainConfig } from "@/config/config";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

const mockMutate = jest.fn((_, { onSuccess } = {}) => {
  if (onSuccess) {
    onSuccess({
      projects: [
        {
          project_id: "project-123",
          name: "Quick Compare History",
          description:
            "This project will by default contain all quick compare history.",
          type: "POINTWISE",
          created_at: "2025-07-22T08:19:43.128+00:00",
        },
      ],
    });
  }
});

jest.mock("@tanstack/react-query", () => {
  const originalModule = jest.requireActual("@tanstack/react-query");

  return {
    ...originalModule,
    useMutation: () => ({
      mutate: mockMutate,
      isLoading: false,
      isSuccess: true,
      isError: false,
    }),
  };
});

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: () => ({
    allProjects: [],
    refreshProjects: () => {},
  }),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: () => ({
    projectActions: [],
  }),
}));

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

describe("Projects page", () => {
  it("renders the Add Project button with icon", () => {
    testRender(<Projects />);

    const addProjectButton = screen.getByTestId("btn-new-project");
    expect(addProjectButton).toBeInTheDocument();
    expect(addProjectButton).toHaveTextContent("Add project");

    const addIcon = screen
      .getAllByTestId("material-icon")
      .find((el) => el.textContent === "add");
    expect(addIcon).toBeInTheDocument();
  });

  if (MainConfig.isAuthEnabled) {
    it("renders the more options menu button", () => {
      testRender(<Projects />);

      const moreVertIcon = screen
        .getAllByTestId("material-icon")
        .find((el) => el.textContent === "more_vert");

      expect(moreVertIcon).toBeInTheDocument();
    });
  }

  it("renders modals in portals", () => {
    testRender(<Projects />);

    const modals = document.querySelectorAll(".mantine-Modal-root");
    expect(modals.length).toBeGreaterThan(0);
  });

  it("renders the layout container with correct classes", () => {
    testRender(<Projects />);

    const layoutContainer = document.querySelector(".gap-3xl.flex.flex-col");
    expect(layoutContainer).toBeInTheDocument();
  });

  it("calls mutation on Add Project click", async () => {
    testRender(<Projects />);
    const user = userEvent.setup();

    const addButton = screen.getByTestId("btn-new-project");
    await user.click(addButton);

    expect(mockMutate).toHaveBeenCalled();
  });
});
