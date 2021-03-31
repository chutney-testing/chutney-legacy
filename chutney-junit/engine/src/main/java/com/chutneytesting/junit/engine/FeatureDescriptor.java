package com.chutneytesting.junit.engine;

import com.chutneytesting.engine.api.execution.StatusDto;
import com.google.common.collect.Ordering;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FeatureDescriptor extends AbstractTestDescriptor implements Node<ChutneyEngineExecutionContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureDescriptor.class);

    private final List<StatusDto> scenarioReportStatus = new ArrayList<>();
    private StatusDto status = StatusDto.NOT_EXECUTED;

    protected FeatureDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
        super(uniqueId, displayName, source);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public ChutneyEngineExecutionContext execute(ChutneyEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        status = StatusDto.RUNNING;
        return context;
    }

    @Override
    public void after(ChutneyEngineExecutionContext context) {
        status = worst(scenarioReportStatus);

        LOGGER.info("Feature {} execution", this.getDisplayName());
        LOGGER.info("status : {}", status);
    }

    protected void addScenarioStatus(StatusDto scenarioReportStatus) {
        this.scenarioReportStatus.add(scenarioReportStatus);
    }

    private static final Ordering<StatusDto> EXECUTION_STATUS_STATUS_ORDERING = Ordering.explicit(StatusDto.EXECUTED, StatusDto.PAUSED, StatusDto.RUNNING, StatusDto.STOPPED, StatusDto.FAILURE, StatusDto.WARN, StatusDto.NOT_EXECUTED, StatusDto.SUCCESS);

    private static StatusDto worst(List<StatusDto> severalStatus) {

        StatusDto reducedStatus = severalStatus.stream()
            .filter(Objects::nonNull)
            .reduce(StatusDto.SUCCESS, EXECUTION_STATUS_STATUS_ORDERING::min);

        if (reducedStatus.equals(StatusDto.NOT_EXECUTED)) {
            List<StatusDto> notExecutedStatus = severalStatus.stream().filter(s -> !s.equals(StatusDto.NOT_EXECUTED)).collect(Collectors.toList());
            if (!notExecutedStatus.isEmpty()) {
                return StatusDto.RUNNING;
            }
        }
        return reducedStatus;
    }
}
