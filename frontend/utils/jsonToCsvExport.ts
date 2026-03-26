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

export const jsonToCsvExport = (data: any[], filename?: string): void => {
  const escapeField = (field: string): string => {
    let stringField = String(field);
    // Replace newlines with a space
    stringField = stringField.replace(/\r?\n/g, " ");
    if (
      stringField.includes(",") ||
      stringField.includes('"') ||
      stringField.includes("\n") ||
      stringField.includes("\r")
    ) {
      return `"${stringField.replace(/"/g, '""')}"`;
    }

    return stringField;
  };
  const rows: string[] = [];
  const columns: string[] = [];
  for (const dataRow of data) {
    for (const column in dataRow) {
      if (!columns.includes(column)) {
        columns.push(column);
      }
    }
  }
  rows.push(columns.join(","));
  data.forEach((item) => {
    const rowData = columns.map((column) => {
      let finalValue = item[column];
      if (finalValue === null || finalValue === undefined) {
        finalValue = "";
      }

      return escapeField(
        typeof finalValue === "object"
          ? JSON.stringify(finalValue)
          : finalValue,
      );
    });
    rows.push(rowData.join(","));
  });
  const csvContent = rows.join("\r\n");
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute(
    "download",
    filename || `export-${new Date().toISOString().slice(0, 10)}.csv`,
  );
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};
