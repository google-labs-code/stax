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

import { UnstyledButton } from "@mantine/core";

interface ViewDetailsButtonProps {
  onClick: (e: React.MouseEvent) => void;
}

export default function ViewDetailsButton({ onClick }: ViewDetailsButtonProps) {
  return (
    <UnstyledButton
      onClick={onClick}
      className="text-brand text-body-12 !font-medium cursor-pointer p-0 h-6 min-h-6 rounded-lg py-1"
    >
      View details
    </UnstyledButton>
  );
}
