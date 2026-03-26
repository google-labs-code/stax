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

import { theme } from "@/config/theme";
import { Nullable, User } from "@/types";
import i18n from "@/utils/i18n";
import { UserDetails } from "@/utils/userDetailsStorage";
import { MantineProvider } from "@mantine/core";
import { Notifications } from "@mantine/notifications";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { QueryClient } from "@tanstack/query-core";
import { QueryClientProvider } from "@tanstack/react-query";
import {
  Dispatch,
  PropsWithChildren,
  SetStateAction,
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";
import { I18nextProvider } from "react-i18next";

type GlobalContextProviderType = {
  colorScheme: string;
  onChange: Dispatch<SetStateAction<string>>;
  accessToken: Nullable<string>;
  setAccessToken: Dispatch<SetStateAction<Nullable<string>>>;
  userDetails: Nullable<User>;
  setUserDetails: Dispatch<SetStateAction<Nullable<User>>>;
};

const GlobalContext = createContext<Nullable<GlobalContextProviderType>>(null);
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false, staleTime: 120000 },
    mutations: { retry: false },
  },
});

export const GlobalContextProvider = (props: PropsWithChildren) => {
  const { children } = props;
  const [colorScheme, setColorScheme] = useState("light");
  const [accessToken, setAccessToken] = useState<Nullable<string>>(null);
  const [userDetails, setUserDetails] = useState<Nullable<User>>(null);

  useEffect(() => {
    setUserDetails(UserDetails.get());
  }, []);

  return (
    <I18nextProvider i18n={i18n}>
      <GlobalContext.Provider
        value={{
          colorScheme,
          onChange: setColorScheme,
          accessToken,
          setAccessToken,
          userDetails,
          setUserDetails,
        }}
      >
        <MantineProvider theme={theme}>
          <Notifications
            position="bottom-center"
            containerWidth="830px"
            bg="transparent"
            zIndex={600}
          />
          <QueryClientProvider client={queryClient}>
            <GoogleOAuthProvider
              clientId={process?.env?.NEXT_PUBLIC_GOOGLE_CLIENT_ID as string}
            >
              {children}
            </GoogleOAuthProvider>
          </QueryClientProvider>
        </MantineProvider>
      </GlobalContext.Provider>
    </I18nextProvider>
  );
};

export function useGlobalContext() {
  return useContext(GlobalContext) as GlobalContextProviderType;
}
