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

import React, { useId } from "react";

interface EditWithSparkIconProps {
  size?: number;
  color?: string;
}

export function EditWithSparkIcon({
  size = 20,
  color,
}: EditWithSparkIconProps) {
  const gradientId = useId();

  return (
    <div className="w-[20px] h-[20px] flex items-center justify-center">
      <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        {!color && (
          <defs>
            <linearGradient id={gradientId} x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#9168C0" />
              <stop offset="100%" stopColor="#1BA1E3" />
            </linearGradient>
          </defs>
        )}
        <g clipPath="url(#clip0_10470_232007)">
          <path
            d="M6 19H7.425L17.2 9.225L15.775 7.8L6 17.575V19ZM4 21V16.75L18.625 2.15L22.825 6.425L8.25 21H4ZM20 6.4L18.6 5L20 6.4ZM16.475 8.525L15.775 7.8L17.2 9.225L16.475 8.525ZM5.5 10C5.48333 10 5.41667 9.95 5.3 9.85C5.03333 8.83333 4.525 7.95833 3.775 7.225C3.04167 6.475 2.16667 5.96667 1.15 5.7C1.11667 5.68333 1.06667 5.61667 1 5.5C1 5.46667 1.05 5.4 1.15 5.3C2.16667 5.03333 3.04167 4.53333 3.775 3.8C4.525 3.05 5.03333 2.16667 5.3 1.15C5.31667 1.11667 5.38333 1.06667 5.5 0.999999C5.53333 0.999999 5.6 1.05 5.7 1.15C5.98333 2.16667 6.49167 3.05 7.225 3.8C7.95833 4.53333 8.83333 5.03333 9.85 5.3C9.88333 5.3 9.93333 5.36667 10 5.5C10 5.51667 9.95 5.58333 9.85 5.7C8.83333 5.96667 7.95 6.475 7.2 7.225C6.46667 7.95833 5.96667 8.83333 5.7 9.85C5.7 9.88333 5.63333 9.93333 5.5 10Z"
            fill={color || `url(#${gradientId})`}
          />
        </g>
        <defs>
          <clipPath id="clip0_10470_232007">
            <rect width="24" height="24" fill="white" />
          </clipPath>
        </defs>
      </svg>
    </div>
  );
}
