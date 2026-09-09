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

import Chip from "@/components/Chip";
import DeleteModal from "@/components/DeleteModal";
import EditProjectModal from "@/components/EditProjectModal";
import GlobalActionMenu from "@/components/GlobalActionMenu";
import MaterialIcon from "@/components/MaterialIcon";
import TokenAutoIcon from "@/components/icons/TokenAutoIcon";
import { getModelDetails } from "@/config/constants";
import { getBaseTableConfig } from "@/config/getBaseTableConfig";
import { routes } from "@/config/routes";
import {
  getProjectExportQuery,
  getProjectsQuery,
  getSxsProjectExport,
} from "@/queries/clientQueries";
import { GAevents, Project, ProjectType } from "@/types";
import dayjs from "@/utils/dayjsSetup";
import { convertMsToS } from "@/utils/helpers";
import { jsonToCsvExport } from "@/utils/jsonToCsvExport";
import logGAevent from "@/utils/logGAevent";
import {
  ActionIcon,
  Button,
  Group,
  Loader,
  Menu,
  Text,
  Tooltip,
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useMutation } from "@tanstack/react-query";
import {
  MRT_ColumnDef,
  MantineReactTable,
  useMantineReactTable,
} from "mantine-react-table";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import ProjectStatusIndicator from "../[id]/components/ProjectStatusIndicator";
import { useProjectContext } from "../hooks/useProjectContext";
import { deleteProjectQuery } from "../state/queries";
import CreateProjectModal from "./CreateProjectModal";
import JobDetailsModal from "./JobDetailsModal";

