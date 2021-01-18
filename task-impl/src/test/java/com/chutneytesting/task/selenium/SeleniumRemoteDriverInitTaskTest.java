package com.chutneytesting.task.selenium;

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

import com.chutneytesting.task.TestFinallyActionRegistry;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.TaskExecutionResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class SeleniumRemoteDriverInitTaskTest {

    public static Object[] parametersForShould_create_quit_finally_action_and_output_remote_webdriver_when_executed() {
        TestLogger logger = new TestLogger();
        TestFinallyActionRegistry finallyActionRegistry = spy(new TestFinallyActionRegistry());

        SeleniumRemoteDriverInitTask firefoxTask = spy(new SeleniumRemoteDriverInitTask(logger, finallyActionRegistry, "", "firefox"));
        SeleniumRemoteDriverInitTask ieTask = spy(new SeleniumRemoteDriverInitTask(logger, finallyActionRegistry, "","internet explorer"));
        SeleniumRemoteDriverInitTask chromeTask = spy(new SeleniumRemoteDriverInitTask(logger, finallyActionRegistry, "","chrome"));


        WebDriver firefoxRemoteDriver = mock(FirefoxDriver.class);
        WebDriver internetExplorerRemoteDriver = mock(InternetExplorerDriver.class);
        WebDriver chromeRemoteDriver = mock(ChromeDriver.class);

        doNothing()
            .when(firefoxTask).configureWebDriver(eq(firefoxRemoteDriver));
        doReturn(firefoxRemoteDriver)
            .when(firefoxTask).createFirefoxRemoteWebDriver();

        doNothing()
            .when(ieTask).configureWebDriver(eq(internetExplorerRemoteDriver));
        doReturn(internetExplorerRemoteDriver)
            .when(ieTask).createInternetExplorerRemoteWebDriver();

        doNothing()
            .when(chromeTask).configureWebDriver(eq(chromeRemoteDriver));
        doReturn(chromeRemoteDriver)
            .when(chromeTask).createChromeRemoteWebDriver();

        return new Object[][]{
            {firefoxTask, firefoxRemoteDriver, finallyActionRegistry},
            {ieTask, internetExplorerRemoteDriver, finallyActionRegistry},
            {chromeTask, chromeRemoteDriver, finallyActionRegistry}
        };
    }

    @ParameterizedTest
    @MethodSource("parametersForShould_create_quit_finally_action_and_output_remote_webdriver_when_executed")
    public void should_create_quit_finally_action_and_output_remote_webdriver_when_executed(SeleniumRemoteDriverInitTask task, WebDriver driverToAssert, TestFinallyActionRegistry finallyActionRegistry) {
        reset(finallyActionRegistry);

        // When
        TaskExecutionResult result = task.execute();

        // Then
        verify(finallyActionRegistry, times(1)).registerFinallyAction(any());
        FinallyAction quitAction = finallyActionRegistry.finallyActions.get(0);
        assertThat(quitAction.actionIdentifier()).isEqualTo("selenium-quit");
        assertThat(quitAction.inputs()).containsOnlyKeys("web-driver");

        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat(result.outputs).containsOnlyKeys("webDriver");
        assertThat(result.outputs).containsValues(driverToAssert);
        assertThat(result.outputs.get("webDriver")).isInstanceOf(driverToAssert.getClass());
    }
}
