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

// ./test-utils/render.tsx
import i18n from "@/utils/i18n";
import { MantineProvider } from "@mantine/core";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { QueryClient } from "@tanstack/query-core";
import { QueryClientProvider } from "@tanstack/react-query";
import { RenderOptions, render } from "@testing-library/react";
import { act } from "react-dom/test-utils";
import { I18nextProvider } from "react-i18next";

import { testTheme } from "./testTheme";

export function testRender(
  ui: React.ReactNode,
  options?: Omit<RenderOptions, "wrapper">,
) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        refetchOnWindowFocus: false,
      },
      mutations: {
        retry: false,
      },
    },
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <I18nextProvider i18n={i18n}>
      <QueryClientProvider client={queryClient}>
        <MantineProvider theme={testTheme}>
          <GoogleOAuthProvider
            clientId={process?.env?.NEXT_PUBLIC_GOOGLE_CLIENT_ID as string}
          >
            {children}
          </GoogleOAuthProvider>
        </MantineProvider>
      </QueryClientProvider>
    </I18nextProvider>
  );

  return render(ui, {
    wrapper,
    ...options,
  });
}

// Enhanced render function that automatically wraps in act()
export async function testRenderWithAct(
  ui: React.ReactNode,
  options?: Omit<RenderOptions, "wrapper">,
) {
  let result: ReturnType<typeof render>;

  await act(async () => {
    result = testRender(ui, options);
  });

  return result!;
}

export function testRenderLite(children: React.ReactNode) {
  return render(<MantineProvider>{children}</MantineProvider>);
}
