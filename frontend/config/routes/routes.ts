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

const routes = {
  root: "/",
  signin: "/login",
  register: "/register",
  feedback: "/feedback",
  settings: "/settings",
  quickCompare: "/quickCompare",
  projects: "/projects",
  // autorater: "/autorater",
  evaluatorGallery: {
    root: "/evaluatorGallery",
    new: "/evaluatorGallery/new",
  },
  datasets: {
    root: "/datasets",
    dataset: "/datasets/id",
  },
  analytics: "/analytics",
};

export default routes;
