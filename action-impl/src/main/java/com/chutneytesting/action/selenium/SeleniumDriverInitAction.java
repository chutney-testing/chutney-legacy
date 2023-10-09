package com.chutneytesting.action.selenium;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

public class SeleniumDriverInitAction implements Action {

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final String driverPath;
    private final String browserPath;
    private final String browser;
    private final Boolean headless;
    private final List<String> chromeOptions;
    private final String firefoxProfile;
    private final Map<String, Object> firefoxPreferences;
    public SeleniumDriverInitAction(Logger logger,
                                    FinallyActionRegistry finallyActionRegistry,
                                    @Input("driverPath") String driverPath,
                                    @Input("browserPath") String browserPath,
                                    @Input("browser") String browser,
                                    @Input("headless") Boolean headless,
                                    @Input("chromeOptions") List<String> chromeOptions,
                                    @Input("firefoxProfile") String firefoxProfile,
                                    @Input("firefoxPreferences") Map<String, Object> firefoxPreferences) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.driverPath = driverPath;
        this.browserPath = browserPath;
        this.browser = browser;
        this.headless = ofNullable(headless).orElse(true);
        this.chromeOptions = ofNullable(chromeOptions).orElse(emptyList());
        this.firefoxProfile = firefoxProfile;
        this.firefoxPreferences = ofNullable(firefoxPreferences).orElse(emptyMap());
    }

    @Override
    public ActionExecutionResult execute() {
        WebDriver webDriver;
        switch (ofNullable(browser).orElse("")) {
            case "chrome" -> webDriver = createChromeWebDriver();
            case "internet explorer" -> webDriver = createInternetExplorerWebDriver();
            case "firefox" -> webDriver = createFirefoxWebDriver();
            default -> {
                logger.error("browser must bes chrome, internet explorer or firefox");
                return ActionExecutionResult.ko();
            }
        }
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
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        try {
            FirefoxProfile profile = firefoxProfile != null ? FirefoxProfile.fromJson(firefoxProfile) : new FirefoxProfile();
            firefoxPreferences.entrySet().forEach(pref -> profile.setPreference(pref.getKey(), pref.getValue()));
            firefoxOptions.setProfile(profile);
        } catch (IOException e) {
            logger.error("Failed to read firefox profile" + e.getMessage());
        }
        firefoxOptions.setHeadless(headless);
        firefoxOptions.setBinary(browserPath);
        firefoxOptions.setLogLevel(FirefoxDriverLogLevel.FATAL);
        return new FirefoxDriver(firefoxOptions);
    }

    WebDriver createInternetExplorerWebDriver() {
        System.setProperty("webdriver.ie.driver", driverPath);
        return new InternetExplorerDriver(setIeOptions());
    }

    WebDriver createChromeWebDriver() {
        System.setProperty("webdriver.chrome.driver", driverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        options.setCapability(ChromeOptions.CAPABILITY, options);
        if (headless) {
            options.addArguments("--headless");
        }
        chromeOptions.stream().forEach(opt -> {
            options.addArguments(opt);
        });
        return new ChromeDriver(options);
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
