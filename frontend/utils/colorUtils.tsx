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

import { useMemo } from "react";

export const getProcessedColor = (
  color?: string | null,
  defaultBgColor = "var(--color-very-light-silver)",
  defaultTextColor = "#FFFFFF",
) => {
  try {
    if (!color) {
      return {
        backgroundColor: defaultBgColor,
        textColor: defaultTextColor,
      };
    }

    let backgroundColor = color;
    let textColor = defaultTextColor;

    if (backgroundColor.includes("var(")) {
      const colorVariable = backgroundColor
        .replace("var(", "")
        .replace(")", "");

      backgroundColor =
        getComputedStyle(document.documentElement)
          .getPropertyValue(colorVariable + "-bg")
          .trim() || backgroundColor;

      textColor =
        getComputedStyle(document.documentElement)
          .getPropertyValue(colorVariable)
          .trim() || defaultTextColor;
    }

    return { backgroundColor, textColor };
  } catch {
    return {
      backgroundColor: color || defaultBgColor,
      textColor: defaultTextColor,
    };
  }
};

export const useProcessedColor = (
  color?: string | null,
  defaultBgColor = "var(--color-very-light-silver)",
  defaultTextColor = "#FFFFFF",
) => {
  return useMemo(() => {
    return getProcessedColor(color, defaultBgColor, defaultTextColor);
  }, [color, defaultBgColor, defaultTextColor]);
};
