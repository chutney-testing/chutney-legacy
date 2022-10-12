package com.chutneytesting.action.selenium;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.chutneytesting.action.TestFinallyActionRegistry;
import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.ActionExecutionResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class SeleniumRemoteDriverInitActionTest {

    public static Object[] parametersForShould_create_quit_finally_action_and_output_remote_webdriver_when_executed() {
        TestLogger logger = new TestLogger();
        TestFinallyActionRegistry finallyActionRegistry = spy(new TestFinallyActionRegistry());

        SeleniumRemoteDriverInitAction firefoxAction = spy(new SeleniumRemoteDriverInitAction(logger, finallyActionRegistry, "", "firefox"));
        SeleniumRemoteDriverInitAction ieAction = spy(new SeleniumRemoteDriverInitAction(logger, finallyActionRegistry, "","internet explorer"));
        SeleniumRemoteDriverInitAction chromeAction = spy(new SeleniumRemoteDriverInitAction(logger, finallyActionRegistry, "","chrome"));


        WebDriver firefoxRemoteDriver = mock(FirefoxDriver.class);
        WebDriver internetExplorerRemoteDriver = mock(InternetExplorerDriver.class);
        WebDriver chromeRemoteDriver = mock(ChromeDriver.class);

        doNothing()
            .when(firefoxAction).configureWebDriver(eq(firefoxRemoteDriver));
        doReturn(firefoxRemoteDriver)
            .when(firefoxAction).createFirefoxRemoteWebDriver();

        doNothing()
            .when(ieAction).configureWebDriver(eq(internetExplorerRemoteDriver));
        doReturn(internetExplorerRemoteDriver)
            .when(ieAction).createInternetExplorerRemoteWebDriver();

        doNothing()
            .when(chromeAction).configureWebDriver(eq(chromeRemoteDriver));
        doReturn(chromeRemoteDriver)
            .when(chromeAction).createChromeRemoteWebDriver();

        return new Object[][]{
            {firefoxAction, firefoxRemoteDriver, finallyActionRegistry},
            {ieAction, internetExplorerRemoteDriver, finallyActionRegistry},
            {chromeAction, chromeRemoteDriver, finallyActionRegistry}
        };
    }

    @ParameterizedTest
    @MethodSource("parametersForShould_create_quit_finally_action_and_output_remote_webdriver_when_executed")
    public void should_create_quit_finally_action_and_output_remote_webdriver_when_executed(SeleniumRemoteDriverInitAction action, WebDriver driverToAssert, TestFinallyActionRegistry finallyActionRegistry) {
        reset(finallyActionRegistry);

        // When
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
}
