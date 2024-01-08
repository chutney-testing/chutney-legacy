/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.Instant;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "SCENARIO")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ScenarioEntity {

    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

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

    public ScenarioEntity() {
    }

    public ScenarioEntity(Long id, String title, String description, String tags, Long creationDate, Boolean activated, String userId, Long updateDate, Integer version, String defaultDataset) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.creationDate = creationDate;
        this.activated = activated;
        this.userId = userId;
        this.updateDate = updateDate;
        this.version = version;
        this.defaultDataset = defaultDataset;
    }

    public ScenarioEntity(Long id, String title, String description, String content, String tags, Instant creationDate, Boolean activated, String userId, Instant updateDate, Integer version, String defaultDataset) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.tags = tags;
        this.creationDate = creationDate.toEpochMilli();
        this.activated = activated;
        this.userId = userId;
        this.updateDate = updateDate.toEpochMilli();
        this.version = version;
        this.defaultDataset = defaultDataset;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isActivated() {
        return activated;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public String getTags() {
        return tags;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public String getUserId() {
        return userId;
    }

    public Long getUpdateDate() {
        return updateDate;
    }

    public Integer getVersion() {
        return version;
    }

    public String getDefaultDataset() {
        return defaultDataset;
    }

    public void deactivate() {
        activated = false;
    }

    public static ScenarioEntity fromGwtTestCase(GwtTestCase testCase) {
        return new ScenarioEntity(
            Long.valueOf(testCase.id()),
            testCase.metadata().title(),
            testCase.metadata().description(),
            ofNullable(testCase.scenario).map(marshaller::serialize).orElse(null),
            TagListMapper.tagsListToString(testCase.metadata().tags()),
            testCase.metadata().creationDate(),
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
}
