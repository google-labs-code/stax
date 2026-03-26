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

import { DatasetRow } from "@/app/(authRoutes)/datasets/types";
import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import { getBaseTableConfig } from "@/config/getBaseTableConfig";
import { ProjectType } from "@/types";
import { MRT_TableInstance } from "mantine-react-table";

const baseTableConfig = getBaseTableConfig<WorkbookItem | DatasetRow>();

export const getTableBodyCellClassName = (columnId: string) => {
  let className = `p-[12px] h-[40px] border-0 border-lightSilver border-solid border-b-[1px] border-r-[1px] hover:outline-none whitespace-nowrap col-${columnId}`;

  if (columnId.includes("evaluation_")) {
    className += " single-eval-column";
  }

  switch (columnId) {
    case "mrt-row-select":
      className +=
        " row-select-column !p-[10px] no-shadow-column !shadow-none border-0 border-lightSilver border-solid ";
      break;
    case "mrt-row-actions":
      className +=
        " actions-column !p-[10px] no-shadow-column justify-center !shadow-none !bg-neutrals-50 border-solid border-b-[1px] border-l-[1px] border-borderColor !z-1000";
      break;
    case "model_name":
      className += " !p-0 !overflow-visible";
      break;
    case "human_evaluation":
      className += " justify-center";
      break;
  }

  return className;
};

export const getTableHeadCellClassName = (columnId: string) => {
  let className = ` p-[12px] hide-sorting-icons py-[4px] text-title-11 uppercase !font-medium text-secondary bg-veryLightSilver border-0 border-lightSilver border-solid border-b-[1px] border-r-[1px] col-${columnId}`;
  if (columnId.includes("evaluation_")) {
    className += " single-eval-column";
  }

  switch (columnId) {
    case "mrt-row-select":
      className +=
        " row-select-column !p-[10px] !bg-veryLightSilver no-shadow-column !shadow-none";
      break;
    case "mrt-row-actions":
      className +=
        " actions-column !p-[10px] !bg-veryLightSilver no-shadow-column !shadow-none border-solid border-b-[1px] border-l-[1px] border-lightSilver !z-1000";
      break;
  }

  return className;
};

export const getTableMantinePaperProps = (table: any) => {
  let fullScreenStyles = "";
  if (table.getState().isFullScreen) {
    fullScreenStyles =
      "!p-[25px] rounded-sm !w-[calc(100%-50px)] !h-[calc(100%-50px)] !m-[auto]";
  }
  const projectType = table.options.meta?.projectType || ProjectType.POINTWISE;

  return {
    ...baseTableConfig.mantinePaperProps,
    className: `border-0 !shadow-none ${fullScreenStyles} ${projectType === ProjectType.SIDE_BY_SIDE && "side-by-side-table"}`,
    "aria-label": "table",
  };
};

export const getTableMantineContainerProps = (
  table: MRT_TableInstance<WorkbookItem | DatasetRow>,
) => {
  return {
    className: `!rounded-sm border-[1px] w-full overflow-auto ${table.getRowCount() < 10 && "overflow-y-hidden"}
    ${
      table.getState().isFullScreen
        ? "md:!max-h-[85%] 2xl:!max-h-[88%]"
        : "md:!max-h-[82%] 2xl:!max-h-[86%] [@media_(max-height:590px)]:!max-h-[75%]"
    }  border-solid border-lightSilver `,
  };
};
