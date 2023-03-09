package com.chutneytesting.action.selenium;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumRemoteDriverInitAction implements Action {

    private final FinallyActionRegistry finallyActionRegistry;
    private final Logger logger;
    private final String hubUrl;
    private final String browser;

    public SeleniumRemoteDriverInitAction(Logger logger,
                                        FinallyActionRegistry finallyActionRegistry,
                                        @Input("hub") String hubUrl,
                                        @Input("browser") String browser) {
        this.finallyActionRegistry = finallyActionRegistry;
        this.logger = logger;
        this.hubUrl = hubUrl;
        this.browser = browser;
    }

    @Override
    public ActionExecutionResult execute() {
        WebDriver webDriver;
        switch (Optional.ofNullable(browser).orElse("")) {
            case "chrome" -> webDriver = createChromeRemoteWebDriver();
            case "internet explorer" -> webDriver = createInternetExplorerRemoteWebDriver();
            default -> webDriver = createFirefoxRemoteWebDriver();
        }

        if (webDriver != null) {
            configureWebDriver(webDriver);
            logger.info("RemoteWebDriver created : " + webDriver);
            createQuitFinallyAction(webDriver);
            return ActionExecutionResult.ok(SeleniumDriverInitAction.toOutputs(webDriver));
        }

        logger.error("RemoteWebDriver creation failed.");
        return ActionExecutionResult.ko();
    }


    private void createQuitFinallyAction(WebDriver webDriver) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("selenium-quit", SeleniumRemoteDriverInitAction.class)
                .withInput("web-driver", webDriver)
                .build()
        );
        logger.info("Quit finally action registered");
    }

    WebDriver createChromeRemoteWebDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("start-maximized");
        chromeOptions.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        return createRemoteWebDriver(chromeOptions);
    }

    WebDriver createInternetExplorerRemoteWebDriver() {
        return createRemoteWebDriver(SeleniumDriverInitAction.setIeOptions());
    }

    WebDriver createFirefoxRemoteWebDriver() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setHeadless(false);
        firefoxOptions.setLogLevel(FirefoxDriverLogLevel.FATAL);
        return createRemoteWebDriver(firefoxOptions);
    }

    private WebDriver createRemoteWebDriver(Capabilities capabilities) {
        try {
            return new RemoteWebDriver(new URL(hubUrl), capabilities);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    void configureWebDriver(WebDriver webDriver) {
        try {
            webDriver.manage().timeouts().implicitlyWait(Duration.of(0, SECONDS));
        } catch (Exception e) {
            logger.error("Default configuration of the remote webDriver failed");
        }
    }
}
