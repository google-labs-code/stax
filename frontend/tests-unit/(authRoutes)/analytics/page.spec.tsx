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

import AnalyticsPage from "@/app/(authRoutes)/analytics/page";
import { act, screen } from "@testing-library/react";

import { testRender } from "../../render";

let mockAnalyticsContext = {
  activeTab: "inference",
  setActiveTab: jest.fn(),
  selectedFilters: [
    {
      project: "",
      model: null,
      tags: [],
    },
  ],
};

jest.mock("@/hooks/useAnalyticsContext", () => ({
  useAnalyticsContext: () => mockAnalyticsContext,
}));

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: () => ({
    allModels: [],
    isLoadingModels: false,
  }),
}));

jest.mock("@/components/Page", () => {
  const MockPage = ({ children }: any) => (
    <div data-testid="Page">{children}</div>
  );
  MockPage.displayName = "MockPage";

  return MockPage;
});

jest.mock("@/components/PageHeader", () => {
  const MockPageHeader = ({ title }: any) => (
    <div data-testid="PageHeader">{title}</div>
  );
  MockPageHeader.displayName = "MockPageHeader";

  return MockPageHeader;
});

jest.mock(
  "@/app/(authRoutes)/analytics/components/inference/Inference",
  () => ({
    __esModule: true,
    default: () => (
      <div data-testid="InferenceAnalytics">Inference Content</div>
    ),
  }),
);

jest.mock(
  "@/app/(authRoutes)/analytics/components/evaluation/Evaluation",
  () => ({
    __esModule: true,
    default: () => (
      <div data-testid="EvaluationAnalytics">Evaluation Content</div>
    ),
  }),
);

describe("AnalyticsPage", () => {
  beforeEach(() => {
    mockAnalyticsContext = {
      activeTab: "inference",
      setActiveTab: jest.fn(),
      selectedFilters: [
        {
          project: "",
          model: null,
          tags: [],
        },
      ],
    };
    jest.clearAllMocks();
  });

  it("renders PageHeader and Tabs with InferenceAnalytics by default", async () => {
    await act(async () => {
      testRender(<AnalyticsPage />);
    });

    expect(screen.getByTestId("PageHeader")).toHaveTextContent("Analytics");
    expect(screen.getByText("Inference")).toBeInTheDocument();
    expect(screen.getByText("Evaluation")).toBeInTheDocument();
    expect(screen.getByTestId("InferenceAnalytics")).toBeInTheDocument();
  });

  it("renders EvaluationAnalytics when activeTab is 'evaluation'", async () => {
    mockAnalyticsContext.activeTab = "evaluation";

    await act(async () => {
      testRender(<AnalyticsPage />);
    });

    expect(screen.getByTestId("EvaluationAnalytics")).toBeInTheDocument();
  });
});
