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

import { theme } from "@/config/theme";
import {
  Accordion,
  Menu,
  Modal,
  Popover,
  createTheme,
  mergeThemeOverrides,
} from "@mantine/core";

export const testTheme = mergeThemeOverrides(
  theme,
  createTheme({
    components: {
      Modal: Modal.extend({
        defaultProps: {
          transitionProps: { duration: 0 },
        },
      }),
      Menu: Menu.extend({
        defaultProps: {
          transitionProps: { duration: 0 },
        },
      }),
      Popover: Popover.extend({
        defaultProps: {
          transitionProps: { duration: 0 },
        },
      }),
      Accordion: Accordion.extend({
        defaultProps: {},
      }),
    },
  }),
);
