package com.chutneytesting.action.selenium.driver;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.List;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumChromeDriverInitAction extends AbstractSeleniumDriverInitAction {

    private final List<String> chromeOptions;

    public SeleniumChromeDriverInitAction(FinallyActionRegistry finallyActionRegistry,
                                          Logger logger,
                                          @Input("hub") String hubUrl,
                                          @Input("headless") Boolean headless,
                                          @Input("driverPath") String driverPath,
                                          @Input("browserPath") String browserPath,
                                          @Input("chromeOptions") List<String> chromeOptions) {
        super(finallyActionRegistry, logger, hubUrl, headless, driverPath, browserPath);
        this.chromeOptions = ofNullable(chromeOptions).orElse(emptyList());
    }

    @Override
    protected MutableCapabilities buildWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        if (headless) {
            options.addArguments("--headless");
        }
        chromeOptions.forEach(options::addArguments);
        options.setCapability(ChromeOptions.CAPABILITY, options);
        return options;
    }

    @Override
    protected WebDriver localWebDriver(Capabilities capabilities) {
        System.setProperty("webdriver.chrome.driver", driverPath);
        ChromeOptions chromeOptions = new ChromeOptions().merge(capabilities);
        chromeOptions.setBinary(browserPath);
        return new ChromeDriver(chromeOptions);
    }

    @Override
    protected Class<?> getChildClass() {
        return SeleniumChromeDriverInitAction.class;
    }
}
