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

type HumanEvalIndicatorProps = SVGProps<SVGSVGElement>;

export default function HumanEvalIndicator(props: HumanEvalIndicatorProps) {
  return (
    <svg
      width={36}
      height={24}
      viewBox="0 0 36 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <rect y="11" width="36" height="2" rx="1" fill="#2A2945"/>
      <circle cx="18" cy="12" r="5" fill="#2A2945"/>
    </svg>
  );
}