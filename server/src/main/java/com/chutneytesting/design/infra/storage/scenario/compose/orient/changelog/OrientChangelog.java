package com.chutneytesting.design.infra.storage.scenario.compose.orient.changelog;

import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB.TESTCASE_CLASS_PROPERTY_UPDATEDATE;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientComponentDB;
import com.chutneytesting.design.infra.storage.scenario.compose.orient.OrientUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndexManager;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class OrientChangelog {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrientChangelog.class);

    @ChangelogOrder(order = 2, uuid = "20190424-create-newmodel")
    public static void createModel(ODatabaseSession dbSession) {
        OClass funcStepVClass = OrientUtils.createClass(OrientComponentDB.STEP_CLASS, "V", 1, dbSession);
        OProperty nameProperty = funcStepVClass.createProperty(OrientComponentDB.STEP_CLASS_PROPERTY_NAME, OType.STRING);
        nameProperty.setMandatory(true);

        funcStepVClass.createIndex(OrientComponentDB.STEP_CLASS_INDEX_NAME, OClass.INDEX_TYPE.UNIQUE.toString(), null, null, "AUTOSHARDING", new String[]{OrientComponentDB.STEP_CLASS_PROPERTY_NAME});

        OrientUtils.createClass(OrientComponentDB.GE_STEP_CLASS, "E", 1, dbSession);

        LOGGER.info("New model created");
    }

    @ChangelogOrder(order = 3, uuid = "20190515-init-testcase-model")
    public static void initTestCaseModel(ODatabaseSession dbSession) {
        OClass testCaseVClass = OrientUtils.createClass(OrientComponentDB.TESTCASE_CLASS, "V", 1, dbSession);

        LOGGER.info("TestCase class created");
    }

    @ChangelogOrder(order = 4, uuid = "20190611-init-lucene-index")
    public static void initLuceneIndex(ODatabaseSession dbSession) {
        /* nothing to see */
    }

    @ChangelogOrder(order = 5, uuid = "20190828-update-selenium-task-parameters")
    public static void updateSeleniumTaskParameters(ODatabaseSession dbSession) {
    }

    @ChangelogOrder(order = 6, uuid = "20191127-update-selenium-task-parameters")
    public static void updateSeleniumTaskParametersRight(ODatabaseSession dbSession) {

        String QUERY_FSTEPS_IMPLEMENTATION_ID =
            "SELECT @rid, " + OrientComponentDB.STEP_CLASS_PROPERTY_IMPLEMENTATION +
                " FROM " + OrientComponentDB.STEP_CLASS + " WHERE " + OrientComponentDB.STEP_CLASS_PROPERTY_IMPLEMENTATION + " is not null";

        OResultSet steps = dbSession.query(QUERY_FSTEPS_IMPLEMENTATION_ID);
        final ObjectMapper objectMapper = new ObjectMapper();
        AtomicInteger countUpdated = new AtomicInteger();
        Lists.newArrayList(steps)
            .stream()
            .map(step -> (OVertex) dbSession.load(new ORecordId(step.getProperty("@rid").toString())))
            .forEach(step -> {
                try {
                    JsonNode implementationNode = objectMapper.readTree(step.getProperty("implementation").toString());
                    if(implementationNode != null) {
                        switch (implementationNode.get("identifier").asText()) {
                            case "selenium-get":
                                removeInputsByName(implementationNode, "action", "by", "wait", "switchType", "menuItemSelector");
                                break;
                            case "selenium-scroll-to":
                            case "selenium-get-text":
                            case "selenium-click":
                            case "selenium-hover-then-click":
                                removeInputsByName(implementationNode, "action", "value", "switchType", "menuItemSelector");
                                break;
                            case "selenium-send-keys":
                            case "selenium-wait":
                                removeInputsByName(implementationNode, "action", "switchType", "menuItemSelector");
                                break;
                            case "selenium-switch-to":
                                removeInputsByName(implementationNode, "action", "value", "menuItemSelector");
                                break;
                        }
                    }
                    step.setProperty(OrientComponentDB.STEP_CLASS_PROPERTY_IMPLEMENTATION, objectMapper.writeValueAsString(implementationNode));
                    step.save();
                    countUpdated.incrementAndGet();
                } catch (Exception e) {
                    LOGGER.error("Cannot read/write implementation", e);
                }
            });
        LOGGER.info(countUpdated.get() + " selenium tasks updated");
    }

    private static void removeInputsByName(JsonNode node, String... inputsNames) {
        JsonNode inputsNode = node.get("inputs");
        if (inputsNode.isArray()) {
            List<String> inputsNamesList = Arrays.asList(inputsNames);
            ArrayNode inputs = (ArrayNode) node.get("inputs");
            for (int i = inputs.size() - 1; i >= 0; i--) {
                JsonNode input = inputs.get(i);
                if (inputsNamesList.contains(input.get("name").asText())) {
                    inputs.remove(i);
                }
            }
        } else if (inputsNode.isObject()) {
            for (String inputName: inputsNames) {
                ((ObjectNode)inputsNode).remove(inputName);
            }
        }
    }

    @ChangelogOrder(order = 7, uuid = "2020127-update-component-strategies")
    public static void updateComponentStrategies(ODatabaseSession dbSession) {

        // Update null strategy with Default strategy
        try(OResultSet steps = dbSession.query("SELECT FROM " + OrientComponentDB.STEP_CLASS + " WHERE `strategy` is null")) {
            steps.stream()
                .filter(o -> o.getProperty("strategy") == null)
                .map(OResult::getVertex)
                .forEach(ov -> ov.ifPresent(v -> {
                    OElement strategy = dbSession.newElement();
                    strategy.setProperty("name", "", OType.STRING);
                    strategy.setProperty("parameters", Collections.emptyMap(), OType.EMBEDDEDMAP);
                    v.setProperty(OrientComponentDB.STEP_CLASS_PROPERTY_STRATEGY, strategy, OType.EMBEDDED);
                    v.save();
                }));
        }

        // Update strategy with data as Loop strategy
        try(OResultSet steps = dbSession.query("SELECT FROM " + OrientComponentDB.STEP_CLASS + " WHERE `strategy` is not null")) {
            steps.stream()
                .filter(o -> o.getProperty("strategy") != null)
                .filter(o -> ((OResult) o.getProperty("strategy")).hasProperty("data"))
                .map(OResult::getVertex)
                .forEach(ov -> ov.ifPresent(v -> {
                    OElement strategy = dbSession.newElement();
                    strategy.setProperty("name", "Loop", OType.STRING);
                    strategy.setProperty("parameters", Collections.singletonMap("data", ((OElement) v.getProperty("strategy")).getProperty("data")), OType.EMBEDDEDMAP);
                    v.setProperty(OrientComponentDB.STEP_CLASS_PROPERTY_STRATEGY, strategy, OType.EMBEDDED);
                    v.save();
                }));
        }

        // Update strategy with retryDelay as Retry strategy
        try(OResultSet steps = dbSession.query("SELECT FROM " + OrientComponentDB.STEP_CLASS + " WHERE `strategy` is not null")) {
            steps.stream()
                .filter(o -> o.getProperty("strategy") != null)
                .filter(o -> ((OResult) o.getProperty("strategy")).hasProperty("retryDelay"))
                .map(OResult::getVertex)
                .forEach(ov -> ov.ifPresent(v -> {
                    Map<String, Object> parameters = new HashMap<>(2);
                    parameters.put("timeOut", ((OElement) v.getProperty("strategy")).getProperty("timeOut"));
                    parameters.put("retryDelay", ((OElement) v.getProperty("strategy")).getProperty("retryDelay"));
                    OElement strategy = dbSession.newElement();
                    strategy.setProperty("name", "retry-with-timeout", OType.STRING);
                    strategy.setProperty("parameters", parameters, OType.EMBEDDEDMAP);
                    v.setProperty(OrientComponentDB.STEP_CLASS_PROPERTY_STRATEGY, strategy, OType.EMBEDDED);
                    v.save();
                }));
        }
    }

    @ChangelogOrder(order = 8, uuid = "20200217-change-lucene-index-analyzer")
    public static void changeLuceneIndexAnalyzer(ODatabaseSession dbSession) {
        String FSTEP_LUCENE_INDEX_QUERY = "CREATE INDEX " + OrientComponentDB.STEP_CLASS_FULLTEXTSEARCH_INDEX_NAME + " ON " + OrientComponentDB.STEP_CLASS + "(" + OrientComponentDB.STEP_CLASS_PROPERTY_NAME + ") " +
            " FULLTEXT ENGINE LUCENE METADATA {" +
            " \"index\": \"com.chutneytesting.design.infra.storage.scenario.compose.orient.lucene.ComposableStepNameAnalyzer\"," +
            " \"query\": \"com.chutneytesting.design.infra.storage.scenario.compose.orient.lucene.ComposableStepNameAnalyzer\"" +
            "}";

        OIndexManager indexManager = dbSession.getMetadata().getIndexManager();
        if (!indexManager.existsIndex(OrientComponentDB.STEP_CLASS_FULLTEXTSEARCH_INDEX_NAME)) {
            try (OResultSet ignored = dbSession.command(FSTEP_LUCENE_INDEX_QUERY)) { }
        }

        if (indexManager.existsIndex(OrientComponentDB.STEP_CLASS_FULLTEXTSEARCH_INDEX_NAME)) {
            indexManager.dropIndex(OrientComponentDB.STEP_CLASS_FULLTEXTSEARCH_INDEX_NAME);
        }

        try (OResultSet ignored = dbSession.command(FSTEP_LUCENE_INDEX_QUERY)) { }
    }

    @ChangelogOrder(order = 9, uuid = "20200424-init-dataset-model")
    public static void initDataSetModel(ODatabaseSession dbSession) {
        OrientUtils.createClass(OrientComponentDB.DATASET_CLASS, null, 1, dbSession);

        OClass oDataSetHistoryClass = OrientUtils.createClass(OrientComponentDB.DATASET_HISTORY_CLASS, null, 0, dbSession);
        OProperty refIdProperty = oDataSetHistoryClass.createProperty(OrientComponentDB.DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID, OType.LINK);
        refIdProperty.setMandatory(true);

        oDataSetHistoryClass.createIndex(OrientComponentDB.DATASET_HISTORY_CLASS_INDEX_LAST, OClass.INDEX_TYPE.NOTUNIQUE.toString(), null, null, "AUTOSHARDING", new String[]{OrientComponentDB.DATASET_HISTORY_CLASS_PROPERTY_DATASET_ID});

        LOGGER.info("DataSet model created");
    }

    @ChangelogOrder(order = 10, uuid = "20201028-init-testcase-updateDate")
    public static void initTestCaseUpdateDate(ODatabaseSession dbSession) {
        try (OResultSet testcases = dbSession.query("SELECT FROM " + OrientComponentDB.TESTCASE_CLASS)) {
            testcases.stream()
                .map(OResult::getElement)
                .forEach(tc -> tc.ifPresent(e -> {
                    Optional<Object> creationDate = ofNullable(e.getProperty(TESTCASE_CLASS_PROPERTY_CREATIONDATE));
                    if (creationDate.isPresent()) {
                        e.setProperty(TESTCASE_CLASS_PROPERTY_UPDATEDATE, creationDate.get(), OType.DATETIME);
                        e.save();
                    } else {
                        LOGGER.warn("TestCase " + e.getIdentity().toString() + " does not have a creation date");
                    }
                }));
        }

        LOGGER.info("TestCase update date initialized");
    }
}
