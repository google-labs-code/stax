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

import InputRightResetButton from "@/components/Model/InputRightResetButton";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

const mockSetFieldValue = jest.fn();

const getForm = (errors: any = {}, setFieldValue = mockSetFieldValue) => ({
  errors,
  setFieldValue,
});

describe("InputRightResetButton", () => {
  const descriptors = {
    temperature: {
      defaultValue: "default-value",
    },
  };

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("should not render if there is no error for the field", () => {
    testRender(
      <InputRightResetButton
        form={getForm({})}
        descriptors={descriptors as any}
        descriptorKey="temperature"
        fieldName="field1"
      />,
    );
    expect(screen.queryByText("Reset")).toBeNull();
  });

  it("should not render if there is no defaultValue in descriptors", () => {
    testRender(
      <InputRightResetButton
        form={getForm({ field1: "Some error" })}
        descriptors={{ temperature: {} } as any}
        descriptorKey="temperature"
        fieldName="field1"
      />,
    );
    expect(screen.queryByText("Reset")).toBeNull();
  });

  it("should render and call setFieldValue on click", () => {
    testRender(
      <InputRightResetButton
        form={getForm({ field1: "Some error" })}
        descriptors={descriptors as any}
        descriptorKey="temperature"
        fieldName="field1"
      />,
    );
    const button = screen.getByText("Reset");
    expect(button).toBeInTheDocument();
    fireEvent.click(button);
    expect(mockSetFieldValue).toHaveBeenCalledWith("field1", "default-value");
  });

  it("should not render if defaultValue is undefined", () => {
    testRender(
      <InputRightResetButton
        form={getForm({ field1: "Some error" })}
        descriptors={{ temperature: { defaultValue: undefined } } as any}
        descriptorKey="temperature"
        fieldName="field1"
      />,
    );
    expect(screen.queryByText("Reset")).toBeNull();
  });

  it("should not render if there is no error even if defaultValue exists", () => {
    testRender(
      <InputRightResetButton
        form={getForm({})}
        descriptors={descriptors as any}
        descriptorKey="temperature"
        fieldName="field1"
      />,
    );
    expect(screen.queryByText("Reset")).toBeNull();
  });

  it("should not call setFieldValue if defaultValue is falsy", () => {
    testRender(
      <InputRightResetButton
        form={getForm({ field1: "Error" })}
        descriptors={{ temperature: { defaultValue: undefined } } as any}
        descriptorKey="temperature"
        fieldName="field1"
      />,
    );
    expect(screen.queryByText("Reset")).toBeNull();
  });
});
