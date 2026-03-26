/*
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

package com.planck.planck.domain.analytics.evaluation.dto;

import com.planck.planck.annotation.ValidTimeRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlTransient;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.Data;

@Data
@ValidTimeRange(message = "Start time must be before end time")
public class UserEvalMonitoringParams {

  @Schema(
      description =
          "Starting timestamp for the query. Defaulted to current time minus 15 days. Must be before the endTime",
      example = "2025-05-14T00:00:00Z")
  private Instant startTime = Instant.now().minus(15, ChronoUnit.DAYS);

  @Schema(
      description = "Ending timestamp for the query. Defaulted to current time",
      example = "2025-06-14T00:00:00Z")
  private Instant endTime = Instant.now();

  @Schema(description = "ProjectId to filter by. By default retrieve data for all projects")
  private String projectId;

  @Schema(
      description =
          "Comma delimited list of scorers to filter by. By default retrieve data for all scorers")
  private List<String> scorerNames;

  @Schema(
      description =
          "Comma delimited list of scorer ids to filter by. By default retrieve data for all scorers")
  private List<String> scorerIds;

  @Schema(
      description =
          "Comma delimited list of tag ids to filter by. By default retrieve data irrespective of whether the monitoring is tagged or not.")
  private List<String> tagIds;

  @Schema(
      description =
          "Comma delimited list of model ids to filter by. Those are the models used on the respective Chat turns.By default retrieve data for all models")
  private List<String> modelIds;

  @XmlTransient @Deprecated private List<String> tagNames;

  @XmlTransient @Deprecated private List<String> evalModelNames;

  @XmlTransient @Deprecated private List<String> evalModelVersions;

  @XmlTransient private String sxsJoinColumnName;

  // Customs fields for handling dates
  @XmlTransient @Deprecated private Timestamp startTimeStamp;
  @XmlTransient @Deprecated private Timestamp endTimeStamp;

  @Deprecated
  public void buildDates() {
    startTimeStamp = Timestamp.from(startTime);
    endTimeStamp = Timestamp.from(endTime);
  }
}
