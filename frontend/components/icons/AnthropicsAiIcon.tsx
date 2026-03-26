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

type AnthropicsIconProps = SVGProps<SVGSVGElement> & { size?: number };

export default function AnthropicsAiIcon({
  size = 18,
  ...props
}: AnthropicsIconProps) {
  const idA = (Math.random() * Date.now()).toString();
  const idB = (Math.random() * Date.now()).toString();

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 128 128"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <g clipPath={`url(#${idA})`}>
        <rect width="128" height="128" fill={`url(#${idB})`} rx="30" />
        <path fill="#D97757" d="M0 0h128v128H0z" opacity=".35" />
        <path
          fill="#FAFAF8"
          d="m35.016 79.694 18.416-10.328.304-.904-.304-.504h-.91l-3.087-.188-10.524-.281-9.107-.376-8.854-.47-2.226-.469-2.074-2.769.202-1.361 1.872-1.268 2.682.235 5.92.422 8.904.61 6.426.376 9.562.986h1.518l.202-.61-.506-.376-.405-.375L43.82 55.8l-9.967-6.572-5.211-3.802-2.783-1.925-1.416-1.784-.608-3.943 2.53-2.816 3.44.235.86.234 3.492 2.676 7.437 5.774 9.714 7.135 1.417 1.173.57-.385.088-.272-.658-1.08-5.262-9.529-5.616-9.716-2.53-4.037-.657-2.394c-.255-1.005-.405-1.836-.405-2.864l2.884-3.943 1.619-.516 3.896.516 1.619 1.409 2.428 5.538 3.896 8.685 6.071 11.829 1.771 3.52.961 3.24.355.985h.607v-.563l.506-6.666.91-8.168.911-10.514.303-2.958 1.468-3.567 2.934-1.925 2.277 1.08 1.872 2.676-.253 1.736-1.113 7.23L72 42.844l-1.416 7.604h.81l.96-.985 3.846-5.07 6.425-8.074 2.834-3.192 3.339-3.52 2.125-1.69h4.047l2.935 4.412-1.316 4.553-4.148 5.258L89 46.6l-4.934 6.608-3.06 5.314.274.439.737-.063 11.13-2.394 6.022-1.08 7.184-1.22 3.238 1.502.354 1.549-1.265 3.145-7.69 1.877-9.006 1.831-13.41 3.157-.15.12.176.26 6.048.547 2.58.14h6.325l11.788.893 3.086 2.018 1.822 2.488-.304 1.925-4.756 2.394-6.374-1.503-14.926-3.567-5.11-1.267h-.708v.422l4.25 4.178 7.842 7.04 9.765 9.107.506 2.254-1.265 1.783-1.316-.188-8.6-6.477-3.34-2.91-7.488-6.338h-.506v.658l1.72 2.534 9.158 13.754.455 4.225-.658 1.361-2.377.845-2.58-.47-5.415-7.557-5.514-8.45-4.453-7.604-.537.34-2.65 28.294-1.214 1.455-2.834 1.08-2.378-1.784-1.264-2.91 1.264-5.774 1.518-7.51 1.215-5.962 1.113-7.416.68-2.478-.06-.166-.544.091-5.591 7.669-8.5 11.5-6.73 7.182-1.618.658-2.783-1.456.253-2.581 1.569-2.3 9.309-11.83 5.616-7.37 3.62-4.23-.036-.611-.2-.018L32.74 89.88l-4.402.563-1.922-1.784.253-2.91.91-.939 7.438-5.117Z"
        />
      </g>
      <defs>
        <linearGradient
          id={idB}
          x1="52.5"
          x2="52.5"
          y1="128"
          y2="0"
          gradientUnits="userSpaceOnUse"
        >
          <stop stopColor="#DC6038" />
          <stop offset="1" stopColor="#D97757" />
        </linearGradient>
        <clipPath id={idA}>
          <rect width="128" height="128" fill="#fff" rx="30" />
        </clipPath>
      </defs>
    </svg>
  );
}
