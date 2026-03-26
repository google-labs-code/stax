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

import { render, screen } from "@testing-library/react";
import React from "react";

import DFTOSPage from "../../../../app/(unauthRoutes)/tos/df/page";

// Mock Mantine Card to avoid style issues
jest.mock("@mantine/core", () => ({
  Card: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

describe("DFTOSPage", () => {
  it("renders the Terms of Service heading", () => {
    render(<DFTOSPage />);
    expect(screen.getByText(/Terms of Service/i)).toBeInTheDocument();
  });

  it("renders the Google Employee Privacy Policy link", () => {
    render(<DFTOSPage />);
    const link = screen.getByRole("link", {
      name: /Google's Employee Privacy Policy/i,
    });
    expect(link).toHaveAttribute("href", "http://go/epp");
  });

  it("renders the Using GenAI Internally policy link", () => {
    render(<DFTOSPage />);
    const link = screen.getByRole("link", {
      name: /Using GenAI Internally policy/i,
    });
    expect(link).toHaveAttribute("href", "http://go/using-genai-internally");
  });

  it("renders the contact email link", () => {
    render(<DFTOSPage />);
    const link = screen.getByRole("link", { name: /stax@google.com/i });
    expect(link).toHaveAttribute("href", "mailto:stax@google.com");
  });
});
