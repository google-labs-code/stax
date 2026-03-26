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

package com.planck.planck.domain.monitoringapp.notifications;

import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.Timestamp;
import com.planck.planck.entitities.UserDeletionStatus;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile("monitoring")
public class GCPMonitoringUserDeletionNotificationService
    implements UserDeletionNotificationService {

  private static final Logger logger =
      LoggerFactory.getLogger(GCPMonitoringUserDeletionNotificationService.class);
  private static final String METRIC_TYPE =
      "custom.googleapis.com/user_deletion/stale_requests_count";

  @Value("${spring.cloud.gcp.projectId:}")
  private String projectId;

  private MetricServiceClient metricServiceClient;

  public GCPMonitoringUserDeletionNotificationService() {
    initializeMetricServiceClient();
  }

  private void initializeMetricServiceClient() {
    try {
      GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
      MetricServiceSettings settings =
          MetricServiceSettings.newBuilder()
              .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
              .build();
      this.metricServiceClient = MetricServiceClient.create(settings);
      logger.info("GCP Monitoring client initialized successfully");
    } catch (IOException e) {
      logger.error("Failed to initialize GCP Monitoring client", e);
      this.metricServiceClient = null;
    }
  }

  @Override
  public void sendStaleUserDeletionNotification(List<UserDeletionStatus> staleUserDeletions) {
    int staleCount = staleUserDeletions != null ? staleUserDeletions.size() : 0;
    logger.info(
        "GCPMonitoringUserDeletionNotificationService: Publishing metric for {} stale user deletions",
        staleCount);

    if (metricServiceClient == null) {
      logger.warn("GCP Monitoring client not available, skipping metric publication");
      return;
    }

    if (projectId == null || projectId.isEmpty()) {
      logger.warn("GCP project ID not configured, skipping metric publication");
      return;
    }

    try {
      publishStaleUserDeletionMetric(staleCount);
      logger.info("Successfully published stale user deletion metric: {}", staleCount);
    } catch (Exception e) {
      logger.error("Failed to publish stale user deletion metric", e);
    }
  }

  private void publishStaleUserDeletionMetric(int staleCount) throws IOException {
    ProjectName projectName = ProjectName.of(projectId);

    TimeInterval interval =
        TimeInterval.newBuilder()
            .setEndTime(
                Timestamp.newBuilder()
                    .setSeconds(Instant.now().getEpochSecond())
                    .setNanos(Instant.now().getNano())
                    .build())
            .build();

    Point point =
        Point.newBuilder()
            .setInterval(interval)
            .setValue(TypedValue.newBuilder().setInt64Value(staleCount).build())
            .build();

    TimeSeries timeSeries =
        TimeSeries.newBuilder()
            .setMetric(Metric.newBuilder().setType(METRIC_TYPE).build())
            .setResource(MonitoredResource.newBuilder().setType("global").build())
            .addPoints(point)
            .build();

    CreateTimeSeriesRequest request =
        CreateTimeSeriesRequest.newBuilder()
            .setName(projectName.toString())
            .addTimeSeries(timeSeries)
            .build();

    metricServiceClient.createTimeSeries(request);
  }
}
