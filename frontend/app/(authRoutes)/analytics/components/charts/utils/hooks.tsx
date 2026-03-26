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

import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import { RefObject, useEffect, useState } from "react";

export function useWindowWidth(): number | undefined {
  const [width, setWidth] = useState<number | undefined>(undefined);

  useEffect(() => {
    const handleResize = () => {
      setWidth(window.innerWidth);
    };
    handleResize();
    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  return width;
}

export function usePDFExport() {
  const [isGenerating, setIsGenerating] = useState(false);

  const exportPDF = async (
    ref: RefObject<HTMLElement>,
    baseFileName = "report",
  ) => {
    if (!ref.current) return;

    setIsGenerating(true);

    try {
      ref.current.classList.add("freeze-chart");

      const plots = document.querySelectorAll(
        "[class^='plot-'][class*='swatch']",
      );
      plots.forEach((el) => {
        el.classList.add("custom-legend-swatch");
      });

      const canvas = await html2canvas(ref.current, {
        scale: 2,
        useCORS: true,
        scrollY: -window.scrollY,
        windowWidth: document.body.scrollWidth,
      });

      const image = canvas.toDataURL("image/png");
      const pdf = new jsPDF("p", "pt", "a4");
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = pdf.internal.pageSize.getHeight();
      const imgHeight = (canvas.height * pdfWidth) / canvas.width;

      let position = 0;
      while (position < imgHeight) {
        pdf.addImage(image, "PNG", 0, -position, pdfWidth, imgHeight);
        position += pdfHeight;
        if (position < imgHeight) pdf.addPage();
      }

      const date = new Date().toISOString().split("T")[0];
      pdf.save(`${baseFileName}-${date}.pdf`);
    } catch {
      setIsGenerating(false);
    } finally {
      ref.current.classList.remove("freeze-chart");
      setIsGenerating(false);
    }
  };

  return { exportPDF, isGenerating };
}
