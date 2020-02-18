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

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.TestFinallyActionRegistry;
import com.chutneytesting.task.TestLogger;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

@RunWith(JUnitParamsRunner.class)
public class SeleniumDriverInitTaskTest {

    public static Object[] parametersForShould_create_quit_finally_action_and_output_webdriver_when_executed() {
        TestLogger logger = new TestLogger();
        TestFinallyActionRegistry finallyActionRegistry = spy(new TestFinallyActionRegistry());

        SeleniumDriverInitTask firefoxTask = spy(new SeleniumDriverInitTask(logger, finallyActionRegistry, "", "", "firefox"));
        SeleniumDriverInitTask ieTask = spy(new SeleniumDriverInitTask(logger, finallyActionRegistry, "", "", "Internet Explorer"));

        WebDriver firefoxDriver = mock(FirefoxDriver.class);
        WebDriver internetExplorerDriver = mock(InternetExplorerDriver.class);

        doNothing()
            .when(firefoxTask).configureWebDriver(eq(firefoxDriver));
        doReturn(firefoxDriver)
            .when(firefoxTask).createFirefoxWebDriver();

        doNothing()
            .when(ieTask).configureWebDriver(eq(internetExplorerDriver));
        doReturn(internetExplorerDriver)
            .when(ieTask).createInternetExplorerWebDriver();

        return new Object[][]{
            {firefoxTask, firefoxDriver, finallyActionRegistry},
            {ieTask, internetExplorerDriver, finallyActionRegistry}
        };
    }

    @Parameters
    @Test
    public void should_create_quit_finally_action_and_output_webdriver_when_executed(SeleniumDriverInitTask task, WebDriver driverToAssert, TestFinallyActionRegistry finallyActionRegistry) {
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
