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
    public final Map<String, String> computedParameters;

    public ExecutableComposedTestCase(TestCaseMetadata metadata, ExecutableComposedScenario composedScenario) {
        this.metadata = metadata;
        this.composedScenario = composedScenario;
        this.computedParameters = buildDataSet();
    }

    public ExecutableComposedTestCase(TestCaseMetadata metadata, ExecutableComposedScenario composedScenario, Map<String, String> computedParameters) {
        this.metadata = metadata;
        this.composedScenario = composedScenario;
        this.computedParameters = computedParameters;
    }

    @Override
    public TestCaseMetadata metadata() {
        return metadata;
    }

    @Override
    public Map<String, String> parameters() {
        return computedParameters;
    }

    @Override
    public TestCase withParameters(final Map<String, String> parameters) {
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
            computedParameters
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutableComposedTestCase that = (ExecutableComposedTestCase) o;
        return Objects.equals(metadata, that.metadata) &&
            Objects.equals(composedScenario, that.composedScenario) &&
            Objects.equals(computedParameters, that.computedParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, composedScenario, computedParameters);
    }

    private Map<String, String> buildDataSet() {
        Map<String, String> dataSet = new HashMap<>();

        composedScenario.composedSteps
            .forEach(composedStep -> dataSet.putAll(composedStep.dataSetGlobalParameters()));

        Optional.ofNullable(composedScenario.parameters)
            .ifPresent(dataSet::putAll);

        return dataSet;
    }

    @Override
    public String toString() {
        return "ExecutableComposedTestCase{" +
            ", metadata=" + metadata +
            ", ExecutableComposedScenario=" + composedScenario +
            ", dataSet=" + computedParameters +
            '}';
    }
}
