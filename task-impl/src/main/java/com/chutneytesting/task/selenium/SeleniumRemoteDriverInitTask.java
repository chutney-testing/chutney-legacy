package com.chutneytesting.task.selenium;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumRemoteDriverInitTask implements Task {

    private final FinallyActionRegistry finallyActionRegistry;
    private final Logger logger;
    private final String hubUrl;
    private final String browser;

    public SeleniumRemoteDriverInitTask(Logger logger,
                                        FinallyActionRegistry finallyActionRegistry,
                                        @Input("hub") String hubUrl,
                                        @Input("browser") String browser) {
        this.finallyActionRegistry = finallyActionRegistry;
        this.logger = logger;
        this.hubUrl = hubUrl;
        this.browser = browser;
    }

    @Override
    public TaskExecutionResult execute() {
        WebDriver webDriver;
        switch (Optional.ofNullable(browser).orElse("")) {
            case "chrome": {
                webDriver = createChromeRemoteWebDriver();
                break;
            }
            case "internet explorer": {
                webDriver = createInternetExplorerRemoteWebDriver();
                break;
            }
            case "firefox":
            default: {
                webDriver = createFirefoxRemoteWebDriver();
                break;
            }
        }

        if (webDriver != null) {
            configureWebDriver(webDriver);
            logger.info("RemoteWebDriver created : " + webDriver);
            createQuitFinallyAction(webDriver);
            return TaskExecutionResult.ok(SeleniumDriverInitTask.toOutputs(webDriver));
        }

        logger.error("RemoteWebDriver creation failed.");
        return TaskExecutionResult.ko();
    }


    private void createQuitFinallyAction(WebDriver webDriver) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("selenium-quit", SeleniumRemoteDriverInitTask.class.getSimpleName())
                .withInput("web-driver", webDriver)
                .build()
        );
        logger.info("Quit finally action registered");
    }

    WebDriver createChromeRemoteWebDriver() {
        DesiredCapabilities chromeCapability = DesiredCapabilities.chrome();
        chromeCapability.setBrowserName(browser);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("start-maximized");
        chromeCapability.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        return createRemoteWebDriver(chromeCapability);
    }

    WebDriver createInternetExplorerRemoteWebDriver() {
        DesiredCapabilities internetExplorerCapabilities = DesiredCapabilities.internetExplorer();
        internetExplorerCapabilities.setBrowserName(browser);
        return createRemoteWebDriver(SeleniumDriverInitTask.setIeOptions());
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
            webDriver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Default configuration of the remote webDriver failed");
        }
    }
}
