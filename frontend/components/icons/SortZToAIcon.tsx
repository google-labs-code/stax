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

type SortZToAIconProps = SVGProps<SVGSVGElement> & { size?: number };

export default function SortZToAIcon({
  size = 20,
  ...props
}: SortZToAIconProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 20 20"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <path
        d="M9.85352 13.8337L12.8535 6.16699H14.1035L17.0827 13.8337H15.791L15.1035 11.917H11.8327L11.1035 13.8337H9.85352ZM12.1868 10.8753H14.7285L13.4785 7.56283H13.416L12.1868 10.8753ZM2.91602 13.8337V12.7087L7.06185 7.25033H3.10352V6.16699H8.39518V7.29199L4.29102 12.7503H8.43685V13.8337H2.91602ZM7.29102 4.41699L9.60352 2.10449L11.916 4.41699H7.29102ZM9.60352 17.8962L7.29102 15.5837H11.916L9.60352 17.8962Z"
        fill="var(--color-secondary)"
      />
    </svg>
  );
}
