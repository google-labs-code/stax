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

"use client";

import { LLMEvaluatorOutputCategory } from "@/queries/types";
import { useProcessedColor } from "@/utils/colorUtils";
import { Box, Paper, Text } from "@mantine/core";
import { useMemo } from "react";

interface OutputCategoriesProps {
  categories: LLMEvaluatorOutputCategory[];
}

export default function OutputCategories({
  categories,
}: OutputCategoriesProps) {
  if (!categories || categories.length === 0) return null;

  const sortedCategories = useMemo(() => {
    return [...categories].sort((a, b) => {
      const getNumericValue = (value: string) => {
        if (typeof value !== "string") return parseFloat(value as any) || 0;

        if (value.includes("/")) {
          const [numerator, denominator] = value.split("/");

          return parseFloat(numerator) / parseFloat(denominator);
        } else if (value.includes("%")) {
          return parseFloat(value) / 100;
        }

        return parseFloat(value);
      };

      const valueA = getNumericValue(a.value);
      const valueB = getNumericValue(b.value);

      return valueA - valueB;
    });
  }, [categories]);

  return (
    <Paper
      radius="sm"
      withBorder
      shadow="none"
      className="rounded-xs overflow-hidden flex border-none"
    >
      {sortedCategories.map((category, index) => {
        const { backgroundColor, textColor } = useProcessedColor(
          category.color,
        );

        return (
          <Box key={`${category.name}-${index}`} className="flex h-6">
            <Box
              className="px-2 flex items-center h-6"
              style={{ backgroundColor }}
              title={category.name}
              aria-label={`${category.name}: ${category.value}`}
            >
              <Text
                size="xs"
                fw={500}
                style={{ color: textColor }}
                className="text-body-11 leading-none"
              >
                {category.value}
              </Text>
            </Box>
            {index < sortedCategories.length - 1 && (
              <Box className="w-[1px] h-full bg-white/30" />
            )}
          </Box>
        );
      })}
    </Paper>
  );
}
