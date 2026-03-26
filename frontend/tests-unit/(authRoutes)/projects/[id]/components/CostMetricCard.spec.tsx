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

import CostMetricCard from "@/app/(authRoutes)/projects/[id]/components/MetricCards/CostMetricCard";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

describe("CostMetricCard", () => {
  it("renders with single data point", () => {
    testRender(<CostMetricCard data={[123.45]} providers={["provider1"]} />);
    expect(screen.getByText("Average Cost")).toBeInTheDocument();
    expect(screen.getByText("$ 123.45")).toBeInTheDocument();
    expect(
      screen.getByText(/Estimated using input\/output prices\/Mtok/i),
    ).toBeInTheDocument();
  });

  it("renders with multiple data points", () => {
    testRender(
      <CostMetricCard data={[10, 20]} providers={["provider1", "provider2"]} />,
    );
    expect(screen.getByText("Average Cost")).toBeInTheDocument();
    expect(screen.getByText("not supported yet")).toBeInTheDocument();
  });

  it("renders models and providers props without error", () => {
    const models = [{ id: "model1", name: "Model 1" }];
    const providers = ["provider1"];
    testRender(
      <CostMetricCard data={[5]} models={models} providers={providers} />,
    );
    expect(screen.getByText("Average Cost")).toBeInTheDocument();
  });
});
