package com.chutneytesting.action.common;

import com.google.common.base.Ascii;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class XmlUtils {
    private static final String DISABLE_DOCTYPE_DECLARATION = "http://apache.org/xml/features/disallow-doctype-decl";
    public static Document toDocument(String documentAsString) throws InvalidXmlDocumentException {

        try {
            return saxBuilder().build(new ByteArrayInputStream(documentAsString.getBytes()));
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
    public static XPathExpression<Object> compileXPath(String xpath, Map<String, String> nsPrefixes) throws InvalidXPathException {
        try {
            List<Namespace> ns = new ArrayList<>();
            nsPrefixes.forEach((prefix, url) -> ns.add(Namespace.getNamespace(prefix, url)));
            return XPathFactory.instance().compile(xpath, Filters.fpassthrough(), null, ns);
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

    public static SAXBuilder saxBuilder() {
        SAXBuilder builder = new SAXBuilder();
        builder.setFeature(DISABLE_DOCTYPE_DECLARATION, true);
        return builder;
    }
}
