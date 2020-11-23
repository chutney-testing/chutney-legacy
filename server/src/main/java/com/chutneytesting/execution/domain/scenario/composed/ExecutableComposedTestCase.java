package com.chutneytesting.execution.domain.scenario.composed;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ExecutableComposedTestCase implements TestCase {

    public final String id; // TODO - To delete
    public final TestCaseMetadata metadata;
    public final ExecutableComposedScenario composedScenario;
    public final Map<String, String> computedParameters; // TODO - refactor dataset - here it's for execution phase

    public ExecutableComposedTestCase(String id, TestCaseMetadata metadata, ExecutableComposedScenario composedScenario) {
        this.id = id;
        this.metadata = metadata;
        this.composedScenario = composedScenario;
        this.computedParameters = buildDataSet();
    }

    public ExecutableComposedTestCase(String id, TestCaseMetadata metadata, ExecutableComposedScenario composedScenario, Map<String, String> computedParameters) {
        this.id = id;
        this.metadata = metadata;
        this.composedScenario = composedScenario;
        this.computedParameters = computedParameters;
    }

    @Override
    public TestCaseMetadata metadata() {
        return metadata;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Map<String, String> computedParameters() {
        return computedParameters;
    }

    @Override
    public TestCase withDataSet(final Map<String, String> dataSet) {
        return new ExecutableComposedTestCase(
            id,
            metadata,
            composedScenario,
            dataSet
        );
    }

    public TestCase withDataSetId(String dataSetId) {
        return new ExecutableComposedTestCase(
            id,
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
        return Objects.equals(id, that.id) &&
            Objects.equals(metadata, that.metadata) &&
            Objects.equals(composedScenario, that.composedScenario) &&
            Objects.equals(computedParameters, that.computedParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, metadata, composedScenario, computedParameters);
    }

    // TODO - refactor dataset
    private Map<String, String> buildDataSet() {
        Map<String, String> dataSet = new HashMap<>();

        composedScenario.composedSteps
            .forEach(composableStep -> dataSet.putAll(composableStep.dataSetGlobalParameters()));

        Optional.ofNullable(composedScenario.parameters)
            .ifPresent(dataSet::putAll);

        return dataSet;
    }

    @Override
    public String toString() {
        return "ComposableTestCase{" +
            "id='" + id + '\'' +
            ", metadata=" + metadata +
            ", ExecutableComposedScenario=" + composedScenario +
            ", dataSet=" + computedParameters +
            '}';
    }
}
