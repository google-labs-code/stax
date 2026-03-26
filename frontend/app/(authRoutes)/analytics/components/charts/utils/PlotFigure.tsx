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

import * as Plot from "@observablehq/plot";
import React, { useEffect, useRef, useState } from "react";

import { TooltipContentItem } from "../../../types/charts";
import Tooltip from "./Tooltip";
import { useWindowWidth } from "./hooks";

export default function PlotFigure({
  className,
  options,
  generateTooltipContent,
}: {
  className: string;
  options: any;
  generateTooltipContent?: (dataPoint: any) => TooltipContentItem[];
}) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [tooltip, setTooltip] = useState({
    x: 0,
    y: 0,
    dataPoint: null,
  });

  const width = useWindowWidth();

  useEffect(() => {
    if (!options) return;

    const plot = Plot.plot({ ...options, width });
    containerRef.current?.append(plot);

    const handleMouseMove = (event: MouseEvent) => {
      const target = event.target as HTMLDivElement & {
        __data__: number;
      };

      if (target.tagName === "circle" || target.tagName === "rect") {
        target.classList.add("custom-z-index-dot");
        const dataIndex = target.__data__;
        const dataPoint = options.data?.[dataIndex];

        if (dataPoint) {
          const { clientX: x, clientY: y } = event;

          if (containerRef.current) {
            containerRef.current.style.cursor = "pointer";
          }

          setTooltip({
            x,
            y,
            dataPoint,
          });
        }
      } else {
        if (containerRef.current) {
          containerRef.current.style.cursor = "default";
        }
        setTooltip({ x: 0, y: 0, dataPoint: null });
      }
    };
    const handleScroll = () => {
      setTooltip({ x: 0, y: 0, dataPoint: null });
    };

    containerRef.current?.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("scroll", handleScroll, true);

    return () => {
      plot.remove();
      containerRef.current?.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("scroll", handleScroll, true);
    };
  }, [options, width]);

  return (
    <div ref={containerRef} className={className}>
      {generateTooltipContent && tooltip.dataPoint && (
        <Tooltip
          position={{ x: tooltip.x, y: tooltip.y }}
          content={generateTooltipContent(tooltip.dataPoint)}
        />
      )}
    </div>
  );
}
