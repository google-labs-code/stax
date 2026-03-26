# Copyright 2025 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#  Use the official maven/Java 8 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM maven:3.9.9 as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package

# Cloud Run assumption
# https://stackoverflow.com/questions/76945843/default-startup-tcp-probe-failed-1-time-consecutively-for-container-develop-on
ENV HOST 0.0.0.0

# Use OpenJDK for base image.
FROM eclipse-temurin:17-jdk

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/target/planck-*.war /planck.war

# opentelemetry_instrumentation_agent_dockerfile
RUN wget -O /opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.31.0/opentelemetry-javaagent.jar

# Run the web service on container startup.
# TODO: troubleshoot network issue (https://cloudlogging.app.goo.gl/2K8e1qnRxbhw37bD6) with OTEL then reenable OTEL
# ENTRYPOINT ["java", "-javaagent:/opentelemetry-javaagent.jar", "-jar", "/planck.war"]
ENTRYPOINT ["java", "-DcloudSqlRefreshStrategy=lazy", "-jar", "/planck.war"]
