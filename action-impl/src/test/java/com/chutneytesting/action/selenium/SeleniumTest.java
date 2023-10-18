package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import com.chutneytesting.action.TestFinallyActionRegistry;
import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.selenium.driver.SeleniumChromeDriverInitAction;
import com.chutneytesting.action.selenium.driver.SeleniumFirefoxDriverInitAction;
import com.chutneytesting.action.spi.ActionExecutionResult;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class SeleniumTest {

    private final Network network = Network.newNetwork();

    @Test
    public void selenium_firefox_remote_driver_integration_test() {
        final BrowserWebDriverContainer firefoxWebDriverContainer = (BrowserWebDriverContainer) new BrowserWebDriverContainer()
            .withCapabilities(new FirefoxOptions())
            .withNetwork(network);

        try (firefoxWebDriverContainer) {
            firefoxWebDriverContainer.start();

            TestLogger logger = new TestLogger();
            TestFinallyActionRegistry finallyActionRegistry = spy(new TestFinallyActionRegistry());
            String url = firefoxWebDriverContainer.getSeleniumAddress().toString();

            SeleniumFirefoxDriverInitAction remoteFirefoxAction = new SeleniumFirefoxDriverInitAction(finallyActionRegistry, logger, url, true, null, null, null, null);
            ActionExecutionResult firefoxActionResult = remoteFirefoxAction.execute();
            assertThat(firefoxActionResult.status).isEqualTo(Success);

            SeleniumQuitAction quitAction = new SeleniumQuitAction(logger, (WebDriver) firefoxActionResult.outputs.get(WEBDRIVER));
            quitAction.execute();
        }
    }

    @Test
    public void selenium_chrome_remote_driver_integration_test() {
        final BrowserWebDriverContainer chromeWebDriverContainer = (BrowserWebDriverContainer) new BrowserWebDriverContainer()
            .withCapabilities(new ChromeOptions())
            .withNetwork(network);
        try (chromeWebDriverContainer) {
            chromeWebDriverContainer.start();

            TestLogger logger = new TestLogger();
            TestFinallyActionRegistry finallyActionRegistry = spy(new TestFinallyActionRegistry());
            String url = chromeWebDriverContainer.getSeleniumAddress().toString();

            SeleniumChromeDriverInitAction remoteChromeAction = new SeleniumChromeDriverInitAction(finallyActionRegistry, logger, url, true, null, null, null);
            ActionExecutionResult chromeActionResult = remoteChromeAction.execute();
            assertThat(chromeActionResult.status).isEqualTo(Success);

            SeleniumQuitAction quitAction = new SeleniumQuitAction(logger, (WebDriver) chromeActionResult.outputs.get(WEBDRIVER));
            quitAction.execute();
        }
    }

}
