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

import CustomModelModalAdditionalDetails from "@/app/(authRoutes)/settings/components/CustomModelModalAdditionalDetails";
import { CustomModelPayload } from "@/queries/types";
import { testRenderLite } from "@/tests-unit/render";
import { useForm } from "@mantine/form";
import { fireEvent, screen } from "@testing-library/react";

describe("CustomModelModalAdditionalDetails", () => {
  const initialValues: CustomModelPayload = {
    properties: {
      temperature: 1,
      max_tokens: 10,
      top_p: 0.5,
      seed: 42,
    },
    label: "",
    url: "",
    supported_provider: "",
  };

  function Wrapper({
    isDisabled = false,
    inputClassNames = "test-input",
  }: {
    isDisabled?: boolean;
    inputClassNames?: string;
  }) {
    const form = useForm<CustomModelPayload>({ initialValues });

    return (
      <CustomModelModalAdditionalDetails
        form={form}
        isDisabled={isDisabled}
        inputClassNames={inputClassNames}
      />
    );
  }

  it("renders all fields with correct initial values", () => {
    testRenderLite(<Wrapper />);
    expect(screen.getByText("Temperature")).toBeInTheDocument();
    expect(screen.getByText("Max tokens")).toBeInTheDocument();
    expect(screen.getByText("Top P")).toBeInTheDocument();
    expect(screen.getByText("Seed")).toBeInTheDocument();

    expect(screen.getAllByRole("slider")[0]).toHaveValue(1);
    expect(screen.getByDisplayValue("10")).toBeInTheDocument();
    expect(screen.getAllByRole("slider")[1]).toHaveValue(0.5);
    expect(screen.getByDisplayValue("42")).toBeInTheDocument();
  });

  it("updates max_tokens input value on change", () => {
    testRenderLite(<Wrapper />);
    const input = screen.getByDisplayValue("10");
    fireEvent.change(input, { target: { value: "15" } });
    expect(input).toHaveValue("15");
  });

  it("updates seed input value on change", () => {
    testRenderLite(<Wrapper />);
    const input = screen.getByDisplayValue("42");
    fireEvent.change(input, { target: { value: "100" } });
    expect(input).toHaveValue("100");
  });

  it("applies custom input class to seed input", () => {
    testRenderLite(<Wrapper inputClassNames="custom-seed-class" />);
    const input = screen.getByDisplayValue("42");
    expect(input).toHaveClass("custom-seed-class");
  });
});
