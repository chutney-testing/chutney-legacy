package com.chutneytesting.action.selenium;

import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.BY;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.SELECTOR;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.VALUE;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WAIT;
import static com.chutneytesting.action.selenium.parameter.SeleniumActionActionParameter.WEBDRIVER;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

public class SeleniumWaitAction extends SeleniumAction implements SeleniumFindBehavior {

    private final String selector;
    private final String by;
    private final Integer wait;
    private final String value;

    public SeleniumWaitAction(Logger logger,
                            @Input(WEBDRIVER) WebDriver webDriver,
                            @Input(SELECTOR) String selector,
                            @Input(BY) String by,
                            @Input(WAIT) Integer wait,
                            @Input(VALUE) String value) {
        super(logger, webDriver);
        this.value = value;
        this.selector = selector;
        this.by = by;
        this.wait = wait;
    }

    @Override
    public ActionExecutionResult executeSeleniumAction() {
        Optional<By> webElement = by(webDriver, selector, by);
        if (webElement.isPresent()) {
            ExpectedByCondition expectedByCondition = new ExpectedByCondition(value);

            Wait explicitWait = new FluentWait<>(webDriver)
                .withTimeout(Duration.of((wait != null ? wait.longValue() : 2L), SECONDS))
                .pollingEvery(Duration.of(500, MILLIS));

            //noinspection unchecked
            explicitWait.until(
                expectedByCondition.toExpectedCondition(webElement.get())
            );
        } else {
            logger.error("Cannot find condition from parameters.");
            return ActionExecutionResult.ko();
        }
        return ActionExecutionResult.ok();
    }

    private class ExpectedByCondition {

        private final Pattern LOGIC_FUNCTION_PATTERN = Pattern.compile("(and|or|not)\\((.*)\\)");

        private final ExpectedByConditionEnum expectedByConditionEnum;
        private final List<ExpectedByCondition> expectedByConditions;

        ExpectedByCondition(String expectedByCondition) {
            Matcher logicFunctionMatcher = LOGIC_FUNCTION_PATTERN.matcher(expectedByCondition);

            if (logicFunctionMatcher.find()) {
                this.expectedByConditionEnum = ExpectedByConditionEnum.findByName(logicFunctionMatcher.group(1));

                this.expectedByConditions =
                    Arrays.stream(logicFunctionMatcher.group(2).split(","))
                        .parallel()
                        .map(ExpectedByCondition::new)
                        .collect(Collectors.toList());
            } else {
                this.expectedByConditionEnum = ExpectedByConditionEnum.findByName(expectedByCondition);
                this.expectedByConditions = new ArrayList<>();
            }
        }

        ExpectedCondition<?> toExpectedCondition(By by) {
            return switch (expectedByConditionEnum) {
                case ELEMENT_TO_BE_SELECTED -> ExpectedConditions.elementToBeSelected(by);
                case ELEMENT_TO_BE_CLICKABLE -> ExpectedConditions.elementToBeClickable(by);
                case FRAME_TO_BE_AVALAIBLE_AND_SWITCH_TO_IT -> ExpectedConditions.frameToBeAvailableAndSwitchToIt(by);
                case INVISIBILITY_OF_ELEMENT_LOCATED -> ExpectedConditions.invisibilityOfElementLocated(by);
                case VISIBILITY_OF_ELEMENT_LOCATED -> ExpectedConditions.visibilityOfElementLocated(by);
                case VISIBILITY_OF_ALL_ELEMENT_LOCATED -> ExpectedConditions.visibilityOfAllElementsLocatedBy(by);
                case PRESENCE_OF_ELEMENT_LOCATED -> ExpectedConditions.presenceOfElementLocated(by);
                case PRESENCE_OF_ALL_ELEMENT_LOCATED -> ExpectedConditions.presenceOfAllElementsLocatedBy(by);
                case AND -> ExpectedConditions.and(
                    (ExpectedCondition<?>[]) expectedByConditions.parallelStream()
                        .map(expectedByCondition -> expectedByCondition.toExpectedCondition(by)).toArray()
                );
                case OR -> ExpectedConditions.or(
                    (ExpectedCondition<?>[]) expectedByConditions.parallelStream()
                        .map(expectedByCondition -> expectedByCondition.toExpectedCondition(by)).toArray()
                );
                case NOT -> ExpectedConditions.not(expectedByConditions.get(0).toExpectedCondition(by));
            };

        }
    }

    private enum ExpectedByConditionEnum {
        ELEMENT_TO_BE_SELECTED("elementToBeSelected"),
        ELEMENT_TO_BE_CLICKABLE("elementToBeClickable"),
        FRAME_TO_BE_AVALAIBLE_AND_SWITCH_TO_IT("frameToBeAvailableAndSwitchToIt"),
        INVISIBILITY_OF_ELEMENT_LOCATED("invisibilityOfElementLocated"),
        VISIBILITY_OF_ELEMENT_LOCATED("visibilityOfElementLocated"),
        VISIBILITY_OF_ALL_ELEMENT_LOCATED("visibilityOfAllElementLocated"),
        PRESENCE_OF_ELEMENT_LOCATED("presenceOfElementLocated"),
        PRESENCE_OF_ALL_ELEMENT_LOCATED("presenceOfAllElementLocated"),
        AND("and"),
        OR("or"),
        NOT("not");

        public final String name;

        ExpectedByConditionEnum(String name) {
            this.name = name;
        }

        static ExpectedByConditionEnum findByName(String name) {
            for (ExpectedByConditionEnum expectedByConditionEnum : ExpectedByConditionEnum.values()) {
                if (expectedByConditionEnum.name.equals(name)) {
                    return expectedByConditionEnum;
                }
            }
            throw new IllegalArgumentException("Unknown ExpectedByConditionEnum name.");
        }
    }
}
