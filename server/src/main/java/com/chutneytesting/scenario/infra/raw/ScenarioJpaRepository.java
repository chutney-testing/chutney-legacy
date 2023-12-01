/*
 * Copyright 2017-2023 Enedis
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

package com.chutneytesting.scenario.infra.raw;

import com.chutneytesting.scenario.infra.jpa.ScenarioEntity;
import jakarta.persistence.criteria.Expression;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ScenarioJpaRepository extends CrudRepository<ScenarioEntity, Long>, JpaSpecificationExecutor<ScenarioEntity> {

    @Query("SELECT s.version FROM SCENARIO s WHERE s.id = :id")
    Optional<Integer> lastVersion(@Param("id") Long id);

    Optional<ScenarioEntity> findByIdAndActivated(Long id, Boolean activated);

    @Query("""
        SELECT new com.chutneytesting.scenario.infra.jpa.ScenarioEntity(s.id, s.title, s.description, s.tags, s.creationDate, s.dataset, s.activated, s.userId, s.updateDate, s.version, s.defaultDataset)
        FROM SCENARIO s
        WHERE s.id = :id
          AND s.activated = :activated
        """)
    Optional<ScenarioEntity> findMetaDataByIdAndActivated(@Param("id") Long id, @Param("activated") Boolean activated);

    @Query("""
        SELECT new com.chutneytesting.scenario.infra.jpa.ScenarioEntity(s.id, s.title, s.description, s.tags, s.creationDate, s.dataset, s.activated, s.userId, s.updateDate, s.version, s.defaultDataset)
        FROM SCENARIO s
        WHERE s.activated = true
        """)
    List<ScenarioEntity> findMetaDataByActivatedTrue();

    static Specification<ScenarioEntity> contentContains(String searchWord) {
        return (root, query, builder) -> {
            Expression<String> content = builder.lower(root.get("content"));
            return builder.like(content, "%" + searchWord.toLowerCase() + "%");
        };
    }

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO SCENARIO (ID, TITLE, DESCRIPTION, CONTENT, TAGS, CREATION_DATE, DATASET, ACTIVATED, USER_ID, UPDATE_DATE, VERSION, DEFAULT_DATASET_ID) VALUES (:id, :title, :description, :content, :tags, :creationDate, :dataset, :activated, :userId, :updateDate, :version, :defaultDataset)")
    void saveWithExplicitId(
        @Param("id") Long id,
        @Param("title") String title,
        @Param("description") String description,
        @Param("content") String content,
        @Param("tags") String tags,
        @Param("creationDate") Long creationDate,
        @Param("dataset") String dataset,
        @Param("activated") Boolean activated,
        @Param("userId") String userId,
        @Param("updateDate") Long updateDate,
        @Param("version") Integer version,
        @Param("defaultDataset") String defaultDataset);
}
