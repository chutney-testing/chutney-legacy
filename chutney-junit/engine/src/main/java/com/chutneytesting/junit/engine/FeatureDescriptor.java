package com.chutneytesting.junit.engine;

import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.ArrayList;
import java.util.List;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public class FeatureDescriptor extends AbstractTestDescriptor implements Node<ChutneyEngineExecutionContext> {

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
    public void after(ChutneyEngineExecutionContext context) throws Exception {
        status = Status.worst(scenarioReportStatus);
//        if (Status.FAILURE.equals(status)) {
//            throw new Exception("Feature " + getDisplayName() + " FAILURE");
//        }
    }

    protected void addScenarioStatus(Status scenarioReportStatus) {
        this.scenarioReportStatus.add(scenarioReportStatus);
    }
}
