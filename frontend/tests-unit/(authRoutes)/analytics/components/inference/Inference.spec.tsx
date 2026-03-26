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

import Inference from "@/app/(authRoutes)/analytics/components/inference/Inference";
import { testRender } from "@/tests-unit/render";
import { act, fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@/hooks/useAnalyticsContext", () => ({
  useAnalyticsContext: () => ({
    selectedFilters: [
      {
        project: "proj-1",
        model: { provider: "model-1" },
        tags: [],
      },
      {
        project: "proj-2",
        model: { provider: "model-2" },
        tags: [],
      },
    ],
  }),
}));

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: () => ({
    allModels: [],
    isLoadingModels: false,
  }),
}));

jest.mock("@/hooks/useTagsContext", () => ({
  useTagsContext: () => ({
    userTags: [],
    isLoadingTags: false,
  }),
}));

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: () => ({
    allProjects: [],
    isLoadingProjects: false,
  }),
}));

jest.mock(
  "@/app/(authRoutes)/analytics/components/inference/InferenceChart",
  () => ({
    __esModule: true,
    default: ({
      selectedFilters,
      selectedDatesLabel,
      duplicateChart,
      deleteChart,
    }: any) => (
      <div>
        <div data-testid="selected-dates">{selectedDatesLabel}</div>
        <div data-testid="duplicate-button" onClick={duplicateChart}>
          Duplicate
        </div>
        {deleteChart && (
          <div data-testid="delete-button" onClick={deleteChart}>
            Delete
          </div>
        )}
        <div data-testid="selected-filters">
          {selectedFilters.map((filter: any, index: number) => (
            <div key={index}>{filter.project}</div>
          ))}
        </div>
      </div>
    ),
  }),
);

describe("Inference Component", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders key sections", () => {
    testRender(<Inference />);

    expect(screen.getByTestId("selected-filters")).toBeInTheDocument();
    expect(screen.getByTestId("selected-dates")).toHaveTextContent(
      "Last 15 days",
    );
  });

  it("duplicates chart correctly", () => {
    testRender(<Inference />);

    const duplicateButton = screen.getByTestId("duplicate-button");
    expect(duplicateButton).toBeInTheDocument();

    fireEvent.click(duplicateButton);

    waitFor(() =>
      expect(screen.getAllByTestId("selected-dates").length).toBe(2),
    );
  });

  it("updates chart dates correctly", async () => {
    testRender(<Inference />);

    const dateLabelBeforeChange = screen.getByTestId("selected-dates");
    expect(dateLabelBeforeChange).toHaveTextContent("Last 15 days");

    await act(async () => {
      fireEvent.click(dateLabelBeforeChange);
    });

    expect(screen.getByTestId("selected-dates")).toHaveTextContent(
      "Last 15 days",
    );
  });
});
