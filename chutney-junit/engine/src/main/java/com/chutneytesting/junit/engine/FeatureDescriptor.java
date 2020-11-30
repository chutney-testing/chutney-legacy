package com.chutneytesting.junit.engine;

import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.ArrayList;
import java.util.List;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureDescriptor extends AbstractTestDescriptor implements Node<ChutneyEngineExecutionContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureDescriptor.class);

    private Status status = Status.NOT_EXECUTED;
    private List<Status> scenarioReportStatus = new ArrayList<>();

    protected FeatureDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
        super(uniqueId, displayName, source);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public ChutneyEngineExecutionContext execute(ChutneyEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
        status = Status.RUNNING;
        return context;
    }

    @Override
    public void after(ChutneyEngineExecutionContext context) {
        status = Status.worst(scenarioReportStatus);

        LOGGER.info("Feature {} execution", this.getDisplayName());
        LOGGER.info("status : {}", status);
    }

    protected void addScenarioStatus(Status scenarioReportStatus) {
        this.scenarioReportStatus.add(scenarioReportStatus);
    }
}
