package blackbox.stepdef;

import blackbox.IntegrationTestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@CucumberContextConfiguration()
public class MainStepDefs {
}
