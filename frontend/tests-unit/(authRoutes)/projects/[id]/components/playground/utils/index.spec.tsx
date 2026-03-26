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

import {
  generateId,
  useDebouncedEffect,
} from "@/app/(authRoutes)/projects/[id]/playground/utils";
import { act, render } from "@testing-library/react";

jest.useFakeTimers();

describe("generateId", () => {
  it("should return a 5-digit string", () => {
    const id = generateId();
    expect(id).toMatch(/^\d{5}$/);
  });

  it("should return different values on multiple calls", () => {
    const ids = new Set(Array.from({ length: 100 }, () => generateId()));
    expect(ids.size).toBeGreaterThan(90);
  });
});

describe("useDebouncedEffect", () => {
  it("debounces the effect by the given delay", () => {
    const effect = jest.fn();

    function TestComponent({ delay }: { delay: number }) {
      useDebouncedEffect(effect, [], delay);

      return null;
    }

    render(<TestComponent delay={300} />);

    expect(effect).not.toHaveBeenCalled();

    act(() => {
      jest.advanceTimersByTime(299);
    });
    expect(effect).not.toHaveBeenCalled();

    act(() => {
      jest.advanceTimersByTime(1);
    });
    expect(effect).toHaveBeenCalledTimes(1);
  });

  it("cleans up previous timeouts on unmount or re-run", () => {
    const effect = jest.fn();
    const clearSpy = jest.spyOn(global, "clearTimeout");

    function TestComponent({ delay }: { delay: number }) {
      useDebouncedEffect(effect, [delay], delay);

      return null;
    }

    const { rerender, unmount } = render(<TestComponent delay={300} />);
    rerender(<TestComponent delay={500} />);
    unmount();

    expect(clearSpy).toHaveBeenCalled();
  });

  it("clears timeout on unmount", () => {
    const effect = jest.fn();
    const clearTimeoutSpy = jest.spyOn(global, "clearTimeout");

    function TestComponent({ delay }: { delay: number }) {
      useDebouncedEffect(effect, [delay], delay);

      return null;
    }

    const { unmount } = render(<TestComponent delay={300} />);
    unmount();

    expect(clearTimeoutSpy).toHaveBeenCalled();
  });

  it("clears timeout when dependencies change", () => {
    const effect = jest.fn();
    const clearTimeoutSpy = jest.spyOn(global, "clearTimeout");

    function TestComponent({ delay }: { delay: number }) {
      useDebouncedEffect(effect, [delay], delay);

      return null;
    }

    const { rerender } = render(<TestComponent delay={300} />);
    rerender(<TestComponent delay={500} />);

    expect(clearTimeoutSpy).toHaveBeenCalled();
  });
});

describe("useDebouncedEffect edge cases", () => {
  it("clears timeout on rerender with same delay", () => {
    const effect = jest.fn();
    const clearSpy = jest.spyOn(global, "clearTimeout");

    function TestComponent({ delay }: { delay: number }) {
      useDebouncedEffect(effect, [delay], delay);

      return null;
    }

    const { rerender } = render(<TestComponent delay={300} />);
    rerender(<TestComponent delay={300} />);

    expect(clearSpy).toHaveBeenCalled();
  });

  it("cleans up timeout on unmount", () => {
    const effect = jest.fn();
    const clearSpy = jest.spyOn(global, "clearTimeout");

    function TestComponent({ delay }: { delay: number }) {
      useDebouncedEffect(effect, [delay], delay);

      return null;
    }

    const { unmount } = render(<TestComponent delay={300} />);
    unmount();

    expect(clearSpy).toHaveBeenCalled();
  });
});
