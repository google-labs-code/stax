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

package com.planck.planck.domain.model;

import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelType;
import com.planck.planck.enums.MonitoringType;
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
public class ModelRepositoryCustomImpl implements ModelRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public List<Model> findWithDynamicFilters(
      User user, String projectId, MonitoringType monitoringType) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Model> cq = cb.createQuery(Model.class);
    Root<Model> model = cq.from(Model.class);

    List<Predicate> predicates = new ArrayList<>();

    predicates.add(cb.isFalse(model.get("isDeprecated")));
    predicates.add(
        cb.or(cb.equal(model.get("user"), user), cb.equal(model.get("type"), ModelType.SYSTEM)));

    if (projectId != null && !projectId.isBlank()) {
      Predicate projectFilterPredicate;

      Predicate inferenceExists =
          cb.exists(createMonitoringSubquery(cq, cb, model, InferenceMonitoring.class, projectId));
      Predicate evaluationExists =
          cb.exists(createMonitoringSubquery(cq, cb, model, EvaluationMonitoring.class, projectId));

      if (monitoringType == MonitoringType.INFERENCE) {
        projectFilterPredicate = inferenceExists;
      } else if (monitoringType == MonitoringType.EVALUATION) {
        projectFilterPredicate = evaluationExists;
      } else {
        projectFilterPredicate = cb.or(inferenceExists, evaluationExists);
      }
      predicates.add(projectFilterPredicate);
    }

    cq.where(predicates.toArray(new Predicate[0]));
    cq.orderBy(cb.desc(model.get("releaseDate")), cb.asc(model.get("label")));

    return em.createQuery(cq).getResultList();
  }

  private <T> Subquery<Integer> createMonitoringSubquery(
      CriteriaQuery<?> cq,
      CriteriaBuilder cb,
      Root<Model> model,
      Class<T> monitoringClass,
      String projectId) {

    Subquery<Integer> subquery = cq.subquery(Integer.class);
    Root<T> monitoringRoot = subquery.from(monitoringClass);
    subquery.select(cb.literal(1)); // SELECT 1

    Predicate correlationPredicate = cb.equal(monitoringRoot.get("model"), model);
    Predicate projectPredicate = cb.equal(monitoringRoot.get("project").get("id"), projectId);

    subquery.where(correlationPredicate, projectPredicate);
    return subquery;
  }
}
