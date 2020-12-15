package com.chutneytesting.design.infra.storage.scenario.compose.dto;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_AUTHOR;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DATASET_ID;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_PARAMETERS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TAGS;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_TITLE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_UPDATEDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.setOnlyOnceProperty;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils.setOrRemoveProperty;
import static java.time.Instant.now;

import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.security.domain.User;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
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

    public static class TestCaseVertexBuilder {

        private OVertex vertex;
        private ODatabaseSession session;

        private String title;
        private String description;
        private Date creationDate;
        private List<String> tags;
        private Map<String, String> parameters;
        private Optional<String> datasetId;
        private Date updateDate;
        private String author;
        private List<ComposableStep> steps;

        private TestCaseVertexBuilder() {}

        public TestCaseVertex build() {

            vertex.setProperty(TESTCASE_CLASS_PROPERTY_TITLE, title, OType.STRING);
            setOrRemoveProperty(vertex, TESTCASE_CLASS_PROPERTY_DESCRIPTION, description, OType.STRING);
            setOnlyOnceProperty(vertex, TESTCASE_CLASS_PROPERTY_CREATIONDATE, creationDate, OType.DATETIME);
            vertex.setProperty(TESTCASE_CLASS_PROPERTY_TAGS, tags, OType.EMBEDDEDLIST);
            setOrRemoveProperty(vertex, TESTCASE_CLASS_PROPERTY_PARAMETERS, parameters, OType.EMBEDDEDMAP);
            setOrRemoveProperty(vertex, TESTCASE_CLASS_PROPERTY_DATASET_ID, datasetId.orElse(null), OType.STRING);
            vertex.setProperty(TESTCASE_CLASS_PROPERTY_UPDATEDATE, Date.from(now()), OType.DATETIME);
            setOrRemoveProperty(vertex, TESTCASE_CLASS_PROPERTY_AUTHOR, author, a -> !User.isAnonymous(a), OType.STRING);

            TestCaseVertex testCaseVertex = new TestCaseVertex(vertex);
            testCaseVertex.setSubStepReferences(steps, session);
            return testCaseVertex;
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
            this.session = dbSession;
            return this;
        }

        public TestCaseVertexBuilder withSteps(List<ComposableStep> composableSteps) {
            this.steps = composableSteps;
            return this;
        }
    }

}
