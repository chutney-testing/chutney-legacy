package com.chutneytesting.design.infra.storage.scenario.compose.dto;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_AUTHOR;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DATASET_ID;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TITLE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_UPDATEDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.load;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.setOnlyOnceProperty;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.setOrRemoveProperty;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.security.domain.User;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestCaseVertex {

    final OVertex testCaseVertex;
    final StepVertex rootStep; // for convenience, vertex is sometime treated like a step

    private TestCaseVertex(OVertex testCaseVertex) {
        this.testCaseVertex = testCaseVertex;
        this.rootStep = StepVertex.builder().from(testCaseVertex).build();
    }

    private void setSubStepReferences(List<ComposableStep> steps, ODatabaseSession session) {
        rootStep.setSubStepReferences(steps, session);
    }

    public static TestCaseVertexBuilder builder() {
        return new TestCaseVertexBuilder();
    }

    public OVertex save() {
        return testCaseVertex.save();
    }

    public String id() {
        return testCaseVertex.getIdentity().toString();
    }

    public String title() {
        return testCaseVertex.getProperty(TESTCASE_CLASS_PROPERTY_TITLE);
    }

    public String description() {
        return testCaseVertex.getProperty(TESTCASE_CLASS_PROPERTY_DESCRIPTION);
    }

    public Instant creationDate() {
        return ((Date) testCaseVertex.getProperty(TESTCASE_CLASS_PROPERTY_CREATIONDATE)).toInstant();
    }

    public List<String> tags() {
        return testCaseVertex.getProperty(TESTCASE_CLASS_PROPERTY_TAGS);
    }

    public String datasetId() {
        return testCaseVertex.getProperty(TESTCASE_CLASS_PROPERTY_DATASET_ID);
    }

    public Instant updateDate() {
        return ((Date) testCaseVertex.getProperty(TESTCASE_CLASS_PROPERTY_UPDATEDATE)).toInstant();
    }

    public String author() {
        return testCaseVertex.getProperty(TESTCASE_CLASS_PROPERTY_AUTHOR);
    }

    public Integer version() {
        return testCaseVertex.getVersion();
    }

    public StepVertex asRootStep() {
        return rootStep;
    }

    public Map<String, String> parameters() {
        return testCaseVertex.getProperty(TESTCASE_CLASS_PROPERTY_PARAMETERS);
    }


    public static class TestCaseVertexBuilder {

        String id;
        ODatabaseSession dbSession;

        OVertex vertex;

        private String title;
        private String description;
        private Date creationDate;
        private List<String> tags;
        private Map<String, String> parameters;
        private Optional<String> datasetId = empty();
        private Date updateDate;
        private String author;
        private List<ComposableStep> steps;

        private TestCaseVertexBuilder() {}

        public TestCaseVertex build() {
            if (this.vertex == null) {
                this.vertex = (OVertex) load(id, dbSession).orElseThrow(() -> new ScenarioNotFoundException(id));
            }

            ofNullable(title).ifPresent(t -> vertex.setProperty(TESTCASE_CLASS_PROPERTY_TITLE, t, OType.STRING) );
            ofNullable(description).ifPresent(d -> setOrRemoveProperty(vertex, TESTCASE_CLASS_PROPERTY_DESCRIPTION, d, OType.STRING) );
            ofNullable(creationDate).ifPresent(cd -> setOnlyOnceProperty(vertex, TESTCASE_CLASS_PROPERTY_CREATIONDATE, cd, OType.DATETIME) );
            ofNullable(tags).ifPresent(t -> vertex.setProperty(TESTCASE_CLASS_PROPERTY_TAGS, t, OType.EMBEDDEDLIST) );
            ofNullable(parameters).ifPresent(p -> setOrRemoveProperty(vertex, TESTCASE_CLASS_PROPERTY_PARAMETERS, p, OType.EMBEDDEDMAP) );
            setOrRemoveProperty(vertex, TESTCASE_CLASS_PROPERTY_DATASET_ID, datasetId.orElse(null), OType.STRING);
            ofNullable(updateDate).ifPresent(ud -> vertex.setProperty(TESTCASE_CLASS_PROPERTY_UPDATEDATE, ud, OType.DATETIME));
            ofNullable(author).ifPresent(author -> setOrRemoveProperty(vertex, TESTCASE_CLASS_PROPERTY_AUTHOR, author, a -> !User.isAnonymous(a), OType.STRING) );

            TestCaseVertex testCaseVertex = new TestCaseVertex(vertex);
            ofNullable(steps).ifPresent(s -> testCaseVertex.setSubStepReferences(s, dbSession));

            return testCaseVertex;
        }

        public TestCaseVertexBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public TestCaseVertexBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public TestCaseVertexBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TestCaseVertexBuilder withCreationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public TestCaseVertexBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public TestCaseVertexBuilder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public TestCaseVertexBuilder withDatasetId(Optional<String> datasetId) {
            this.datasetId = datasetId;
            return this;
        }

        public TestCaseVertexBuilder withUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
            return this;
        }

        public TestCaseVertexBuilder withAuthor(String author) {
            this.author = author;
            return this;
        }

        public TestCaseVertexBuilder from(OVertex vertex) {
            this.vertex = vertex;
            return this;
        }

        public TestCaseVertexBuilder usingSession(ODatabaseSession dbSession) {
            this.dbSession = dbSession;
            return this;
        }

        public TestCaseVertexBuilder withSteps(List<ComposableStep> composableSteps) {
            this.steps = composableSteps;
            return this;
        }
    }

}
