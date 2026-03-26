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

class HatsApi {
  private static instance: HatsApi;
  private hatsApi: any;

  public static getInstance(window: Window | null | undefined): HatsApi {
    if (!HatsApi.instance) {
      HatsApi.instance = new HatsApi();
    }

    if (window && !HatsApi.instance.hatsApi) {
      HatsApi.instance.hatsApi = window.help.service.Lazy.create(0, {
        apiKey: process.env.NEXT_PUBLIC_HATS_API_KEY,
        locale: "en-US",
      });
    }

    return HatsApi.instance;
  }

  public requestSurvey(): void {
    this.hatsApi.requestSurvey({
      triggerId: process.env.NEXT_PUBLIC_HATS_TRIGGER_ID,
      enableTestingMode: process.env.NODE_ENV !== "production",
      callback: (requestSurveyCallbackParam: any) => {
        if (!requestSurveyCallbackParam.surveyData) {
          return;
        }
        this.hatsApi.presentSurvey({
          surveyData: requestSurveyCallbackParam.surveyData,
          colorScheme: 1,
          customZIndex: 100,
        });
      },
    });
  }
}

export default HatsApi;
