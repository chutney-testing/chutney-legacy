package com.chutneytesting.scenario.infra.jpa;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;

import com.chutneytesting.execution.domain.GwtScenarioMarshaller;
import com.chutneytesting.scenario.api.raw.mapper.GwtScenarioMapper;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.infra.raw.TagListMapper;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.security.User;
import com.chutneytesting.tools.Try;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity(name = "SCENARIO")
public class Scenario {

    public static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CONTENT")
    @Basic(fetch = FetchType.LAZY)
    private String content;

    @Column(name = "TAGS")
    private String tags;

    @Column(name = "CREATION_DATE", updatable = false)
    private Long creationDate;

    @Column(name = "DATASET")
    private String dataset;

    @Column(name = "ACTIVATED")
    private Boolean activated;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "UPDATE_DATE")
    private Long updateDate;

    @Column(name = "VERSION")
    @Version
    private Integer version;

    @Column(name = "DEFAULT_DATASET_ID")
    private String defaultDataset;

    public Scenario() {
    }

    public Scenario(Long id, String title, String description, String tags, Long creationDate, String dataset, Boolean activated, String userId, Long updateDate, Integer version, String defaultDataset) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.creationDate = creationDate;
        this.dataset = dataset;
        this.activated = activated;
        this.userId = userId;
        this.updateDate = updateDate;
        this.version = version;
        this.defaultDataset = defaultDataset;
    }

    public Scenario(Long id, String title, String description, String content, String tags, Instant creationDate, String dataset, Boolean activated, String userId, Instant updateDate, Integer version, String defaultDataset) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.tags = tags;
        this.creationDate = creationDate.toEpochMilli();
        this.dataset = dataset;
        this.activated = activated;
        this.userId = userId;
        this.updateDate = updateDate.toEpochMilli();
        this.version = version;
        this.defaultDataset = defaultDataset;
    }

    public Long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public boolean activated() {
        return activated;
    }

    public void deactivate() {
        activated = false;
    }

    public static Scenario fromGwtTestCase(GwtTestCase testCase) {
        return new Scenario(
            Long.valueOf(testCase.id()),
            testCase.metadata().title(),
            testCase.metadata().description(),
            ofNullable(testCase.scenario).map(marshaller::serialize).orElse(null),
            TagListMapper.tagsListToString(testCase.metadata().tags()),
            testCase.metadata().creationDate(),
            transformParametersToJson(testCase.executionParameters()),
            true,
            User.isAnonymous(testCase.metadata().author()) ? null : testCase.metadata().author(),
            testCase.metadata().updateDate(),
            testCase.metadata().version(),
            testCase.metadata().defaultDataset()
        );
    }

    public GwtTestCase toGwtTestCase() {
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(valueOf(id))
                .withTitle(title)
                .withDescription(description)
                .withCreationDate(Instant.ofEpochMilli(creationDate))
                .withTags(TagListMapper.tagsStringToList(tags))
                .withAuthor(userId)
                .withUpdateDate(Instant.ofEpochMilli(updateDate))
                .withVersion(version)
                .withDefaultDataset(defaultDataset)
                .build())
            .withScenario(ofNullable(content).map(c -> new GwtScenarioMapper().deserialize(title, description, c)).orElse(null))
            .withExecutionParameters(transformParametersMap(dataset))
            .build();
    }

    public TestCaseMetadata toTestCaseMetadata() {
        return TestCaseMetadataImpl.builder()
            .withId(valueOf(id))
            .withTitle(title)
            .withDescription(description)
            .withTags(TagListMapper.tagsStringToList(tags))
            .withCreationDate(Instant.ofEpochMilli(creationDate))
            .withAuthor(userId)
            .withUpdateDate(Instant.ofEpochMilli(updateDate))
            .withVersion(version)
            .withDefaultDataset(defaultDataset)
            .build();
    }

    public static String transformParametersToJson(Map<String, String> executionParameters) {
        if (executionParameters != null && !executionParameters.isEmpty()) {
            return Try.exec(() -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.writeValueAsString(executionParameters);
                }
            ).runtime();
        }
        return null;
    }

    private static Map<String, String> transformParametersMap(String parameters) {
        return Try.exec(() -> {
                ObjectMapper objectMapper = new ObjectMapper();
                TypeReference<Map<String, String>> typeRef = new TypeReference<>() {
                };
                return objectMapper.readValue(parameters != null ? parameters : "{}", typeRef);
            }
        ).runtime();
    }
}
