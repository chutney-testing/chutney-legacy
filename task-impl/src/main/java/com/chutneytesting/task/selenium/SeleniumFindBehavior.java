package com.chutneytesting.task.selenium;

import com.chutneytesting.task.spi.injectable.Logger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

public interface SeleniumFindBehavior {

    default Optional<WebElement> findElement(Logger logger, WebDriver webDriver, String selector, String by, Integer wait) {
        Optional<By> webElementBy = by(webDriver, selector, by);
        if (webElementBy.isPresent()) {
            try {
                Wait explicitWait = new FluentWait<>(webDriver)
                    .withTimeout(Duration.of((wait != null ? wait : 1L), ChronoUnit.SECONDS))
                    .pollingEvery(Duration.of(500, ChronoUnit.MILLIS))
                    .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

                @SuppressWarnings("unchecked")
                WebElement elementFound = (WebElement) explicitWait.until(
                    findExpectation(webElementBy.get())
                );

                return Optional.of(elementFound);
            } catch (Exception e) {
                logger.error("Cannot retrieve element : " + by + " - " + selector);
                logger.error(e.toString());
            }
        }
        return Optional.empty();
    }

    default Optional<By> by(WebDriver webDriver, String selector, String by) {
        if (selector != null && by != null) {
            return Optional.of(WebElementFindBy
                .findByName(by)
                .by(webDriver, selector)
            );
        }
        return Optional.empty();
    }

    default Function<WebDriver, WebElement> findExpectation(By by) {
        return ExpectedConditions.presenceOfElementLocated(by);
    }

    enum WebElementFindBy {
        ID("id") {
            @Override
            public By by(WebDriver webDriver, String byValue) {
                return By.id(byValue);
            }
        },
        NAME("name") {
            @Override
            public By by(WebDriver webDriver, String byValue) {
                return By.name(byValue);
            }
        },
        CLASS_NAME("className") {
            @Override
            public By by(WebDriver webDriver, String byValue) {
                return By.className(byValue);
            }
        },
        CSS_SELECTOR("cssSelector") {
            @Override
            public By by(WebDriver webDriver, String byValue) {
                return By.cssSelector(byValue);
            }
        },
        XPATH("xpath") {
            @Override
            public By by(WebDriver webDriver, String byValue) {
                return By.xpath(byValue);
            }
        },
        ZK("zk") {
            @Override
            public By by(WebDriver webDriver, String byValue) {
                String zkId = "UNDEFINED";
                try{
                    zkId = (String) ((JavascriptExecutor) webDriver).executeScript(String.format("return zk.Widget.$(jq('$%s')).uuid;", byValue));
                }catch (JavascriptException exception){
                    return by(webDriver,byValue);
                }
                return By.xpath(String.format("//*[@id='%s']", zkId));
            }
        };

        public final String name;

        WebElementFindBy(String name) {
            this.name = name;
        }

        public static WebElementFindBy findByName(String name) {
            for (WebElementFindBy webElementFindBy : WebElementFindBy.values()) {
                if (webElementFindBy.name.equals(name)) {
                    return webElementFindBy;
                }
            }
            throw new IllegalArgumentException("Unknown WebElementFindBy name.");
        }

        abstract public By by(WebDriver webDriver, String byValue);
    }


}
