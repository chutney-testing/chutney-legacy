package blackbox.stepdef.tasks;

import blackbox.stepdef.edition.EditionStepDef;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import java.io.File;
import org.apache.commons.lang3.SystemUtils;
import org.junit.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class SeleniumStepsDef {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumStepsDef.class);

    private static final String DRIVER_PATH = "##DRIVER_PATH##";
    private static final String BROWSER_PATH = "##BROWSER_PATH##";
    private static final String ACTIONS_STEPS_SERVER_PORT = "##SERVER_PORT##";
    private static final String ACTIONS_STEPS_PLACEHOLDER = "##ACTIONS_STEPS##";

    private static final String SELENIUM_SCENARIO_TEMPLATE = String.join(
        System.lineSeparator(),
        "{",
            "title: selenium scenario",
            "tags: []",
            "executions: []",
            "content:",
            "'''",
            "{",
                "scenario: {",
                "name: Test driver init and get url",
                    "steps: [",
                        "{",
                            "name : Init Selenium driver",
                            "type: selenium-driver-init",
                            "inputs: {",
                                "driverPath: " + DRIVER_PATH + "",
                                "browserPath: " + BROWSER_PATH + "",
                            "},",
                        "},",
                        ACTIONS_STEPS_PLACEHOLDER,
                    "]",
                "}",
            "}",
            "'''",
        "}"
    );

    @Before
    public void before() {
        // Did not work ??? Why ??
        //Assume.assumeFalse(SystemUtils.IS_OS_UNIX);
    }

    @Value("${server.port}")
    private int port;
    private EditionStepDef scenarioEditionStepDefs;

    public SeleniumStepsDef(EditionStepDef scenarioEditionStepDefs) {
        this.scenarioEditionStepDefs = scenarioEditionStepDefs;
    }

    @Given("^a scenario with following selenium actions is saved$")
    public void saveSeleniumScenario(String scenarioSeleniumPartialSeleniumSteps) throws Throwable {
        String template = SELENIUM_SCENARIO_TEMPLATE.replace(DRIVER_PATH, absolutePathFromClassPath("selenium/" + osPath() + arch32Or64() + "firefox/" + driverExe()));
        template = template.replace(BROWSER_PATH, absolutePathFromClassPath("selenium/" + osPath() + arch32Or64() + "firefox/" + browserExe()));

        scenarioSeleniumPartialSeleniumSteps = scenarioSeleniumPartialSeleniumSteps.replace(ACTIONS_STEPS_SERVER_PORT, String.valueOf(port));
        String completeScenario = template.replace(ACTIONS_STEPS_PLACEHOLDER, scenarioSeleniumPartialSeleniumSteps);

        this.scenarioEditionStepDefs.savedScenario(completeScenario);
    }

    private String driverExe() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "geckodriver.exe";
        } else if(SystemUtils.IS_OS_UNIX) {
            return "geckodriver";
        }
        throw new AssumptionViolatedException("OS not supported...");
    }

    private String browserExe() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "firefox/App/Firefox/firefox.exe";
        } else if(SystemUtils.IS_OS_UNIX) {
            return "firefox/firefox";
        }
        throw new AssumptionViolatedException("OS not supported...");
    }

    private String osPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "win/";
        } else if(SystemUtils.IS_OS_UNIX) {
            return "linux/";
        }
        throw new AssumptionViolatedException("OS not supported...");
    }

    private String arch32Or64() {
        boolean is64bit;
        if (SystemUtils.IS_OS_WINDOWS) {
            is64bit = (System.getenv("ProgramFiles(x86)") != null);
        } else {
            is64bit = (System.getProperty("os.arch").contains("64"));
        }

        if (is64bit) {
            return "64/";
        } else {
            return "32/";
        }
    }

    private String absolutePathFromClassPath(String path) {
        LOGGER.info("Trying to get file path for : {}", path);

        return new File(SeleniumStepsDef.class.getClassLoader().getResource(path).getFile())
            .getAbsolutePath();
    }
}
