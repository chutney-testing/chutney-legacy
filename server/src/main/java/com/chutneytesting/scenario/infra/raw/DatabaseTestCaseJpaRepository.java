package com.chutneytesting.scenario.infra.raw;

import com.chutneytesting.scenario.infra.jpa.Scenario;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DatabaseTestCaseJpaRepository extends CrudRepository<Scenario, Long>, JpaSpecificationExecutor<Scenario> {

    @Query("SELECT s FROM SCENARIO s WHERE s.id = :id and s.activated = true")
    Optional<Scenario> findById(@Param("id") Long id);

    @Query("SELECT s FROM SCENARIO s WHERE s.activated = true")
    List<Scenario> findAll();

    @Modifying
    @Query("UPDATE FROM SCENARIO s SET s.activated = false WHERE s.id = :id")
    void deactivateScenario(@Param("id") Long id);

    @Query("SELECT s.version FROM  SCENARIO s WHERE s.id = :id")
    Optional<Integer> getLastVersion(@Param("id") Long id);


    static Specification<Scenario> contentContains(String searchWord) {
        return (root, query, builder) -> {
            Expression<String> content = builder.lower(root.get("content"));
            return builder.like(content, "%" + searchWord.toLowerCase() + "%");
        };
    }

}
