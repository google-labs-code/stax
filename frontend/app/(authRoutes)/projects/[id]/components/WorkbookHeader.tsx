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

import ApiKeyMissingHoverCard from "@/components/ApiKeyMissingHoverCard";
import BulkActionMenu from "@/components/BulkActionMenu";
import MaterialIcon from "@/components/MaterialIcon";
import {
  getErrorNotificationConfig,
  getSuccessNotificationConfig,
} from "@/config/notifications";
import { routes } from "@/config/routes";
import { useModelsContext } from "@/hooks/useModelsContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import {
  getChatsExportQuery,
  getExportALLQuery,
  getSxsProjectBulkExport,
  getSxsProjectExport,
} from "@/queries/clientQueries";
import { BulkActionMenuItem, GAevents, GenerateOutputsType } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import { jsonToCsvExport } from "@/utils/jsonToCsvExport";
import logGAevent from "@/utils/logGAevent";
import {
  Button,
  Group,
  Popover,
  PopoverDropdown,
  PopoverTarget,
  Stack,
  Switch,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { MRT_Column, MRT_VisibilityState } from "mantine-react-table";
import { useRouter } from "next/navigation";
import { useCallback, useMemo, useState } from "react";

import { useProjectContext } from "../../hooks/useProjectContext";
import {
  PROJECT_ONBOARDING,
  SHOW_PROJECT_ONBOARDING,
} from "../playground/consts";
import { WorkbookHeaderProps, WorkbookItem } from "../types";
import OnboardingTooltip from "./OnboardingTooltip";
import TableSelectionBanner from "./TableSelectionBanner";

export default function WorkbookHeader({
  onAddData,
  onEvaluate,
  onGenerateModels,
  onAddTags,
  onRemoveTags,
  onAddToProject,
  onClearResults,
  onAddToDataset,
  table,
  openWorkbookDeleteModal,
  hideTooltips = true,
  projectId,
  selectionBanner,
  selectedRows,
}: WorkbookHeaderProps) {
  const tableHasDataRows = useMemo(() => {
    return table.getRowCount() > 1; // "Add row" counts as a row, so we need to check if more than 1 not 0
  }, [table.getRowCount()]);
  const hasSelectedRows = !!Object.keys(table.getState().rowSelection).length;
  const selectedIds = selectedRows.map((row) => row.original.chat_id);

  const hasSubRowSelected = selectedRows.some((row) => row.depth > 0);
  const [onboardingIndex, setOnboardingIndex] = useState<number | null>(0);
  const { allModels } = useModelsContext();
  const { setSelectedChatIds, setSelectedChatTurnIds } = usePlaygroundContext();
  const { isSideBySide } = useProjectContext();
  const canEvaluate = hasSelectedRows
    ? !!selectedRows.find((row) => {
        if (isSideBySide) {
          return (
            row.original.chat_turn_a?.output ||
            row.original.chat_turn_b?.output ||
            row.original.output
          );
        }

        return row.original.output;
      })
    : !!table.getRowModel().rows.find((row) => {
        if (isSideBySide) {
          return (
            row.original.chat_turn_a?.output ||
            row.original.chat_turn_b?.output ||
            row.original.output
          );
        }

        return row.original.output;
      });

  const userHasApiKeys = allModels.find(
    (model) => model?.is_api_key_present === true,
  );

  const selectedRowsHaveInput = selectedRows.some(
    (row) => row.original?.raw_input,
  );

  const [tooltipsCompleted, setTooltipsCompleted] = useState(false);
  const router = useRouter();

  const handlePlaygroundRedirect = () => {
    setSelectedChatIds([]);
    setSelectedChatTurnIds([]);
    router.push(`${routes.projects}/${projectId}/playground`);
  };

  const handleCompleteTooltips = () => {
    LocalStorage.set(SHOW_PROJECT_ONBOARDING, "true");
    setTooltipsCompleted(true);
  };
  const [viewColumnsOpen, setViewColumnsOpen] = useState(false);
  const shouldHideTooltips = hideTooltips || tooltipsCompleted;

  const columns = [...table.getAllColumns()].filter(
    (column) =>
      ![
        "mrt-row-actions",
        "mrt-row-select",
        "mrt-row-expand",
        "input",
        "side_by_side_model_A",
        "side_by_side_model_B",
        "side_by_side_eval",
      ].includes(column.id),
  );

  const getChatsExportMutation = useMutation({
    mutationFn: (chatIds: string[]) => {
      return getChatsExportQuery(chatIds);
    },
    onSuccess: (res: WorkbookItem[]) => {
      jsonToCsvExport(
        res,
        `export-chats-${new Date().toISOString().slice(0, 10)}.csv`,
      );

      notifications.show(
        getSuccessNotificationConfig(
          "Export completed successfully",
          "export-success",
        ),
      );
    },
  });

  const exportAllRowsMutation = useMutation({
    mutationFn: () => {
      notifications.show(
        getSuccessNotificationConfig(
          "Exporting all rows from the project. This may take some time for large projects.",
          "export-all-started",
        ),
      );

      if (isSideBySide) {
        return getSxsProjectExport(projectId) as Promise<WorkbookItem[]>;
      } else {
        return getExportALLQuery(projectId) as Promise<WorkbookItem[]>;
      }
    },
    onSuccess: (res: WorkbookItem[]) => {
      jsonToCsvExport(
        res,
        `export-project-${new Date().toISOString().slice(0, 10)}.csv`,
      );

      if (selectionBanner?.onClearSelection) {
        selectionBanner.onClearSelection();
      }

      notifications.show(
        getSuccessNotificationConfig(
          "Export of all project rows completed successfully",
          "export-all-success",
        ),
      );
    },
    onError: (error) => {
      notifications.show(
        getErrorNotificationConfig(
          error.message || "Failed to export data",
          "export-error",
        ),
      );
    },
  });

  const getSxsExportMutation = useMutation({
    mutationFn: (pairIds: string[]) => {
      return getSxsProjectBulkExport(projectId, pairIds);
    },
    onSuccess: (res: WorkbookItem[]) => {
      jsonToCsvExport(
        res,
        `export-sxs-${new Date().toISOString().slice(0, 10)}.csv`,
      );

      notifications.show(
        getSuccessNotificationConfig(
          "Export completed successfully",
          "export-success",
        ),
      );
    },
    onError: (error) => {
      notifications.show(
        getErrorNotificationConfig(
          error.message || "Failed to export data",
          "export-error",
        ),
      );
    },
  });

  const handleExport = useCallback(() => {
    if (selectionBanner?.selectAllRowsInProject) {
      exportAllRowsMutation.mutate();
    } else if (isSideBySide) {
      const pairIds = selectedRows
        .map((row) => row.original.pairId)
        .filter((id) => id !== undefined && id !== null);

      getSxsExportMutation.mutate(pairIds);
    } else {
      getChatsExportMutation.mutate(selectedIds);
    }
  }, [
    exportAllRowsMutation,
    getChatsExportMutation,
    getSxsExportMutation,
    selectedIds,
    selectedRows,
    selectionBanner,
    isSideBySide,
  ]);

  const menuItems: BulkActionMenuItem[] = [
    {
      label: "Export",
      icon: "download",
      onClick: handleExport,
    },
    {
      label: "Add tags",
      icon: "label",
      onClick: () => onAddTags(),
    },
    {
      label: "Remove tags",
      icon: "label",
      onClick: () => onRemoveTags(),
    },
    {
      label: "Add to project",
      icon: "add",
      onClick: () => onAddToProject(),
    },
    {
      icon: "add",
      label: "Add to dataset",
      onClick: () => onAddToDataset(),
    },
    {
      label: "Clear results",
      icon: "clear",
      onClick: () => onClearResults(),
    },
    ...(!hasSubRowSelected &&
      ([
        {
          icon: "delete",
          label: "Delete",
          onClick: () => openWorkbookDeleteModal && openWorkbookDeleteModal(),
        },
      ] as any)),
  ];

  const onHideAll = () => {
    table.setColumnVisibility(() => {
      const newState: { [key: string]: boolean } = {};
      for (const col of columns) {
        newState[col.id] = false;
      }

      return newState;
    });
  };

  const onGenerateOutputs = useCallback(
    (type: GenerateOutputsType) => {
      onGenerateModels(type);
      logGAevent(GAevents.GENERATE_OUTPUT, {
        source: "workbook",
      });
    },
    [onGenerateModels],
  );

  const ViewColumnItem = ({ column }: { column: MRT_Column<WorkbookItem> }) => (
    <Group className="gap-lg flex flex-row items-center p-[12px]">
      <Switch
        size="xs"
        checked={table.getColumn(column.id).getIsVisible()}
        className="w-[26px] min-w-[26px]"
        data-testid="switch-element"
        onChange={() => {
          let selectedColumnVisibility = false;
          table.setColumnVisibility((state: MRT_VisibilityState) => {
            const newState = { ...state };
            if (newState[column.id] !== undefined) {
              if (newState[column.id]) {
                newState[column.id] = false;
              } else {
                newState[column.id] = true;
              }
            } else {
              newState[column.id] = false;
            }

            selectedColumnVisibility = newState[column.id];

            return newState;
          });

          try {
            const projectTableConfig = LocalStorage.get("projectTableConfig");
            if (!projectTableConfig) {
              return;
            }

            const projectTableConfigParsed = JSON.parse(projectTableConfig);

            LocalStorage.set("projectTableConfig", {
              ...projectTableConfigParsed,
              columnVisibility: {
                ...projectTableConfigParsed.columnVisibility,
                [column.id]: selectedColumnVisibility,
              } as Record<string, boolean>,
            });
          } catch {
            //
          }
        }}
      />
      <Text className="text-body-14">{column.columnDef.header}</Text>
    </Group>
  );

  return (
    <Stack className="w-full pt-4 px-1" gap={6}>
      <Group
        justify="space-between"
        className={`${selectionBanner?.allRowsSelected ? "mb-0" : "mb-4"}  w-[100%]`}
      >
        <Group gap="8px">
          <OnboardingTooltip
            localStorageItemName={SHOW_PROJECT_ONBOARDING}
            opened={onboardingIndex === 0}
            onNext={() => setOnboardingIndex(1)}
            onClose={() => setOnboardingIndex(null)}
            index={0}
            position="bottom-start"
            content={PROJECT_ONBOARDING}
            forceHide={shouldHideTooltips}
          >
            <UnstyledButton
              onClick={handlePlaygroundRedirect}
              className="gap-sm z-10 flex items-center justify-center py-[8px] pl-[6px] pr-[12px] rounded-sm hover:bg-veryLightSilver"
            >
              <MaterialIcon name="call_to_action" size={20} />
              <Text className="text-title-12">Open Playground</Text>
            </UnstyledButton>
          </OnboardingTooltip>

          <UnstyledButton
            onClick={onAddData}
            className="gap-sm flex items-center justify-center py-[8px] pl-[6px] pr-[12px] rounded-sm hover:bg-veryLightSilver"
          >
            <MaterialIcon name="cloud_upload" size={20} />
            <Text className="text-title-12">
              {isSideBySide ? "Upload Data" : "Upload Dataset"}
            </Text>
          </UnstyledButton>

          <Popover opened={viewColumnsOpen} onChange={setViewColumnsOpen}>
            <PopoverTarget>
              <UnstyledButton
                variant="transparent"
                onClick={() => setViewColumnsOpen(!viewColumnsOpen)}
                className={`gap-sm flex items-center justify-center py-[8px] pl-[6px] pr-[12px] rounded-sm ${
                  viewColumnsOpen
                    ? "bg-veryLightSilver"
                    : "hover:bg-veryLightSilver"
                }`}
              >
                <MaterialIcon name="view_column" size={20} />
                <Text className="text-title-12">Columns</Text>
              </UnstyledButton>
            </PopoverTarget>
            <PopoverDropdown className="min-w-[200px] overflow-auto rounded-lg !p-0 md:max-h-[350px] 2xl:max-h-[450px]">
              <UnstyledButton
                className="gap-lg border-b-default flex w-[100%] flex-row items-center rounded-tl-[16px] rounded-tr-[16px] border-0 p-[12px]"
                onClick={onHideAll}
              >
                <MaterialIcon
                  name="visibility_off"
                  size={20}
                  className="text-secondary"
                />
                <Text className="text-body-14">Hide All</Text>
              </UnstyledButton>

              {columns.map((column, key) => (
                <ViewColumnItem key={key} column={column} />
              ))}
            </PopoverDropdown>
          </Popover>

          <BulkActionMenu
            isDisabled={!hasSelectedRows}
            menuItems={menuItems}
            label="More"
            customClassName=" py-[8px] pl-[6px] pr-[12px]"
          />
        </Group>

        <Group gap={16}>
          <OnboardingTooltip
            localStorageItemName={SHOW_PROJECT_ONBOARDING}
            opened={onboardingIndex === 1}
            onNext={() => setOnboardingIndex(2)}
            onPrevious={() => setOnboardingIndex(0)}
            onClose={() => setOnboardingIndex(null)}
            index={1}
            position="top"
            content={PROJECT_ONBOARDING}
            forceHide={shouldHideTooltips}
          >
            <ApiKeyMissingHoverCard show={!userHasApiKeys}>
              <Button
                disabled={
                  !tableHasDataRows ||
                  !userHasApiKeys ||
                  (selectedRows?.length > 0 && !selectedRowsHaveInput) ||
                  hasSubRowSelected
                }
                variant="defaultDisabled"
                className="min-w-[150px] !px-[16px] !rounded-sm"
                onClick={() =>
                  onGenerateOutputs(
                    hasSelectedRows
                      ? GenerateOutputsType.SELECTED_ROWS
                      : GenerateOutputsType.ALL,
                  )
                }
              >
                {hasSelectedRows ? "Generate outputs" : "Generate all outputs"}
              </Button>
            </ApiKeyMissingHoverCard>
          </OnboardingTooltip>

          <OnboardingTooltip
            localStorageItemName={SHOW_PROJECT_ONBOARDING}
            opened={onboardingIndex === 2}
            onNext={handleCompleteTooltips}
            onPrevious={() => setOnboardingIndex(1)}
            onClose={() => setOnboardingIndex(null)}
            index={2}
            position="top"
            content={PROJECT_ONBOARDING}
            forceHide={shouldHideTooltips}
          >
            <ApiKeyMissingHoverCard show={!userHasApiKeys}>
              <Button
                disabled={
                  ((!tableHasDataRows || !canEvaluate || !userHasApiKeys) &&
                    !selectionBanner?.selectAllRowsInProject) ||
                  (selectionBanner?.selectAllRowsInProject && !userHasApiKeys)
                }
                variant="defaultDisabled"
                onClick={onEvaluate}
                className="!rounded-sm"
              >
                {hasSelectedRows ? "Evaluate" : "Evaluate all"}
              </Button>
            </ApiKeyMissingHoverCard>
          </OnboardingTooltip>
        </Group>
      </Group>
      {selectionBanner && selectionBanner.visible && (
        <TableSelectionBanner
          selectAllRowsInProject={selectionBanner.selectAllRowsInProject}
          allRowsSelected={selectionBanner.allRowsSelected}
          totalInProject={selectionBanner.totalInProject}
          currentPageSize={selectionBanner.currentPageSize}
          selectedCount={selectionBanner.selectedCount}
          onSelectAll={selectionBanner.onSelectAll}
          onClearSelection={selectionBanner.onClearSelection}
          visible={selectionBanner.visible}
        />
      )}
    </Stack>
  );
}
