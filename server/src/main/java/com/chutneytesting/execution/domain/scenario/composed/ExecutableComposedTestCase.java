package com.chutneytesting.execution.domain.scenario.composed;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ExecutableComposedTestCase implements TestCase {

    public final TestCaseMetadata metadata;
    public final ExecutableComposedScenario composedScenario;
    public final Map<String, String> executionParameters;

    public ExecutableComposedTestCase(TestCaseMetadata metadata, ExecutableComposedScenario composedScenario) {
        this.metadata = metadata;
        this.composedScenario = composedScenario;
        this.executionParameters = buildDataSet();
    }

    public ExecutableComposedTestCase(TestCaseMetadata metadata, ExecutableComposedScenario composedScenario, Map<String, String> executionParameters) {
        this.metadata = metadata;
        this.composedScenario = composedScenario;
        this.executionParameters = executionParameters;
    }

    @Override
    public TestCaseMetadata metadata() {
        return metadata;
    }

    @Override
    public Map<String, String> executionParameters() {
        return executionParameters;
    }

    @Override
    public TestCase usingExecutionParameters(final Map<String, String> parameters) {
        return new ExecutableComposedTestCase(
            metadata,
            composedScenario,
            parameters
        );
    }

    public TestCase withDataSetId(String dataSetId) {
        return new ExecutableComposedTestCase(
            TestCaseMetadataImpl.TestCaseMetadataBuilder.from(metadata)
                .withDatasetId(dataSetId)
                .build(),
            composedScenario,
            executionParameters
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutableComposedTestCase that = (ExecutableComposedTestCase) o;
        return Objects.equals(metadata, that.metadata) &&
            Objects.equals(composedScenario, that.composedScenario) &&
            Objects.equals(executionParameters, that.executionParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, composedScenario, executionParameters);
    }

    private Map<String, String> buildDataSet() {
        Map<String, String> dataSet = new HashMap<>();

        composedScenario.composedSteps
            .forEach(composedStep -> dataSet.putAll(composedStep.getEmptyExecutionParameters()));

        Optional.ofNullable(composedScenario.parameters)
            .ifPresent(dataSet::putAll);

        return dataSet;
    }

    @Override
    public String toString() {
        return "ExecutableComposedTestCase{" +
            ", metadata=" + metadata +
            ", ExecutableComposedScenario=" + composedScenario +
            ", dataSet=" + executionParameters +
            '}';
    }
}
