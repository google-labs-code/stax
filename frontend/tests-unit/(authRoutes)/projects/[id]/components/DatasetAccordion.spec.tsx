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

import DatasetAccordion from "@/app/(authRoutes)/projects/[id]/components/CreateDataset/Accordion";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen, waitFor } from "@testing-library/react";

describe("DatasetAccordion", () => {
  it("renders the accordion header with correct label and tooltip", () => {
    testRender(<DatasetAccordion />);
    expect(screen.getByText("Define Variables (optional)")).toBeInTheDocument();
    // Tooltip is rendered on hover, but the info icon should be present
    expect(screen.getAllByTestId("material-icon").length).toBeGreaterThan(0);
  });

  it("renders the textarea with correct placeholder", () => {
    testRender(<DatasetAccordion />);
    const textarea = screen.getByPlaceholderText(
      /Example format to define variables/,
    );
    expect(textarea).toBeInTheDocument();
  });

  it("renders the Upload CSV button with icon", async () => {
    testRender(<DatasetAccordion />);
    const header = screen.getByText("Define Variables (optional)");
    fireEvent.click(header); // Expand the panel so the button is visible
    const button = await screen.findByRole("button", { name: /upload csv/i });
    expect(button).toBeInTheDocument();
    // The icon is a MaterialIcon, which renders as a <span> inside the button
    expect(
      button.querySelector('[data-testid="material-icon"]'),
    ).toBeInTheDocument();
  });

  it("expands and collapses the accordion panel on header click", async () => {
    testRender(<DatasetAccordion />);
    const header = screen.getByText("Define Variables (optional)");
    let textarea = screen.getByPlaceholderText(
      /Example format to define variables/,
    );
    expect(textarea).toBeInTheDocument();
    // The accordion panel should be collapsed initially (aria-hidden="true")
    const accordionPanel = textarea.closest(".mantine-Accordion-panel");
    expect(accordionPanel).toHaveAttribute("aria-hidden", "true");
    // Click to expand
    fireEvent.click(header);
    textarea = screen.getByPlaceholderText(
      /Example format to define variables/,
    );
    await waitFor(() => {
      expect(textarea.closest(".mantine-Accordion-panel")).toBeVisible();
    });
    // Click again to collapse
    fireEvent.click(header);
    textarea = screen.getByPlaceholderText(
      /Example format to define variables/,
    );
    await waitFor(() => {
      expect(textarea.closest(".mantine-Accordion-panel")).toHaveAttribute(
        "aria-hidden",
        "true",
      );
    });
  });
});
