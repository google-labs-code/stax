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

import { EvaluatorPageAction } from "@/app/(authRoutes)/evaluatorGallery/types";
import MaterialIcon from "@/components/MaterialIcon";
import { routes } from "@/config/routes";
import { Button } from "@mantine/core";
import { useRouter } from "next/navigation";

type EvaluatorDuplicateButtonProps = {
  id: string;
  size?: "xs" | "sm" | "md";
  className?: string;
  onClick?: (e: React.MouseEvent) => void;
};

export default function EvaluatorDuplicateButton({
  id,
  size = "xs",
  className = "",
  onClick,
}: EvaluatorDuplicateButtonProps) {
  const router = useRouter();

  const handleDuplicateClick = (e: React.MouseEvent) => {
    e.stopPropagation();

    if (onClick) {
      onClick(e);
    } else {
      router.push(
        `${routes.evaluatorGallery.root}/${id}/${EvaluatorPageAction.DUPLICATE}`,
      );
    }
  };

  return (
    <Button
      variant="outline"
      size={size}
      onClick={handleDuplicateClick}
      data-testid={`duplicate-button-${id || ""}`}
      className={`flex-shrink-0 border-neutrals-300 text-secondaryDark hover:bg-veryLightSilver hover:border-neutrals-400 ${className}`}
      leftSection={
        <MaterialIcon
          name="library_add"
          size={size === "xs" ? 16 : size === "sm" ? 18 : 20}
          className="text-secondaryDark"
        />
      }
    >
      Duplicate
    </Button>
  );
}
