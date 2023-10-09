package com.chutneytesting.action.selenium;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumRemoteDriverInitAction implements Action {

    private final FinallyActionRegistry finallyActionRegistry;
    private final Logger logger;
    private final String hubUrl;
    private final String browser;
    private final Boolean headless;
    private final List<String> chromeOptions;
    private final String firefoxProfile;
    private final Map<String, Object> firefoxPreferences;

    public SeleniumRemoteDriverInitAction(Logger logger,
                                          FinallyActionRegistry finallyActionRegistry,
                                          @Input("hub") String hubUrl,
                                          @Input("browser") String browser,
                                          @Input("headless") Boolean headless,
                                          @Input("chromeOptions") List<String> chromeOptions,
                                          @Input("firefoxProfile") String firefoxProfile,
                                          @Input("firefoxPreferences") Map<String, Object> firefoxPreferences) {
        this.finallyActionRegistry = finallyActionRegistry;
        this.logger = logger;
        this.hubUrl = hubUrl;
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
            case "chrome" -> webDriver = createChromeRemoteWebDriver();
            case "internet explorer" -> webDriver = createInternetExplorerRemoteWebDriver();
            case "firefox" -> webDriver = createFirefoxRemoteWebDriver();
            default -> {
                logger.error("browser must bes chrome, internet explorer or firefox");
                return ActionExecutionResult.ko();
            }
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
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        options.setCapability(ChromeOptions.CAPABILITY, options);
        if (headless) {
            options.addArguments("--headless");
        }
        chromeOptions.stream().forEach(opt -> {
            options.addArguments(opt);
        });
        return createRemoteWebDriver(options);
    }

    WebDriver createInternetExplorerRemoteWebDriver() {
        return createRemoteWebDriver(SeleniumDriverInitAction.setIeOptions());
    }

    WebDriver createFirefoxRemoteWebDriver() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        try {
            FirefoxProfile profile = firefoxProfile != null ? FirefoxProfile.fromJson(firefoxProfile) : new FirefoxProfile();
            firefoxPreferences.entrySet().forEach(pref -> profile.setPreference(pref.getKey(), pref.getValue()));
            firefoxOptions.setProfile(profile);
        } catch (IOException e) {
            logger.error("Failed to read firefox profile" + e.getMessage());
        }

        firefoxOptions.setHeadless(headless);
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
