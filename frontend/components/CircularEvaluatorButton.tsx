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

import MaterialIcon from "@/components/MaterialIcon";
import { Button } from "@mantine/core";
import React from "react";

interface CircularEvaluatorButtonProps {
  isLoading: boolean;
  onClick: () => void;
  disabled?: boolean;
  className?: string;
  variant?: "gradient" | "brand";
}

export function CircularEvaluatorButton({
  isLoading,
  onClick,
  disabled = false,
  className = "",
  variant = "gradient",
}: CircularEvaluatorButtonProps) {
  const getBackgroundClass = () => {
    if (isLoading) return "bg-veryLightSilver";
    if (disabled) return "bg-disabled";
    
    return variant === "brand"
      ? "bg-brand"
      : "bg-gradient-to-r from-[#9168C0] to-[#1BA1E3]";
  };

  return (
    <Button
      className={`w-[36px] h-[36px] min-w-[36px] max-w-[36px] rounded-[8px] !p-[8px] flex items-center justify-center ${getBackgroundClass()} ${className}`}
      onClick={onClick}
      disabled={disabled}
      p={0}
      variant="unstyled"
    >
      {isLoading ? (
        <div className="flex items-center justify-center w-full h-full">
          <MaterialIcon
            name="stop_circle"
            size={24}
            className="bg-gradient-to-r from-[#9168C0] to-[#1BA1E3] bg-clip-text text-transparent !font-thin"
          />
        </div>
      ) : (
        <MaterialIcon name="send" size={20} className="text-neutrals-50" />
      )}
    </Button>
  );
}
