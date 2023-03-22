package com.chutneytesting.action.assertion;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notEmptyMapValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.action.assertion.placeholder.PlaceholderAsserter;
import com.chutneytesting.action.assertion.placeholder.PlaceholderAsserterUtils;
import com.chutneytesting.action.common.XmlUtils;
import com.chutneytesting.action.jms.domain.XmlContent;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.List;
import java.util.Map;
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

public class XmlAssertAction implements Action {

    private final Logger logger;
    private final String documentAsString;
    private final Map<String, Object> xpathsAndExpectedResults;

    public XmlAssertAction(Logger logger, @Input("document") String documentAsString, @Input("expected") Map<String, Object> xpathsAndExpectedResults) {
        this.logger = logger;
        this.documentAsString = documentAsString;
        this.xpathsAndExpectedResults = xpathsAndExpectedResults;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(documentAsString, "document"),
            notEmptyMapValidation(xpathsAndExpectedResults, "expected")
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            SAXBuilder saxBuilder = XmlUtils.saxBuilder();
            Document document = new XmlContent(saxBuilder, documentAsString).buildDocumentWithoutNamespaces();
            boolean assertTrue = true;
            for (Map.Entry<String, Object> xpathAndExpected : xpathsAndExpectedResults.entrySet()) {
                String xpath = xpathAndExpected.getKey();
                Object expected = xpathAndExpected.getValue();
                try {
                    assertTrue = assertTrue && assertXpathMatchExpectation(document, xpath, expected);
                } catch (XmlUtils.InvalidXPathException e) {
                    logger.error(e.getMessage());
                    return ActionExecutionResult.ko();
                }
            }
            return assertTrue ? ActionExecutionResult.ok() : ActionExecutionResult.ko();
        } catch (XmlContent.InvalidXmlDocumentException e) {
            logger.error(e.getMessage());
            return ActionExecutionResult.ko();
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
        if (evaluationResult instanceof Text text) {
            evaluatedValueAsString = text.getText();
        } else if (evaluationResult instanceof Element element) {
            List<Content> contents = element.getContent((Filter<Content>) new ContentFilter(ContentFilter.COMMENT).negate());
            List<Content> cdata = element.getContent(new ContentFilter(ContentFilter.CDATA));
            if (contents.size() == 1) {
                evaluatedValueAsString = convertEvaluationResultToString(contents.get(0));
            } else if (cdata.size() == 1) {
                evaluatedValueAsString = ((CDATA) cdata.get(0)).getText();
            } else if (contents.size() == 0) {
                return null;
            } else {
                return "!!!MULTIPLE!";
            }
        } else if (evaluationResult instanceof Attribute attribute) {
            evaluatedValueAsString = attribute.getValue();
        } else {
            evaluatedValueAsString = String.valueOf(evaluationResult);
        }
        return evaluatedValueAsString;
    }
}
