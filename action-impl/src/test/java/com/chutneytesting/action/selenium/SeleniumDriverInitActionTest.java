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

public class SeleniumDriverInitActionTest {

    public static Object[] parametersForShould_create_quit_finally_action_and_output_webdriver_when_executed() {
        TestLogger logger = new TestLogger();
        TestFinallyActionRegistry finallyActionRegistry = spy(new TestFinallyActionRegistry());

        SeleniumDriverInitAction firefoxAction = spy(new SeleniumDriverInitAction(logger, finallyActionRegistry, "", "", "firefox", null, null, null, null));
        SeleniumDriverInitAction ieAction = spy(new SeleniumDriverInitAction(logger, finallyActionRegistry, "", "", "internet explorer", null, null, null, null));
        SeleniumDriverInitAction chromeAction = spy(new SeleniumDriverInitAction(logger, finallyActionRegistry, "", "", "chrome", null, null, null, null));

        WebDriver firefoxDriver = mock(FirefoxDriver.class);
        WebDriver internetExplorerDriver = mock(InternetExplorerDriver.class);
        WebDriver chromeDriver = mock(ChromeDriver.class);

        doNothing()
            .when(firefoxAction).configureWebDriver(eq(firefoxDriver));
        doReturn(firefoxDriver)
            .when(firefoxAction).createFirefoxWebDriver();
        doReturn(chromeDriver)
            .when(chromeAction).createChromeWebDriver();

        doNothing()
            .when(ieAction).configureWebDriver(eq(internetExplorerDriver));
        doReturn(internetExplorerDriver)
            .when(ieAction).createInternetExplorerWebDriver();

        return new Object[][]{
            {firefoxAction, firefoxDriver, finallyActionRegistry},
            {ieAction, internetExplorerDriver, finallyActionRegistry},
            {chromeAction, chromeDriver, finallyActionRegistry}
        };
    }

    @ParameterizedTest
    @MethodSource("parametersForShould_create_quit_finally_action_and_output_webdriver_when_executed")
    public void should_create_quit_finally_action_and_output_webdriver_when_executed(SeleniumDriverInitAction action, WebDriver driverToAssert, TestFinallyActionRegistry finallyActionRegistry) {
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
