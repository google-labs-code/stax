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

import { Text } from "@mantine/core";

type SideBySideModelsIconProps = {
  variant: "first" | "second";
  greyedOut?: boolean;
  className?: string;
};

export default function SideBySideModelsIcon({
  variant,
  greyedOut = false,
  className = "",
}: SideBySideModelsIconProps) {
  const letter = variant === "first" ? "A" : "B";

  return (
    <div
      className={`flex h-5 w-5 items-center justify-center rounded ${
        greyedOut ? "bg-transparent" : "bg-lightBlue"
      } ${className}`}
    >
      <Text
        size="xs"
        fw={500}
        className={`${className} ${greyedOut ? "text-white" : "text-brand"}`}
      >
        {letter}
      </Text>
    </div>
  );
}
