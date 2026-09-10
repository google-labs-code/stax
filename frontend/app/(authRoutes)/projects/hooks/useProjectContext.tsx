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

"use client";

import { Dataset } from "@/app/(authRoutes)/datasets/types";
import { Workbook } from "@/app/(authRoutes)/projects/[id]/types";
import {
  deleteSxSRowsQuery,
  deleteWorkbookRowsQuery,
} from "@/app/(authRoutes)/projects/state/queries";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  SxsHumanEvalPassRateQuery,
  getPointwiseEvalAnalyticsQuery,
  getProjectByIdQuery,
  getProjectQuery,
  getProjectSxSQuery,
  getSxSEvalAnalyticsQuery,
  getSxSEvalAnalyticsSxSQuery,
  humanEvalPassRateQuery,
  inferenceSxsMetricsQuery,
  metricsSummaryQuery,
  projectHasRemainingJobsQuery,
} from "@/queries/clientQueries";
import {
  EvalAnalyticsScore,
  EvalSxSAnalyticsScores,
  HumanEvalPassRateData,
  MetricsSummaryResponse,
  Project,
  ProjectType,
  SxsHumanEvalPassRateData,
  SxsInferenceMetricsResponse,
} from "@/types";
import { notifications } from "@mantine/notifications";
import {
  QueryObserverResult,
  RefetchOptions,
  useMutation,
} from "@tanstack/react-query";
import { useParams } from "next/navigation";
import {
  Dispatch,
  PropsWithChildren,
  SetStateAction,
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";

import { getProjectModals } from "../[id]/utils/projectModals";

type ProjectMetricsData = MetricsSummaryResponse | SxsInferenceMetricsResponse;
interface ProjectState {
  projectId: string;
  project: Project | null;
  activeStep: number;
  metricsData: ProjectMetricsData | undefined;
  humanEvalPassRate: HumanEvalPassRateData;
  importedDataset: Dataset | null;
  pageSize: number;
  page: number;
  totalSize: number | undefined;
  isLoadingProjects: boolean;
  isLoadingProject: boolean;
  evalAnalyticsScores: EvalAnalyticsScore[];
  evalSxSAnalyticsScores: EvalSxSAnalyticsScores;
  projectData: Workbook | null;
  humanEvalPassRateIsLoading: boolean;
  inferenceMetricsIsLoading: boolean;
  evaluatorMetricsIsLoading: boolean;
  evaluatorSxSMetricsIsLoading: boolean;
  sorting: SortType[];
  displayedFilters: FiltersDisplayed;
}

export interface IProjectModals {
  isEditProjectModalOpened: boolean;
  isAddDatasetModalOpened: boolean;
  isUploadDatasetModalOpened: boolean;
  isGetStartedModalOpened: boolean;

  openEditProjectModal: () => void;
  closeEditProjectModal: () => void;
  closeAddDatasetModal: () => void;
  openUploadDatasetModal: () => void;
  closeUploadDatasetModal: () => void;
  openAddDatasetModal: () => void;
  openGetStartedModal: () => void;
  closeGetStartedModal: () => void;
}

interface ProjectActions {
  setActiveStep: (step: number) => void;
  setImportedDataset: Dispatch<SetStateAction<Dataset | null>>;
  setPageSize: (size: number) => void;
  setPage: Dispatch<SetStateAction<number>>;
  setRefreshHumanPassRate: React.Dispatch<React.SetStateAction<boolean>>;
  setHasRemainingJobs: (has: boolean) => void;
  resetPagination: () => void;
  setSorting: Dispatch<SetStateAction<SortType[]>>;
  setDisplayedFilters: Dispatch<SetStateAction<FiltersDisplayed>>;
  resetProjectData: () => void;
}

interface ProjectQueries {
  refetchProject: (
    options?: RefetchOptions,
  ) => Promise<QueryObserverResult<Workbook | null, Error>>;
  deleteWorkbookRowMutation: any;
  projectError: Error | null;
  refetchAfterAction: () => Promise<void>;
  stopPolling: () => void;
  startPolling: () => void;
  resetMetrics: () => void;
}

interface ProjectContextType {
  projectState: ProjectState;
  projectActions: ProjectActions;
  projectQueries: ProjectQueries;
  isSideBySide: boolean;
  projectType: ProjectType;
  projectModals: IProjectModals;
}

type SortType = {
  id: string;
  desc: boolean;
};

export type FiltersDisplayed = {
  input: boolean;
  output: boolean;
  expected_output: boolean;
  model_version: boolean;
  human_evaluation: boolean;
  tags: boolean;
  date_added: boolean;
};

const ProjectContext = createContext<ProjectContextType | null>(null);

export const ProjectProvider = (props: PropsWithChildren) => {
  const { children } = props;
  const { id: projectId } = useParams<{ id: string }>();
  const DEFAULT_PAGE_SIZE = 15;
  const [sorting, setSorting] = useState<SortType[]>([]);
  const [displayedFilters, setDisplayedFilters] = useState<FiltersDisplayed>({
    input: false,
    output: false,
    expected_output: false,
    model_version: false,
    human_evaluation: false,
    tags: false,
    date_added: false,
  });

  const [activeStep, setActiveStep] = useState<number>(1);
  const [shouldRefreshHumanPassRate, setRefreshHumanPassRate] = useState(true);
  const [metricsData, setMetricsData] = useState<
    ProjectMetricsData | undefined
  >();
  const [evalAnalyticsScores, setEvalAnalyticsScores] = useState<
    EvalAnalyticsScore[]
  >([]);
  const [evalSxSAnalyticsScores, setEvalSxSAnalyticsScores] =
    useState<EvalSxSAnalyticsScores>({
      pointwise: [],
      sideBySide: [],
    });
  const [humanEvalPassRate, setHumanEvalPassRate] =
    useState<HumanEvalPassRateData>({
      passRate: null,
      likes: 0,
      dislikes: 0,
    });
  const [sxsHumanEvalPassRate, setSxsHumanEvalPassRate] =
    useState<SxsHumanEvalPassRateData>({
      passRate: null,
      likes: 0,
      dislikes: 0,
      ratingCounts: { A_IS_BETTER: 0, B_IS_BETTER: 0 },
      total: 0,
    });
  const [importedDataset, setImportedDataset] = useState<Dataset | null>(null);
  const [pageSize, setPageSize] = useState<number>(DEFAULT_PAGE_SIZE);
  const [hasRemainingJobs, setHasRemainingJobs] = useState<boolean>(false);
  const [page, setPage] = useState<number>(1);
  const [project, setProject] = useState<Project | null>(null);
  const { allProjects, isLoadingProjects } = useProjectsContext();
  const [lastRemainingJobsStatus, setLastRemainingJobsStatus] = useState<
    boolean | null
  >(false);

  const timerRef = useRef<NodeJS.Timeout | null>(null);
  const isActiveRef = useRef(true);
  const [projectData, setProjectData] = useState<Workbook | null>(null);
  const [initialLoading, setInitialLoading] = useState(true);

  const projectHasRemainingJobsMutation = useMutation<boolean, Error, void>({
    mutationFn: () => projectHasRemainingJobsQuery(projectId || ""),
    onSuccess: (response) => {
      if (
        response === true ||
        (lastRemainingJobsStatus === true && response === false) ||
        hasRemainingJobs
      ) {
        setHasRemainingJobs(false);
        getProjectMutation.mutate();
      }

      setLastRemainingJobsStatus(response);
      startPolling();
    },
  });

  const getProjectMutation = useMutation({
    mutationFn: (): any => {
      if (project?.type === ProjectType.POINTWISE) {
        return getProjectQuery(projectId, pageSize, page - 1);
      } else if (project?.type === ProjectType.SIDE_BY_SIDE) {
        return getProjectSxSQuery(projectId, pageSize, page - 1);
      }
    },
    onSuccess: (data) => {
      setProjectData(data as unknown as Workbook);
    },
    onSettled: () => {
      setInitialLoading(false);
    },
  });

  const clearTimer = useCallback(() => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  const startPolling = () => {
    isActiveRef.current = true;
    if (projectId && !getProjectMutation.isPending && isActiveRef.current) {
      clearTimer();

      timerRef.current = setTimeout(() => {
        if (isActiveRef.current) {
          projectHasRemainingJobsMutation.mutate();
        }
      }, 5000);
    }
  };

  const refetchProject = () => {
    if (!projectId || isLoadingProjects || !project) return;

    setHasRemainingJobs(false);
    setLastRemainingJobsStatus(false);
    setInitialLoading(true);
    setRefreshHumanPassRate(true);
    getProjectMutation.mutate();
  };

  const refetchAfterAction = useCallback(async () => {
    return refetchProject();
  }, [refetchProject, project?.type, isLoadingProjects]);

  const [totalSize, setTotalSize] = useState<number | undefined>(
    projectData?.total_size,
  );

  useEffect(() => {
    if (totalSize !== projectData?.total_size && projectData) {
      setTotalSize(projectData?.total_size);
    }
  }, [projectData?.total_size, totalSize]);

  useEffect(() => {
    if (!projectId) return;

    if (allProjects && allProjects.length > 0) {
      const foundProject = allProjects.find(
        (proj) => proj.project_id === projectId,
      );
      if (foundProject) {
        setProject(foundProject);
        return;
      }
    }

    getProjectByIdQuery(projectId)
      .then((proj) => {
        if (proj) {
          setProject(proj);
        }
      })
      .catch((err) => {
        console.error("Failed to load project by ID", err);
      });
  }, [projectId, allProjects]);

  const resetMetrics = () => {
    setEvalAnalyticsScores([]);
    setEvalSxSAnalyticsScores({
      pointwise: [],
      sideBySide: [],
    });
    setHumanEvalPassRate({
      passRate: null,
      likes: 0,
      dislikes: 0,
    });
    setSxsHumanEvalPassRate({
      passRate: null,
      likes: 0,
      dislikes: 0,
      ratingCounts: { A_IS_BETTER: 0, B_IS_BETTER: 0 },
      total: 0,
    });
  };

  const initMetrics = useMutation({
    mutationFn: async () => {
      return metricsSummaryQuery(projectId || "");
    },
    onSuccess: (data) => {
      setMetricsData(data);
    },
  });

  const humanEvalPassRateMutation = useMutation({
    mutationFn: () => humanEvalPassRateQuery(projectId || ""),
    onSuccess: (response) => {
      setHumanEvalPassRate({
        passRate: response.passRate,
        likes: response.scoreCounts[1],
        dislikes: response.scoreCounts[-1],
      });
      setRefreshHumanPassRate(false);
    },
    onError: () => {
      setRefreshHumanPassRate(false);
    },
  });

  const sxsHumanEvalPassRateMutation = useMutation<
    SxsHumanEvalPassRateData,
    Error,
    void
  >({
    mutationFn: () =>
      SxsHumanEvalPassRateQuery(
        projectId || "",
      ) as Promise<SxsHumanEvalPassRateData>,
    onSuccess: (response) => {
      const a_better = response.ratingCounts.A_IS_BETTER || 0;
      const b_better = response.ratingCounts.B_IS_BETTER || 0;

      let passRate = null;
      if (a_better + b_better > 0) {
        if (a_better > b_better) {
          passRate = (a_better / (a_better + b_better)) * 100;
        } else if (b_better > a_better) {
          passRate = (b_better / (a_better + b_better)) * 100;
        } else {
          passRate = 50;
        }
      }

      setSxsHumanEvalPassRate({
        passRate,
        likes: 0,
        dislikes: 0,
        ratingCounts: response.ratingCounts || {
          A_IS_BETTER: 0,
          B_IS_BETTER: 0,
        },
        total: response.total || 0,
      });
      setRefreshHumanPassRate(false);
    },
  });

  const evalAnalyticsMutation = useMutation({
    mutationFn: () => getPointwiseEvalAnalyticsQuery(projectId || ""),
    onSuccess: (response) => {
      setEvalAnalyticsScores(response);
    },
  });

  const evalSxsAnalyticsScoreMutation = useMutation({
    mutationFn: () => getSxSEvalAnalyticsQuery(projectId || ""),
    onSuccess: (response) => {
      setEvalSxSAnalyticsScores((prevData) => {
        return {
          ...prevData,
          pointwise: response,
        };
      });
    },
  });

  const evalSxsAnalyticsScoreSxSMutation = useMutation({
    mutationFn: () => getSxSEvalAnalyticsSxSQuery(projectId || ""),
    onSuccess: (response) => {
      setEvalSxSAnalyticsScores((prevData) => {
        return {
          ...prevData,
          sideBySide: response,
        };
      });
    },
  });

  const inferenceSxsMetricsMutation = useMutation<
    SxsInferenceMetricsResponse,
    Error,
    void
  >({
    mutationFn: () =>
      inferenceSxsMetricsQuery(
        projectId || "",
      ) as Promise<SxsInferenceMetricsResponse>,
    onSuccess: (response: SxsInferenceMetricsResponse) => {
      setMetricsData(response);
    },
  });

  useEffect(() => {
    if (shouldRefreshHumanPassRate) {
      if (project?.type === ProjectType.POINTWISE) {
        humanEvalPassRateMutation.mutate();
      } else if (project?.type === ProjectType.SIDE_BY_SIDE) {
        sxsHumanEvalPassRateMutation.mutate();
      }
    }
  }, [shouldRefreshHumanPassRate, project?.type]);

  useEffect(() => {
    if (project?.type === ProjectType.POINTWISE && projectId) {
      initMetrics.mutate();
      evalAnalyticsMutation.mutate();
    }
  }, [project, projectData?.workbook_rows]);

  useEffect(() => {
    if (project?.type === ProjectType.SIDE_BY_SIDE && projectId) {
      inferenceSxsMetricsMutation.mutate();
      evalSxsAnalyticsScoreMutation.mutate();
      evalSxsAnalyticsScoreSxSMutation.mutate();
    }
  }, [project, projectData?.sxs_rows]);

  const deleteWorkbookRowMutation = useMutation<unknown, Error, string[]>({
    mutationFn: async (ids: string[]) => {
      if (!projectId) {
        throw new Error("Project ID is required");
      }

      if (project?.type === ProjectType.SIDE_BY_SIDE) {
        return deleteSxSRowsQuery(projectId, ids);
      }

      return deleteWorkbookRowsQuery(projectId, ids);
    },
    onSuccess: async () => {
      refetchAfterAction();
      notifications.show(
        getSuccessNotificationConfig(
          "The selected rows have been deleted successfully.",
          "rows-deleted-successfully",
        ),
      );
    },
  });

  const getCurrentHumanEvalPassRate = ():
    | HumanEvalPassRateData
    | SxsHumanEvalPassRateData => {
    return project?.type === ProjectType.SIDE_BY_SIDE
      ? sxsHumanEvalPassRate
      : humanEvalPassRate;
  };

  const isSideBySide = useMemo(() => {
    return project?.type === ProjectType.SIDE_BY_SIDE;
  }, [project]);

  const projectType = useMemo(() => {
    return project?.type || ProjectType.POINTWISE;
  }, [project]);

  const projectContext: ProjectContextType = {
    projectState: {
      projectId: projectId || project?.project_id || "",
      project,
      activeStep,
      metricsData,
      humanEvalPassRate: getCurrentHumanEvalPassRate(),
      importedDataset,
      pageSize,
      page,
      totalSize,
      isLoadingProjects,
      isLoadingProject: initialLoading,
      evalAnalyticsScores,
      evalSxSAnalyticsScores,
      projectData: projectData as unknown as Workbook | null,
      humanEvalPassRateIsLoading:
        humanEvalPassRateMutation.isPending ||
        sxsHumanEvalPassRateMutation.isPending,
      inferenceMetricsIsLoading:
        initMetrics.isPending || inferenceSxsMetricsMutation.isPending,
      evaluatorMetricsIsLoading:
        evalSxsAnalyticsScoreMutation.isPending ||
        evalAnalyticsMutation.isPending,
      evaluatorSxSMetricsIsLoading: evalSxsAnalyticsScoreSxSMutation.isPending,
      sorting,
      displayedFilters,
    },
    projectActions: {
      setActiveStep,
      setImportedDataset,
      setPageSize,
      setPage,
      setRefreshHumanPassRate,
      setHasRemainingJobs,
      resetPagination: () => {
        setPage(1);
        setPageSize(DEFAULT_PAGE_SIZE);
      },
      resetProjectData: () => {
        setProjectData(null);
        setProject(null);
      },
      setSorting,
      setDisplayedFilters,
    },
    projectQueries: {
      refetchProject: refetchProject as unknown as (
        options?: RefetchOptions,
      ) => Promise<QueryObserverResult<Workbook | null, Error>>,
      deleteWorkbookRowMutation,
      projectError: getProjectMutation.error,
      refetchAfterAction,
      stopPolling: () => {
        setInitialLoading(false);
        isActiveRef.current = false;
        clearTimer();
      },
      startPolling,
      resetMetrics,
    },
    projectModals: getProjectModals(),
    isSideBySide,
    projectType,
  };

  return (
    <ProjectContext.Provider value={projectContext}>
      {children}
    </ProjectContext.Provider>
  );
};

export function useProjectContext() {
  return useContext(ProjectContext) as ProjectContextType;
}
