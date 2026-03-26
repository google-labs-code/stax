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

import JsonCodeMirror from "@/components/JsonCodeMirror";
import { fireEvent, render, screen } from "@testing-library/react";

interface MockCodeMirrorProps {
  value: string;
  onChange?: (value: string) => void;
  className?: string;
  readOnly?: boolean;
  height?: string;
}

jest.mock("@uiw/react-codemirror", () => {
  const MockCodeMirror = ({
    value,
    onChange,
    className,
    height,
  }: MockCodeMirrorProps) => {
    return (
      <div
        className={className}
        data-testid="codemirror-mock"
        style={{ height }}
      >
        <textarea
          value={value}
          onChange={(e) => onChange?.(e.target.value)}
          data-testid="codemirror-textarea"
        />
      </div>
    );
  };

  return MockCodeMirror;
});

jest.mock("@codemirror/commands", () => ({
  indentWithTab: jest.fn(),
}));
jest.mock("@codemirror/lang-json", () => ({
  json: jest.fn(),
}));
jest.mock("@codemirror/language", () => ({
  HighlightStyle: {
    define: jest.fn(),
  },
  syntaxHighlighting: jest.fn(),
}));
jest.mock("@codemirror/view", () => ({
  EditorView: {
    lineWrapping: jest.fn(),
    editable: {
      of: jest.fn(),
    },
  },
  keymap: {
    of: jest.fn(),
  },
}));
jest.mock("@lezer/highlight", () => ({
  tags: {},
}));

describe("JsonCodeMirror", () => {
  it("renders with default props", () => {
    render(<JsonCodeMirror value='{"test": "value"}' />);

    const codeMirror = screen.getByTestId("codemirror-mock");
    expect(codeMirror).toBeInTheDocument();
    expect(codeMirror).toHaveClass("w-full");
  });

  it("applies custom classes", () => {
    render(
      <JsonCodeMirror value='{"test": "value"}' className="custom-class" />,
    );

    const codeMirror = screen.getByTestId("codemirror-mock");
    expect(codeMirror).toHaveClass("custom-class");
  });

  it("adds error class when error is provided", () => {
    render(<JsonCodeMirror value='{"test": "value"}' error="Invalid JSON" />);

    const codeMirror = screen.getByTestId("codemirror-mock");
    expect(codeMirror).toHaveClass("!border-red");
  });

  it("applies custom height", () => {
    render(<JsonCodeMirror value='{"test": "value"}' height="500px" />);

    const codeMirror = screen.getByTestId("codemirror-mock");
    expect(codeMirror.style.height).toBe("500px");
  });

  it("handles onChange events", () => {
    const handleChange = jest.fn();
    render(
      <JsonCodeMirror value='{"test": "value"}' onChange={handleChange} />,
    );

    const textarea = screen.getByTestId("codemirror-textarea");
    fireEvent.change(textarea, { target: { value: '{"updated": "data"}' } });

    expect(handleChange).toHaveBeenCalledWith('{"updated": "data"}');
  });
});
