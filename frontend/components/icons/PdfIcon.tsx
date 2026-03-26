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

type DeepseekIconProps = SVGProps<SVGSVGElement> & { size?: number };

export default function PdfIcon({ size = 24, ...props }: DeepseekIconProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox={`0 0 ${size} ${size}`}
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <path
        d="M11.4 10.6H12.1V8.5H14.25V7.8H12.1V6.1H14.25V5.4H11.4V10.6ZM2.4 10.6H3.1V8.6H4.5C4.8 8.6 5.05833 8.49167 5.275 8.275C5.49167 8.05833 5.6 7.8 5.6 7.5V6.5C5.6 6.2 5.49167 5.94167 5.275 5.725C5.05833 5.50833 4.8 5.4 4.5 5.4H2.4V10.6ZM3.1 7.9V6.1H4.5C4.6 6.1 4.69167 6.14167 4.775 6.225C4.85833 6.30833 4.9 6.4 4.9 6.5V7.5C4.9 7.6 4.85833 7.69167 4.775 7.775C4.69167 7.85833 4.6 7.9 4.5 7.9H3.1ZM6.8 10.6H8.9C9.2 10.6 9.45833 10.4917 9.675 10.275C9.89167 10.0583 10 9.8 10 9.5V6.5C10 6.2 9.89167 5.94167 9.675 5.725C9.45833 5.50833 9.2 5.4 8.9 5.4H6.8V10.6ZM7.5 9.9V6.1H8.9C9 6.1 9.09167 6.14167 9.175 6.225C9.25833 6.30833 9.3 6.4 9.3 6.5V9.5C9.3 9.6 9.25833 9.69167 9.175 9.775C9.09167 9.85833 9 9.9 8.9 9.9H7.5ZM1.8 15.7C1.36667 15.7 1.00833 15.5583 0.725 15.275C0.441667 14.9917 0.3 14.6333 0.3 14.2V1.8C0.3 1.36667 0.441667 1.00833 0.725 0.724999C1.00833 0.441666 1.36667 0.299999 1.8 0.299999H14.2C14.6333 0.299999 14.9917 0.441666 15.275 0.724999C15.5583 1.00833 15.7 1.36667 15.7 1.8V14.2C15.7 14.6333 15.5583 14.9917 15.275 15.275C14.9917 15.5583 14.6333 15.7 14.2 15.7H1.8Z"
        fill="#E64A3D"
      />
    </svg>
  );
}
