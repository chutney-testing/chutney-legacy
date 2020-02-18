package blackbox.stepdef;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {
    private final Map<ContextKey, Object> variables = new HashMap<>();

    public void putExecutionReport(Object value) {
        variables.put(ContextKey.EXECUTION_REPORT, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExecutionReport() {
        return (T) variables.get(ContextKey.EXECUTION_REPORT);
    }

    public enum ContextKey {
        EXECUTION_REPORT
    }
}
