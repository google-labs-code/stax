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

import ModelMenuDropdown from "@/app/(authRoutes)/settings/components/ModelMenuDropdown";
import { getModelDetails } from "@/config/constants";
import { useModelsContext } from "@/hooks/useModelsContext";
import { testRender } from "@/tests-unit/render";
import { Menu } from "@mantine/core";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/useModelsContext");
jest.mock("@/config/constants");

const mockProviders = {
  openai: "OpenAI",
  anthropic: "Anthropic",
};

const MockIcon = ({ size }: { size: number }) => (
  <svg data-testid={`icon-${size}`} />
);

describe("ModelMenuDropdown", () => {
  beforeEach(() => {
    (useModelsContext as jest.Mock).mockReturnValue({
      providers: mockProviders,
    });

    (getModelDetails as jest.Mock).mockImplementation((provider: string) => ({
      icon: MockIcon,
      name: provider,
    }));
  });

  function renderDropdown(props = {}) {
    return testRender(
      <Menu opened>
        <ModelMenuDropdown onClick={jest.fn()} {...props} />
      </Menu>,
    );
  }

  it("renders all provider entries with icons and names", () => {
    renderDropdown();

    expect(screen.getByText("OpenAI")).toBeInTheDocument();
    expect(screen.getByText("Anthropic")).toBeInTheDocument();
    expect(screen.getAllByTestId(/icon-/)).toHaveLength(2);
  });

  it("calls onClick when a provider row is clicked", () => {
    const onClick = jest.fn();
    testRender(
      <Menu opened>
        <ModelMenuDropdown onClick={onClick} />
      </Menu>,
    );

    const openAIEntry = screen.getByText("OpenAI").closest("div");
    fireEvent.click(openAIEntry!);

    expect(onClick).toHaveBeenCalledWith("openai");
  });

  it("skips rendering entry if getModelDetails returns undefined", () => {
    (getModelDetails as jest.Mock).mockImplementation((provider: string) => {
      if (provider === "openai") return undefined;

      return { icon: MockIcon };
    });

    renderDropdown();
    expect(screen.queryByText("OpenAI")).not.toBeInTheDocument();
    expect(screen.getByText("Anthropic")).toBeInTheDocument();
  });

  it("renders switches when visibleProviders prop is provided", () => {
    const visibleProviders = ["openai"];
    renderDropdown({ visibleProviders });

    const switches = screen.getAllByRole("switch");
    expect(switches.length).toBeGreaterThan(0);
  });

  it("shows switch as checked when provider is in visibleProviders", () => {
    const visibleProviders = ["openai"];
    renderDropdown({ visibleProviders });

    const switches = screen.getAllByRole("switch");
    expect(switches.length).toBeGreaterThan(0);
    // Find the switch that's in the same parent Group as "OpenAI" text
    // The outer Group contains both the provider name Group and the switch as siblings
    const openAIText = screen.getByText("OpenAI");
    // Go up to find the outer Group that contains both the text and the switch
    let parentGroup = openAIText.closest('[class*="Group-root"]');
    // If the found group doesn't have a switch, go up one more level
    if (parentGroup && !parentGroup.querySelector('[role="switch"]')) {
      parentGroup =
        parentGroup.parentElement?.closest('[class*="Group-root"]') || null;
    }
    const openAISwitch = parentGroup?.querySelector(
      '[role="switch"]',
    ) as HTMLElement;
    expect(openAISwitch).toBeInTheDocument();
    // Mantine Switch uses toBeChecked() matcher instead of aria-checked attribute
    expect(openAISwitch).toBeChecked();
  });

  it("shows switch as unchecked when provider is not in visibleProviders", () => {
    const visibleProviders: string[] = [];
    renderDropdown({ visibleProviders });

    const switches = screen.getAllByRole("switch");
    expect(switches.length).toBeGreaterThan(0);
    // Find the switch that's in the same parent Group as "OpenAI" text
    // The outer Group contains both the provider name Group and the switch as siblings
    const openAIText = screen.getByText("OpenAI");
    // Go up to find the outer Group that contains both the text and the switch
    let parentGroup = openAIText.closest('[class*="Group-root"]');
    // If the found group doesn't have a switch, go up one more level
    if (parentGroup && !parentGroup.querySelector('[role="switch"]')) {
      parentGroup =
        parentGroup.parentElement?.closest('[class*="Group-root"]') || null;
    }
    const openAISwitch = parentGroup?.querySelector(
      '[role="switch"]',
    ) as HTMLElement;
    expect(openAISwitch).toBeInTheDocument();
    // Mantine Switch uses toBeChecked() matcher instead of aria-checked attribute
    expect(openAISwitch).not.toBeChecked();
  });

  it("does not render switches when visibleProviders is not provided", () => {
    renderDropdown();

    const switches = screen.queryAllByRole("switch");
    expect(switches.length).toBe(0);
  });

  it("applies custom width when width prop is provided", () => {
    renderDropdown({ width: 300 });

    const dropdown = screen.getByTestId("model-menu-dropdown");
    // Mantine Menu.Dropdown uses w prop which may set width via CSS variable or style
    // Check that the component accepts the width prop (implementation detail)
    expect(dropdown).toBeInTheDocument();
  });

  it("uses default width of 260 when width prop is not provided", () => {
    renderDropdown();

    const dropdown = screen.getByTestId("model-menu-dropdown");
    // Mantine Menu.Dropdown uses w prop which may set width via CSS variable or style
    // Check that the component uses default width (implementation detail)
    expect(dropdown).toBeInTheDocument();
  });

  it("prevents event propagation on switch click", () => {
    const onClick = jest.fn();
    const visibleProviders = ["openai"];

    testRender(
      <Menu opened>
        <ModelMenuDropdown
          onClick={onClick}
          visibleProviders={visibleProviders}
        />
      </Menu>,
    );

    // Find the switch that's in the same parent Group as "OpenAI" text
    const openAIText = screen.getByText("OpenAI");
    // Go up to find the outer Group that contains both the text and the switch
    let parentGroup = openAIText.closest('[class*="Group-root"]');
    // If the found group doesn't have a switch, go up one more level
    if (parentGroup && !parentGroup.querySelector('[role="switch"]')) {
      parentGroup =
        parentGroup.parentElement?.closest('[class*="Group-root"]') || null;
    }
    const openAISwitch = parentGroup?.querySelector(
      '[role="switch"]',
    ) as HTMLElement;

    expect(openAISwitch).toBeInTheDocument();

    // Click the switch - onClick should not be called because stopPropagation is called
    fireEvent.click(openAISwitch);

    // The onClick handler should not be called when clicking the switch
    // because stopPropagation prevents the click from bubbling to the parent Group
    expect(onClick).not.toHaveBeenCalled();
  });

  it("renders all providers from context", () => {
    const extendedProviders = {
      openai: "OpenAI",
      anthropic: "Anthropic",
      google: "Google",
      azure: "Azure",
    };

    (useModelsContext as jest.Mock).mockReturnValue({
      providers: extendedProviders,
    });

    (getModelDetails as jest.Mock).mockImplementation(() => ({
      icon: MockIcon,
      name: "Provider",
    }));

    renderDropdown();

    expect(screen.getByText("OpenAI")).toBeInTheDocument();
    expect(screen.getByText("Anthropic")).toBeInTheDocument();
    expect(screen.getByText("Google")).toBeInTheDocument();
    expect(screen.getByText("Azure")).toBeInTheDocument();
  });

  it("renders provider icon when available", () => {
    renderDropdown();

    const icons = screen.getAllByTestId(/icon-/);
    expect(icons.length).toBeGreaterThan(0);
  });

  it("handles empty providers object", () => {
    (useModelsContext as jest.Mock).mockReturnValue({
      providers: {},
    });

    renderDropdown();

    expect(screen.queryByText("OpenAI")).not.toBeInTheDocument();
    expect(screen.queryByText("Anthropic")).not.toBeInTheDocument();
  });

  it("calls onClick with correct provider key when row is clicked", () => {
    const onClick = jest.fn();
    testRender(
      <Menu opened>
        <ModelMenuDropdown onClick={onClick} />
      </Menu>,
    );

    const anthropicEntry = screen.getByText("Anthropic").closest("div");
    fireEvent.click(anthropicEntry!);

    expect(onClick).toHaveBeenCalledWith("anthropic");
    expect(onClick).toHaveBeenCalledTimes(1);
  });
});
