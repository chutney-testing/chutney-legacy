package com.chutneytesting.action.selenium;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

public class SeleniumDriverInitAction implements Action {

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final String driverPath;
    private final String browserPath;
    private final String browser;

    public SeleniumDriverInitAction(Logger logger,
                                  FinallyActionRegistry finallyActionRegistry,
                                  @Input("driverPath") String driverPath,
                                  @Input("browserPath") String browserPath,
                                  @Input("browser") String browser) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.driverPath = driverPath;
        this.browserPath = browserPath;
        this.browser = browser;
    }

    @Override
    public ActionExecutionResult execute() {
        WebDriver webDriver = ("Internet Explorer".equals(browser)) ? createInternetExplorerWebDriver() : createFirefoxWebDriver();
        configureWebDriver(webDriver);
        logger.info("WebDriver created : " + webDriver);
        createQuitFinallyAction(webDriver);
        return ActionExecutionResult.ok(toOutputs(webDriver));
    }

    private void createQuitFinallyAction(WebDriver webDriver) {

        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("selenium-quit", SeleniumDriverInitAction.class)
                .withInput("web-driver", webDriver)
                .build()
        );
        logger.info("Quit finally action registered");
    }

    WebDriver createFirefoxWebDriver() {
        System.setProperty("webdriver.gecko.driver", driverPath);
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);
        options.setBinary(browserPath);
        options.setLogLevel(FirefoxDriverLogLevel.FATAL);
        return new FirefoxDriver(options);
    }

    WebDriver createInternetExplorerWebDriver() {
        System.setProperty("webdriver.ie.driver", driverPath);
        return new InternetExplorerDriver(setIeOptions());
    }

    static InternetExplorerOptions setIeOptions() {
        InternetExplorerOptions ieOptions = new InternetExplorerOptions();
        ieOptions.setCapability("nativeEvents", true);
        ieOptions.setCapability("unexpectedAlertBehaviour", "accept");
        ieOptions.setCapability("ignoreProtectedModeSettings", true);
        ieOptions.setCapability("disable-popup-blocking", true);
        ieOptions.setCapability("enablePersistentHover", true);
        ieOptions.setCapability("ignoreZoomSetting", false);
        ieOptions.setCapability("javascriptEnabled", true);
        ieOptions.setCapability("ensureCleanSession", true);
        ieOptions.setCapability("AcceptInsecureCertificates", true);
        ieOptions.setCapability("introduceInstabilityByIgnoringProtectedModeSettings", true);
        return ieOptions;
    }

    void configureWebDriver(WebDriver webDriver) {
        try {
            webDriver.manage().timeouts().implicitlyWait(Duration.of(0, SECONDS));
        } catch (Exception e) {
            logger.error("Default configuration of webDriver failed");
        }
    }

    static Map<String, Object> toOutputs(WebDriver webDriver) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("webDriver", webDriver);
        return outputs;
    }
}
