package com.chutneytesting.junit.engine;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.junit.engine.jackson.ChutneyModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Predicate;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScenarioDescriptor extends AbstractTestDescriptor implements Node<ChutneyEngineExecutionContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioDescriptor.class);
    private static final ObjectMapper om = new ObjectMapper()
        .findAndRegisterModules()
        .registerModule(new ChutneyModule())
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .configure(FAIL_ON_EMPTY_BEANS, false);
    private static final Predicate<StepExecutionReportDto> isStepFailed = dto -> !StatusDto.SUCCESS.equals(dto.status);

    private final StepDefinitionDto stepDefinition;
    private StepExecutionReportDto report;

    protected ScenarioDescriptor(UniqueId uniqueId, String displayName, TestSource source, StepDefinitionDto stepDefinition) {
        super(uniqueId, displayName, source);
        this.stepDefinition = stepDefinition;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public ChutneyEngineExecutionContext execute(ChutneyEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        report = context.executeScenario(stepDefinition).blockingLast();
        return context;
    }

    @Override
    public void after(ChutneyEngineExecutionContext context) throws Exception {
        FeatureDescriptor featureDescriptor = (FeatureDescriptor) this.getParent().get();
        featureDescriptor.addScenarioStatus(report.status);

        LOGGER.info("Scenario {} execution", this.getDisplayName());
        LOGGER.info("status : {}", report.status);
        LOGGER.info(om.writerWithDefaultPrettyPrinter().writeValueAsString(report));

        if (isStepFailed.test(report)) {
            StepExecutionReportDto failedStepReport = findFailedStep(report);
            throw new Exception(failedStepReport.name + ": " + failedStepReport.errors.stream().reduce((s, s2) -> String.join("\n", s, s2))
                .orElse("Scenario " + stepDefinition.name + " FAILURE")
            );
        }
    }

    private StepExecutionReportDto findFailedStep(StepExecutionReportDto rootReport) {
        if (rootReport.steps.isEmpty()) {
            return rootReport;
        }

        StepExecutionReportDto failedChild = rootReport.steps.stream()
            .filter(isStepFailed)
            .findFirst().get();
        return findFailedStep(failedChild);
    }
}
