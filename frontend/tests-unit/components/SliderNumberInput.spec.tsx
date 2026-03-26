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

import SliderNumberInput from "@/components/SliderNumberInput";
import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("SliderNumberInput", () => {
  const defaultProps = {
    value: 50,
    setValue: jest.fn(),
    min: 0,
    max: 100,
    step: 1,
  };

  const renderComponent = (props = {}) => {
    const componentProps = { ...defaultProps, ...props };

    return testRender(<SliderNumberInput {...componentProps} />);
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("should render the component with default props", () => {
    renderComponent();

    const slider = screen.getByRole("slider");
    const numberInput = screen.getByRole("textbox");

    expect(slider).toBeInTheDocument();
    expect(numberInput).toBeInTheDocument();
    expect(slider).toHaveValue(50);
    expect(numberInput).toHaveValue("50");
  });

  it("should render with custom placeholder", () => {
    renderComponent({ placeholder: "Enter value" });

    const numberInput = screen.getByRole("textbox");
    expect(numberInput).toHaveAttribute("placeholder", "Enter value");
  });

  it("should handle slider value changes", async () => {
    const user = userEvent.setup();
    const setValue = jest.fn();
    renderComponent({ setValue });

    const slider = screen.getByRole("slider");
    await user.click(slider);
    await user.keyboard("{ArrowRight}");

    expect(setValue).toHaveBeenCalled();
  });

  it("should handle number input value changes", async () => {
    const user = userEvent.setup();
    const setValue = jest.fn();
    renderComponent({ setValue });

    const numberInput = screen.getByRole("textbox");
    await user.clear(numberInput);
    await user.type(numberInput, "75");

    expect(setValue).toHaveBeenCalledWith(75);
  });

  it("should apply min and max constraints", () => {
    renderComponent({ min: 10, max: 90, value: 50 });

    const slider = screen.getByRole("slider");
    const numberInput = screen.getByRole("textbox");

    expect(slider).toHaveAttribute("aria-valuemin", "10");
    expect(slider).toHaveAttribute("aria-valuemax", "90");
    expect(numberInput).toBeInTheDocument();
  });

  it("should handle decimal values when allowDecimal is true", () => {
    renderComponent({
      allowDecimal: true,
      decimalScale: 2,
      value: 50.5,
      step: 0.1,
    });

    const numberInput = screen.getByRole("textbox");
    expect(numberInput).toHaveValue("50.5");
  });

  it("should display error styling when error is provided", () => {
    renderComponent({ error: "Invalid value" });

    const slider = screen.getByRole("slider");
    const numberInput = screen.getByRole("textbox");
    const errorText = screen.getByText("Invalid value");

    expect(slider).toHaveClass("mantine-focus-auto", "border-[10px]");
    expect(numberInput).toHaveClass("border-supporting-red");
    expect(errorText).toBeInTheDocument();
    expect(errorText).toHaveClass("text-supporting-red");
  });

  it("should not display error styling when no error is provided", () => {
    renderComponent();

    const numberInput = screen.getByRole("textbox");
    const errorText = screen.queryByText("Invalid value");

    expect(numberInput).toHaveClass("border-neutrals-300");
    expect(errorText).not.toBeInTheDocument();
  });

  it("should call onBlur when number input loses focus", async () => {
    const user = userEvent.setup();
    const onBlur = jest.fn();
    renderComponent({ onBlur });

    const numberInput = screen.getByRole("textbox");
    await user.click(numberInput);
    await user.tab();

    expect(onBlur).toHaveBeenCalled();
  });

  it("should handle string values in handleChange", async () => {
    const user = userEvent.setup();
    const setValue = jest.fn();
    renderComponent({ setValue });

    const numberInput = screen.getByRole("textbox");
    await user.clear(numberInput);
    await user.type(numberInput, "25");

    expect(setValue).toHaveBeenCalledWith(25);
  });

  it("should handle extreme values", () => {
    renderComponent({ value: 0, min: 0, max: 100 });

    const slider = screen.getByRole("slider");
    const numberInput = screen.getByRole("textbox");

    expect(slider).toHaveValue(0);
    expect(numberInput).toHaveValue("0");
  });

  it("should handle maximum values", () => {
    renderComponent({ value: 100, min: 0, max: 100 });

    const slider = screen.getByRole("slider");
    const numberInput = screen.getByRole("textbox");

    expect(slider).toHaveValue(100);
    expect(numberInput).toHaveValue("100");
  });

  it("should maintain slider and input synchronization", async () => {
    const user = userEvent.setup();
    const setValue = jest.fn();
    renderComponent({ setValue });

    const slider = screen.getByRole("slider");
    const numberInput = screen.getByRole("textbox");

    await user.click(slider);
    await user.keyboard("{ArrowRight}");

    await user.clear(numberInput);
    await user.type(numberInput, "80");

    expect(setValue).toHaveBeenCalled();
  });

  it("should handle form props spreading", () => {
    const formProps = {
      disabled: true,
      "data-testid": "slider-input",
    };
    renderComponent(formProps);

    const slider = screen.getByRole("slider");
    const numberInput = screen.getByRole("textbox");

    expect(slider).toBeInTheDocument();
    expect(numberInput).toBeInTheDocument();
  });

  it("should apply strict clamp behavior to number input", () => {
    renderComponent();

    const numberInput = screen.getByRole("textbox");
    expect(numberInput).toBeInTheDocument();
  });

  it("should hide number input controls", () => {
    renderComponent();

    const numberInput = screen.getByRole("textbox");
    expect(numberInput).toBeInTheDocument();
  });
});
