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

import BadgeWithIcon from "@/components/BadgeWithIcon";
import { FloatingPosition } from "@mantine/core";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../render";

jest.mock("@/components/MaterialIcon", () => ({
  __esModule: true,
  default: ({
    name,
    className,
    tooltipLabel,
    tooltipPosition,
    disabled,
    size,
  }: {
    name: string;
    className?: string;
    tooltipLabel?: string;
    tooltipPosition?: FloatingPosition;
    disabled?: boolean;
    size?: number;
  }) => (
    <span
      data-testid="material-icon-mock"
      className={className}
      data-name={name}
      data-tooltip-label={tooltipLabel}
      data-tooltip-position={tooltipPosition}
      data-disabled={disabled ? "true" : "false"}
      data-size={size}
    />
  ),
}));

describe("BadgeWithIcon", () => {
  it("renders with required props", () => {
    testRender(<BadgeWithIcon title="Test Badge" />);
    expect(screen.getByText("Test Badge")).toBeInTheDocument();
  });

  it("renders icon before text by default", () => {
    testRender(<BadgeWithIcon title="Test Badge" icon="settings" />);
    const icon = screen.getByTestId("material-icon-mock");
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveAttribute("data-name", "settings");
    expect(icon).toHaveClass("mr-2");
  });

  it("renders icon after text when iconPosition is 'after'", () => {
    testRender(
      <BadgeWithIcon title="Test Badge" icon="settings" iconPosition="after" />,
    );
    const icon = screen.getByTestId("material-icon-mock");
    expect(icon).toHaveClass("ml-2");
  });

  it("applies custom class names", () => {
    testRender(<BadgeWithIcon title="Test Badge" className="test-class" />);
    const badge = screen.getByTestId("badge-with-icon");
    expect(badge).toHaveClass("test-class");
  });

  it("applies custom icon class names", () => {
    testRender(
      <BadgeWithIcon
        title="Test Badge"
        icon="settings"
        iconClassName="icon-test-class"
      />,
    );
    const icon = screen.getByTestId("material-icon-mock");
    expect(icon).toHaveClass("icon-test-class");
  });

  it("handles click events", () => {
    const handleClick = jest.fn();
    testRender(<BadgeWithIcon title="Test Badge" onClick={handleClick} />);
    const badge = screen.getByTestId("badge-with-icon");
    fireEvent.click(badge);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it("doesn't call onClick when disabled", () => {
    const handleClick = jest.fn();
    testRender(
      <BadgeWithIcon title="Test Badge" onClick={handleClick} disabled />,
    );
    const badge = screen.getByTestId("badge-with-icon");
    expect(badge).toHaveClass("opacity-50");
    fireEvent.click(badge);
    expect(handleClick).not.toHaveBeenCalled();
  });

  it("renders with custom data-testid", () => {
    testRender(
      <BadgeWithIcon title="Test Badge" dataTestId="custom-test-id" />,
    );
    expect(screen.getByTestId("custom-test-id")).toBeInTheDocument();
  });

  it("passes tooltip props to the icon", () => {
    testRender(
      <BadgeWithIcon
        title="Test Badge"
        icon="settings"
        tooltipLabel="Tooltip Text"
        tooltipPosition="bottom"
      />,
    );
    const icon = screen.getByTestId("material-icon-mock");
    expect(icon).toHaveAttribute("data-tooltip-label", "Tooltip Text");
    expect(icon).toHaveAttribute("data-tooltip-position", "bottom");
  });

  it("renders in default variant with fixed width", () => {
    testRender(<BadgeWithIcon title="Test Badge" />);
    const badge = screen.getByTestId("badge-with-icon");
    expect(badge).toHaveClass("w-[188.95px]");
  });

  it("renders in inline variant without fixed width", () => {
    testRender(<BadgeWithIcon title="Test Badge" variant="inline" />);
    const badge = screen.getByTestId("badge-with-icon");
    expect(badge).not.toHaveClass("w-[188.95px]");
  });
});
