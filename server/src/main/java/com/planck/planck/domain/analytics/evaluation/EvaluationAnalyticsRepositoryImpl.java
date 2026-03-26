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

package com.planck.planck.domain.analytics.evaluation;

import com.planck.planck.domain.analytics.evaluation.dto.EvaluationAnalyticsRowDTO;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalMonitoringParams;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class EvaluationAnalyticsRepositoryImpl implements EvaluationAnalyticsRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<EvaluationAnalyticsRowDTO> getAnalyticsData(
      UserEvalMonitoringParams params, User user) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<EvaluationAnalyticsRowDTO> cq = cb.createQuery(EvaluationAnalyticsRowDTO.class);

    Root<ScoreV2> scoreRoot = cq.from(ScoreV2.class);
    Join<ScoreV2, EvaluationStatus> evalStatusJoin = scoreRoot.join("evaluationStatus");
    Join<ScoreV2, EvaluationMonitoring> evalMonitoringJoin =
        scoreRoot.join("evaluationMonitoring", JoinType.LEFT);

    Join<ScoreV2, ModelResponse> modelResponseJoin = null;

    if (params.getSxsJoinColumnName() != null && !params.getSxsJoinColumnName().isEmpty()) {
      modelResponseJoin = scoreRoot.join("modelResponse");
    }

    Predicate predicate =
        buildPredicates(
            cb, cq, scoreRoot, evalStatusJoin, evalMonitoringJoin, modelResponseJoin, params, user);

    cq.select(
        cb.construct(
            EvaluationAnalyticsRowDTO.class,
            scoreRoot.get("score"),
            scoreRoot.get("scorer"),
            scoreRoot.get("llmEvaluator").get("id"),
            evalStatusJoin.get("status"),
            evalMonitoringJoin.get("promptTokens"),
            evalMonitoringJoin.get("completionTokens"),
            evalMonitoringJoin.get("timeTaken")));

    cq.where(predicate);

    return entityManager.createQuery(cq).getResultList();
  }

  private Predicate buildPredicates(
      CriteriaBuilder cb,
      CriteriaQuery<?> cq,
      Root<ScoreV2> scoreRoot,
      Join<ScoreV2, EvaluationStatus> evalStatusJoin,
      Join<ScoreV2, EvaluationMonitoring> evalMonitoringJoin,
      Join<ScoreV2, ModelResponse> modelResponseJoin,
      UserEvalMonitoringParams params,
      User user) {
    Predicate predicate = cb.equal(scoreRoot.get("userId"), user.getId());

    if (params.getStartTime() != null) {
      predicate =
          cb.and(
              predicate,
              cb.greaterThanOrEqualTo(
                  scoreRoot.get("createdAt"), Timestamp.from(params.getStartTime())));
    }

    if (params.getEndTime() != null) {
      predicate =
          cb.and(
              predicate,
              cb.lessThanOrEqualTo(
                  scoreRoot.get("createdAt"), Timestamp.from(params.getEndTime())));
    }

    if (params.getScorerNames() != null && !params.getScorerNames().isEmpty()) {
      predicate = cb.and(predicate, scoreRoot.get("scorer").in(params.getScorerNames()));
    }

    if (params.getScorerIds() != null && !params.getScorerIds().isEmpty()) {
      predicate =
          cb.and(predicate, scoreRoot.get("llmEvaluator").get("id").in(params.getScorerIds()));
    }

    if (params.getModelIds() != null && !params.getModelIds().isEmpty()) {
      predicate = getModelFilter(cb, scoreRoot, params.getModelIds(), predicate);
    }

    if (params.getProjectId() != null && !params.getProjectId().isEmpty()) {
      predicate =
          cb.and(predicate, cb.equal(evalStatusJoin.get("containerId"), params.getProjectId()));
    }

    if (params.getTagIds() != null && !params.getTagIds().isEmpty()) {
      predicate =
          cb.and(
              predicate,
              tagFilterPredicate(cb, cq, scoreRoot, params.getTagIds(), evalMonitoringJoin));
    }

    if (params.getSxsJoinColumnName() != null
        && !params.getSxsJoinColumnName().isEmpty()
        && modelResponseJoin != null
        && params.getProjectId() != null
        && !params.getProjectId().isEmpty()) {
      predicate = cb.and(predicate, buildSxsFilterPredicate(cb, cq, modelResponseJoin, params));
    }

    return predicate;
  }

  private Predicate buildSxsFilterPredicate(
      CriteriaBuilder cb,
      CriteriaQuery<?> cq,
      Join<ScoreV2, ModelResponse> modelResponseJoin,
      UserEvalMonitoringParams params) {

    if ("chatTurnA".equals(params.getSxsJoinColumnName())
        || "chatTurnB".equals(params.getSxsJoinColumnName())) {
      return buildChatTurnFilterPredicate(
          cb, cq, modelResponseJoin, params, params.getSxsJoinColumnName());
    }

    return null;
  }

  private Predicate buildChatTurnFilterPredicate(
      CriteriaBuilder cb,
      CriteriaQuery<?> cq,
      Join<ScoreV2, ModelResponse> modelResponseJoin,
      UserEvalMonitoringParams params,
      String joinColumnName) {

    Subquery<String> sxsSubquery = cq.subquery(String.class);
    Root<SxsEvaluationPair> sxsSubRoot = sxsSubquery.from(SxsEvaluationPair.class);

    Join<SxsEvaluationPair, ChatTurn> chatTurnSubJoin = sxsSubRoot.join(joinColumnName);

    sxsSubquery
        .select(sxsSubRoot.get("id"))
        .where(
            cb.and(
                cb.equal(chatTurnSubJoin.get("modelResponse"), modelResponseJoin),
                cb.equal(sxsSubRoot.get("container").get("id"), params.getProjectId())));

    return cb.exists(sxsSubquery);
  }

  private Predicate getModelFilter(
      CriteriaBuilder cb, Root<ScoreV2> scoreRoot, List<String> modelIds, Predicate predicate) {
    Join<ScoreV2, ModelResponse> modelResponseJoin = scoreRoot.join("modelResponse");
    predicate = cb.and(predicate, modelResponseJoin.get("model").get("id").in(modelIds));
    return predicate;
  }

  private Predicate tagFilterPredicate(
      CriteriaBuilder cb,
      CriteriaQuery<?> cq,
      Root<ScoreV2> scoreRoot,
      List<String> tagIds,
      Join<ScoreV2, EvaluationMonitoring> evalMonitoringJoin) {

    Subquery<Long> tagCountSubquery = cq.subquery(Long.class);
    Root<TagLink> tagLinkRoot = tagCountSubquery.from(TagLink.class);
    tagCountSubquery.select(cb.countDistinct(tagLinkRoot.get("tag").get("id")));

    tagCountSubquery.where(
        cb.and(
            cb.equal(tagLinkRoot.get("targetId"), evalMonitoringJoin.get("id")),
            tagLinkRoot.get("tag").get("id").in(tagIds)));

    return cb.greaterThanOrEqualTo(tagCountSubquery, (long) tagIds.size());
  }
}
