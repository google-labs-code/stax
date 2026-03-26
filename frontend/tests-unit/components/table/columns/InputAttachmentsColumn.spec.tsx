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

import { inputAttachmentsColumn } from "@/components/table/columns/inputAttachmentsColumn";
import { testRenderLite } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

jest.mock("@/app/(authRoutes)/projects/[id]/components/ColumnHeader", () => {
  const MockColumnHeader = ({ tooltip }: { tooltip: string }) => (
    <div data-testid="mock-column-header">{tooltip}</div>
  );

  return MockColumnHeader;
});

jest.mock("@/components/MaterialIcon", () => {
  const MockMaterialIcon = ({ name }: { name: string }) => (
    <div data-testid="mock-material-icon">{name}</div>
  );

  return MockMaterialIcon;
});

describe("inputAttachmentsColumn.Cell", () => {
  it("renders image with the attachment URL if available", () => {
    const row = {
      original: {
        input_attachments: [{ url: "https://example.com/image.png" }],
      },
    };

    const Cell = inputAttachmentsColumn.Cell({ row });

    testRenderLite(<div data-testid="cell-wrapper">{Cell}</div>);

    const img = screen.getByAltText("Image attachment");
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute("src", "https://example.com/image.png");

    const materialIcon = screen.getByTestId("mock-material-icon");
    expect(materialIcon).toBeInTheDocument();
    expect(materialIcon).toHaveTextContent("cloud_upload");
  });
});

describe("inputAttachmentsColumn Header", () => {
  it("renders the ColumnHeader with tooltip", () => {
    const mockProps = {
      column: { columnDef: { header: "Input Attachments" } },
      table: {},
    };

    const Header = inputAttachmentsColumn.Header(mockProps);

    testRenderLite(<div data-testid="header-wrapper">{Header}</div>);

    const tooltip = screen.getByTestId("mock-column-header");
    expect(tooltip).toBeInTheDocument();
    expect(tooltip).toHaveTextContent("Images attached to this input");
  });
});
