package blackbox.stepdef;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TestContext {
    private final Map<ContextKey, Object> variables = new HashMap<>();

    public void putExecutionReport(Object value) {
        variables.put(ContextKey.EXECUTION_REPORT, value);
    }

    public void putScenarioId(String value) {
        variables.put(ContextKey.LAST_SCENARIO_ID, value);
    }

    public void putViewedScenario(Object value) {
        variables.put(ContextKey.LAST_VIEWED_SCENARIO, value);
    }

    public <T> T getExecutionReport() {
        return (T) variables.get(ContextKey.EXECUTION_REPORT);
    }

    public void putCampaign(Object value) {
        variables.put(ContextKey.CAMPAIGN, value);
    }

    public void putStatus(Object value) {
        variables.put(ContextKey.STATUS, value);
    }

    public void putHEADERS(Object value) {
        variables.put(ContextKey.HEADERS, value);
    }

    public <T> T getCampaign() {
        return (T) variables.get(ContextKey.CAMPAIGN);
    }

    public <T> T getStatus() {
        return (T) variables.get(ContextKey.STATUS);
    }

    public <T> T getHEADERS() {
        return (T) variables.get(ContextKey.HEADERS);
    }

    public String getLastScenarioId() {
        return (String) variables.get(ContextKey.LAST_SCENARIO_ID);
    }

    public <T> T getLastViewedScenario() {
        return (T) variables.get(ContextKey.LAST_VIEWED_SCENARIO);
    }

    public <T> T getTrustStorePath() {
        return (T) variables.get(ContextKey.TRUSTSTORE_PATH);
    }

    public void putTrustStorePath(Object value) {
        variables.put(ContextKey.TRUSTSTORE_PATH, value);
    }

    public void putMockServer(Object value) {
        variables.put(ContextKey.MOCK_SERVER, value);
    }

    public <T> T getMockServer() {
        return (T) variables.get(ContextKey.MOCK_SERVER);
    }

    public void addScenarioVariables(String key, String value) {
        Map<String, String> scenario_vars = getScenarioVariables();
        if (scenario_vars == null) {
            scenario_vars = new HashMap<>();
            variables.put(ContextKey.SCENARIO_VARIABLES, scenario_vars);
        }
        scenario_vars.put(key, value);
    }

    public Map<String, String> getScenarioVariables() {
        return (Map<String, String>) variables.get(ContextKey.SCENARIO_VARIABLES);
    }

    public String replaceVariables(String content) {
        if(getTrustStorePath() != null) {
            content = content.replace("%%trustStoreAbsolutePath%%", getTrustStorePath());
        }
        Map<String, String> scenario_vars = getScenarioVariables();
        if(scenario_vars != null) {
            for (String k : scenario_vars.keySet()) {
                content = content.replace("##"+k+"##", scenario_vars.get(k));
            }
        }
        return content;
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
        SCENARIO_VARIABLES
    }
}