export default function ProjectsTable() {
  const { projectActions } = useProjectContext();
  const [isLoadingProjects, setIsLoadingProjects] = useState(true);
  const [allProjects, setAllProjects] = useState<Project[]>([]);
  const [
    isJobDetailsModalOpened,
    { open: openJobDetailsModal, close: closeJobDetailsModal },
  ] = useDisclosure(false);
  const [
    isCreateProjectModalOpened,
    { open: openCreateProjectModal, close: closeCreateProjectModal },
  ] = useDisclosure(false);
  const [
    isDeleteModalOpened,
    { open: openDeleteModal, close: closeDeleteModal },
  ] = useDisclosure(false);
  const [
    isEditProjectModalOpened,
    { open: openEditProjectModal, close: closeEditProjectModal },
  ] = useDisclosure(false);
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const router = useRouter();

  const getProjectsMutation = useMutation({
    mutationFn: () => getProjectsQuery({ include: "all" }),
    onSuccess: (res: any) => {
      setAllProjects(res?.["projects"] || []);
      setIsLoadingProjects(false);
    },
    onError: () => {
      setIsLoadingProjects(false);
    },
  });

  useEffect(() => {
    setIsLoadingProjects(true);
    getProjectsMutation.mutate();
  }, []);

  const getProjectExportMutation = useMutation({
    mutationFn: (project: Project) => {
      if (project.type === ProjectType.SIDE_BY_SIDE) {
        return getSxsProjectExport(project.project_id);
      }

      return getProjectExportQuery(project.project_id);
    },
    onSuccess: (res: any, params: Project) => {
      jsonToCsvExport(
        res,
        `export-${params.name}-${new Date().toISOString().slice(0, 10)}.csv`,
      );
    },
  });

  const actionButtons = [
    {
      label: "Show job details",
      icon: "view_list",
      testId: "show-details-btn",
      onClick: (project: Project) => {
        setSelectedProject(project);
        openJobDetailsModal();
      },
    },
    {
      label: "Export",
      icon: "download",
      testId: "export-btn",
      onClick: (project: Project) => {
        getProjectExportMutation.mutate(project);
      },
    },
    {
      label: "Edit",
      icon: "edit",
      testId: "edit-btn",
      onClick: (project: Project) => {
        setSelectedProject(project);
        openEditProjectModal();
      },
    },
    {
      label: "Delete",
      icon: "delete",
      testId: "delete-btn",
      onClick: (project: Project) => {
        setSelectedProject(project);
        openDeleteModal();
      },
    },
  ];

  const columns: MRT_ColumnDef<Project>[] = [
    {
      accessorKey: "name",
      header: "Project name",
      Cell: (props) => (
        <Link
          href="#"
          onClick={() => {
            projectActions.resetPagination();
            projectActions.resetProjectData();
            setTimeout(() => {
              router.push(
                `${routes.projects}/${props.row.original?.project_id}`,
              );
            });
          }}
          className="flex items-center gap-md"
        >
          <MaterialIcon
            name={
              props.row.original?.type === ProjectType.SIDE_BY_SIDE
                ? "split_scene"
                : "linear_scale"
            }
            size={16}
            className="box-content flex h-[16px] w-[16px] items-center rounded-sm bg-lightBlue p-[4px] text-brand"
          />
          <Text className="text-body-12">{props.renderedCellValue}</Text>
        </Link>
      ),
    },
    {
      accessorKey: "models",
      header: "Models",
      Cell: ({ row }) => {
        const providers = row.original?.providers;
        const className =
          "ml-[-5px] rounded-sm border-default bg-white p-[4px]";

        if (!providers || providers.length === 0) {
          return (
            <Text className="!font-medium text-disabled text-body-11">
              No models yet
            </Text>
          );
        }

        return (
          <Group gap={0} className="pl-[5px]">
            {providers?.map((provider, index) => {
              const IconComponent = getModelDetails(provider)?.icon;

              return (
                <Text className={className} key={index}>
                  {IconComponent && <IconComponent size={16} />}
                </Text>
              );
            })}
          </Group>
        );
      },
    },
    {
      accessorKey: "status",
      header: "Job status",
      Cell: ({ row }) => {
        const firstJob = row.original?.job_statuses?.[0];

        if (row.original?.total_job_tasks === 0) {
          return (
            <Text className="!font-medium text-disabled text-body-11">
              No jobs yet
            </Text>
          );
        }

        return (
          <ProjectStatusIndicator
            firstNonCompletedJob={firstJob}
            project={row.original}
          />
        );
      },
    },
    {
      accessorKey: "evaluation_scores",
      header: "Evaluation scores",
      Cell: ({ row }) => {
        const originalRow = row.original;
        let value = null;
        let label = null;

        if (originalRow?.latest_eval_score?.avg_score) {
          value = originalRow?.latest_eval_score?.avg_score?.toFixed(2);
          label = `Average ${originalRow?.latest_eval_score?.name}`;
        } else if (
          originalRow?.human_eval_metrics?.passRate ||
          originalRow?.human_eval_metrics?.passRate === 0
        ) {
          value = parseFloat(
            (originalRow?.human_eval_metrics?.passRate * 100).toFixed(2),
          ).toString();
          label = "Human Pass Rate";
        }

        return (
          <Group className="flex flex-row gap-sm flex-nowrap">
            {value && <Chip label={value} />}

            {label ? (
              <Text className="text-secondary text-body-12 whitespace-nowrap">
                {label}
              </Text>
            ) : (
              <Text className="!font-medium text-disabled text-body-11 whitespace-nowrap">
                No evals yet
              </Text>
            )}
          </Group>
        );
      },
    },
    {
      accessorKey: "metrics",
      header: "Inference metrics",
      Cell: ({ row }) => {
        const inferenceMonitoringSummary =
          row.original?.inference_monitoring_summary;

        return (
          <Group className="flex flex-row items-center gap-xl flex-nowrap cursor-default">
            <Group className="flex flex-row gap-sm flex-nowrap">
              <MaterialIcon
                name="timer"
                size={20}
                className="text-resting"
                tooltipLabel="Average latency"
              />
              <Text className="!font-medium text-resting text-body-12 whitespace-nowrap">
                {convertMsToS(
                  inferenceMonitoringSummary?.average_turn_time_taken,
                )}
              </Text>
            </Group>
            <Group className="flex flex-row gap-sm flex-nowrap">
              <Tooltip label="Total tokens" position="bottom">
                <Group>
                  <TokenAutoIcon size={20} fill="var(--color-resting)" />
                </Group>
              </Tooltip>
              <Text className="!font-medium text-resting text-body-12 whitespace-nowrap">
                {inferenceMonitoringSummary?.total_tokens > 0
                  ? `${inferenceMonitoringSummary?.total_tokens}`
                  : "0"}
              </Text>
            </Group>
          </Group>
        );
      },
    },
    {
      accessorKey: "updated_at",
      header: "Last Updated",
      Cell: ({ row }) => {
        return (
          <Text className="text-secondary">
            {dayjs(row.original.updated_at).local().format("MMMM D, YYYY")}
          </Text>
        );
      },
    },
  ];

  const deleteProjectMutation = useMutation({
    mutationFn: deleteProjectQuery,
    onSuccess: async () => getProjectsMutation.mutate(),
  });

  const tableConfig = getBaseTableConfig<Project>();
  const table = useMantineReactTable({
    ...tableConfig,
    columns,
    data: allProjects,
    enablePagination: false,
    enableRowActions: true,
    enableTopToolbar: false,
    enableBottomToolbar: false,
    enableColumnResizing: false,
    enableSorting: false,
    enableColumnActions: false,
    enableRowSelection: false,
    enableFilters: false,
    enableDensityToggle: false,
    enableFullScreenToggle: false,
    enableHiding: false,
    layoutMode: "grid",
    positionActionsColumn: "last",
    mantineSelectAllCheckboxProps: { size: "sm" },
    mantineSelectCheckboxProps: { size: "sm" },
    initialState: {
      pagination: { pageSize: 10, pageIndex: 0 },
      columnPinning: {
        right: ["mrt-row-actions"],
      },
    },
    displayColumnDefOptions: {
      "mrt-row-actions": {
        enableResizing: false,
        header: "",
      },
    },
    mantineTableHeadCellProps: ({ column }) => ({
      className: `${column.id === "mrt-row-actions" && "col-actions !shadow-none"} p-[12px] text-title-11 uppercase !font-medium text-secondary bg-veryLightSilver border-0`,
    }),
    mantineTableBodyCellProps: ({ column }) => ({
      className: `${column.id === "mrt-row-actions" && "col-actions !shadow-none !rounded-sm"} p-[12px] min-h-[40px] border-0`,
    }),
    mantinePaperProps: {
      ...tableConfig.mantinePaperProps,
      className: ` border-0 shadow-none w-full bg-transparent projects-table`,
      "aria-label": "table",
    },
    mantineTableHeadRowProps: () => {
      return {
        className: "border-0",
      };
    },
    mantineTableBodyProps: ({ table }) => {
      if (table.getRowCount() === 0) {
        return {
          className: "!hidden",
        };
      }

      return {};
    },
    mantineTableBodyRowProps: ({ row }) => ({
      onClick: () => {
        projectActions.resetPagination();
        projectActions.resetProjectData();
        setTimeout(() => {
          router.push(`${routes.projects}/${row.original.project_id}`);
        });
      },
      className: "cursor-pointer",
    }),
    mantinePaginationProps: {
      showRowsPerPage: false,
    },
    mantineTableContainerProps: {
      className: " border-0 !bg-transparent",
    },
    renderRowActions: ({ row }) => {
      return (
        <Group className="flex w-[100%] justify-center">
          <Menu
            classNames={{
              dropdown: "rounded-lg shadow-sm",
            }}
          >
            <Menu.Target>
              <ActionIcon
                size={24}
                variant="transparent"
                data-testid="action-btn"
                onClick={(e) => {
                  e.stopPropagation();
                }}
              >
                <MaterialIcon
                  name="more_vert"
                  size={24}
                  className="text-secondary"
                />
              </ActionIcon>
            </Menu.Target>

            <Menu.Dropdown>
              {actionButtons.map((button, key) => (
                <Menu.Item
                  key={key}
                  className="p-[12px]"
                  data-testid={button?.testId}
                  onClick={(e) => {
                    e.stopPropagation();
                    button.onClick(row.original);
                  }}
                >
                  <Group className="align-center flex flex-row items-center gap-lg">
                    <MaterialIcon
                      name={button.icon}
                      size={20}
                      className="!font-light text-secondary"
                    />
                    <Text className="text-body-14">{button.label}</Text>
                  </Group>
                </Menu.Item>
              ))}
            </Menu.Dropdown>
          </Menu>
        </Group>
      );
    },
    defaultColumn: {
      Cell: (props) => (
        <Text className="text-secondaryDark text-body-14">
          {props.renderedCellValue}
        </Text>
      ),
    },
    positionToolbarAlertBanner: "bottom",
    state: {
      isLoading: getProjectsMutation.isPending,
    },
  });

  return (
    <Group className="w-full flex-col gap-3xl">
      <Group justify="space-between" className="w-full" gap={0}>
        <Group gap={4}>
          <Text className="text-title-21">Evaluation Projects</Text>
        </Group>
        <Group gap={8}>
          <Button
            className="!min-w-auto !w-auto px-[16px] py-[8px]"
            classNames={{
              label: "text-title-12",
            }}
            style={{ borderRadius: "8px" }}
            data-testid="btn-new-project"
            onClick={() => {
              logGAevent(GAevents.NEW_PROJECT);
              openCreateProjectModal();
            }}
          >
            <MaterialIcon name="add" size={20} className="mr-[4px]" />
            Add project
          </Button>
          <GlobalActionMenu />
        </Group>
      </Group>
      {getProjectsMutation.isPending && isLoadingProjects && (
        <Loader size="sm" />
      )}

      {!getProjectsMutation.isPending && allProjects.length > 0 && (
        <MantineReactTable table={table} />
      )}

      <CreateProjectModal
        isOpened={isCreateProjectModalOpened}
        onClose={closeCreateProjectModal}
      />

      <JobDetailsModal
        projectData={selectedProject}
        isOpened={isJobDetailsModalOpened}
        onClose={closeJobDetailsModal}
      />

      <EditProjectModal
        isOpened={isEditProjectModalOpened}
        onClose={closeEditProjectModal}
        projectId={selectedProject?.project_id}
      />

      <DeleteModal
        isOpen={isDeleteModalOpened}
        onClose={closeDeleteModal}
        onConfirm={() => {
          if (selectedProject?.project_id) {
            deleteProjectMutation.mutate(selectedProject.project_id);
            closeDeleteModal();
          }
        }}
        isLoading={deleteProjectMutation.isPending}
        title={`Delete ${selectedProject?.name || "Project"}`}
        description="Are you sure you want to delete this project? This cannot be undone."
      />

      {!isLoadingProjects &&
        !getProjectsMutation.isPending &&
        allProjects.length === 0 && (
          <Group className="mt-[15px] flex flex-1 flex-col gap-3xl py-[16px]">
            <Group className="flex flex-1 flex-col">
              <Text className="text-secondary text-body-14">
                You don&apos;t have any projects.
              </Text>
            </Group>
          </Group>
        )}
    </Group>
  );
}
