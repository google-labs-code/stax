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

import { indentWithTab } from "@codemirror/commands";
import { json } from "@codemirror/lang-json";
import { HighlightStyle, syntaxHighlighting } from "@codemirror/language";
import { EditorView } from "@codemirror/view";
import { keymap } from "@codemirror/view";
import { tags } from "@lezer/highlight";
import CodeMirror from "@uiw/react-codemirror";

interface JsonCodeMirrorProps {
  value: string;
  onChange?: (value: string) => void;
  readOnly?: boolean;
  height?: string;
  className?: string;
  error?: string | null;
}

export default function JsonCodeMirror({
  value,
  onChange,
  readOnly = false,
  height = "auto",
  className = "",
  error = null,
}: JsonCodeMirrorProps) {
  const jsonHighlightStyle = HighlightStyle.define([
    {
      tag: tags.string,
      color: "var(--color-secondary-dark)",
      fontFamily: "var(--font-google-sans-code)",
      fontSize: "14px",
    },
    {
      tag: tags.propertyName,
      color: "var(--color-brand)",
      fontFamily: "var(--font-google-sans-code)",
      fontSize: "14px",
    },
  ]);

  return (
    <CodeMirror
      className={`w-full ${error ? "!border-red" : ""} ${className}`}
      value={value}
      height={height}
      extensions={[
        json(),
        ...(readOnly ? [] : [keymap.of([indentWithTab])]),
        EditorView.lineWrapping,
        syntaxHighlighting(jsonHighlightStyle),
        ...(readOnly ? [EditorView.editable.of(false)] : []),
      ]}
      theme="none"
      onChange={onChange}
      basicSetup={{
        lineNumbers: false,
        highlightActiveLineGutter: false,
        highlightSpecialChars: false,
        foldGutter: false,
        drawSelection: false,
        dropCursor: false,
        allowMultipleSelections: false,
        indentOnInput: !readOnly,
        bracketMatching: false,
        closeBrackets: !readOnly,
        autocompletion: !readOnly,
        rectangularSelection: false,
        crosshairCursor: false,
        highlightActiveLine: false,
        highlightSelectionMatches: false,
      }}
    />
  );
}
