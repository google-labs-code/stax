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
  usePDFExport,
  useWindowWidth,
} from "@/app/(authRoutes)/analytics/components/charts/utils/hooks";
import { act, renderHook } from "@testing-library/react";
import html2canvas from "html2canvas";

describe("useWindowWidth", () => {
  it("returns current window width", () => {
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 1024,
    });

    const { result } = renderHook(() => useWindowWidth());

    expect(result.current).toBe(1024);

    act(() => {
      window.innerWidth = 768;
      window.dispatchEvent(new Event("resize"));
    });

    expect(result.current).toBe(768);
  });
});

jest.mock("html2canvas", () =>
  jest.fn().mockResolvedValue({
    toDataURL: () => "mocked-image",
    width: 1000,
    height: 2000,
  }),
);

const saveMock = jest.fn();
const addImageMock = jest.fn();
const addPageMock = jest.fn();

jest.mock("jspdf", () => {
  return jest.fn().mockImplementation(() => ({
    internal: {
      pageSize: {
        getWidth: () => 595.28,
        getHeight: () => 841.89,
      },
    },
    addImage: addImageMock,
    addPage: addPageMock,
    save: saveMock,
  }));
});

describe("usePDFExport", () => {
  let ref: any;

  beforeEach(() => {
    saveMock.mockClear();
    addImageMock.mockClear();
    addPageMock.mockClear();

    ref = {
      current: document.createElement("div"),
    };

    const legendEl = document.createElement("div");
    legendEl.className = "plot-legend-swatch";
    document.body.appendChild(legendEl);
    document.body.appendChild(ref.current);
  });

  it("calls all internal functions and saves PDF", async () => {
    const { result } = renderHook(() => usePDFExport());

    await act(async () => {
      await result.current.exportPDF(ref, "my-report");
    });

    expect(ref.current.classList.contains("freeze-chart")).toBe(false);
    expect(addImageMock).toHaveBeenCalled();
    expect(saveMock).toHaveBeenCalledWith(
      expect.stringMatching(/my-report-\d{4}-\d{2}-\d{2}.pdf/),
    );
  });

  it("returns early if ref.current is null", async () => {
    const { result } = renderHook(() => usePDFExport());

    await act(async () => {
      await result.current.exportPDF({ current: null }, "skip");
    });

    expect(saveMock).not.toHaveBeenCalled();
  });

  it("handles html2canvas error", async () => {
    (html2canvas as jest.Mock).mockRejectedValueOnce(new Error("fail"));

    const { result } = renderHook(() => usePDFExport());

    await act(async () => {
      await result.current.exportPDF(ref, "error-test");
    });

    expect(saveMock).not.toHaveBeenCalled();
    expect(ref.current.classList.contains("freeze-chart")).toBe(false);
  });
});
