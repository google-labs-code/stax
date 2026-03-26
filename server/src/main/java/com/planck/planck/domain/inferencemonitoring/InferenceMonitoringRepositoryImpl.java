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

package com.planck.planck.domain.inferencemonitoring;

import com.planck.planck.domain.analytics.inference.dto.TimeSeriesAnalyticsDTO;
import com.planck.planck.enums.AggregationWindow;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.util.DbResultParserUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class InferenceMonitoringRepositoryImpl implements InferenceMonitoringRepositoryCustom {

  private static final Logger log =
      LoggerFactory.getLogger(InferenceMonitoringRepositoryImpl.class);

  @PersistenceContext private EntityManager entityManager;

  // --- Existing Formatters ---
  private static final DateTimeFormatter MYSQL_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter MYSQL_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter SQLITE_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter SQLITE_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Override
  public List<TimeSeriesAnalyticsDTO> getAnalyticsTimeSeries(
      String modelId,
      String userId,
      String projectId,
      ModelProvider modelProvider,
      Instant startDate,
      Instant endDate,
      AggregationWindow window,
      List<String> tagIds) {

    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date cannot be null.");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date.");
    }

    // --- 1. Detect Database Dialect ---
    Dialect dialect = getDialect();
    boolean isMySQL = dialect instanceof MySQLDialect;

    log.debug("Detected DB Dialect: {}", dialect.getClass().getSimpleName());

    // --- 2. Build Native SQL Query Conditionally ---
    // ... (SQL building logic remains largely the same as your original code) ...
    // ... (Make sure the windowStartExpressionSql and expectedInputFormat are set correctly) ...
    StringBuilder sqlSelect = new StringBuilder("SELECT ");
    StringBuilder sqlFrom = new StringBuilder("FROM inference_monitoring im "); // Use table name
    StringBuilder sqlWhere = new StringBuilder("WHERE 1=1 ");
    StringBuilder sqlGroupBy = new StringBuilder();
    StringBuilder sqlOrderBy =
        new StringBuilder("ORDER BY window_start ASC"); // Sorting in SQL helps initial mapping

    String windowStartExpressionSql;
    DateTimeFormatter expectedInputFormat = null; // To parse the result back

    // --- Start of Dialect-Specific Logic ---
    if (!isMySQL) {
      // --- SQLite Logic (Copied from your code) ---
      log.debug("Using SQLite functions for time window (handling epoch milliseconds).");
      String dateTimeExpr = "datetime(im.created_at / 1000, 'unixepoch')"; // Base conversion
      switch (window) {
        case PER_SECOND:
          windowStartExpressionSql = "strftime('%Y-%m-%d %H:%M:%S', " + dateTimeExpr + ")";
          expectedInputFormat = SQLITE_DATETIME_FORMAT;
          break;
        case PER_15_SECONDS:
          windowStartExpressionSql = "datetime(((im.created_at / 1000) / 15) * 15, 'unixepoch')";
          expectedInputFormat = SQLITE_DATETIME_FORMAT;
          break;
        case PER_MINUTE:
          windowStartExpressionSql = "strftime('%Y-%m-%d %H:%M:00', " + dateTimeExpr + ")";
          expectedInputFormat = SQLITE_DATETIME_FORMAT;
          break;
        case HOURLY:
          windowStartExpressionSql = "strftime('%Y-%m-%d %H:00:00', " + dateTimeExpr + ")";
          expectedInputFormat = SQLITE_DATETIME_FORMAT;
          break;
        case WEEKLY:
          // Note: SQLite's WEEKLY strftime behaviour (%W depends on start day) might need careful
          // alignment with Java padding logic
          windowStartExpressionSql =
              "strftime('%Y-%m-%d', "
                  + dateTimeExpr
                  + ", 'weekday 0', '-6 days')"; // Start of week (Sunday) - Adjust if Monday needed
          expectedInputFormat = SQLITE_DATE_FORMAT; // Represents start of week
          log.warn(
              "Using simplified SQLite WEEKLY start date (Sunday). Ensure alignment if needed.");
          break;
        case MONTHLY:
          windowStartExpressionSql = "strftime('%Y-%m-01', " + dateTimeExpr + ")";
          expectedInputFormat = SQLITE_DATE_FORMAT; // Represents start of month
          break;
        case DAILY:
        default:
          windowStartExpressionSql = "date(" + dateTimeExpr + ")"; // YYYY-MM-DD
          expectedInputFormat = SQLITE_DATE_FORMAT;
          break;
      }
    } else if (isMySQL) {
      // --- MySQL Specific Functions (Copied from your code) ---
      log.debug("Using MySQL functions for time window.");
      switch (window) {
        case PER_SECOND:
          windowStartExpressionSql = "DATE_FORMAT(im.created_at, '%Y-%m-%d %H:%i:%S')";
          expectedInputFormat = MYSQL_DATETIME_FORMAT;
          break;
        case PER_15_SECONDS:
          windowStartExpressionSql =
              "FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(im.created_at) / 15) * 15)";
          expectedInputFormat = MYSQL_DATETIME_FORMAT;
          break;
        case PER_MINUTE:
          windowStartExpressionSql = "DATE_FORMAT(im.created_at, '%Y-%m-%d %H:%i:00')";
          expectedInputFormat = MYSQL_DATETIME_FORMAT;
          break;
        case HOURLY:
          windowStartExpressionSql = "DATE_FORMAT(im.created_at, '%Y-%m-%d %H:00:00')";
          expectedInputFormat = MYSQL_DATETIME_FORMAT;
          break;
        case WEEKLY:
          // Mode 1: Week starts on Monday (MySQL %X%V behaviour)
          windowStartExpressionSql =
              "STR_TO_DATE(CONCAT(YEARWEEK(im.created_at, 1), ' Monday'), '%X%V %W')";
          expectedInputFormat = null; // DB returns a timestamp type directly
          break;
        case MONTHLY:
          windowStartExpressionSql = "DATE_FORMAT(im.created_at, '%Y-%m-01')";
          expectedInputFormat = MYSQL_DATE_FORMAT; // Represents start of month
          break;
        case DAILY:
        default:
          windowStartExpressionSql = "DATE(im.created_at)";
          expectedInputFormat = MYSQL_DATE_FORMAT; // Represents the date
          break;
      }
    } else {
      log.error(
          "Unsupported database dialect: {}. Cannot generate correct time series query.",
          dialect.getClass().getName());
      throw new UnsupportedOperationException(
          "Database dialect not supported for time series analytics: "
              + dialect.getClass().getName());
    }
    // --- End of Dialect-Specific Logic ---

    // --- 3. Complete SQL Query ---
    sqlSelect.append(windowStartExpressionSql).append(" as window_start, ");
    sqlSelect.append("   COUNT(im.id), "); // index 1
    sqlSelect.append("   SUM(im.turn_time_taken), "); // index 2
    sqlSelect.append("   AVG(im.turn_time_taken), "); // index 3
    sqlSelect.append("   SUM(im.turn_prompt_tokens), "); // index 4
    sqlSelect.append("   SUM(im.turn_completion_tokens), "); // index 5
    sqlSelect.append("   SUM(im.turn_total_tokens), "); // index 6
    sqlSelect.append("   AVG(im.avg_chat_latency), "); // index 7
    sqlSelect.append("   SUM(im.chat_total_prompt_tokens), "); // index 8
    sqlSelect.append("   SUM(im.chat_total_completion_tokens), "); // index 9
    sqlSelect.append("   SUM(im.chat_total_tokens) "); // index 10

    // --- 4. Build WHERE clause and parameters map ---
    Map<Integer, Object> parameters = new HashMap<>();
    int paramIndex = 1;

    sqlWhere.append("AND im.created_at >= ?").append(paramIndex).append(" ");
    parameters.put(paramIndex++, Timestamp.from(startDate));

    sqlWhere
        .append("AND im.created_at < ?")
        .append(paramIndex)
        .append(" "); // Use < for end date when grouping
    parameters.put(
        paramIndex++,
        Timestamp.from(
            endDate.plus(
                Duration.ofMillis(
                    1)))); // Add 1ms to include the end date if it falls exactly on a boundary

    // Add optional filters (Copied from your code)
    if (StringUtils.hasText(modelId)) {
      sqlWhere.append("AND im.model_id = ?").append(paramIndex).append(" ");
      parameters.put(paramIndex++, modelId);
    }
    if (StringUtils.hasText(userId)) {
      sqlWhere.append("AND im.user_id = ?").append(paramIndex).append(" ");
      parameters.put(paramIndex++, userId);
    }
    if (StringUtils.hasText(projectId)) {
      sqlWhere.append("AND im.container_id = ?").append(paramIndex).append(" ");
      parameters.put(paramIndex++, projectId);
    }
    if (modelProvider != null) {
      sqlWhere.append("AND im.model_provider = ?").append(paramIndex).append(" ");
      parameters.put(paramIndex++, modelProvider.name());
    }

    if (tagIds != null && !tagIds.isEmpty()) {
      sqlWhere.append(
          "AND (SELECT COUNT(DISTINCT tl.tag_id) FROM tag_link tl WHERE tl.target_id = im.id ");
      sqlWhere
          .append("AND tl.target_type = 'INFERENCE_MONITORING' AND tl.tag_id IN ?")
          .append(paramIndex);
      parameters.put(paramIndex++, tagIds);
      sqlWhere.append(") >= ?").append(paramIndex).append(" ");
      parameters.put(paramIndex++, tagIds.size());
    }

    // --- 5. Set GROUP BY ---
    sqlGroupBy.append("GROUP BY window_start ");

    // --- 6. Assemble final SQL ---
    String finalSql =
        sqlSelect.toString()
            + sqlFrom.toString()
            + sqlWhere.toString()
            + sqlGroupBy.toString()
            + sqlOrderBy
                .toString(); // ORDER BY helps map processing slightly but final sort is done in
    // Java

    log.debug(
        "Executing Native SQL for Dialect [{}]: {}", dialect.getClass().getSimpleName(), finalSql);
    log.debug("Parameter keys: {}", parameters.keySet());

    // --- 7. Create and Execute Native Query ---
    Query query = entityManager.createNativeQuery(finalSql);
    for (Map.Entry<Integer, Object> entry : parameters.entrySet()) {
      query.setParameter(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    List<Object[]> rawResults = query.getResultList();
    log.info("Native query returned {} raw results.", rawResults.size());

    // --- 8. Manually Map Results to DTO ---
    // This list contains DTOs ONLY for windows with actual data
    List<TimeSeriesAnalyticsDTO> actualResults =
        mapNativeResultToDTO(rawResults, window, expectedInputFormat, !isMySQL, isMySQL);
    log.info("Mapped {} DTOs from raw results.", actualResults.size());

    // --- 9. Padding Logic ---
    List<TimeSeriesAnalyticsDTO> paddedResults =
        addPadding(actualResults, startDate, endDate, window);

    log.info("Returning {} results after padding.", paddedResults.size());
    return paddedResults;
  }

  // Helper method to get the dialect (Copied from your code)
  private Dialect getDialect() {
    Session session = entityManager.unwrap(Session.class);
    SessionFactoryImplementor sfi = (SessionFactoryImplementor) session.getSessionFactory();
    return sfi.getJdbcServices().getDialect();
  }

  // Helper method for mapping results (Copied from your code, ensure windowStartTs is not null)
  private List<TimeSeriesAnalyticsDTO> mapNativeResultToDTO(
      List<Object[]> rawResults,
      AggregationWindow window,
      DateTimeFormatter expectedInputFormat,
      boolean isSQLite, // Pass dialect info if needed for parsing edge cases
      boolean isMySQL) {
    List<TimeSeriesAnalyticsDTO> resultList = new ArrayList<>();
    for (Object[] row : rawResults) {
      // (Indices based on SELECT clause)
      Object rawWindowStart = row[0];
      Long count = DbResultParserUtils.getLongFromResult(row[1]);
      Double sumTurnTime = DbResultParserUtils.getDoubleFromResult(row[2]);
      Double avgTurnTime = DbResultParserUtils.getDoubleFromResult(row[3]);
      Long sumPrompt = DbResultParserUtils.getLongFromResult(row[4]);
      Long sumCompletion = DbResultParserUtils.getLongFromResult(row[5]);
      Long sumTotal = DbResultParserUtils.getLongFromResult(row[6]);
      Double avgChatLatency = DbResultParserUtils.getDoubleFromResult(row[7]);
      Long sumChatPrompt = DbResultParserUtils.getLongFromResult(row[8]);
      Long sumChatCompletion = DbResultParserUtils.getLongFromResult(row[9]);
      Long sumChatTotal = DbResultParserUtils.getLongFromResult(row[10]);

      try {
        Timestamp windowStartTs = null;

        if (rawWindowStart instanceof Timestamp) {
          windowStartTs = (Timestamp) rawWindowStart;
        } else {
          String windowStartStr = Objects.toString(rawWindowStart, null);
          if (windowStartStr != null) {
            if (expectedInputFormat != null) {
              TemporalAccessor temporalAccessor =
                  expectedInputFormat.parseBest(
                      windowStartStr, LocalDateTime::from, LocalDate::from);
              if (temporalAccessor instanceof LocalDateTime) {
                windowStartTs = Timestamp.valueOf((LocalDateTime) temporalAccessor);
              } else if (temporalAccessor instanceof LocalDate) {
                windowStartTs = Timestamp.valueOf(((LocalDate) temporalAccessor).atStartOfDay());
              }
            } else if (window == AggregationWindow.WEEKLY && isSQLite && !isMySQL) {
              // Handle SQLite YYYY-MM-DD format for week start (assuming it was generated)
              try {
                LocalDate weekStartDate = LocalDate.parse(windowStartStr, SQLITE_DATE_FORMAT);
                windowStartTs = Timestamp.valueOf(weekStartDate.atStartOfDay());
                log.debug(
                    "Parsed SQLite WEEKLY date string '{}' to Timestamp {}",
                    windowStartStr,
                    windowStartTs);
              } catch (DateTimeParseException e) {
                log.warn(
                    "Could not parse SQLite WEEKLY date string '{}' with format {}.",
                    windowStartStr,
                    SQLITE_DATE_FORMAT,
                    e);
              }
            } else if (window == AggregationWindow.WEEKLY && isMySQL && !isSQLite) {
              // MySQL weekly handled by instanceof Timestamp check above if STR_TO_DATE worked
              log.debug(
                  "MySQL WEEKLY raw value type: {}",
                  rawWindowStart != null ? rawWindowStart.getClass().getName() : "null");
              // Add fallback parsing if needed
            } else {
              log.warn(
                  "No specific parsing logic for window '{}', dialect SQLite={}, MySQL={}, raw value '{}'",
                  window,
                  isSQLite,
                  isMySQL,
                  windowStartStr);
            }
          }
        }

        // If windowStartTs is still null after parsing attempts, skip this row or log error
        if (windowStartTs == null) {
          log.error(
              "Could not determine windowStart Timestamp for row. sumTurnTime: {}, count: {}",
              (sumTurnTime != null ? sumTurnTime : "null"),
              (count != null ? count : "null")); // Avoid logging full row which may contain PII
          continue; // Skip this row
        }

        TimeSeriesAnalyticsDTO dto =
            new TimeSeriesAnalyticsDTO(
                windowStartTs,
                count,
                sumTurnTime,
                avgTurnTime,
                sumPrompt,
                sumCompletion,
                sumTotal,
                avgChatLatency,
                sumChatPrompt,
                sumChatCompletion,
                sumChatTotal);
        resultList.add(dto);

      } catch (Exception e) { // Catch broader exceptions during mapping
        log.error(
            "Error mapping row to TimeSeriesAnalyticsDTO. Row length: {}. Error type: {}",
            row != null ? row.length : "N/A", // Avoid logging full row which may contain PII.
            e);
      }
    }
    return resultList;
  }

  // --- Padding Logic Implementation ---

  private List<TimeSeriesAnalyticsDTO> addPadding(
      List<TimeSeriesAnalyticsDTO> actualResults,
      Instant overallStartDate,
      Instant overallEndDate,
      AggregationWindow window) {

    Map<Instant, TimeSeriesAnalyticsDTO> actualResultsMap =
        actualResults.stream()
            .filter(dto -> dto.getWindowStart() != null) // Ensure timestamp is not null
            .collect(
                Collectors.toMap(
                    // Add explicit types INSIDE the lambdas
                    (TimeSeriesAnalyticsDTO dto) -> dto.getWindowStart(),
                    (TimeSeriesAnalyticsDTO dto) -> dto,
                    (TimeSeriesAnalyticsDTO dto1,
                        TimeSeriesAnalyticsDTO dto2) -> { // Add types here too
                      log.warn(
                          "Duplicate Instant found: {}. Keeping first encountered.",
                          dto1.getWindowStart());
                      return dto1;
                    },
                    LinkedHashMap::new // Keep the supplier for LinkedHashMap
                    ));

    // 2. Generate all expected window start Instants
    List<Instant> expectedInstants =
        generateExpectedWindowInstants(overallStartDate, overallEndDate, window);

    // 3. Iterate expected instants and build the final list
    List<TimeSeriesAnalyticsDTO> paddedList = new ArrayList<>();
    for (Instant expectedInstant : expectedInstants) {
      TimeSeriesAnalyticsDTO dto = actualResultsMap.get(expectedInstant);
      if (dto != null) {
        // Found data for this window
        paddedList.add(dto);
      } else {
        // No data found, create a zero-filled DTO
        paddedList.add(createZeroDto(expectedInstant));
      }
    }

    // 4. Ensure final list is sorted by window start time (optional if generation and map are
    // ordered)
    paddedList.sort(Comparator.comparing(TimeSeriesAnalyticsDTO::getWindowStart));

    return paddedList;
  }

  // --- Helper to generate expected window start Instants ---
  private List<Instant> generateExpectedWindowInstants(
      Instant overallStartDate, Instant overallEndDate, AggregationWindow window) {
    List<Instant> instants = new ArrayList<>();
    if (overallStartDate.isAfter(overallEndDate)) {
      return instants; // Empty list if dates are invalid
    }

    // Use ZonedDateTime for calendar-based truncation and addition (UTC is safest)
    ZonedDateTime currentWindowStart = ZonedDateTime.ofInstant(overallStartDate, ZoneOffset.UTC);
    ZonedDateTime endZoned = ZonedDateTime.ofInstant(overallEndDate, ZoneOffset.UTC);

    // Truncate the start time to the beginning of its window
    currentWindowStart = truncateToWindowStart(currentWindowStart, window);

    while (!currentWindowStart.isAfter(endZoned)) {
      // Add the *start* of the current window
      instants.add(currentWindowStart.toInstant());

      // Increment to the *start* of the next window
      currentWindowStart = incrementWindow(currentWindowStart, window);

      // Safety break for unexpected loops (e.g., zero duration)
      if (window == AggregationWindow.PER_SECOND && instants.size() > 1000000) { // Example limit
        log.warn(
            "Reached excessive number of instants for PER_SECOND window. Stopping generation.");
        break;
      }
      // Add similar checks for other high-frequency windows if needed
    }

    log.debug(
        "Generated {} expected window instants from {} to {} with window {}",
        instants.size(),
        overallStartDate,
        overallEndDate,
        window);
    return instants;
  }

  // --- Helper to truncate ZonedDateTime to the start of its window ---
  private ZonedDateTime truncateToWindowStart(ZonedDateTime zdt, AggregationWindow window) {
    switch (window) {
      case PER_SECOND:
        return zdt.truncatedTo(ChronoUnit.SECONDS);
      case PER_15_SECONDS:
        long epochSecond = zdt.toEpochSecond();
        long truncatedEpochSecond = (epochSecond / 15) * 15;
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(truncatedEpochSecond), zdt.getZone());
      case PER_MINUTE:
        return zdt.truncatedTo(ChronoUnit.MINUTES);
      case HOURLY:
        return zdt.truncatedTo(ChronoUnit.HOURS);
      case DAILY:
        return zdt.truncatedTo(ChronoUnit.DAYS);
      case WEEKLY:
        // Adjust to start of the week (e.g., Monday ISO standard)
        return zdt.truncatedTo(ChronoUnit.DAYS)
            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
      // Or use Sunday: .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
      // **Important**: Ensure this matches the SQL logic's week start definition!
      case MONTHLY:
        return zdt.truncatedTo(ChronoUnit.DAYS).with(TemporalAdjusters.firstDayOfMonth());
      default:
        log.warn("Unhandled AggregationWindow in truncateToWindowStart: {}", window);
        return zdt; // No truncation
    }
  }

  // --- Helper to increment ZonedDateTime by one window ---
  private ZonedDateTime incrementWindow(ZonedDateTime zdt, AggregationWindow window) {
    switch (window) {
      case PER_SECOND:
        return zdt.plusSeconds(1);
      case PER_15_SECONDS:
        return zdt.plusSeconds(15);
      case PER_MINUTE:
        return zdt.plusMinutes(1);
      case HOURLY:
        return zdt.plusHours(1);
      case DAILY:
        return zdt.plusDays(1);
      case WEEKLY:
        return zdt.plusWeeks(1);
      case MONTHLY:
        return zdt.plusMonths(1);
      default:
        log.warn("Unhandled AggregationWindow in incrementWindow: {}. No increment.", window);
        return zdt.plusNanos(1); // Prevent infinite loop in worst case
    }
  }

  private TimeSeriesAnalyticsDTO createZeroDto(Instant windowStartInstant) {
    return new TimeSeriesAnalyticsDTO(
        Timestamp.from(windowStartInstant), // Convert Instant back to Timestamp for DTO
        0L, // totalInferences
        0.0, // totalTurnTimeTaken
        0.0, // averageTurnTimeTaken
        0L, // totalTurnPromptTokens
        0L, // totalTurnCompletionTokens
        0L, // totalTurnTotalTokens
        0.0, // averageAvgChatLatency
        0L, // totalChatPromptTokens
        0L, // totalChatCompletionTokens
        0L // totalChatTotalTokens
        );
  }
}
