package blackbox.stepdef.tasks;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.qpid.server.SystemLauncher;
import org.springframework.core.io.ClassPathResource;

public class AmqpStepsDef {

    private SystemLauncher systemLauncher;

    public AmqpStepsDef() {
    }

    @After
    public void destroy() {
        if (systemLauncher != null) {
            systemLauncher.shutdown();
        }
    }

    @Given("^an embedded amqp server")
    public void an_amqp_server_is_started() throws Exception {
        systemLauncher = new SystemLauncher();
        systemLauncher.startup(createSystemConfig());
    }

    private Map<String, Object> createSystemConfig() throws IOException {
        Map<String, Object> attributes = new HashMap<>();
        URL initialConfig = (new ClassPathResource("/blackbox/qpid.json")).getURL();
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
        attributes.put("startupLoggedToSystemOut", true);
        return attributes;
    }
}
