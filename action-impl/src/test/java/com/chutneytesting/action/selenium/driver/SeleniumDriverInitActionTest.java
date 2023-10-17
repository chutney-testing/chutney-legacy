package com.chutneytesting.action.selenium.driver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.chutneytesting.action.TestFinallyActionRegistry;
import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumDriverInitActionTest {

    public static Stream<Arguments> parametersForShould_create_quit_finally_action_and_output_webdriver_when_executed() {
        TestLogger logger = new TestLogger();
        TestFinallyActionRegistry finallyActionRegistry = spy(new TestFinallyActionRegistry());

        SeleniumFirefoxInitAction localFirefoxAction = spy(new SeleniumFirefoxInitAction(finallyActionRegistry, logger, "", false, "driverPath", "browserPath", null, null));
        SeleniumChromeDriverInitAction localChromeAction = spy(new SeleniumChromeDriverInitAction(finallyActionRegistry, logger, "", false, "driverPath", "browserPath", null));

        WebDriver firefoxDriver = mock(FirefoxDriver.class);
        WebDriver chromeDriver = mock(ChromeDriver.class);

        doReturn(firefoxDriver)
            .when(localFirefoxAction).localWebDriver(any());
        doReturn(chromeDriver)
            .when(localChromeAction).localWebDriver(any());

        SeleniumFirefoxInitAction remoteFirefoxAction = spy(new SeleniumFirefoxInitAction(finallyActionRegistry, logger, "http://hub:99", false, "", "", null, null));
        SeleniumChromeDriverInitAction remoteChromeAction = spy(new SeleniumChromeDriverInitAction(finallyActionRegistry, logger, "http://hub:99", false, "", "", null));

        RemoteWebDriver firefoxRemoteWebDriver = mock(RemoteWebDriver.class);
        RemoteWebDriver chromeRemoteWebDriver = mock(RemoteWebDriver.class);
        doReturn(firefoxRemoteWebDriver)
            .when(remoteFirefoxAction).createRemoteWebDriver(any());
        doReturn(chromeRemoteWebDriver)
            .when(remoteChromeAction).createRemoteWebDriver(any());
        return Stream.of(
            of(localFirefoxAction, firefoxDriver, finallyActionRegistry),
            of(localChromeAction, chromeDriver, finallyActionRegistry),
            of(remoteFirefoxAction, firefoxRemoteWebDriver, finallyActionRegistry),
            of(remoteChromeAction, chromeRemoteWebDriver, finallyActionRegistry)
        );
    }

    @ParameterizedTest
    @MethodSource("parametersForShould_create_quit_finally_action_and_output_webdriver_when_executed")
    public void should_create_quit_finally_action_and_output_webdriver_when_executed(AbstractSeleniumDriverInitAction action, WebDriver driverToAssert, TestFinallyActionRegistry finallyActionRegistry) {
        reset(finallyActionRegistry);

        // When
        List<String> strings = action.validateInputs();
        assertThat(strings).isEmpty();
        ActionExecutionResult result = action.execute();

        // Then
        verify(finallyActionRegistry, times(1)).registerFinallyAction(any());
        FinallyAction quitAction = finallyActionRegistry.finallyActions.get(0);
        assertThat(quitAction.type()).isEqualTo("selenium-quit");
        assertThat(quitAction.inputs()).containsOnlyKeys("web-driver");

        assertThat(result.status).isEqualTo(ActionExecutionResult.Status.Success);
        assertThat(result.outputs).containsOnlyKeys("webDriver");
        assertThat(result.outputs).containsValues(driverToAssert);
        assertThat(result.outputs.get("webDriver")).isInstanceOf(driverToAssert.getClass());
    }

    @ParameterizedTest
    @MethodSource("parametersForshould_retun_error_when_wrong_input")
    public void should_retun_error_when_wrong_input(Action action) {
        assertThat(action.validateInputs()).hasSize(1);
    }

    public static Stream<Arguments> parametersForshould_retun_error_when_wrong_input() {
        Action everyInputEmpty = new SeleniumFirefoxInitAction(mock(FinallyActionRegistry.class), null, "", false, "", "", null, null);
        Action driverPathOKBrowserPathEmpty = new SeleniumChromeDriverInitAction(mock(FinallyActionRegistry.class), null, "", false, "driverPath", "", null);
        Action driverPathEmptyBrowserPathOK = new SeleniumChromeDriverInitAction(mock(FinallyActionRegistry.class), null, "", false, "", "browserPath", null);

        Action everyInputNull = new SeleniumFirefoxInitAction(mock(FinallyActionRegistry.class), null, null, false, null, null, null, null);
        Action driverPathOKBrowserPathNull = new SeleniumChromeDriverInitAction(mock(FinallyActionRegistry.class), null, null, false, "driverPath", null, null);
        Action driverPathNullBrowserPathOK = new SeleniumChromeDriverInitAction(mock(FinallyActionRegistry.class), null, null, false, null, "", null);

        return Stream.of(
            of(everyInputEmpty),
            of(driverPathOKBrowserPathEmpty),
            of(driverPathEmptyBrowserPathOK),
            of(everyInputNull),
            of(driverPathOKBrowserPathNull),
            of(driverPathNullBrowserPathOK)
        );
    }
}
