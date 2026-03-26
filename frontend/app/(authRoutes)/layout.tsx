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

import SideNavigation from "@/components/SideNav/SideNavigation";
import { AnalyticsFilterProvider } from "@/hooks/useAnalyticsContext";
import { ChatProvider } from "@/hooks/useChatContext";
import { DatasetsProvider } from "@/hooks/useDatasetsContext";
import { HumanEvalsProvider } from "@/hooks/useHumanEvalsContext";
import { ModelsProvider } from "@/hooks/useModelsContext";
import { ProjectsProvider } from "@/hooks/useProjectsContext";
import { TagsProvider } from "@/hooks/useTagsContext";
import "@/styles/main.scss";
import { usePathname } from "next/navigation";
import { ReactNode } from "react";

export default function RootLayout({
  modal,
  children,
}: {
  modal: React.ReactNode;
  children: ReactNode;
}) {
  const gradient = {
    background:
      "radial-gradient(circle, rgba(59,130,246,0.7) 0%, transparent 70%)",
  };
  const pathname = usePathname();
  const isAutoraterRoute = pathname === "/autorater";

  return (
    <div className="relative flex h-screen w-full flex-row items-center justify-center overflow-hidden pb-[10px] pr-2 pt-2">
      <div
        className="absolute -left-[150px] bottom-0 -z-10 h-[400px] w-[400px] rounded-full
    blur-[100px] will-change-[filter] [transform:translateZ(0)]"
        style={gradient}
      >
        <div className="h-[400px] w-[400px] bg-brand-700" />
      </div>

      <HumanEvalsProvider>
        <ProjectsProvider>
          <DatasetsProvider>
            <ModelsProvider>
              <ChatProvider>
                <AnalyticsFilterProvider>
                  <TagsProvider>
                    {!isAutoraterRoute && <SideNavigation />}
                    <main className="flex h-full grow basis-10/12 flex-row overflow-auto">
                      <div className="flex h-full w-full grow flex-col overflow-hidden rounded-2xl bg-primary">
                        <div className="flex h-full w-full grow flex-col overflow-y-auto">
                          {children}
                        </div>
                        {modal}
                      </div>
                    </main>
                  </TagsProvider>
                </AnalyticsFilterProvider>
              </ChatProvider>
            </ModelsProvider>
          </DatasetsProvider>
        </ProjectsProvider>
      </HumanEvalsProvider>
    </div>
  );
}
