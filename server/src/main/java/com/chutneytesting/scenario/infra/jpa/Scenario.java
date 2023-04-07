package com.chutneytesting.scenario.infra.jpa;

import static java.lang.String.valueOf;

import com.chutneytesting.campaign.infra.jpa.Campaign;
import com.chutneytesting.scenario.api.raw.mapper.GwtScenarioMapper;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.scenario.infra.raw.TagListMapper;
import com.chutneytesting.scenario.infra.raw.TestCaseData;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.security.User;
import com.chutneytesting.tools.Try;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Version;

@Entity(name = "SCENARIO")
public class Scenario {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CONTENT", columnDefinition = "TEXT")
    private String content;

    @Column(name = "TAGS")
    private String tags;

    @Column(name = "CREATION_DATE", updatable = false)
    private Long creationDate;

    @Column(name = "DATASET", columnDefinition = "TEXT")
    private String dataset;

    @Column(name = "ACTIVATED")
    private Boolean activated;

    @Column(name = "CONTENT_VERSION")
    private String contentVersion;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "UPDATE_DATE")
    private Long updateDate;

    @Column(name = "VERSION")
    @Version
    private Integer version;

    @ManyToMany(mappedBy = "scenarios")
    private Set<Campaign> campaigns;

    public Scenario() {
    }

    public Scenario(Long id, String title, String description, String content, String tags, Instant creationDate, String dataset, Boolean activated, String contentVersion, String userId, Instant updateDate, Integer version) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.tags = tags;
        this.creationDate = creationDate.toEpochMilli();
        this.dataset = dataset;
        this.activated = activated;
        this.contentVersion = contentVersion;
        this.userId = userId;
        this.updateDate = updateDate.toEpochMilli();
        this.version = version;
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

    public Set<Campaign> campaigns() {
        return campaigns;
    }

    public static Scenario fromTestCaseData(TestCaseData scenario) {
        return new Scenario(
            Long.valueOf(scenario.id),
            scenario.title,
            scenario.description,
            scenario.rawScenario,
            TagListMapper.tagsListToString(scenario.tags),
            scenario.creationDate,
            transformParametersToJson(scenario.executionParameters),
            true,
            scenario.contentVersion,
            User.isAnonymous(scenario.author) ? null : scenario.author,
            scenario.updateDate,
            scenario.version
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
                .build())
            .withScenario(new GwtScenarioMapper().deserialize(title, description, content))
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
            .build();
    }

    private static String transformParametersToJson(Map<String, String> executionParameters) {
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
