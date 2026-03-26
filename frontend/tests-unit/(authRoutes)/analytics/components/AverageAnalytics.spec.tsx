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

import AverageAnalytics from "@/app/(authRoutes)/analytics/components/AverageAnalytics";
import { MantineProvider } from "@mantine/core";
import { render, screen } from "@testing-library/react";

jest.mock("@/components/MaterialIcon", () => {
  const MockMaterialIcon = ({ name, className, tooltipLabel }: any) => (
    <div data-testid="material-icon" className={className}>
      {name} - {tooltipLabel}
    </div>
  );
  MockMaterialIcon.displayName = "MockMaterialIcon";

  return MockMaterialIcon;
});

jest.mock("@/components/icons/TokenAutoIcon", () => {
  const MockTokenAutoIcon = () => <svg data-testid="token-auto-icon" />;
  MockTokenAutoIcon.displayName = "MockTokenAutoIcon";

  return MockTokenAutoIcon;
});

jest.mock("@/utils/helpers", () => ({
  convertMsToS: (ms: number) => `${ms / 1000}s`,
}));

const renderWithMantine = (ui: React.ReactNode) =>
  render(<MantineProvider>{ui}</MantineProvider>);

describe("AverageAnalytics", () => {
  const defaultProps = {
    label: "Test Label",
    time: 1500,
    prompts: 10,
    completed: 20,
    total: 30,
    isSecondFilterOn: false,
  };

  it("renders label and average latency when isSecondFilterOn is false", () => {
    renderWithMantine(<AverageAnalytics {...defaultProps} />);

    expect(screen.getByText("Test Label")).toBeInTheDocument();
    expect(screen.getByText("1.5s")).toBeInTheDocument();
    expect(screen.getByTestId("material-icon")).toHaveTextContent(
      "timer - Average latency",
    );
    expect(screen.getByTestId("token-auto-icon")).toBeInTheDocument();
    expect(
      screen.getByText(/Input: 10 • Output: 20 • Total: 30/),
    ).toBeInTheDocument();
  });

  it("does not render average latency and tokens section when isSecondFilterOn is true", () => {
    renderWithMantine(
      <AverageAnalytics {...defaultProps} isSecondFilterOn={true} />,
    );

    expect(screen.getByText("Test Label")).toBeInTheDocument();
    expect(screen.queryByText("1.5s")).not.toBeInTheDocument();
    expect(screen.queryByTestId("material-icon")).not.toBeInTheDocument();
    expect(screen.queryByTestId("token-auto-icon")).not.toBeInTheDocument();
    expect(screen.queryByText(/Input:/)).not.toBeInTheDocument();
  });
});
