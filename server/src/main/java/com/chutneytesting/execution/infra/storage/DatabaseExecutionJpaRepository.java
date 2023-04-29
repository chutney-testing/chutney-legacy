package com.chutneytesting.execution.infra.storage;

import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecution;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import java.util.List;
import javax.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DatabaseExecutionJpaRepository extends CrudRepository<ScenarioExecution, Long>, JpaSpecificationExecutor<ScenarioExecution> {

    List<ScenarioExecution> findByStatus(ServerReportStatus status);

    List<ScenarioExecution> findFirst20ByScenarioIdOrderByIdDesc(Long scenarioId);

    @Query("select max(se.id), se.scenarioId from SCENARIO_EXECUTIONS se where se.scenarioId in :scenarioIds group by se.scenarioId")
    List<Tuple> findLastExecutionsByScenarioId(@Param("scenarioIds") List<Long> scenarioIds);

    List<ScenarioExecution> findAllByScenarioId(Long scenarioId);
}
