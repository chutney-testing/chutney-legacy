package com.chutneytesting.documentation.infra;

import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.infra.storage.scenario.DelegateScenarioRepository;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import com.chutneytesting.tools.Streams;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExamplesRepository implements DelegateScenarioRepository {

    private static Instant START_TIME = Instant.MIN;

    private Map<String, String> examples; // fileName, Content
    private boolean isActive;
    private final String ORIGIN = "examples";

    public ExamplesRepository(@Value("${chutney.examples.active:false}") boolean isActive,
                              @Qualifier("embeddedExamples") Map<String, String> examples) {
        this.isActive = isActive;
        this.examples = examples;
    }

    @Override
    public String alias() {
        return ORIGIN;
    }

    @Override
    public String save(TestCaseData scenario) {
        return "0";
    }

    @Override
    public Optional<TestCaseData> findById(String scenarioId) {
        return examples.entrySet().stream()
            .filter(entry -> resolveExampleID(entry).equals(scenarioId))
            .map(this::mapToTestCase)
            .collect(Streams.collectUniqueResult());
    }

    private String resolveExampleID(Map.Entry<String, String> entry) {
        return String.valueOf(entry.getKey().hashCode()).replaceFirst("-", "_");
    }

    @Override
    public List<TestCaseMetadata> findAll() {
        if (!isActive) {
            return Collections.emptyList();
        }

        return examples.entrySet().stream()
            .map(this::mapToMetadata)
            .collect(Collectors.toList());
    }

    @Override
    public void removeById(String scenarioId) { /* nothing to do */ }

    @Override
    public Optional<Integer> lastVersion(String scenarioId) {
        Optional<TestCaseData> testCaseData = findById(scenarioId);
        return testCaseData.map(tcd -> tcd.version);
    }

    // TODO - remove duplication & do it only once on startup in DocumentationConfiguration
    private TestCaseData mapToTestCase(Map.Entry<String, String> entry) {
        return  TestCaseData.builder()
            .withContentVersion("RAW")
            .withId(resolveExampleID(entry))
            .withTitle(entry.getKey())
            .withCreationDate(START_TIME)
            .withDescription("Embedded example for documentation purpose. Cannot be edited.")
            .withTags(Collections.singletonList("documentation"))
            .withExecutionParameters(Collections.emptyMap())
            .withRawScenario(entry.getValue())
            .withAuthor("system")
            .build();
    }

    private TestCaseMetadata mapToMetadata(Map.Entry<String, String> entry) {
        return TestCaseMetadataImpl.builder()
            .withId(resolveExampleID(entry))
            .withTitle(entry.getKey())
            .withDescription("Chutney Example")
            .withCreationDate(START_TIME)
            .withTags(Collections.singletonList("documentation"))
            .withRepositorySource(ORIGIN)
            .withAuthor("system")
            .build();
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean toggleActivation() {
        isActive = !isActive;
        return isActive;
    }
}
