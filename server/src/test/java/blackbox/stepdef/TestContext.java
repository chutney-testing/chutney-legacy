package blackbox.stepdef;

import static blackbox.stepdef.TestContext.ContextKey.CAMPAIGN;
import static blackbox.stepdef.TestContext.ContextKey.EXECUTION_REPORT;
import static blackbox.stepdef.TestContext.ContextKey.HEADERS;
import static blackbox.stepdef.TestContext.ContextKey.HTTP_RESPONSE_BODY;
import static blackbox.stepdef.TestContext.ContextKey.LAST_SCENARIO_ID;
import static blackbox.stepdef.TestContext.ContextKey.LAST_VIEWED_SCENARIO;
import static blackbox.stepdef.TestContext.ContextKey.MOCK_SERVER;
import static blackbox.stepdef.TestContext.ContextKey.SCENARIO_VARIABLES;
import static blackbox.stepdef.TestContext.ContextKey.STATUS;
import static blackbox.stepdef.TestContext.ContextKey.TESTCASE_EDITION;
import static blackbox.stepdef.TestContext.ContextKey.TRUSTSTORE_PATH;

import com.chutneytesting.design.api.editionlock.TestCaseEditionDto;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TestContext {
    private final Map<ContextKey, Object> variables = new HashMap<>();

    public void putExecutionReport(Object value) {
        variables.put(EXECUTION_REPORT, value);
    }

    public void putScenarioId(String value) {
        variables.put(LAST_SCENARIO_ID, value);
    }

    public void putViewedScenario(Object value) {
        variables.put(LAST_VIEWED_SCENARIO, value);
    }

    public <T> T getExecutionReport() {
        return (T) variables.get(EXECUTION_REPORT);
    }

    public void putCampaign(Object value) {
        variables.put(CAMPAIGN, value);
    }

    public void putStatus(Object value) {
        variables.put(STATUS, value);
    }

    public void putHEADERS(Object value) {
        variables.put(HEADERS, value);
    }

    public <T> T getCampaign() {
        return (T) variables.get(CAMPAIGN);
    }

    public <T> T getStatus() {
        return (T) variables.get(STATUS);
    }

    public <T> T getHEADERS() {
        return (T) variables.get(HEADERS);
    }

    public String getLastScenarioId() {
        return (String) variables.get(LAST_SCENARIO_ID);
    }

    public <T> T getLastViewedScenario() {
        return (T) variables.get(LAST_VIEWED_SCENARIO);
    }

    public <T> T getTrustStorePath() {
        return (T) variables.get(TRUSTSTORE_PATH);
    }

    public void putTrustStorePath(Object value) {
        variables.put(TRUSTSTORE_PATH, value);
    }

    public void putMockServer(Object value) {
        variables.put(MOCK_SERVER, value);
    }

    public <T> T getMockServer() {
        return (T) variables.get(MOCK_SERVER);
    }

    public void addScenarioVariables(String key, String value) {
        Map<String, String> scenario_vars = getScenarioVariables();
        if (scenario_vars == null) {
            scenario_vars = new HashMap<>();
            variables.put(SCENARIO_VARIABLES, scenario_vars);
        }
        scenario_vars.put(key, value);
    }

    public Map<String, String> getScenarioVariables() {
        return (Map<String, String>) variables.get(SCENARIO_VARIABLES);
    }

    public String replaceVariables(String content) {
        if (getTrustStorePath() != null) {
            content = content.replace("%%trustStoreAbsolutePath%%", getTrustStorePath());
        }
        Map<String, String> scenario_vars = getScenarioVariables();
        if (scenario_vars != null) {
            for (String k : scenario_vars.keySet()) {
                content = content.replace("##" + k + "##", scenario_vars.get(k));
            }
        }
        return content;
    }

    public void putResponseBody(String body) {
        variables.put(HTTP_RESPONSE_BODY, body);
    }

    public String getResponseBody() {
        return (String) variables.get(HTTP_RESPONSE_BODY);
    }

    public void putTestCaseEdition(TestCaseEditionDto testCaseEditionDto) {
        variables.put(TESTCASE_EDITION, testCaseEditionDto);
    }

    public Object getTestCaseEdition() {
        return variables.get(TESTCASE_EDITION);
    }

    public enum ContextKey {
        EXECUTION_REPORT,
        CAMPAIGN,
        STATUS,
        HEADERS,
        LAST_SCENARIO_ID,
        TRUSTSTORE_PATH,
        MOCK_SERVER,
        LAST_VIEWED_SCENARIO,
        SCENARIO_VARIABLES,
        HTTP_RESPONSE_BODY,
        TESTCASE_EDITION
    }
}
