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

package com.planck.planck.domain.tags.repository;

import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.MonitoringType;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.enums.TagType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TagRepositoryCustomImpl implements TagRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public List<Tag> findWithFilters(
      User user, TagType tagType, String projectId, String modelId, MonitoringType monitoringType) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Tag> cq = cb.createQuery(Tag.class);
    Root<Tag> tag = cq.from(Tag.class);

    List<Predicate> mainPredicates = new ArrayList<>();

    mainPredicates.add(cb.equal(tag.get("user"), user));
    if (tagType != null) {
      mainPredicates.add(cb.equal(tag.get("type"), tagType));
    }

    if (projectId != null || modelId != null) {
      Subquery<String> tagLinkSubquery = cq.subquery(String.class);
      Root<TagLink> tagLink = tagLinkSubquery.from(TagLink.class);
      tagLinkSubquery.select(tagLink.get("tag").get("id"));

      Subquery<String> inferenceIds =
          createMonitoringSubquery(cq, cb, InferenceMonitoring.class, projectId, modelId);
      Subquery<String> evaluationIds =
          createMonitoringSubquery(cq, cb, EvaluationMonitoring.class, projectId, modelId);

      Predicate inferenceMatch =
          cb.and(
              cb.equal(tagLink.get("targetType"), TagLinkTargetType.INFERENCE_MONITORING.name()),
              tagLink.get("targetId").in(inferenceIds));
      Predicate evaluationMatch =
          cb.and(
              cb.equal(tagLink.get("targetType"), TagLinkTargetType.EVALUATION_MONITORING.name()),
              tagLink.get("targetId").in(evaluationIds));

      Predicate finalTagLinkPredicate;
      if (monitoringType == MonitoringType.INFERENCE) {
        finalTagLinkPredicate = inferenceMatch;
      } else if (monitoringType == MonitoringType.EVALUATION) {
        finalTagLinkPredicate = evaluationMatch;
      } else {
        // show all if type is not specified
        finalTagLinkPredicate = cb.or(inferenceMatch, evaluationMatch);
      }

      tagLinkSubquery.where(finalTagLinkPredicate);

      mainPredicates.add(tag.get("id").in(tagLinkSubquery));
    }
    // if no project and model - will work as before by type (ex. USER) and user

    cq.where(mainPredicates.toArray(new Predicate[0]));
    cq.orderBy(cb.desc(tag.get("createdAt")));

    return em.createQuery(cq).getResultList();
  }

  private <T> Subquery<String> createMonitoringSubquery(
      CriteriaQuery<?> cq,
      CriteriaBuilder cb,
      Class<T> monitoringClass,
      String projectId,
      String modelId) {

    Subquery<String> subquery = cq.subquery(String.class);
    Root<T> monitoringRoot = subquery.from(monitoringClass);
    subquery.select(monitoringRoot.get("id")); // SELECT id FROM ...

    List<Predicate> predicates = new ArrayList<>();
    if (projectId != null) {
      predicates.add(cb.equal(monitoringRoot.get("project").get("id"), projectId));
    }
    if (modelId != null) {
      predicates.add(cb.equal(monitoringRoot.get("model").get("id"), modelId));
    }

    if (!predicates.isEmpty()) {
      subquery.where(predicates.toArray(new Predicate[0]));
    }

    return subquery;
  }
}
