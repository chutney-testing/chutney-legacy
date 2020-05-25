package blackbox.stepdef.tasks;

import blackbox.stepdef.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import org.apache.activemq.broker.BrokerService;

public class JmsStepsDef {

    private final TestContext context;

    private BrokerService broker;

    public JmsStepsDef(TestContext context) {
        this.context = context;
    }

    @After
    public void destroy() throws Exception {
        if (broker != null) {
            broker.stop();
        }
    }

    @Given("^a mock jms endpoint with reference port (.*) with host (.*)$")
    public void an_jms_server_is_started(String portVarKey, String host) throws Exception {
        broker = new BrokerService();
        broker.addConnector("tcp://"+host+":"+context.getScenarioVariables().get(portVarKey));
        broker.setPersistent(false);
        broker.start();
    }
}
