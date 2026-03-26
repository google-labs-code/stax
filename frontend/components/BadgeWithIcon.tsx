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

import { FloatingPosition } from "@mantine/core";

import MaterialIcon from "./MaterialIcon";

type BadgeWithIconProps = {
  title: string;
  icon?: string;
  iconPosition?: "before" | "after";
  className?: string;
  iconClassName?: string;
  dataTestId?: string;
  onClick?: () => void;
  tooltipLabel?: string;
  tooltipPosition?: FloatingPosition;
  disabled?: boolean;
  variant?: "default" | "inline";
};

export default function BadgeWithIcon({
  title,
  icon,
  iconPosition = "before",
  className = "",
  iconClassName = "",
  dataTestId = "badge-with-icon",
  onClick,
  tooltipLabel,
  tooltipPosition,
  disabled = false,
  variant = "default",
}: BadgeWithIconProps) {
  const isInline = variant === "inline";

  return (
    <span
      className={`
        inline-flex items-center justify-start
        bg-veryLightSilver rounded-xl px-3
        ${isInline ? "" : "w-[188.95px]"}
        h-9 min-w-[80px] whitespace-nowrap
        ${onClick && !disabled ? "cursor-pointer" : ""}
        ${disabled ? "opacity-50" : ""} 
        ${className}
      `}
      onClick={disabled ? undefined : onClick}
      data-testid={dataTestId}
    >
      {icon && iconPosition === "before" && (
        <MaterialIcon
          name={icon}
          className={`mr-2 ${iconClassName}`}
          tooltipLabel={tooltipLabel}
          tooltipPosition={tooltipPosition}
          disabled={disabled}
          size={24}
        />
      )}
      <span className="text-title-24 whitespace-nowrap">{title}</span>
      {icon && iconPosition === "after" && (
        <MaterialIcon
          name={icon}
          className={`ml-2 ${iconClassName}`}
          tooltipLabel={tooltipLabel}
          tooltipPosition={tooltipPosition}
          disabled={disabled}
          size={24}
        />
      )}
    </span>
  );
}
