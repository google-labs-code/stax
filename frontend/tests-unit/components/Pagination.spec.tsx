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

import Pagination from "@/components/Pagination";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../render";

describe("Pagination", () => {
  const onPageChangeMock = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders current page and total pages correctly", () => {
    testRender(
      <Pagination
        currentPage={2}
        nextPageToken={3}
        totalPages={5}
        onPageChange={onPageChangeMock}
      />,
    );

    expect(screen.getByText("2 of 5")).toBeInTheDocument();
  });

  it("calls onPageChange with next page when clicking on the chevron_right icon span", () => {
    testRender(
      <Pagination
        currentPage={2}
        nextPageToken={3}
        totalPages={5}
        onPageChange={onPageChangeMock}
      />,
    );

    const iconSpans = screen.getAllByTestId("material-icon");
    const nextIconSpan = iconSpans.find(
      (el) => el.textContent === "chevron_right",
    );
    expect(nextIconSpan).toBeDefined();

    fireEvent.click(nextIconSpan!);

    expect(onPageChangeMock).toHaveBeenCalledWith(3);
  });

  it("does NOT call onPageChange when clicking next page icon if no nextPageToken", () => {
    testRender(
      <Pagination
        currentPage={5}
        nextPageToken={null}
        totalPages={5}
        onPageChange={onPageChangeMock}
      />,
    );

    const iconSpans = screen.getAllByTestId("material-icon");
    const nextIconSpan = iconSpans.find(
      (el) => el.textContent === "chevron_right",
    );
    expect(nextIconSpan).toBeDefined();

    fireEvent.click(nextIconSpan!);

    expect(onPageChangeMock).not.toHaveBeenCalled();
  });

  it("calls onPageChange with previous page when clicking on the chevron_left icon span", () => {
    testRender(
      <Pagination
        currentPage={3}
        nextPageToken={4}
        totalPages={5}
        onPageChange={onPageChangeMock}
      />,
    );

    const iconSpans = screen.getAllByTestId("material-icon");
    const prevIconSpan = iconSpans.find(
      (el) => el.textContent === "chevron_left",
    );
    expect(prevIconSpan).toBeDefined();

    fireEvent.click(prevIconSpan!);

    expect(onPageChangeMock).toHaveBeenCalledWith(2);
  });

  it("does NOT call onPageChange when clicking previous page icon on first page", () => {
    testRender(
      <Pagination
        currentPage={1}
        nextPageToken={2}
        totalPages={5}
        onPageChange={onPageChangeMock}
      />,
    );

    const iconSpans = screen.getAllByTestId("material-icon");
    const prevIconSpan = iconSpans.find(
      (el) => el.textContent === "chevron_left",
    );
    expect(prevIconSpan).toBeDefined();

    fireEvent.click(prevIconSpan!);

    expect(onPageChangeMock).not.toHaveBeenCalled();
  });
});
