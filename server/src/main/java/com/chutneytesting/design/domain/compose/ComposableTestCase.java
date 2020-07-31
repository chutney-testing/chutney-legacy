package com.chutneytesting.design.domain.compose;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ComposableTestCase implements TestCase {

    public final String id; // TODO - To delete
    public final TestCaseMetadata metadata;
    public final ComposableScenario composableScenario;
    public final Map<String, String> computedParameters; // TODO - refactor dataset - here it's for execution phase

    public ComposableTestCase(String id, TestCaseMetadata metadata, ComposableScenario composableScenario) {
        this.id = id;
        this.metadata = metadata;
        this.composableScenario = composableScenario;
        this.computedParameters = buildDataSet();
    }

    public ComposableTestCase(String id, TestCaseMetadata metadata, ComposableScenario composableScenario, Map<String, String> computedParameters) {
        this.id = id;
        this.metadata = metadata;
        this.composableScenario = composableScenario;
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
        return new ComposableTestCase(
            id,
            metadata,
            composableScenario,
            dataSet
        );
    }

    public TestCase withDataSetId(String dataSetId) {
        return new ComposableTestCase(
            id,
            TestCaseMetadataImpl.TestCaseMetadataBuilder.from(metadata)
                .withDatasetId(dataSetId)
                .build(),
            composableScenario,
            computedParameters
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
            Objects.equals(computedParameters, that.computedParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, metadata, composableScenario, computedParameters);
    }

    // TODO - refactor dataset
    private Map<String, String> buildDataSet() {
        Map<String, String> dataSet = new HashMap<>();

        composableScenario.functionalSteps
            .forEach(functionalStep -> dataSet.putAll(functionalStep.dataSetGlobalParameters()));

        Optional.ofNullable(composableScenario.parameters)
            .ifPresent(dataSet::putAll);

        return dataSet;
    }

    @Override
    public String toString() {
        return "ComposableTestCase{" +
            "id='" + id + '\'' +
            ", metadata=" + metadata +
            ", composableScenario=" + composableScenario +
            ", dataSet=" + computedParameters +
            '}';
    }
}
