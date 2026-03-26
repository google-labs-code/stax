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

import TokensMetricCard from "@/app/(authRoutes)/projects/[id]/components/MetricCards/TokensMetricCard";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { testRender } from "@/tests-unit/render";
import { ProjectType } from "@/types";
import { screen } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

const mockedUseProjectContext = useProjectContext as jest.Mock;

beforeAll(() => {
  Object.defineProperty(window, "matchMedia", {
    writable: true,
    value: jest.fn().mockImplementation((query) => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: jest.fn(),
      removeListener: jest.fn(),
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      dispatchEvent: jest.fn(),
    })),
  });
});

describe("TokensMetricCard", () => {
  it("renders pointwise mode with input and output", () => {
    mockedUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: ProjectType.POINTWISE },
        inferenceMetricsIsLoading: false,
      },
    });

    testRender(
      <TokensMetricCard
        tokenMetricsA={{ input: 100, output: 200 }}
        providers={[]}
      />,
    );

    expect(screen.getByText("Input")).toBeInTheDocument();
    expect(screen.getByText("Output")).toBeInTheDocument();
    expect(screen.getByText("100")).toBeInTheDocument();
    expect(screen.getByText("200")).toBeInTheDocument();
  });

  it("renders SxS mode with output and total values", () => {
    mockedUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: ProjectType.SIDE_BY_SIDE },
        inferenceMetricsIsLoading: false,
      },
    });

    testRender(
      <TokensMetricCard
        tokenMetricsA={{ output: 100, total: 150 }}
        tokenMetricsB={{ output: 120, total: 180 }}
        providers={[]}
      />,
    );

    expect(screen.getByText("Output")).toBeInTheDocument();
    expect(screen.getByText("Input")).toBeInTheDocument();
    expect(screen.getByText("100")).toBeInTheDocument();
  });

  it("shows differences when B values are present and non-zero", () => {
    mockedUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: ProjectType.SIDE_BY_SIDE },
        inferenceMetricsIsLoading: false,
      },
    });

    testRender(
      <TokensMetricCard
        tokenMetricsA={{ output: 100, total: 200 }}
        tokenMetricsB={{ output: 130, total: 220 }}
        providers={[]}
      />,
    );

    expect(screen.getByText("100")).toBeInTheDocument();
  });

  it("shows red text for negative differences", () => {
    mockedUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: ProjectType.SIDE_BY_SIDE },
        inferenceMetricsIsLoading: false,
      },
    });

    testRender(
      <TokensMetricCard
        tokenMetricsA={{ output: 150, total: 220 }}
        tokenMetricsB={{ output: 130, total: 180 }}
        providers={[]}
      />,
    );

    expect(screen.getByText("150")).toBeInTheDocument();
  });

  it("renders labels in SxS mode", () => {
    mockedUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: ProjectType.SIDE_BY_SIDE },
        inferenceMetricsIsLoading: false,
      },
    });

    testRender(
      <TokensMetricCard
        tokenMetricsA={{ output: 100, total: 200 }}
        tokenMetricsB={{ output: 120, total: 220 }}
        providers={[]}
      />,
    );

    expect(screen.getAllByText("Output").length).toBe(1);
  });
});
