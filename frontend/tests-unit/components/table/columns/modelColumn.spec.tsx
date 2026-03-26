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

import { modelColumn } from "@/components/table/columns/modelColumn";
import { useModelsContext } from "@/hooks/useModelsContext";
import { testRenderLite } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

const GeminiIcon = ({ size = 16 }: { size?: number }) => (
  <svg data-testid="gemini-icon" width={size} height={size} />
);

describe("modelColumn.Cell", () => {
  const modelLabel = "Gemini";

  beforeEach(() => {
    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: [
        {
          label: modelLabel,
          icon: GeminiIcon,
        },
      ],
    });
  });

  it("renders model label and icon if model is found", () => {
    const row = {
      original: {
        model_label: modelLabel,
      },
    };

    const Cell = modelColumn.Cell({ row });

    testRenderLite(<>{Cell}</>);

    expect(screen.getByText(modelLabel)).toBeInTheDocument();
    expect(screen.getByTestId("gemini-icon")).toBeInTheDocument();
  });

  it("renders nothing if model is not found", () => {
    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: [],
    });

    const row = {
      original: {
        model_label: "Unknown Model",
      },
    };

    const Cell = modelColumn.Cell({ row });
    testRenderLite(<div data-testid="cell-wrapper">{Cell}</div>);
    expect(screen.getByTestId("cell-wrapper")).toBeEmptyDOMElement();
  });
});

describe("modelColumn.Header", () => {
  it("renders the ColumnHeader with tooltip", () => {
    const mockProps = {
      column: { columnDef: { header: "Model nickname" } },
      table: {},
    };
    const Header = modelColumn.Header(mockProps);

    const { getByText } = testRenderLite(<>{Header}</>);
    expect(getByText("Model nickname")).toBeInTheDocument();
  });
});
