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

import LatencyMetricCard from "@/app/(authRoutes)/projects/[id]/components/MetricCards/LatencyMetricCard";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { testRender } from "@/tests-unit/render";
import { ProjectType } from "@/types";
import { screen } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");

const mockedUseProjectContext = useProjectContext as jest.Mock;

describe("LatencyMetricCard", () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  function setup(
    projectType = ProjectType.POINTWISE,
    inferenceMetricsIsLoading = false,
  ) {
    mockedUseProjectContext.mockReturnValue({
      projectState: {
        project: { type: projectType },
        inferenceMetricsIsLoading,
      },
    });
  }

  it("renders single latency when not side-by-side mode", async () => {
    setup(ProjectType.POINTWISE, false);

    testRender(
      <LatencyMetricCard
        latencyA={1000}
        latencyB={null}
        models={[]}
        providers={["p1"]}
      />,
    );

    expect(screen.getByText("1.00s")).toBeInTheDocument();
  });

  it("renders side-by-side latencies and difference when in SxS mode", () => {
    setup(ProjectType.SIDE_BY_SIDE, false);

    testRender(
      <LatencyMetricCard
        latencyA={1000}
        latencyB={1500}
        models={[]}
        providers={["p1", "p2"]}
      />,
    );

    expect(screen.getByText("1.00s")).toBeInTheDocument();
  });

  it("does not show difference if latencyB is zero, null or undefined", () => {
    setup(ProjectType.SIDE_BY_SIDE, false);

    const { rerender } = testRender(
      <LatencyMetricCard latencyA={1000} latencyB={0} providers={[]} />,
    );

    expect(screen.queryByText("0.00s")).not.toBeInTheDocument();

    rerender(
      <LatencyMetricCard latencyA={1000} latencyB={null} providers={[]} />,
    );
    expect(screen.queryByText("0.00s")).not.toBeInTheDocument();

    rerender(
      <LatencyMetricCard latencyA={1000} latencyB={undefined} providers={[]} />,
    );
    expect(screen.queryByText("0.00s")).not.toBeInTheDocument();
  });

  it("renders lime text color when latency difference is negative", () => {
    setup(ProjectType.SIDE_BY_SIDE, false);

    testRender(
      <LatencyMetricCard
        latencyA={1500}
        latencyB={1000}
        models={[]}
        providers={[]}
      />,
    );

    expect(screen.getByText("1.50s")).toHaveClass(
      "mantine-focus-auto !font-medium text-title-28 m_b6d8b162 mantine-Text-root",
    );
  });

  it("shows '--' for zero, undefined, or null latency values", () => {
    setup(ProjectType.POINTWISE);

    const { rerender } = testRender(
      <LatencyMetricCard latencyA={0} providers={[]} />,
    );

    rerender(<LatencyMetricCard latencyA={undefined} providers={[]} />);
    expect(screen.getByText("--")).toBeInTheDocument();

    rerender(<LatencyMetricCard latencyB={null} providers={[]} />);
    expect(screen.getByText("--")).toBeInTheDocument();
  });

  it("renders footer text", () => {
    setup(ProjectType.POINTWISE);

    testRender(<LatencyMetricCard latencyA={100} providers={[]} />);

    expect(
      screen.getByText("May differ from production performance"),
    ).toBeInTheDocument();
  });
});
