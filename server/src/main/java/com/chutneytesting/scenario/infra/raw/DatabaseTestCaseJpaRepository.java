package com.chutneytesting.scenario.infra.raw;

import com.chutneytesting.scenario.infra.jpa.Scenario;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DatabaseTestCaseJpaRepository extends CrudRepository<Scenario, Long>, JpaSpecificationExecutor<Scenario> {

    @Query("SELECT s.version FROM SCENARIO s WHERE s.id = :id")
    Optional<Integer> getLastVersion(@Param("id") Long id);

    Optional<Scenario> findByIdAndActivated(Long id, Boolean activated);

    @Query("""
        SELECT new com.chutneytesting.scenario.infra.jpa.Scenario(s.id, s.title, s.description, s.tags, s.creationDate, s.dataset, s.activated, s.userId, s.updateDate, s.version)
        FROM SCENARIO s
        WHERE s.id = :id
          AND s.activated = :activated
        """)
    Optional<Scenario> findMetaDataByIdAndActivated(@Param("id") Long id, @Param("activated") Boolean activated);

    @Query("""
        SELECT new com.chutneytesting.scenario.infra.jpa.Scenario(s.id, s.title, s.description, s.tags, s.creationDate, s.dataset, s.activated, s.userId, s.updateDate, s.version)
        FROM SCENARIO s
        WHERE s.activated = true
        """)
    List<Scenario> findMetaDataByActivatedTrue();

    static Specification<Scenario> contentContains(String searchWord) {
        return (root, query, builder) -> {
            Expression<String> content = builder.lower(root.get("content"));
            return builder.like(content, "%" + searchWord.toLowerCase() + "%");
        };
    }
}
