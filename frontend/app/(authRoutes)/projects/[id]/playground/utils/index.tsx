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

import {
  Decoration,
  DecorationSet,
  EditorView,
  RangeSetBuilder,
  ViewPlugin,
  ViewUpdate,
} from "@uiw/react-codemirror";
import { useEffect } from "react";

export const generateId = () =>
  Math.floor(10000 + Math.random() * 90000).toString();

export const variablesHighlighter = ViewPlugin.define(
  (view: EditorView) => {
    let decorations: DecorationSet;

    const buildDecorations = (view: EditorView) => {
      const builder = new RangeSetBuilder<Decoration>();
      const text = view.state.doc.toString();

      for (const { from, to } of view.visibleRanges) {
        let pos = from;
        while (pos < to) {
          const startIdx = text.indexOf("{{", pos);
          if (startIdx === -1 || startIdx >= to) break;

          const endIdx = text.indexOf("}}", startIdx + 2);
          const highlightEnd = endIdx !== -1 && endIdx < to ? endIdx + 2 : to;

          builder.add(
            startIdx,
            highlightEnd,
            Decoration.mark({ class: "cm-mustache-highlight" }),
          );

          pos = highlightEnd;
        }
      }

      return builder.finish();
    };

    decorations = buildDecorations(view);

    return {
      update(update: ViewUpdate) {
        if (update.docChanged || update.viewportChanged) {
          decorations = buildDecorations(update.view);
        }
      },
      get decorations() {
        return decorations;
      },
    };
  },
  {
    decorations: (v) => v.decorations,
  },
);

export function useDebouncedEffect(
  effect: () => void,
  deps: any[],
  delay: number,
) {
  useEffect(() => {
    const handler = setTimeout(() => {
      effect();
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [...(deps || []), delay]);
}

export const getEditorView = () => {
  return {
    "&": {
      hight: "100%",
    },
    ".cm-line": {
      fontFamily: "var(--font-google-sans)",
      fontSize: "14px",
    },
    ".cm-mustache-highlight": {
      fontFamily: "var(--font-google-sans-code)",
      color: "var(--color-brand)",
      backgroundColor: "transparent",
    },
  };
};

export const scrollToCardsContainer = (
  top: number | null = null,
  timeout = 200,
) => {
  const timeoutId = setTimeout(() => {
    const container = document.getElementsByClassName(
      "playground-scroll-area-viewport",
    )[0];

    if (container) {
      container.scrollTo({
        top: top !== null ? top : container.scrollHeight,
        behavior: "smooth",
      });
      clearTimeout(timeoutId);
    }
  }, timeout);
};

export const scrollDownOutputCardsContainer = () => {
  const timeoutId = setTimeout(() => {
    const container = document.getElementsByClassName(
      "playground-scroll-area-viewport-output",
    );

    if (container) {
      Array.from(container).forEach((item) => {
        item.scrollTo({
          top: item.scrollHeight,
          behavior: "smooth",
        });
      });
      clearTimeout(timeoutId);
    }
  }, 200);
};
