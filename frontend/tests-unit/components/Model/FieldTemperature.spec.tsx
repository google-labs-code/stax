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

import FieldTemperature from "@/components/Model/FieldTemperature";
import { ModelDescriptors } from "@/queries/types";
import { testRenderLite } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

describe("FieldTemperature", () => {
  const getMockForm = (initialValue: number | undefined = 0.7) => ({
    getInputProps: () => ({}),
    values: { temperature: initialValue },
    setFieldValue: jest.fn(),
  });

  const descriptors: ModelDescriptors = {
    temperature: {
      description: "Controls randomness in token sampling",
      minValue: 0,
      maxValue: 2,
      defaultValue: 1,
      label: "",
      type: "",
      comparableMaxValue: 0,
      comparableMinValue: 0,
      key: "",
    },
  };

  it("renders correctly with descriptors", () => {
    const form = getMockForm();
    testRenderLite(<FieldTemperature form={form} descriptors={descriptors} />);

    expect(screen.getByText("Temperature")).toBeInTheDocument();
    expect(screen.getByRole("textbox")).toBeInTheDocument();
  });

  it("renders correctly without descriptors", () => {
    const form = getMockForm();
    testRenderLite(<FieldTemperature form={form} />);

    expect(screen.getByText("Temperature")).toBeInTheDocument();
  });

  it("calls setFieldValue when input changes", () => {
    const form = getMockForm();
    testRenderLite(<FieldTemperature form={form} descriptors={descriptors} />);

    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "1.25" } });

    expect(form.setFieldValue).toHaveBeenCalledWith("temperature", 1.25);
  });

  it("sets default value on blur when input is cleared", () => {
    const form = getMockForm(0.9);
    testRenderLite(<FieldTemperature form={form} descriptors={descriptors} />);

    const input = screen.getByRole("textbox");
    fireEvent.change(input, { target: { value: "" } });
    fireEvent.blur(input);

    expect(form.setFieldValue).toHaveBeenCalledWith("temperature", "");
  });

  it("respects disabled prop", () => {
    const form = getMockForm();
    testRenderLite(
      <FieldTemperature
        form={form}
        descriptors={descriptors}
        disabled={true}
      />,
    );

    const input = screen.getByRole("textbox");
    expect(input).toBeDisabled();
  });
});
