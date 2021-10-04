package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.TaskValidatorsUtils.mapValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.assertion.placeholder.PlaceholderAsserter;
import com.chutneytesting.task.assertion.placeholder.PlaceholderAsserterUtils;
import com.chutneytesting.task.assertion.xml.XmlUtils;
import com.chutneytesting.task.jms.domain.XmlContent;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.filter.ContentFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;

public class XmlAssertTask implements Task {

    private final Logger logger;
    private final String documentAsString;
    private final Map<String, Object> xpathsAndExpectedResults;

    public XmlAssertTask(Logger logger, @Input("document") String documentAsString, @Input("expected") Map<String, Object> xpathsAndExpectedResults) {
        this.logger = logger;
        this.documentAsString = documentAsString;
        this.xpathsAndExpectedResults = xpathsAndExpectedResults;
    }

    @Override
    public List<String> validateInputs() {
        Validator<String> xmlValidation = of(documentAsString)
            .validate(Objects::nonNull, "No document provided")
            .validate(j -> {
                SAXBuilder saxBuilder = new SAXBuilder();
                return new XmlContent(saxBuilder, j).buildDocumentWithoutNamespaces();
            }, noException -> true, "Cannot parse json");
        return getErrorsFrom(xmlValidation, mapValidation(xpathsAndExpectedResults, "expected"));
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = new XmlContent(saxBuilder, documentAsString).buildDocumentWithoutNamespaces();
            boolean assertTrue = true;
            for (Map.Entry<String, Object> xpathAndExpected : xpathsAndExpectedResults.entrySet()) {
                String xpath = xpathAndExpected.getKey();
                Object expected = xpathAndExpected.getValue();
                try {
                    assertTrue = assertTrue && assertXpathMatchExpectation(document, xpath, expected);
                } catch (XmlUtils.InvalidXPathException e) {
                    logger.error(e.getMessage());
                    return TaskExecutionResult.ko();
                }
            }
            return assertTrue ? TaskExecutionResult.ok() : TaskExecutionResult.ko();
        } catch (XmlContent.InvalidXmlDocumentException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }

    private boolean assertXpathMatchExpectation(Document document, String xpath, Object expectedResult) throws XmlUtils.InvalidXPathException {
        XPathExpression<Object> xpathExpression = XmlUtils.compileXPath(xpath);
        String actualResult = convertEvaluationResultToString(xpathExpression.evaluateFirst(document));

        Optional<PlaceholderAsserter> asserts = PlaceholderAsserterUtils.getAsserterMatching(expectedResult);
        if (asserts.isPresent()) {
            return asserts.get().assertValue(logger, actualResult, expectedResult);
        } else if (String.valueOf(expectedResult).equals(actualResult)) {
            logger.info(xpath + " = " + actualResult);
            return true;
        } else {
            logger.error(xpath + " != " + expectedResult + " (found " + actualResult + ")");
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private String convertEvaluationResultToString(Object evaluationResult) {
        if (evaluationResult == null) {
            return null;
        }
        final String evaluatedValueAsString;
        if (evaluationResult instanceof Text) {
            evaluatedValueAsString = ((Text) evaluationResult).getText();
        } else if (evaluationResult instanceof Element) {
            List<Content> contents = ((Element) evaluationResult).getContent((Filter<Content>) new ContentFilter(ContentFilter.COMMENT).negate());
            List<Content> cdata = ((Element) evaluationResult).getContent(new ContentFilter(ContentFilter.CDATA));
            if (contents.size() == 1) {
                evaluatedValueAsString = convertEvaluationResultToString(contents.get(0));
            } else if (cdata.size() == 1) {
                evaluatedValueAsString = ((CDATA) cdata.get(0)).getText();
            } else if (contents.size() == 0) {
                return null;
            } else {
                return "!!!MULTIPLE!";
            }
        } else if (evaluationResult instanceof Attribute) {
            evaluatedValueAsString = ((Attribute) evaluationResult).getValue();
        } else {
            evaluatedValueAsString = String.valueOf(evaluationResult);
        }
        return evaluatedValueAsString;
    }
}
