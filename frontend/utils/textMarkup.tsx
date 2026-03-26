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

import { Tooltip } from "@mantine/core";

export function ReplaceVariablesValueMarkupWithValue(str: string) {
  return str.replace(
    /<variable[^>]*\bvalue="([^"]*)"[^>]*\/>/g,
    (_, value) => value,
  );
}

export function GetFormattedChildrenMarkup(children: any, plainText?: boolean) {
  const extractAttributes = (str: string) => {
    const regex = /(\w+)="([^"]*)"/g;
    const attributes: Record<string, string> = {};
    let match;
    while ((match = regex.exec(str)) !== null) {
      attributes[match[1]] = match[2];
    }

    return attributes;
  };

  if (Array.isArray(children)) {
    return children?.map((child: any, index: number) => {
      if (typeof child === "string" && child?.includes("<variable")) {
        const attributes = extractAttributes(child);
        const color = attributes?.color || "";
        const tooltip = attributes?.tooltip || "";

        if (plainText) {
          return attributes.value;
        }

        return (
          <Tooltip label={tooltip} key={index}>
            <span style={{ color }}>{attributes.value}</span>
          </Tooltip>
        );
      }

      return child;
    });
  }

  return children;
}
