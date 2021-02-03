package com.chutneytesting.design.domain.scenario.compose;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ComposableTestCase implements TestCase {

    public final String id;
    public final TestCaseMetadata metadata;
    public final ComposableScenario composableScenario;
    public final Map<String, String> executionParameters;

    public ComposableTestCase(String id, TestCaseMetadata metadata, ComposableScenario composableScenario) {
        this.id = id;
        this.metadata = metadata;
        this.composableScenario = composableScenario;
        this.executionParameters = getExecutionParameters();
    }

    private ComposableTestCase(String id, TestCaseMetadata metadata, ComposableScenario composableScenario, Map<String, String> executionParameters) {
        this.id = id;
        this.metadata = metadata;
        this.composableScenario = composableScenario;
        this.executionParameters = executionParameters;
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
    public Map<String, String> executionParameters() {
        return executionParameters;
    }

    @Override
    public TestCase usingExecutionParameters(final Map<String, String> parameters) {
        return new ComposableTestCase(
            id,
            metadata,
            composableScenario,
            parameters
        );
    }

    public TestCase withDataSetId(String dataSetId) {
        return new ComposableTestCase(
            id,
            TestCaseMetadataImpl.TestCaseMetadataBuilder.from(metadata)
                .withDatasetId(dataSetId)
                .build(),
            composableScenario,
            executionParameters
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComposableTestCase that = (ComposableTestCase) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(metadata, that.metadata) &&
            Objects.equals(composableScenario, that.composableScenario) &&
            Objects.equals(executionParameters, that.executionParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, metadata, composableScenario, executionParameters);
    }

    private Map<String, String> getExecutionParameters() { // TODO - is it still needed here for edition ?
        Map<String, String> parameters = new HashMap<>();

        // Bubble up empty params from steps
        composableScenario.composableSteps.forEach(composableStep -> parameters.putAll(composableStep.getEmptyExecutionParameters()));

        // Take all params from scenario
        Optional.ofNullable(composableScenario.parameters).ifPresent(parameters::putAll);

        return parameters;
    }

    @Override
    public String toString() {
        return "ComposableTestCase{" +
            "id='" + id + '\'' +
            ", metadata=" + metadata +
            ", composableScenario=" + composableScenario +
            ", parameters=" + executionParameters +
            '}';
    }
}
