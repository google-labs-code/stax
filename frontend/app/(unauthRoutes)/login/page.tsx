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

import Loading from "@/app/loading";
import { MainConfig } from "@/config/config";
import {
  JWT_TOKEN_KEY,
  LOCAL_STORAGE_AUTO_SURVEY_MAXIMUM_LOGIN_DAY_SPAN,
  LOCAL_STORAGE_AUTO_SURVEY_MINIMUM_LOGIN_COUNT,
  LOCAL_STORAGE_AUTO_SURVEY_SHOWN_KEY,
  LOCAL_STORAGE_LOGINS_LIST_KEY,
} from "@/config/constants";
import { getErrorNotificationConfig } from "@/config/notifications";
import { routes } from "@/config/routes";
import { useGlobalContext } from "@/hooks/useGlobalContext";
import { Token, UserWithToken } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import HatsApi from "@/utils/hatsApi";
import { UserDetails } from "@/utils/userDetailsStorage";
import { notifications } from "@mantine/notifications";
import { useGoogleLogin } from "@react-oauth/google";
import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import AddKeysWelcomeModal from "./components/AddKeysWelcomeModal";
import { acceptTos, signInWithGoogle } from "./state/queries";

export default function SignInPage() {
  const { setUserDetails } = useGlobalContext();
  const [showAddKeysWelcome, setShowAddKeysWelcome] = useState(false);
  const [hasError, setHasError] = useState(false);
  const router = useRouter();

  const signInWithGoogleAction = useGoogleLogin({
    flow: "auth-code",
    ux_mode: "redirect",
    select_account: true,
    redirect_uri: process.env.NEXT_PUBLIC_APP_BASE_URL ?? "",
  });

  useEffect(() => {
    if (!MainConfig.isAuthEnabled) {
      router.push(routes.projects);

      return;
    }

    const queryParameters = new URLSearchParams(window.location.search);
    const code = queryParameters.get("code");
    const error = queryParameters.get("error");
    if (code) {
      signInWithGoogleMutation.mutate({
        authCode: decodeURIComponent(code),
        redirectUrl: process.env.NEXT_PUBLIC_APP_BASE_URL ?? "",
      });
      recordLoginAndShowAutoSurvey();
    } else if (error) {
      setHasError(true);
      notifications.show(
        getErrorNotificationConfig(error, "google-token-error"),
      );

      // Sign in again after 3 seconds
      setTimeout(() => {
        signInWithGoogleAction();
      }, 3000);
    } else {
      setTimeout(() => {
        signInWithGoogleAction();
      }, 1000);
    }
  }, []);

  const recordLoginAndShowAutoSurvey = () => {
    const THIRTY_DAYS =
      LOCAL_STORAGE_AUTO_SURVEY_MAXIMUM_LOGIN_DAY_SPAN * 24 * 60 * 60 * 1000;
    const timestampNow = Date.now();
    let logins: number[] = JSON.parse(
      LocalStorage.get(LOCAL_STORAGE_LOGINS_LIST_KEY) || "[]",
    );
    const autoSurveyShown = JSON.parse(
      LocalStorage.get(LOCAL_STORAGE_AUTO_SURVEY_SHOWN_KEY) || "false",
    );
    logins.push(timestampNow);
    logins = logins.filter(
      (timestamp) => timestampNow - timestamp <= THIRTY_DAYS,
    );
    LocalStorage.set(LOCAL_STORAGE_LOGINS_LIST_KEY, JSON.stringify(logins));
    if (
      !autoSurveyShown &&
      logins.length >= LOCAL_STORAGE_AUTO_SURVEY_MINIMUM_LOGIN_COUNT
    ) {
      LocalStorage.set(
        LOCAL_STORAGE_AUTO_SURVEY_SHOWN_KEY,
        JSON.stringify(true),
      );
      window.setTimeout(() => {
        HatsApi.getInstance(window).requestSurvey();
      }, 2000);
    }
  };

  const signInWithGoogleMutation = useMutation({
    mutationFn: signInWithGoogle,
    onSuccess: (response) => {
      const res = response as UserWithToken;
      const { token, tosId, tosContent } = res;
      let tosAuthResponse: Token | null | undefined;

      if (token.match("tos-")) {
        if (tosId && tosContent) {
          tosAuthResponse = {
            tosId,
            tosContent,
            token,
          };
        }

        if (tosAuthResponse) {
          acceptTosMutation.mutate(tosAuthResponse);
        }
      } else {
        LocalStorage.set(JWT_TOKEN_KEY, response.token);
        setUserData(res);
      }
    },
    onError: () => {
      setHasError(true);
      setTimeout(() => {
        signInWithGoogleAction();
      }, 3000);
    },
  });

  const acceptTosMutation = useMutation({
    mutationFn: acceptTos,
    onSuccess: (response) => {
      LocalStorage.set(JWT_TOKEN_KEY, response.token);
      const res = response as UserWithToken;
      setUserData(res, false);
    },
    onError: async () => {
      setHasError(true);
      setTimeout(() => {
        signInWithGoogleAction();
      }, 3000);
    },
  });

  const setUserData = (res: UserWithToken, withRedirect = true) => {
    const { ...user } = res;

    const userData = {
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
    };
    UserDetails.set(userData);
    setUserDetails(userData);

    if (withRedirect) {
      router.push(routes.projects);
    } else {
      setShowAddKeysWelcome(true);
    }
  };

  if (hasError) {
    return null;
  }

  if (showAddKeysWelcome) {
    return (
      <AddKeysWelcomeModal
        isOpened={showAddKeysWelcome}
        onProceed={() => {
          setShowAddKeysWelcome(false);
          router.push(routes.projects);
        }}
      />
    );
  }

  return <Loading />;
}
