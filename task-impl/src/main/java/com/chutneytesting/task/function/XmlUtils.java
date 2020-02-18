package com.chutneytesting.task.function;

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

class XmlUtils {
    static Document toDocument(String documentAsString) throws InvalidXmlDocumentException {
        SAXBuilder sxb = new SAXBuilder();
        try {
            return sxb.build(new ByteArrayInputStream(documentAsString.getBytes()));
        } catch (JDOMException | IOException e) {
            throw new InvalidXmlDocumentException(documentAsString);
        }
    }

    static XPathExpression<Object> compileXPath(String xpath, Map<String, String> nsPrefixes) throws InvalidXPathException {
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
}
