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

import { Group } from "@mantine/core";
import React from "react";

import MaterialIcon from "./MaterialIcon";

type PaginationProprs = {
  currentPage: number;
  nextPageToken: number | null | undefined;
  totalPages: number;
  onPageChange: (page: number) => void;
};

const Pagination = ({
  currentPage,
  nextPageToken,
  totalPages,
  onPageChange,
}: PaginationProprs) => {
  const handlePrevious = () => {
    if (currentPage > 1) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages) {
      onPageChange(currentPage + 1);
    }
  };

  return (
    <Group gap={2}>
      <div className="text-secondary text-body-12">
        {currentPage} of {totalPages > 1 ? totalPages : 1}
      </div>
      <div className="flex items-center justify-center gap-0">
        <MaterialIcon
          name="chevron_left"
          size={20}
          onClick={handlePrevious}
          className={`rounded-full p-[2px] ${
            currentPage === 1
              ? "!cursor-default text-gray-400"
              : "cursor-pointer text-gray-700 hover:bg-gray-200"
          }`}
          aria-label="Previous page"
        />

        <MaterialIcon
          name="chevron_right"
          size={20}
          onClick={handleNext}
          className={`rounded-full p-[2px] ${
            !nextPageToken
              ? "!cursor-default text-gray-400"
              : "cursor-pointer text-gray-700 hover:bg-gray-200"
          }`}
          aria-label="Next page"
        />
      </div>
    </Group>
  );
};

export default Pagination;
