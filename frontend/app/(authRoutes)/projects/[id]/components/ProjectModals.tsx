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

import { Dataset } from "@/app/(authRoutes)/datasets/types";
import AddDatasetModal from "@/app/(authRoutes)/projects/[id]/components/AddDatasetModal";
import { Workbook } from "@/app/(authRoutes)/projects/[id]/types";
import EditProjectModal from "@/components/EditProjectModal";
import { UploadDatasetModal } from "@/components/UploadDatasetModal";
import { UploadDatasetModalSource } from "@/types";
import { QueryObserverResult, RefetchOptions } from "@tanstack/query-core";
import { Dispatch, SetStateAction } from "react";

import { useProjectContext } from "../../hooks/useProjectContext";
import GetStartedModal from "./GetStartedModal";

type ProjectModalsProps = {
  projectId: string;
  importedDataset: Dataset | null;
  setImportedDataset: Dispatch<SetStateAction<Dataset | null>>;
  refetchProject?: (
    options?: RefetchOptions,
  ) => Promise<QueryObserverResult<Workbook | null, Error>>;
  onTooltipVisibilityChange?: (show: boolean) => void;
};

export default function ProjectModals({
  projectId,
  importedDataset,
  setImportedDataset,
  refetchProject,
  onTooltipVisibilityChange,
}: ProjectModalsProps) {
  const { projectType, projectModals } = useProjectContext();
  const {
    isEditProjectModalOpened,
    isAddDatasetModalOpened,
    isUploadDatasetModalOpened,
    isGetStartedModalOpened,
    closeEditProjectModal,
    closeAddDatasetModal,
    openUploadDatasetModal,
    closeUploadDatasetModal,
    openAddDatasetModal,
    closeGetStartedModal,
  } = projectModals;

  const handleCloseGetStartedModal = () => {
    closeGetStartedModal();
    if (onTooltipVisibilityChange) {
      onTooltipVisibilityChange(true);
    }
  };

  const handleRedirectToPlayground = () => {
    closeGetStartedModal();

    if (onTooltipVisibilityChange) {
      onTooltipVisibilityChange(false);
    }
  };

  const handleDatasetUploaded = () => {
    closeAddDatasetModal();

    if (onTooltipVisibilityChange) {
      onTooltipVisibilityChange(true);
    }

    refetchProject && refetchProject();
  };

  const handleOpenAddDatasetModal = () => {
    if (onTooltipVisibilityChange) {
      onTooltipVisibilityChange(false);
    }

    openAddDatasetModal();
  };

  return (
    <>
      <EditProjectModal
        isOpened={isEditProjectModalOpened}
        onClose={closeEditProjectModal}
        projectId={projectId}
      />

      <GetStartedModal
        isOpened={isGetStartedModalOpened}
        onClose={handleCloseGetStartedModal}
        projectId={projectId}
        openAddDatasetModal={handleOpenAddDatasetModal}
        redirectToPlayground={handleRedirectToPlayground}
      />

      <AddDatasetModal
        isOpened={isAddDatasetModalOpened}
        onClose={closeAddDatasetModal}
        importedDataset={importedDataset}
        onImport={handleDatasetUploaded}
        onUploadCSV={() => {
          closeAddDatasetModal();
          openUploadDatasetModal();
        }}
      />

      <UploadDatasetModal
        isOpened={isUploadDatasetModalOpened}
        onClose={() => {
          closeUploadDatasetModal();
        }}
        onSuccess={(dataset) => {
          refetchProject && refetchProject();
          setImportedDataset(dataset);
        }}
        source={UploadDatasetModalSource.PROJECT}
        projectType={projectType}
        tooltipLabel={
          "Map the columns from your uploaded file to the corresponding columns in this project. Input mapping is required."
        }
      />
    </>
  );
}
