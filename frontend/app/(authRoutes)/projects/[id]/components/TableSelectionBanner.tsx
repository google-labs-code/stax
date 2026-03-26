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

import { Text, UnstyledButton } from "@mantine/core";

interface TableSelectionBannerProps {
  selectAllRowsInProject: boolean;
  allRowsSelected: boolean;
  totalInProject: number;
  currentPageSize: number;
  selectedCount: number;
  onSelectAll: () => void;
  onClearSelection: () => void;
  visible: boolean;
}

export default function TableSelectionBanner({
  selectAllRowsInProject,
  allRowsSelected,
  totalInProject,
  currentPageSize,
  onSelectAll,
  onClearSelection,
  visible,
}: TableSelectionBannerProps) {
  if (!visible || (!allRowsSelected && !selectAllRowsInProject)) {
    return null;
  }

  const pageContainsAllRows = currentPageSize >= totalInProject;

  return (
    <div className="mb-2 bg-veryLightSilver rounded-sm border-y border-borderColor w-full h-[40px] flex items-center justify-center">
      {selectAllRowsInProject || pageContainsAllRows ? (
        <div className="flex items-center gap-1">
          <Text component="span" className="text-secondary text-body-11">
            All {totalInProject} rows in the project are selected.
          </Text>
          <UnstyledButton
            onClick={onClearSelection}
            className="text-brand text-body-11 !font-medium"
          >
            Clear selection
          </UnstyledButton>
        </div>
      ) : (
        allRowsSelected && (
          <div className="flex items-center gap-1">
            <Text component="span" className="text-secondary text-body-11">
              All {currentPageSize} rows on this page are selected.
            </Text>
            <UnstyledButton
              onClick={onSelectAll}
              className="text-brand text-body-11 !font-medium"
            >
              Select all {totalInProject} rows in the project
            </UnstyledButton>
          </div>
        )
      )}
    </div>
  );
}
