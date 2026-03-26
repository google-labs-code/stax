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

import CustomModelCard from "@/components/CustomModelCard";
import { Model } from "@/queries/types";
import { Provider } from "@/types";
import "@testing-library/jest-dom";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../render";

const mockModel: Model = {
  id: "1",
  label: "Test Model",
  name: "Gemini",
  provider: Provider.GOOGLE,
  url: "https://example.com",
  api_key: "secret",
  description: "Test model description",
  additional_headers: {},
  properties: {
    temperature: 0.7,
    max_tokens: 512,
    top_p: 1,
    seed: 42,
  },
  version: "",
  tag: "",
  model_type: "",
};

describe("CustomModelCard", () => {
  const onDeleteModel = jest.fn();
  const onShowDetails = jest.fn();
  const onCopyModel = jest.fn();
  const onEdit = jest.fn();

  const renderComponent = () =>
    testRender(
      <CustomModelCard
        model={mockModel}
        onDeleteModel={onDeleteModel}
        onShowDetails={onShowDetails}
        onCopyModel={onCopyModel}
        onEdit={onEdit}
      />,
    );

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders the model label and base model name", () => {
    renderComponent();

    expect(screen.getByText("Test Model")).toBeInTheDocument();
    expect(screen.getByText("Base model:")).toBeInTheDocument();
    expect(screen.getByText("Gemini")).toBeInTheDocument();
  });

  it("renders model properties (temperature and max tokens)", () => {
    renderComponent();

    expect(screen.getByText("0.7")).toBeInTheDocument();
    expect(screen.getByText("512")).toBeInTheDocument();
  });

  it("calls onShowDetails when clicking on card", () => {
    renderComponent();

    const card = screen.getByTestId("model-card");
    fireEvent.click(card);

    expect(onShowDetails).toHaveBeenCalled();
  });

  it("does not trigger onShowDetails when clicking inside dropdown", () => {
    renderComponent();
  });
});
