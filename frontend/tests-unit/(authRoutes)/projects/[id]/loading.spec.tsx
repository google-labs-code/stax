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

import Loading from "@/app/(authRoutes)/projects/[id]/loading";
import { MantineProvider } from "@mantine/core";
import "@testing-library/jest-dom";
import { render } from "@testing-library/react";

describe("Loading", () => {
  it("renders the loading spinner", () => {
    render(
      <MantineProvider>
        <Loading />
      </MantineProvider>,
    );

    const loader = document.querySelector(".mantine-Loader-root");
    expect(loader).toBeInTheDocument();
  });
});
