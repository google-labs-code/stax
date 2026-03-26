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

import { NavigationLinkData } from "@/components/SideNav/types";
import PerformanceAutoIcon from "@/components/icons/PeformanceAuto";

import routes from "./routes";

const ALL_PAGE_LINKS: NavigationLinkData[] = [
  {
    link: routes.projects,
    key: "projects",
    label: "Evaluation Projects",
    icon: "home_storage",
    isExpanded: true,
    childLinks: [],
  },
  {
    link: routes.datasets.root,
    key: "datasets",
    label: "Datasets",
    icon: "data_table",
    childLinks: [],
  },
  {
    link: routes.evaluatorGallery.root,
    key: "evaluatorGallery",
    label: "Evaluator Gallery",
    icon: PerformanceAutoIcon,
    childLinks: [],
  },
  {
    link: routes.analytics,
    key: "analytics",
    label: "Analytics",
    icon: "trending_up",
    childLinks: [],
  },
] as const;

export { ALL_PAGE_LINKS, routes };
