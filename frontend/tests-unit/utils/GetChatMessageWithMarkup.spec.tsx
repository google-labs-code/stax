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

import GetChatMessageWithMarkup from "@/utils/GetChatMessageWithMarkup";
import { render, screen } from "@testing-library/react";

import { testRender } from "../render";

describe("GetChatMessageWithMarkup", () => {
  it("renders plain paragraph content", () => {
    render(<GetChatMessageWithMarkup message="Hello, world!" />);
    expect(screen.getByText("Hello, world!")).toBeInTheDocument();
  });

  it("renders unordered list", () => {
    render(<GetChatMessageWithMarkup message={"- Item 1\n- Item 2"} />);
    expect(screen.getByText("Item 1")).toBeInTheDocument();
    expect(screen.getByText("Item 2")).toBeInTheDocument();
    expect(screen.getByRole("list")).toBeInTheDocument();
  });

  it("renders ordered list", () => {
    render(<GetChatMessageWithMarkup message={"1. First\n2. Second"} />);
    expect(screen.getByText("First")).toBeInTheDocument();
    expect(screen.getByText("Second")).toBeInTheDocument();
    expect(screen.getByRole("list")).toBeInTheDocument();
  });

  it("renders inline code", () => {
    render(<GetChatMessageWithMarkup message={"This is `inline code`"} />);
    expect(screen.getByText("inline code")).toBeInTheDocument();
  });

  it("renders code block with language", () => {
    testRender(
      <GetChatMessageWithMarkup
        message={`\`\`\`js\nconsole.log("hello");\n\`\`\``}
      />,
    );
    expect(screen.getByText(/Use code with caution/i)).toBeInTheDocument();
    expect(screen.getByText(/console/)).toBeInTheDocument();
  });
});
