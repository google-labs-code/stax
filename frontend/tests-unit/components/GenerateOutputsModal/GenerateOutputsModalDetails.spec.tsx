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

import GenerateOutputsModalDetails from "@/components/GenerateOutputsModal/GenerateOutputsModalDetails";
import { Model } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { Provider } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

describe("GenerateOutputsModalDetails", () => {
  const baseModel: Model = {
    id: "model-1",
    label: "Model One",
    provider: Provider.GOOGLE,
    icon: () => <div data-testid="icon" />,
    properties: {
      temperature: 0.7,
      max_tokens: 1000,
      top_p: 0.9,
      seed: 42,
    },
    name: "",
    version: "",
    url: "",
    tag: "",
    model_type: "",
    additional_headers: {},
  };

  it("renders children and shows model config on hover", async () => {
    testRender(
      <GenerateOutputsModalDetails model={baseModel}>
        <button>Hover</button>
      </GenerateOutputsModalDetails>,
    );

    expect(screen.getByText("Hover")).toBeInTheDocument();

    fireEvent.mouseOver(screen.getByText("Hover"));

    await waitFor(() => {
      expect(screen.getByText("Model Configurations")).toBeInTheDocument();
    });

    expect(screen.getByText("Temperature:")).toBeInTheDocument();
    expect(screen.getByText("0.7")).toBeInTheDocument();

    expect(screen.getByText("Max Tokens:")).toBeInTheDocument();
    expect(screen.getByText("1000")).toBeInTheDocument();

    expect(screen.getByText("Top P:")).toBeInTheDocument();
    expect(screen.getByText("0.9")).toBeInTheDocument();

    expect(screen.getByText("Seed:")).toBeInTheDocument();
    expect(screen.getByText("42")).toBeInTheDocument();
  });

  it("does not render missing fields", async () => {
    const modelWithoutSomeProps = {
      ...baseModel,
      properties: {
        temperature: 0.5,
        max_tokens: undefined,
        top_p: undefined,
        seed: undefined,
      },
    };

    testRender(
      <GenerateOutputsModalDetails model={modelWithoutSomeProps}>
        <div>Hover trigger</div>
      </GenerateOutputsModalDetails>,
    );

    fireEvent.mouseOver(screen.getByText("Hover trigger"));

    await waitFor(() => {
      expect(screen.getByText("Model Configurations")).toBeInTheDocument();
    });

    expect(screen.getByText("Temperature:")).toBeInTheDocument();
    expect(screen.queryByText("Max Tokens:")).not.toBeInTheDocument();
    expect(screen.queryByText("Top P:")).not.toBeInTheDocument();
    expect(screen.queryByText("Seed:")).not.toBeInTheDocument();
  });

  it("shows fallback message if no properties exist", async () => {
    const modelWithNoProps: Model = {
      ...baseModel,
      properties: {} as any,
    };

    testRender(
      <GenerateOutputsModalDetails model={modelWithNoProps}>
        <div>Hover again</div>
      </GenerateOutputsModalDetails>,
    );

    fireEvent.mouseOver(screen.getByText("Hover again"));

    await waitFor(() => {
      expect(screen.getByText("Model Configurations")).toBeInTheDocument();
    });
  });
});
