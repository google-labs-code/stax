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

import InputMessageBoxAttachment from "@/app/(authRoutes)/projects/[id]/playground/components/Input/InputMessageBoxAttachment";
import { testRender } from "@/tests-unit/render";
import { InferenceChatCompletionPromptRole } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

const mockSetInputs = jest.fn();
const mockSetIsPlaygroundModified = jest.fn();

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: () => ({
    setInputs: mockSetInputs,
    setIsPlaygroundModified: mockSetIsPlaygroundModified,
    setIsNewChat: jest.fn(),
  }),
}));

beforeAll(() => {
  global.URL.createObjectURL = jest.fn(() => "blob:http://localhost/image");
});

afterEach(() => {
  mockSetInputs.mockClear();
});

describe("InputMessageBoxAttachment", () => {
  const baseProps = {
    id: "test-id",
    role: InferenceChatCompletionPromptRole.USER,
    attachment: null,
  };

  it("renders attach button for USER role and no attachment", () => {
    testRender(<InputMessageBoxAttachment {...baseProps} />);
    expect(screen.getByText("Attach")).toBeInTheDocument();
  });

  it("does not render attach button for ASSISTANT role", () => {
    testRender(
      <InputMessageBoxAttachment
        {...baseProps}
        role={InferenceChatCompletionPromptRole.ASSISTANT}
      />,
    );
    expect(screen.queryByText("Attach")).not.toBeInTheDocument();
  });

  it("renders image thumbnail when attachment is image", () => {
    const imageFile = new File(["dummy"], "image.png", { type: "image/png" });

    testRender(
      <InputMessageBoxAttachment {...baseProps} attachment={imageFile} />,
    );

    const img = screen.getByRole("img");
    expect(img).toHaveAttribute("alt", "image.png");
  });

  it("renders PDF thumbnail when attachment is PDF", () => {
    const pdfFile = new File(["%PDF-1.4"], "doc.pdf", {
      type: "application/pdf",
    });

    testRender(
      <InputMessageBoxAttachment {...baseProps} attachment={pdfFile} />,
    );

    expect(screen.getByText("doc.pdf")).toBeInTheDocument();
    expect(screen.getByText("PDF")).toBeInTheDocument();
  });

  it("calls setInputs to remove attachment when delete button is clicked", async () => {
    const imageFile = new File(["dummy"], "image.png", { type: "image/png" });

    testRender(
      <InputMessageBoxAttachment {...baseProps} attachment={imageFile} />,
    );

    const deleteButton = screen.getByRole("button");

    fireEvent.click(deleteButton);

    await waitFor(() => {
      expect(mockSetInputs).toHaveBeenCalledTimes(1);
      const callback = mockSetInputs.mock.calls[0][0];
      const updatedInputs = callback([
        { id: "test-id", attachment: imageFile, modified: false },
      ]);
      expect(updatedInputs[0].attachment).toBeNull();
      expect(mockSetIsPlaygroundModified).toHaveBeenCalledWith(true);
    });
  });
});
