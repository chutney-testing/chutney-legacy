package blackbox.stepdef;

import cucumber.api.java.Before;
import blackbox.IntegrationTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
public class MainStepDefs {

    /**
     * Cucumber needs an annotated method to starts spring with {@link IntegrationTestConfiguration}
     */
    @Before
    public void springInitialization() {
        // Nothing to do
    }
}
