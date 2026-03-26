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

package com.planck.planck.domain.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
public class UserDataDeletionRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public UserDataDeletionRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Transactional
  public Integer deleteByTableAndUserWithLimit(String tableName, String userId, Integer limit) {
    validateInputs(tableName, userId, limit);

    if (!tableExists(tableName)) {
      log.warn("Skipping deletion for non-existent table: {}", tableName);
      return 0;
    }

    // mapping tables don't have user_id, so need some special handling
    if ("turn_inputs".equals(tableName)) {
      return deleteFromTurnInputsByUser(userId, limit);
    } else if ("llm_turn_inputs".equals(tableName)) {
      return deleteFromLlmTurnInputsByUser(userId, limit);
    } else if ("pairwise_llm_turn_inputs".equals(tableName)) {
      return deleteFromPairwiseLlmTurnInputsByUser(userId, limit);
    } else if ("user".equals(tableName)) {
      return deleteFromUserTable(userId, limit);
    }

    String sql = "DELETE FROM " + tableName + " WHERE user_id = :userId LIMIT :limit";
    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    params.put("limit", limit);

    return namedParameterJdbcTemplate.update(sql, params);
  }

  @Transactional(readOnly = true)
  public Long countByTableAndUser(String tableName, String userId) {
    validateInputs(tableName, userId, null);

    if (!tableExists(tableName)) {
      log.warn("Table '{}' does not exist, returning count 0", tableName);
      return 0L;
    }

    // mapping tables don't have user_id, so need some special handling
    if ("turn_inputs".equals(tableName)) {
      return countTurnInputsByUser(userId);
    } else if ("llm_turn_inputs".equals(tableName)) {
      return countLlmTurnInputsByUser(userId);
    } else if ("pairwise_llm_turn_inputs".equals(tableName)) {
      return countPairwiseLlmTurnInputsByUser(userId);
    } else if ("user".equals(tableName)) {
      return countFromUserTable(userId);
    }

    String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE user_id = :userId";
    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
  }

  private Integer deleteFromTurnInputsByUser(String userId, Integer limit) {
    String selectSql =
        "SELECT ti.turn_id FROM turn_inputs ti INNER JOIN chat_turns ct ON ti.turn_id = ct.id WHERE ct.user_id = :userId LIMIT :limit";

    Map<String, Object> selectParams = new HashMap<>();
    selectParams.put("userId", userId);
    selectParams.put("limit", limit);

    List<String> turnInputIds =
        namedParameterJdbcTemplate.queryForList(selectSql, selectParams, String.class);

    if (turnInputIds.isEmpty()) {
      return 0;
    }

    String deleteSql = "DELETE FROM turn_inputs WHERE turn_id IN (:turnInputIds)";
    Map<String, Object> deleteParams = new HashMap<>();
    deleteParams.put("turnInputIds", turnInputIds);

    return namedParameterJdbcTemplate.update(deleteSql, deleteParams);
  }

  private Long countTurnInputsByUser(String userId) {
    String sql =
        """
        SELECT COUNT(*) FROM turn_inputs ti
        INNER JOIN chat_turns ct ON ti.turn_id = ct.id
        WHERE ct.user_id = :userId
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
  }

  private Integer deleteFromLlmTurnInputsByUser(String userId, Integer limit) {
    String selectSql =
        "SELECT lti.turn_id FROM llm_turn_inputs lti INNER JOIN llm_evaluator le ON lti.turn_id = le.id WHERE le.user_id = :userId LIMIT :limit";

    Map<String, Object> selectParams = new HashMap<>();
    selectParams.put("userId", userId);
    selectParams.put("limit", limit);

    List<String> llmTurnInputIds =
        namedParameterJdbcTemplate.queryForList(selectSql, selectParams, String.class);

    if (llmTurnInputIds.isEmpty()) {
      return 0;
    }

    String deleteSql = "DELETE FROM llm_turn_inputs WHERE turn_id IN (:llmTurnInputIds)";
    Map<String, Object> deleteParams = new HashMap<>();
    deleteParams.put("llmTurnInputIds", llmTurnInputIds);

    return namedParameterJdbcTemplate.update(deleteSql, deleteParams);
  }

  private Integer deleteFromPairwiseLlmTurnInputsByUser(String userId, Integer limit) {
    String selectSql =
        "SELECT plti.turn_id FROM pairwise_llm_turn_inputs plti INNER JOIN pairwise_llm_evaluator ple ON plti.turn_id = ple.id WHERE ple.user_id = :userId LIMIT :limit";

    Map<String, Object> selectParams = new HashMap<>();
    selectParams.put("userId", userId);
    selectParams.put("limit", limit);

    List<String> pairwiseLlmTurnInputIds =
        namedParameterJdbcTemplate.queryForList(selectSql, selectParams, String.class);

    if (pairwiseLlmTurnInputIds.isEmpty()) {
      return 0;
    }

    String deleteSql =
        "DELETE FROM pairwise_llm_turn_inputs WHERE turn_id IN (:pairwiseLlmTurnInputIds)";
    Map<String, Object> deleteParams = new HashMap<>();
    deleteParams.put("pairwiseLlmTurnInputIds", pairwiseLlmTurnInputIds);

    return namedParameterJdbcTemplate.update(deleteSql, deleteParams);
  }

  private Long countLlmTurnInputsByUser(String userId) {
    String sql =
        """
        SELECT COUNT(*) FROM llm_turn_inputs lti
        INNER JOIN llm_evaluator le ON lti.turn_id = le.id
        WHERE le.user_id = :userId
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
  }

  private Long countPairwiseLlmTurnInputsByUser(String userId) {
    String sql =
        """
        SELECT COUNT(*) FROM pairwise_llm_turn_inputs plti
        INNER JOIN pairwise_llm_evaluator ple ON plti.turn_id = ple.id
        WHERE ple.user_id = :userId
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
  }

  private Integer deleteFromUserTable(String userId, Integer limit) {
    String sql = "DELETE FROM user WHERE id = :userId LIMIT :limit";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    params.put("limit", limit);

    return namedParameterJdbcTemplate.update(sql, params);
  }

  private Long countFromUserTable(String userId) {
    String sql = "SELECT COUNT(*) FROM user WHERE id = :userId";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
  }

  private void validateInputs(String tableName, String userId, Integer limit) {
    if (tableName == null || tableName.trim().isEmpty()) {
      throw new IllegalArgumentException("Table name cannot be null or empty");
    }

    if (userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    if (limit != null && limit <= 0) {
      throw new IllegalArgumentException("Limit must be a positive integer");
    }
  }

  private boolean tableExists(String tableName) {
    try {
      String sql =
          """
          SELECT COUNT(*) > 0
          FROM information_schema.tables
          WHERE table_name = :tableName
          AND table_schema = (SELECT DATABASE())
          """;

      Map<String, Object> params = new HashMap<>();
      params.put("tableName", tableName);

      Boolean exists = namedParameterJdbcTemplate.queryForObject(sql, params, Boolean.class);
      if (!Boolean.TRUE.equals(exists)) {
        log.warn("Table '{}' does not exist in the database", tableName);
        return false;
      }
      return true;
    } catch (Exception e) {
      log.error("Error checking if table '{}' exists", tableName, e);
      return false;
    }
  }
}
