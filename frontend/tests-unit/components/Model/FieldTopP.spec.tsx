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

import FieldTopP from "@/components/Model/FieldTopP";
import { ModelDescriptors } from "@/queries/types";
import { testRenderLite } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

describe("FieldTopP", () => {
  const getMockForm = (initialValue: number | undefined = 0.5) => ({
    getInputProps: () => ({}),
    values: { topP: initialValue },
    setFieldValue: jest.fn(),
  });

  const descriptors: ModelDescriptors = {
    top_p: {
      description: "Controls diversity via nucleus sampling",
      minValue: 0,
      maxValue: 1,
      defaultValue: 0.8,
      label: "",
      type: "",
      comparableMaxValue: 0,
      comparableMinValue: 0,
      key: "",
    },
  };

  it("renders correctly with descriptors", () => {
    const form = getMockForm();
    testRenderLite(<FieldTopP form={form} descriptors={descriptors} />);

    expect(screen.getByText("Top P")).toBeInTheDocument();
    expect(screen.getByRole("textbox")).toBeInTheDocument();
  });

  it("renders without descriptors", () => {
    const form = getMockForm();
    testRenderLite(<FieldTopP form={form} />);
    expect(screen.getByText("Top P")).toBeInTheDocument();
  });

  it("calls setFieldValue when value changes", () => {
    const form = getMockForm();
    testRenderLite(<FieldTopP form={form} descriptors={descriptors} />);

    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "0.7" } });

    expect(form.setFieldValue).toHaveBeenCalledWith("topP", 0.7);
  });

  it("sets default value on blur when input is empty", () => {
    const form = getMockForm(0.5);
    testRenderLite(<FieldTopP form={form} descriptors={descriptors} />);

    const input = screen.getByRole("textbox");

    fireEvent.change(input, { target: { value: "" } });

    fireEvent.blur(input);

    expect(form.setFieldValue).toHaveBeenCalledWith("topP", "");
  });
});
