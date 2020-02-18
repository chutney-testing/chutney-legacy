package com.chutneytesting.task.assertion.xml;

import com.google.common.base.Ascii;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class XmlUtils {
    public static Document toDocument(String documentAsString) throws InvalidXmlDocumentException {
        SAXBuilder sxb = new SAXBuilder();
        try {
            return sxb.build(new ByteArrayInputStream(documentAsString.getBytes()));
        } catch (JDOMException | IOException e) {
            throw new InvalidXmlDocumentException(documentAsString);
        }
    }

    public static XPathExpression<Object> compileXPath(String xpath) throws InvalidXPathException {
        try {
            return XPathFactory.instance().compile(xpath);
        } catch (IllegalArgumentException e) {
            throw new InvalidXPathException(xpath);
        }
    }

    @SuppressWarnings("serial")
    public static class InvalidXmlDocumentException extends Exception {
        InvalidXmlDocumentException(String documentAsString) {
            super("Unable to parse XML document: " + Ascii.truncate(documentAsString, 100, "..."));
        }
    }

    @SuppressWarnings("serial")
    public static class InvalidXPathException extends Exception {
        InvalidXPathException(String xpath) {
            super("Unable to compile XPath: " + xpath);
        }
    }
}
