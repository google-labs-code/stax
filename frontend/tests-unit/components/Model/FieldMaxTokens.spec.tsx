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

import FieldMaxTokens from "@/components/Model/FieldMaxTokens";
import { ModelDescriptors } from "@/queries/types";
import { testRenderLite } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

describe("FieldMaxTokens", () => {
  const getMockForm = (initialValue = 100, error = "") => ({
    getInputProps: () => ({}),
    values: { maxTokens: initialValue },
    errors: { maxTokens: error },
    setFieldValue: jest.fn(),
    setFieldError: jest.fn(),
    clearFieldError: jest.fn(),
  });

  const descriptors: ModelDescriptors = {
    max_output_tokens: {
      description: "Max tokens allowed",
      minValue: 50,
      maxValue: 300,
      defaultValue: 0,
      label: "",
      type: "",
      comparableMaxValue: 0,
      comparableMinValue: 0,
      key: "",
    },
  };

  it("renders correctly with descriptors", () => {
    const form = getMockForm();
    testRenderLite(<FieldMaxTokens form={form} descriptors={descriptors} />);

    expect(screen.getByText("Max tokens")).toBeInTheDocument();
    expect(screen.getByRole("textbox")).toBeInTheDocument();
  });

  it("renders without descriptors", () => {
    const form = getMockForm();
    testRenderLite(<FieldMaxTokens form={form} />);

    expect(screen.getByText("Max tokens")).toBeInTheDocument();
  });

  it("calls setFieldValue and clears error on valid input", () => {
    const form = getMockForm();
    testRenderLite(<FieldMaxTokens form={form} descriptors={descriptors} />);

    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "150" } });

    expect(form.setFieldValue).toHaveBeenCalledWith("maxTokens", 150);
    expect(form.clearFieldError).toHaveBeenCalledWith("maxTokens");
  });

  it("sets error on invalid input (below min)", () => {
    const form = getMockForm();
    testRenderLite(<FieldMaxTokens form={form} descriptors={descriptors} />);

    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "10" } });

    expect(form.setFieldError).toHaveBeenCalledWith("maxTokens", "error");
  });

  it("sets error on empty input", () => {
    const form = getMockForm();
    testRenderLite(<FieldMaxTokens form={form} descriptors={descriptors} />);

    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "" } });

    expect(form.setFieldError).toHaveBeenCalledWith("maxTokens", "error");
  });
});
