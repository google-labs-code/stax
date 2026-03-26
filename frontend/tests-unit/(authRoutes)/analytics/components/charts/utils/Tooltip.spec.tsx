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

import Tooltip from "@/app/(authRoutes)/analytics/components/charts/utils/Tooltip";
import { MantineProvider } from "@mantine/core";
import { render, screen } from "@testing-library/react";

describe("Tooltip", () => {
  it("renders tooltip content", () => {
    const content = [
      { label: "Label", value: "A" },
      { label: "Score", value: "0.95" },
    ];

    function renderWithMantine(children: React.ReactNode) {
      return render(<MantineProvider>{children}</MantineProvider>);
    }

    renderWithMantine(
      <Tooltip position={{ x: 100, y: 100 }} content={content} />,
    );

    expect(screen.getByText("Label")).toBeInTheDocument();
    expect(screen.getByText("A")).toBeInTheDocument();
    expect(screen.getByText("Score")).toBeInTheDocument();
    expect(screen.getByText("0.95")).toBeInTheDocument();
  });
});
