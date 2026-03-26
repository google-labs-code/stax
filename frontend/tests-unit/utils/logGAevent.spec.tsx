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

import { GAevents } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { UserDetails } from "@/utils/userDetailsStorage";

jest.mock("@/utils/userDetailsStorage", () => ({
  UserDetails: {
    get: jest.fn(),
  },
}));

describe("logGAevent", () => {
  let originalEnv: NodeJS.ProcessEnv;

  beforeEach(() => {
    originalEnv = { ...process.env };
    jest.clearAllMocks();
    (window as any).gtag = jest.fn();
  });

  afterEach(() => {
    process.env = originalEnv;
  });

  describe("when NEXT_PUBLIC_GA_TAG_ID is defined", () => {
    beforeEach(() => {
      process.env.NEXT_PUBLIC_GA_TAG_ID = "test-ga-id";
    });

    it("logs an event without setting user_group for non-Google emails", () => {
      (UserDetails.get as jest.Mock).mockReturnValue({
        email: "user@example.com",
      });

      logGAevent(GAevents.NEW_PROJECT);

      expect((window as any).gtag).toHaveBeenCalledTimes(1);
      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.NEW_PROJECT,
        {},
      );
    });

    it("logs an event and sets user_group for Google emails", () => {
      (UserDetails.get as jest.Mock).mockReturnValue({
        email: "user@google.com",
      });

      logGAevent(GAevents.ADD_DATA);

      expect((window as any).gtag).toHaveBeenCalledTimes(2);
      expect((window as any).gtag).toHaveBeenCalledWith(
        "set",
        "user_properties",
        { user_group: "google" },
      );
      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.ADD_DATA,
        {},
      );
    });

    it("passes event parameters correctly", () => {
      (UserDetails.get as jest.Mock).mockReturnValue({
        email: "user@example.com",
      });

      const params = { param1: "value1", param2: "value2" };
      logGAevent(GAevents.PROJECT_EVAL, params);

      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.PROJECT_EVAL,
        params,
      );
    });

    it("handles undefined user details gracefully", () => {
      (UserDetails.get as jest.Mock).mockReturnValue(undefined);

      logGAevent(GAevents.CREATE_PROJECT);

      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.CREATE_PROJECT,
        {},
      );
    });

    it("handles null user details gracefully", () => {
      (UserDetails.get as jest.Mock).mockReturnValue(null);

      logGAevent(GAevents.CREATE_PROJECT);

      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.CREATE_PROJECT,
        {},
      );
    });

    it("handles user details without email property gracefully", () => {
      (UserDetails.get as jest.Mock).mockReturnValue({ email: "" });

      logGAevent(GAevents.GENERATE_OUTPUT);

      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.GENERATE_OUTPUT,
        {},
      );
    });

    it("handles empty event parameters object", () => {
      (UserDetails.get as jest.Mock).mockReturnValue({
        email: "user@example.com",
      });

      logGAevent(GAevents.SELECT_MODEL, {});

      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.SELECT_MODEL,
        {},
      );
    });

    it("logs different types of events", () => {
      (UserDetails.get as jest.Mock).mockReturnValue({
        email: "user@example.com",
      });

      logGAevent(GAevents.ADDS_API_KEY);
      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.ADDS_API_KEY,
        {},
      );

      jest.clearAllMocks();

      logGAevent(GAevents.ADD_HUMAN_RATING);
      expect((window as any).gtag).toHaveBeenCalledWith(
        "event",
        GAevents.ADD_HUMAN_RATING,
        {},
      );
    });
  });

  describe("when NEXT_PUBLIC_GA_TAG_ID is not defined", () => {
    beforeEach(() => {
      delete process.env.NEXT_PUBLIC_GA_TAG_ID;
    });

    it("does not log event when GA tag ID is not present", () => {
      logGAevent(GAevents.NEW_PROJECT);
      expect((window as any).gtag).not.toHaveBeenCalled();
    });

    it("does not log event with parameters when GA tag ID is not present", () => {
      logGAevent(GAevents.PROJECT_EVAL, { param: "value" });
      expect((window as any).gtag).not.toHaveBeenCalled();
    });
  });
});
