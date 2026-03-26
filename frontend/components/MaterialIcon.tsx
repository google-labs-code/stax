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

import { FloatingPosition, Tooltip } from "@mantine/core";
import { FocusEventHandler, MouseEventHandler } from "react";

type MaterialIconProps = {
  name: string;
  className?: string;
  onClick?: MouseEventHandler<HTMLSpanElement>;
  disabled?: boolean;
  onBlur?: FocusEventHandler<HTMLSpanElement>;
  onMouseDown?: MouseEventHandler<HTMLSpanElement>;
  onMouseUp?: MouseEventHandler<HTMLSpanElement>;
  size?: number;
  tooltipLabel?: string;
  tooltipClassName?: string;
  tooltipPosition?: FloatingPosition | undefined;
  tooltipOffset?: number;
  dataTestId?: string;
};

export default function MaterialIcon({
  name,
  className,
  tooltipOffset,
  disabled,
  tooltipPosition = "bottom-start",
  onClick,
  onBlur,
  size,
  tooltipLabel,
  onMouseDown,
  onMouseUp,
  tooltipClassName,
  dataTestId,
}: MaterialIconProps) {
  return tooltipLabel ? (
    <Tooltip
      className={`${tooltipClassName} max-w-[113px] px-2 py-1`}
      position={tooltipPosition}
      multiline
      offset={tooltipOffset}
      label={tooltipLabel}
      zIndex={9999}
    >
      <span
        className={`material-symbols ${disabled && "text-disabled"} ${className} ${size ? `!text-[${size}px]` : null} ${onClick ? "cursor-pointer" : null}`}
        onClick={onClick}
        onBlur={onBlur}
        onMouseUp={onMouseUp}
        onMouseDown={onMouseDown}
        data-testid={dataTestId || "material-icon"}
      >
        {name}
      </span>
    </Tooltip>
  ) : (
    <span
      className={`material-symbols ${disabled && "text-disabled"} ${className} ${size ? `!text-[${size}px]` : null} ${onClick ? "cursor-pointer" : null}`}
      onClick={onClick}
      data-testid={dataTestId || "material-icon"}
    >
      {name}
    </span>
  );
}
