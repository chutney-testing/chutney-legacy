package com.chutneytesting.execution.infra.storage;

import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionReportEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ScenarioExecutionReportJpaRepository extends CrudRepository<ScenarioExecutionReportEntity, Long>, JpaSpecificationExecutor<ScenarioExecutionReportEntity> {
}
