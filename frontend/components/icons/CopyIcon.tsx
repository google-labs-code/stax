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

import { SVGProps } from "react";

type CopyIconProps = SVGProps<SVGSVGElement> & { size?: number };

export default function CopyIcon({
  size = 20,
  className = "stroke-white",
  ...props
}: CopyIconProps) {
  return (
    <svg
      fill="none"
      height={size}
      viewBox="0 0 20 20"
      width={size}
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      {...props}
    >
      <g id="Group">
        <rect
          id="Rectangle"
          x="5.83203"
          y="5.83179"
          width="11.6715"
          height="11.6715"
          rx="2.5"
          stroke-width="1.25"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          id="Path"
          d="M5.83179 14.1686H4.99811C3.61682 14.1686 2.49707 13.0488 2.49707 11.6676V4.99811C2.49707 3.61682 3.61682 2.49707 4.99811 2.49707H11.6676C13.0488 2.49707 14.1686 3.61682 14.1686 4.99811V5.83179"
          stroke-width="1.25"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </g>
    </svg>
  );
}
