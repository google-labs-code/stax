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

import DateRangeInput from "@/components/DateRangeInput";
import dayjs from "@/utils/dayjsSetup";
import "@testing-library/jest-dom";
import { fireEvent, screen, waitFor } from "@testing-library/react";

import { testRender } from "../render";

describe("DateRangeInput", () => {
  it("displays preset options when opened", async () => {
    testRender(<DateRangeInput onChange={jest.fn()} />);
    const trigger = screen.getByRole("button");
    fireEvent.click(trigger);

    expect(await screen.findByText("Last 7 days")).toBeInTheDocument();
    expect(screen.getByText("Last 1 hour")).toBeInTheDocument();
  });

  it("selects a preset and calls onChange", async () => {
    const mockOnChange = jest.fn();
    testRender(<DateRangeInput onChange={mockOnChange} />);

    const trigger = screen.getByRole("button");
    fireEvent.click(trigger);

    const preset = await screen.findByText("Last 7 days");
    fireEvent.click(preset);

    await waitFor(() => {
      expect(mockOnChange).toHaveBeenCalled();
    });

    const [start, end] = mockOnChange.mock.calls[0][0];

    expect(dayjs(end).diff(start, "days")).toBe(7);
  });

  it("clears selected value when clear button is clicked", async () => {
    const mockOnChange = jest.fn();
    testRender(
      <DateRangeInput
        onChange={mockOnChange}
        defaultValue="Today"
        defaultRange={[dayjs().startOf("day"), dayjs()]}
        clearable
      />,
    );

    const clearBtn = screen.getByLabelText("Clear value");
    fireEvent.click(clearBtn);

    expect(mockOnChange).toHaveBeenCalledWith([], "");
  });

  it("toggles dropdown on trigger click", async () => {
    testRender(<DateRangeInput onChange={jest.fn()} />);

    const trigger = screen.getByRole("button");
    fireEvent.click(trigger);

    expect(await screen.findByText("Last 7 days")).toBeInTheDocument();

    fireEvent.click(trigger);
    await waitFor(() => {
      expect(screen.queryByText("Last 7 days")).toBeInTheDocument();
    });
  });

  it("cancels custom range selection and closes dropdown", async () => {
    testRender(<DateRangeInput onChange={jest.fn()} />);

    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(await screen.findByText("Custom"));

    const cancelBtn = screen.getByText("Cancel");
    fireEvent.click(cancelBtn);

    await waitFor(() => {
      expect(screen.queryByText("Apply")).not.toBeInTheDocument();
    });
  });

  it("resets range when Reset button clicked", async () => {
    const mockOnChange = jest.fn();
    testRender(
      <DateRangeInput
        onChange={mockOnChange}
        clearable
        defaultValue="Last 7 days"
        defaultRange={[dayjs().subtract(7, "days"), dayjs()]}
      />,
    );

    fireEvent.click(screen.getByText("Last 6 hours"));
    fireEvent.click(await screen.findByText("Custom"));

    const resetBtn = screen.getByText("Reset");
    fireEvent.click(resetBtn);

    expect(mockOnChange).toHaveBeenCalledWith([], "");
  });

  it("updates time dropdowns and reflects changes in selected dates", async () => {
    testRender(<DateRangeInput onChange={jest.fn()} />);

    fireEvent.click(screen.getByRole("button"));
    fireEvent.click(await screen.findByText("Custom"));

    const startTimeInput = screen.getByLabelText("Start time");
    const endTimeInput = screen.getByLabelText("End time");

    fireEvent.change(startTimeInput, { target: { value: "05:30" } });
    fireEvent.change(endTimeInput, { target: { value: "18:45" } });

    expect(startTimeInput).toHaveValue("05:30");
    expect(endTimeInput).toHaveValue("18:45");
  });
});
